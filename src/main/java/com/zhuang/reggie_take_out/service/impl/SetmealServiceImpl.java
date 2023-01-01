package com.zhuang.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuang.reggie_take_out.common.CustomException;
import com.zhuang.reggie_take_out.dto.SetmealDto;
import com.zhuang.reggie_take_out.entity.Dish;
import com.zhuang.reggie_take_out.entity.Setmeal;
import com.zhuang.reggie_take_out.entity.SetmealDish;
import com.zhuang.reggie_take_out.mapper.SetmealMapper;
import com.zhuang.reggie_take_out.service.DishService;
import com.zhuang.reggie_take_out.service.SetmealDishService;
import com.zhuang.reggie_take_out.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * description: SetmealServiceImpl
 * date: 2022/12/8 11:43
 * author: Zhuang
 * version: 1.0
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        //获取菜品信息
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        // 保存套餐和菜品的关联信息，操作setmeal_dish操作
        setmealDishService.saveBatch(dishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     *
     * @param ids List<Long>
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        // 不能删除，抛出一个业务异常
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("套餐正在售卖中，不能删除");
        }

        // 如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);
        // 删除关系表中的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }

    @Override
    @Transactional
    public SetmealDto getByIdWithDishes(Long id) {
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);
        //构造条件查询器，根据套餐id查询对应的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithDishes(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        //获取菜品套餐信息
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        //先删除套餐id关联的菜品，在添加新值进去
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //添加新值，为每项菜品绑定套餐id
        dishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishes);
    }

    @Override
    @Transactional
    public void deleteWithDishes(Long id) {
        this.removeById(id);
        //删除关联的套餐菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        setmealDishService.remove(queryWrapper);
    }

    @Override
    @Transactional
    public void updateWithStatus(Setmeal setmeal) {
        //停售套餐后，对应的套餐菜品应该更改状态,起售套餐应判断关联的菜品停售
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        if (setmeal.getStatus() == 0) {
            //如果套餐停售，套餐菜品也停售
            queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
            SetmealDish setmealDish = new SetmealDish();
            //1为停售，0为起售
            setmeal.setStatus(0);
            setmealDishService.update(setmealDish, queryWrapper);
            this.updateById(setmeal);
            return;
        }//如果套餐起售，判断套餐中的菜品是否停售
        //根据套餐id获取对应的菜品id
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> list = setmealDishService.list();
        //遍历list，拿到每一个菜品id查询dish表，能查到停售状态的数据则不能起售
        for (SetmealDish setmealDish : list) {
            LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Dish::getStatus, 0).eq(Dish::getId, setmealDish.getDishId());
            if (dishService.count(queryWrapper1) > 0) {
                throw new CustomException("套餐存在停售的菜品。无法起售");
            }
        }
        this.updateById(setmeal);
    }
}
