package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.dto.TagBindRequest;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.ElderlyTag;
import com.smart.elderly.entity.ElderlyTagRelation;
import com.smart.elderly.service.ElderlyService;
import com.smart.elderly.service.ElderlyTagRelationService;
import com.smart.elderly.service.ElderlyTagService;
import com.smart.elderly.vo.ElderlyWithTagsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/elderly-tags")
public class ElderlyTagController {

    @Autowired
    private ElderlyTagService elderlyTagService;

    @Autowired
    private ElderlyTagRelationService elderlyTagRelationService;

    @Autowired
    private ElderlyService elderlyService;

    @GetMapping("/list")
    public Result<List<ElderlyTag>> list(@RequestParam(required = false) String status) {
        List<ElderlyTag> list;
        if (status != null && !status.isEmpty()) {
            list = elderlyTagService.lambdaQuery()
                    .eq(ElderlyTag::getStatus, status)
                    .orderByAsc(ElderlyTag::getSort)
                    .orderByDesc(ElderlyTag::getCreatedAt)
                    .list();
        } else {
            list = elderlyTagService.lambdaQuery()
                    .orderByAsc(ElderlyTag::getSort)
                    .orderByDesc(ElderlyTag::getCreatedAt)
                    .list();
        }
        return Result.success(list);
    }

    @OperationLog(operation = "新增标签", description = "新增老人标签")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody ElderlyTag tag) {
        if (tag.getStatus() == null) {
            tag.setStatus("启用");
        }
        if (tag.getSort() == null) {
            tag.setSort(0);
        }
        elderlyTagService.save(tag);
        return Result.success("添加成功");
    }

    @OperationLog(operation = "编辑标签", description = "编辑老人标签")
    @PutMapping("/update")
    public Result<String> update(@RequestBody ElderlyTag tag) {
        if (tag.getId() == null) {
            return Result.error("ID不能为空");
        }
        elderlyTagService.updateById(tag);
        return Result.success("修改成功");
    }

    @OperationLog(operation = "删除标签", description = "删除老人标签")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        elderlyTagService.removeById(id);
        return Result.success("删除成功");
    }

    @OperationLog(operation = "停用标签", description = "停用老人标签")
    @PutMapping("/disable/{id}")
    public Result<String> disable(@PathVariable Integer id) {
        ElderlyTag tag = new ElderlyTag();
        tag.setId(id);
        tag.setStatus("停用");
        elderlyTagService.updateById(tag);
        return Result.success("停用成功");
    }

    @OperationLog(operation = "启用标签", description = "启用老人标签")
    @PutMapping("/enable/{id}")
    public Result<String> enable(@PathVariable Integer id) {
        ElderlyTag tag = new ElderlyTag();
        tag.setId(id);
        tag.setStatus("启用");
        elderlyTagService.updateById(tag);
        return Result.success("启用成功");
    }

    @GetMapping("/elderly/{elderlyId}")
    public Result<List<ElderlyTag>> getTagsByElderlyId(@PathVariable Integer elderlyId) {
        List<Integer> tagIds = elderlyTagRelationService.getTagIdsByElderlyId(elderlyId);
        if (tagIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<ElderlyTag> tags = elderlyTagService.listByIds(tagIds);
        return Result.success(tags);
    }

    @OperationLog(operation = "批量绑定标签", description = "为老人批量绑定标签")
    @PostMapping("/bind")
    public Result<String> bindTags(@RequestBody TagBindRequest request) {
        if (request.getElderlyIds() == null || request.getElderlyIds().isEmpty()) {
            return Result.error("请选择老人");
        }
        if (request.getTagIds() == null || request.getTagIds().isEmpty()) {
            return Result.error("请选择标签");
        }
        for (Integer elderlyId : request.getElderlyIds()) {
            List<Integer> existingTagIds = elderlyTagRelationService.getTagIdsByElderlyId(elderlyId);
            for (Integer tagId : request.getTagIds()) {
                if (!existingTagIds.contains(tagId)) {
                    ElderlyTagRelation relation = new ElderlyTagRelation();
                    relation.setElderlyId(elderlyId);
                    relation.setTagId(tagId);
                    elderlyTagRelationService.save(relation);
                }
            }
        }
        return Result.success("绑定成功");
    }

    @OperationLog(operation = "批量解绑标签", description = "为老人批量解绑标签")
    @PostMapping("/unbind")
    public Result<String> unbindTags(@RequestBody TagBindRequest request) {
        if (request.getElderlyIds() == null || request.getElderlyIds().isEmpty()) {
            return Result.error("请选择老人");
        }
        if (request.getTagIds() == null || request.getTagIds().isEmpty()) {
            return Result.error("请选择标签");
        }
        for (Integer elderlyId : request.getElderlyIds()) {
            for (Integer tagId : request.getTagIds()) {
                elderlyTagRelationService.deleteByElderlyIdAndTagId(elderlyId, tagId);
            }
        }
        return Result.success("解绑成功");
    }

    @GetMapping("/elderly/list-by-tag")
    public Result<List<ElderlyWithTagsVO>> getElderlyListByTagId(
            @RequestParam(required = false) Integer tagId,
            @RequestParam(required = false) String keyword) {
        List<Elderly> elderlyList;
        if (tagId != null) {
            List<Integer> elderlyIds = elderlyTagRelationService.getElderlyIdsByTagId(tagId);
            if (elderlyIds.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            elderlyList = elderlyService.lambdaQuery()
                    .in(Elderly::getId, elderlyIds)
                    .like(keyword != null && !keyword.isEmpty(), Elderly::getName, keyword)
                    .list();
        } else {
            elderlyList = elderlyService.lambdaQuery()
                    .like(keyword != null && !keyword.isEmpty(), Elderly::getName, keyword)
                    .list();
        }
        List<ElderlyWithTagsVO> result = new ArrayList<>();
        for (Elderly elderly : elderlyList) {
            ElderlyWithTagsVO vo = new ElderlyWithTagsVO();
            vo.setElderly(elderly);
            List<Integer> tagIds = elderlyTagRelationService.getTagIdsByElderlyId(elderly.getId());
            if (!tagIds.isEmpty()) {
                vo.setTags(elderlyTagService.listByIds(tagIds));
            } else {
                vo.setTags(new ArrayList<>());
            }
            result.add(vo);
        }
        return Result.success(result);
    }
}
