package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.ElderlyTagRelation;
import com.smart.elderly.mapper.ElderlyTagRelationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElderlyTagRelationService extends ServiceImpl<ElderlyTagRelationMapper, ElderlyTagRelation> {
    
    public List<Integer> getTagIdsByElderlyId(Integer elderlyId) {
        return baseMapper.selectTagIdsByElderlyId(elderlyId);
    }
    
    public List<Integer> getElderlyIdsByTagId(Integer tagId) {
        return baseMapper.selectElderlyIdsByTagId(tagId);
    }
    
    public void deleteByElderlyId(Integer elderlyId) {
        baseMapper.deleteByElderlyId(elderlyId);
    }
    
    public void deleteByElderlyIdAndTagId(Integer elderlyId, Integer tagId) {
        baseMapper.deleteByElderlyIdAndTagId(elderlyId, tagId);
    }
}
