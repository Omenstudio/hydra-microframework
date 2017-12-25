package com.github.omenstudio.hydra.aspect;

import com.github.omenstudio.hydra.builder.ResponseBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
public class HydraControllerAspect {

    @Autowired
    private ResponseBuilder responseBuilder;


    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.request.HydraGetRequest)")
    public void hydraGetRequest() {
    }


    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.request.HydraPostRequest)")
    public void hydraPostRequest() {
    }


    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.request.HydraPutRequest)")
    public void hydraPutRequest() {
    }


    @SneakyThrows
    @Around("hydraGetRequest() || hydraPostRequest() || hydraPutRequest()")
    public Object makeHydraResponseForGet(ProceedingJoinPoint thisJoinPoint) {

        Object objectFromController = thisJoinPoint.proceed();


        return responseBuilder.buildResponse(objectFromController);
    }

}