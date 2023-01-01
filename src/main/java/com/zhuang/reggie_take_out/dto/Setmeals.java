package com.zhuang.reggie_take_out.dto;

import com.zhuang.reggie_take_out.entity.SetmealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Setmeals extends SetmealDish {
    private String image;
    private String description;
}
