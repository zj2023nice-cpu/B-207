package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.ElderlyFollow;
import com.smart.elderly.vo.FollowedElderlyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ElderlyFollowMapper extends BaseMapper<ElderlyFollow> {
    
    @Select("SELECT " +
            "e.id as elderly_id, " +
            "e.name, " +
            "e.age, " +
            "e.gender, " +
            "e.phone, " +
            "e.address, " +
            "e.status, " +
            "f.created_at as follow_time, " +
            "hr.check_time as last_check_time, " +
            "CASE " +
            "  WHEN hr.id IS NOT NULL THEN " +
            "    CONCAT(" +
            "      '血压:', IFNULL(hr.blood_pressure, '-'), " +
            "      ', 体温:', IFNULL(hr.temperature, '-'), " +
            "      ', 心率:', IFNULL(hr.heart_rate, '-'), " +
            "      ', 血氧:', IFNULL(hr.blood_oxygen, '-'), " +
            "      ', 血糖:', IFNULL(hr.blood_sugar, '-')" +
            "    ) " +
            "  ELSE NULL " +
            "END as last_health_summary, " +
            "hr.is_abnormal as last_is_abnormal, " +
            "(SELECT COUNT(*) FROM health_warning_records w WHERE w.elderly_id = e.id AND w.status = 'PENDING') as pending_warning_count, " +
            "(SELECT COUNT(*) FROM notifications n WHERE n.elderly_id = e.id AND n.user_id = #{userId} AND n.status = 'UNREAD') as unread_notification_count " +
            "FROM elderly_follow f " +
            "LEFT JOIN elderly e ON f.elderly_id = e.id " +
            "LEFT JOIN (" +
            "  SELECT h1.* FROM health_records h1 " +
            "  INNER JOIN (" +
            "    SELECT elderly_id, MAX(check_time) as max_time " +
            "    FROM health_records GROUP BY elderly_id" +
            "  ) h2 ON h1.elderly_id = h2.elderly_id AND h1.check_time = h2.max_time " +
            ") hr ON e.id = hr.elderly_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<FollowedElderlyVO> getFollowedElderlyList(@Param("userId") Integer userId);
    
    @Select("SELECT elderly_id FROM elderly_follow WHERE user_id = #{userId}")
    List<Integer> getFollowedElderlyIds(@Param("userId") Integer userId);
}
