package com.zhuang.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuang.reggie_take_out.common.CustomException;
import com.zhuang.reggie_take_out.entity.Category;
import com.zhuang.reggie_take_out.entity.Dish;
import com.zhuang.reggie_take_out.entity.Setmeal;
import com.zhuang.reggie_take_out.mapper.CategoryMapper;
import com.zhuang.reggie_take_out.service.CategoryService;
import com.zhuang.reggie_take_out.service.DishService;
import com.zhuang.reggie_take_out.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description: CategoryServiceImpl
 * date: 2022/12/7 12:20
 * author: Zhuang
 * version: 1.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param ids Long
     */
    @Override
    public void remove(Long ids) {
        //添加查询条件，根据分类id进行查询菜品数据
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //如果已经关联，抛出一个业务异常
        if (count1 > 0) {
            throw new CustomException("当前分类下关联了菜品，不能删除");//已经关联菜品，抛出一个业务异常
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除");//已经关联套餐，抛出一个业务异常
        }

        //正常删除分类
        super.removeById(ids);
    }
}
