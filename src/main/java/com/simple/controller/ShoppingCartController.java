package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.common.BaseContext;
import com.simple.common.R;
import com.simple.entity.ShoppingCart;
import com.simple.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
     @Autowired
     private ShoppingCartService shoppingCartService;

     //添加至购物车
     @PostMapping("/add")
     public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
          log.info("购物车数据：{}", shoppingCart);
          //设置用户id，指定当前是哪个用户的购物车数据
          shoppingCart.setUserId(BaseContext.getCurrentId());
          //查询当前菜品或者套餐是否已经在购物车当中
          Long dishId = shoppingCart.getDishId();
          LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

          if (dishId!=null){
               //添加到购物车的为菜品
               queryWrapper.eq(ShoppingCart::getDishId,dishId);
          }else {
               //添加到购物车的为套餐
               queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
          }

          //查询购物车中菜品或套餐数据
          ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
          if (cartServiceOne!=null){
               //如果已经存在，则在原来的基础上加一
               Integer shoppingCartNumber = cartServiceOne.getNumber();
               cartServiceOne.setNumber(shoppingCartNumber+1);
               shoppingCartService.updateById(cartServiceOne);

          }else {
               //如果不存在，则添加到购物车中，默认为一
               shoppingCart.setNumber(1);
               shoppingCart.setCreateTime(LocalDateTime.now());
               shoppingCartService.save(shoppingCart);
               cartServiceOne=shoppingCart;
          }

          return R.success(cartServiceOne);
     }

     //查看购物车
     @GetMapping("/list")
     public R<List<ShoppingCart>> list(){
          log.info("查看购物车");
          LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
          queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

          List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
          return R.success(list);
     }

     //清空购物车
     @DeleteMapping("/clean")
     public R<String> clean(){
          LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
          shoppingCartService.remove(queryWrapper);
          return R.success("清空购物车成功");
     }

}
