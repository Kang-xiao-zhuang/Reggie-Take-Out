package com.zhuang.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuang.reggie_take_out.entity.ShoppingCart;
import com.zhuang.reggie_take_out.mapper.ShoppingCartMapper;
import com.zhuang.reggie_take_out.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
