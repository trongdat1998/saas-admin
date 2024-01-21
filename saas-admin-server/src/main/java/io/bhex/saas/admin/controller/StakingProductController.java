package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.dto.StakingProductPermissionDTO;
import io.bhex.saas.admin.controller.param.BrokerIdPO;
import io.bhex.saas.admin.service.impl.StakingProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class StakingProductController {


    @Autowired
    StakingProductService stakingProductService;

    /**
     * 获取broker的理财产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/get_permission", method = RequestMethod.POST)
    public ResultModel<StakingProductPermissionDTO> getBrokerProductPermission(@RequestBody @Validated BrokerIdPO po) {

        StakingProductPermissionDTO dto = stakingProductService.getBrokerProductPermission(po.getBrokerId());

        return ResultModel.ok(dto);
    }

    /**
     * 添加borker定期产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/add_fixed_permission", method = RequestMethod.POST)
    public ResultModel addBrokerFixedProductPermission(@RequestBody @Validated BrokerIdPO po) {
        stakingProductService.setBrokerFixedProductPermission(po.getBrokerId(), 1);
        return ResultModel.ok();
    }

    /**
     * 删除borker定期产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/delete_fixed_permission", method = RequestMethod.POST)
    public ResultModel deleteBrokerFixedProductPermission(@RequestBody @Validated BrokerIdPO po) {
        stakingProductService.setBrokerFixedProductPermission(po.getBrokerId(), 0);
        return ResultModel.ok();
    }

    /**
     * 添加borker定期锁仓产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/add_fixedlock_permission", method = RequestMethod.POST)
    public ResultModel addBrokerFixedLockProductPermission(@RequestBody @Validated BrokerIdPO po) {
        stakingProductService.setBrokerFixedLockProductPermission(po.getBrokerId(), 1);
        return ResultModel.ok();
    }

    /**
     * 删除borker定期锁仓产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/delete_fixedlock_permission", method = RequestMethod.POST)
    public ResultModel deleteBrokerFixedLockProductPermission(@RequestBody @Validated BrokerIdPO po) {
        stakingProductService.setBrokerFixedLockProductPermission(po.getBrokerId(), 0);
        return ResultModel.ok();
    }

    /**
     * 添加borker活期产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/add_current_permission", method = RequestMethod.POST)
    public ResultModel addBrokerCurrentProductPermission(@RequestBody @Validated BrokerIdPO po) {
        stakingProductService.setBrokerCurrentProductPermission(po.getBrokerId(), 1);
        return ResultModel.ok();
    }

    /**
     * 删除borker活期产品权限
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/staking/product/delete_current_permission", method = RequestMethod.POST)
    public ResultModel deleteBrokerCurrentProductPermission(@RequestBody @Validated BrokerIdPO po) {
        stakingProductService.setBrokerCurrentProductPermission(po.getBrokerId(), 0);
        return ResultModel.ok();
    }

}
