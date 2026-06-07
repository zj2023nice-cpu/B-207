package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_sessions")
public class UserSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private String sessionId;
    private String loginIp;
    private String loginLocation;
    private String userAgent;
    private String deviceType;
    private String browser;
    private String os;
    private String status;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private LocalDateTime expireTime;
    private LocalDateTime invalidatedAt;
    private Integer invalidatedBy;
}
