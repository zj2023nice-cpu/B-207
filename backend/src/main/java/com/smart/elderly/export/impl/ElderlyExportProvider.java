package com.smart.elderly.export.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.export.ElderlyExcelVO;
import com.smart.elderly.export.ExportDataProvider;
import com.smart.elderly.mapper.ElderlyMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ElderlyExportProvider implements ExportDataProvider<ElderlyExcelVO> {

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Override
    public String getExportType() {
        return "ELDERLY";
    }

    @Override
    public String getExportTypeDesc() {
        return "老人管理";
    }

    @Override
    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("姓名");
        headers.add("年龄");
        headers.add("性别");
        headers.add("电话");
        headers.add("地址");
        headers.add("紧急联系人");
        headers.add("紧急联系人电话");
        headers.add("紧急联系人关系");
        headers.add("状态");
        headers.add("创建时间");
        return headers;
    }

    @Override
    public List<ElderlyExcelVO> fetchData(String exportParams) {
        List<Elderly> elderlyList = elderlyMapper.selectList(new QueryWrapper<>());
        List<ElderlyExcelVO> result = new ArrayList<>();
        for (Elderly elderly : elderlyList) {
            ElderlyExcelVO vo = new ElderlyExcelVO();
            BeanUtils.copyProperties(elderly, vo);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Object> convertToRow(ElderlyExcelVO data) {
        List<Object> row = new ArrayList<>();
        row.add(data.getId());
        row.add(data.getName());
        row.add(data.getAge());
        row.add(data.getGender());
        row.add(data.getPhone());
        row.add(data.getAddress());
        row.add(data.getEmergencyContactName());
        row.add(data.getEmergencyContactPhone());
        row.add(data.getEmergencyContactRelation());
        row.add(data.getStatus());
        row.add(data.getCreatedAt());
        return row;
    }

    @Override
    public String generateFileName() {
        return "老人数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
