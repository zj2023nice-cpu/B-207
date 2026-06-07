package com.smart.elderly.vo;

import java.time.LocalDateTime;

public class FollowedElderlyVO {
    private Integer elderlyId;
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String address;
    private String status;
    
    private LocalDateTime lastCheckTime;
    private String lastHealthSummary;
    private Boolean lastIsAbnormal;
    
    private Integer pendingWarningCount;
    private Integer unreadNotificationCount;
    
    private LocalDateTime followTime;

    public Integer getElderlyId() {
        return elderlyId;
    }

    public void setElderlyId(Integer elderlyId) {
        this.elderlyId = elderlyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(LocalDateTime lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public String getLastHealthSummary() {
        return lastHealthSummary;
    }

    public void setLastHealthSummary(String lastHealthSummary) {
        this.lastHealthSummary = lastHealthSummary;
    }

    public Boolean getLastIsAbnormal() {
        return lastIsAbnormal;
    }

    public void setLastIsAbnormal(Boolean lastIsAbnormal) {
        this.lastIsAbnormal = lastIsAbnormal;
    }

    public Integer getPendingWarningCount() {
        return pendingWarningCount;
    }

    public void setPendingWarningCount(Integer pendingWarningCount) {
        this.pendingWarningCount = pendingWarningCount;
    }

    public Integer getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(Integer unreadNotificationCount) {
        this.unreadNotificationCount = unreadNotificationCount;
    }

    public LocalDateTime getFollowTime() {
        return followTime;
    }

    public void setFollowTime(LocalDateTime followTime) {
        this.followTime = followTime;
    }
}
