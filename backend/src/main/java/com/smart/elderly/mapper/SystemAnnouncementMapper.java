package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.SystemAnnouncement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SystemAnnouncementMapper extends BaseMapper<SystemAnnouncement> {

    @Select("SELECT sa.*, " +
            "CASE WHEN arr.id IS NOT NULL THEN TRUE ELSE FALSE END AS isRead " +
            "FROM system_announcements sa " +
            "LEFT JOIN announcement_read_records arr " +
            "ON sa.id = arr.announcement_id AND arr.user_id = #{userId} " +
            "WHERE sa.status = 'PUBLISHED' " +
            "AND sa.publish_start_time <= #{now} " +
            "AND (sa.publish_end_time IS NULL OR sa.publish_end_time >= #{now}) " +
            "ORDER BY sa.is_pinned DESC, sa.publish_start_time DESC, sa.created_at DESC")
    List<SystemAnnouncement> findActiveAnnouncementsWithReadStatus(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    @Select("SELECT COUNT(*) FROM system_announcements sa " +
            "WHERE sa.status = 'PUBLISHED' " +
            "AND sa.publish_start_time <= #{now} " +
            "AND (sa.publish_end_time IS NULL OR sa.publish_end_time >= #{now}) " +
            "AND sa.id NOT IN (SELECT announcement_id FROM announcement_read_records WHERE user_id = #{userId})")
    long countUnreadAnnouncements(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    @Select("SELECT * FROM system_announcements " +
            "WHERE status = 'PUBLISHED' " +
            "AND publish_start_time <= #{now} " +
            "AND (publish_end_time IS NULL OR publish_end_time >= #{now}) " +
            "ORDER BY is_pinned DESC, publish_start_time DESC, created_at DESC")
    List<SystemAnnouncement> findAllActiveAnnouncements(@Param("now") LocalDateTime now);
}
