package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthWarningRecordService extends ServiceImpl<HealthWarningRecordMapper, HealthWarningRecord> {

    @Autowired
    private HealthWarningThresholdService thresholdService;

    @Autowired
    private NotificationService notificationService;

    public List<HealthWarningRecord> getAllWithElderlyName() {
        return baseMapper.findAllWithElderlyName();
    }

    public List<HealthWarningRecord> getByElderlyId(Integer elderlyId) {
        return baseMapper.findByElderlyIdWithName(elderlyId);
    }

    public List<HealthWarningRecord> getPending() {
        return baseMapper.findByStatusWithElderlyName("PENDING");
    }

    public long countPending() {
        return baseMapper.countPending();
    }

    @Transactional
    public HealthWarningRecord createWarning(Integer elderlyId, Integer healthRecordId, 
                                               String indicatorType, BigDecimal actualValue,
                                               BigDecimal thresholdValue, String warningLevel,
                                               String warningMessage) {
        HealthWarningRecord record = new HealthWarningRecord();
        record.setElderlyId(elderlyId);
        record.setHealthRecordId(healthRecordId);
        record.setIndicatorType(indicatorType);
        record.setActualValue(actualValue);
        record.setThresholdValue(thresholdValue);
        record.setWarningLevel(warningLevel);
        record.setWarningMessage(warningMessage);
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());
        
        this.save(record);
        
        notificationService.createWarningNotification(record);
        
        return record;
    }

    @Transactional
    public boolean handleWarning(Integer id, String handledBy, String remark) {
        HealthWarningRecord record = this.getById(id);
        if (record == null) {
            return false;
        }
        
        record.setStatus("HANDLED");
        record.setHandledAt(LocalDateTime.now());
        record.setHandledBy(handledBy);
        record.setHandleRemark(remark);
        
        return this.updateById(record);
    }

    @Transactional
    public boolean markAsRead(Integer id) {
        HealthWarningRecord record = this.getById(id);
        if (record == null) {
            return false;
        }
        if ("PENDING".equals(record.getStatus())) {
            record.setStatus("READ");
            return this.updateById(record);
        }
        return true;
    }
}
