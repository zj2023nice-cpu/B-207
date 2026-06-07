package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.AnnouncementReadRecord;
import com.smart.elderly.mapper.AnnouncementReadRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AnnouncementReadRecordService extends ServiceImpl<AnnouncementReadRecordMapper, AnnouncementReadRecord> {

    public boolean hasRead(Integer announcementId, Integer userId) {
        LambdaQueryWrapper<AnnouncementReadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementReadRecord::getAnnouncementId, announcementId)
               .eq(AnnouncementReadRecord::getUserId, userId);
        return this.count(wrapper) > 0;
    }

    @Transactional
    public void markAsRead(Integer announcementId, Integer userId) {
        if (!hasRead(announcementId, userId)) {
            baseMapper.insertOrIgnore(announcementId, userId);
        }
    }

    @Transactional
    public void deleteByAnnouncementId(Integer announcementId) {
        baseMapper.deleteByAnnouncementId(announcementId);
    }
}
