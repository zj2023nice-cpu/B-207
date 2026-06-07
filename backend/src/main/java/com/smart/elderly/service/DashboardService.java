package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smart.elderly.entity.*;
import com.smart.elderly.mapper.ElderlyFollowMapper;
import com.smart.elderly.mapper.NotificationMapper;
import com.smart.elderly.vo.DashboardOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private HealthWarningRecordService warningRecordService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private NotificationSubscriptionService subscriptionService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ElderlyFollowMapper elderlyFollowMapper;

    public DashboardOverviewVO getOverview(Integer userId) {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        vo.setElderlyTotal(countElderlyTotal());
        vo.setTodayHealthRecords(countTodayHealthRecords());
        vo.setRecentAbnormalElderly(countRecentAbnormalElderly());
        vo.setPendingWarnings(countPendingWarnings());
        vo.setUnreadNotifications(countUnreadNotifications(userId));
        vo.setRecentActivities(getRecentActivities());

        return vo;
    }

    private Long countElderlyTotal() {
        return elderlyService.count();
    }

    private Long countTodayHealthRecords() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(HealthRecord::getCheckTime, startOfDay)
               .le(HealthRecord::getCheckTime, endOfDay);

        return healthRecordService.count(wrapper);
    }

    private Long countRecentAbnormalElderly() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getIsAbnormal, true)
               .ge(HealthRecord::getCheckTime, twentyFourHoursAgo)
               .groupBy(HealthRecord::getElderlyId);

        return (long) healthRecordService.list(wrapper).size();
    }

    private Long countPendingWarnings() {
        return warningRecordService.countPending();
    }

    private Long countUnreadNotifications(Integer userId) {
        if (userId == null) {
            return 0L;
        }

        NotificationSubscription subscription = subscriptionService.getByUserId(userId);
        boolean subscriptionEnabled = subscription.getEnabled() != null && subscription.getEnabled();

        if (!subscriptionEnabled) {
            return notificationMapper.countVisibleUnread(userId);
        }

        List<String> notificationTypes = subscriptionService.getNotificationTypesList(subscription);
        Boolean onlyAbnormal = subscription.getOnlyAbnormal();
        Boolean onlyFollowedElderly = subscription.getOnlyFollowedElderly();
        List<Integer> followedElderlyIds = null;

        if (Boolean.TRUE.equals(onlyFollowedElderly)) {
            List<Integer> followedIds = elderlyFollowMapper.getFollowedElderlyIds(userId);
            followedElderlyIds = followedIds == null ? Collections.emptyList() : followedIds;
        }

        return notificationMapper.countAllUnreadWithSubscription(
                userId,
                notificationTypes,
                onlyAbnormal,
                onlyFollowedElderly,
                followedElderlyIds
        );
    }

    private List<OperationLog> getRecentActivities() {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getOperationTime)
               .last("LIMIT 20");

        return operationLogService.list(wrapper);
    }
}
