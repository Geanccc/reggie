package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.entity.Dish;
import com.simple.dto.DishDto;
import com.simple.entity.DishFlavor;
import com.simple.mapper.DishMapper;
import com.simple.service.DishFlavorService;
import com.simple.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
     @Autowired
     public DishFlavorService dishFlavorService;

     @Override
     @Transactional
     //新增菜品保存
     public void savaWithFlavor(DishDto dishDto) {
          //保存菜品基本信息到菜品表dish
          this.save(dishDto);

          //保存菜品口味到菜品数据表dish_flavor
          //取出json中菜品的id值
          Long dishid = dishDto.getId();
          //菜品口味
          //前端穿过来的json数据中包含了口味信息，是一个列表
          //遍历口味列表，将菜品的id值一个一个赋值给每一个口味
          List<DishFlavor> flavors = dishDto.getFlavors();

          flavors = flavors.stream().map((item) -> {
               item.setDishId(dishid);
               return item;
          }).collect(Collectors.toList());
          //dishFlavorService.saveBatch(dishDto.getFlavors());
          //保存菜品口味到菜品数据表dish_flavor
          //使用批量保存
          dishFlavorService.saveBatch(flavors);
     }

     @Override
     //菜品回显
     public DishDto getByIdWithFlavor(Long id) {
          //查询菜品基本信息
          Dish dish = this.getById(id);

          DishDto dishDto = new DishDto();
          BeanUtils.copyProperties(dish,dishDto);

          //查询菜品口味信息
          LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(DishFlavor::getDishId,dish.getId());
          List<DishFlavor> list = dishFlavorService.list(queryWrapper);

          dishDto.setFlavors(list);

          return dishDto;
     }

     @Override
     @Transactional
     //修改菜品
     public void updateWithFlavor(DishDto dishDto) {
          //更新dish表基本信息
          this.updateById(dishDto);
          //更新dish_flavor表信息delete操作
          LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
          dishFlavorService.remove(queryWrapper);

          //更新dish_flavor表信息insert操作
          //遍历口味列表，将菜品的id值一个一个赋值给每一个口味
          List<DishFlavor> flavors = dishDto.getFlavors();
          flavors=flavors.stream().map((item)->{
               item.setDishId(dishDto.getId());
               return item;
          }).collect(Collectors.toList());

          dishFlavorService.saveBatch(flavors);
     }
}
