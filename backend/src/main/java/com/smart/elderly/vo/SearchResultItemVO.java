package com.smart.elderly.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SearchResultItemVO {
    private Integer id;
    private String module;
    private String moduleName;
    private String title;
    private String description;
    private String routePath;
    private String targetType;
    private Integer targetId;
    private Integer targetElderlyId;
    private LocalDateTime time;
    private String extraInfo;
}
