package com.example.Genius.intercepter;

import com.example.Genius.Contants.Contants;
import com.example.Genius.DAO.LoginTicketMapper;
import com.example.Genius.DAO.UserMapper;
import com.example.Genius.controller.LoginController;
import com.example.Genius.model.HostHolder;
import com.example.Genius.model.LoginTicket;
import com.example.Genius.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class PassportInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(PassportInterceptor.class);
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserMapper userMapper;
    @Autowired
    LoginTicketMapper loginTicketMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = null;
        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if(cookie.getName().equals(Contants.cookies.LOGIN_TICKET_NAME)){
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        if(ticket != null){
            LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
            if(loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != Contants.loginTicket.LOGIN_STATUS ){
                logger.error("login check fail return false");
                return false;
            }

            User user = userMapper.selectByUserId(loginTicket.getUserId());
            hostHolder.setCurrentUsers(user);
            logger.error("login check successful and userId ="+user.getoId());
            return true;
        }
        logger.info("check fail cookies中没有ticket字段");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        modelAndView.addObject("user", hostHolder.getCurrentUser());
        //FIXME：登录页面跳转时这里会报空指针异常
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();//视图渲染完成，完成本次浏览器请求的所有内容，清除本线程的currentUser并终止本线程,返回主线程
    }
}
