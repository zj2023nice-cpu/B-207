package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("elderly_tag_relation")
public class ElderlyTagRelation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer elderlyId;
    
    private Integer tagId;
    
    private LocalDateTime createdAt;
}
