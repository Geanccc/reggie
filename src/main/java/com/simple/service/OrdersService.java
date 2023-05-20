package com.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simple.entity.Orders;

public interface OrdersService extends IService<Orders> {
     public void submit(Orders orders);
}
