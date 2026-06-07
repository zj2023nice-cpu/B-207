package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.HealthWarningTimeline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HealthWarningTimelineMapper extends BaseMapper<HealthWarningTimeline> {
    
    @Select("SELECT * FROM health_warning_timeline WHERE warning_record_id = #{warningRecordId} ORDER BY created_at ASC")
    List<HealthWarningTimeline> findByWarningRecordId(Integer warningRecordId);
}
