package com.github.omenstudio.hydra.aspect;

import com.github.omenstudio.hydra.annotation.HydraEntity;
import com.github.omenstudio.hydra.annotation.HydraLink;
import com.github.omenstudio.hydra.utils.AnnotationJsonExclusionStrategy;
import com.github.omenstudio.hydra.utils.HydraUrlResolver;
import com.google.gson.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

@Aspect
@Component
public class HydraRequestHandler {

    private static Logger logger = LoggerFactory.getLogger(HydraRequestHandler.class);

    private static final Gson gsonBuilder;

    private static final JsonParser gsonParser;

    static {
        gsonBuilder = new GsonBuilder()
                .setExclusionStrategies(new AnnotationJsonExclusionStrategy())
                .create();
        gsonParser = new JsonParser();
    }

    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.request.HydraGetRequest)")
    public void hydraGetRequest() {
    }

    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.request.HydraPostRequest)")
    public void hydraPostRequest() {
    }

    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.request.HydraPutRequest)")
    public void hydraPutRequest() {
    }

    @Around("hydraGetRequest() || hydraPostRequest() || hydraPutRequest()")
    public Object makeHydraResponseForGet(ProceedingJoinPoint thisJoinPoint) {
        String methodName = thisJoinPoint.getSignature().getName();

        Object objectFromController = null;
        try {
            objectFromController = thisJoinPoint.proceed();
            if (objectFromController == null)
                throw new NullPointerException("object returned from controller method (" + methodName + ") is null");
        } catch (Throwable throwable) {
            return ResponseEntity.notFound();
        }

        String response = buildResponse(objectFromController);

        return ResponseEntity.ok()
                .header("Access-Control-Expose-Headers", "Link")
                .header("Link", "<" + HydraUrlResolver.getVocabAddress() + ">; " +
                        "rel=\"http://www.w3.org/ns/hydra/core#apiDocumentation\"")
                .body(response);
    }

    /**
     * Transforms what we get from Web MVC controller
     * to JSON-LD format with additional properties
     *
     * @param objectFromController - object returned by Web MVC controller
     * @return response body representation in JSON-LD
     * @see #serializeCollection
     * @see #serializeEntityFully
     */
    private String buildResponse(Object objectFromController) {
        if (objectFromController instanceof Collection) {
            return serializeCollection(((Collection) objectFromController));
        }

        String entitySerialized = serializeEntityFully(objectFromController);
        if (entitySerialized != null) {
            return entitySerialized;
        }

        return objectFromController.toString();
    }


    /**
     * Serialize collection of entities returned by Web MVC Controller
     *
     * @param entityCollection
     * @return
     */
    private String serializeCollection(Collection entityCollection) {
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
    private String serializeEntityFully(Object entityObject) {
        HydraEntity hydraAnnotation = entityObject.getClass().getDeclaredAnnotation(HydraEntity.class);

        if (hydraAnnotation == null)
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
        for (Field field : entityObject.getClass().getDeclaredFields()) {
            if (field.getDeclaredAnnotation(HydraLink.class) == null)
                continue;

            try {
                field.setAccessible(true);
                resultJson.add(field.getName(), serializeLinkTo(field.get(entityObject)));
            } catch (IllegalAccessException | ClassCastException e) {
                logger.error("#serializeEntityFully: " + e.toString());
            }
        }


        return resultJson.toString();
    }

    private static JsonElement serializeLinkTo(Object object) {
        if (object instanceof Collection) {
            return serializeLinkToCollection(((Collection) object));
        }

        return serializeLinkToEntity(object);
    }


    private static JsonElement serializeLinkToCollection(Collection entityCollection) {
        JsonArray answer = new JsonArray();

        for (Object entity : entityCollection) {
            answer.add(serializeLinkToEntity(entity));
        }

        return answer;
    }

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
            logger.error("#serializeLinkToEntity: " + e.toString());
        }

        JsonObject itemJsonObject = new JsonObject();
        itemJsonObject.addProperty("@id", itemPathId + Long.toString(id));
        itemJsonObject.addProperty("@type", itemType);


        return itemJsonObject;
    }

}