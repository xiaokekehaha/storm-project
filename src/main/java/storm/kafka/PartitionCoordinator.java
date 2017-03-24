package storm.kafka;

import java.util.List;

public interface PartitionCoordinator {
	
	/**
	 * 拿到我管理的分区列表，得到所有的Partition
	 * @return
	 */
    List<PartitionManager> getMyManagedPartitions();

    /**
     * 依据制定的分区partition，去getManager,得到一个分区管理器
     * @param partition
     * @return
     */
    PartitionManager getManager(Partition partition);
}
