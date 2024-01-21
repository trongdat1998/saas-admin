package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.TokenApplyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TokenApplyMapper extends tk.mybatis.mapper.common.Mapper<TokenApplyRecord> {




}
