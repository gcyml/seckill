package com.secskill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 秒杀系统控制器
 *
 * @author seckill
 */
@Controller
@RequestMapping("/seckill")
public class SecSkillController {

    /**
     * 首页
     */
    @GetMapping("/index")
    public String index() {
        return "seckill/index";
    }
}

