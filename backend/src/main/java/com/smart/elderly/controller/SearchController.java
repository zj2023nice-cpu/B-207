package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.service.SearchService;
import com.smart.elderly.vo.SearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public Result<SearchResultVO> search(
            @RequestParam String keyword,
            @RequestParam(required = false) List<String> modules,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        SearchResultVO result = searchService.search(keyword, modules, limit);
        return Result.success(result);
    }
}
