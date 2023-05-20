package com.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simple.entity.Category;

public interface CategoryService extends IService<Category> {
     public void remove(Long id);
}
