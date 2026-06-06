package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.ShiftHandoverWarningRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ShiftHandoverWarningRelationMapper extends BaseMapper<ShiftHandoverWarningRelation> {

    @Select("SELECT warning_record_id FROM shift_handover_warning_relations WHERE handover_record_id = #{handoverRecordId}")
    List<Integer> findWarningRecordIdsByHandoverId(Integer handoverRecordId);

    @Select("DELETE FROM shift_handover_warning_relations WHERE handover_record_id = #{handoverRecordId}")
    void deleteByHandoverId(Integer handoverRecordId);
}
