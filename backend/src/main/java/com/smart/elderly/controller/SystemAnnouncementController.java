package com.smart.elderly.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.SystemAnnouncement;
import com.smart.elderly.service.SystemAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-announcement")
public class SystemAnnouncementController {

    @Autowired
    private SystemAnnouncementService systemAnnouncementService;

    @GetMapping("/list")
    public Result<List<SystemAnnouncement>> getList() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.success(systemAnnouncementService.getActiveAnnouncements());
        }
        return Result.success(systemAnnouncementService.getActiveAnnouncementsWithReadStatus(userId));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            userId = 1;
        }
        return Result.success(systemAnnouncementService.countUnread(userId));
    }

    @GetMapping("/detail/{id}")
    public Result<SystemAnnouncement> getDetail(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("公告ID不能为空");
        }
        Integer userId = UserContextHolder.getUserId();
        SystemAnnouncement announcement;
        if (userId != null) {
            announcement = systemAnnouncementService.getDetailWithReadStatus(id, userId);
        } else {
            announcement = systemAnnouncementService.getDetailById(id);
        }
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("公告ID不能为空");
        }
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            userId = 1;
        }
        systemAnnouncementService.markAsRead(id, userId);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            userId = 1;
        }
        systemAnnouncementService.markAllAsRead(userId);
        return Result.success("已全部标记为已读");
    }

    @GetMapping("/admin/page")
    public Result<Page<SystemAnnouncement>> getAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(systemAnnouncementService.getAdminAnnouncements(pageNum, pageSize, status));
    }

    @GetMapping("/admin/{id}")
    public Result<SystemAnnouncement> getAdminDetail(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("公告ID不能为空");
        }
        SystemAnnouncement announcement = systemAnnouncementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    @PostMapping("/admin")
    public Result<SystemAnnouncement> create(@RequestBody SystemAnnouncement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            return Result.error("公告标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
            return Result.error("公告正文不能为空");
        }
        return Result.success(systemAnnouncementService.createAnnouncement(announcement));
    }

    @PutMapping("/admin")
    public Result<SystemAnnouncement> update(@RequestBody SystemAnnouncement announcement) {
        if (announcement.getId() == null) {
            return Result.error("公告ID不能为空");
        }
        return Result.success(systemAnnouncementService.updateAnnouncement(announcement));
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("公告ID不能为空");
        }
        systemAnnouncementService.deleteAnnouncement(id);
        return Result.success("删除成功");
    }
}
