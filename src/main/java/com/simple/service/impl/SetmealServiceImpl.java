package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.dto.SetmealDto;
import com.simple.entity.Setmeal;
import com.simple.entity.SetmealDish;
import com.simple.mapper.SetmealMapper;
import com.simple.service.SetmealDishService;
import com.simple.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
     @Autowired
     private SetmealDishService setmealDishService;

     @Override
     @Transactional
     public void saveWithDish(SetmealDto setmealDto) {
          //保存套餐基本信息
          this.save(setmealDto);
          //取出前端传过来的json里的SetmealDishes（套餐菜品列表）
          //遍历套餐菜品列表，给他们设置好，归属的套餐id
          List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
          setmealDishes.stream().map((item)->{
               item.setSetmealId(setmealDto.getId());
               return item;
          }).collect(Collectors.toList());

          //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
          setmealDishService.saveBatch(setmealDishes);
     }

     //根据id回显套餐
     @Override
     public SetmealDto getByIdWithDish(Long id) {
          Setmeal setmeal = this.getById(id);
          SetmealDto setmealDto = new SetmealDto();
          BeanUtils.copyProperties(setmeal,setmealDto);

          LambdaQueryWrapper<SetmealDish> queryWrapper= new LambdaQueryWrapper<>();
          queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
          List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

          setmealDto.setSetmealDishes(setmealDishes);

          return setmealDto;
     }

     //保存套餐修改
     @Override
     @Transactional
     public void updateWithDish(SetmealDto setmealDto) {
          this.updateById(setmealDto);

          LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
          setmealDishService.remove(queryWrapper);

          List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
          List<SetmealDish> list = setmealDishes.stream().map((item) -> {
               item.setSetmealId(setmealDto.getId());
               return item;
          }).collect((Collectors.toList()));

          setmealDishService.saveBatch(list);
     }


}
