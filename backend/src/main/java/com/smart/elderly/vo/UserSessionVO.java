package com.smart.elderly.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSessionVO {
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
    private Boolean isCurrent;
}
