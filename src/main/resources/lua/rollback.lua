-- Redis 回滚脚本
-- 用于数据库操作失败时，回滚 Redis 中预扣减的库存和限购记录
--
-- 参数说明：
-- KEYS[1]: userId
-- KEYS[2]: buyNum (购买数量，需要回滚的数量)
-- KEYS[3]: skuId
-- KEYS[4]: perSkuLim (SKU级限购，0表示不限制)
-- KEYS[5]: actId
-- KEYS[6]: perActLim (活动级限购，0表示不限制)
--
-- 返回值：
-- '1': 回滚成功
-- '0': 回滚失败

local userId = KEYS[1]
local buyNum = tonumber(KEYS[2])
local skuId = KEYS[3]
local perSkuLim = tonumber(KEYS[4])
local actId = KEYS[5]
local perActLim = tonumber(KEYS[6])

-- 定义各个hash
local sku_amount_hash = 'sec_'..actId..'_sku_amount_hash'
local user_sku_hash = 'sec_'..actId..'_u_sku_hash'
local user_act_hash = 'sec_'..actId..'_u_act_hash'

-- 1. 恢复库存（增加库存）
redis.call('hincrby', sku_amount_hash, skuId, buyNum)

-- 2. 回滚SKU级限购记录（如果有限购配置）
if perSkuLim > 0 then
    local goodsUserKey = userId..'_'..skuId
    -- 减少用户的已购数量（负数表示减少）
    redis.call('hincrby', user_sku_hash, goodsUserKey, -buyNum)
end

-- 3. 回滚活动级限购记录（如果有限购配置）
if perActLim > 0 then
    local userActKey = userId..'_'..actId
    -- 减少用户的已购数量（负数表示减少）
    redis.call('hincrby', user_act_hash, userActKey, -buyNum)
end

-- 回滚成功
return '1'

