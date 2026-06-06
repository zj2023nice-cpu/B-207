package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.VisitorVisitRecord;
import com.smart.elderly.mapper.VisitorVisitRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitorVisitRecordService extends ServiceImpl<VisitorVisitRecordMapper, VisitorVisitRecord> {

    @Autowired
    private ElderlyService elderlyService;

    @Transactional
    public boolean saveRecord(VisitorVisitRecord record) {
        if (record.getElderlyId() == null) {
            throw new IllegalArgumentException("探访对象ID不能为空");
        }

        Elderly elderly = elderlyService.getById(record.getElderlyId());
        if (elderly == null) {
            throw new IllegalArgumentException("老人不存在，ID: " + record.getElderlyId());
        }

        if (record.getVisitTime() == null) {
            record.setVisitTime(LocalDateTime.now());
        }

        if (record.getStatus() == null || record.getStatus().trim().isEmpty()) {
            record.setStatus("VISITING");
        }

        if (record.getId() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        record.setUpdatedAt(LocalDateTime.now());

        return this.saveOrUpdate(record);
    }

    @Transactional
    public boolean registerLeave(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        VisitorVisitRecord record = this.getById(id);
        if (record == null) {
            throw new IllegalArgumentException("记录不存在，ID: " + id);
        }
        record.setLeaveTime(LocalDateTime.now());
        record.setStatus("LEFT");
        record.setUpdatedAt(LocalDateTime.now());
        return this.updateById(record);
    }

    public List<VisitorVisitRecord> getRecordsWithNames() {
        return baseMapper.selectWithElderlyName();
    }

    public List<VisitorVisitRecord> getByElderlyId(Integer elderlyId) {
        if (elderlyId == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }
        return baseMapper.selectByElderlyId(elderlyId);
    }

    public IPage<VisitorVisitRecord> getRecordsWithPage(Integer pageNum, Integer pageSize) {
        Page<VisitorVisitRecord> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPageWithElderlyName(page);
    }

    public IPage<VisitorVisitRecord> getRecordsWithFilters(
            Integer pageNum,
            Integer pageSize,
            Integer elderlyId,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        Page<VisitorVisitRecord> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPageWithFilters(page, elderlyId, status, startTime, endTime);
    }

    @Transactional
    public boolean deleteRecord(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        return this.removeById(id);
    }

    public VisitorVisitRecord getRecordById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        return this.getById(id);
    }
}
