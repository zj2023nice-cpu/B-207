package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.elderly.entity.VisitorVisitRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VisitorVisitRecordMapper extends BaseMapper<VisitorVisitRecord> {

    @Select("SELECT v.*, e.name as elderly_name FROM visitor_visit_records v LEFT JOIN elderly e ON v.elderly_id = e.id ORDER BY v.visit_time DESC")
    List<VisitorVisitRecord> selectWithElderlyName();

    @Select("SELECT v.*, e.name as elderly_name FROM visitor_visit_records v LEFT JOIN elderly e ON v.elderly_id = e.id WHERE v.elderly_id = #{elderlyId} ORDER BY v.visit_time DESC")
    List<VisitorVisitRecord> selectByElderlyId(@Param("elderlyId") Integer elderlyId);

    @Select("SELECT v.*, e.name as elderly_name FROM visitor_visit_records v LEFT JOIN elderly e ON v.elderly_id = e.id ORDER BY v.visit_time DESC")
    IPage<VisitorVisitRecord> selectPageWithElderlyName(Page<VisitorVisitRecord> page);

    @Select("<script>" +
            "SELECT v.*, e.name as elderly_name FROM visitor_visit_records v LEFT JOIN elderly e ON v.elderly_id = e.id " +
            "<where>" +
            "<if test='elderlyId != null'>AND v.elderly_id = #{elderlyId}</if>" +
            "<if test='status != null and status != \"\"'>AND v.status = #{status}</if>" +
            "<if test='startTime != null'>AND v.visit_time &gt;= #{startTime}</if>" +
            "<if test='endTime != null'>AND v.visit_time &lt;= #{endTime}</if>" +
            "</where>" +
            "ORDER BY v.visit_time DESC" +
            "</script>")
    IPage<VisitorVisitRecord> selectPageWithFilters(
            Page<VisitorVisitRecord> page,
            @Param("elderlyId") Integer elderlyId,
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
