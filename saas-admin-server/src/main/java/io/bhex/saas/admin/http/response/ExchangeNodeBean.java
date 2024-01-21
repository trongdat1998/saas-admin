package io.bhex.saas.admin.http.response;

import lombok.Data;

@Data
public class ExchangeNodeBean {
    private String cluster;
    private long createAt;
    private int id;
    private boolean master;
    private String name;
    private long onlineTime;
    private int port;
    private  int replicaSets;
    private int status;
    private String statusString;
    private int type;
    private String typeString;
}
