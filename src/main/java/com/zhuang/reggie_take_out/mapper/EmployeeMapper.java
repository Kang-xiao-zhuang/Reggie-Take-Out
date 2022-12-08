package com.zhuang.reggie_take_out.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuang.reggie_take_out.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * description: EmployeeMapper
 * date: 2022/12/7 12:19
 * author: Zhuang
 * version: 1.0
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
