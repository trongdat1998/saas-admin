package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeOpRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ExchangeOpRecordMapper extends tk.mybatis.mapper.common.Mapper<ExchangeOpRecord> {
}
