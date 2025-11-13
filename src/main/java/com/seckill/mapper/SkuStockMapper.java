package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.SkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * SKU库存 Mapper
 *
 * @author seckill
 */
@Mapper
public interface SkuStockMapper extends BaseMapper<SkuStock> {

    /**
     * 扣减库存（最简版，无乐观锁）
     * @param actId 活动ID
     * @param skuId SKU ID
     * @param buyNum 购买数量
     * @return 更新行数
     */
    @Update("UPDATE sku_stock SET amount = amount - #{buyNum} " +
            "WHERE act_id = #{actId} AND sku_id = #{skuId} " +
            "AND amount >= #{buyNum} AND deleted = 0")
    int decreaseStock(@Param("actId") String actId, 
                      @Param("skuId") String skuId, 
                      @Param("buyNum") Integer buyNum);

    /**
     * 扣减库存（乐观锁版本）
     * 
     * 原理：
     * 1. 查询时获取 version
     * 2. UPDATE 时带上 version 条件：WHERE version = #{version}
     * 3. 同时更新 version：SET version = version + 1
     * 4. 如果 version 不匹配，UPDATE 返回 0（更新失败，说明数据被其他线程修改）
     * 
     * @param actId 活动ID
     * @param skuId SKU ID
     * @param buyNum 购买数量
     * @param version 版本号（从查询结果中获取）
     * @return 更新行数（0表示version不匹配，更新失败）
     */
    @Update("UPDATE sku_stock SET amount = amount - #{buyNum}, version = version + 1 " +
            "WHERE act_id = #{actId} AND sku_id = #{skuId} " +
            "AND amount >= #{buyNum} AND deleted = 0 AND version = #{version}")
    int decreaseStockWithVersion(@Param("actId") String actId, 
                                 @Param("skuId") String skuId, 
                                 @Param("buyNum") Integer buyNum,
                                 @Param("version") Integer version);
}

