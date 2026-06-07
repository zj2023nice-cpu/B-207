package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.entity.NotificationSubscription;
import com.smart.elderly.mapper.ElderlyFollowMapper;
import com.smart.elderly.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

    @Autowired
    private NotificationSubscriptionService subscriptionService;

    @Autowired
    private ElderlyFollowMapper elderlyFollowMapper;

    public List<Notification> getAllWithElderlyName() {
        return baseMapper.findAllWithElderlyName();
    }

    public List<Notification> getAllUnreadWithElderlyName() {
        return baseMapper.findAllUnreadWithElderlyName();
    }

    public List<Notification> getByElderlyIdWithName(Integer elderlyId) {
        return baseMapper.findByElderlyIdWithName(elderlyId);
    }

    public Map<String, Object> countUnread() {
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", baseMapper.countAllUnread());
        return result;
    }

    public List<Notification> getByUserId(Integer userId) {
        return baseMapper.findByUserId(userId);
    }

    public List<Notification> getUnreadByUserId(Integer userId) {
        return baseMapper.findUnreadByUserId(userId);
    }

    public long countUnreadByUserId(Integer userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    public List<Notification> getAllWithSubscription(Integer userId) {
        NotificationSubscription subscription = subscriptionService.getByUserId(userId);
        if (subscription.getEnabled() == null || !subscription.getEnabled()) {
            return baseMapper.findAllWithElderlyName();
        }
        
        List<String> notificationTypes = null;
        if (subscription.getNotificationTypes() != null && !subscription.getNotificationTypes().isEmpty()) {
            notificationTypes = List.of(subscription.getNotificationTypes().split(","));
        }
        
        List<Integer> followedElderlyIds = null;
        if (subscription.getOnlyFollowedElderly() != null && subscription.getOnlyFollowedElderly()) {
            followedElderlyIds = elderlyFollowMapper.getFollowedElderlyIds(userId);
        }
        
        return baseMapper.findAllWithSubscription(
            notificationTypes,
            subscription.getOnlyAbnormal(),
            subscription.getOnlyFollowedElderly(),
            followedElderlyIds
        );
    }

    public List<Notification> getAllUnreadWithSubscription(Integer userId) {
        NotificationSubscription subscription = subscriptionService.getByUserId(userId);
        if (subscription.getEnabled() == null || !subscription.getEnabled()) {
            return baseMapper.findAllUnreadWithElderlyName();
        }
        
        List<String> notificationTypes = null;
        if (subscription.getNotificationTypes() != null && !subscription.getNotificationTypes().isEmpty()) {
            notificationTypes = List.of(subscription.getNotificationTypes().split(","));
        }
        
        List<Integer> followedElderlyIds = null;
        if (subscription.getOnlyFollowedElderly() != null && subscription.getOnlyFollowedElderly()) {
            followedElderlyIds = elderlyFollowMapper.getFollowedElderlyIds(userId);
        }
        
        return baseMapper.findAllUnreadWithSubscription(
            notificationTypes,
            subscription.getOnlyAbnormal(),
            subscription.getOnlyFollowedElderly(),
            followedElderlyIds
        );
    }

    public Map<String, Object> countUnreadWithSubscription(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        NotificationSubscription subscription = subscriptionService.getByUserId(userId);
        
        long unreadCount;
        if (subscription.getEnabled() == null || !subscription.getEnabled()) {
            unreadCount = baseMapper.countAllUnread();
        } else {
            List<String> notificationTypes = null;
            if (subscription.getNotificationTypes() != null && !subscription.getNotificationTypes().isEmpty()) {
                notificationTypes = List.of(subscription.getNotificationTypes().split(","));
            }
            
            List<Integer> followedElderlyIds = null;
            if (subscription.getOnlyFollowedElderly() != null && subscription.getOnlyFollowedElderly()) {
                followedElderlyIds = elderlyFollowMapper.getFollowedElderlyIds(userId);
            }
            
            unreadCount = baseMapper.countAllUnreadWithSubscription(
                notificationTypes,
                subscription.getOnlyAbnormal(),
                subscription.getOnlyFollowedElderly(),
                followedElderlyIds
            );
        }
        
        result.put("unreadCount", unreadCount);
        result.put("subscriptionEnabled", subscription.getEnabled() != null && subscription.getEnabled());
        result.put("subscription", subscription);
        return result;
    }

    @Transactional
    public void markAsRead(Integer id) {
        baseMapper.markAsRead(id);
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        baseMapper.markAllAsRead(userId);
    }

    @Transactional
    public void markAllSystemAsRead() {
        baseMapper.markAllSystemAsRead();
    }

    @Transactional
    public Notification createWarningNotification(HealthWarningRecord warningRecord) {
        Notification notification = new Notification();
        notification.setElderlyId(warningRecord.getElderlyId());
        notification.setWarningRecordId(warningRecord.getId());
        notification.setTitle("健康预警通知");
        notification.setContent(warningRecord.getWarningMessage());
        notification.setNotificationType("HEALTH_WARNING");
        notification.setStatus("UNREAD");
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
        notification.setStatus("UNREAD");
        notification.setCreatedAt(LocalDateTime.now());
        
        this.save(notification);
        return notification;
    }
}
