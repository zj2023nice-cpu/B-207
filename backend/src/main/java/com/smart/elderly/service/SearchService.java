package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smart.elderly.entity.*;
import com.smart.elderly.mapper.*;
import com.smart.elderly.vo.SearchResultItemVO;
import com.smart.elderly.vo.SearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private HealthWarningRecordMapper healthWarningRecordMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    public SearchResultVO search(String keyword, List<String> modules, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            SearchResultVO empty = new SearchResultVO();
            empty.setKeyword("");
            empty.setTotal(0L);
            empty.setModuleCounts(new HashMap<>());
            empty.setItems(new ArrayList<>());
            return empty;
        }

        String searchKeyword = "%" + keyword.trim() + "%";
        List<SearchResultItemVO> allItems = new ArrayList<>();
        Map<String, Long> moduleCounts = new LinkedHashMap<>();

        if (modules == null || modules.isEmpty() || modules.contains("elderly")) {
            List<SearchResultItemVO> elderlyItems = searchElderly(searchKeyword, limit);
            moduleCounts.put("elderly", (long) elderlyItems.size());
            allItems.addAll(elderlyItems);
        }

        if (modules == null || modules.isEmpty() || modules.contains("health_record")) {
            List<SearchResultItemVO> healthItems = searchHealthRecords(searchKeyword, limit);
            moduleCounts.put("health_record", (long) healthItems.size());
            allItems.addAll(healthItems);
        }

        if (modules == null || modules.isEmpty() || modules.contains("warning")) {
            List<SearchResultItemVO> warningItems = searchWarnings(searchKeyword, limit);
            moduleCounts.put("warning", (long) warningItems.size());
            allItems.addAll(warningItems);
        }

        if (modules == null || modules.isEmpty() || modules.contains("notification")) {
            List<SearchResultItemVO> notificationItems = searchNotifications(searchKeyword, limit);
            moduleCounts.put("notification", (long) notificationItems.size());
            allItems.addAll(notificationItems);
        }

        allItems.sort((a, b) -> {
            if (b.getTime() == null && a.getTime() == null) return 0;
            if (b.getTime() == null) return -1;
            if (a.getTime() == null) return 1;
            return b.getTime().compareTo(a.getTime());
        });

        SearchResultVO result = new SearchResultVO();
        result.setKeyword(keyword.trim());
        result.setTotal((long) allItems.size());
        result.setModuleCounts(moduleCounts);
        result.setItems(allItems);

        return result;
    }

    private List<SearchResultItemVO> searchElderly(String keyword, Integer limit) {
        LambdaQueryWrapper<Elderly> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Elderly::getName, keyword)
                .or().like(Elderly::getPhone, keyword)
                .or().like(Elderly::getAddress, keyword)
                .or().like(Elderly::getEmergencyContactName, keyword)
                .or().like(Elderly::getEmergencyContactPhone, keyword)
                .orderByDesc(Elderly::getUpdatedAt)
                .last(limit != null ? "LIMIT " + limit : "");

        List<Elderly> list = elderlyMapper.selectList(wrapper);
        return list.stream().map(e -> {
            SearchResultItemVO item = new SearchResultItemVO();
            item.setId(e.getId());
            item.setModule("elderly");
            item.setModuleName("老人信息");
            item.setTitle(e.getName());
            StringBuilder desc = new StringBuilder();
            if (e.getAge() != null) desc.append(e.getAge()).append("岁 ");
            if (e.getGender() != null) desc.append(e.getGender()).append(" ");
            if (e.getPhone() != null) desc.append(e.getPhone());
            item.setDescription(desc.toString());
            item.setRoutePath("/elderly");
            item.setTime(e.getUpdatedAt() != null ? e.getUpdatedAt() : e.getCreatedAt());
            item.setExtraInfo(e.getStatus());
            return item;
        }).collect(Collectors.toList());
    }

    private List<SearchResultItemVO> searchHealthRecords(String keyword, Integer limit) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getIsAbnormal, true)
                .and(w -> w.like(HealthRecord::getAbnormalReason, keyword)
                        .or().like(HealthRecord::getBloodPressure, keyword))
                .orderByDesc(HealthRecord::getCheckTime)
                .last(limit != null ? "LIMIT " + limit : "");

        List<HealthRecord> list = healthRecordMapper.selectList(wrapper);
        Set<Integer> elderlyIds = list.stream().map(HealthRecord::getElderlyId).collect(Collectors.toSet());
        Map<Integer, String> elderlyNameMap = new HashMap<>();
        if (!elderlyIds.isEmpty()) {
            List<Elderly> elderlyList = elderlyMapper.selectBatchIds(elderlyIds);
            elderlyNameMap = elderlyList.stream().collect(Collectors.toMap(Elderly::getId, Elderly::getName));
        }

        final Map<Integer, String> finalElderlyNameMap = elderlyNameMap;
        return list.stream().map(r -> {
            SearchResultItemVO item = new SearchResultItemVO();
            item.setId(r.getId());
            item.setModule("health_record");
            item.setModuleName("异常健康记录");
            String elderlyName = finalElderlyNameMap.getOrDefault(r.getElderlyId(), "未知老人");
            item.setTitle(elderlyName + " - 异常记录");
            item.setDescription(r.getAbnormalReason());
            item.setRoutePath("/health?elderlyId=" + r.getElderlyId());
            item.setTime(r.getCheckTime());
            item.setExtraInfo(r.getCorrected() != null && r.getCorrected() ? "已修正" : "未修正");
            return item;
        }).collect(Collectors.toList());
    }

    private List<SearchResultItemVO> searchWarnings(String keyword, Integer limit) {
        LambdaQueryWrapper<HealthWarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HealthWarningRecord::getStatus, Arrays.asList("PENDING", "REOPENED", "ESCALATED"))
                .and(w -> w.like(HealthWarningRecord::getWarningMessage, keyword)
                        .or().like(HealthWarningRecord::getIndicatorType, keyword)
                        .or().like(HealthWarningRecord::getWarningLevel, keyword))
                .orderByDesc(HealthWarningRecord::getCreatedAt)
                .last(limit != null ? "LIMIT " + limit : "");

        List<HealthWarningRecord> list = healthWarningRecordMapper.selectList(wrapper);
        Set<Integer> elderlyIds = list.stream().map(HealthWarningRecord::getElderlyId).collect(Collectors.toSet());
        Map<Integer, String> elderlyNameMap = new HashMap<>();
        if (!elderlyIds.isEmpty()) {
            List<Elderly> elderlyList = elderlyMapper.selectBatchIds(elderlyIds);
            elderlyNameMap = elderlyList.stream().collect(Collectors.toMap(Elderly::getId, Elderly::getName));
        }

        final Map<Integer, String> finalElderlyNameMap = elderlyNameMap;
        return list.stream().map(w -> {
            SearchResultItemVO item = new SearchResultItemVO();
            item.setId(w.getId());
            item.setModule("warning");
            item.setModuleName("待处理预警");
            String elderlyName = finalElderlyNameMap.getOrDefault(w.getElderlyId(), "未知老人");
            item.setTitle(elderlyName + " - " + w.getWarningLevel() + "预警");
            item.setDescription(w.getWarningMessage());
            item.setRoutePath("/warning?id=" + w.getId());
            item.setTime(w.getCreatedAt());
            item.setExtraInfo(w.getStatus());
            return item;
        }).collect(Collectors.toList());
    }

    private List<SearchResultItemVO> searchNotifications(String keyword, Integer limit) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Notification::getTitle, keyword)
                .or().like(Notification::getContent, keyword)
                .orderByDesc(Notification::getCreatedAt)
                .last(limit != null ? "LIMIT " + limit : "");

        List<Notification> list = notificationMapper.selectList(wrapper);
        Set<Integer> elderlyIds = list.stream()
                .filter(n -> n.getElderlyId() != null)
                .map(Notification::getElderlyId)
                .collect(Collectors.toSet());
        Map<Integer, String> elderlyNameMap = new HashMap<>();
        if (!elderlyIds.isEmpty()) {
            List<Elderly> elderlyList = elderlyMapper.selectBatchIds(elderlyIds);
            elderlyNameMap = elderlyList.stream().collect(Collectors.toMap(Elderly::getId, Elderly::getName));
        }

        final Map<Integer, String> finalElderlyNameMap = elderlyNameMap;
        return list.stream().map(n -> {
            SearchResultItemVO item = new SearchResultItemVO();
            item.setId(n.getId());
            item.setModule("notification");
            item.setModuleName("通知消息");
            item.setTitle(n.getTitle());
            item.setDescription(n.getContent());
            if (n.getElderlyId() != null) {
                item.setRoutePath("/notification?elderlyId=" + n.getElderlyId());
            } else {
                item.setRoutePath("/notification");
            }
            item.setTime(n.getCreatedAt());
            String status = n.getStatus();
            if (n.getInvalidated() != null && n.getInvalidated()) {
                status = "已失效";
            }
            item.setExtraInfo(status);
            return item;
        }).collect(Collectors.toList());
    }
}
