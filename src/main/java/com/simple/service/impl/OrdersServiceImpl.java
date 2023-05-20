package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.common.BaseContext;
import com.simple.common.CustomException;
import com.simple.entity.*;
import com.simple.mapper.OrdersMapper;
import com.simple.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

     @Autowired
     private ShoppingCartService shoppingcartService;

     @Autowired
     private UserService userService;

     @Autowired
     private AddressBookService addressBookService;

     @Autowired
     private OrderDetailService orderDetailService;

     @Override
     @Transactional
     public void submit(Orders orders) {
          //获取当前用户id
          Long currentId = BaseContext.getCurrentId();
          //查询当前用户的购物车数据
          LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(ShoppingCart::getUserId,currentId);
          List<ShoppingCart> list = shoppingcartService.list(queryWrapper);

          if (list==null||list.size()==0){
               throw new CustomException("购物车为空，不能下单");
          }
          //查询用户数据
          User user = userService.getById(currentId);
          //查询地址数据
          Long addressBookId = orders.getAddressBookId();
          AddressBook addressBook = addressBookService.getById(addressBookId);
          if(addressBook==null){
               throw new CustomException("地址有误，不能下单");
          }
          //随机生成订单号
          long orderId = IdWorker.getId();//订单号

          //用于计算购物车总金额
          AtomicInteger amount=new AtomicInteger(0);

          //遍历购物车
          //复制一些属性到OrderDetail（订单明细表）中
          List<OrderDetail> orderDetails=list.stream().map((item)->{
               OrderDetail orderDetail = new OrderDetail();
               orderDetail.setOrderId(orderId);
               orderDetail.setNumber(item.getNumber());
               orderDetail.setDishFlavor(item.getDishFlavor());
               orderDetail.setDishId(item.getDishId());
               orderDetail.setSetmealId(item.getSetmealId());
               orderDetail.setName(item.getName());
               orderDetail.setImage(item.getImage());
               orderDetail.setAmount(item.getAmount());
               amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
               return orderDetail;
          }).collect(Collectors.toList());
          //向订单表中插入一条数据
          orders.setNumber(String.valueOf(orderId));
          orders.setId(orderId);
          orders.setOrderTime(LocalDateTime.now());
          orders.setCheckoutTime(LocalDateTime.now());
          orders.setStatus(2);
          orders.setAmount(new BigDecimal(amount.get()));//计算总金额
          orders.setUserId(currentId);
          orders.setUserName(user.getName());
          orders.setConsignee(addressBook.getConsignee());
          orders.setPhone(addressBook.getPhone());
          orders.setAddress((addressBook.getProvinceName()==null?"":addressBook.getProvinceName())
                  +(addressBook.getCityName()==null?"":addressBook.getCityName())
                  +(addressBook.getDistrictName()==null?"":addressBook.getDistrictName())
                  +(addressBook.getDetail()==null?"":addressBook.getDetail()));
          this.save(orders);
          //向订单明细表中插入多条数据
          orderDetailService.saveBatch(orderDetails);
          //清空购物车数据
          shoppingcartService.remove(queryWrapper);

     }
}
