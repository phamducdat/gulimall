package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.MemberAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberFeignService memberFeignService;

    // Calculate shipping fee based on the delivery address
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        // Get delivery address information based on the delivery address ID
        R addrInfoR = memberFeignService.addrInfo(addrId);
        // Get MemberAddressVo from addrInfoR
        MemberAddressVo memberAddressVo = addrInfoR.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (memberAddressVo != null) {
            // Call a third-party logistics interface to calculate the shipping fee
            // Simulate shipping fee calculation: use the last digit of the user's phone number as the shipping fee
            String phone = memberAddressVo.getPhone();
            String substring = phone.substring(phone.length() - 1);
            BigDecimal bigDecimal = new BigDecimal(substring);
            // Set the member's delivery address
            fareVo.setAddress(memberAddressVo);
            // Set the shipping fee
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

    // Paginated conditional query
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wareInfoEntityQueryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wareInfoEntityQueryWrapper);

        return new PageUtils(page);
    }

}
