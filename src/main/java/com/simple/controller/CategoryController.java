package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.common.R;
import com.simple.entity.Category;
import com.simple.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
     @Autowired
     private CategoryService categoryService;

     //新增分类
     @PostMapping
     public R<String> save(@RequestBody Category category){
          log.info("category:{}",category);
          categoryService.save(category);
          return R.success("新增分类成功");
     }

     //分页查询
     @GetMapping("/page")
     public R<Page> page(int page,int pageSize){
          //构造分页构造器
          Page<Category> page1 = new Page<>(page,pageSize);
          //构造条件构造器
          LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
          //添加排序条件，根据sort进行排序
          queryWrapper.orderByAsc(Category::getSort);

          //进行分页查询
          categoryService.page(page1,queryWrapper);
          return R.success(page1);

     }

     //根据id删除分类
     @DeleteMapping
     public R<String> delete(Long ids){
          log.info("删除分类，id为{}",ids);
          //重写了删除方法，删除前判断该分类是否关联了菜品或套餐
          categoryService.remove(ids);
          return R.success("分类信息删除成功");
     }

     //修改分类信息
     @PutMapping
     public  R<String> update(@RequestBody Category category) {
          log.info("修改分类：{}", category);
          categoryService.updateById(category);
          return R.success("分类修改成功");
     }

     //根据条件查询分类数据
     @GetMapping("/list")
     public R<List<Category>> list(Category category){
          LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());

          queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);

          List<Category> list = categoryService.list(queryWrapper);
          return R.success(list);
     }
}
