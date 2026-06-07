package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.dto.ShiftHandoverCreateDTO;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.ShiftHandoverRecord;
import com.smart.elderly.entity.ShiftHandoverWarningRelation;
import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.enums.WarningLevel;
import com.smart.elderly.mapper.ShiftHandoverRecordMapper;
import com.smart.elderly.mapper.ShiftHandoverWarningRelationMapper;
import com.smart.elderly.vo.ShiftHandoverDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShiftHandoverRecordService extends ServiceImpl<ShiftHandoverRecordMapper, ShiftHandoverRecord> {

    @Autowired
    private ShiftHandoverWarningRelationMapper relationMapper;

    @Autowired
    private HealthWarningRecordService warningRecordService;

    public List<ShiftHandoverRecord> getAll() {
        return baseMapper.findAllOrderByTime();
    }

    public IPage<ShiftHandoverRecord> getByPage(Integer pageNum, Integer pageSize) {
        Page<ShiftHandoverRecord> page = new Page<>(pageNum, pageSize);
        return baseMapper.findAllByPage(page);
    }

    public ShiftHandoverDetailVO getDetailById(Integer id) {
        ShiftHandoverRecord record = this.getById(id);
        if (record == null) {
            return null;
        }

        ShiftHandoverDetailVO vo = new ShiftHandoverDetailVO();
        BeanUtils.copyProperties(record, vo);

        List<Integer> warningIds = relationMapper.findWarningRecordIdsByHandoverId(id);
        if (warningIds != null && !warningIds.isEmpty()) {
            List<HealthWarningRecord> warnings = warningRecordService.listByIds(warningIds)
                    .stream()
                    .map(w -> {
                        HealthWarningRecord full = warningRecordService.getById(w.getId());
                        return full;
                    })
                    .collect(Collectors.toList());
            vo.setRelatedWarnings(warnings);
        }

        return vo;
    }

    @Transactional
    public ShiftHandoverRecord create(ShiftHandoverCreateDTO dto) {
        validateDTO(dto);
        ShiftHandoverRecord record = new ShiftHandoverRecord();
        BeanUtils.copyProperties(dto, record);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        this.save(record);

        if (dto.getWarningRecordIds() != null && !dto.getWarningRecordIds().isEmpty()) {
            for (Integer warningId : dto.getWarningRecordIds()) {
                ShiftHandoverWarningRelation relation = new ShiftHandoverWarningRelation();
                relation.setHandoverRecordId(record.getId());
                relation.setWarningRecordId(warningId);
                relation.setCreatedAt(LocalDateTime.now());
                relationMapper.insert(relation);
            }
        }

        return record;
    }

    @Transactional
    public boolean update(Integer id, ShiftHandoverCreateDTO dto) {
        validateDTO(dto);
        ShiftHandoverRecord record = this.getById(id);
        if (record == null) {
            return false;
        }

        BeanUtils.copyProperties(dto, record);
        record.setId(id);
        record.setUpdatedAt(LocalDateTime.now());
        boolean success = this.updateById(record);

        if (success) {
            relationMapper.deleteByHandoverId(id);
            if (dto.getWarningRecordIds() != null && !dto.getWarningRecordIds().isEmpty()) {
                for (Integer warningId : dto.getWarningRecordIds()) {
                    ShiftHandoverWarningRelation relation = new ShiftHandoverWarningRelation();
                    relation.setHandoverRecordId(id);
                    relation.setWarningRecordId(warningId);
                    relation.setCreatedAt(LocalDateTime.now());
                    relationMapper.insert(relation);
                }
            }
        }

        return success;
    }

    @Transactional
    public boolean delete(Integer id) {
        relationMapper.deleteByHandoverId(id);
        return this.removeById(id);
    }

    public List<Integer> getWarningRecordIdsByHandoverId(Integer handoverId) {
        return relationMapper.findWarningRecordIdsByHandoverId(handoverId);
    }

    private void validateDTO(ShiftHandoverCreateDTO dto) {
        if (dto.getKeyElderly() == null || dto.getKeyElderly().trim().isEmpty()) {
            throw new IllegalArgumentException("重点关注老人不能为空");
        }
        if (dto.getPendingWarningSummary() == null || dto.getPendingWarningSummary().trim().isEmpty()) {
            throw new IllegalArgumentException("待跟进预警摘要不能为空");
        }
        if (dto.getRemarks() == null || dto.getRemarks().trim().isEmpty()) {
            throw new IllegalArgumentException("备注不能为空");
        }
    }

    public String generateWarningSummary(List<Integer> warningIds) {
        if (warningIds == null || warningIds.isEmpty()) {
            return "";
        }

        List<HealthWarningRecord> warnings = warningRecordService.listByIds(warningIds);
        StringBuilder summary = new StringBuilder();

        int pendingCount = 0;
        int highLevelCount = 0;

        for (HealthWarningRecord warning : warnings) {
            if (HealthWarningStatus.PENDING.getCode().equals(warning.getStatus())) {
                pendingCount++;
            }
            if (WarningLevel.HIGH.getCode().equals(warning.getWarningLevel())) {
                highLevelCount++;
            }
        }

        summary.append("共关联").append(warnings.size()).append("条预警");
        if (pendingCount > 0) {
            summary.append("，其中待处理").append(pendingCount).append("条");
        }
        if (highLevelCount > 0) {
            summary.append("，高级别预警").append(highLevelCount).append("条");
        }

        return summary.toString();
    }
}
