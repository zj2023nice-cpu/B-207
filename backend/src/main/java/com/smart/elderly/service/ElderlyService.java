package com.smart.elderly.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.mapper.ElderlyMapper;
import org.springframework.stereotype.Service;

@Service
public class ElderlyService extends ServiceImpl<ElderlyMapper, Elderly> {
}
