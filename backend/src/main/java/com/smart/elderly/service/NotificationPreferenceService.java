package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.NotificationPreference;
import com.smart.elderly.enums.NotificationType;
import com.smart.elderly.mapper.NotificationPreferenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationPreferenceService extends ServiceImpl<NotificationPreferenceMapper, NotificationPreference> {

    private static final List<String> DEFAULT_HIGH_PRIORITY_TYPES = NotificationType.getDefaultHighPriorityTypes();
    private static final String DEFAULT_DND_START = "22:00";
    private static final String DEFAULT_DND_END = "08:00";

    public NotificationPreference getByUserId(Integer userId) {
        LambdaQueryWrapper<NotificationPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPreference::getUserId, userId);
        NotificationPreference preference = this.getOne(wrapper);
        
        if (preference == null) {
            preference = createDefaultPreference(userId);
        }
        return preference;
    }

    @Transactional
    public NotificationPreference savePreference(Integer userId, NotificationPreference preference) {
        LambdaQueryWrapper<NotificationPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPreference::getUserId, userId);
        NotificationPreference existing = this.getOne(wrapper);
        
        if (existing == null) {
            preference.setUserId(userId);
            preference.setCreatedAt(java.time.LocalDateTime.now());
            preference.setUpdatedAt(java.time.LocalDateTime.now());
            this.save(preference);
            return preference;
        } else {
            existing.setEnabledTypes(preference.getEnabledTypes());
            existing.setHighPriorityTypes(preference.getHighPriorityTypes());
            existing.setDoNotDisturbEnabled(preference.getDoNotDisturbEnabled());
            existing.setDoNotDisturbStart(preference.getDoNotDisturbStart());
            existing.setDoNotDisturbEnd(preference.getDoNotDisturbEnd());
            existing.setShowBadgeInDnd(preference.getShowBadgeInDnd());
            existing.setSoundEnabled(preference.getSoundEnabled());
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            this.updateById(existing);
            return existing;
        }
    }

    public List<String> getEnabledTypesList(NotificationPreference preference) {
        if (preference == null || preference.getEnabledTypes() == null || preference.getEnabledTypes().trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(preference.getEnabledTypes().split(","));
    }

    public List<String> getHighPriorityTypesList(NotificationPreference preference) {
        if (preference == null || preference.getHighPriorityTypes() == null || preference.getHighPriorityTypes().trim().isEmpty()) {
            return DEFAULT_HIGH_PRIORITY_TYPES;
        }
        return Arrays.asList(preference.getHighPriorityTypes().split(","));
    }

    public boolean isTypeEnabled(String notificationType, NotificationPreference preference) {
        if (preference == null) {
            return true;
        }
        List<String> enabledTypes = getEnabledTypesList(preference);
        if (enabledTypes.isEmpty()) {
            return true;
        }
        return enabledTypes.contains(notificationType);
    }

    public boolean isHighPriority(String notificationType, NotificationPreference preference) {
        List<String> highPriorityTypes = getHighPriorityTypesList(preference);
        return highPriorityTypes.contains(notificationType);
    }

    public boolean isInDoNotDisturbPeriod(NotificationPreference preference) {
        if (preference == null || !Boolean.TRUE.equals(preference.getDoNotDisturbEnabled())) {
            return false;
        }
        
        String startTimeStr = preference.getDoNotDisturbStart() != null ? preference.getDoNotDisturbStart() : DEFAULT_DND_START;
        String endTimeStr = preference.getDoNotDisturbEnd() != null ? preference.getDoNotDisturbEnd() : DEFAULT_DND_END;
        
        LocalTime now = LocalTime.now();
        LocalTime startTime = parseTime(startTimeStr);
        LocalTime endTime = parseTime(endTimeStr);
        
        if (startTime.isBefore(endTime)) {
            return !now.isBefore(startTime) && !now.isAfter(endTime);
        } else {
            return !now.isBefore(startTime) || !now.isAfter(endTime);
        }
    }

    public boolean shouldShowBadge(NotificationPreference preference) {
        if (!isInDoNotDisturbPeriod(preference)) {
            return true;
        }
        return Boolean.TRUE.equals(preference.getShowBadgeInDnd());
    }

    public boolean shouldPlaySound(NotificationPreference preference) {
        if (isInDoNotDisturbPeriod(preference)) {
            return false;
        }
        return preference == null || Boolean.TRUE.equals(preference.getSoundEnabled());
    }

    private NotificationPreference createDefaultPreference(Integer userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEnabledTypes("");
        preference.setHighPriorityTypes(String.join(",", DEFAULT_HIGH_PRIORITY_TYPES));
        preference.setDoNotDisturbEnabled(false);
        preference.setDoNotDisturbStart(DEFAULT_DND_START);
        preference.setDoNotDisturbEnd(DEFAULT_DND_END);
        preference.setShowBadgeInDnd(false);
        preference.setSoundEnabled(true);
        preference.setCreatedAt(java.time.LocalDateTime.now());
        preference.setUpdatedAt(java.time.LocalDateTime.now());
        this.save(preference);
        return preference;
    }

    private LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return LocalTime.MIN;
        }
    }
}
