package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVO;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * Social Login - Weibo Login
     */
    @GetMapping("/oauth2.0weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "3682553736");
        map.put("client_secret", "ba4e058b83491111b19d64f997b60fac");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0weibo/success");
        map.put("code", code);
        // Get Access Token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());

        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            // Convert JSON string to SocialUser object
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            // Call remote microservice: Social Login
            R loginR = memberFeignService.oauthLogin(socialUser);

            if (loginR.getCode() == 0) {
                // Login successful
                // Get loginUser from loginR
                MemberResponseVO loginUser = loginR.getData(new TypeReference<MemberResponseVO>() {
                });
                // Store user login information (loginUser) in the session
                session.setAttribute(AuthServerConstant.LOGIN_USER, loginUser);
                // Redirect to website homepage
                return "redirect:http://gulimall.com";
            } else {
                // Login failed
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            // Login failed
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}

