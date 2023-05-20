package com.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simple.dto.SetmealDto;
import com.simple.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
     //新增套餐，同时要保持与菜品的关联关系
     public void saveWithDish(SetmealDto setmealDto);

     //套餐回显
     public SetmealDto getByIdWithDish(Long id);

     //保存套餐修改
     public void updateWithDish(SetmealDto setmealDto);
}
