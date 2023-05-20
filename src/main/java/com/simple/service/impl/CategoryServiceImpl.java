package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.common.CustomException;
import com.simple.entity.Category;
import com.simple.entity.Dish;
import com.simple.entity.Setmeal;
import com.simple.mapper.CategoryMapper;
import com.simple.service.CategoryService;
import com.simple.service.DishService;
import com.simple.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
     @Autowired
     public DishService dishService;

     @Autowired
     public SetmealService setmealService;

     @Override
     public void remove(Long id) {
          //添加查询条件，根据分类id进行查询
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(Dish::getCategoryId,id);
          //根据条数查找
          int count1 = dishService.count(queryWrapper);
          //查询当前分类是否关联菜品,如果已经关联，抛出业务异常
          if (count1>0){
               //已经关联菜品，抛出业务异常
               throw new CustomException("已经关联菜品，不能删除");
          }

          //添加查询条件，根据分类id进行查询
          LambdaQueryWrapper<Setmeal> queryWrapper2 = new LambdaQueryWrapper<>();
          queryWrapper2.eq(Setmeal::getCategoryId,id);
          //根据条数查找
          int count2 = setmealService.count(queryWrapper2);
          //查询当前分类是否关联了套餐，如果已经关联，抛出业务异常
          if (count2>0){
               //已经关联套餐，抛出业务异常
               throw new CustomException("已经关联套餐，不能删除");
          }

          //正常删除分类
          super.removeById(id);
     }
}
