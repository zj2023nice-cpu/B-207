package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

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
