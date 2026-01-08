package com.sr_banking.banking_project.aspect;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RepositoryTracingAspect {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryTracingAspect.class);

    @Autowired
    private Tracer tracer;

    @Around("execution(* com.sr_banking.banking_project.repository.*.*(..))")
    public Object traceRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String spanName = className + "." + methodName;

        Span span = tracer.spanBuilder(spanName)
                .startSpan();

        try {
            span.setAttribute("repository.class", className);
            span.setAttribute("repository.method", methodName);

            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                span.setAttribute("repository.args", Arrays.toString(args));
                logger.debug("Repository call: {}.{}({})", className, methodName, Arrays.toString(args));
            } else {
                logger.debug("Repository call: {}.{}()", className, methodName);
            }

            Object result = joinPoint.proceed();

            if (result != null) {
                span.setAttribute("repository.resultType", result.getClass().getSimpleName());
                if (result instanceof java.util.Collection) {
                    span.setAttribute("repository.resultSize", ((java.util.Collection<?>) result).size());
                }
            }

            span.setStatus(StatusCode.OK);
            logger.debug("Repository call completed: {}.{}", className, methodName);

            return result;

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Repository call failed: {}.{} - {}", className, methodName, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
