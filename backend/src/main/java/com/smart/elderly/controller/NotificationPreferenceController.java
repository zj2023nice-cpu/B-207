package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.NotificationPreference;
import com.smart.elderly.service.NotificationPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-preference")
public class NotificationPreferenceController {

    @Autowired
    private NotificationPreferenceService preferenceService;

    @GetMapping
    public Result<NotificationPreference> getPreference() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(preferenceService.getByUserId(userId));
    }

    @PutMapping
    public Result<NotificationPreference> updatePreference(@RequestBody NotificationPreference preference) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(preferenceService.savePreference(userId, preference));
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> getPreferenceStatus() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        NotificationPreference preference = preferenceService.getByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("preference", preference);
        result.put("inDoNotDisturb", preferenceService.isInDoNotDisturbPeriod(preference));
        result.put("shouldShowBadge", preferenceService.shouldShowBadge(preference));
        result.put("shouldPlaySound", preferenceService.shouldPlaySound(preference));
        result.put("highPriorityTypes", preferenceService.getHighPriorityTypesList(preference));
        result.put("enabledTypes", preferenceService.getEnabledTypesList(preference));
        
        return Result.success(result);
    }
}
