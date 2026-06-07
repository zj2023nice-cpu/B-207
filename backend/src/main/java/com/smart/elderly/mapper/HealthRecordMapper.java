package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
    @Select("SELECT h.*, e.name as elderly_name FROM health_records h LEFT JOIN elderly e ON h.elderly_id = e.id ORDER BY h.check_time DESC")
    List<HealthRecord> selectWithElderlyName();

    @Select("<script>" +
            "SELECT h.*, e.name as elderly_name FROM health_records h " +
            "LEFT JOIN elderly e ON h.elderly_id = e.id " +
            "<where>" +
            "  <if test='elderlyId != null'>AND h.elderly_id = #{elderlyId}</if>" +
            "  <if test='isAbnormal != null'>AND h.is_abnormal = #{isAbnormal}</if>" +
            "</where>" +
            "ORDER BY h.check_time DESC" +
            "</script>")
    List<HealthRecord> selectWithFilters(@Param("elderlyId") Integer elderlyId, 
                                          @Param("isAbnormal") Boolean isAbnormal);
}
