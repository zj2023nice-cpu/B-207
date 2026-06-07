package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.NotificationSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationSubscriptionMapper extends BaseMapper<NotificationSubscription> {
    
    @Select("SELECT * FROM notification_subscription WHERE user_id = #{userId}")
    NotificationSubscription findByUserId(Integer userId);
}
