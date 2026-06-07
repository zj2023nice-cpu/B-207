package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.NotificationSubscription;
import com.smart.elderly.mapper.NotificationSubscriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class NotificationSubscriptionService extends ServiceImpl<NotificationSubscriptionMapper, NotificationSubscription> {

    public NotificationSubscription getByUserId(Integer userId) {
        NotificationSubscription subscription = baseMapper.findByUserId(userId);
        if (subscription == null) {
            subscription = createDefaultSubscription(userId);
        }
        return subscription;
    }

    @Transactional
    public NotificationSubscription saveOrUpdate(Integer userId, NotificationSubscription subscription) {
        NotificationSubscription existing = baseMapper.findByUserId(userId);
        if (existing == null) {
            subscription.setUserId(userId);
            subscription.setCreatedAt(LocalDateTime.now());
            subscription.setUpdatedAt(LocalDateTime.now());
            if (subscription.getEnabled() == null) {
                subscription.setEnabled(true);
            }
            if (subscription.getOnlyAbnormal() == null) {
                subscription.setOnlyAbnormal(false);
            }
            if (subscription.getOnlyFollowedElderly() == null) {
                subscription.setOnlyFollowedElderly(false);
            }
            this.save(subscription);
            return subscription;
        } else {
            existing.setEnabled(subscription.getEnabled());
            existing.setNotificationTypes(subscription.getNotificationTypes());
            existing.setOnlyAbnormal(subscription.getOnlyAbnormal());
            existing.setOnlyFollowedElderly(subscription.getOnlyFollowedElderly());
            existing.setUpdatedAt(LocalDateTime.now());
            this.updateById(existing);
            return existing;
        }
    }

    private NotificationSubscription createDefaultSubscription(Integer userId) {
        NotificationSubscription subscription = new NotificationSubscription();
        subscription.setUserId(userId);
        subscription.setEnabled(true);
        subscription.setNotificationTypes("");
        subscription.setOnlyAbnormal(false);
        subscription.setOnlyFollowedElderly(false);
        return subscription;
    }

    public List<String> getNotificationTypesList(NotificationSubscription subscription) {
        if (subscription.getNotificationTypes() == null || subscription.getNotificationTypes().isEmpty()) {
            return null;
        }
        return Arrays.asList(subscription.getNotificationTypes().split(","));
    }
}
