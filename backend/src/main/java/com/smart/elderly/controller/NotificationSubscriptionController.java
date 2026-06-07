package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.entity.NotificationSubscription;
import com.smart.elderly.service.NotificationSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification-subscription")
public class NotificationSubscriptionController {

    @Autowired
    private NotificationSubscriptionService notificationSubscriptionService;

    @GetMapping("/{userId}")
    public Result<NotificationSubscription> getByUserId(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationSubscriptionService.getByUserId(userId));
    }

    @PostMapping("/{userId}")
    public Result<NotificationSubscription> saveOrUpdate(@PathVariable Integer userId, @RequestBody NotificationSubscription subscription) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(notificationSubscriptionService.saveOrUpdate(userId, subscription));
    }
}
