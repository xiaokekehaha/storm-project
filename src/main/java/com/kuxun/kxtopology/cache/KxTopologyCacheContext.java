package com.kuxun.kxtopology.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.kuxun.kxlog.utils.TimeCacheMap;
import com.kuxun.kxtopology.pojo.ObjectExpireCallback;

/**
 * 默认 认为一个Class对应的缓存类型已经定义好 线程安全， 缓存支持Collection,Map集合类操作，也支持普通的java类
 * 但是对于一些非Collection，Map的容器，其并不能支持容器的增删改查操作，例如TimeCacheMap
 * 为了支持如TimeCacheMap的操作，必须得到该类的实例，然后方可进行操作。 该类为BeanFactory的单例模式实现，
 * 
 * 
 * 未来的改进可以可以从减少锁粒度出发
 * 
 * @author dengzh
 *
 *
 */
public class KxTopologyCacheContext implements CacheFactory {

	private final static KxTopologyCacheContext cacheContext = new KxTopologyCacheContext();
	// 缓存
	Map<Class, Map<Class, Object>> cache;

	Object _lock = new Object();

	private KxTopologyCacheContext() {
		cache = new ConcurrentHashMap<Class, Map<Class, Object>>();
	}

	public static KxTopologyCacheContext getSingleCacheContext() {
		return cacheContext;
	}

	/*
	 * @Override public <T> T getObject(Class clazzKey, Class<T> clazzValue) {
	 * // TODO Auto-generated method stub Map<Class, Object> clazzValues =
	 * cache.get(clazzKey); if (clazzValues != null) return
	 * clazzValues.get(clazzValue); return null; }
	 */

	public Map getAllObjectsCached(Class clazzKey) {
		// TODO Auto-generated method stub
		return cache.get(clazzKey);
	}

	public boolean contains(Class clazzKey, Class clazzValue, Object compareObject) {
		// TODO Auto-generated method stub
		if (compareObject == null)
			return false;
		Object obj = getObject(clazzKey, clazzValue);
		synchronized (_lock) {
			if (obj instanceof Collection) {
				Collection collection = (Collection) obj;
				return collection.contains(compareObject);
			} else if (obj instanceof Map) {
				Map map = (Map) obj;
				Object key = compareObject;
				if (compareObject instanceof MapAware)
					key = ((MapAware) compareObject).getKey();
				return map.containsKey(key);

			} else if (obj instanceof TimeCacheMap) {
				TimeCacheMap timeCache = (TimeCacheMap) obj;
				Object key = compareObject;
				if (compareObject instanceof MapAware)
					key = ((MapAware) compareObject).getKey();
				return timeCache.containsKey(key);
			} else {
				return compareObject.equals(obj);
			}
		}
	}

	public boolean contains(Class clazzKey, Object compareObject) {
		// TODO Auto-generated method stub
		Map map = getAllObjectsCached(clazzKey);
		synchronized (_lock) {
			if (map != null) {
				Set<Class> keySet = map.keySet();
				for (Iterator iter = keySet.iterator(); iter.hasNext();) {
					boolean flag = contains(clazzKey, (Class) iter.next(), compareObject);
					if (flag)
						return true;
				}

			}
			return false;
		}
	}

	public void removeAll(Class clazzKey) {
		// TODO Auto-generated method stub
		cache.remove(clazzKey);
	}

	public void add(Class clazzKey, Class clazzValue, Object value) {
		// TODO Auto-generated method stub
		synchronized (_lock) {
			ensure(clazzKey, clazzValue);
			Object obj = getObject(clazzKey, clazzValue);
			if (obj instanceof Collection) {
				((Collection) obj).add(value);
			} else if (obj instanceof Map) {
				if (value instanceof MapAware) {
					MapAware valueToStore = (MapAware) value;
					((Map) obj).put(valueToStore.getKey(), valueToStore.getValue());
				} else {
					throw new RuntimeException("unsurported type, please check the " + value
							+ " has implemented the MapAware interface");
				}
			} else if (obj instanceof TimeCacheMap) {
				if (value instanceof MapAware) {
					MapAware valueToStore = (MapAware) value;
					((TimeCacheMap) obj).put(valueToStore.getKey(), valueToStore.getValue());
				} else {
					throw new RuntimeException("unsurported type, please check the " + value
							+ " has implemented the MapAware interface");
				}
			} else {
				Map<Class, Object> store = cache.get(clazzKey);
				store.put(clazzValue, value);
			}
		}
	}

