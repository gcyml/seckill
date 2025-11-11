package com.secskill.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类示例
 * 
 * @author seckill
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;
}

