package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.ElderlyFollow;
import com.smart.elderly.mapper.ElderlyFollowMapper;
import com.smart.elderly.vo.FollowedElderlyVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElderlyFollowService extends ServiceImpl<ElderlyFollowMapper, ElderlyFollow> {

    @Transactional
    public boolean follow(Integer elderlyId) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null || elderlyId == null) {
            return false;
        }
        
        ElderlyFollow exist = this.getOne(new LambdaQueryWrapper<ElderlyFollow>()
                .eq(ElderlyFollow::getUserId, userId)
                .eq(ElderlyFollow::getElderlyId, elderlyId));
        
        if (exist != null) {
            return true;
        }
        
        ElderlyFollow follow = new ElderlyFollow();
        follow.setUserId(userId);
        follow.setElderlyId(elderlyId);
        follow.setCreatedAt(LocalDateTime.now());
        return this.save(follow);
    }

    @Transactional
    public boolean unfollow(Integer elderlyId) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null || elderlyId == null) {
            return false;
        }
        return this.remove(new LambdaQueryWrapper<ElderlyFollow>()
                .eq(ElderlyFollow::getUserId, userId)
                .eq(ElderlyFollow::getElderlyId, elderlyId));
    }

    public boolean isFollowed(Integer elderlyId) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null || elderlyId == null) {
            return false;
        }
        Long count = this.count(new LambdaQueryWrapper<ElderlyFollow>()
                .eq(ElderlyFollow::getUserId, userId)
                .eq(ElderlyFollow::getElderlyId, elderlyId));
        return count > 0;
    }

    public List<FollowedElderlyVO> getFollowedList() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return List.of();
        }
        return baseMapper.getFollowedElderlyList(userId);
    }

    public List<Integer> getFollowedIds() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return List.of();
        }
        return baseMapper.getFollowedElderlyIds(userId);
    }

    public Map<Integer, Boolean> getFollowedMap() {
        List<Integer> ids = getFollowedIds();
        Map<Integer, Boolean> map = new HashMap<>();
        for (Integer id : ids) {
            map.put(id, true);
        }
        return map;
    }
}
