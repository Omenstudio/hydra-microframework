package com.github.omenstudio.hydra.builder;

import com.github.omenstudio.hydra.annotation.HydraEntity;
import com.github.omenstudio.hydra.annotation.HydraField;
import com.github.omenstudio.hydra.annotation.HydraLink;
import com.github.omenstudio.hydra.utils.HydraUrlResolver;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;


public class ContextBuilder {

    private final static Logger logger = LoggerFactory.getLogger(ContextBuilder.class);

    // doc this
    public static JsonObject buildForEntryPoint(String... links) {
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("EntryPoint", "vocab:EntryPoint");

        for (String link : links) {
            JsonObject linkObject = new JsonObject();
            linkObject.addProperty("@id", "vocab:EntryPoint/" + link);
            linkObject.addProperty("@type", "@id");
            resultJson.add(link, linkObject);
        }

        return wrapContext(resultJson);
    }

    // doc this
    public static JsonObject buildForCollection(Class collectionItemClass) {
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty(
                collectionItemClass.getSimpleName() + "Collection",
                "vocab:" + collectionItemClass.getSimpleName() + "Collection"
        );
        resultJson.addProperty("members", "http://www.w3.org/ns/hydra/core#member");

        return wrapContext(resultJson);
    }

    // doc this
    public static JsonObject buildForClass(Class beanClass) {
        JsonObject resultJson = new JsonObject();

        // At first we write info about class
        resultJson = buildInfoAboutEntity(beanClass, resultJson);

        // Then we write info about each field
        resultJson = buildInfoAboutFields(beanClass, resultJson);

        // and in the end add some preparations
        return wrapContext(resultJson);
    }

    // doc this
    private static JsonObject buildInfoAboutEntity(Class beanClass, JsonObject jsonObject) {
        Annotation hydraTypeAnnotation = beanClass.getDeclaredAnnotation(HydraEntity.class);

        if (hydraTypeAnnotation == null) {
            logger.error("Can't build context for class " + beanClass.toString() + ". " +
                    "There are no @HydraField annotation on class");
            return jsonObject;
        }

        jsonObject.addProperty(beanClass.getSimpleName(), ((HydraEntity) hydraTypeAnnotation).value());
        return jsonObject;
    }

    // doc this
    private static JsonObject buildInfoAboutFields(Class beanClass, JsonObject jsonObject) throws NullPointerException {

        for (Field field : beanClass.getDeclaredFields()) {

            // Build context only for fields, which have annotation @HydraType
            buildInfoAboutHydraTypeField(field, beanClass, jsonObject);

            // Build context for field, which linked to some entity
            buildInfoAboutFieldWithLink(field, beanClass, jsonObject);

        }

        return jsonObject;
    }

    private static void buildInfoAboutHydraTypeField(Field field, Class beanClass, JsonObject resultObject) {
        HydraField annotation = field.getDeclaredAnnotation(HydraField.class);
        if (annotation == null)
            return;

        String[] values = annotation.value();
        String[] keys = new String[0];

        // If annotation has been used with different keys/values -> skip
        if (values.length == 0 || values.length > 1 && values.length != keys.length) {
            logger.warn("Can't build context for field " + field.getName() +
                    " of class " + field.getDeclaringClass().toString() + ". " +
                    "Illegal @HydraField annotation arguments");
            return;
        }

        // If values[] has 1 value, but keys hasn't => key is field name
        if (keys.length == 0) {
            resultObject.addProperty(field.getName(), values[0]);
        }
        // Elsewhere build additional inner json object
        else {
            JsonObject innerObject = new JsonObject();
            for (int i = 0; i < keys.length; i++) {
                innerObject.addProperty(keys[i], values[i]);
            }
            resultObject.add(field.getName(), innerObject);
        }
    }


    private static void buildInfoAboutFieldWithLink(Field field, Class beanClass, JsonObject resultJson) {
        HydraLink annotation = field.getDeclaredAnnotation(HydraLink.class);
        if (annotation == null)
            return;

        resultJson.addProperty(field.getName(), annotation.value());
    }

    private static JsonObject wrapContext(JsonObject contextInfo) {
        contextInfo.addProperty("hydra", "http://www.w3.org/ns/hydra/core#");
        contextInfo.addProperty("vocab", HydraUrlResolver.getVocabAddress() + "#");

        JsonObject contextWrapper = new JsonObject();
        contextWrapper.add("@context", contextInfo);
        return contextWrapper;
    }
}
