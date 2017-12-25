package com.github.omenstudio.hydra.aspect;


import com.github.omenstudio.hydra.builder.ContextBuilder;
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
@Order(3)
@Component
public class HydraContextAspect {

    @Autowired
    private ContextBuilder contextBuilder;


    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.context.HydraContextEntryPoint)")
    public void requestForEntryPoint() {}


    @SneakyThrows
    @Around("requestForEntryPoint()")
    public Object makeResponseForEntryPoint(ProceedingJoinPoint joinPoint) {
        Object returnedFromJoinPoint = joinPoint.proceed();

        String[] params = ((String[]) returnedFromJoinPoint);

        return contextBuilder.buildForEntryPoint(params);
    }


    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.context.HydraContextClass)")
    public void requestForClass() {}


    @SneakyThrows
    @Around("requestForClass()")
    public Object makeResponseForClass(ProceedingJoinPoint joinPoint) {
        return contextBuilder.buildForClass(((Class) joinPoint.proceed()));
    }


    @Pointcut("@annotation(com.github.omenstudio.hydra.annotation.context.HydraContextCollection)")
    public void requestForCollection() {}


    @SneakyThrows
    @Around("requestForCollection()")
    public Object makeResponseForCollection(ProceedingJoinPoint joinPoint) {
        return contextBuilder.buildForCollection(((Class) joinPoint.proceed()));
    }
}
