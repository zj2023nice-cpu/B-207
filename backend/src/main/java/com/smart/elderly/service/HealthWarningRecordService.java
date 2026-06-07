package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.HealthWarningTimeline;
import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import com.smart.elderly.mapper.HealthWarningTimelineMapper;
import com.smart.elderly.vo.HealthWarningDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthWarningRecordService extends ServiceImpl<HealthWarningRecordMapper, HealthWarningRecord> {

    @Autowired
    private HealthWarningThresholdService thresholdService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private HealthWarningTimelineMapper timelineMapper;

    public List<HealthWarningRecord> getAllWithElderlyName() {
        return baseMapper.findAllWithElderlyName();
    }

    public List<HealthWarningRecord> getByElderlyId(Integer elderlyId) {
        return baseMapper.findByElderlyIdWithName(elderlyId);
    }

    public List<HealthWarningRecord> getByStatus(String status) {
        return baseMapper.findByStatusWithElderlyName(status);
    }

    public List<HealthWarningRecord> getPending() {
        return baseMapper.findByStatusWithElderlyName("PENDING");
    }

    public long countPending() {
        return baseMapper.countPending();
    }

    public HealthWarningDetailVO getDetailById(Integer id) {
        HealthWarningRecord record = this.getById(id);
        if (record == null) {
            return null;
        }
        
        HealthWarningStatus currentStatus = HealthWarningStatus.fromCode(record.getStatus());
        List<HealthWarningTimeline> timeline = timelineMapper.findByWarningRecordId(id);
        
        HealthWarningDetailVO vo = new HealthWarningDetailVO();
        vo.setId(record.getId());
        vo.setElderlyId(record.getElderlyId());
        vo.setElderlyName(record.getElderlyName());
        vo.setHealthRecordId(record.getHealthRecordId());
        vo.setIndicatorType(record.getIndicatorType());
        vo.setActualValue(record.getActualValue());
        vo.setThresholdValue(record.getThresholdValue());
        vo.setWarningLevel(record.getWarningLevel());
        vo.setWarningMessage(record.getWarningMessage());
        vo.setStatus(record.getStatus());
        vo.setStatusName(currentStatus.getDisplayName());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setHandledAt(record.getHandledAt());
        vo.setHandledBy(record.getHandledBy());
        vo.setHandleRemark(record.getHandleRemark());
        vo.setTimeline(timeline);
        vo.setAllowedActions(
            currentStatus.getAllowedTransitions().stream()
                .map(HealthWarningStatus::getCode)
                .collect(Collectors.toList())
        );
        
        return vo;
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
        record.setStatus(HealthWarningStatus.PENDING.getCode());
        record.setCreatedAt(LocalDateTime.now());
        
        this.save(record);
        
        addTimeline(record.getId(), "CREATE", null, 
            HealthWarningStatus.PENDING.getCode(), "系统", "系统创建预警");
        
        notificationService.createWarningNotification(record);
        
        return record;
    }

    @Transactional
    public boolean handleWarning(Integer id, String handledBy, String remark) {
        return transitionStatus(id, HealthWarningStatus.HANDLED, handledBy, remark);
    }

    @Transactional
    public boolean markAsRead(Integer id) {
        return transitionStatus(id, HealthWarningStatus.READ, "系统", "标记为已读");
    }

    @Transactional
    public boolean ignoreWarning(Integer id, String operator, String remark) {
        return transitionStatus(id, HealthWarningStatus.IGNORED, operator, 
            remark != null ? remark : "忽略该预警");
    }

    @Transactional
    public boolean reopenWarning(Integer id, String operator, String remark) {
        return transitionStatus(id, HealthWarningStatus.REOPENED, operator, 
            remark != null ? remark : "重新打开该预警");
    }

    @Transactional
    public boolean escalateWarning(Integer id, String operator, String remark) {
        return transitionStatus(id, HealthWarningStatus.ESCALATED, operator, 
            remark != null ? remark : "升级处理该预警");
    }

    @Transactional
    public boolean transitionStatus(Integer id, HealthWarningStatus targetStatus, 
                                     String operator, String remark) {
        HealthWarningRecord record = this.getById(id);
        if (record == null) {
            return false;
        }
        
        HealthWarningStatus currentStatus = HealthWarningStatus.fromCode(record.getStatus());
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalArgumentException(
                String.format("非法状态切换：无法从 %s 切换到 %s", 
                    currentStatus.getDisplayName(), targetStatus.getDisplayName())
            );
        }
        
        String fromStatus = record.getStatus();
        record.setStatus(targetStatus.getCode());
        
        if (targetStatus == HealthWarningStatus.HANDLED) {
            record.setHandledAt(LocalDateTime.now());
            record.setHandledBy(operator);
            record.setHandleRemark(remark);
        }
        
        this.updateById(record);
        
        String actionType = getActionType(targetStatus);
        addTimeline(id, actionType, fromStatus, targetStatus.getCode(), operator, remark);
        
        return true;
    }

    private String getActionType(HealthWarningStatus status) {
        switch (status) {
            case READ:
                return "READ";
            case HANDLED:
                return "HANDLE";
            case IGNORED:
                return "IGNORE";
            case REOPENED:
                return "REOPEN";
            case ESCALATED:
                return "ESCALATE";
            default:
                return "UPDATE";
        }
    }

    private void addTimeline(Integer warningRecordId, String actionType, 
                             String fromStatus, String toStatus, 
                             String operator, String remark) {
        HealthWarningTimeline timeline = new HealthWarningTimeline();
        timeline.setWarningRecordId(warningRecordId);
        timeline.setActionType(actionType);
        timeline.setFromStatus(fromStatus);
        timeline.setToStatus(toStatus);
        timeline.setOperator(operator);
        timeline.setRemark(remark);
        timeline.setCreatedAt(LocalDateTime.now());
        timelineMapper.insert(timeline);
    }
}
