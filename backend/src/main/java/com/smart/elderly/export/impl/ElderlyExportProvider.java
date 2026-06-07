package com.smart.elderly.export.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.export.BaseExportProvider;
import com.smart.elderly.export.ElderlyExcelVO;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.service.ElderlyTagRelationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ElderlyExportProvider extends BaseExportProvider<ElderlyExcelVO> {

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private ElderlyTagRelationService elderlyTagRelationService;

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
        List<String> headers = new ArrayList<String>();
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
        Map<String, Object> params = parseExportParams(exportParams);
        Integer tagId = getIntegerParam(params, "tagId");
        String keyword = getStringParam(params, "keyword");

        List<Elderly> elderlyList;
        if (tagId != null) {
            List<Integer> elderlyIds = elderlyTagRelationService.getElderlyIdsByTagId(tagId);
            if (elderlyIds.isEmpty()) {
                elderlyList = new ArrayList<Elderly>();
            } else {
                elderlyList = elderlyMapper.selectList(
                        new QueryWrapper<Elderly>()
                                .in("id", elderlyIds)
                                .isNull("merged_to_id")
                                .like(keyword != null && !keyword.isEmpty(), "name", keyword)
                );
            }
        } else {
            elderlyList = elderlyMapper.selectList(
                    new QueryWrapper<Elderly>()
                            .isNull("merged_to_id")
                            .like(keyword != null && !keyword.isEmpty(), "name", keyword)
            );
        }

        List<ElderlyExcelVO> result = new ArrayList<ElderlyExcelVO>();
        for (Elderly elderly : elderlyList) {
            ElderlyExcelVO vo = new ElderlyExcelVO();
            BeanUtils.copyProperties(elderly, vo);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Object> convertToRow(ElderlyExcelVO data) {
        List<Object> row = new ArrayList<Object>();
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
        return generateFileName("老人数据");
    }
}
