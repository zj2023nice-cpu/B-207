package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smart.elderly.dto.WorkbenchQueryDTO;
import com.smart.elderly.entity.*;
import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.mapper.HealthRecordMapper;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import com.smart.elderly.mapper.NotificationMapper;
import com.smart.elderly.vo.WorkbenchItemVO;
import com.smart.elderly.vo.WorkbenchStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkbenchService {

    private static final String ITEM_TYPE_WARNING = "WARNING";
    private static final String ITEM_TYPE_NOTIFICATION = "NOTIFICATION";
    private static final String ITEM_TYPE_HEALTH_RECORD = "HEALTH_RECORD";

    private static final String PRIORITY_HIGH = "HIGH";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_LOW = "LOW";

    private static final int SCORE_TYPE_WARNING = 100;
    private static final int SCORE_TYPE_NOTIFICATION = 70;
    private static final int SCORE_TYPE_HEALTH_RECORD = 50;

    private static final int SCORE_LEVEL_HIGH = 3;
    private static final int SCORE_LEVEL_MEDIUM = 2;
    private static final int SCORE_LEVEL_LOW = 1;

    @Autowired
    private HealthWarningRecordMapper warningRecordMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private NotificationPreferenceService preferenceService;

    public WorkbenchStatsVO getStats(Integer userId) {
        WorkbenchStatsVO stats = new WorkbenchStatsVO();
        List<WorkbenchItemVO> allItems = getAllWorkbenchItems(userId);

        stats.setTotalCount((long) allItems.size());
        stats.setHighPriorityCount(allItems.stream().filter(i -> PRIORITY_HIGH.equals(i.getPriority())).count());
        stats.setMediumPriorityCount(allItems.stream().filter(i -> PRIORITY_MEDIUM.equals(i.getPriority())).count());
        stats.setLowPriorityCount(allItems.stream().filter(i -> PRIORITY_LOW.equals(i.getPriority())).count());
        stats.setWarningCount(allItems.stream().filter(i -> ITEM_TYPE_WARNING.equals(i.getItemType())).count());
        stats.setNotificationCount(allItems.stream().filter(i -> ITEM_TYPE_NOTIFICATION.equals(i.getItemType())).count());
        stats.setHealthRecordCount(allItems.stream().filter(i -> ITEM_TYPE_HEALTH_RECORD.equals(i.getItemType())).count());

        Map<Integer, List<WorkbenchItemVO>> itemsByElderly = allItems.stream()
                .filter(i -> i.getElderlyId() != null)
                .collect(Collectors.groupingBy(WorkbenchItemVO::getElderlyId));

        List<Map<String, Object>> elderlyStats = new ArrayList<>();
        for (Map.Entry<Integer, List<WorkbenchItemVO>> entry : itemsByElderly.entrySet()) {
            Map<String, Object> stat = new HashMap<>();
            Elderly elderly = elderlyMapper.selectById(entry.getKey());
            stat.put("elderlyId", entry.getKey());
            stat.put("elderlyName", elderly != null ? elderly.getName() : "未知");
            stat.put("count", entry.getValue().size());
            stat.put("highPriorityCount", entry.getValue().stream().filter(i -> PRIORITY_HIGH.equals(i.getPriority())).count());
            elderlyStats.add(stat);
        }
        elderlyStats.sort((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")));
        stats.setElderlyStats(elderlyStats);

        List<WorkbenchItemVO> topItems = allItems.stream()
                .sorted(Comparator.comparingInt(WorkbenchItemVO::getPriorityScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
        stats.setTopPriorityItems(topItems);

        return stats;
    }

    public List<WorkbenchItemVO> getWorkbenchItems(WorkbenchQueryDTO queryDTO) {
        List<WorkbenchItemVO> allItems = getAllWorkbenchItems(queryDTO.getUserId());

        return allItems.stream()
                .filter(item -> {
                    if (queryDTO.getElderlyIds() != null && !queryDTO.getElderlyIds().isEmpty()) {
                        if (item.getElderlyId() == null || !queryDTO.getElderlyIds().contains(item.getElderlyId())) {
                            return false;
                        }
                    }
                    if (queryDTO.getPriorities() != null && !queryDTO.getPriorities().isEmpty()) {
                        if (!queryDTO.getPriorities().contains(item.getPriority())) {
                            return false;
                        }
                    }
                    if (queryDTO.getItemTypes() != null && !queryDTO.getItemTypes().isEmpty()) {
                        if (!queryDTO.getItemTypes().contains(item.getItemType())) {
                            return false;
                        }
                    }
                    if (queryDTO.getStartTime() != null) {
                        if (item.getCreatedAt() == null || item.getCreatedAt().isBefore(queryDTO.getStartTime())) {
                            return false;
                        }
                    }
                    if (queryDTO.getEndTime() != null) {
                        if (item.getCreatedAt() == null || item.getCreatedAt().isAfter(queryDTO.getEndTime())) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparingInt(WorkbenchItemVO::getPriorityScore).reversed()
                        .thenComparing(Comparator.comparing(WorkbenchItemVO::getCreatedAt).reversed()))
                .skip((long) (queryDTO.getPageNum() - 1) * queryDTO.getPageSize())
                .limit(queryDTO.getPageSize())
                .collect(Collectors.toList());
    }

    private List<WorkbenchItemVO> getAllWorkbenchItems(Integer userId) {
        List<WorkbenchItemVO> items = new ArrayList<>();

        items.addAll(collectWarningItems());
        items.addAll(collectNotificationItems(userId));
        items.addAll(collectHealthRecordItems());

        return items;
    }

    private List<WorkbenchItemVO> collectWarningItems() {
        List<WorkbenchItemVO> items = new ArrayList<>();

        List<String> pendingStatuses = Arrays.asList(
                HealthWarningStatus.PENDING.getCode(),
                HealthWarningStatus.READ.getCode(),
                HealthWarningStatus.REOPENED.getCode(),
                HealthWarningStatus.ESCALATED.getCode()
        );

        LambdaQueryWrapper<HealthWarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HealthWarningRecord::getStatus, pendingStatuses);
        wrapper.orderByDesc(HealthWarningRecord::getCreatedAt);

        List<HealthWarningRecord> warnings = warningRecordMapper.selectList(wrapper);
        List<Integer> elderlyIds = warnings.stream()
                .map(HealthWarningRecord::getElderlyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> elderlyNameMap = getElderlyNameMap(elderlyIds);

        for (HealthWarningRecord warning : warnings) {
            WorkbenchItemVO item = new WorkbenchItemVO();
            item.setItemId(ITEM_TYPE_WARNING + "_" + warning.getId());
            item.setItemType(ITEM_TYPE_WARNING);
            item.setItemTypeName("健康预警");
            item.setSourceId(warning.getId());
            item.setElderlyId(warning.getElderlyId());
            item.setElderlyName(elderlyNameMap.get(warning.getElderlyId()));
            item.setTitle(warning.getWarningMessage() != null ? warning.getWarningMessage() : "健康预警提醒");
            item.setDescription(buildWarningDescription(warning));
            item.setStatus(warning.getStatus());
            item.setStatusName(HealthWarningStatus.fromCode(warning.getStatus()).getDisplayName());
            item.setCreatedAt(warning.getCreatedAt());

            int levelScore = calculateWarningLevelScore(warning.getWarningLevel());
            int timeScore = calculateTimeDecayScore(warning.getCreatedAt());
            int totalScore = (SCORE_TYPE_WARNING + levelScore * 10) * timeScore / 100;

            item.setPriorityScore(totalScore);
            item.setPriority(determinePriority(totalScore));
            item.setPriorityName(getPriorityDisplayName(item.getPriority()));
            item.setQuickActions(buildWarningQuickActions(warning));
            item.setDetailUrl("/warning/record/" + warning.getId());
            item.setModuleUrl("/warning");

            items.add(item);
        }

        return items;
    }

    private List<WorkbenchItemVO> collectNotificationItems(Integer userId) {
        List<WorkbenchItemVO> items = new ArrayList<>();

        if (userId == null) {
            return items;
        }

        NotificationPreference preference = preferenceService.getByUserId(userId);
        List<String> highPriorityTypes = preferenceService.getHighPriorityTypesList(preference);
        List<String> enabledTypes = preferenceService.getEnabledTypesList(preference);

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.ne(Notification::getStatus, "READ");
        wrapper.ne(Notification::getInvalidated, true);
        wrapper.orderByDesc(Notification::getCreatedAt);

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        List<Integer> elderlyIds = notifications.stream()
                .map(Notification::getElderlyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> elderlyNameMap = getElderlyNameMap(elderlyIds);

        for (Notification notification : notifications) {
            boolean isHighPriority = highPriorityTypes.contains(notification.getNotificationType());
            boolean isEnabled = enabledTypes.isEmpty() || enabledTypes.contains(notification.getNotificationType());

            if (!isEnabled) {
                continue;
            }

            WorkbenchItemVO item = new WorkbenchItemVO();
            item.setItemId(ITEM_TYPE_NOTIFICATION + "_" + notification.getId());
            item.setItemType(ITEM_TYPE_NOTIFICATION);
            item.setItemTypeName("通知消息");
            item.setSourceId(notification.getId());
            item.setElderlyId(notification.getElderlyId());
            item.setElderlyName(elderlyNameMap.get(notification.getElderlyId()));
            item.setTitle(notification.getTitle());
            item.setDescription(notification.getContent());
            item.setStatus(notification.getStatus());
            item.setStatusName(notification.getStatus() != null ? notification.getStatus() : "未读");
            item.setCreatedAt(notification.getCreatedAt());

            int typeScore = isHighPriority ? SCORE_LEVEL_HIGH : SCORE_LEVEL_LOW;
            int timeScore = calculateTimeDecayScore(notification.getCreatedAt());
            int totalScore = (SCORE_TYPE_NOTIFICATION + typeScore * 15) * timeScore / 100;

            item.setPriorityScore(totalScore);
            item.setPriority(isHighPriority ? PRIORITY_HIGH : determinePriority(totalScore));
            item.setPriorityName(getPriorityDisplayName(item.getPriority()));
            item.setQuickActions(buildNotificationQuickActions(notification));
            item.setDetailUrl("/notification/" + notification.getId());
            item.setModuleUrl("/notification");

            items.add(item);
        }

        return items;
    }

    private List<WorkbenchItemVO> collectHealthRecordItems() {
        List<WorkbenchItemVO> items = new ArrayList<>();

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getIsAbnormal, true);
        wrapper.ge(HealthRecord::getCheckTime, twentyFourHoursAgo);
        wrapper.orderByDesc(HealthRecord::getCheckTime);

        List<HealthRecord> healthRecords = healthRecordMapper.selectList(wrapper);
        List<Integer> elderlyIds = healthRecords.stream()
                .map(HealthRecord::getElderlyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> elderlyNameMap = getElderlyNameMap(elderlyIds);

        for (HealthRecord record : healthRecords) {
            WorkbenchItemVO item = new WorkbenchItemVO();
            item.setItemId(ITEM_TYPE_HEALTH_RECORD + "_" + record.getId());
            item.setItemType(ITEM_TYPE_HEALTH_RECORD);
            item.setItemTypeName("异常健康记录");
            item.setSourceId(record.getId());
            item.setElderlyId(record.getElderlyId());
            item.setElderlyName(elderlyNameMap.get(record.getElderlyId()));
            item.setTitle("健康记录异常提醒");
            item.setDescription(buildHealthRecordDescription(record));
            item.setStatus(record.getIsAbnormal() ? "ABNORMAL" : "NORMAL");
            item.setStatusName(record.getIsAbnormal() ? "异常" : "正常");
            item.setCreatedAt(record.getCheckTime());

            int abnormalScore = calculateAbnormalSeverity(record);
            int timeScore = calculateTimeDecayScore(record.getCheckTime());
            int totalScore = (SCORE_TYPE_HEALTH_RECORD + abnormalScore * 10) * timeScore / 100;

            item.setPriorityScore(totalScore);
            item.setPriority(determinePriority(totalScore));
            item.setPriorityName(getPriorityDisplayName(item.getPriority()));
            item.setQuickActions(buildHealthRecordQuickActions(record));
            item.setDetailUrl("/health/record/" + record.getId());
            item.setModuleUrl("/health");

            items.add(item);
        }

        return items;
    }

    private String buildWarningDescription(HealthWarningRecord warning) {
        StringBuilder sb = new StringBuilder();
        if (warning.getIndicatorType() != null) {
            sb.append("指标: ").append(warning.getIndicatorType());
        }
        if (warning.getActualValue() != null && warning.getThresholdValue() != null) {
            sb.append(", 当前值: ").append(warning.getActualValue());
            sb.append(", 阈值: ").append(warning.getThresholdValue());
        }
        if (warning.getWarningLevel() != null) {
            sb.append(", 级别: ").append(warning.getWarningLevel());
        }
        return sb.toString();
    }

    private String buildHealthRecordDescription(HealthRecord record) {
        StringBuilder sb = new StringBuilder();
        List<String> abnormalIndicators = new ArrayList<>();
        if (record.getSystolicPressure() != null && record.getDiastolicPressure() != null) {
            abnormalIndicators.add("血压:" + record.getSystolicPressure() + "/" + record.getDiastolicPressure());
        }
        if (record.getTemperature() != null) {
            abnormalIndicators.add("体温:" + record.getTemperature());
        }
        if (record.getHeartRate() != null) {
            abnormalIndicators.add("心率:" + record.getHeartRate());
        }
        if (record.getBloodOxygen() != null) {
            abnormalIndicators.add("血氧:" + record.getBloodOxygen());
        }
        if (record.getBloodSugar() != null) {
            abnormalIndicators.add("血糖:" + record.getBloodSugar());
        }
        if (!abnormalIndicators.isEmpty()) {
            sb.append(String.join(", ", abnormalIndicators));
        }
        if (record.getAbnormalReason() != null && !record.getAbnormalReason().isEmpty()) {
            sb.append(" - 原因: ").append(record.getAbnormalReason());
        }
        return sb.toString();
    }

    private int calculateWarningLevelScore(String warningLevel) {
        if (warningLevel == null) {
            return SCORE_LEVEL_MEDIUM;
        }
        switch (warningLevel.toUpperCase()) {
            case "HIGH":
            case "严重":
                return SCORE_LEVEL_HIGH;
            case "MEDIUM":
            case "中等":
                return SCORE_LEVEL_MEDIUM;
            case "LOW":
            case "轻微":
                return SCORE_LEVEL_LOW;
            default:
                return SCORE_LEVEL_MEDIUM;
        }
    }

    private int calculateAbnormalSeverity(HealthRecord record) {
        int score = 0;
        if (record.getSystolicPressure() != null) {
            int sys = record.getSystolicPressure();
            if (sys >= 180 || sys <= 90) score += 3;
            else if (sys >= 160 || sys <= 100) score += 2;
            else if (sys >= 140) score += 1;
        }
        if (record.getTemperature() != null) {
            double temp = record.getTemperature().doubleValue();
            if (temp >= 39 || temp <= 35) score += 3;
            else if (temp >= 38.5 || temp <= 35.5) score += 2;
            else if (temp >= 37.5) score += 1;
        }
        if (record.getHeartRate() != null) {
            int hr = record.getHeartRate();
            if (hr >= 120 || hr <= 50) score += 3;
            else if (hr >= 100 || hr <= 60) score += 2;
        }
        if (record.getBloodOxygen() != null) {
            int spo2 = record.getBloodOxygen();
            if (spo2 <= 90) score += 3;
            else if (spo2 <= 93) score += 2;
            else if (spo2 <= 95) score += 1;
        }
        return Math.min(score, SCORE_LEVEL_HIGH);
    }

    private int calculateTimeDecayScore(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 100;
        }
        long hours = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
        if (hours <= 1) return 100;
        if (hours <= 6) return 90;
        if (hours <= 12) return 80;
        if (hours <= 24) return 70;
        if (hours <= 48) return 60;
        if (hours <= 72) return 50;
        return 40;
    }

    private String determinePriority(int totalScore) {
        if (totalScore >= 120) return PRIORITY_HIGH;
        if (totalScore >= 80) return PRIORITY_MEDIUM;
        return PRIORITY_LOW;
    }

    private String getPriorityDisplayName(String priority) {
        switch (priority) {
            case PRIORITY_HIGH:
                return "高";
            case PRIORITY_MEDIUM:
                return "中";
            case PRIORITY_LOW:
                return "低";
            default:
                return "未知";
        }
    }

    private List<WorkbenchItemVO.QuickAction> buildWarningQuickActions(HealthWarningRecord warning) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<>();
        String status = warning.getStatus();

        if (HealthWarningStatus.PENDING.getCode().equals(status)) {
            WorkbenchItemVO.QuickAction readAction = new WorkbenchItemVO.QuickAction();
            readAction.setActionKey("READ");
            readAction.setActionName("标记已读");
            readAction.setActionType("PRIMARY");
            readAction.setApiUrl("/api/warning/record/read/" + warning.getId());
            readAction.setMethod("PUT");
            readAction.setRequireConfirm(false);
            actions.add(readAction);
        }

        if (Arrays.asList(HealthWarningStatus.PENDING.getCode(), HealthWarningStatus.READ.getCode(),
                HealthWarningStatus.REOPENED.getCode(), HealthWarningStatus.ESCALATED.getCode()).contains(status)) {
            WorkbenchItemVO.QuickAction handleAction = new WorkbenchItemVO.QuickAction();
            handleAction.setActionKey("HANDLE");
            handleAction.setActionName("处理预警");
            handleAction.setActionType("SUCCESS");
            handleAction.setApiUrl("/api/warning/record/handle/" + warning.getId());
            handleAction.setMethod("PUT");
            handleAction.setRequireConfirm(true);
            handleAction.setConfirmText("确认处理该预警？");
            actions.add(handleAction);

            WorkbenchItemVO.QuickAction ignoreAction = new WorkbenchItemVO.QuickAction();
            ignoreAction.setActionKey("IGNORE");
            ignoreAction.setActionName("忽略预警");
            ignoreAction.setActionType("DEFAULT");
            ignoreAction.setApiUrl("/api/warning/record/ignore/" + warning.getId());
            ignoreAction.setMethod("PUT");
            ignoreAction.setRequireConfirm(true);
            ignoreAction.setConfirmText("确认忽略该预警？");
            actions.add(ignoreAction);
        }

        return actions;
    }

    private List<WorkbenchItemVO.QuickAction> buildNotificationQuickActions(Notification notification) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<>();

        if (!"READ".equals(notification.getStatus())) {
            WorkbenchItemVO.QuickAction readAction = new WorkbenchItemVO.QuickAction();
            readAction.setActionKey("READ");
            readAction.setActionName("标记已读");
            readAction.setActionType("PRIMARY");
            readAction.setApiUrl("/api/notification/mark-read/" + notification.getId());
            readAction.setMethod("PUT");
            readAction.setRequireConfirm(false);
            actions.add(readAction);
        }

        return actions;
    }

    private List<WorkbenchItemVO.QuickAction> buildHealthRecordQuickActions(HealthRecord record) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<>();

        WorkbenchItemVO.QuickAction viewAction = new WorkbenchItemVO.QuickAction();
        viewAction.setActionKey("VIEW");
        viewAction.setActionName("查看详情");
        viewAction.setActionType("PRIMARY");
        viewAction.setApiUrl("/api/health/record/" + record.getId());
        viewAction.setMethod("GET");
        viewAction.setRequireConfirm(false);
        actions.add(viewAction);

        return actions;
    }

    private Map<Integer, String> getElderlyNameMap(List<Integer> elderlyIds) {
        if (elderlyIds == null || elderlyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Elderly> elderlyList = elderlyMapper.selectBatchIds(elderlyIds);
        return elderlyList.stream()
                .collect(Collectors.toMap(Elderly::getId, Elderly::getName, (a, b) -> a));
    }

    public List<Map<String, String>> getFilterOptions() {
        List<Map<String, String>> options = new ArrayList<>();

        Map<String, String> typeOption1 = new HashMap<>();
        typeOption1.put("value", ITEM_TYPE_WARNING);
        typeOption1.put("label", "健康预警");
        options.add(typeOption1);

        Map<String, String> typeOption2 = new HashMap<>();
        typeOption2.put("value", ITEM_TYPE_NOTIFICATION);
        typeOption2.put("label", "通知消息");
        options.add(typeOption2);

        Map<String, String> typeOption3 = new HashMap<>();
        typeOption3.put("value", ITEM_TYPE_HEALTH_RECORD);
        typeOption3.put("label", "异常健康记录");
        options.add(typeOption3);

        return options;
    }

    public List<Map<String, String>> getPriorityOptions() {
        List<Map<String, String>> options = new ArrayList<>();

        Map<String, String> option1 = new HashMap<>();
        option1.put("value", PRIORITY_HIGH);
        option1.put("label", "高优先级");
        options.add(option1);

        Map<String, String> option2 = new HashMap<>();
        option2.put("value", PRIORITY_MEDIUM);
        option2.put("label", "中优先级");
        options.add(option2);

        Map<String, String> option3 = new HashMap<>();
        option3.put("value", PRIORITY_LOW);
        option3.put("label", "低优先级");
        options.add(option3);

        return options;
    }
}
