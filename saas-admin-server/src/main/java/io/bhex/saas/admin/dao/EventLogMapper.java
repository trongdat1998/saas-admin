package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.EventLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.sql.Timestamp;
import java.util.List;

@Component
@org.apache.ibatis.annotations.Mapper
public interface EventLogMapper extends Mapper<EventLog> {

    @Select("select * from tb_event_log where status = 0 order by id asc limit 100")
    List<EventLog> getUnDoneTask();

    @Update("update tb_event_log set status = 1,updated=#{updated} where id = #{id}")
    int eventEnd(@Param("id") long id, @Param("updated") Timestamp now);

    @Select("select * from tb_event_log where id = #{id} for update")
    EventLog getEventLogForUpdate(@Param("id") long id);

    @Select("select * from tb_event_log where broker_id = #{brokerId} and request_id = #{requestId} and status = 0 limit 1")
    EventLog getStatus0Event(@Param("brokerId") long brokerId, @Param("requestId") long requestId);

    @Select("select * from tb_event_log where broker_id = #{brokerId} and request_info = #{requestInfo} and status = 0 limit 1")
    EventLog getStatus0Event2(@Param("brokerId") long brokerId, @Param("requestInfo") String requestInfo);


    @Select("select * from tb_event_log where broker_id = #{brokerId} and request_id = #{requestId} order by id desc limit 1")
    EventLog getEventLog(@Param("brokerId") long brokerId, @Param("requestId") long requestId);
}



