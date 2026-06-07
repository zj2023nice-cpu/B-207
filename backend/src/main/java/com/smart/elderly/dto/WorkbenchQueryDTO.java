package com.smart.elderly.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkbenchQueryDTO {
    private List<Integer> elderlyIds;
    private List<String> priorities;
    private List<String> itemTypes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
