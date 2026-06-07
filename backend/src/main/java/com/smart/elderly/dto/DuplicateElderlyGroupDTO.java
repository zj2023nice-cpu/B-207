package com.smart.elderly.dto;

import com.smart.elderly.entity.Elderly;
import lombok.Data;
import java.util.List;

@Data
public class DuplicateElderlyGroupDTO {
    private String groupId;
    private List<Elderly> elderlyList;
    private Integer confidence;
    private String matchReason;
    private Integer relatedDataCount;
}
