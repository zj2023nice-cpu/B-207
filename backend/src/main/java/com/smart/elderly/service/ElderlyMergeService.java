package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smart.elderly.dto.DuplicateElderlyGroupDTO;
import com.smart.elderly.dto.ElderlyMergeDTO;
import com.smart.elderly.dto.MergePreviewDTO;
import com.smart.elderly.entity.*;
import com.smart.elderly.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElderlyMergeService {

    @Autowired
    private ElderlyMapper elderlyMapper;
    @Autowired
    private HealthRecordMapper healthRecordMapper;
    @Autowired
    private HealthWarningRecordMapper healthWarningRecordMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private ElderlyTagRelationMapper elderlyTagRelationMapper;
    @Autowired
    private ElderlyFollowMapper elderlyFollowMapper;
    @Autowired
    private NursingObservationRecordMapper nursingObservationRecordMapper;
    @Autowired
    private VisitorVisitRecordMapper visitorVisitRecordMapper;
    @Autowired
    private HealthRecordCorrectionMapper healthRecordCorrectionMapper;
    @Autowired
    private HealthWarningThresholdMapper healthWarningThresholdMapper;

    private static final Map<String, String> FIELD_LABELS = new HashMap<>();
    static {
        FIELD_LABELS.put("name", "姓名");
        FIELD_LABELS.put("age", "年龄");
        FIELD_LABELS.put("gender", "性别");
        FIELD_LABELS.put("phone", "联系电话");
        FIELD_LABELS.put("address", "居住地址");
        FIELD_LABELS.put("emergencyContactName", "紧急联系人姓名");
        FIELD_LABELS.put("emergencyContactPhone", "紧急联系人电话");
        FIELD_LABELS.put("emergencyContactRelation", "紧急联系人关系");
        FIELD_LABELS.put("status", "状态");
    }

    public List<DuplicateElderlyGroupDTO> detectDuplicates() {
        List<Elderly> allElderly = elderlyMapper.selectList(
            new LambdaQueryWrapper<Elderly>()
                .isNull(Elderly::getMergedToId)
                .orderByAsc(Elderly::getId)
        );

        List<DuplicateElderlyGroupDTO> groups = new ArrayList<>();
        Set<Integer> processed = new HashSet<>();

        for (int i = 0; i < allElderly.size(); i++) {
            Elderly e1 = allElderly.get(i);
            if (processed.contains(e1.getId())) continue;

            List<Elderly> group = new ArrayList<>();
            group.add(e1);
            processed.add(e1.getId());
            int maxConfidence = 0;
            String matchReason = "";

            for (int j = i + 1; j < allElderly.size(); j++) {
                Elderly e2 = allElderly.get(j);
                if (processed.contains(e2.getId())) continue;

                MatchResult result = calculateMatchScore(e1, e2);
                if (result.confidence >= 65) {
                    group.add(e2);
                    processed.add(e2.getId());
                    if (result.confidence > maxConfidence) {
                        maxConfidence = result.confidence;
                        matchReason = result.reason;
                    }
                }
            }

            if (group.size() >= 2) {
                DuplicateElderlyGroupDTO dto = new DuplicateElderlyGroupDTO();
                dto.setGroupId(UUID.randomUUID().toString());
                dto.setElderlyList(group);
                dto.setConfidence(maxConfidence);
                dto.setMatchReason(matchReason);
                dto.setRelatedDataCount(calculateTotalRelatedData(group));
                groups.add(dto);
            }
        }

        groups.sort((a, b) -> b.getConfidence() - a.getConfidence());
        return groups;
    }

    private MatchResult calculateMatchScore(Elderly e1, Elderly e2) {
        int score = 0;
        String reason = "";

        String name1 = e1.getName() == null ? "" : e1.getName().trim();
        String name2 = e2.getName() == null ? "" : e2.getName().trim();
        String phone1 = e1.getPhone() == null ? "" : e1.getPhone().trim();
        String phone2 = e2.getPhone() == null ? "" : e2.getPhone().trim();
        String emerPhone1 = e1.getEmergencyContactPhone() == null ? "" : e1.getEmergencyContactPhone().trim();
        String emerPhone2 = e2.getEmergencyContactPhone() == null ? "" : e2.getEmergencyContactPhone().trim();

        if (!name1.isEmpty() && name1.equals(name2)) {
            if (!phone1.isEmpty() && phone1.equals(phone2)) {
                score = 95;
                reason = "姓名和联系电话完全相同";
            } else if (!emerPhone1.isEmpty() && emerPhone1.equals(emerPhone2)) {
                score = 90;
                reason = "姓名和紧急联系人电话完全相同";
            } else if (e1.getGender() != null && e1.getGender().equals(e2.getGender())
                    && e1.getAge() != null && e2.getAge() != null
                    && Math.abs(e1.getAge() - e2.getAge()) <= 3) {
                double addrSim = calculateStringSimilarity(
                    e1.getAddress() == null ? "" : e1.getAddress(),
                    e2.getAddress() == null ? "" : e2.getAddress()
                );
                if (addrSim >= 0.7) {
                    score = 80;
                    reason = "姓名、性别相同，年龄相近，地址高度相似";
                } else {
                    score = 65;
                    reason = "姓名、性别相同，年龄相近";
                }
            }
        } else {
            double nameSim = calculateStringSimilarity(name1, name2);
            if (nameSim >= 0.8 && !phone1.isEmpty() && !phone2.isEmpty()
                    && phone1.length() >= 8 && phone2.length() >= 8
                    && phone1.substring(phone1.length() - 8).equals(phone2.substring(phone2.length() - 8))) {
                score = 65;
                reason = "姓名高度相似，联系电话后8位相同";
            }
        }

        return new MatchResult(score, reason);
    }

    private double calculateStringSimilarity(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }

    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[len1][len2];
    }

    private int calculateTotalRelatedData(List<Elderly> group) {
        int total = 0;
        for (Elderly e : group) {
            total += countRelatedData(e.getId());
        }
        return total;
    }

    private int countRelatedData(Integer elderlyId) {
        int count = 0;
        count += healthRecordMapper.selectCount(new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getElderlyId, elderlyId)).intValue();
        count += healthWarningRecordMapper.selectCount(new LambdaQueryWrapper<HealthWarningRecord>().eq(HealthWarningRecord::getElderlyId, elderlyId)).intValue();
        count += notificationMapper.selectCount(new LambdaQueryWrapper<Notification>().eq(Notification::getElderlyId, elderlyId)).intValue();
        count += elderlyTagRelationMapper.selectCount(new LambdaQueryWrapper<ElderlyTagRelation>().eq(ElderlyTagRelation::getElderlyId, elderlyId)).intValue();
        count += elderlyFollowMapper.selectCount(new LambdaQueryWrapper<ElderlyFollow>().eq(ElderlyFollow::getElderlyId, elderlyId)).intValue();
        count += nursingObservationRecordMapper.selectCount(new LambdaQueryWrapper<NursingObservationRecord>().eq(NursingObservationRecord::getElderlyId, elderlyId)).intValue();
        count += visitorVisitRecordMapper.selectCount(new LambdaQueryWrapper<VisitorVisitRecord>().eq(VisitorVisitRecord::getElderlyId, elderlyId)).intValue();
        count += healthRecordCorrectionMapper.selectCount(new LambdaQueryWrapper<HealthRecordCorrection>().eq(HealthRecordCorrection::getElderlyId, elderlyId)).intValue();
        count += healthWarningThresholdMapper.selectCount(new LambdaQueryWrapper<HealthWarningThreshold>().eq(HealthWarningThreshold::getElderlyId, elderlyId)).intValue();
        return count;
    }

    public MergePreviewDTO getMergePreview(Integer primaryId, Integer mergedId) {
        Elderly primary = elderlyMapper.selectById(primaryId);
        Elderly merged = elderlyMapper.selectById(mergedId);

        if (primary == null || merged == null) {
            throw new RuntimeException("老人档案不存在");
        }

        MergePreviewDTO dto = new MergePreviewDTO();
        dto.setPrimaryElderly(primary);
        dto.setMergedElderly(merged);
        dto.setConflictFields(identifyConflictFields(primary, merged));
        dto.setRelatedDataCounts(getRelatedDataCounts(mergedId));

        return dto;
    }

    private List<MergePreviewDTO.ConflictField> identifyConflictFields(Elderly primary, Elderly merged) {
        List<MergePreviewDTO.ConflictField> conflicts = new ArrayList<>();

        addConflictIfDifferent(conflicts, "name", primary.getName(), merged.getName(), primary.getName());
        addConflictIfDifferent(conflicts, "age", primary.getAge(), merged.getAge(), primary.getAge());
        addConflictIfDifferent(conflicts, "gender", primary.getGender(), merged.getGender(), primary.getGender());
        addConflictIfDifferent(conflicts, "phone", primary.getPhone(), merged.getPhone(), primary.getPhone());
        addConflictIfDifferent(conflicts, "address", primary.getAddress(), merged.getAddress(), primary.getAddress());
        addConflictIfDifferent(conflicts, "emergencyContactName", primary.getEmergencyContactName(), merged.getEmergencyContactName(), primary.getEmergencyContactName());
        addConflictIfDifferent(conflicts, "emergencyContactPhone", primary.getEmergencyContactPhone(), merged.getEmergencyContactPhone(), primary.getEmergencyContactPhone());
        addConflictIfDifferent(conflicts, "emergencyContactRelation", primary.getEmergencyContactRelation(), merged.getEmergencyContactRelation(), primary.getEmergencyContactRelation());
        addConflictIfDifferent(conflicts, "status", primary.getStatus(), merged.getStatus(), primary.getStatus());

        return conflicts;
    }

    private void addConflictIfDifferent(List<MergePreviewDTO.ConflictField> conflicts, String fieldName,
                                        Object primaryValue, Object mergedValue, Object recommended) {
        boolean pEmpty = primaryValue == null || (primaryValue instanceof String && ((String) primaryValue).trim().isEmpty());
        boolean mEmpty = mergedValue == null || (mergedValue instanceof String && ((String) mergedValue).trim().isEmpty());

        if (pEmpty && !mEmpty) {
            MergePreviewDTO.ConflictField cf = new MergePreviewDTO.ConflictField();
            cf.setFieldName(fieldName);
            cf.setFieldLabel(FIELD_LABELS.getOrDefault(fieldName, fieldName));
            cf.setPrimaryValue(primaryValue);
            cf.setMergedValue(mergedValue);
            cf.setRecommendedValue(mergedValue);
            conflicts.add(cf);
        } else if (!pEmpty && !mEmpty && !primaryValue.equals(mergedValue)) {
            MergePreviewDTO.ConflictField cf = new MergePreviewDTO.ConflictField();
            cf.setFieldName(fieldName);
            cf.setFieldLabel(FIELD_LABELS.getOrDefault(fieldName, fieldName));
            cf.setPrimaryValue(primaryValue);
            cf.setMergedValue(mergedValue);
            cf.setRecommendedValue(recommended);
            conflicts.add(cf);
        }
    }

    private Map<String, Integer> getRelatedDataCounts(Integer elderlyId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("健康记录", healthRecordMapper.selectCount(new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getElderlyId, elderlyId)).intValue());
        counts.put("健康预警", healthWarningRecordMapper.selectCount(new LambdaQueryWrapper<HealthWarningRecord>().eq(HealthWarningRecord::getElderlyId, elderlyId)).intValue());
        counts.put("通知消息", notificationMapper.selectCount(new LambdaQueryWrapper<Notification>().eq(Notification::getElderlyId, elderlyId)).intValue());
        counts.put("标签关联", elderlyTagRelationMapper.selectCount(new LambdaQueryWrapper<ElderlyTagRelation>().eq(ElderlyTagRelation::getElderlyId, elderlyId)).intValue());
        counts.put("关注关系", elderlyFollowMapper.selectCount(new LambdaQueryWrapper<ElderlyFollow>().eq(ElderlyFollow::getElderlyId, elderlyId)).intValue());
        counts.put("护理观察", nursingObservationRecordMapper.selectCount(new LambdaQueryWrapper<NursingObservationRecord>().eq(NursingObservationRecord::getElderlyId, elderlyId)).intValue());
        counts.put("探访记录", visitorVisitRecordMapper.selectCount(new LambdaQueryWrapper<VisitorVisitRecord>().eq(VisitorVisitRecord::getElderlyId, elderlyId)).intValue());
        counts.put("记录修正", healthRecordCorrectionMapper.selectCount(new LambdaQueryWrapper<HealthRecordCorrection>().eq(HealthRecordCorrection::getElderlyId, elderlyId)).intValue());
        counts.put("阈值配置", healthWarningThresholdMapper.selectCount(new LambdaQueryWrapper<HealthWarningThreshold>().eq(HealthWarningThreshold::getElderlyId, elderlyId)).intValue());
        return counts;
    }

    @Transactional(rollbackFor = Exception.class)
    public void mergeElderly(ElderlyMergeDTO mergeDTO, String operator) {
        Integer primaryId = mergeDTO.getPrimaryElderlyId();
        Integer mergedId = mergeDTO.getMergedElderlyId();

        if (primaryId.equals(mergedId)) {
            throw new RuntimeException("不能合并同一个档案");
        }

        Elderly primary = elderlyMapper.selectById(primaryId);
        Elderly merged = elderlyMapper.selectById(mergedId);

        if (primary == null || merged == null) {
            throw new RuntimeException("老人档案不存在");
        }

        if (merged.getMergedToId() != null) {
            throw new RuntimeException("该档案已被合并，不能再次合并");
        }

        if (mergeDTO.getFieldOverrides() != null && !mergeDTO.getFieldOverrides().isEmpty()) {
            applyFieldOverrides(primary, mergeDTO.getFieldOverrides());
            primary.setUpdatedAt(LocalDateTime.now());
            elderlyMapper.updateById(primary);
        }

        migrateRelatedData(mergedId, primaryId);

        merged.setMergedToId(primaryId);
        merged.setMergedAt(LocalDateTime.now());
        merged.setMergedBy(operator);
        merged.setUpdatedAt(LocalDateTime.now());
        elderlyMapper.updateById(merged);
    }

    private void applyFieldOverrides(Elderly elderly, Map<String, Object> overrides) {
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            switch (field) {
                case "name":
                    elderly.setName((String) value);
                    break;
                case "age":
                    if (value instanceof Number) {
                        elderly.setAge(((Number) value).intValue());
                    } else if (value instanceof String) {
                        elderly.setAge(Integer.parseInt((String) value));
                    }
                    break;
                case "gender":
                    elderly.setGender((String) value);
                    break;
                case "phone":
                    elderly.setPhone((String) value);
                    break;
                case "address":
                    elderly.setAddress((String) value);
                    break;
                case "emergencyContactName":
                    elderly.setEmergencyContactName((String) value);
                    break;
                case "emergencyContactPhone":
                    elderly.setEmergencyContactPhone((String) value);
                    break;
                case "emergencyContactRelation":
                    elderly.setEmergencyContactRelation((String) value);
                    break;
                case "status":
                    elderly.setStatus((String) value);
                    break;
            }
        }
    }

    private void migrateRelatedData(Integer fromId, Integer toId) {
        healthRecordMapper.update(null,
            new LambdaUpdateWrapper<HealthRecord>()
                .eq(HealthRecord::getElderlyId, fromId)
                .set(HealthRecord::getElderlyId, toId)
        );

        healthWarningRecordMapper.update(null,
            new LambdaUpdateWrapper<HealthWarningRecord>()
                .eq(HealthWarningRecord::getElderlyId, fromId)
                .set(HealthWarningRecord::getElderlyId, toId)
        );

        notificationMapper.update(null,
            new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getElderlyId, fromId)
                .set(Notification::getElderlyId, toId)
        );

        migrateTagRelations(fromId, toId);
        migrateFollows(fromId, toId);

        nursingObservationRecordMapper.update(null,
            new LambdaUpdateWrapper<NursingObservationRecord>()
                .eq(NursingObservationRecord::getElderlyId, fromId)
                .set(NursingObservationRecord::getElderlyId, toId)
        );

        visitorVisitRecordMapper.update(null,
            new LambdaUpdateWrapper<VisitorVisitRecord>()
                .eq(VisitorVisitRecord::getElderlyId, fromId)
                .set(VisitorVisitRecord::getElderlyId, toId)
        );

        healthRecordCorrectionMapper.update(null,
            new LambdaUpdateWrapper<HealthRecordCorrection>()
                .eq(HealthRecordCorrection::getElderlyId, fromId)
                .set(HealthRecordCorrection::getElderlyId, toId)
        );

        migrateThresholds(fromId, toId);
    }

    private void migrateTagRelations(Integer fromId, Integer toId) {
        List<Integer> fromTags = elderlyTagRelationMapper.selectTagIdsByElderlyId(fromId);
        List<Integer> toTags = elderlyTagRelationMapper.selectTagIdsByElderlyId(toId);

        for (Integer tagId : fromTags) {
            if (!toTags.contains(tagId)) {
                ElderlyTagRelation relation = new ElderlyTagRelation();
                relation.setElderlyId(toId);
                relation.setTagId(tagId);
                relation.setCreatedAt(LocalDateTime.now());
                elderlyTagRelationMapper.insert(relation);
            }
        }

        elderlyTagRelationMapper.delete(new LambdaQueryWrapper<ElderlyTagRelation>()
            .eq(ElderlyTagRelation::getElderlyId, fromId));
    }

    private void migrateFollows(Integer fromId, Integer toId) {
        List<ElderlyFollow> fromFollows = elderlyFollowMapper.selectList(
            new LambdaQueryWrapper<ElderlyFollow>().eq(ElderlyFollow::getElderlyId, fromId)
        );
        List<ElderlyFollow> toFollows = elderlyFollowMapper.selectList(
            new LambdaQueryWrapper<ElderlyFollow>().eq(ElderlyFollow::getElderlyId, toId)
        );

        Set<Integer> toUserIds = toFollows.stream()
            .map(ElderlyFollow::getUserId)
            .collect(Collectors.toSet());

        for (ElderlyFollow follow : fromFollows) {
            if (!toUserIds.contains(follow.getUserId())) {
                ElderlyFollow newFollow = new ElderlyFollow();
                newFollow.setElderlyId(toId);
                newFollow.setUserId(follow.getUserId());
                newFollow.setCreatedAt(follow.getCreatedAt());
                elderlyFollowMapper.insert(newFollow);
            }
        }

        elderlyFollowMapper.delete(new LambdaQueryWrapper<ElderlyFollow>()
            .eq(ElderlyFollow::getElderlyId, fromId));
    }

    private void migrateThresholds(Integer fromId, Integer toId) {
        List<HealthWarningThreshold> fromThresholds = healthWarningThresholdMapper.selectList(
            new LambdaQueryWrapper<HealthWarningThreshold>().eq(HealthWarningThreshold::getElderlyId, fromId)
        );
        List<HealthWarningThreshold> toThresholds = healthWarningThresholdMapper.selectList(
            new LambdaQueryWrapper<HealthWarningThreshold>().eq(HealthWarningThreshold::getElderlyId, toId)
        );

        Set<String> toIndicators = toThresholds.stream()
            .map(HealthWarningThreshold::getIndicatorType)
            .collect(Collectors.toSet());

        for (HealthWarningThreshold threshold : fromThresholds) {
            if (!toIndicators.contains(threshold.getIndicatorType())) {
                HealthWarningThreshold newThreshold = new HealthWarningThreshold();
                newThreshold.setElderlyId(toId);
                newThreshold.setIndicatorType(threshold.getIndicatorType());
                newThreshold.setHighThreshold(threshold.getHighThreshold());
                newThreshold.setLowThreshold(threshold.getLowThreshold());
                newThreshold.setEnabled(threshold.getEnabled());
                newThreshold.setCreatedAt(threshold.getCreatedAt());
                newThreshold.setUpdatedAt(threshold.getUpdatedAt());
                healthWarningThresholdMapper.insert(newThreshold);
            }
        }

        healthWarningThresholdMapper.delete(new LambdaQueryWrapper<HealthWarningThreshold>()
            .eq(HealthWarningThreshold::getElderlyId, fromId));
    }

    private static class MatchResult {
        int confidence;
        String reason;

        MatchResult(int confidence, String reason) {
            this.confidence = confidence;
            this.reason = reason;
        }
    }
}
