package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.NotificationReadRecord;
import com.smart.elderly.mapper.NotificationReadRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationReadRecordService extends ServiceImpl<NotificationReadRecordMapper, NotificationReadRecord> {

    public boolean hasRead(Integer notificationId, Integer userId) {
        LambdaQueryWrapper<NotificationReadRecord> wrapper = new LambdaQueryWrapper<NotificationReadRecord>();
        wrapper.eq(NotificationReadRecord::getNotificationId, notificationId)
                .eq(NotificationReadRecord::getUserId, userId);
        return this.count(wrapper) > 0;
    }

    @Transactional
    public void markAsRead(Integer notificationId, Integer userId) {
        if (!hasRead(notificationId, userId)) {
            baseMapper.insertOrIgnore(notificationId, userId);
        }
    }
}
