package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.simple.common.BaseContext;
import com.simple.common.R;
import com.simple.entity.AddressBook;
import com.simple.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
     @Autowired
     private AddressBookService addressBookService;

     /**
      * 新增
      */
     @PostMapping
     public R<AddressBook> save(@RequestBody AddressBook addressBook){
          addressBook.setUserId(BaseContext.getCurrentId());
          log.info("addressBook:{}", addressBook);

          addressBookService.save(addressBook);
          return R.success(addressBook);
     }


     /**
      * 设置默认地址
      */
     @PutMapping("default")
     public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
          log.info("addressBook:{}", addressBook);
          LambdaUpdateWrapper<AddressBook> wrapper= new LambdaUpdateWrapper<>();
          wrapper.eq(AddressBook::getUserId,addressBook.getUserId());
          wrapper.set(AddressBook::getIsDefault,0);
          addressBookService.updateById(addressBook);

          addressBook.setIsDefault(1);
          addressBookService.updateById(addressBook);
          return R.success(addressBook);
     }

     /**
      * 根据id查询地址
      */
     @GetMapping("/{id}")
     public R get(@PathVariable Long id){
          AddressBook addressBook = addressBookService.getById(id);
          if (addressBook!=null){
               return R.success(addressBook);
          }else {
               return R.success("没有找到该用户");
          }
     }

     /**
      * 查询默认地址
      */
     @GetMapping("default")
     public R<AddressBook> getDefault(){
          LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
          queryWrapper.eq(AddressBook::getIsDefault,1);

          AddressBook addressBook = addressBookService.getOne(queryWrapper);

          if (addressBook==null){
               return R.error("没有找到该用户默认地址");
          }else {
               return R.success(addressBook);
          }
     }

     /**
      * 查询指定用户的全部地址
      */
     @GetMapping("/list")
     public R<List<AddressBook>> getList(AddressBook addressBook){
          addressBook.setUserId(BaseContext.getCurrentId());
          log.info("addressBook:{}", addressBook);

          LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(addressBook.getUserId()!=null,AddressBook::getUserId,addressBook.getUserId());
          queryWrapper.orderByDesc(AddressBook::getUpdateTime);

          List<AddressBook> list = addressBookService.list(queryWrapper);
          return R.success(list);
     }
}
