package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.model.VerifyFlowConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Component
@org.apache.ibatis.annotations.Mapper
public interface VerifyBizRecordMapper extends Mapper<VerifyBizRecord> {

    @Update("update tb_verify_biz_record set status = #{status} where id = #{bizRecordId}")
    int updateRecordStatus(@Param("bizRecordId") long bizRecordId, @Param("status") int status);


    @SelectProvider(type = Provider.class, method = "getVerifyFlowRecords")
    List<VerifyBizRecord> getVerifiedFlowRecords(@Param("orgId") long orgId, @Param("bizType") int bizType,
                                                 @Param("lastId") long lastId, @Param("verifyConfigs")List<VerifyFlowConfig> verifyConfigs,
                                                 @Param("pageSize") int pageSize, @Param("adminId") long adminId);

    class Provider {
        public String getVerifyFlowRecords(Map<String, Object> parameter) {
            final List<VerifyFlowConfig> configs = (List<VerifyFlowConfig>) parameter.get("verifyConfigs");
            Long lastId = (Long) parameter.get("lastId");
            Long adminId = (Long) parameter.get("adminId");
            Integer bizType = (Integer) parameter.get("bizType");

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select * from tb_verify_biz_record where org_id = #{orgId} and status not in(4,41) and verify_user_ids like '%").append(adminId).append("%'");
            if (bizType > 0) {
                sqlBuilder.append(" and biz_type = #{bizType} ");
            }
            if (lastId > 0) {
                sqlBuilder.append(" and id > #{lastId} ");
            }
            sqlBuilder.append(" and (");
            for (int i = 0; i < configs.size(); i++) {
                VerifyFlowConfig config = configs.get(i);
                if (i > 0) {
                    sqlBuilder.append(" or ");
                }
                sqlBuilder.append("(") .append("biz_type = ").append(config.getBizType())
                        .append(" and current_verify_level >= ").append(config.getLevel()).append(")");
            }
            sqlBuilder.append(")").append(" order by id desc limit 0, #{pageSize}");

            return sqlBuilder.toString();
        }
    }


}
