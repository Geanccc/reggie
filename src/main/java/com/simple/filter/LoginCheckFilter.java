package com.simple.filter;

import com.alibaba.fastjson.JSON;
import com.simple.common.BaseContext;
import com.simple.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @ClassName: LoginCheckFilter
 * @Description: 打开任意页面时，检查用户是否已经完成登录
 * @author: 名字
 * @date: 2022/5/10  15:57
 */

@Slf4j
//添加filter过滤器，设置过滤器名称和过滤哪些请求
//用户判断当前页面是否登录
//注意：启动类上需要添加@ServletComponentScan注解
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
     //路径匹配器，支持通配符
     //用于将前端请求的路径和我们设置需要放行的路径对比，是否一致
     public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

     @Override
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
          //用于把获取和返回前端的对象转化成Http形式
          HttpServletRequest request=(HttpServletRequest) servletRequest;
          HttpServletResponse response=(HttpServletResponse) servletResponse;

          // 1、获取本次请求的URI
          String requestURI = request.getRequestURI();
          log.info("拦截到请求：{}",requestURI);

          //        定义不需要处理的请求路径
          String[] urls=new String[]{
                  "/employee/login",
                  "/employee/logout",
                  "/backend/**",
                  "/front/**",
                  "/common/**",
                  "/user/sendMsg",
                  "/user/login"
          };

          //        2、判断本次请求是否需要处理
          boolean check = check(urls, requestURI);

          //        3、如果不需要处理，则直接放行
          if (check){
               log.info("本次请求{}不需要处理",requestURI);
               filterChain.doFilter(request,response);
               return;
          }

          //        4-1、判断登录状态，如果已登录，则直接放行
          if (request.getSession().getAttribute("Employee")!=null){
               log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("Employee"));

               Long empId = (Long) request.getSession().getAttribute("Employee");
               BaseContext.setCurrentId(empId);

               filterChain.doFilter(request,response);
               return;
          }
          //        4-2、判断登录状态，如果已登录，则直接放行
          if (request.getSession().getAttribute("user") != null) {
               log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));

               Long userId= (Long) request.getSession().getAttribute("user");

               BaseContext.setCurrentId(userId);

               filterChain.doFilter(request, response);
               return;
          }

          log.info("用户未登录");
          //        5、如果未登录则返回未登录结果,通过输出流向客户端页面响应数据
          response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
          return;
     }

     //路径匹配，检查本次请求是否需要放行
     public boolean check(String[] urls,String requestURI){
          for (String url : urls) {
               boolean match = PATH_MATCHER.match(url, requestURI);
               if (match==true){
                    return true;
               }
          }
          return false;
     }
}
