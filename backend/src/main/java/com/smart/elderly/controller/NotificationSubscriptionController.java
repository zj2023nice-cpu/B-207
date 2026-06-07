package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.NotificationSubscription;
import com.smart.elderly.service.NotificationSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification-subscription")
public class NotificationSubscriptionController {

    @Autowired
    private NotificationSubscriptionService notificationSubscriptionService;

    @GetMapping
    public Result<NotificationSubscription> getCurrentUserSubscription() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationSubscriptionService.getByUserId(userId));
    }

    @PostMapping
    public Result<NotificationSubscription> saveOrUpdate(@RequestBody NotificationSubscription subscription) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(notificationSubscriptionService.saveOrUpdate(userId, subscription));
    }
}
