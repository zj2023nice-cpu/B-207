package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.SystemAnnouncement;
import com.smart.elderly.mapper.SystemAnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemAnnouncementService extends ServiceImpl<SystemAnnouncementMapper, SystemAnnouncement> {

    @Autowired
    private AnnouncementReadRecordService readRecordService;

    public List<SystemAnnouncement> getActiveAnnouncementsWithReadStatus(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        List<SystemAnnouncement> announcements = baseMapper.findActiveAnnouncementsWithReadStatus(userId, now);
        return announcements;
    }

    public List<SystemAnnouncement> getActiveAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        return baseMapper.findAllActiveAnnouncements(now);
    }

    public Map<String, Object> countUnread(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        long count = baseMapper.countUnreadAnnouncements(userId, now);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    public Page<SystemAnnouncement> getAdminAnnouncements(Integer pageNum, Integer pageSize, String status) {
        Page<SystemAnnouncement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemAnnouncement> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SystemAnnouncement::getStatus, status);
        }
        wrapper.orderByDesc(SystemAnnouncement::getIsPinned)
               .orderByDesc(SystemAnnouncement::getPublishStartTime)
               .orderByDesc(SystemAnnouncement::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Transactional
    public SystemAnnouncement createAnnouncement(SystemAnnouncement announcement) {
        Integer userId = UserContextHolder.getUserId();
        String username = UserContextHolder.getUsername();
        announcement.setPublisherId(userId);
        announcement.setPublisherName(username);
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        if (announcement.getStatus() == null) {
            announcement.setStatus("PUBLISHED");
        }
        if (announcement.getIsPinned() == null) {
            announcement.setIsPinned(false);
        }
        if (announcement.getPublishStartTime() == null) {
            announcement.setPublishStartTime(LocalDateTime.now());
        }
        this.save(announcement);
        return announcement;
    }

    @Transactional
    public SystemAnnouncement updateAnnouncement(SystemAnnouncement announcement) {
        announcement.setUpdatedAt(LocalDateTime.now());
        this.updateById(announcement);
        readRecordService.deleteByAnnouncementId(announcement.getId());
        return announcement;
    }

    @Transactional
    public void deleteAnnouncement(Integer id) {
        this.removeById(id);
        readRecordService.deleteByAnnouncementId(id);
    }

    public SystemAnnouncement getDetailById(Integer id) {
        return this.getById(id);
    }

    public SystemAnnouncement getDetailWithReadStatus(Integer id, Integer userId) {
        SystemAnnouncement announcement = this.getById(id);
        if (announcement != null) {
            boolean isRead = readRecordService.hasRead(id, userId);
            announcement.setIsRead(isRead);
        }
        return announcement;
    }

    @Transactional
    public void markAsRead(Integer announcementId, Integer userId) {
        readRecordService.markAsRead(announcementId, userId);
    }

    @Transactional
    public boolean markAsReadAndCheck(Integer announcementId, Integer userId) {
        boolean hasRead = readRecordService.hasRead(announcementId, userId);
        if (!hasRead) {
            readRecordService.markAsRead(announcementId, userId);
        }
        return true;
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        List<SystemAnnouncement> active = getActiveAnnouncements();
        for (SystemAnnouncement announcement : active) {
            readRecordService.markAsRead(announcement.getId(), userId);
        }
    }
}
