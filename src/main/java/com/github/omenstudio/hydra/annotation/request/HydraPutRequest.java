package com.github.omenstudio.hydra.annotation.request;


import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CrossOrigin
@RequestMapping(method = RequestMethod.PUT, produces = "application/ld+json")
public @interface HydraPutRequest {
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};
}
