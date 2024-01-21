package io.bhex.saas.admin.controller.param;

import lombok.Data;

@Data
public class VerifyListPO {

    private int bizType = 0;

    private int status = 0;

    private long lastId = 0;

    private int pageSize = 100;

}
