package com.simple.common;

//自定义异常类
//判断该分类里，是否关联了菜品或套餐
public class CustomException extends RuntimeException{
     public CustomException(String message){
          super(message);
     }
}
