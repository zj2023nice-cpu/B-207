package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<List<Notification>> list() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.getAllWithElderlyName(userId));
    }

    @GetMapping("/unread")
    public Result<List<Notification>> getUnread() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.getAllUnreadWithElderlyName(userId));
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> countUnread() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.countUnread(userId));
    }

    @GetMapping("/list/with-subscription")
    public Result<List<Notification>> listWithSubscription() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.getAllWithSubscription(userId));
    }

    @GetMapping("/unread/with-subscription")
    public Result<List<Notification>> getUnreadWithSubscription() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.getAllUnreadWithSubscription(userId));
    }

    @GetMapping("/count/with-subscription")
    public Result<Map<String, Object>> countUnreadWithSubscription() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.countUnreadWithSubscription(userId));
    }

    @GetMapping("/elderly/{elderlyId}")
    public Result<List<Notification>> getByElderlyId(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationService.getByElderlyIdWithName(elderlyId, userId));
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        notificationService.markAsRead(id, userId);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记为已读");
    }
}
