package com.smart.elderly.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class QualityReviewSubmitDTO {
    @NotNull(message = "复核记录ID不能为空")
    private Integer reviewId;
    
    @NotNull(message = "复核结论不能为空")
    private String reviewStatus;
    
    private String reviewConclusion;
    private String ignoreReason;
}
