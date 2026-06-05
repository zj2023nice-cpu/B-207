package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_warning_thresholds")
public class HealthWarningThreshold {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer elderlyId;
    
    @NotBlank(message = "指标类型不能为空")
    private String indicatorType;
    
    private BigDecimal highThreshold;
    
    private BigDecimal lowThreshold;
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
