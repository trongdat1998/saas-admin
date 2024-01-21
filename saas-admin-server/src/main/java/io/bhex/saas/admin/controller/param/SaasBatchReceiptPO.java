package io.bhex.saas.admin.controller.param;

import lombok.Data;
import org.json.JSONObject;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-16 15:08
 */
@Data
public class SaasBatchReceiptPO {
    @NotNull
    public Long orgId;
    public String brokerName = "";
    public String tokenId = "";
    //需要入账的ID，【,】分隔
    @NotEmpty
    public String accountIds ;
    //需要入账的ID，【,】分隔
    @NotEmpty
    public String receiptOrderIds ;
    public List<JSONObject> receiptDetails = new ArrayList<>();
}
