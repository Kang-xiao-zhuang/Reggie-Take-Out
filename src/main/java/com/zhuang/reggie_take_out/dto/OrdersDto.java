package com.zhuang.reggie_take_out.dto;

import com.zhuang.reggie_take_out.entity.OrderDetail;
import com.zhuang.reggie_take_out.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
