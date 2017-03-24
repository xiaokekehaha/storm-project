package storm.kafka;

import java.util.List;

import backtype.storm.spout.Scheme;

public interface KeyValueScheme extends Scheme {

    public List<Object> deserializeKeyAndValue(byte[] key, byte[] value);

}
