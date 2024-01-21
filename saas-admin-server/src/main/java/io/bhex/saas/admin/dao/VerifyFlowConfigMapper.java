package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.VerifyFlowConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@org.apache.ibatis.annotations.Mapper
public interface VerifyFlowConfigMapper extends Mapper<VerifyFlowConfig> {

    @Select("select * from tb_verify_flow_config where org_id = #{orgId} and status = 1 and biz_type = #{bizType} limit 1")
    VerifyFlowConfig getVerifyFlowConfig(@Param("orgId") long orgId, @Param("bizType") int bizType);

    @Select("select * from tb_verify_flow_config where org_id = #{orgId} order by status desc")
    List<VerifyFlowConfig> getVerifyFlowConfigs(@Param("orgId") long orgId);

    @Select("select * from tb_verify_flow_config where org_id = #{orgId} and status = 1")
    List<VerifyFlowConfig> getAvailabeVerifyFlowConfigs(@Param("orgId") long orgId);


}
