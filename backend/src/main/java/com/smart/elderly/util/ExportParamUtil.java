package com.smart.elderly.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class ExportParamUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> parseParams(String exportParams) {
        if (exportParams == null || exportParams.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(exportParams, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public static <T> T getParam(Map<String, Object> params, String key, Class<T> clazz) {
        if (params == null || !params.containsKey(key)) {
            return null;
        }
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        try {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.valueOf(value.toString()));
            }
            if (clazz == Long.class) {
                return clazz.cast(Long.valueOf(value.toString()));
            }
            if (clazz == String.class) {
                return clazz.cast(value.toString());
            }
            if (clazz == Boolean.class) {
                return clazz.cast(Boolean.valueOf(value.toString()));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String getStringParam(Map<String, Object> params, String key) {
        return getParam(params, key, String.class);
    }

    public static Integer getIntegerParam(Map<String, Object> params, String key) {
        return getParam(params, key, Integer.class);
    }

    public static Boolean getBooleanParam(Map<String, Object> params, String key) {
        return getParam(params, key, Boolean.class);
    }
}
