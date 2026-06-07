package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    String BASE_SELECT = "SELECT n.id, n.user_id, n.elderly_id, n.warning_record_id, n.title, n.content, n.notification_type, " +
            "CASE WHEN nr.id IS NOT NULL THEN 'READ' ELSE 'UNREAD' END AS status, n.created_at, n.read_at, e.name AS elderly_name, " +
            "n.invalidated, n.invalidated_at, n.invalidated_reason ";

    String NOT_INVALIDATED = "AND (n.invalidated IS NULL OR n.invalidated = FALSE) ";

    @Select(BASE_SELECT +
            "FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE (n.user_id IS NULL OR n.user_id = #{userId}) " +
            NOT_INVALIDATED +
            "ORDER BY n.created_at DESC")
    List<Notification> findAllVisibleWithElderlyName(@Param("userId") Integer userId);

    @Select("SELECT n.id, n.user_id, n.elderly_id, n.warning_record_id, n.title, n.content, n.notification_type, " +
            "'UNREAD' AS status, n.created_at, n.read_at, e.name AS elderly_name, " +
            "n.invalidated, n.invalidated_at, n.invalidated_reason " +
            "FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE nr.id IS NULL AND (n.user_id IS NULL OR n.user_id = #{userId}) " +
            NOT_INVALIDATED +
            "ORDER BY n.created_at DESC")
    List<Notification> findAllVisibleUnreadWithElderlyName(@Param("userId") Integer userId);

    @Select(BASE_SELECT +
            "FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE (n.user_id IS NULL OR n.user_id = #{userId}) AND n.elderly_id = #{elderlyId} " +
            NOT_INVALIDATED +
            "ORDER BY n.created_at DESC")
    List<Notification> findVisibleByElderlyIdWithName(@Param("elderlyId") Integer elderlyId, @Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM notifications n " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE nr.id IS NULL AND (n.user_id IS NULL OR n.user_id = #{userId}) " +
            NOT_INVALIDATED)
    long countVisibleUnread(@Param("userId") Integer userId);

    @Select("<script>" +
            "SELECT n.id, n.user_id, n.elderly_id, n.warning_record_id, n.title, n.content, n.notification_type, " +
            "CASE WHEN nr.id IS NOT NULL THEN 'READ' ELSE 'UNREAD' END AS status, n.created_at, n.read_at, e.name AS elderly_name, " +
            "n.invalidated, n.invalidated_at, n.invalidated_reason " +
            "FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE (n.user_id IS NULL OR n.user_id = #{userId}) " +
            "AND (n.invalidated IS NULL OR n.invalidated = FALSE) " +
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
            @Param("userId") Integer userId,
            @Param("notificationTypes") List<String> notificationTypes,
            @Param("onlyAbnormal") Boolean onlyAbnormal,
            @Param("onlyFollowedElderly") Boolean onlyFollowedElderly,
            @Param("followedElderlyIds") List<Integer> followedElderlyIds);

    @Select("<script>" +
            "SELECT n.id, n.user_id, n.elderly_id, n.warning_record_id, n.title, n.content, n.notification_type, " +
            "'UNREAD' AS status, n.created_at, n.read_at, e.name AS elderly_name, " +
            "n.invalidated, n.invalidated_at, n.invalidated_reason " +
            "FROM notifications n " +
            "LEFT JOIN elderly e ON n.elderly_id = e.id " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE nr.id IS NULL AND (n.user_id IS NULL OR n.user_id = #{userId}) " +
            "AND (n.invalidated IS NULL OR n.invalidated = FALSE) " +
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
            @Param("userId") Integer userId,
            @Param("notificationTypes") List<String> notificationTypes,
            @Param("onlyAbnormal") Boolean onlyAbnormal,
            @Param("onlyFollowedElderly") Boolean onlyFollowedElderly,
            @Param("followedElderlyIds") List<Integer> followedElderlyIds);

    @Select("<script>" +
            "SELECT COUNT(*) FROM notifications n " +
            "LEFT JOIN notification_read_records nr ON nr.notification_id = n.id AND nr.user_id = #{userId} " +
            "WHERE nr.id IS NULL AND (n.user_id IS NULL OR n.user_id = #{userId}) " +
            "AND (n.invalidated IS NULL OR n.invalidated = FALSE) " +
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
            @Param("userId") Integer userId,
            @Param("notificationTypes") List<String> notificationTypes,
            @Param("onlyAbnormal") Boolean onlyAbnormal,
            @Param("onlyFollowedElderly") Boolean onlyFollowedElderly,
            @Param("followedElderlyIds") List<Integer> followedElderlyIds);
}
