package com.github.omenstudio.hydra.utils;

import com.github.omenstudio.hydra.annotation.model.HydraEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;


public class HydraUrlResolver {

    @Getter
    @Setter
    private static String serverAddress;

    @Getter
    @Setter
    private static String apiAddress;

    @Getter
    @Setter
    private static String vocabAddress;

    @Getter
    @Setter
    private static String contextsAddress;


    /**
     * <p>
     * Gets path to item's collection.
     *
     * <p>
     * Takes value from HydraEntity annotation
     * or calculate custom, based on the class simple name.
     *
     * @param collectionItem any object from collection
     * @return
     *
     * @see HydraEntity
     */
    public static String getPathToCollection(Object collectionItem) {
        HydraEntity hydraEntityAnnotation = collectionItem.getClass().getDeclaredAnnotation(HydraEntity.class);

        if (hydraEntityAnnotation != null && !hydraEntityAnnotation.pathToCollection().isEmpty()) {
            return apiAddress + hydraEntityAnnotation.pathToCollection();
        }

        return apiAddress + "/" + collectionItem.getClass().getSimpleName().toLowerCase() + "s/";
    }


    @SneakyThrows
    public static String getPathToEntity(Object entityObject) {
        // Let's find object #ID
        Object objectId = 0;
        Field field = entityObject.getClass().getDeclaredField("id");
        field.setAccessible(true);
        objectId = field.get(entityObject);

        // If programmer has setup the url to item
        HydraEntity hydraEntityAnnotation = entityObject.getClass().getDeclaredAnnotation(HydraEntity.class);
        if (hydraEntityAnnotation != null && !hydraEntityAnnotation.pathToEntity().isEmpty()) {
            return apiAddress + hydraEntityAnnotation.pathToEntity() + objectId.toString();
        }

        // Default value
        return apiAddress + "/" + entityObject.getClass().getSimpleName().toLowerCase() + "s/" + objectId.toString();
    }



}
