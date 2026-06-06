package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.elderly.entity.ShiftHandoverRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ShiftHandoverRecordMapper extends BaseMapper<ShiftHandoverRecord> {

    @Select("SELECT * FROM shift_handover_records ORDER BY handover_time DESC")
    List<ShiftHandoverRecord> findAllOrderByTime();

    @Select("SELECT * FROM shift_handover_records ORDER BY handover_time DESC")
    IPage<ShiftHandoverRecord> findAllByPage(Page<ShiftHandoverRecord> page);
}
