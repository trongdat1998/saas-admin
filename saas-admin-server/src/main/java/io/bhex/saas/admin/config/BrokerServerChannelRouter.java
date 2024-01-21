package io.bhex.saas.admin.config;


import io.bhex.base.grpc.client.channel.IGrpcClientPool;
import io.bhex.saas.admin.dao.BrokerInstanceDetailMapper;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BrokerServerChannelRouter {

    @Autowired
    private IGrpcClientPool pool;
    @Autowired
    private BrokerInstanceDetailMapper detailMapper;

    public Channel getChannelByBrokerId(long brokerId) {
        if (brokerId < 100) {
            brokerId = 6002L;
        }
        Channel channel = pool.borrowChannel(String.valueOf(brokerId));
        if (channel != null) {
            return channel;
        }

        BrokerInstanceDetail detail = detailMapper.getInstanceDetailByBrokerId(brokerId);
        if (detail != null) {
            String namespace = detail.getAdminInternalApiUrl().split("broker-admin-server.")[1]
                    .split(":")[0];
            pool.setShortcut(String.valueOf(detail.getBrokerId()), "broker-server." + namespace, 7023);
        }

        channel = pool.borrowChannel(String.valueOf(brokerId));
        if (channel == null) {
            log.error("cannot get channel from pool, {}", brokerId);
            return null;
        }

        return channel;
    }
}
