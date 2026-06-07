package com.smart.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.elderly.entity.HealthRecordQualityReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HealthRecordQualityReviewMapper extends BaseMapper<HealthRecordQualityReview> {

    @Select({"<script>",
            "SELECT r.*, e.name as elderlyName FROM health_record_quality_reviews r ",
            "LEFT JOIN elderly e ON r.elderly_id = e.id ",
            "WHERE 1=1 ",
            "<if test='elderlyId != null'> AND r.elderly_id = #{elderlyId} </if>",
            "<if test='reviewStatus != null'> AND r.review_status = #{reviewStatus} </if>",
            "ORDER BY r.created_at DESC",
            "</script>"})
    List<HealthRecordQualityReview> selectReviewsWithFilters(
            @Param("elderlyId") Integer elderlyId,
            @Param("reviewStatus") String reviewStatus);

    @Select("SELECT COUNT(*) FROM health_record_quality_reviews WHERE review_status = 'PENDING'")
    long countPendingReviews();
}
