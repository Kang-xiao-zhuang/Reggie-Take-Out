package com.zhuang.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuang.reggie_take_out.dto.SetmealDto;
import com.zhuang.reggie_take_out.entity.Setmeal;

import java.util.List;

/**
 * description: SetmealService
 * date: 2022/12/8 11:41
 * author: Zhuang
 * version: 1.0
 */
public interface SetmealService extends IService<Setmeal>{

    public void saveWithDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);
}
