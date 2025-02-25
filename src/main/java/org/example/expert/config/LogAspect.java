package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

@Aspect
@Slf4j
@Component
public class LogAspect {
    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public void adminController() {}


    @Around("adminController()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        ObjectMapper requestObjectMapper = new ObjectMapper();

        Map<String, Object> requestData = new HashMap<>();
        log.info("userId : {}", request.getAttribute("userId"));
        log.info("time : {}", LocalDateTime.now());
        log.info("url : {}", request.getRequestURL().toString());

        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
        String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);


        requestData.put("requestBody", requestObjectMapper.readValue(requestBody, Map.class));

        log.info(requestObjectMapper.writeValueAsString(requestData));

        Object result = joinPoint.proceed();
        ObjectMapper responseObjectMapper = new ObjectMapper();

        Map<String, Object> responseData = new HashMap<>();
        log.info("userId : {}", request.getAttribute("userId"));
        log.info("time : {}", LocalDateTime.now());
        log.info("url : {}", request.getRequestURL().toString());
        if (result != null) {
            responseData.put("responseBody", responseObjectMapper.readValue(result.toString(), Map.class));
            log.info(responseObjectMapper.writeValueAsString(responseData));
        }


        return result;
    }
}
