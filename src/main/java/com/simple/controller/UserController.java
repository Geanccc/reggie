package com.simple.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.simple.common.R;
import com.simple.entity.User;
import com.simple.service.UserService;
import com.simple.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
     @Autowired
     private UserService userService;

     @Autowired
     private RedisTemplate redisTemplate;

     //发送验证码
     @PostMapping("/sendMsg")
     public R<String> sendmsg(@RequestBody User user, HttpSession session){
          //获取手机号
          String phone = user.getPhone();

          if (phone!=null) {
               //生成随机验证码
               String code = ValidateCodeUtils.generateValidateCode(4).toString();
               log.info("验证码："+code);

               //保存验证码
//               session.setAttribute(phone,code);

               //保存验证码到Redis中，有效期5分钟
               redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

               return R.success("手机验证码短信发送成功");
          }
          return R.error("手机短信发送失败");
     }

     //登录
     @PostMapping("/login")
     public R<User> login(@RequestBody Map map, HttpSession session){
          //获取手机号
          String phone = map.get("phone").toString();

          //获取验证码
          String code = map.get("code").toString();

          //获取session中的验证码
//          Object codeInSession = session.getAttribute(phone);

          //从Redis中获取验证码
          Object codeInSession = redisTemplate.opsForValue().get(phone);

          //如果验证码正确
          if (codeInSession!=null&&codeInSession.equals(code)){
               //判断是否新用户
               User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone,phone));
               //不存在则创建新用户
               if (user==null){
                    user = new User();
                    user.setPhone(phone);
                    userService.save(user);
               }
               //将登录用户的id传入session，否则跳转页面时会被过滤器拦截
               session.setAttribute("user",user.getId());
               //如果用户登录成功则删除Redis中缓存的验证
               redisTemplate.delete(phone);
               return R.success(user);
          }
          return R.error("登陆失败");
     }
}
