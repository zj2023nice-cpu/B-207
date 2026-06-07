package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.common.SecurityConstants;
import com.smart.elderly.entity.User;
import com.smart.elderly.entity.UserSession;
import com.smart.elderly.enums.UserSessionStatus;
import com.smart.elderly.mapper.UserSessionMapper;
import com.smart.elderly.vo.UserSessionVO;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSessionService extends ServiceImpl<UserSessionMapper, UserSession> {

    public static final String SESSION_USER_KEY = SecurityConstants.SESSION_USER_KEY;

    public String recordLoginSession(User user, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        String loginIp = getClientIp(request);
        String deviceType = userAgent.getOperatingSystem().getDeviceType().toString();
        String browser = userAgent.getBrowser().getName();
        String os = userAgent.getOperatingSystem().getName();
        String userAgentStr = request.getHeader("User-Agent");

        UserSession userSession = new UserSession();
        userSession.setUserId(user.getId());
        userSession.setSessionId(sessionId);
        userSession.setLoginIp(loginIp);
        userSession.setUserAgent(userAgentStr);
        userSession.setDeviceType(deviceType);
        userSession.setBrowser(browser);
        userSession.setOs(os);
        userSession.setStatus(UserSessionStatus.ACTIVE.getCode());
        userSession.setLoginTime(LocalDateTime.now());
        userSession.setLastActiveTime(LocalDateTime.now());
        userSession.setExpireTime(LocalDateTime.now().plusHours(SecurityConstants.DEFAULT_SESSION_EXPIRE_HOURS));

        this.save(userSession);

        session.setAttribute("USER_SESSION_ID", userSession.getId());

        return sessionId;
    }

    public List<UserSessionVO> getUserSessions(Integer userId, String currentSessionId) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId)
                .orderByDesc(UserSession::getLoginTime);

        List<UserSession> sessions = this.list(wrapper);

        return sessions.stream().map(session -> {
            UserSessionVO vo = new UserSessionVO();
            vo.setId(session.getId());
            vo.setUserId(session.getUserId());
            vo.setSessionId(session.getSessionId());
            vo.setLoginIp(session.getLoginIp());
            vo.setLoginLocation(session.getLoginLocation());
            vo.setUserAgent(session.getUserAgent());
            vo.setDeviceType(session.getDeviceType());
            vo.setBrowser(session.getBrowser());
            vo.setOs(session.getOs());
            vo.setStatus(session.getStatus());
            vo.setLoginTime(session.getLoginTime());
            vo.setLastActiveTime(session.getLastActiveTime());
            vo.setExpireTime(session.getExpireTime());
            vo.setIsCurrent(session.getSessionId().equals(currentSessionId));
            return vo;
        }).collect(Collectors.toList());
    }

    public boolean invalidateSession(Long sessionId, Integer currentUserId, Integer operatorId) {
        UserSession session = this.getById(sessionId);
        if (session == null) {
            return false;
        }
        if (!session.getUserId().equals(currentUserId)) {
            return false;
        }
        UserSessionStatus currentStatus = UserSessionStatus.fromCode(session.getStatus());
        if (currentStatus.isActive()) {
            session.setStatus(UserSessionStatus.INVALIDATED.getCode());
            session.setInvalidatedAt(LocalDateTime.now());
            session.setInvalidatedBy(operatorId);
            this.updateById(session);
        }
        return true;
    }

    public void updateLastActiveTime(String sessionId) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getSessionId, sessionId);
        UserSession session = this.getOne(wrapper);
        UserSessionStatus status = UserSessionStatus.fromCode(session != null ? session.getStatus() : null);
        if (session != null && status.isActive()) {
            session.setLastActiveTime(LocalDateTime.now());
            this.updateById(session);
        }
    }

    public boolean isSessionValid(String sessionId) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getSessionId, sessionId);
        UserSession session = this.getOne(wrapper);
        if (session == null) {
            return true;
        }
        UserSessionStatus status = UserSessionStatus.fromCode(session.getStatus());
        return status.isActive() &&
                (session.getExpireTime() == null || session.getExpireTime().isAfter(LocalDateTime.now()));
    }

    public void markSessionLogout(String sessionId) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getSessionId, sessionId);
        UserSession session = this.getOne(wrapper);
        UserSessionStatus status = UserSessionStatus.fromCode(session != null ? session.getStatus() : null);
        if (session != null && status.isActive()) {
            session.setStatus(UserSessionStatus.LOGOUT.getCode());
            session.setInvalidatedAt(LocalDateTime.now());
            this.updateById(session);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
