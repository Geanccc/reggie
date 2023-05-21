package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.common.R;
import com.simple.dto.SetmealDto;
import com.simple.entity.Category;
import com.simple.entity.Setmeal;
import com.simple.service.CategoryService;
import com.simple.service.SetmealDishService;
import com.simple.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
     @Autowired
     private SetmealService setmealService;
     @Autowired
     private CategoryService categoryService;
     @Autowired
     private SetmealDishService setmealDishService;

     //新增套餐，同时要保持与菜品的关联关系
     @PostMapping
     @CacheEvict(value = "setmealCache",allEntries = true)
     //删除所有套餐缓存
     public R<String> sava(@RequestBody SetmealDto setmealDto){
          log.info("setmeal:{}",setmealDto);
          setmealService.saveWithDish(setmealDto);
          return R.success("新增套餐成功");
     }

     //列表分页查询
     @GetMapping("/page")
     public R<Page> page(int page,int pageSize,String name){
          //构造分页构造器
          Page<Setmeal> page1 = new Page<>(page,pageSize);
          //因为dish实体类中缺少我们需要的属性（分类名），所以要添加一个实际满足我们需求的实体类
          Page<SetmealDto> page2 = new Page<>();

          //构造条件构造器
          LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
          //构造条件
          queryWrapper.eq(name!=null,Setmeal::getName,name);
          //排序条件
          queryWrapper.orderByAsc(Setmeal::getUpdateTime);
          //进行分页查询
          setmealService.page(page1,queryWrapper);

          //对象拷贝
          //形参解释：从哪个开始，拷贝到哪，需要排除的属性
          //page分页对象中的records这个属性里有实际的菜品对象（dish），是一个列表
          //因为我们要对菜品对象中的id进行获取，设置。所以要先设置好才进行拷贝
          BeanUtils.copyProperties(page1,page2,"records");

          //获取实际菜品对象（dish）
          List<Setmeal> records = page1.getRecords();
          //遍历records，每一次遍历都设置好我们的需求，然后装入一个新的list中。重新赋值给dishDtoPage
          List<SetmealDto> list = records.stream().map((item) -> {
               SetmealDto setmealDto = new SetmealDto();
               //拷贝属性
               BeanUtils.copyProperties(item, setmealDto);
               //获取实际对象的id
               Long categoryId = item.getCategoryId();
               //根据id查询分类
               Category category = categoryService.getById(categoryId);
               String categoryName = category.getName();
               setmealDto.setCategoryName(categoryName);
               return setmealDto;
          }).collect(Collectors.toList());//将流中所有元素导入到list中

          //将设置过的实际对象列表（dish）传入到dishDtoPage
          page2.setRecords(list);
          return R.success(page2);
     }

     //删除套餐
     @DeleteMapping()
     @CacheEvict(value = "setmealCache",allEntries = true)
     //删除所有套餐缓存
     public R<String> delete(Long ids){
          setmealService.removeById(ids);
          return R.success("删除套餐成功");
     }

     //修改套餐回显
     @GetMapping("/{id}")
     public R<SetmealDto> getById(@PathVariable Long id){
          SetmealDto setmealDto = setmealService.getByIdWithDish(id);
          return R.success(setmealDto);
     }

     //保存套餐修改
     @PutMapping
     public R<String> updateWithDish(@RequestBody SetmealDto setmealDto){
          setmealService.updateWithDish(setmealDto);
          return R.success("修改套餐成功");
     }

     //套餐启售，停售
     @PostMapping("/status/{status}")
     public R<String> status(@PathVariable Integer status ,Long ids){
          Setmeal setmeal = setmealService.getById(ids);
          setmeal.setStatus(status);
          LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(Setmeal::getId,setmeal.getId());

          setmealService.update(setmeal,queryWrapper);
          return R.success("套餐修改成功");
     }

     //列表显示套餐
     @GetMapping("/list")
     @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status",unless = "#result==null")
     //如果缓存过直接使用缓存，否则执行sql
     public R<List<Setmeal>> list(Setmeal setmeal){
          LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
          queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
          queryWrapper.orderByDesc(Setmeal::getUpdateTime);

          List<Setmeal> list = setmealService.list(queryWrapper);
          return R.success(list);
     }
}
