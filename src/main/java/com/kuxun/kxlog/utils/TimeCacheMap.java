package com.kuxun.kxlog.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.kuxun.kxtopology.pojo.ObjectExpireCallback;

import backtype.storm.utils.Time;

public class TimeCacheMap<K, V> implements Serializable {

	private static final int DEFAULT_NUM_BUCKETS = 3;

	public static interface ExpiredCallback<K, V> extends Serializable {
		public void expire(K key, V val);
	}

	private LinkedList<HashMap<K, V>> _buckets;

	private final Integer _lock = new Integer(1);

	private Thread _cleaner;
	private ExpiredCallback _callback;

	/**
	 * 提供一个缺省的TimeCacheMap
	 */
	public TimeCacheMap() {
		this(60,3,new ObjectExpireCallback());
	}

	public TimeCacheMap(int expirationSecs, int numBuckets, ExpiredCallback<K, V> callback) {
		if (numBuckets < 2) {
			throw new IllegalArgumentException("numBuckets must be >= 2");
		}
		_buckets = new LinkedList<HashMap<K, V>>();

		for (int i = 0; i < numBuckets; i++) {
			_buckets.add(new HashMap<K, V>());
		}

		_callback = callback;

		final long expirationMillis = expirationSecs * 1000L;
		final long sleepTime = expirationMillis / (numBuckets - 1);

		_cleaner = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						Map<K, V> dead = null;
						Time.sleep(sleepTime);

						synchronized (_lock) {
							dead = _buckets.removeLast();
							_buckets.addFirst(new HashMap<K, V>());
						}
						if (_callback != null) {
							for (Entry<K, V> entry : dead.entrySet()) {
								_callback.expire(entry.getKey(), entry.getValue());
							}
						}
					}
				} catch (InterruptedException ex) {

				}
			}
		});

		_cleaner.setDaemon(true);
		_cleaner.start();
	}

	public TimeCacheMap(int expirationSecs, ExpiredCallback<K, V> callback) {
		this(expirationSecs, DEFAULT_NUM_BUCKETS, callback);
	}

	public TimeCacheMap(int expirationSecs) {
		this(expirationSecs, DEFAULT_NUM_BUCKETS);
	}

	public TimeCacheMap(int expirationSecs, int numBuckets) {
		this(expirationSecs, numBuckets, null);
	}

	public boolean containsKey(K key) {
		synchronized (_lock) {
			for (HashMap<K, V> bucket : _buckets) {
				if (bucket.containsKey(key)) {
					return true;
				}
			}
			return false;
		}
	}

	public V get(K key) {
		synchronized (_lock) {
			for (HashMap<K, V> bucket : _buckets) {
				if (bucket.containsKey(key)) {
					return bucket.get(key);
				}
			}
			return null;
		}
	}

	//
	public void put(K key, V value) {
		synchronized (_lock) {
			Iterator<HashMap<K, V>> it = _buckets.iterator();
			HashMap<K, V> bucket = it.next();
			bucket.put(key, value);
			while (it.hasNext()) {
				bucket = it.next();
				bucket.remove(key);
			}
		}
	}

	public Object remove(K key) {
		synchronized (_lock) {
			for (HashMap<K, V> bucket : _buckets) {
				if (bucket.containsKey(key)) {
					return bucket.remove(key);
				}
			}
			return null;
		}
	}

	public int size() {
		synchronized (_lock) {
			int size = 0;
			for (HashMap<K, V> bucket : _buckets) {
				size += bucket.size();
			}
			return size;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			_cleaner.interrupt();
		} finally {
			super.finalize();
		}
	}

	public HashSet<K> keySet() {
		HashSet<K> set = new HashSet<K>();
		for (HashMap<K, V> map : _buckets) {
			set.addAll(map.keySet());
		}
		return set;
	}

	public void removeAll() {
		synchronized (_lock) {
			for (HashMap<K, V> map : _buckets) {
				map.clear();
			}
		}
	}
}