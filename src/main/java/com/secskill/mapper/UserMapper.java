package com.secskill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secskill.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis Plus 的 BaseMapper，自动提供基础的 CRUD 操作
 *
 * @author seckill
 */
//@Mapper
public interface UserMapper {
//public interface UserMapper extends BaseMapper<User> {
    // 可以在这里添加自定义的 SQL 方法
    // 如果需要复杂查询，可以在 resources/mapper 目录下创建对应的 XML 文件
}

