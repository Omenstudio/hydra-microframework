package com.github.omenstudio.hydra.utils;

import com.github.omenstudio.hydra.annotation.JsonExclude;
import com.github.omenstudio.hydra.annotation.model.HydraLink;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class AnnotationJsonExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(JsonExclude.class) != null || f.getAnnotation(HydraLink.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
