package storm.kafka;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kafka.javaapi.consumer.SimpleConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.trident.IBrokerReader;

/**
 * 了解Storm如何来封装kafka接口，如何处理Connection连接的封装性问题
 * 动态的 【分区连接】
 * @author cuijh
 *
 */
public class DynamicPartitionConnections {

    public static final Logger LOG = LoggerFactory.getLogger(DynamicPartitionConnections.class);

    /**
     * 持有了一个kafka底层的SimpleConsumer对象
     * 持有了具体的分区
     * @author cuijh
     *
     */
    static class ConnectionInfo {
    	
    	//内部维持了一个SimpleConsumer
        SimpleConsumer consumer;
        
        //分区
        Set<Integer> partitions = new HashSet();

        public ConnectionInfo(SimpleConsumer consumer) {
            this.consumer = consumer;
        }
    }

    // 也就是kafka的每个节点都维持了一个ConnectionInfo,ConnectionInfo
    Map<Broker, ConnectionInfo> _connections = new HashMap();
    KafkaConfig _config;
    
    //IBrokerReader这里初始化的是ZKBrokerReader
    IBrokerReader _reader;

    /**
     * 
     * @param config
     * 		kafka配置
     * @param brokerReader
     * 		IBrokerReader - 用于拿到当前的接口
     */
    public DynamicPartitionConnections(KafkaConfig config, IBrokerReader brokerReader) {
        _config = config;
        _reader = brokerReader;
    }

    public SimpleConsumer register(Partition partition) {
        Broker broker = _reader.getCurrentBrokers().getBrokerFor(partition.partition);
        return register(broker, partition.partition);
    }

    public SimpleConsumer register(Broker host, int partition) {
        if (!_connections.containsKey(host)) {
            _connections.put(host, new ConnectionInfo(new SimpleConsumer(host.host, host.port, _config.socketTimeoutMs, _config.bufferSizeBytes, _config.clientId)));
        }
        ConnectionInfo info = _connections.get(host);
        info.partitions.add(partition);
        return info.consumer;
    }

    public SimpleConsumer getConnection(Partition partition) {
        ConnectionInfo info = _connections.get(partition.host);
        if (info != null) {
            return info.consumer;
        }
        return null;
    }

    public void unregister(Broker port, int partition) {
        ConnectionInfo info = _connections.get(port);
        info.partitions.remove(partition);
        if (info.partitions.isEmpty()) {
            info.consumer.close();
            _connections.remove(port);
        }
    }

    public void unregister(Partition partition) {
        unregister(partition.host, partition.partition);
    }

    public void clear() {
        for (ConnectionInfo info : _connections.values()) {
            info.consumer.close();
        }
        _connections.clear();
    }
}
