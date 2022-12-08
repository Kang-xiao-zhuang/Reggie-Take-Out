package com.zhuang.reggie_take_out.dto;

import com.zhuang.reggie_take_out.entity.Dish;
import com.zhuang.reggie_take_out.entity.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
