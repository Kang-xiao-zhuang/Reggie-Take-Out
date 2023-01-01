package com.zhuang.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuang.reggie_take_out.common.R;
import com.zhuang.reggie_take_out.entity.User;
import com.zhuang.reggie_take_out.service.UserService;
import com.zhuang.reggie_take_out.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

    /**
     * 发送手机号验证码
     *
     * @param user User
     * @return R<String>
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpSession session, @RequestBody User user) {
        //获取手机号
        String phone = user.getPhone();
        //判断手机号的正确性
        if (StringUtils.isNotEmpty(phone)) {
            //生成四位随机的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码为:" + code);
            //调用阿里云的短信服务
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //将验证码存到session中
            //session.setAttribute(phone, code);
            //缓存验证码(设置过期时间为3分钟)
            redisTemplate.opsForValue().set(phone, code, 3, TimeUnit.MINUTES);
            log.info("执行redis操作");
            return R.success("验证码已发送!");
        }
        return R.error("验证码获取失败!");
    }

    /**
     * 用户登录
     *
     * @param map     Map
     * @param session HttpSession
     * @return R<User>
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info("登录信息为:" + map);
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取验证码
        //Object attribute = session.getAttribute(phone);
        //从缓存中获取验证码
        String attribute = (String) redisTemplate.opsForValue().get(phone);
        //进行验证码对比
        if (attribute != null && attribute.equals(code)) {
            //对比一致,证明登录成功
            //判断用户是否存在
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            User one = userService.getOne(lambdaQueryWrapper);
            if (one == null) {
                //不存在,进行注册
                User user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                one = user;
            }
            Long id = one.getId();
            //将user.id传到session中
            session.setAttribute("user", id);
            //从Redis中删除缓存的验证码
            redisTemplate.delete(phone);
            return R.success(one);
        }
        //不一致,返回失败
        return R.error("登录失败!");
    }

    /**
     * 用户退出
     *
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session) {
        log.info(session.getAttribute("user") + "用户退出");
        session.invalidate();
        return R.success("退出成功");
    }

}
