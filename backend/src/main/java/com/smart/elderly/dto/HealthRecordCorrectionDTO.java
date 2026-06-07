package com.smart.elderly.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class HealthRecordCorrectionDTO {
    @NotNull(message = "健康记录ID不能为空")
    private Integer healthRecordId;
    
    private String bloodPressure;
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private BigDecimal temperature;
    private Integer heartRate;
    private Integer bloodOxygen;
    private BigDecimal bloodSugar;
    
    @NotBlank(message = "更正原因不能为空")
    private String correctionReason;
    
    private String correctionRemark;
}
