package com.smart.elderly.export.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.export.BaseExportProvider;
import com.smart.elderly.export.WarningRecordExcelVO;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WarningRecordExportProvider extends BaseExportProvider<WarningRecordExcelVO> {

    @Autowired
    private HealthWarningRecordMapper warningRecordMapper;

    private static final Map<String, String> INDICATOR_TYPE_MAP = new HashMap<>();
    static {
        INDICATOR_TYPE_MAP.put("temperature", "体温");
        INDICATOR_TYPE_MAP.put("systolicPressure", "收缩压");
        INDICATOR_TYPE_MAP.put("diastolicPressure", "舒张压");
        INDICATOR_TYPE_MAP.put("heartRate", "心率");
        INDICATOR_TYPE_MAP.put("bloodOxygen", "血氧");
        INDICATOR_TYPE_MAP.put("bloodSugar", "血糖");
    }

    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        for (com.smart.elderly.enums.HealthWarningStatus status : com.smart.elderly.enums.HealthWarningStatus.values()) {
            STATUS_MAP.put(status.getCode(), status.getDisplayName());
        }
        STATUS_MAP.put(com.smart.elderly.enums.HealthWarningStatus.INVALIDATED_CODE, "已失效");
    }

    @Override
    public String getExportType() {
        return "WARNING_RECORD";
    }

    @Override
    public String getExportTypeDesc() {
        return "预警记录";
    }

    @Override
    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("老人姓名");
        headers.add("指标类型");
        headers.add("实际值");
        headers.add("阈值");
        headers.add("预警级别");
        headers.add("预警消息");
        headers.add("状态");
        headers.add("创建时间");
        headers.add("处理时间");
        headers.add("处理人");
        headers.add("处理备注");
        return headers;
    }

    @Override
    public List<WarningRecordExcelVO> fetchData(String exportParams) {
        Map<String, Object> params = parseExportParams(exportParams);
        String status = getStringParam(params, "status");
        Integer elderlyId = getIntegerParam(params, "elderlyId");
        String indicatorType = getStringParam(params, "indicatorType");

        QueryWrapper<HealthWarningRecord> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (elderlyId != null) {
            queryWrapper.eq("elderly_id", elderlyId);
        }
        if (indicatorType != null && !indicatorType.isEmpty()) {
            queryWrapper.eq("indicator_type", indicatorType);
        }
        queryWrapper.orderByDesc("created_at");

        List<HealthWarningRecord> records = warningRecordMapper.selectList(queryWrapper);
        Map<Integer, String> elderlyNameMap = getElderlyNameMap();

        List<WarningRecordExcelVO> result = new ArrayList<>();
        for (HealthWarningRecord record : records) {
            WarningRecordExcelVO vo = new WarningRecordExcelVO();
            BeanUtils.copyProperties(record, vo);
            vo.setElderlyName(elderlyNameMap.getOrDefault(record.getElderlyId(), "未知"));
            vo.setIndicatorTypeDesc(INDICATOR_TYPE_MAP.getOrDefault(record.getIndicatorType(), record.getIndicatorType()));
            vo.setStatusDesc(STATUS_MAP.getOrDefault(record.getStatus(), record.getStatus()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Object> convertToRow(WarningRecordExcelVO data) {
        List<Object> row = new ArrayList<>();
        row.add(data.getId());
        row.add(data.getElderlyName());
        row.add(data.getIndicatorTypeDesc());
        row.add(data.getActualValue());
        row.add(data.getThresholdValue());
        row.add(data.getWarningLevel());
        row.add(data.getWarningMessage());
        row.add(data.getStatusDesc());
        row.add(data.getCreatedAt());
        row.add(data.getHandledAt());
        row.add(data.getHandledBy());
        row.add(data.getHandleRemark());
        return row;
    }

    @Override
    public String generateFileName() {
        return generateFileName("预警记录");
    }
}
