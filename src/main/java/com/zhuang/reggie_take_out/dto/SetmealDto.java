package com.zhuang.reggie_take_out.dto;

import com.zhuang.reggie_take_out.entity.Setmeal;
import com.zhuang.reggie_take_out.entity.SetmealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
