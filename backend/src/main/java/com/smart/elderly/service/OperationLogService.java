package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.OperationLog;
import com.smart.elderly.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {
    
    public Page<OperationLog> getLogPage(Integer pageNum, Integer pageSize, 
                                          String username, String operation,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (StringUtils.hasText(operation)) {
            wrapper.eq(OperationLog::getOperation, operation);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getOperationTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getOperationTime, endTime);
        }
        
        wrapper.orderByDesc(OperationLog::getOperationTime);
        return page(page, wrapper);
    }
}
