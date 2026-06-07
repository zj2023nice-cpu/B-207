package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    
    @Select("<script>" +
            "SELECT n.*, e.name as elderly_name FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "WHERE 1=1 " +
            "<if test='notificationTypes != null and notificationTypes.size() > 0'>" +
            "  AND n.notification_type IN " +
            "  <foreach item='type' collection='notificationTypes' open='(' separator=',' close=')'>" +
            "    #{type}" +
            "  </foreach>" +
            "</if>" +
            "<if test='onlyAbnormal != null and onlyAbnormal == true'>" +
            "  AND n.notification_type LIKE '%WARNING%'" +
            "</if>" +
            "<if test='followedElderlyIds != null and followedElderlyIds.size() > 0'>" +
            "  AND (n.elderly_id IS NULL OR n.elderly_id IN " +
            "  <foreach item='elderlyId' collection='followedElderlyIds' open='(' separator=',' close=')'>" +
            "    #{elderlyId}" +
            "  </foreach>" +
            "  )" +
            "</if>" +
            "<if test='onlyFollowedElderly != null and onlyFollowedElderly == true and followedElderlyIds != null and followedElderlyIds.size() == 0'>" +
            "  AND n.elderly_id IS NULL" +
            "</if>" +
            "ORDER BY n.created_at DESC" +
            "</script>")
    List<Notification> findAllWithSubscription(
            @Param("notificationTypes") List<String> notificationTypes,
            @Param("onlyAbnormal") Boolean onlyAbnormal,
            @Param("onlyFollowedElderly") Boolean onlyFollowedElderly,
            @Param("followedElderlyIds") List<Integer> followedElderlyIds);
    
    @Select("<script>" +
            "SELECT n.*, e.name as elderly_name FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "WHERE n.status = 'UNREAD' " +
            "<if test='notificationTypes != null and notificationTypes.size() > 0'>" +
            "  AND n.notification_type IN " +
            "  <foreach item='type' collection='notificationTypes' open='(' separator=',' close=')'>" +
            "    #{type}" +
            "  </foreach>" +
            "</if>" +
            "<if test='onlyAbnormal != null and onlyAbnormal == true'>" +
            "  AND n.notification_type LIKE '%WARNING%'" +
            "</if>" +
            "<if test='followedElderlyIds != null and followedElderlyIds.size() > 0'>" +
            "  AND (n.elderly_id IS NULL OR n.elderly_id IN " +
            "  <foreach item='elderlyId' collection='followedElderlyIds' open='(' separator=',' close=')'>" +
            "    #{elderlyId}" +
            "  </foreach>" +
            "  )" +
            "</if>" +
            "<if test='onlyFollowedElderly != null and onlyFollowedElderly == true and followedElderlyIds != null and followedElderlyIds.size() == 0'>" +
            "  AND n.elderly_id IS NULL" +
            "</if>" +
            "ORDER BY n.created_at DESC" +
            "</script>")
    List<Notification> findAllUnreadWithSubscription(
            @Param("notificationTypes") List<String> notificationTypes,
            @Param("onlyAbnormal") Boolean onlyAbnormal,
            @Param("onlyFollowedElderly") Boolean onlyFollowedElderly,
            @Param("followedElderlyIds") List<Integer> followedElderlyIds);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM notifications n " +
            "WHERE n.status = 'UNREAD' " +
            "<if test='notificationTypes != null and notificationTypes.size() > 0'>" +
            "  AND n.notification_type IN " +
            "  <foreach item='type' collection='notificationTypes' open='(' separator=',' close=')'>" +
            "    #{type}" +
            "  </foreach>" +
            "</if>" +
            "<if test='onlyAbnormal != null and onlyAbnormal == true'>" +
            "  AND n.notification_type LIKE '%WARNING%'" +
            "</if>" +
            "<if test='followedElderlyIds != null and followedElderlyIds.size() > 0'>" +
            "  AND (n.elderly_id IS NULL OR n.elderly_id IN " +
            "  <foreach item='elderlyId' collection='followedElderlyIds' open='(' separator=',' close=')'>" +
            "    #{elderlyId}" +
            "  </foreach>" +
            "  )" +
            "</if>" +
            "<if test='onlyFollowedElderly != null and onlyFollowedElderly == true and followedElderlyIds != null and followedElderlyIds.size() == 0'>" +
            "  AND n.elderly_id IS NULL" +
            "</if>" +
            "</script>")
    long countAllUnreadWithSubscription(
            @Param("notificationTypes") List<String> notificationTypes,
            @Param("onlyAbnormal") Boolean onlyAbnormal,
            @Param("onlyFollowedElderly") Boolean onlyFollowedElderly,
            @Param("followedElderlyIds") List<Integer> followedElderlyIds);
    
    @Update("UPDATE notifications SET status = 'READ', read_at = NOW() WHERE id = #{id}")
    void markAsRead(Integer id);
    
    @Update("UPDATE notifications SET status = 'READ', read_at = NOW() WHERE user_id = #{userId} AND status = 'UNREAD'")
    void markAllAsRead(Integer userId);
    
    @Update("UPDATE notifications SET status = 'READ', read_at = NOW() WHERE status = 'UNREAD'")
    void markAllSystemAsRead();
}
