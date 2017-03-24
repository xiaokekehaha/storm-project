package storm.kafka;

import static storm.kafka.KafkaUtils.taskId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.trident.GlobalPartitionInformation;
/**
 * ZKCoordinator协调器
 * @author cuijh
 *
 */
public class ZkCoordinator implements PartitionCoordinator {
    public static final Logger LOG = LoggerFactory.getLogger(ZkCoordinator.class);

    SpoutConfig _spoutConfig;
    int _taskIndex;
    int _totalTasks;
    String _topologyInstanceId;
    
    //每一个分区对应着一个分区管理器
    Map<Partition, PartitionManager> _managers = new HashMap();
    
    //缓存的List
    List<PartitionManager> _cachedList;
    
    //上次刷新的时间
    Long _lastRefreshTime = null;
    
    //刷新频率 毫秒
    int _refreshFreqMs;
    
    //动态BrokersReader
    DynamicPartitionConnections _connections;
    DynamicBrokersReader _reader;
    ZkState _state;
    Map _stormConf;

    /**
     * 
     * @param connections
     * 		动态的分区连接
     * @param stormConf
     * 		Storm的配置文件
     * @param spoutConfig
     * 		Storm spout的配置文件
     * @param state
     * 		对于ZKState的连接
     * @param taskIndex
     * 		任务
     * @param totalTasks
     * 		总共的任务
     * @param topologyInstanceId
     * 		拓扑的实例ID
     */
    public ZkCoordinator(DynamicPartitionConnections connections, Map stormConf, SpoutConfig spoutConfig, ZkState state, int taskIndex, int totalTasks, String topologyInstanceId) {
        this(connections, stormConf, spoutConfig, state, taskIndex, totalTasks, topologyInstanceId, buildReader(stormConf, spoutConfig));
    }

    public ZkCoordinator(DynamicPartitionConnections connections, Map stormConf, SpoutConfig spoutConfig, ZkState state, int taskIndex, int totalTasks, String topologyInstanceId, DynamicBrokersReader reader) {
        _spoutConfig = spoutConfig;
        _connections = connections;
        _taskIndex = taskIndex;
        _totalTasks = totalTasks;
        _topologyInstanceId = topologyInstanceId;
        _stormConf = stormConf;
        _state = state;
        ZkHosts brokerConf = (ZkHosts) spoutConfig.hosts;
        _refreshFreqMs = brokerConf.refreshFreqSecs * 1000;
        _reader = reader;
    }

    private static DynamicBrokersReader buildReader(Map stormConf, SpoutConfig spoutConfig) {
        ZkHosts hosts = (ZkHosts) spoutConfig.hosts;
        return new DynamicBrokersReader(stormConf, hosts.brokerZkStr, hosts.brokerZkPath, spoutConfig.topic);
    }

   
    public List<PartitionManager> getMyManagedPartitions() {
        if (_lastRefreshTime == null || (System.currentTimeMillis() - _lastRefreshTime) > _refreshFreqMs) {
            refresh();
            _lastRefreshTime = System.currentTimeMillis();
        }
        return _cachedList;
    }

    /**
     * 简单的刷新行为
     */
    void refresh() {
        try {
            LOG.info(taskId(_taskIndex, _totalTasks) + "Refreshing partition manager connections");
            
            //拿到所有的分区信息
            GlobalPartitionInformation brokerInfo = _reader.getBrokerInfo();
            
            //拿到自己任务的所有分区
            List<Partition> mine = KafkaUtils.calculatePartitionsForTask(brokerInfo, _totalTasks, _taskIndex);

            //拿到当前任务的分区
            Set<Partition> curr = _managers.keySet();
            
            //构造一个集合
            Set<Partition> newPartitions = new HashSet<Partition>(mine);
            
            //在new分区中，移除掉所有自己拥有的分区
            newPartitions.removeAll(curr);

            //要删除的分区
            Set<Partition> deletedPartitions = new HashSet<Partition>(curr);
            deletedPartitions.removeAll(mine);

            LOG.info(taskId(_taskIndex, _totalTasks) + "Deleted partition managers: " + deletedPartitions.toString());

            for (Partition id : deletedPartitions) {
                PartitionManager man = _managers.remove(id);
                man.close();
            }
            LOG.info(taskId(_taskIndex, _totalTasks) + "New partition managers: " + newPartitions.toString());

            for (Partition id : newPartitions) {
                PartitionManager man = new PartitionManager(_connections, _topologyInstanceId, _state, _stormConf, _spoutConfig, id);
                _managers.put(id, man);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        _cachedList = new ArrayList<PartitionManager>(_managers.values());
        LOG.info(taskId(_taskIndex, _totalTasks) + "Finished refreshing");
    }

    
    public PartitionManager getManager(Partition partition) {
        return _managers.get(partition);
    }
}
