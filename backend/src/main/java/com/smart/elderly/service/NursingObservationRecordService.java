package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.NursingObservationRecord;
import com.smart.elderly.mapper.NursingObservationRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NursingObservationRecordService extends ServiceImpl<NursingObservationRecordMapper, NursingObservationRecord> {

    @Autowired
    private ElderlyService elderlyService;

    @Transactional
    public boolean saveRecord(NursingObservationRecord record) {
        if (record.getElderlyId() == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }

        if (record.getRemark() == null || record.getRemark().trim().isEmpty()) {
            throw new IllegalArgumentException("备注内容不能为空");
        }

        Elderly elderly = elderlyService.getById(record.getElderlyId());
        if (elderly == null) {
            throw new IllegalArgumentException("老人不存在，ID: " + record.getElderlyId());
        }

        if (record.getObservationTime() == null) {
            record.setObservationTime(LocalDateTime.now());
        }

        if (record.getId() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        record.setUpdatedAt(LocalDateTime.now());

        return this.saveOrUpdate(record);
    }

    public List<NursingObservationRecord> getRecordsWithNames() {
        return baseMapper.selectWithElderlyName();
    }

    public List<NursingObservationRecord> getByElderlyId(Integer elderlyId) {
        if (elderlyId == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }
        return baseMapper.selectByElderlyId(elderlyId);
    }

    public IPage<NursingObservationRecord> getRecordsWithPage(Integer pageNum, Integer pageSize) {
        Page<NursingObservationRecord> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPageWithElderlyName(page);
    }

    @Transactional
    public boolean deleteRecord(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        return this.removeById(id);
    }

    public NursingObservationRecord getRecordById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        return this.getById(id);
    }
}
