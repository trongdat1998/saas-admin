package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ContractApplyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ContractApplyMapper extends tk.mybatis.mapper.common.Mapper<ContractApplyRecord> {

    @Select("select * from tb_contract_apply_record where symbol_id=#{symbolId}")
    ContractApplyRecord getBySymbolId(@Param("symbolId") String symbolId);

}
