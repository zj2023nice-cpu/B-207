package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("shift_handover_warning_relations")
public class ShiftHandoverWarningRelation {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer handoverRecordId;

    private Integer warningRecordId;

    private LocalDateTime createdAt;
}
