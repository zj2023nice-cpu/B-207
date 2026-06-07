package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.HealthRecordCorrection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HealthRecordCorrectionMapper extends BaseMapper<HealthRecordCorrection> {
    
    @Select("SELECT c.*, e.name as elderly_name FROM health_record_corrections c " +
            "LEFT JOIN elderly e ON c.elderly_id = e.id " +
            "WHERE c.health_record_id = #{healthRecordId} " +
            "ORDER BY c.corrected_at DESC")
    List<HealthRecordCorrection> findByHealthRecordIdWithName(@Param("healthRecordId") Integer healthRecordId);
    
    @Select("SELECT c.*, e.name as elderly_name FROM health_record_corrections c " +
            "LEFT JOIN elderly e ON c.elderly_id = e.id " +
            "WHERE c.elderly_id = #{elderlyId} " +
            "ORDER BY c.corrected_at DESC")
    List<HealthRecordCorrection> findByElderlyIdWithName(@Param("elderlyId") Integer elderlyId);
    
    @Select("SELECT COUNT(*) FROM health_record_corrections WHERE health_record_id = #{healthRecordId}")
    Integer countByHealthRecordId(@Param("healthRecordId") Integer healthRecordId);
}
