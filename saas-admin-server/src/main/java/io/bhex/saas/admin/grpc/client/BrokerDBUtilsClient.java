package io.bhex.saas.admin.grpc.client;

import io.bhex.broker.grpc.admin.FetchOneRequest;
import io.bhex.broker.grpc.admin.FetchOneResponse;

public interface BrokerDBUtilsClient {
    FetchOneResponse fetchOneBroker(String namespace, FetchOneRequest request);

    FetchOneResponse fetchOneStatistics(String namespace, FetchOneRequest request);
}
