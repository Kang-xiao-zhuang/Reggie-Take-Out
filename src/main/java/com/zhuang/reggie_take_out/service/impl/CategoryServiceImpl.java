package com.zhuang.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuang.reggie_take_out.entity.Category;
import com.zhuang.reggie_take_out.entity.Employee;
import com.zhuang.reggie_take_out.mapper.CategoryMapper;
import com.zhuang.reggie_take_out.mapper.EmployeeMapper;
import com.zhuang.reggie_take_out.service.CategoryService;
import com.zhuang.reggie_take_out.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * description: CategoryServiceImpl
 * date: 2022/12/7 12:20
 * author: Zhuang
 * version: 1.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
