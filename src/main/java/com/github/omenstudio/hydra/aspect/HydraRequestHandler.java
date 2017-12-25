package com.github.omenstudio.hydra.aspect;

import com.github.omenstudio.hydra.builder.JsonLdBuilder;
import com.github.omenstudio.hydra.utils.HydraUrlResolver;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class HydraRequestHandler {

    @Autowired
    JsonLdBuilder jsonLdBuilder;

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

        String response = jsonLdBuilder.buildResponse(objectFromController);

        return ResponseEntity.ok()
                .header("Access-Control-Expose-Headers", "Link")
                .header("Link", "<" + HydraUrlResolver.getVocabAddress() + ">; " +
                        "rel=\"http://www.w3.org/ns/hydra/core#apiDocumentation\"")
                .body(response);
    }



}