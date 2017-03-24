package storm.kafka.trident;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.DynamicBrokersReader;
import storm.kafka.ZkHosts;


public class ZkBrokerReader implements IBrokerReader {

    public static final Logger LOG = LoggerFactory.getLogger(ZkBrokerReader.class);

    GlobalPartitionInformation cachedBrokers;
    DynamicBrokersReader reader;
    long lastRefreshTimeMs;


    long refreshMillis;

    public ZkBrokerReader(Map conf, String topic, ZkHosts hosts) {
        reader = new DynamicBrokersReader(conf, hosts.brokerZkStr, hosts.brokerZkPath, topic);
        cachedBrokers = reader.getBrokerInfo();
        lastRefreshTimeMs = System.currentTimeMillis();
        refreshMillis = hosts.refreshFreqSecs * 1000L;

    }

  
    public GlobalPartitionInformation getCurrentBrokers() {
        long currTime = System.currentTimeMillis();
        if (currTime > lastRefreshTimeMs + refreshMillis) {
            LOG.info("brokers need refreshing because " + refreshMillis + "ms have expired");
            cachedBrokers = reader.getBrokerInfo();
            lastRefreshTimeMs = currTime;
        }
        return cachedBrokers;
    }

    public void close() {
        reader.close();
    }
}
