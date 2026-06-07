package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.service.ElderlyFollowService;
import com.smart.elderly.vo.FollowedElderlyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elderly-follow")
public class ElderlyFollowController {

    @Autowired
    private ElderlyFollowService elderlyFollowService;

    @OperationLog(operation = "关注老人", description = "将某位老人加入关注清单")
    @PostMapping("/follow/{elderlyId}")
    public Result<String> follow(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        boolean success = elderlyFollowService.follow(elderlyId);
        if (success) {
            return Result.success("关注成功");
        } else {
            return Result.error("关注失败");
        }
    }

    @OperationLog(operation = "取消关注老人", description = "将某位老人从关注清单中移除")
    @PostMapping("/unfollow/{elderlyId}")
    public Result<String> unfollow(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        boolean success = elderlyFollowService.unfollow(elderlyId);
        if (success) {
            return Result.success("取消关注成功");
        } else {
            return Result.error("取消关注失败");
        }
    }

    @GetMapping("/check/{elderlyId}")
    public Result<Boolean> checkFollowed(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        return Result.success(elderlyFollowService.isFollowed(elderlyId));
    }

    @GetMapping("/list")
    public Result<List<FollowedElderlyVO>> list() {
        return Result.success(elderlyFollowService.getFollowedList());
    }

    @GetMapping("/ids")
    public Result<List<Integer>> getFollowedIds() {
        return Result.success(elderlyFollowService.getFollowedIds());
    }

    @GetMapping("/map")
    public Result<Map<Integer, Boolean>> getFollowedMap() {
        return Result.success(elderlyFollowService.getFollowedMap());
    }
}
