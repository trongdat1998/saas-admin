package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.SaasTransferRecord;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component
@org.apache.ibatis.annotations.Mapper
public interface SaasTransferRecordMapper  extends Mapper<SaasTransferRecord> {
}
