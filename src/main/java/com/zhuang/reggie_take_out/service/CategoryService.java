package com.zhuang.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuang.reggie_take_out.entity.Category;

/**
 * description: CategoryService
 * date: 2022/12/7 16:43
 * author: Zhuang
 * version: 1.0
 */
public interface CategoryService extends IService<Category> {

    //根据ID删除分类
    public void remove(Long ids);
}
