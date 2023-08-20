package com.zhuang.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuang.reggie_take_out.common.R;
import com.zhuang.reggie_take_out.dto.SetmealDto;
import com.zhuang.reggie_take_out.entity.Category;
import com.zhuang.reggie_take_out.entity.Setmeal;
import com.zhuang.reggie_take_out.service.CategoryService;
import com.zhuang.reggie_take_out.service.SetmealDishService;
import com.zhuang.reggie_take_out.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * description: SetmealController
 * date: 2022/12/7 22:07
 * author: Zhuang
 * version: 1.0
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐
     *
     * @param setmealDto SetmealDto
     * @return String
     */
    @PostMapping
    public R<String> add(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page     int
     * @param pageSize int
     * @param name     String
     * @return Page
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 根据套餐id获取对应信息回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Cacheable(value = "setmealCache", key = "'setmeal'+#id")
    public R<SetmealDto> get(@PathVariable long id) {
        //查询setmeal表和套餐对应的菜品
        SetmealDto setmealDto = setmealService.getByIdWithDishes(id);
        return R.success(setmealDto);
    }

    /**
     * 更新套餐数据
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        //更新setmeal表和对应的setmealDish数据
        setmealService.updateWithDishes(setmealDto);
        return R.success("更新成功");
    }

    /**
     * 删除套餐操作
     *
     * @param ids List
     * @return R<String>
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true, beforeInvocation = false)
    public R<String> delete(@RequestParam List<Long> ids) {
        for (Long id : ids) {
            //删除套餐信息和关联的套餐菜品信息
            setmealService.deleteWithDishes(id);
        }
        return R.success("删除成功");
    }

    /**
     * 更新套餐状态
     *
     * @param ids    List
     * @param status Integer
     * @return R
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> updateStatus(@RequestParam List<Long> ids, @PathVariable Integer status) {
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            //如果当前菜品关联了套餐则不能更改状态
            setmealService.updateWithStatus(setmeal);
        }
        if (status == 1) {
            return R.success("已起售");
        }
        return R.success("已停售");
    }

    /**
     * 根据条件查询套餐数据
     *
     * @param setmeal Setmeal
     * @return List<Setmeal>
     */
    @GetMapping("/list")
    //@Cacheable(value = "setmealCache", key = "#setmeal.categoryId+'_'+#setmeal.status")
    //清除setmealCache名称下,所有的缓存数据
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
}
