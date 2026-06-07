package com.smart.elderly.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class QualityReviewSubmitDTO {
    @NotNull(message = "复核记录ID不能为空")
    private Integer reviewId;

    @NotBlank(message = "复核状态不能为空")
    private String reviewStatus;

    @NotBlank(message = "复核结论不能为空")
    private String reviewConclusion;

    private String ignoreReason;
}
