package com.smart.elderly.export.impl;

import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.export.BaseExportProvider;
import com.smart.elderly.export.HealthRecordExcelVO;
import com.smart.elderly.mapper.HealthRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HealthRecordExportProvider extends BaseExportProvider<HealthRecordExcelVO> {

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Override
    public String getExportType() {
        return "HEALTH_RECORD";
    }

    @Override
    public String getExportTypeDesc() {
        return "健康记录";
    }

    @Override
    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("老人姓名");
        headers.add("血压");
        headers.add("收缩压");
        headers.add("舒张压");
        headers.add("体温");
        headers.add("心率");
        headers.add("血氧");
        headers.add("血糖");
        headers.add("是否异常");
        headers.add("异常原因");
        headers.add("检测时间");
        return headers;
    }

    @Override
    public List<HealthRecordExcelVO> fetchData(String exportParams) {
        Map<String, Object> params = parseExportParams(exportParams);
        Integer elderlyId = getIntegerParam(params, "elderlyId");
        Boolean isAbnormal = getBooleanParam(params, "isAbnormal");

        List<HealthRecord> records = healthRecordMapper.selectWithFilters(elderlyId, isAbnormal);
        Map<Integer, String> elderlyNameMap = getElderlyNameMap();

        List<HealthRecordExcelVO> result = new ArrayList<>();
        for (HealthRecord record : records) {
            HealthRecordExcelVO vo = new HealthRecordExcelVO();
            BeanUtils.copyProperties(record, vo);
            vo.setElderlyName(elderlyNameMap.getOrDefault(record.getElderlyId(), "未知"));
            vo.setIsAbnormalDesc(Boolean.TRUE.equals(record.getIsAbnormal()) ? "是" : "否");
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Object> convertToRow(HealthRecordExcelVO data) {
        List<Object> row = new ArrayList<>();
        row.add(data.getId());
        row.add(data.getElderlyName());
        row.add(data.getBloodPressure());
        row.add(data.getSystolicPressure());
        row.add(data.getDiastolicPressure());
        row.add(data.getTemperature());
        row.add(data.getHeartRate());
        row.add(data.getBloodOxygen());
        row.add(data.getBloodSugar());
        row.add(data.getIsAbnormalDesc());
        row.add(data.getAbnormalReason());
        row.add(data.getCheckTime());
        return row;
    }

    @Override
    public String generateFileName() {
        return generateFileName("健康记录");
    }
}
