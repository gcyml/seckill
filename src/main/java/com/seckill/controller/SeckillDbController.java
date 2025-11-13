package com.seckill.controller;

import com.seckill.service.SeckillService;
import com.seckill.util.ServerResponseUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 数据库版本的秒杀控制器（用于性能对比测试）
 *
 * @author seckill
 */
@Controller
@RequestMapping("/seckill-db")
public class SeckillDbController {

    @Resource
    @Qualifier("seckillServiceDbOptimistic")
    private SeckillService seckillService;


    /**
     * 添加活动中的sku（数据库版本）
     */
    @GetMapping("/skuadd")
    @ResponseBody
    public Object skuAdd(@RequestParam(value = "actid", required = true, defaultValue = "") String actId,
                         @RequestParam(value = "skuid", required = true, defaultValue = "") String skuId,
                         @RequestParam(value = "amount", required = true, defaultValue = "0") int amount) {
        if (actId.equals("")) {
            return new ServerResponseUtil(1, "activity id不可为空", "");
        }
        if (skuId.equals("")) {
            return new ServerResponseUtil(1, "sku id不可为空", "");
        }
        if (amount <= 0) {
            return new ServerResponseUtil(1, "sku库存必須大于0", "");
        }

        boolean isSucc = seckillService.skuAdd(actId, skuId, amount);
        int status = 1;
        String msg = "";

        if (isSucc) {
            status = 0;
            msg = "add sku amount success (DB version)";
        } else {
            status = 1;
            msg = "add sku amount failed (DB version)";
        }

        ServerResponseUtil response = new ServerResponseUtil(status, msg, "");
        return response;
    }

     /**
     * 秒杀指定sku（乐观锁版本）
     */
    @GetMapping("/skusecond")
    @ResponseBody
    public Object skuSecond(@RequestParam(value = "actid", required = true, defaultValue = "") String actId,
                            @RequestParam(value = "userid", required = true, defaultValue = "") String userId,
                            @RequestParam(value = "buynum", required = true, defaultValue = "0") int buyNum,
                            @RequestParam(value = "skuid", required = true, defaultValue = "") String skuId,
                            @RequestParam(value = "perskulim", required = true, defaultValue = "0") int perSkuLim,
                            @RequestParam(value = "peractlim", required = true, defaultValue = "0") int perActLim) {

        if (actId.equals("")) {
            return new ServerResponseUtil(1, "活动id不可为空", "");
        }
        if (userId.equals("")) {
            return new ServerResponseUtil(1, "用户id不可为空", "");
        }
        if (skuId.equals("")) {
            return new ServerResponseUtil(1, "sku id不可为空", "");
        }
        if (buyNum <= 0) {
            return new ServerResponseUtil(1, "购买数量必須大于0", "");
        }

        String result = seckillService.skuSecond(actId, userId, buyNum, skuId, perSkuLim, perActLim);

        String msg = "";
        int status = 1;

        if (result.equals("-1")) {
            msg = "已超出当前活动每件sku允许每人秒杀的数量";
            status = 1;
        } else if (result.equals("-2")) {
            msg = "已超出当前活动允许每人秒杀的数量";
            status = 1;
        } else if (result.equals("-3")) {
            msg = "sku不存在或秒杀数量未设置";
            status = 1;
        } else if (result.equals("-4")) {
            msg = "数据被并发修改，秒杀失败 (乐观锁检测到version不匹配)";
            status = 1;
        } else if (result.equals("0")) {
            msg = "库存数量不足，秒杀失败";
            status = 1;
        } else {
            msg = "秒杀成功;秒杀编号:" + result + "";
            status = 0;
        }

        ServerResponseUtil response = new ServerResponseUtil(status, msg, "");
        return response;
    }

    /**
     * 首页
     */
    @GetMapping("/index")
    public String index() {
        return "seckill/index2";
    }
}

