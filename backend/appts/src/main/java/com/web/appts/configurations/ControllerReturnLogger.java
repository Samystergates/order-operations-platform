package com.web.appts.configurations;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerReturnLogger {
    private static final Logger logger = LoggerFactory.getLogger(ControllerReturnLogger.class);

    @AfterReturning(pointcut = "execution(* com.web.appts.controllers..*(..))", returning = "result")
    public void logControllerReturn(JoinPoint joinPoint, Object result) {
        if (result instanceof java.util.Collection) {
            logger.debug("Returned collection from: {} | Size: {}",
                    joinPoint.getSignature().toShortString(),
                    ((java.util.Collection<?>) result).size());
        } else {
            logger.debug("Returned from: {}", joinPoint.getSignature().toShortString());
        }
    }
}
