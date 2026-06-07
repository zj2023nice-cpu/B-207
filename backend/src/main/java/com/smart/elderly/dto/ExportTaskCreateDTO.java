package com.smart.elderly.dto;

import lombok.Data;

@Data
public class ExportTaskCreateDTO {
    private String exportType;
    private String exportParams;
    private String exportRangeDesc;
    private String taskName;
}
