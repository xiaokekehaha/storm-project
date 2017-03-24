package storm.kafka.trident;

import java.util.Map;
import java.util.UUID;

import storm.kafka.Partition;
import storm.trident.spout.IPartitionedTridentSpout;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;


public class TransactionalTridentKafkaSpout implements IPartitionedTridentSpout<GlobalPartitionInformation, Partition, Map> {

    TridentKafkaConfig _config;
    String _topologyInstanceId = UUID.randomUUID().toString();

    public TransactionalTridentKafkaSpout(TridentKafkaConfig config) {
        _config = config;
    }


   
    public IPartitionedTridentSpout.Coordinator getCoordinator(Map conf, TopologyContext context) {
        return new storm.kafka.trident.Coordinator(conf, _config);
    }

  
    public IPartitionedTridentSpout.Emitter getEmitter(Map conf, TopologyContext context) {
        return new TridentKafkaEmitter(conf, context, _config, _topologyInstanceId).asTransactionalEmitter();
    }

    
    public Fields getOutputFields() {
        return _config.scheme.getOutputFields();
    }

  
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}