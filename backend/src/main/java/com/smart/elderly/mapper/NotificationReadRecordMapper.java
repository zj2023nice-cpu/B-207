package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.NotificationReadRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationReadRecordMapper extends BaseMapper<NotificationReadRecord> {

    @Insert("INSERT IGNORE INTO notification_read_records (notification_id, user_id, read_at) " +
            "VALUES (#{notificationId}, #{userId}, NOW())")
    void insertOrIgnore(@Param("notificationId") Integer notificationId, @Param("userId") Integer userId);
}
