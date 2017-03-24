package storm.kafka.trident;

import java.util.Map;

import storm.kafka.KafkaUtils;
import storm.trident.spout.IOpaquePartitionedTridentSpout;
import storm.trident.spout.IPartitionedTridentSpout;

/**
 * Date: 11/05/2013
 * Time: 19:35
 */
class Coordinator implements IPartitionedTridentSpout.Coordinator<GlobalPartitionInformation>, IOpaquePartitionedTridentSpout.Coordinator<GlobalPartitionInformation> {

    private IBrokerReader reader;
    private TridentKafkaConfig config;

    public Coordinator(Map conf, TridentKafkaConfig tridentKafkaConfig) {
        config = tridentKafkaConfig;
        reader = KafkaUtils.makeBrokerReader(conf, config);
    }

  
    public void close() {
        config.coordinator.close();
    }

   
    public boolean isReady(long txid) {
        return config.coordinator.isReady(txid);
    }


    public GlobalPartitionInformation getPartitionsForBatch() {
        return reader.getCurrentBrokers();
    }
}
