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
}

