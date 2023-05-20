package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.common.R;
import com.simple.entity.Category;
import com.simple.entity.Dish;
import com.simple.dto.DishDto;
import com.simple.entity.DishFlavor;
import com.simple.service.CategoryService;
import com.simple.service.DishFlavorService;
import com.simple.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
     @Autowired
     public DishService dishService;

     @Autowired
     public DishFlavorService dishFlavorService;

     @Autowired
     public CategoryService categoryService;

     //新增菜品
     @PostMapping
     public R<String> sava(@RequestBody DishDto dishDto){
          //因为前端传过来的json数据中包含菜品口味列表信息，所以需要自定义方法
          //dishDto这个数据传输对象，里面包含了菜品和菜品口味
          dishService.savaWithFlavor(dishDto);
          return R.success("新增菜品成功");
     }

     //菜品分页查询
     @GetMapping("/page")
     public R<Page> page(int page,int pageSize,String name){
          //构造分页构造器
          Page<Dish> dishPage = new Page<>(page,pageSize);
          //因为dish实体类中缺少我们需要的属性（分类名），所以要添加一个实际满足我们需求的实体类
          Page<DishDto> dishDtoPage = new Page<>();

          //构造条件构造器
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          //构造条件
          queryWrapper.like(name!=null,Dish::getName,name);
          //排序条件
          queryWrapper.orderByDesc(Dish::getUpdateTime);
          //进行分页查询
          dishService.page(dishPage);

          //对象拷贝
          //形参解释：从哪个开始，拷贝到哪，需要排除的属性
          //page分页对象中的records这个属性里有实际的菜品对象（dish），是一个列表
          //因为我们要对菜品对象中的id进行获取，设置。所以要先设置好才进行拷贝
          BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

          //获取实际菜品对象（dish）
          List<Dish> records = dishPage.getRecords();
          //遍历records，每一次遍历都设置好我们的需求，然后装入一个新的list中。重新赋值给dishDtoPage
          List<DishDto> list = records.stream().map((item)->{
               DishDto dishDto = new DishDto();
               //拷贝属性
               BeanUtils.copyProperties(item,dishDto);
               //获取实际对象的id
               Long categoryId  = item.getCategoryId();
               //根据id查询分类
               Category category = categoryService.getById(categoryId);
               if (category!=null){
                    String name1 = category.getName();
                    dishDto.setCategoryName(name1);
               }
               return dishDto;
          }).collect(Collectors.toList());//将流中所有元素导入到list中

          //将设置过的实际对象列表（dish）传入到dishDtoPage
          dishDtoPage.setRecords(list);

          return R.success(dishDtoPage);

     }

     //菜品回显
     @GetMapping("/{id}")
     public R<DishDto> getById(@PathVariable Long id){
          DishDto dishDto = dishService.getByIdWithFlavor(id);
          return R.success(dishDto);
     }

     //修改菜品
     @PutMapping
     public R<String> update(@RequestBody DishDto dishDto){
          dishService.updateWithFlavor(dishDto);
          return R.success("修改菜品成功");
     }

     //根据条件查询对应菜品数据
     @GetMapping("/list")
     public R<List<DishDto>> list(Dish dish){
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
          queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
          queryWrapper.eq(Dish::getStatus,1);

          //获取菜品列表
          List<Dish> list = dishService.list(queryWrapper);

          //遍历菜品列表
          List<DishDto> dishDtoList = list.stream().map((item)->{
               //创建一个菜品Dto类，里面包含了菜品，归属分类名称，菜品口味
               DishDto dishDto = new DishDto();
               //拷贝属性
               BeanUtils.copyProperties(item,dishDto);
               //获取实际对象的归属分类id
               Long categoryId  = item.getCategoryId();
               //根据id查询分类
               Category category = categoryService.getById(categoryId);
               if (category!=null){
                    String name1 = category.getName();
                    //给菜品设置分类名称
                    dishDto.setCategoryName(name1);
               }
               //获取菜品id
               Long id = item.getId();
               //查询菜品口味表中，归属菜品id的口味
               LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
               lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
               List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
               //dto类设置菜品口味信息
               dishDto.setFlavors(dishFlavorList);
               //返回菜品Dto类
               return dishDto;
          }).collect(Collectors.toList());//将流中所有元素导入到list中

          return R.success(dishDtoList);
     }

}
