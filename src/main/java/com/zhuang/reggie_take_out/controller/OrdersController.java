package com.zhuang.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuang.reggie_take_out.common.BaseContext;
import com.zhuang.reggie_take_out.common.R;
import com.zhuang.reggie_take_out.dto.OrdersDto;
import com.zhuang.reggie_take_out.entity.OrderDetail;
import com.zhuang.reggie_take_out.entity.Orders;
import com.zhuang.reggie_take_out.entity.ShoppingCart;
import com.zhuang.reggie_take_out.service.OrderDetailService;
import com.zhuang.reggie_take_out.service.OrdersService;
import com.zhuang.reggie_take_out.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 订单页面分页查询
     *
     * @param page      int
     * @param pageSize  int
     * @param number    Long
     * @param beginTime String
     * @param endTime   String
     * @return R<Page < Orders>>
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, Long number, String beginTime, String endTime) {

        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件(订单号,时间范围)

        if (number != null) {
            queryWrapper.eq(Orders::getId, number);
        }
        if (beginTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime beginTime1 = LocalDateTime.parse(beginTime, formatter);
            LocalDateTime endTime1 = LocalDateTime.parse(endTime, formatter);
            queryWrapper.between(Orders::getOrderTime, beginTime1, endTime1);
        }
        //添加排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //执行查询
        ordersService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 提交订单
     *
     * @param orders Orders
     * @return R<Orders>
     */
    @PostMapping("/submit")
    public R<Orders> submit(@RequestBody Orders orders) {
        log.info("订单信息为:" + orders);
        ordersService.submit(orders);
        return R.success(orders);
    }

    /**
     * 移动端获取用户订单
     *
     * @param page     int
     * @param pageSize int
     * @return R<Page < OrdersDto>>
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        //添加排序规则
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据用户id查询订单
        lambdaQueryWrapper.eq(Orders::getUserId, userId);
        lambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        //执行sql
        ordersService.page(pageInfo, lambdaQueryWrapper);
        //将orders数据复制到orderdto中
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        //根据用户id查询到订单id
        List<Orders> list = ordersService.list(lambdaQueryWrapper);
        List<Long> collect = list.stream().map(Orders::getId).collect(Collectors.toList());
        //根据订单id查询订单菜品信息
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderDetail::getOrderId, collect);
        List<OrderDetail> list1 = orderDetailService.list(queryWrapper);
        //创建orderDto集合,存储订单菜品信息
        List<OrdersDto> ordersDtoList = new ArrayList<>();
        //循环遍历根据用户id查询出来的订单表
        for (Orders orders : list) {
            //新建一个dto用于存储订单信息
            OrdersDto ordersDto = new OrdersDto();
            //再将订单信息复制到dto中
            BeanUtils.copyProperties(orders, ordersDto);
            //获得订单id
            Long ordersid = ordersDto.getId();
            //根据订单id从订单关系表中筛选出订单
            List<OrderDetail> collect1 = list1.stream().filter(orderDetail -> ordersid.equals(orderDetail.getOrderId())).collect(Collectors.toList());
            //再将其设置到dto的订单关系集合中
            ordersDto.setOrderDetails(collect1);
            //将这个dto存放在list集合中
            ordersDtoList.add(ordersDto);
        }
        //再将这个dtolist集合,封装到page中,返回
        dtoPage.setRecords(ordersDtoList);
        return R.success(dtoPage);
    }

    /**
     * 更改订单状态为...
     *
     * @param orders Orders
     * @return R<Orders>
     */
    @PutMapping
    public R<Orders> putstatus(@RequestBody Orders orders) {
        log.info("更改订单信息为:" + orders);
        //获取订单id
        Long id = orders.getId();
        //获取订单信息
        Orders byId = ordersService.getById(id);
        //设置订单状态
        byId.setStatus(orders.getStatus());
        //执行更新操作
        log.info("订单信息为:" + byId);
        ordersService.updateById(byId);
        return R.success(byId);
    }

    //运行时有页面逻辑问题

    /**
     * 再来一单
     *
     * @param orders Orders
     * @return R<String>
     */
    @PostMapping("/again")
    @CacheEvict(value = "shoppingCache", allEntries = true)
    public R<String> again(@RequestBody Orders orders) {
        Long ordersId = orders.getId();
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderDetail::getOrderId, ordersId);
        List<OrderDetail> dishlist = orderDetailService.list(lambdaQueryWrapper);
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : dishlist) {
            ShoppingCart shoppingCart = new ShoppingCart();
//            shoppingCart.setUserId(userId);
//            if(orderDetail.getDishId() != null){
//                Integer number = orderDetail.getNumber();
//                shoppingCart.setNumber(number+1);
//                shoppingCartService.updateById(shoppingCart);
//                continue;
//            }
//            if(orderDetail.getSetmealId() != null){
//                Integer number = orderDetail.getNumber();
//                shoppingCart.setNumber(number+1);
//                shoppingCartService.updateById(shoppingCart);
//                continue;
//            }
//            BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
//            shoppingCartList.add(shoppingCart);
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getDishId, orderDetail.getDishId());
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            ShoppingCart one = shoppingCartService.getOne(queryWrapper);

            if (one != null) {
                Integer number = one.getNumber();
                one.setNumber(number + 1);
                shoppingCartService.updateById(one);
                continue;
            } else {
                LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(ShoppingCart::getSetmealId, orderDetail.getSetmealId());
                queryWrapper1.eq(ShoppingCart::getUserId, userId);
                ShoppingCart one1 = shoppingCartService.getOne(queryWrapper1);

                if (one1 != null) {
                    Integer number = one1.getNumber();
                    one1.setNumber(number + 1);
                    shoppingCartService.updateById(one1);
                    continue;
                } else {
                    shoppingCart.setUserId(userId);
                    shoppingCart.setNumber(orderDetail.getNumber());
                    shoppingCart.setAmount(orderDetail.getAmount());
                    shoppingCart.setDishId(orderDetail.getDishId());
                    shoppingCart.setSetmealId(orderDetail.getSetmealId());
                    shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    shoppingCart.setName(orderDetail.getName());
                    shoppingCart.setImage(orderDetail.getImage());
                    shoppingCartList.add(shoppingCart);
                }
            }
        }
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("执行成功!");
    }
}