package com.smart.elderly.export;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.util.ExportParamUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseExportProvider<T> implements ExportDataProvider<T> {

    @Autowired
    protected ElderlyMapper elderlyMapper;

    protected Map<String, Object> parseExportParams(String exportParams) {
        return ExportParamUtil.parseParams(exportParams);
    }

    protected String getStringParam(Map<String, Object> params, String key) {
        return ExportParamUtil.getStringParam(params, key);
    }

    protected Integer getIntegerParam(Map<String, Object> params, String key) {
        return ExportParamUtil.getIntegerParam(params, key);
    }

    protected Boolean getBooleanParam(Map<String, Object> params, String key) {
        return ExportParamUtil.getBooleanParam(params, key);
    }

    protected Map<Integer, String> getElderlyNameMap() {
        Map<Integer, String> elderlyNameMap = new HashMap<>();
        List<Elderly> elderlyList = elderlyMapper.selectList(new QueryWrapper<>());
        for (Elderly e : elderlyList) {
            elderlyNameMap.put(e.getId(), e.getName());
        }
        return elderlyNameMap;
    }

    protected String generateFileName(String prefix) {
        return prefix + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
