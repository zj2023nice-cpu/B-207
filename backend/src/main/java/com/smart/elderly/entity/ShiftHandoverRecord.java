package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("shift_handover_records")
public class ShiftHandoverRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "交班人不能为空")
    private String handoverPerson;

    @NotBlank(message = "接班人不能为空")
    private String takeoverPerson;

    @NotNull(message = "交接时间不能为空")
    private LocalDateTime handoverTime;

    @NotBlank(message = "重点关注老人不能为空")
    private String keyElderly;

    @NotBlank(message = "待跟进预警摘要不能为空")
    private String pendingWarningSummary;

    @NotBlank(message = "备注不能为空")
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Integer> warningRecordIds;

    @TableField(exist = false)
    private List<HealthWarningRecord> relatedWarnings;
}
