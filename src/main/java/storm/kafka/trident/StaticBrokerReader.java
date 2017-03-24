package storm.kafka.trident;

public class StaticBrokerReader implements IBrokerReader {

    private GlobalPartitionInformation brokers = new GlobalPartitionInformation();

    public StaticBrokerReader(GlobalPartitionInformation partitionInformation) {
        this.brokers = partitionInformation;
    }

    
    public GlobalPartitionInformation getCurrentBrokers() {
        return brokers;
    }

   
    public void close() {
    }
}
