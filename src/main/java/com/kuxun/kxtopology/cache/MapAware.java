package com.kuxun.kxtopology.cache;
/**
 * 使用CacheFatory缓存Map(key,value)对象时，
 * 需要实现该接口，通过该接口得到要缓存的key，Value
 * 值
 * @author dengzh
 * @param <K>
 * @param <V>
 */
public interface MapAware<K, V> {
	/**
	 * 获取对象的key值
	 * @return
	 */
	public K getKey();
	/**
	 * 获取对象的Value值
	 * @return
	 */
    public V getValue();
}
