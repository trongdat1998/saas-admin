package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.SymbolMatchTransfer;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: exchange
 * @Package: io.bhex.ex.admingrpc.mapper
 * @Author: ming.xu
 * @CreateDate: 22/11/2018 11:14 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface SymbolMatchTransferMapper extends Mapper<SymbolMatchTransfer> {

}
