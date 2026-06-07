package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("quality_review_rules")
public class QualityReviewRule {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String ruleCode;
    private String ruleName;
    private String ruleDescription;
    private String ruleType;
    private String severity;
    private Boolean enabled;
    private String configJson;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
