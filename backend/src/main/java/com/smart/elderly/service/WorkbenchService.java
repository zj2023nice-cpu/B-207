package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smart.elderly.common.WorkbenchConstants;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.WorkbenchQueryDTO;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.entity.WarningFollowupTask;
import com.smart.elderly.enums.NotificationStatus;
import com.smart.elderly.enums.PriorityLevel;
import com.smart.elderly.enums.WarningFollowupTaskStatus;
import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.enums.WarningLevel;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.mapper.HealthRecordMapper;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import com.smart.elderly.mapper.WarningFollowupTaskMapper;
import com.smart.elderly.vo.WorkbenchItemVO;
import com.smart.elderly.vo.WorkbenchStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkbenchService {

    private static final String ITEM_TYPE_WARNING = WorkbenchConstants.ITEM_TYPE_WARNING;
    private static final String ITEM_TYPE_NOTIFICATION = WorkbenchConstants.ITEM_TYPE_NOTIFICATION;
    private static final String ITEM_TYPE_HEALTH_RECORD = WorkbenchConstants.ITEM_TYPE_HEALTH_RECORD;
    private static final String ITEM_TYPE_FOLLOWUP_TASK = WorkbenchConstants.ITEM_TYPE_FOLLOWUP_TASK;

    private static final int SCORE_TYPE_FOLLOWUP_TASK = WorkbenchConstants.SCORE_TYPE_FOLLOWUP_TASK;

    private static final String PRIORITY_HIGH = PriorityLevel.HIGH.getCode();
    private static final String PRIORITY_MEDIUM = PriorityLevel.MEDIUM.getCode();
    private static final String PRIORITY_LOW = PriorityLevel.LOW.getCode();

    private static final int SCORE_TYPE_WARNING = WorkbenchConstants.SCORE_TYPE_WARNING;
    private static final int SCORE_TYPE_NOTIFICATION = WorkbenchConstants.SCORE_TYPE_NOTIFICATION;
    private static final int SCORE_TYPE_HEALTH_RECORD = WorkbenchConstants.SCORE_TYPE_HEALTH_RECORD;

    private static final int SCORE_LEVEL_HIGH = WorkbenchConstants.SCORE_LEVEL_HIGH;
    private static final int SCORE_LEVEL_MEDIUM = WorkbenchConstants.SCORE_LEVEL_MEDIUM;
    private static final int SCORE_LEVEL_LOW = WorkbenchConstants.SCORE_LEVEL_LOW;

    @Autowired
    private HealthWarningRecordMapper warningRecordMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private WarningFollowupTaskMapper followupTaskMapper;

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ElderlyFollowService elderlyFollowService;

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
        stats.setFollowupTaskCount(allItems.stream().filter(i -> ITEM_TYPE_FOLLOWUP_TASK.equals(i.getItemType())).count());

        Map<Integer, List<WorkbenchItemVO>> itemsByElderly = allItems.stream()
                .filter(i -> i.getElderlyId() != null)
                .collect(Collectors.groupingBy(WorkbenchItemVO::getElderlyId));

        List<Map<String, Object>> elderlyStats = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Integer, List<WorkbenchItemVO>> entry : itemsByElderly.entrySet()) {
            Map<String, Object> stat = new HashMap<String, Object>();
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

    public List<WorkbenchItemVO> getWorkbenchItems(Integer userId, WorkbenchQueryDTO queryDTO) {
        List<WorkbenchItemVO> allItems = getAllWorkbenchItems(userId);

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
        List<WorkbenchItemVO> items = new ArrayList<WorkbenchItemVO>();
        boolean isAdmin = UserContextHolder.isAdmin();
        List<Integer> visibleElderlyIds = isAdmin ? Collections.<Integer>emptyList() : elderlyFollowService.getFollowedIds();

        items.addAll(collectWarningItems(isAdmin, visibleElderlyIds));
        items.addAll(collectNotificationItems(userId));
        items.addAll(collectHealthRecordItems(isAdmin, visibleElderlyIds));
        items.addAll(collectFollowupTaskItems(userId, isAdmin, visibleElderlyIds));

        return items;
    }

    private List<WorkbenchItemVO> collectWarningItems(boolean isAdmin, List<Integer> visibleElderlyIds) {
        if (!isAdmin && (visibleElderlyIds == null || visibleElderlyIds.isEmpty())) {
            return Collections.emptyList();
        }

        List<String> pendingStatuses = HealthWarningStatus.getPendingStatusCodes();

        LambdaQueryWrapper<HealthWarningRecord> wrapper = new LambdaQueryWrapper<HealthWarningRecord>();
        wrapper.in(HealthWarningRecord::getStatus, pendingStatuses);
        if (!isAdmin) {
            wrapper.in(HealthWarningRecord::getElderlyId, visibleElderlyIds);
        }
        wrapper.orderByDesc(HealthWarningRecord::getCreatedAt);

        List<HealthWarningRecord> warnings = warningRecordMapper.selectList(wrapper);
        List<Integer> elderlyIds = warnings.stream()
                .map(HealthWarningRecord::getElderlyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> elderlyNameMap = getElderlyNameMap(elderlyIds);

        List<WorkbenchItemVO> items = new ArrayList<WorkbenchItemVO>();
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
            item.setStatusName(resolveWarningStatusName(warning.getStatus()));
            item.setCreatedAt(warning.getCreatedAt());

            int levelScore = calculateWarningLevelScore(warning.getWarningLevel());
            int timeScore = calculateTimeDecayScore(warning.getCreatedAt());
            int totalScore = (SCORE_TYPE_WARNING + levelScore * 10) * timeScore / 100;

            item.setPriorityScore(totalScore);
            item.setPriority(determinePriority(totalScore));
            item.setPriorityName(getPriorityDisplayName(item.getPriority()));
            item.setDetailUrl("/warning?id=" + warning.getId());
            item.setModuleUrl("/warning");
            item.setQuickActions(buildWarningQuickActions(warning));
            items.add(item);
        }

        return items;
    }

    private List<WorkbenchItemVO> collectNotificationItems(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Notification> notifications = notificationService.getAllUnreadWithPreference(userId);
        List<WorkbenchItemVO> items = new ArrayList<WorkbenchItemVO>();

        for (Notification notification : notifications) {
            WorkbenchItemVO item = new WorkbenchItemVO();
            item.setItemId(ITEM_TYPE_NOTIFICATION + "_" + notification.getId());
            item.setItemType(ITEM_TYPE_NOTIFICATION);
            item.setItemTypeName("通知消息");
            item.setSourceId(notification.getId());
            item.setElderlyId(notification.getElderlyId());
            item.setElderlyName(notification.getElderlyName());
            item.setTitle(notification.getTitle());
            item.setDescription(notification.getContent());
            item.setStatus(notification.getStatus());
            item.setStatusName(NotificationStatus.fromCode(notification.getStatus()).getDisplayName());
            item.setCreatedAt(notification.getCreatedAt());

            boolean isHighPriority = Boolean.TRUE.equals(notification.getHighPriority());
            int typeScore = isHighPriority ? SCORE_LEVEL_HIGH : SCORE_LEVEL_LOW;
            int timeScore = calculateTimeDecayScore(notification.getCreatedAt());
            int totalScore = (SCORE_TYPE_NOTIFICATION + typeScore * 15) * timeScore / 100;

            item.setPriorityScore(totalScore);
            item.setPriority(isHighPriority ? PRIORITY_HIGH : determinePriority(totalScore));
            item.setPriorityName(getPriorityDisplayName(item.getPriority()));
            item.setDetailUrl("/notification?id=" + notification.getId());
            item.setModuleUrl("/notification");
            item.setQuickActions(buildNotificationQuickActions(notification));
            items.add(item);
        }

        return items;
    }

    private List<WorkbenchItemVO> collectHealthRecordItems(boolean isAdmin, List<Integer> visibleElderlyIds) {
        if (!isAdmin && (visibleElderlyIds == null || visibleElderlyIds.isEmpty())) {
            return Collections.emptyList();
        }

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<HealthRecord>();
        wrapper.eq(HealthRecord::getIsAbnormal, true);
        wrapper.ge(HealthRecord::getCheckTime, twentyFourHoursAgo);
        if (!isAdmin) {
            wrapper.in(HealthRecord::getElderlyId, visibleElderlyIds);
        }
        wrapper.orderByDesc(HealthRecord::getCheckTime);

        List<HealthRecord> healthRecords = healthRecordMapper.selectList(wrapper);
        List<Integer> elderlyIds = healthRecords.stream()
                .map(HealthRecord::getElderlyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> elderlyNameMap = getElderlyNameMap(elderlyIds);

        List<WorkbenchItemVO> items = new ArrayList<WorkbenchItemVO>();
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
            item.setDetailUrl("/health?elderlyId=" + record.getElderlyId() + "&recordId=" + record.getId());
            item.setModuleUrl("/health");
            item.setQuickActions(buildHealthRecordQuickActions(record));
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
        List<String> abnormalIndicators = new ArrayList<String>();
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
        return WarningLevel.fromCode(warningLevel).getScore();
    }

    private int calculateAbnormalSeverity(HealthRecord record) {
        int score = 0;
        if (record.getSystolicPressure() != null) {
            int sys = record.getSystolicPressure();
            if (sys >= 180 || sys <= 90) {
                score += 3;
            } else if (sys >= 160 || sys <= 100) {
                score += 2;
            } else if (sys >= 140) {
                score += 1;
            }
        }
        if (record.getTemperature() != null) {
            double temp = record.getTemperature().doubleValue();
            if (temp >= 39 || temp <= 35) {
                score += 3;
            } else if (temp >= 38.5 || temp <= 35.5) {
                score += 2;
            } else if (temp >= 37.5) {
                score += 1;
            }
        }
        if (record.getHeartRate() != null) {
            int hr = record.getHeartRate();
            if (hr >= 120 || hr <= 50) {
                score += 3;
            } else if (hr >= 100 || hr <= 60) {
                score += 2;
            }
        }
        if (record.getBloodOxygen() != null) {
            int spo2 = record.getBloodOxygen();
            if (spo2 <= 90) {
                score += 3;
            } else if (spo2 <= 93) {
                score += 2;
            } else if (spo2 <= 95) {
                score += 1;
            }
        }
        return Math.min(score, SCORE_LEVEL_HIGH);
    }

    private int calculateTimeDecayScore(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 100;
        }
        long hours = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
        if (hours <= 1) {
            return 100;
        }
        if (hours <= 6) {
            return 90;
        }
        if (hours <= 12) {
            return 80;
        }
        if (hours <= 24) {
            return 70;
        }
        if (hours <= 48) {
            return 60;
        }
        if (hours <= 72) {
            return 50;
        }
        return 40;
    }

    private String determinePriority(int totalScore) {
        return PriorityLevel.fromScore(totalScore, 
            WorkbenchConstants.PRIORITY_HIGH_THRESHOLD, 
            WorkbenchConstants.PRIORITY_MEDIUM_THRESHOLD).getCode();
    }

    private String getPriorityDisplayName(String priority) {
        return PriorityLevel.fromCode(priority).getDisplayName();
    }

    private String resolveWarningStatusName(String status) {
        HealthWarningStatus warningStatus = HealthWarningStatus.fromCode(status);
        return warningStatus != null ? warningStatus.getDisplayName() : status;
    }

    private List<WorkbenchItemVO.QuickAction> buildWarningQuickActions(HealthWarningRecord warning) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<WorkbenchItemVO.QuickAction>();

        WorkbenchItemVO.QuickAction openAction = new WorkbenchItemVO.QuickAction();
        openAction.setActionKey(WorkbenchConstants.ACTION_OPEN);
        openAction.setActionName("查看/处理");
        openAction.setActionType(WorkbenchConstants.ACTION_TYPE_PRIMARY);
        openAction.setApiUrl("/warning?id=" + warning.getId());
        openAction.setMethod(WorkbenchConstants.METHOD_ROUTE);
        openAction.setRequireConfirm(false);
        actions.add(openAction);

        if (HealthWarningStatus.PENDING.getCode().equals(warning.getStatus())) {
            WorkbenchItemVO.QuickAction readAction = new WorkbenchItemVO.QuickAction();
            readAction.setActionKey(WorkbenchConstants.ACTION_READ);
            readAction.setActionName("标记已读");
            readAction.setActionType(WorkbenchConstants.ACTION_TYPE_INFO);
            readAction.setApiUrl("/api/warning/record/read/" + warning.getId());
            readAction.setMethod(WorkbenchConstants.METHOD_PUT);
            readAction.setRequireConfirm(false);
            actions.add(readAction);
        }

        return actions;
    }

    private List<WorkbenchItemVO.QuickAction> buildNotificationQuickActions(Notification notification) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<WorkbenchItemVO.QuickAction>();

        WorkbenchItemVO.QuickAction openAction = new WorkbenchItemVO.QuickAction();
        openAction.setActionKey(WorkbenchConstants.ACTION_OPEN);
        openAction.setActionName("打开通知");
        openAction.setActionType(WorkbenchConstants.ACTION_TYPE_PRIMARY);
        openAction.setApiUrl("/notification?id=" + notification.getId());
        openAction.setMethod(WorkbenchConstants.METHOD_ROUTE);
        openAction.setRequireConfirm(false);
        actions.add(openAction);

        NotificationStatus status = NotificationStatus.fromCode(notification.getStatus());
        if (status != NotificationStatus.READ) {
            WorkbenchItemVO.QuickAction readAction = new WorkbenchItemVO.QuickAction();
            readAction.setActionKey(WorkbenchConstants.ACTION_READ);
            readAction.setActionName("标记已读");
            readAction.setActionType(WorkbenchConstants.ACTION_TYPE_INFO);
            readAction.setApiUrl("/api/notification/read/" + notification.getId());
            readAction.setMethod(WorkbenchConstants.METHOD_PUT);
            readAction.setRequireConfirm(false);
            actions.add(readAction);
        }

        return actions;
    }

    private List<WorkbenchItemVO.QuickAction> buildHealthRecordQuickActions(HealthRecord record) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<WorkbenchItemVO.QuickAction>();
        WorkbenchItemVO.QuickAction viewAction = new WorkbenchItemVO.QuickAction();
        viewAction.setActionKey(WorkbenchConstants.ACTION_VIEW);
        viewAction.setActionName("查看详情");
        viewAction.setActionType(WorkbenchConstants.ACTION_TYPE_PRIMARY);
        viewAction.setApiUrl("/health?elderlyId=" + record.getElderlyId() + "&recordId=" + record.getId());
        viewAction.setMethod(WorkbenchConstants.METHOD_ROUTE);
        viewAction.setRequireConfirm(false);
        actions.add(viewAction);
        return actions;
    }

    private Map<Integer, String> getElderlyNameMap(List<Integer> elderlyIds) {
        if (elderlyIds == null || elderlyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Elderly> elderlyList = elderlyMapper.selectBatchIds(elderlyIds);
        return elderlyList.stream().collect(Collectors.toMap(Elderly::getId, Elderly::getName, (a, b) -> a));
    }

    private List<WorkbenchItemVO> collectFollowupTaskItems(Integer userId, boolean isAdmin, List<Integer> visibleElderlyIds) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<String> activeStatuses = WarningFollowupTaskStatus.getActiveStatusCodes();

        LambdaQueryWrapper<WarningFollowupTask> wrapper = new LambdaQueryWrapper<WarningFollowupTask>();
        wrapper.in(WarningFollowupTask::getStatus, activeStatuses);
        wrapper.eq(WarningFollowupTask::getAssigneeId, userId);
        if (!isAdmin && visibleElderlyIds != null && !visibleElderlyIds.isEmpty()) {
            wrapper.in(WarningFollowupTask::getElderlyId, visibleElderlyIds);
        }
        wrapper.orderByAsc(WarningFollowupTask::getDeadline);

        List<WarningFollowupTask> tasks = followupTaskMapper.selectList(wrapper);
        List<Integer> elderlyIds = tasks.stream()
                .map(WarningFollowupTask::getElderlyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> elderlyNameMap = getElderlyNameMap(elderlyIds);

        List<WorkbenchItemVO> items = new ArrayList<WorkbenchItemVO>();
        for (WarningFollowupTask task : tasks) {
            WorkbenchItemVO item = new WorkbenchItemVO();
            item.setItemId(ITEM_TYPE_FOLLOWUP_TASK + "_" + task.getId());
            item.setItemType(ITEM_TYPE_FOLLOWUP_TASK);
            item.setItemTypeName("跟进任务");
            item.setSourceId(task.getId());
            item.setElderlyId(task.getElderlyId());
            item.setElderlyName(elderlyNameMap.get(task.getElderlyId()));
            item.setTitle(task.getTitle());
            item.setDescription(buildFollowupTaskDescription(task));
            item.setStatus(task.getStatus());
            item.setStatusName(resolveFollowupTaskStatusName(task.getStatus()));
            item.setCreatedAt(task.getCreatedAt());
            item.setDeadline(task.getDeadline());

            int priorityScore = calculateFollowupTaskPriorityScore(task);
            int timeScore = calculateFollowupTaskTimeScore(task.getDeadline());
            int totalScore = (SCORE_TYPE_FOLLOWUP_TASK + priorityScore * 10) * timeScore / 100;

            item.setPriorityScore(totalScore);
            item.setPriority(determinePriority(totalScore));
            item.setPriorityName(getPriorityDisplayName(item.getPriority()));
            item.setDetailUrl("/warning/followup-task?id=" + task.getId());
            item.setModuleUrl("/warning/followup-task");
            item.setQuickActions(buildFollowupTaskQuickActions(task));
            items.add(item);
        }

        return items;
    }

    private String buildFollowupTaskDescription(WarningFollowupTask task) {
        StringBuilder sb = new StringBuilder();
        if (task.getPriority() != null) {
            sb.append("优先级: ").append(getPriorityDisplayName(task.getPriority()));
        }
        if (task.getDeadline() != null) {
            sb.append(", 截止时间: ").append(task.getDeadline());
        }
        if (task.getAssigneeName() != null) {
            sb.append(", 负责人: ").append(task.getAssigneeName());
        }
        return sb.toString();
    }

    private String resolveFollowupTaskStatusName(String status) {
        WarningFollowupTaskStatus taskStatus = WarningFollowupTaskStatus.fromCode(status);
        return taskStatus != null ? taskStatus.getDisplayName() : status;
    }

    private int calculateFollowupTaskPriorityScore(WarningFollowupTask task) {
        return PriorityLevel.fromCode(task.getPriority()).getScore();
    }

    private int calculateFollowupTaskTimeScore(LocalDateTime deadline) {
        if (deadline == null) {
            return 50;
        }
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), deadline);
        if (hours < 0) {
            return 100;
        }
        if (hours <= 1) {
            return 100;
        }
        if (hours <= 6) {
            return 90;
        }
        if (hours <= 24) {
            return 80;
        }
        if (hours <= 72) {
            return 60;
        }
        return 40;
    }

    private List<WorkbenchItemVO.QuickAction> buildFollowupTaskQuickActions(WarningFollowupTask task) {
        List<WorkbenchItemVO.QuickAction> actions = new ArrayList<WorkbenchItemVO.QuickAction>();

        WorkbenchItemVO.QuickAction openAction = new WorkbenchItemVO.QuickAction();
        openAction.setActionKey(WorkbenchConstants.ACTION_OPEN);
        openAction.setActionName("查看详情");
        openAction.setActionType(WorkbenchConstants.ACTION_TYPE_PRIMARY);
        openAction.setApiUrl("/warning/followup-task?id=" + task.getId());
        openAction.setMethod(WorkbenchConstants.METHOD_ROUTE);
        openAction.setRequireConfirm(false);
        actions.add(openAction);

        if (WarningFollowupTaskStatus.PENDING.getCode().equals(task.getStatus())) {
            WorkbenchItemVO.QuickAction startAction = new WorkbenchItemVO.QuickAction();
            startAction.setActionKey(WorkbenchConstants.ACTION_START);
            startAction.setActionName("开始处理");
            startAction.setActionType(WorkbenchConstants.ACTION_TYPE_WARNING);
            startAction.setApiUrl("/api/warning/followup-task/start/" + task.getId());
            startAction.setMethod(WorkbenchConstants.METHOD_PUT);
            startAction.setRequireConfirm(false);
            actions.add(startAction);
        }

        if (WarningFollowupTaskStatus.PENDING.getCode().equals(task.getStatus())
                || WarningFollowupTaskStatus.IN_PROGRESS.getCode().equals(task.getStatus())
                || WarningFollowupTaskStatus.OVERDUE.getCode().equals(task.getStatus())) {
            WorkbenchItemVO.QuickAction completeAction = new WorkbenchItemVO.QuickAction();
            completeAction.setActionKey(WorkbenchConstants.ACTION_COMPLETE);
            completeAction.setActionName("完成");
            completeAction.setActionType(WorkbenchConstants.ACTION_TYPE_SUCCESS);
            completeAction.setApiUrl("/warning/followup-task?id=" + task.getId());
            completeAction.setMethod(WorkbenchConstants.METHOD_ROUTE);
            completeAction.setRequireConfirm(false);
            actions.add(completeAction);
        }

        return actions;
    }

    public List<Map<String, String>> getFilterOptions() {
        List<Map<String, String>> options = new ArrayList<Map<String, String>>();

        Map<String, String> typeOption1 = new HashMap<String, String>();
        typeOption1.put("value", ITEM_TYPE_WARNING);
        typeOption1.put("label", "健康预警");
        options.add(typeOption1);

        Map<String, String> typeOption2 = new HashMap<String, String>();
        typeOption2.put("value", ITEM_TYPE_NOTIFICATION);
        typeOption2.put("label", "通知消息");
        options.add(typeOption2);

        Map<String, String> typeOption3 = new HashMap<String, String>();
        typeOption3.put("value", ITEM_TYPE_HEALTH_RECORD);
        typeOption3.put("label", "异常健康记录");
        options.add(typeOption3);

        Map<String, String> typeOption4 = new HashMap<String, String>();
        typeOption4.put("value", ITEM_TYPE_FOLLOWUP_TASK);
        typeOption4.put("label", "跟进任务");
        options.add(typeOption4);

        return options;
    }

    public List<Map<String, String>> getPriorityOptions() {
        List<Map<String, String>> options = new ArrayList<Map<String, String>>();

        Map<String, String> option1 = new HashMap<String, String>();
        option1.put("value", PRIORITY_HIGH);
        option1.put("label", "高优先级");
        options.add(option1);

        Map<String, String> option2 = new HashMap<String, String>();
        option2.put("value", PRIORITY_MEDIUM);
        option2.put("label", "中优先级");
        options.add(option2);

        Map<String, String> option3 = new HashMap<String, String>();
        option3.put("value", PRIORITY_LOW);
        option3.put("label", "低优先级");
        options.add(option3);

        return options;
    }
}
