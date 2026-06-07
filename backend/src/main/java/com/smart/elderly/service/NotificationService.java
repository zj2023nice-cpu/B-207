package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.entity.NotificationSubscription;
import com.smart.elderly.enums.NotificationType;
import com.smart.elderly.mapper.ElderlyFollowMapper;
import com.smart.elderly.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

    @Autowired
    private NotificationSubscriptionService subscriptionService;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private NotificationReadRecordService notificationReadRecordService;

    @Autowired
    private ElderlyFollowMapper elderlyFollowMapper;

    public List<Notification> getAllWithElderlyName(Integer userId) {
        return baseMapper.findAllVisibleWithElderlyName(userId);
    }

    public List<Notification> getAllUnreadWithElderlyName(Integer userId) {
        return baseMapper.findAllVisibleUnreadWithElderlyName(userId);
    }

    public List<Notification> getByElderlyIdWithName(Integer elderlyId, Integer userId) {
        return baseMapper.findVisibleByElderlyIdWithName(elderlyId, userId);
    }

    public Map<String, Object> countUnread(Integer userId) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("unreadCount", baseMapper.countVisibleUnread(userId));
        return result;
    }

    public List<Notification> getAllWithSubscription(Integer userId) {
        NotificationQueryContext queryContext = buildQueryContext(userId);
        if (!queryContext.subscriptionEnabled) {
            return baseMapper.findAllVisibleWithElderlyName(userId);
        }

        return baseMapper.findAllWithSubscription(
                userId,
                queryContext.notificationTypes,
                queryContext.onlyAbnormal,
                queryContext.onlyFollowedElderly,
                queryContext.followedElderlyIds
        );
    }

    public List<Notification> getAllUnreadWithSubscription(Integer userId) {
        NotificationQueryContext queryContext = buildQueryContext(userId);
        if (!queryContext.subscriptionEnabled) {
            return baseMapper.findAllVisibleUnreadWithElderlyName(userId);
        }

        return baseMapper.findAllUnreadWithSubscription(
                userId,
                queryContext.notificationTypes,
                queryContext.onlyAbnormal,
                queryContext.onlyFollowedElderly,
                queryContext.followedElderlyIds
        );
    }

    public Map<String, Object> countUnreadWithSubscription(Integer userId) {
        Map<String, Object> result = new HashMap<String, Object>();
        NotificationQueryContext queryContext = buildQueryContext(userId);

        long unreadCount;
        if (!queryContext.subscriptionEnabled) {
            unreadCount = baseMapper.countVisibleUnread(userId);
        } else {
            unreadCount = baseMapper.countAllUnreadWithSubscription(
                    userId,
                    queryContext.notificationTypes,
                    queryContext.onlyAbnormal,
                    queryContext.onlyFollowedElderly,
                    queryContext.followedElderlyIds
            );
        }

        result.put("unreadCount", unreadCount);
        result.put("subscriptionEnabled", queryContext.subscriptionEnabled);
        result.put("subscription", queryContext.subscription);
        return result;
    }

    @Transactional
    public void markAsRead(Integer id, Integer userId) {
        notificationReadRecordService.markAsRead(id, userId);
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        List<Notification> unreadNotifications = getAllUnreadWithSubscription(userId);
        for (Notification notification : unreadNotifications) {
            notificationReadRecordService.markAsRead(notification.getId(), userId);
        }
    }

    @Transactional
    public Notification createWarningNotification(HealthWarningRecord warningRecord) {
        Notification notification = new Notification();
        notification.setElderlyId(warningRecord.getElderlyId());
        notification.setWarningRecordId(warningRecord.getId());
        notification.setTitle("健康预警通知");
        notification.setContent(warningRecord.getWarningMessage());
        notification.setNotificationType(NotificationType.HEALTH_WARNING.getCode());
        notification.setCreatedAt(LocalDateTime.now());

        this.save(notification);
        return notification;
    }

    @Transactional
    public Notification createNotification(Integer userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(type);
        notification.setCreatedAt(LocalDateTime.now());

        this.save(notification);
        return notification;
    }

    @Transactional
    public int invalidateNotificationsByWarningIds(List<Integer> warningRecordIds, Integer correctionId, String reason) {
        if (warningRecordIds == null || warningRecordIds.isEmpty()) {
            return 0;
        }
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Notification::getWarningRecordId, warningRecordIds);
        List<Notification> notifications = this.list(wrapper);
        
        int count = 0;
        for (Notification notification : notifications) {
            notification.setInvalidated(true);
            notification.setInvalidatedAt(LocalDateTime.now());
            notification.setInvalidatedReason(reason);
            notification.setInvalidatedByCorrectionId(correctionId);
            if (this.updateById(notification)) {
                count++;
            }
        }
        return count;
    }

    private NotificationQueryContext buildQueryContext(Integer userId) {
        NotificationSubscription subscription = subscriptionService.getByUserId(userId);
        NotificationQueryContext context = new NotificationQueryContext();
        context.subscription = subscription;
        context.subscriptionEnabled = subscription.getEnabled() != null && subscription.getEnabled();
        context.notificationTypes = subscriptionService.getNotificationTypesList(subscription);
        context.onlyAbnormal = subscription.getOnlyAbnormal();
        context.onlyFollowedElderly = subscription.getOnlyFollowedElderly();
        if (Boolean.TRUE.equals(subscription.getOnlyFollowedElderly())) {
            List<Integer> followedIds = elderlyFollowMapper.getFollowedElderlyIds(userId);
            context.followedElderlyIds = followedIds == null ? Collections.<Integer>emptyList() : followedIds;
        } else {
            context.followedElderlyIds = null;
        }
        return context;
    }

    private static class NotificationQueryContext {
        private NotificationSubscription subscription;
        private boolean subscriptionEnabled;
        private List<String> notificationTypes;
        private Boolean onlyAbnormal;
        private Boolean onlyFollowedElderly;
        private List<Integer> followedElderlyIds;
    }

    public List<Notification> getAllWithPreference(Integer userId) {
        com.smart.elderly.entity.NotificationPreference preference = preferenceService.getByUserId(userId);
        List<String> enabledTypes = preferenceService.getEnabledTypesList(preference);
        List<String> highPriorityTypes = preferenceService.getHighPriorityTypesList(preference);
        return baseMapper.findAllWithPreference(userId, enabledTypes, highPriorityTypes);
    }

    public List<Notification> getAllUnreadWithPreference(Integer userId) {
        com.smart.elderly.entity.NotificationPreference preference = preferenceService.getByUserId(userId);
        List<String> enabledTypes = preferenceService.getEnabledTypesList(preference);
        List<String> highPriorityTypes = preferenceService.getHighPriorityTypesList(preference);
        return baseMapper.findAllUnreadWithPreference(userId, enabledTypes, highPriorityTypes);
    }

    public Map<String, Object> countUnreadWithPreference(Integer userId) {
        Map<String, Object> result = new HashMap<String, Object>();
        com.smart.elderly.entity.NotificationPreference preference = preferenceService.getByUserId(userId);
        List<String> enabledTypes = preferenceService.getEnabledTypesList(preference);
        List<String> highPriorityTypes = preferenceService.getHighPriorityTypesList(preference);

        long totalUnread = baseMapper.countAllUnreadWithPreference(userId, enabledTypes);
        long highPriorityUnread = baseMapper.countHighPriorityUnreadWithPreference(userId, enabledTypes, highPriorityTypes);
        boolean inDoNotDisturb = preferenceService.isInDoNotDisturbPeriod(preference);
        boolean shouldShowBadge = preferenceService.shouldShowBadge(preference);
        boolean shouldPlaySound = preferenceService.shouldPlaySound(preference);

        result.put("totalUnread", totalUnread);
        result.put("highPriorityUnread", highPriorityUnread);
        result.put("normalUnread", totalUnread - highPriorityUnread);
        result.put("displayUnread", shouldShowBadge ? totalUnread : 0);
        result.put("inDoNotDisturb", inDoNotDisturb);
        result.put("shouldShowBadge", shouldShowBadge);
        result.put("shouldPlaySound", shouldPlaySound);
        result.put("preference", preference);
        result.put("highPriorityTypes", highPriorityTypes);
        result.put("enabledTypes", enabledTypes);
        return result;
    }

    @Transactional
    public void markAllAsReadWithPreference(Integer userId) {
        List<Notification> unreadNotifications = getAllUnreadWithPreference(userId);
        for (Notification notification : unreadNotifications) {
            notificationReadRecordService.markAsRead(notification.getId(), userId);
        }
    }
}
