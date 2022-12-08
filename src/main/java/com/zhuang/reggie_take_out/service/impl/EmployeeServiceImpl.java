package com.zhuang.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuang.reggie_take_out.entity.Employee;
import com.zhuang.reggie_take_out.mapper.EmployeeMapper;
import com.zhuang.reggie_take_out.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * description: EmployeeServiceImpl
 * date: 2022/12/7 12:20
 * author: Zhuang
 * version: 1.0
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