	/**
	 * 确保实例缓存能够存储，类型的实例化
	 * 
	 * @param clazzKey
	 * @param clazzValue
	 * @return
	 */
	public Object ensure(Class clazzKey, Class clazzValue) {
		Object cachedValue = null;
		boolean isCollectionOrMap = Collection.class.isAssignableFrom(clazzValue)
				|| Map.class.isAssignableFrom(clazzValue) || TimeCacheMap.class.isAssignableFrom(clazzValue);
		synchronized (_lock) {
			boolean contains = cache.containsKey(clazzKey) && cache.get(clazzKey).containsKey(clazzValue);
			if (!contains) {
				Map<Class, Object> map = cache.get(clazzKey);
				if ((map = cache.get(clazzKey)) == null) {
					map = new HashMap<Class, Object>();
					cache.put(clazzKey, map);
				}
				try {
					if (!map.containsKey(clazzValue)) {
						if (isCollectionOrMap) {
							if (TimeCacheMap.class.isAssignableFrom(clazzValue)) {
								cachedValue = new TimeCacheMap(60, 3, new ObjectExpireCallback());
							} else
								cachedValue = clazzValue.newInstance();
							map.put(clazzValue, cachedValue);
						} else {
							// 为了保持一致性，当clazzValue不是一个容器时，直接往其中存储一个null值
							map.put(clazzValue, null);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return cachedValue;
		}
	}

	public void remove(Class clazzKey, Class clazzValue, Object toRemove) {
		// TODO Auto-generated method stub
		if (!cache.containsKey(clazzKey))
			return;
		Map<Class, Object> store = cache.get(clazzKey);
		if (!store.containsKey(clazzValue))
			return;
		Object value = store.get(clazzValue);
		if (value instanceof Collection) {
			if (toRemove instanceof Collection) {
				for (Iterator iter = ((Collection) toRemove).iterator(); iter.hasNext();) {
					((Collection) value).remove(iter.next());
				}
			} else
				((Collection) value).remove(toRemove);
		} else if (value instanceof Map) {
			if (toRemove instanceof MapAware) {
				MapAware keyValue = (MapAware) toRemove;
				((Map) value).remove(keyValue.getKey());
			} else
				((Map) value).remove(toRemove);

		} else if (value instanceof TimeCacheMap) {
			if (toRemove instanceof MapAware) {
				MapAware keyValue = (MapAware) toRemove;
				((TimeCacheMap) value).remove(keyValue.getKey());
			} else
				((TimeCacheMap) value).remove(toRemove);

		} else {
			store.remove(clazzValue);
		}
	}

	public void remove(Class clazzKey, Class classValue) {
		// TODO Auto-generated method stub
		Map<Class, Object> belongsToClass = cache.get(clazzKey);
		if (belongsToClass == null)
			return;
		belongsToClass.remove(classValue);
	}

	public int size(Class clazzKey) {
		// TODO Auto-generated method stub
		synchronized (_lock) {
			if (!cache.containsKey(clazzKey))
				return 0;
			Map<Class, Object> value = cache.get(clazzKey);
			int count = 0;
			for (Iterator<Class> iter = value.keySet().iterator(); iter.hasNext();) {
				count += size(clazzKey, iter.next());
			}
			return cache.get(clazzKey).size();
		}
	}

	public int size(Class clazzKey, Class clazzValue) {
		// TODO Auto-generated method stub
		synchronized (_lock) {
			if (!cache.containsKey(clazzKey))
				return 0;
			Map<Class, Object> store = cache.get(clazzKey);
			if (!store.containsKey(clazzValue))
				return 0;
			Object obj = store.get(clazzValue);

			if (obj instanceof Collection) {
				return ((Collection) obj).size();

			} else if (obj instanceof Map) {
				return ((Map) obj).size();
			}
			return 1;
		}
	}

	public int size() {
		// TODO Auto-generated method stub
		synchronized (_lock) {
			Set<Class> key = cache.keySet();
			int count = 0;
			for (Iterator<Class> iter = key.iterator(); iter.hasNext();) {
				count += size(iter.next());
			}
			return count;
		}
	}

	public <T> T getObject(Class<?> clazzKey, Class<T> clazzValue) {
		// TODO Auto-generated method stub
		Map<Class, Object> clazzValues = cache.get(clazzKey);
		if (clazzValues != null)
			return (T) clazzValues.get(clazzValue);
		return null;
	}

	public <T> T getValue(Class<?> clazzKey, Class<?> clazzValue, Class<T> storeType, Object key) {
		Object obj = getObject(clazzKey, clazzValue);
		if (obj == null)
			return null;

		Object realKey = null;
		if (key instanceof MapAware) {
			realKey = ((MapAware) key).getKey();
		} else {
			realKey = key;
		}

		if (obj instanceof Map) {
			Map map = (Map) obj;
			Object value = map.get(realKey);
			if (value != null)
				return (T) value;
		} else if (obj instanceof TimeCacheMap) {
			TimeCacheMap cache = (TimeCacheMap) obj;
			Object value = cache.get(realKey);
			if (value != null)
				return (T) value;
		} else if (obj instanceof Collection) {
			Collection l = (Collection) obj;
			Object value = null;
			for (Object o : l) {
				if (o != null && o.equals(realKey)) {
					value = o;
					break;
				}
			}
			if (value != null) {
				return (T) value;
			}

		} else {
			if (obj != null)
				return (T) obj;
		}
		return null;
	}

	public void remove(Class clazzKey, Object toRemove) {
		// TODO Auto-generated method stub
		Map<Class, Object> belongsToClass = cache.get(clazzKey);
		if (belongsToClass == null)
			return;
		Collection<Class> cl = belongsToClass.keySet();
		for(Class klass : cl){
			  remove(clazzKey, klass, toRemove);
		}
	}

	public <T> void remove(T t) {
		Set<Map.Entry<Class,Map<Class,Object>>> entrySet = cache.entrySet();
		for(Iterator<Map.Entry<Class,Map<Class,Object>>> iter = entrySet.iterator();iter.hasNext();){
			Map.Entry<Class,Map<Class,Object>> me = iter.next();
            Class klassKey = me.getKey();
            remove(klassKey, t);
		}
	}
}
