package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.WarningFollowupTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WarningFollowupTaskMapper extends BaseMapper<WarningFollowupTask> {

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "WHERE t.warning_record_id = #{warningRecordId} " +
            "ORDER BY t.created_at DESC")
    List<WarningFollowupTask> selectByWarningRecordIdWithDetails(@Param("warningRecordId") Integer warningRecordId);

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "WHERE t.assignee_id = #{assigneeId} " +
            "ORDER BY t.deadline ASC")
    List<WarningFollowupTask> selectByAssigneeIdWithDetails(@Param("assigneeId") Integer assigneeId);

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "WHERE t.status IN ('PENDING', 'IN_PROGRESS') " +
            "AND t.deadline BETWEEN #{now} AND #{deadlineThreshold} " +
            "AND t.reminder_24h_sent = FALSE " +
            "ORDER BY t.deadline ASC")
    List<WarningFollowupTask> selectTasksFor24hReminder(@Param("now") LocalDateTime now, @Param("deadlineThreshold") LocalDateTime deadlineThreshold);

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "WHERE t.status IN ('PENDING', 'IN_PROGRESS') " +
            "AND t.deadline BETWEEN #{now} AND #{deadlineThreshold} " +
            "AND t.reminder_1h_sent = FALSE " +
            "ORDER BY t.deadline ASC")
    List<WarningFollowupTask> selectTasksFor1hReminder(@Param("now") LocalDateTime now, @Param("deadlineThreshold") LocalDateTime deadlineThreshold);

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "WHERE t.status IN ('PENDING', 'IN_PROGRESS') " +
            "AND t.deadline < #{now} " +
            "AND t.overdue_reminder_sent = FALSE " +
            "ORDER BY t.deadline ASC")
    List<WarningFollowupTask> selectOverdueTasks(@Param("now") LocalDateTime now);

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "ORDER BY t.created_at DESC")
    List<WarningFollowupTask> selectAllWithDetails();

    @Select("SELECT t.*, e.name as elderlyName, w.warning_message as warningMessage, w.indicator_type as warningIndicatorType " +
            "FROM warning_followup_tasks t " +
            "LEFT JOIN elderly e ON t.elderly_id = e.id " +
            "LEFT JOIN health_warning_records w ON t.warning_record_id = w.id " +
            "WHERE t.id = #{id}")
    WarningFollowupTask selectByIdWithDetails(@Param("id") Integer id);
}
