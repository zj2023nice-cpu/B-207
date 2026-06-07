package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<List<Notification>> list() {
        return Result.success(notificationService.getAllWithElderlyName());
    }

    @GetMapping("/unread")
    public Result<List<Notification>> getUnread() {
        return Result.success(notificationService.getAllUnreadWithElderlyName());
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> countUnread() {
        return Result.success(notificationService.countUnread());
    }

    @GetMapping("/list/with-subscription/{userId}")
    public Result<List<Notification>> listWithSubscription(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationService.getAllWithSubscription(userId));
    }

    @GetMapping("/unread/with-subscription/{userId}")
    public Result<List<Notification>> getUnreadWithSubscription(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationService.getAllUnreadWithSubscription(userId));
    }

    @GetMapping("/count/with-subscription/{userId}")
    public Result<Map<String, Object>> countUnreadWithSubscription(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationService.countUnreadWithSubscription(userId));
    }

    @GetMapping("/elderly/{elderlyId}")
    public Result<List<Notification>> getByElderlyId(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        return Result.success(notificationService.getByElderlyIdWithName(elderlyId));
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        notificationService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead() {
        notificationService.markAllSystemAsRead();
        return Result.success("已全部标记为已读");
    }

    @GetMapping("/list/{userId}")
    public Result<List<Notification>> listByUserId(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationService.getByUserId(userId));
    }

    @GetMapping("/unread/{userId}")
    public Result<List<Notification>> getUnreadByUserId(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationService.getUnreadByUserId(userId));
    }

    @GetMapping("/count/{userId}")
    public Result<Map<String, Object>> countUnreadByUserId(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", notificationService.countUnreadByUserId(userId));
        return Result.success(result);
    }

    @PutMapping("/read-all/{userId}")
    public Result<String> markAllAsReadByUserId(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记为已读");
    }
}
