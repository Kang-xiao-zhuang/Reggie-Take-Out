package com.zhuang.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuang.reggie_take_out.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     *
     * @param orders
     */
    public void submit(Orders orders);
}
