package com.smart.elderly.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class ElderlyMergeDTO {
    @NotNull(message = "主档案ID不能为空")
    private Integer primaryElderlyId;
    
    @NotNull(message = "被合并档案ID不能为空")
    private Integer mergedElderlyId;
    
    private Map<String, Object> fieldOverrides;
}
