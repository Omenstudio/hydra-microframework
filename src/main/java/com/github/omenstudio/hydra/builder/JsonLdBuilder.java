package com.github.omenstudio.hydra.builder;

import com.github.omenstudio.hydra.annotation.HydraEntity;
import com.github.omenstudio.hydra.annotation.HydraField;
import com.github.omenstudio.hydra.annotation.HydraLink;
import com.github.omenstudio.hydra.utils.AnnotationJsonExclusionStrategy;
import com.github.omenstudio.hydra.utils.HydraUrlResolver;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;


@Slf4j
@Component
public class JsonLdBuilder {

    private final Gson gsonBuilder;

    private final JsonParser gsonParser;



    public JsonLdBuilder() {
        gsonBuilder = new GsonBuilder()
                .setExclusionStrategies(new AnnotationJsonExclusionStrategy())
                .create();
        gsonParser = new JsonParser();
    }


    /**
     * Transforms what we get from Web MVC controller
     * to JSON-LD format with additional properties
     *
     * @param objectFromController - object returned by Web MVC controller
     * @return response body representation in JSON-LD
     * @see #buildResponseForCollection
     * @see #buildResponseForEntity
     */
    public String buildResponse(Object objectFromController) {
        // Depending from object type we need to call different methods

        // Collection
        if (objectFromController instanceof Collection) {
            return buildResponseForCollection(((Collection) objectFromController));
        }

        // HydraEntity
        String entitySerialized = buildResponseForEntity(objectFromController);
        if (entitySerialized != null) {
            return entitySerialized;
        }

        // If can't serialize, just return pure toString()
        return objectFromController.toString();
    }


    /**
     * Serialize collection of entities returned by Web MVC Controller
     *
     * @param entityCollection
     * @return
     */
    private String buildResponseForCollection(Collection entityCollection) {
        if (entityCollection.isEmpty()) {
            return "[]";
        }

        JsonObject resultJson = new JsonObject();
        JsonArray membersJson = new JsonArray();
        boolean inited = false;

        for (Object obj : entityCollection) {
            if (!inited) {
                String className = obj.getClass().getSimpleName();
                String itemPathId = HydraUrlResolver.getApiPath() + "/" + className.toLowerCase() + "s/";
                resultJson.addProperty("@id", itemPathId);
                resultJson.addProperty("@context", HydraUrlResolver.getApiPath() + "/contexts/" + className + "Collection");
                resultJson.addProperty("@type", className + "Collection");

                inited = true;
            }

            membersJson.add(serializeLinkToEntity(obj));
        }

        resultJson.add("members", membersJson);

        return resultJson.toString();
    }


    /**
     * <p>
     * Serializes single entity. Add properties @id, @context, @type,
     * which calculated based on class name.
     * <p>
     * <p>
     * For example:
     * <pre>
     * {
     *     "@context": "/hydra/event-api/contexts/Event.jsonld",
     *     "@id": "/hydra/event-api/events/117",
     *     "@type": "Event",
     *     "name": "HOST",
     *     "description": "the host",
     *     "start_date": "2017-11-18T13:58:11Z",
     *     "end_date": "2017-11-18T14:58:11Z"
     * }
     * </pre>
     *
     * @param entityObject Potential entity object, which can be presented in JSON-LD format
     * @return null if object is not HydraEntity, String representing json-ld object representation elsewhere
     * @see HydraEntity
     */
    private String buildResponseForEntity(Object entityObject) {
        HydraEntity hydraEntityAnnotation = entityObject.getClass().getDeclaredAnnotation(HydraEntity.class);

        if (hydraEntityAnnotation == null)
            return null;

        String className = entityObject.getClass().getSimpleName();
        long objectId = 0;
        try {
            Field field = entityObject.getClass().getDeclaredField("id");
            field.setAccessible(true);
            objectId = ((long) field.get(entityObject));
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {

        }

        JsonObject resultJson = gsonParser.parse(gsonBuilder.toJson(entityObject)).getAsJsonObject();
        resultJson.addProperty("@id", HydraUrlResolver.getApiPath() + "/" + className.toLowerCase() + "s/" + objectId);
        resultJson.addProperty("@context", HydraUrlResolver.getApiPath() + "/contexts/" + className);
        resultJson.addProperty("@type", className);

        // For each field, which must be serialized
        for (Field entityField : entityObject.getClass().getDeclaredFields()) {
            if (entityField.getDeclaredAnnotation(HydraLink.class) == null)
                continue;

            try {
                entityField.setAccessible(true);
                resultJson.add(entityField.getName(), serializeLinkTo(entityField.get(entityObject)));
            } catch (IllegalAccessException | ClassCastException e) {
                log.error("#buildResponseForEntity: " + e.toString());
            }
        }


        return resultJson.toString();
    }


    /**
     * Common method for serializing link to some object: entity of collection
     *
     * @param object
     * @return
     *
     * @see #serializeLinkToCollection
     * @see #serializeLinkToEntity
     */
    private static JsonElement serializeLinkTo(Object object) {
        if (object instanceof Collection) {
            return serializeLinkToCollection(((Collection) object));
        }

        return serializeLinkToEntity(object);
    }


    /**
     *
     *
     *
     * @param entityCollection
     * @return
     */
    private static JsonElement serializeLinkToCollection(Collection entityCollection) {
        JsonArray answer = new JsonArray();

        for (Object entity : entityCollection) {
            answer.add(serializeLinkToEntity(entity));
        }

        return answer;
    }


    /**
     * @param entityObject
     * @return
     */
    private static JsonObject serializeLinkToEntity(Object entityObject) {
        if (entityObject == null)
            return null;

        HydraEntity annotation = entityObject.getClass().getDeclaredAnnotation(HydraEntity.class);

        String className = entityObject.getClass().getSimpleName();
        String itemPathId = HydraUrlResolver.getApiPath() + "/" + className.toLowerCase() + "s/";
        String itemType = annotation != null ? annotation.value() : "http://schema.org/" + className;

        long id = 0;
        try {
            Field idField = entityObject.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            id = ((long) idField.get(entityObject));
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            log.error("#serializeLinkToEntity: " + e.toString());
        }

        JsonObject itemJsonObject = new JsonObject();
        itemJsonObject.addProperty("@id", itemPathId + Long.toString(id));
        itemJsonObject.addProperty("@type", itemType);

        // And we need to serialize additional properties,
        // which marked by includeInCollection property by HydraField annotation
        for (Field entityField : entityObject.getClass().getDeclaredFields()) {
            HydraField hydraFieldAnnotation = entityField.getDeclaredAnnotation(HydraField.class);
            HydraLink hydraLinkAnnotation = entityField.getDeclaredAnnotation(HydraLink.class);

            // Skip if don't need to include in collection
            if (!(hydraFieldAnnotation != null && hydraFieldAnnotation.includeInCollection() ||
                    hydraLinkAnnotation != null && hydraLinkAnnotation.includeInCollection())) {
                continue;
            }

            entityField.setAccessible(true);
            String key = entityField.getName();
            Object value;

            try {
                value = entityField.get(entityObject);
            } catch (IllegalAccessException e) {
                continue;
            }

            // If we trying to serialize HydraLink -> call proprietary method
            if (hydraLinkAnnotation == null) {
                itemJsonObject.addProperty(key, value.toString());
            }
            else {
                itemJsonObject.add(key, serializeLinkTo(value));
            }

        }


        return itemJsonObject;
    }

}
