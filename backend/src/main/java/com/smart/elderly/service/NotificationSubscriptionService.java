package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.NotificationSubscription;
import com.smart.elderly.mapper.NotificationSubscriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        String normalizedTypes = normalizeNotificationTypes(subscription == null ? null : subscription.getNotificationTypes());
        Boolean enabled = subscription != null ? subscription.getEnabled() : null;
        Boolean onlyAbnormal = subscription != null ? subscription.getOnlyAbnormal() : null;
        Boolean onlyFollowedElderly = subscription != null ? subscription.getOnlyFollowedElderly() : null;

        if (existing == null) {
            NotificationSubscription entity = new NotificationSubscription();
            entity.setUserId(userId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setEnabled(enabled != null ? enabled : true);
            entity.setNotificationTypes(normalizedTypes);
            entity.setOnlyAbnormal(onlyAbnormal != null ? onlyAbnormal : false);
            entity.setOnlyFollowedElderly(onlyFollowedElderly != null ? onlyFollowedElderly : false);
            this.save(entity);
            return entity;
        }

        existing.setUserId(userId);
        existing.setEnabled(enabled != null ? enabled : true);
        existing.setNotificationTypes(normalizedTypes);
        existing.setOnlyAbnormal(onlyAbnormal != null ? onlyAbnormal : false);
        existing.setOnlyFollowedElderly(onlyFollowedElderly != null ? onlyFollowedElderly : false);
        existing.setUpdatedAt(LocalDateTime.now());
        this.updateById(existing);
        return existing;
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
        if (subscription == null) {
            return null;
        }
        String normalizedTypes = normalizeNotificationTypes(subscription.getNotificationTypes());
        if (normalizedTypes == null || normalizedTypes.isEmpty()) {
            return null;
        }
        return Arrays.asList(normalizedTypes.split(","));
    }

    private String normalizeNotificationTypes(String notificationTypes) {
        if (notificationTypes == null || notificationTypes.trim().isEmpty()) {
            return "";
        }

        String[] rawTypes = notificationTypes.split(",");
        Set<String> normalized = new LinkedHashSet<String>();
        for (String rawType : rawTypes) {
            if (rawType == null) {
                continue;
            }
            String type = rawType.trim();
            if (!type.isEmpty()) {
                normalized.add(type);
            }
        }

        if (normalized.isEmpty()) {
            return "";
        }
        return String.join(",", new ArrayList<String>(normalized));
    }
}
