package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.ElderlyTagRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ElderlyTagRelationMapper extends BaseMapper<ElderlyTagRelation> {
    
    @Select("SELECT tag_id FROM elderly_tag_relation WHERE elderly_id = #{elderlyId}")
    List<Integer> selectTagIdsByElderlyId(@Param("elderlyId") Integer elderlyId);
    
    @Select("SELECT elderly_id FROM elderly_tag_relation WHERE tag_id = #{tagId}")
    List<Integer> selectElderlyIdsByTagId(@Param("tagId") Integer tagId);
    
    @Delete("DELETE FROM elderly_tag_relation WHERE elderly_id = #{elderlyId}")
    void deleteByElderlyId(@Param("elderlyId") Integer elderlyId);
    
    @Delete("DELETE FROM elderly_tag_relation WHERE elderly_id = #{elderlyId} AND tag_id = #{tagId}")
    void deleteByElderlyIdAndTagId(@Param("elderlyId") Integer elderlyId, @Param("tagId") Integer tagId);
}
