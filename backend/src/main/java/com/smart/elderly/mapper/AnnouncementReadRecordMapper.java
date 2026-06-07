package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.AnnouncementReadRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnnouncementReadRecordMapper extends BaseMapper<AnnouncementReadRecord> {

    @Insert("INSERT IGNORE INTO announcement_read_records (announcement_id, user_id, read_at) " +
            "VALUES (#{announcementId}, #{userId}, NOW())")
    void insertOrIgnore(@Param("announcementId") Integer announcementId, @Param("userId") Integer userId);

    @Delete("DELETE FROM announcement_read_records WHERE announcement_id = #{announcementId}")
    void deleteByAnnouncementId(@Param("announcementId") Integer announcementId);
}
