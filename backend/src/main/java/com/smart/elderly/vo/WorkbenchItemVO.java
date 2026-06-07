package com.smart.elderly.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkbenchItemVO {
    private String itemId;
    private String itemType;
    private String itemTypeName;
    private Integer sourceId;
    private Integer elderlyId;
    private String elderlyName;
    private String title;
    private String description;
    private String priority;
    private String priorityName;
    private Integer priorityScore;
    private String status;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private List<QuickAction> quickActions;
    private String detailUrl;
    private String moduleUrl;

    @Data
    public static class QuickAction {
        private String actionKey;
        private String actionName;
        private String actionType;
        private String apiUrl;
        private String method;
        private Boolean requireConfirm;
        private String confirmText;
    }
}
