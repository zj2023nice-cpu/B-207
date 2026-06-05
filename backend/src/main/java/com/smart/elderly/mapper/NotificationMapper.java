package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    @Select("SELECT n.*, e.name as elderly_name FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "ORDER BY n.created_at DESC")
    List<Notification> findAllWithElderlyName();
    
    @Select("SELECT n.*, e.name as elderly_name FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "WHERE n.status = 'UNREAD' " +
            "ORDER BY n.created_at DESC")
    List<Notification> findAllUnreadWithElderlyName();
    
    @Select("SELECT n.*, e.name as elderly_name FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "WHERE n.elderly_id = #{elderlyId} " +
            "ORDER BY n.created_at DESC")
    List<Notification> findByElderlyIdWithName(Integer elderlyId);
    
    @Select("SELECT COUNT(*) FROM notifications WHERE status = 'UNREAD'")
    long countAllUnread();
    
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Notification> findByUserId(Integer userId);
    
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND status = 'UNREAD' ORDER BY created_at DESC")
    List<Notification> findUnreadByUserId(Integer userId);
    
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND status = 'UNREAD'")
    long countUnreadByUserId(Integer userId);
    
    @Update("UPDATE notifications SET status = 'READ', read_at = NOW() WHERE id = #{id}")
    void markAsRead(Integer id);
    
    @Update("UPDATE notifications SET status = 'READ', read_at = NOW() WHERE user_id = #{userId} AND status = 'UNREAD'")
    void markAllAsRead(Integer userId);
    
    @Update("UPDATE notifications SET status = 'READ', read_at = NOW() WHERE status = 'UNREAD'")
    void markAllSystemAsRead();
}
