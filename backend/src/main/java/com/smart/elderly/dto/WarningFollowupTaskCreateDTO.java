package com.smart.elderly.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarningFollowupTaskCreateDTO {
    private Integer warningRecordId;
    private String title;
    private String description;
    private Integer assigneeId;
    private String assigneeName;
    private LocalDateTime deadline;
    private String priority;
}
