package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_event_log")
public class EventLog {

  @Id
  @GeneratedValue(generator="JDBC")
  private Long id;
  private Long brokerId;
  private String cmd;
  private String requestInfo;
  private Long requestId;
  private java.sql.Timestamp created;
  private java.sql.Timestamp updated;
  private Integer status;
  private String remark;

  public String uniqBrokerSymbol() {
    return brokerId + "-" + requestInfo;
  }
}
