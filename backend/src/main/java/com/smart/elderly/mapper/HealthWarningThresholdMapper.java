package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.HealthWarningThreshold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HealthWarningThresholdMapper extends BaseMapper<HealthWarningThreshold> {
    
    @Select("SELECT * FROM health_warning_thresholds WHERE elderly_id = #{elderlyId} AND enabled = 1")
    List<HealthWarningThreshold> findByElderlyId(Integer elderlyId);
    
    @Select("SELECT * FROM health_warning_thresholds WHERE elderly_id IS NULL AND enabled = 1")
    List<HealthWarningThreshold> findSystemDefaults();
    
    @Select("SELECT * FROM health_warning_thresholds WHERE elderly_id = #{elderlyId} AND indicator_type = #{indicatorType} AND enabled = 1 LIMIT 1")
    HealthWarningThreshold findByElderlyIdAndIndicatorType(Integer elderlyId, String indicatorType);
    
    @Select("SELECT * FROM health_warning_thresholds WHERE elderly_id IS NULL AND indicator_type = #{indicatorType} AND enabled = 1 LIMIT 1")
    HealthWarningThreshold findSystemDefaultByIndicatorType(String indicatorType);
}
