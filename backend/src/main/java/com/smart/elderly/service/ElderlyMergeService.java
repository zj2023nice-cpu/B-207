package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smart.elderly.dto.DuplicateElderlyGroupDTO;
import com.smart.elderly.dto.ElderlyMergeDTO;
import com.smart.elderly.dto.MergePreviewDTO;
import com.smart.elderly.dto.MergePreviewRequestDTO;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.ElderlyFollow;
import com.smart.elderly.entity.ElderlyTagRelation;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.HealthRecordCorrection;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.HealthWarningThreshold;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.entity.NursingObservationRecord;
import com.smart.elderly.entity.VisitorVisitRecord;
import com.smart.elderly.mapper.ElderlyFollowMapper;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.mapper.ElderlyTagRelationMapper;
import com.smart.elderly.mapper.HealthRecordCorrectionMapper;
import com.smart.elderly.mapper.HealthRecordMapper;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import com.smart.elderly.mapper.HealthWarningThresholdMapper;
import com.smart.elderly.mapper.NotificationMapper;
import com.smart.elderly.mapper.NursingObservationRecordMapper;
import com.smart.elderly.mapper.VisitorVisitRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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

    private static final Map<String, String> FIELD_LABELS = new HashMap<String, String>();
    private static final List<String> RELATED_DATA_TYPES = Arrays.asList(
            "健康记录", "健康预警", "通知消息", "标签关联", "关注关系", "护理观察", "探访记录", "记录修正", "阈值配置"
    );

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

        List<DuplicateElderlyGroupDTO> groups = new ArrayList<DuplicateElderlyGroupDTO>();
        Set<Integer> processed = new HashSet<Integer>();

        for (int i = 0; i < allElderly.size(); i++) {
            Elderly e1 = allElderly.get(i);
            if (processed.contains(e1.getId())) {
                continue;
            }

            List<Elderly> group = new ArrayList<Elderly>();
            group.add(e1);
            processed.add(e1.getId());
            int maxConfidence = 0;
            String matchReason = "";

            for (int j = i + 1; j < allElderly.size(); j++) {
                Elderly e2 = allElderly.get(j);
                if (processed.contains(e2.getId())) {
                    continue;
                }

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
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }

    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

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
        for (Elderly elderly : group) {
            total += countRelatedData(elderly.getId());
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

    public MergePreviewDTO getMergePreview(MergePreviewRequestDTO requestDTO) {
        Integer primaryId = requestDTO.getPrimaryElderlyId();
        Elderly primary = getAvailableElderly(primaryId, "主档案不存在");
        List<Integer> mergedIds = normalizeMergedIds(requestDTO.getMergedElderlyIds(), primaryId);
        List<Elderly> mergedElderlyList = getAvailableMergedElderlyList(mergedIds, primaryId);

        MergePreviewDTO dto = new MergePreviewDTO();
        dto.setPrimaryElderly(primary);
        dto.setMergedElderlyList(mergedElderlyList);
        dto.setConflictFields(identifyConflictFields(primary, mergedElderlyList));
        dto.setRelatedDataCounts(getAggregatedRelatedDataCounts(mergedIds));
        return dto;
    }

    private List<MergePreviewDTO.ConflictField> identifyConflictFields(Elderly primary, List<Elderly> mergedElderlyList) {
        List<MergePreviewDTO.ConflictField> conflicts = new ArrayList<MergePreviewDTO.ConflictField>();
        addConflictField(conflicts, "name", primary, mergedElderlyList, Elderly::getName);
        addConflictField(conflicts, "age", primary, mergedElderlyList, Elderly::getAge);
        addConflictField(conflicts, "gender", primary, mergedElderlyList, Elderly::getGender);
        addConflictField(conflicts, "phone", primary, mergedElderlyList, Elderly::getPhone);
        addConflictField(conflicts, "address", primary, mergedElderlyList, Elderly::getAddress);
        addConflictField(conflicts, "emergencyContactName", primary, mergedElderlyList, Elderly::getEmergencyContactName);
        addConflictField(conflicts, "emergencyContactPhone", primary, mergedElderlyList, Elderly::getEmergencyContactPhone);
        addConflictField(conflicts, "emergencyContactRelation", primary, mergedElderlyList, Elderly::getEmergencyContactRelation);
        addConflictField(conflicts, "status", primary, mergedElderlyList, Elderly::getStatus);
        return conflicts;
    }

    private void addConflictField(List<MergePreviewDTO.ConflictField> conflicts,
                                  String fieldName,
                                  Elderly primary,
                                  List<Elderly> mergedElderlyList,
                                  Function<Elderly, Object> valueGetter) {
        Object primaryValue = valueGetter.apply(primary);
        LinkedHashMap<String, MergePreviewDTO.ValueOption> optionMap = new LinkedHashMap<String, MergePreviewDTO.ValueOption>();
        addOption(optionMap, primaryValue, "主档案(ID:" + primary.getId() + ")");
        for (Elderly merged : mergedElderlyList) {
            addOption(optionMap, valueGetter.apply(merged), merged.getName() + "(ID:" + merged.getId() + ")");
        }

        if (optionMap.isEmpty()) {
            return;
        }

        boolean primaryHasValue = !isEmptyValue(primaryValue);
        if (optionMap.size() == 1 && primaryHasValue) {
            String onlyKey = optionMap.keySet().iterator().next();
            if (onlyKey.equals(normalizeOptionValue(primaryValue))) {
                return;
            }
        }

        List<MergePreviewDTO.ValueOption> options = new ArrayList<MergePreviewDTO.ValueOption>(optionMap.values());
        Object recommendedValue = primaryHasValue ? primaryValue : options.get(0).getValue();
        String recommendedKey = normalizeOptionValue(recommendedValue);
        for (int i = 0; i < options.size(); i++) {
            MergePreviewDTO.ValueOption option = options.get(i);
            option.setOptionKey(fieldName + "_option_" + i);
            option.setRecommended(normalizedEquals(option.getValue(), recommendedKey));
        }

        if (options.size() == 1 && !primaryHasValue) {
            MergePreviewDTO.ConflictField field = new MergePreviewDTO.ConflictField();
            field.setFieldName(fieldName);
            field.setFieldLabel(FIELD_LABELS.getOrDefault(fieldName, fieldName));
            field.setPrimaryValue(primaryValue);
            field.setRecommendedValue(recommendedValue);
            field.setOptions(options);
            conflicts.add(field);
            return;
        }

        if (options.size() > 1) {
            MergePreviewDTO.ConflictField field = new MergePreviewDTO.ConflictField();
            field.setFieldName(fieldName);
            field.setFieldLabel(FIELD_LABELS.getOrDefault(fieldName, fieldName));
            field.setPrimaryValue(primaryValue);
            field.setRecommendedValue(recommendedValue);
            field.setOptions(options);
            conflicts.add(field);
        }
    }

    private void addOption(LinkedHashMap<String, MergePreviewDTO.ValueOption> optionMap, Object value, String sourceLabel) {
        if (isEmptyValue(value)) {
            return;
        }
        String normalizedValue = normalizeOptionValue(value);
        MergePreviewDTO.ValueOption option = optionMap.get(normalizedValue);
        if (option == null) {
            option = new MergePreviewDTO.ValueOption();
            option.setValue(value);
            option.setSourceLabels(new ArrayList<String>());
            optionMap.put(normalizedValue, option);
        }
        option.getSourceLabels().add(sourceLabel);
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        return false;
    }

    private String normalizeOptionValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return String.valueOf(value);
    }

    private boolean normalizedEquals(Object value, String normalizedValue) {
        return normalizeOptionValue(value).equals(normalizedValue);
    }

    private Map<String, Integer> getAggregatedRelatedDataCounts(List<Integer> elderlyIds) {
        Map<String, Integer> totalCounts = createEmptyRelatedDataCounts();
        for (Integer elderlyId : elderlyIds) {
            Map<String, Integer> counts = getRelatedDataCounts(elderlyId);
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                totalCounts.put(entry.getKey(), totalCounts.get(entry.getKey()) + entry.getValue());
            }
        }
        return totalCounts;
    }

    private Map<String, Integer> getRelatedDataCounts(Integer elderlyId) {
        Map<String, Integer> counts = createEmptyRelatedDataCounts();
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

    private Map<String, Integer> createEmptyRelatedDataCounts() {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (String type : RELATED_DATA_TYPES) {
            counts.put(type, 0);
        }
        return counts;
    }

    @Transactional(rollbackFor = Exception.class)
    public void mergeElderly(ElderlyMergeDTO mergeDTO, String operator) {
        Integer primaryId = mergeDTO.getPrimaryElderlyId();
        Elderly primary = getAvailableElderly(primaryId, "主档案不存在");
        List<Integer> mergedIds = normalizeMergedIds(mergeDTO.getMergedElderlyIds(), primaryId);
        List<Elderly> mergedElderlyList = getAvailableMergedElderlyList(mergedIds, primaryId);

        if (mergeDTO.getFieldOverrides() != null && !mergeDTO.getFieldOverrides().isEmpty()) {
            applyFieldOverrides(primary, mergeDTO.getFieldOverrides());
            primary.setUpdatedAt(LocalDateTime.now());
            elderlyMapper.updateById(primary);
        }

        for (Elderly merged : mergedElderlyList) {
            migrateRelatedData(merged.getId(), primaryId);
            merged.setMergedToId(primaryId);
            merged.setMergedAt(LocalDateTime.now());
            merged.setMergedBy(operator);
            merged.setUpdatedAt(LocalDateTime.now());
            elderlyMapper.updateById(merged);
        }
    }

    private Elderly getAvailableElderly(Integer elderlyId, String notFoundMessage) {
        Elderly elderly = elderlyMapper.selectById(elderlyId);
        if (elderly == null) {
            throw new RuntimeException(notFoundMessage);
        }
        if (elderly.getMergedToId() != null) {
            throw new RuntimeException("该档案已被合并，不能继续操作");
        }
        return elderly;
    }

    private List<Integer> normalizeMergedIds(List<Integer> mergedIds, Integer primaryId) {
        if (mergedIds == null || mergedIds.isEmpty()) {
            throw new RuntimeException("被合并档案不能为空");
        }

        LinkedHashSet<Integer> uniqueIds = new LinkedHashSet<Integer>();
        for (Integer mergedId : mergedIds) {
            if (mergedId == null) {
                continue;
            }
            if (mergedId.equals(primaryId)) {
                throw new RuntimeException("主档案不能出现在被合并档案列表中");
            }
            uniqueIds.add(mergedId);
        }

        if (uniqueIds.isEmpty()) {
            throw new RuntimeException("被合并档案不能为空");
        }
        return new ArrayList<Integer>(uniqueIds);
    }

    private List<Elderly> getAvailableMergedElderlyList(List<Integer> mergedIds, Integer primaryId) {
        List<Elderly> mergedList = elderlyMapper.selectBatchIds(mergedIds);
        if (mergedList.size() != mergedIds.size()) {
            throw new RuntimeException("存在不存在的副档案");
        }

        Map<Integer, Elderly> mergedMap = mergedList.stream().collect(Collectors.toMap(Elderly::getId, Function.identity()));
        List<Elderly> orderedMergedList = new ArrayList<Elderly>();
        for (Integer mergedId : mergedIds) {
            Elderly merged = mergedMap.get(mergedId);
            if (merged == null) {
                throw new RuntimeException("存在不存在的副档案");
            }
            if (mergedId.equals(primaryId)) {
                throw new RuntimeException("主档案不能出现在被合并档案列表中");
            }
            if (merged.getMergedToId() != null) {
                throw new RuntimeException("档案ID " + mergedId + " 已被合并，不能再次合并");
            }
            orderedMergedList.add(merged);
        }
        return orderedMergedList;
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
                default:
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
