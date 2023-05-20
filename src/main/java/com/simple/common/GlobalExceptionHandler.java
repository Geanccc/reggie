package com.simple.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

//全局异常处理器
//设置：按照注解类型来捕获异常

@Slf4j
@RestControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

     //SQLIntegrityConstraintViolationException.class是账号名重复才会报的异常类
     //这里指定选择该异常类，专门用于处理账号名重复异常
     @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
     public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
          log.error(ex.getMessage());
          //判断异常信息中如果包含"Duplicate entry"的字段
          //"Duplicate entry"是SQLIntegrityConstraintViolationException.class这个异常类中提示的信息
          if (ex.getMessage().contains("Duplicate entry")){
               //将异常信息以空格形式分割
               String[] split = ex.getMessage().split(" ");
               //获取异常信息中包含账号名的字段
               String msg="账号"+split[2]+"已存在";
               return R.error(msg);
          }
          return R.error("未知错误");
     }

     //该分类里，是否关联了菜品或套餐
     @ExceptionHandler(CustomException.class)
     public R<String> exceptionHandler(CustomException ex){
          log.error(ex.getMessage());
          return R.error(ex.getMessage());
     }
}
