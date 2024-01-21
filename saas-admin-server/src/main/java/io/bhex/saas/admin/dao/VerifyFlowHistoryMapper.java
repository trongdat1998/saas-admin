package io.bhex.saas.admin.dao;


import io.bhex.saas.admin.model.VerifyFlowHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Component
@org.apache.ibatis.annotations.Mapper
public interface VerifyFlowHistoryMapper extends Mapper<VerifyFlowHistory> {

    @Select("select * from tb_verify_flow_history where org_id = #{orgId} and biz_record_id = #{bizRecordId} order by id")
    List<VerifyFlowHistory> getHistories(@Param("orgId") long orgId, @Param("bizRecordId") long bizRecordId);



}
