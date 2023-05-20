package com.simple.controller;

import com.simple.common.R;
import com.simple.entity.Orders;
import com.simple.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
     @Autowired
     private OrdersService ordersService;

     //用户下单
     @PostMapping("/submit")
     public R<String> submit(@RequestBody Orders orders){
          log.info("订单数据:{}",orders);
          ordersService.submit(orders);
          return R.success("下单成功");
     }
}
