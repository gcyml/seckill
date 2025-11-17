
local userId = KEYS[1]
local buyNum = tonumber(KEYS[2])

local skuId = KEYS[3]
local perSkuLim = tonumber(KEYS[4])

local actId = KEYS[5]
local perActLim = tonumber(KEYS[6])

local orderTime = KEYS[7]

--用到的各个hash
-- 用户维度-活动维度-已购数量哈希：记录每个用户在当前活动中已购买的商品总件数，用于活动级限购校验
local user_sku_hash = 'sec_'..actId..'_u_sku_hash'
-- 用户维度-SKU维度-已购数量哈希：记录每个用户对每个SKU已购买的件数，用于SKU级限购校验
local user_act_hash = 'sec_'..actId..'_u_act_hash'
-- SKU维度-库存数量哈希：记录每个SKU在当前活动中的剩余库存，用于库存扣减与校验
local sku_amount_hash = 'sec_'..actId..'_sku_amount_hash'
-- 秒杀成功订单日志哈希：记录秒杀成功的订单快照，用于后续订单核对与日志追踪
local second_log_hash = 'sec_'..actId..'_log_hash'

local user_sku_hash = 'sec_'..actId..'_u_sku_hash'
local user_act_hash = 'sec_'..actId..'_u_act_hash'
local sku_amount_hash = 'sec_'..actId..'_sku_amount_hash'
local second_log_hash = 'sec_'..actId..'_log_hash'

--当前sku是否还有库存
local skuAmountStr = redis.call('hget',sku_amount_hash,skuId)
if skuAmountStr == false then
--         redis.log(redis.LOG_NOTICE,'skuAmountStr is nil ')
        return '-3'
end;
local skuAmount = tonumber(skuAmountStr)
--  redis.log(redis.LOG_NOTICE,'sku:'..skuId..';skuAmount:'..skuAmount)
 if skuAmount <= 0 then
   return '0'
end

redis.log(redis.LOG_NOTICE,'perActLim:'..perActLim)
local userActKey = userId..'_'..actId
--当前用户已购买此活动多少件，用于活动级限购校验
 if perActLim > 0 then
   local userActNumInt = 0
   local userActNum = redis.call('hget',user_act_hash,userActKey)
   if userActNum == false then
      redis.log(redis.LOG_NOTICE,'userActKey:'..userActKey..' is nil')
      userActNumInt = buyNum
   else
      redis.log(redis.LOG_NOTICE,userActKey..':userActNum:'..userActNum..';perActLim:'..perActLim)
      local curUserActNumInt = tonumber(userActNum)
      userActNumInt =  curUserActNumInt+buyNum
   end
   if userActNumInt > perActLim then
       return '-2'
   end
 end

local goodsUserKey = userId..'_'..skuId
redis.log(redis.LOG_NOTICE,'perSkuLim:'..perSkuLim)
--当前用户已购买此sku多少件，用于SKU级限购校验
if perSkuLim > 0 then
   local goodsUserNum = redis.call('hget',user_sku_hash,goodsUserKey)
   local goodsUserNumint = 0
   if goodsUserNum == false then
      redis.log(redis.LOG_NOTICE,'goodsUserNum is nil')
      goodsUserNumint = buyNum
   else
      redis.log(redis.LOG_NOTICE,'goodsUserNum:'..goodsUserNum..';perSkuLim:'..perSkuLim)
      local curSkuUserNumint = tonumber(goodsUserNum)
      goodsUserNumint =  curSkuUserNumint+buyNum
   end

   redis.log(redis.LOG_NOTICE,'------goodsUserNumint:'..goodsUserNumint..';perSkuLim:'..perSkuLim)
   if goodsUserNumint > perSkuLim then
       return '-1'
   end
end

--判断是否还有库存满足当前秒杀数量
if skuAmount >= buyNum then
     local decrNum = 0-buyNum
     --  Hincrby 命令用于为哈希表中的字段值加上指定增量值。
     redis.call('hincrby',sku_amount_hash,skuId,decrNum)
     redis.log(redis.LOG_NOTICE,'second success:'..skuId..'-'..buyNum)

     if perSkuLim > 0 then
         -- 给用户对当前SKU的已购数量哈希表增加购买数量
         redis.call('hincrby',user_sku_hash,goodsUserKey,buyNum)
     end

     if perActLim > 0 then
        -- 给用户对当前活动的已购数量哈希表增加购买数量
        redis.call('hincrby',user_act_hash,userActKey,buyNum)
     end

     local orderKey = userId..'_'..skuId..'_'..buyNum..'_'..orderTime
     local orderStr = '1'
     redis.call('hset',second_log_hash,orderKey,orderStr)

   return orderKey
else
   -- 库存不足
   return '0'
end
