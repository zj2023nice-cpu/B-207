package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.elderly.entity.NursingObservationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface NursingObservationRecordMapper extends BaseMapper<NursingObservationRecord> {

    @Select("SELECT n.*, e.name as elderly_name FROM nursing_observation_records n LEFT JOIN elderly e ON n.elderly_id = e.id ORDER BY n.observation_time DESC")
    List<NursingObservationRecord> selectWithElderlyName();

    @Select("SELECT n.*, e.name as elderly_name FROM nursing_observation_records n LEFT JOIN elderly e ON n.elderly_id = e.id WHERE n.elderly_id = #{elderlyId} ORDER BY n.observation_time DESC")
    List<NursingObservationRecord> selectByElderlyId(@Param("elderlyId") Integer elderlyId);

    @Select("SELECT n.*, e.name as elderly_name FROM nursing_observation_records n LEFT JOIN elderly e ON n.elderly_id = e.id ORDER BY n.observation_time DESC")
    IPage<NursingObservationRecord> selectPageWithElderlyName(Page<NursingObservationRecord> page);
}
