package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.WorkbenchQueryDTO;
import com.smart.elderly.service.WorkbenchService;
import com.smart.elderly.vo.WorkbenchItemVO;
import com.smart.elderly.vo.WorkbenchStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {

    @Autowired
    private WorkbenchService workbenchService;

    @GetMapping("/stats")
    public Result<WorkbenchStatsVO> getStats() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(workbenchService.getStats(userId));
    }

    @PostMapping("/items")
    public Result<List<WorkbenchItemVO>> getItems(@RequestBody WorkbenchQueryDTO queryDTO) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        if (queryDTO.getPageNum() == null) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null) {
            queryDTO.setPageSize(20);
        }
        return Result.success(workbenchService.getWorkbenchItems(userId, queryDTO));
    }

    @GetMapping("/items")
    public Result<List<WorkbenchItemVO>> getItemsGet(
            @RequestParam(required = false) List<Integer> elderlyIds,
            @RequestParam(required = false) List<String> priorities,
            @RequestParam(required = false) List<String> itemTypes,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        WorkbenchQueryDTO queryDTO = new WorkbenchQueryDTO();
        queryDTO.setElderlyIds(elderlyIds);
        queryDTO.setPriorities(priorities);
        queryDTO.setItemTypes(itemTypes);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        return Result.success(workbenchService.getWorkbenchItems(userId, queryDTO));
    }

    @GetMapping("/filter-options")
    public Result<Map<String, List<Map<String, String>>>> getFilterOptions() {
        Map<String, List<Map<String, String>>> options = new java.util.HashMap<String, List<Map<String, String>>>();
        options.put("itemTypes", workbenchService.getFilterOptions());
        options.put("priorities", workbenchService.getPriorityOptions());
        return Result.success(options);
    }
}
