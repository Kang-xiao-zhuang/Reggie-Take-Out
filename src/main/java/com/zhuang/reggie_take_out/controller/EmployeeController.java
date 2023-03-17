package com.zhuang.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuang.reggie_take_out.common.R;
import com.zhuang.reggie_take_out.entity.Employee;
import com.zhuang.reggie_take_out.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * description: EmployeeController
 * date: 2022/12/7 12:22
 * author: Zhuang
 * version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private static final String EMPLOYEE = "employee";

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     *
     * @param employee Employee
     * @param request  HttpServletRequest
     * @return Employee
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute(EMPLOYEE, emp.getId());
        return R.success(emp);

    }

    /**
     * 员工退出
     *
     * @param request HttpServletRequest
     * @return String
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清楚Session中保存的当前登录员工的id
        request.getSession().removeAttribute(EMPLOYEE);
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param employee Employee
     * @return String
     */
    @PostMapping
    public R<String> add(@RequestBody Employee employee, HttpServletRequest request) {
        log.info("新增员工，员工信息：{}", employee);
        // 设置初始密码123456，进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获取当前用户id
        Long empId = (Long) request.getSession().getAttribute(EMPLOYEE);

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     *
     * @param page     int
     * @param pageSize int
     * @param name     String
     * @return Page
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee Employee
     * @param request  HttpServletRequest
     * @return String
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request) {
        log.info(employee.toString());

        Long empId = (Long) request.getSession().getAttribute(EMPLOYEE);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     *
     * @param id Long
     * @return Employee
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        /*
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
        */
        return employee == null ? R.error("没有查询到对应员工信息") : R.success(employee);
    }
}
