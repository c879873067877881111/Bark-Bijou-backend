package com.smallnine.apiserver.service;

import com.smallnine.apiserver.entity.VipLevel;

import java.math.BigDecimal;
import java.util.List;

public interface VipLevelService {

    VipLevel findById(Long id);

    VipLevel findByName(String name);

    List<VipLevel> findAllActive();

    List<VipLevel> findAll();

    VipLevel getVipLevelBySpending(BigDecimal spending);

    VipLevel createVipLevel(VipLevel vipLevel);

    VipLevel updateVipLevel(VipLevel vipLevel);

    void deleteVipLevel(Long id);

    long count();

    long countActive();
}
