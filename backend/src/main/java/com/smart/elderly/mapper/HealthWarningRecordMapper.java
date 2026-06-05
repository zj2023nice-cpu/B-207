package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.HealthWarningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HealthWarningRecordMapper extends BaseMapper<HealthWarningRecord> {
    
    @Select("SELECT w.*, e.name as elderly_name FROM health_warning_records w " +
            "LEFT JOIN elderly e ON w.elderly_id = e.id " +
            "ORDER BY w.created_at DESC")
    List<HealthWarningRecord> findAllWithElderlyName();
    
    @Select("SELECT w.*, e.name as elderly_name FROM health_warning_records w " +
            "LEFT JOIN elderly e ON w.elderly_id = e.id " +
            "WHERE w.elderly_id = #{elderlyId} " +
            "ORDER BY w.created_at DESC")
    List<HealthWarningRecord> findByElderlyIdWithName(Integer elderlyId);
    
    @Select("SELECT w.*, e.name as elderly_name FROM health_warning_records w " +
            "LEFT JOIN elderly e ON w.elderly_id = e.id " +
            "WHERE w.status = #{status} " +
            "ORDER BY w.created_at DESC")
    List<HealthWarningRecord> findByStatusWithElderlyName(String status);
    
    @Select("SELECT COUNT(*) FROM health_warning_records WHERE status = 'PENDING'")
    long countPending();
}
