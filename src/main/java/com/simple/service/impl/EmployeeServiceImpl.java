package com.simple.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simple.entity.Employee;
import com.simple.mapper.EmployeeMapper;
import com.simple.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>  implements EmployeeService {
}
