package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.QualityReviewResult;
import com.smart.elderly.dto.QualityReviewSubmitDTO;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.HealthRecordQualityReview;
import com.smart.elderly.enums.QualityStatus;
import com.smart.elderly.enums.ReviewStatus;
import com.smart.elderly.mapper.HealthRecordMapper;
import com.smart.elderly.mapper.HealthRecordQualityReviewMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HealthRecordQualityReviewService extends ServiceImpl<HealthRecordQualityReviewMapper, HealthRecordQualityReview> {

    @Autowired
    private QualityReviewEngine reviewEngine;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private HealthRecordQualityReviewMapper reviewMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void processHealthRecordQuality(HealthRecord record) {
        QualityReviewResult result = reviewEngine.analyze(record);
        
        record.setQualityScore(result.getQualityScore());
        record.setQualityIssues(toJson(result.getIssues()));
        
        if (result.isSuspicious()) {
            record.setQualityStatus(QualityStatus.SUSPICIOUS.getCode());
            createReview(record, result);
        } else {
            record.setQualityStatus(QualityStatus.NORMAL.getCode());
        }
        
        if (record.getId() != null) {
            healthRecordMapper.updateById(record);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return "[]";
        }
    }

    private void createReview(HealthRecord record, QualityReviewResult result) {
        HealthRecordQualityReview review = new HealthRecordQualityReview();
        review.setHealthRecordId(record.getId());
        review.setElderlyId(record.getElderlyId());
        review.setReviewStatus(ReviewStatus.PENDING.getCode());
        review.setTriggeredRules(toJson(result.getTriggeredRules()));
        review.setIssuesSummary(result.getIssuesSummary());
        review.setQualityScore(result.getQualityScore());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    public List<HealthRecordQualityReview> getReviewsWithFilters(Integer elderlyId, String reviewStatus) {
        return reviewMapper.selectReviewsWithFilters(elderlyId, reviewStatus);
    }

    public HealthRecordQualityReview getReviewDetail(Integer id) {
        HealthRecordQualityReview review = reviewMapper.selectById(id);
        if (review != null) {
            HealthRecord record = healthRecordMapper.selectById(review.getHealthRecordId());
            review.setHealthRecord(record);
        }
        return review;
    }

    @Transactional
    public void submitReview(QualityReviewSubmitDTO dto) {
        HealthRecordQualityReview review = reviewMapper.selectById(dto.getReviewId());
        if (review == null) {
            throw new IllegalArgumentException("复核记录不存在");
        }

        review.setReviewStatus(dto.getReviewStatus());
        review.setReviewConclusion(dto.getReviewConclusion());
        review.setIgnoreReason(dto.getIgnoreReason());
        review.setReviewerId(UserContextHolder.getUserId());
        review.setReviewerName(UserContextHolder.getUsername() != null ? UserContextHolder.getUsername() : "系统");
        review.setReviewTime(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        HealthRecord record = healthRecordMapper.selectById(review.getHealthRecordId());
        if (record != null) {
            String recordStatus = ReviewStatus.IGNORED.getCode().equals(dto.getReviewStatus())
                    ? QualityStatus.IGNORED.getCode()
                    : QualityStatus.REVIEWED.getCode();
            record.setQualityStatus(recordStatus);
            healthRecordMapper.updateById(record);
        }
    }

    public Map<String, Long> getReviewStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", reviewMapper.countPendingReviews());
        stats.put("total", reviewMapper.selectCount(null));
        
        long approved = reviewMapper.selectCount(
            new LambdaQueryWrapper<HealthRecordQualityReview>()
                .eq(HealthRecordQualityReview::getReviewStatus, ReviewStatus.APPROVED.getCode())
        );
        stats.put("approved", approved);
        
        long corrected = reviewMapper.selectCount(
            new LambdaQueryWrapper<HealthRecordQualityReview>()
                .eq(HealthRecordQualityReview::getReviewStatus, ReviewStatus.CORRECTED.getCode())
        );
        stats.put("corrected", corrected);
        
        long ignored = reviewMapper.selectCount(
            new LambdaQueryWrapper<HealthRecordQualityReview>()
                .eq(HealthRecordQualityReview::getReviewStatus, ReviewStatus.IGNORED.getCode())
        );
        stats.put("ignored", ignored);
        
        return stats;
    }

    public HealthRecordQualityReview getByHealthRecordId(Integer healthRecordId) {
        return reviewMapper.selectOne(
            new LambdaQueryWrapper<HealthRecordQualityReview>()
                .eq(HealthRecordQualityReview::getHealthRecordId, healthRecordId)
                .orderByDesc(HealthRecordQualityReview::getCreatedAt)
                .last("LIMIT 1")
        );
    }
}
