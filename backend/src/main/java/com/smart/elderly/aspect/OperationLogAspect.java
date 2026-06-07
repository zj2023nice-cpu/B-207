package com.smart.elderly.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "pwd", "passwd", "oldPassword", "newPassword", "confirmPassword"
    ));

    private static final String MASK_VALUE = "******";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";

    @Around("@annotation(com.smart.elderly.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;

        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long time = System.currentTimeMillis() - beginTime;
            saveOperationLog(point, result, exception, time);
        }
    }

    private void saveOperationLog(ProceedingJoinPoint point, Object result, Throwable exception, long time) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        com.smart.elderly.entity.OperationLog operationLog = new com.smart.elderly.entity.OperationLog();

        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
        if (operationLogAnnotation != null) {
            operationLog.setOperation(operationLogAnnotation.operation());
            operationLog.setDescription(operationLogAnnotation.description());
        }

        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        operationLog.setMethod(className + "." + methodName + "()");

        Object[] args = point.getArgs();
        try {
            String params = serializeAndMaskSensitiveData(args);
            if (params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
            operationLog.setParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        extractUserInfo(args, operationLog);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setIp(getIpAddr(request));
            
            if (operationLog.getUserId() == null) {
                String userIdStr = request.getHeader(USER_ID_HEADER);
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        operationLog.setUserId(Integer.parseInt(userIdStr));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            if (operationLog.getUsername() == null) {
                String username = request.getHeader(USERNAME_HEADER);
                if (username != null && !username.isEmpty()) {
                    operationLog.setUsername(username);
                }
            }
        }

        if (operationLog.getUserId() == null) {
            Integer contextUserId = UserContextHolder.getUserId();
            if (contextUserId != null) {
                operationLog.setUserId(contextUserId);
            }
        }
        if (operationLog.getUsername() == null) {
            String contextUsername = UserContextHolder.getUsername();
            if (contextUsername != null) {
                operationLog.setUsername(contextUsername);
            }
        }

        operationLog.setOperationTime(LocalDateTime.now());

        boolean success = true;
        String errorMsg = null;

        if (exception != null) {
            success = false;
            errorMsg = exception.getMessage();
        } else if (result != null) {
            try {
                JsonNode resultNode = objectMapper.valueToTree(result);
                if (resultNode.has("code")) {
                    int code = resultNode.get("code").asInt();
                    if (code != 200) {
                        success = false;
                        if (resultNode.has("message")) {
                            errorMsg = resultNode.get("message").asText();
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        operationLog.setSuccess(success);
        if (!success && errorMsg != null) {
            if (errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500) + "...";
            }
            operationLog.setErrorMsg(errorMsg);
        }

        operationLogService.save(operationLog);
    }

    private String serializeAndMaskSensitiveData(Object[] args) throws Exception {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        JsonNode rootNode = objectMapper.valueToTree(args);
        maskSensitiveFields(rootNode);
        return objectMapper.writeValueAsString(rootNode);
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (SENSITIVE_FIELDS.contains(fieldName.toLowerCase())) {
                    objectNode.put(fieldName, MASK_VALUE);
                } else {
                    maskSensitiveFields(objectNode.get(fieldName));
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                maskSensitiveFields(element);
            }
        }
    }

    private void extractUserInfo(Object[] args, com.smart.elderly.entity.OperationLog operationLog) {
        for (Object arg : args) {
            if (arg == null) continue;
            
            try {
                JsonNode node = objectMapper.valueToTree(arg);
                
                if (node.has("username") && !node.get("username").isNull()) {
                    operationLog.setUsername(node.get("username").asText());
                }
                if (node.has("userId") && !node.get("userId").isNull()) {
                    try {
                        operationLog.setUserId(node.get("userId").asInt());
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
