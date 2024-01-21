package io.bhex.saas.admin.grpc.client;

import io.bhex.base.margin.QueryMarginSymbolReply;
import io.bhex.base.margin.QueryMarginSymbolRequest;
import io.bhex.base.margin.SetSymbolConfigReply;
import io.bhex.base.margin.SetSymbolConfigRequest;
import io.bhex.base.margin.cross.FundingCrossReply;
import io.bhex.base.margin.cross.FundingCrossRequest;
import io.bhex.base.margin.cross.UpdateFundingCrossReply;
import io.bhex.base.margin.cross.UpdateFundingCrossRequest;

public interface MarginClient {
    UpdateFundingCrossReply updateFundingCross(UpdateFundingCrossRequest request);

    FundingCrossReply funingCross(FundingCrossRequest request);

    QueryMarginSymbolReply queryMarginSymbol(QueryMarginSymbolRequest request);

    SetSymbolConfigReply updateSymbolInfo(SetSymbolConfigRequest request);

}
