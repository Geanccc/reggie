package com.simple.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.common.R;
import com.simple.entity.Employee;
import com.simple.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
     @Autowired
     private EmployeeService employeeService;

     //员工登陆
     @PostMapping("/login")
     public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
          //1、将页面提交的密码进行md5加密处理
          //   因为数据库中的密码已经加密过，所以要将用户填写的密码加密处理后才能跟数据库中的比对
          String password = employee.getPassword();
          password= DigestUtils.md5DigestAsHex(password.getBytes());

          //2、根据页面提交的用户名来查数据库
          LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
          wrapper.eq(Employee::getUsername,employee.getUsername());
          Employee e = employeeService.getOne(wrapper);

          //3、如果没有查询到则返回失败结果
          if(e==null){
               return R.error("登陆失败");
          }

          //4、比对密码，如果不一致则返回失败结果
          if(!e.getPassword().equals(password)){
               return R.error("密码失败");
          }

          //5、查看员工状态，如果已禁用状态，则返回员工已禁用结果
          if(e.getStatus()==0){
               return R.error("账号已被禁用");
          }

          //6、登录成功，将用户id存入Session并返回成功结果
          request.getSession().setAttribute("Employee",e.getId());
          return R.success(e);
     }

     //员工退出
     @PostMapping("/logout")
     public R<String> logout(HttpServletRequest request){
          request.getSession().removeAttribute("Employee");
          return R.success("退出成功");
     }

     //新增员工
     @PostMapping
     public R<String> sava(HttpServletRequest request, @RequestBody Employee employee){
          log.info("新增员工，员工信息：{}",employee.toString());
          //设置初始密码，需要进行md5加密处理
          employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
          //设置创建，修改时间
          employee.setCreateTime(LocalDateTime.now());
          employee.setUpdateTime((LocalDateTime.now()));

          //获取正在登陆的用户id
          Long empId = (Long) request.getSession().getAttribute("Employee");
          //设置创建,修改人
          employee.setCreateUser(empId);
          employee.setUpdateUser(empId);

          //调用保存方法
          employeeService.save(employee);
          return R.success("新增员工成功");

          //数据库表中账号名具有唯一性
          //如果新增员工中账号名重复，使用全局异常处理器。拦截到该异常后作出处理
     }

     //列表分页查询
     @GetMapping("/page")
     public R<Page> page(int page,int pageSize,String name){
          log.info("page={},pageSize={},name={}", page, pageSize, name);

          //构造分页构造器
          Page pageInfo = new Page(page, pageSize);

          //构造条件构造器
          LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();

          //添加过滤条件
          //如果页面搜索框中没有数据，则跳过此条件。反之根据条件进行模糊查询
          //这里like方法有三个参数：
          // 第一个参数：该参数是一个布尔类型，只有该参数是true时，才将like条件拼接到sql中；本例中，当形参name不为空时，则拼接name字段的like查询条件；
          // 第二个参数：该参数是数据库中的字段名；
          // 第三个参数：该参数值字段值；
          wrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);

          //添加排序条件
          wrapper.orderByDesc(Employee::getUpdateTime);

          employeeService.page(pageInfo,wrapper);

          return R.success(pageInfo);

     }

     //保存按钮，保存员工信息
     @PutMapping
     public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
          log.info(employee.toString());

          //获取当前账号id，返回的是Object类，强转成Long类型
          Long empId = (Long) request.getSession().getAttribute("Employee");

          employee.setUpdateUser(empId);
          employee.setUpdateTime(LocalDateTime.now());
          employeeService.updateById(employee);

          return R.success("员工信息修改成功");

//          代码修复:
//          通过观察控制台输出的SQL发现页面传递过来的员工id的值和数据库中的id值不一致，这是怎么回事呢?
//          分页查询时(我们)服务端响应给页面的数据中id的值为19位数字，类型为long。这时是正确的
//          页面中js处理long型数字只能精确到前16位，所以最终通过ajax请求提交给服务端的时候id就改变了
//          前面我们已经发现了问题的原因，即js对long型数据进行处理时丢失精度，导致提交的id和数据库中的id不一致。
//          如何解决这个问题?
//          我们可以在服务端给页面响应json数据时进行处理，将long型数据统一转为String字符串。

//          具体实现步骤:
//          1：文件名：JacksonObjectMapper.java
//          提供对象转换器JacksonobjectMapper，基于Jackson进行Java对象到json数据的转换（资料中已经提供，直接复制到项目中使用)
//          2:
//          在WebMvcConfig配置类中扩展Spring mvc的消息转换器，在此消息转换器中使用提供的对象转换器进行Java对象到json数据的转换
     }

     //根据id修改员工信息，显示员工信息
     @GetMapping("/{id}")
     public R<Employee> getById(@PathVariable String id){
          log.info("根据id查对象");

          Employee emp = employeeService.getById(id);
          if (emp!=null){
               return R.success(emp);
          }
          return R.error("没有查询到该用户信息");

     }
}
