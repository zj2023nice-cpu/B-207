package com.smart.elderly.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class MergePreviewRequestDTO {
    @NotNull(message = "主档案ID不能为空")
    private Integer primaryElderlyId;

    @NotEmpty(message = "被合并档案ID不能为空")
    private List<Integer> mergedElderlyIds;
}
