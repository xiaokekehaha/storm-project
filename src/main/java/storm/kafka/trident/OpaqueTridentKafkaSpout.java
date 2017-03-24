package storm.kafka.trident;

import java.util.Map;
import java.util.UUID;

import storm.kafka.Partition;
import storm.trident.spout.IOpaquePartitionedTridentSpout;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;


public class OpaqueTridentKafkaSpout implements IOpaquePartitionedTridentSpout<GlobalPartitionInformation, Partition, Map> {


    TridentKafkaConfig _config;
    String _topologyInstanceId = UUID.randomUUID().toString();

    public OpaqueTridentKafkaSpout(TridentKafkaConfig config) {
        _config = config;
    }

    public IOpaquePartitionedTridentSpout.Emitter<GlobalPartitionInformation, Partition, Map> getEmitter(Map conf, TopologyContext context) {
        return new TridentKafkaEmitter(conf, context, _config, _topologyInstanceId).asOpaqueEmitter();
    }

 
    public IOpaquePartitionedTridentSpout.Coordinator getCoordinator(Map conf, TopologyContext tc) {
        return new storm.kafka.trident.Coordinator(conf, _config);
    }


    public Fields getOutputFields() {
        return _config.scheme.getOutputFields();
    }

   
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}
