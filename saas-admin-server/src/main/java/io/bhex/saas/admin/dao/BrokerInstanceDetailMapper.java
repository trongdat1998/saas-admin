package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.BrokerInstanceDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface BrokerInstanceDetailMapper extends tk.mybatis.mapper.common.Mapper<BrokerInstanceDetail> {

    @Select("select * from tb_broker_instance_detail where broker_id = #{brokerId}")
    BrokerInstanceDetail getInstanceDetailByBrokerId(@Param("brokerId") Long brokerId);

    @Select("select * from tb_broker_instance_detail where deleted = 0")
    List<BrokerInstanceDetail> getAll();

    @Update("update tb_broker_instance_detail set status = #{status} where  broker_id = #{brokerId}")
    int updateStatus(@Param("brokerId") Long brokerId, @Param("status") Integer status);
}
