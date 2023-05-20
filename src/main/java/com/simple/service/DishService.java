package com.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simple.entity.Dish;
import com.simple.dto.DishDto;

public interface DishService extends IService<Dish> {
     //新增菜品保存
     public void savaWithFlavor(DishDto dishDto);

     //菜品回显
     public DishDto getByIdWithFlavor(Long id);

     //修改菜品
     public void updateWithFlavor(DishDto dishDto);
}
