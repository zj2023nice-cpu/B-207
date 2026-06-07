package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.dto.ChangePasswordDTO;
import com.smart.elderly.dto.UpdateProfileDTO;
import com.smart.elderly.entity.User;
import com.smart.elderly.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    public User login(String username, String password) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password));
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            updateById(user);
        }
        return user;
    }

    public boolean register(User user) {
        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            user.setDisplayName(user.getUsername());
        }
        return save(user);
    }

    public Map<String, Object> getProfile(Integer userId) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("displayName", user.getDisplayName());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole());
        profile.put("lastLoginTime", user.getLastLoginTime());
        profile.put("createdAt", user.getCreatedAt());
        return profile;
    }

    public boolean updateProfile(Integer userId, UpdateProfileDTO dto) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        return updateById(user);
    }

    public String changePassword(Integer userId, ChangePasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return "两次输入的新密码不一致";
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            return "新密码不能与旧密码相同";
        }
        User user = getById(userId);
        if (user == null) {
            return "用户不存在";
        }
        if (!user.getPassword().equals(dto.getOldPassword())) {
            return "旧密码不正确";
        }
        user.setPassword(dto.getNewPassword());
        updateById(user);
        return null;
    }
}
