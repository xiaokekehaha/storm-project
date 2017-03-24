package com.kuxun.kxtopology.cache;

import java.io.Serializable;
import java.util.Map;

/**
 * 保存与Class实例相关的数据，也可以直接在java类中用static关键字
 * 来声明相关的变量，对于一个类来说，可以有多个这样的变量，因此在
 * 这种数据结果的设计上，一个Class对应有多个存储的类型，这种类型
 * 用Map结构来封装，Map的key为变量的Class实例，value为对应的值
 * 例如,有这样的一个类：
 *   Class A{
 *     public static List list = new ArrayList();
 *     
 *     public static T t = a;
 *   }
 * 
 *  从类的声明上看，有这样的关系：
 *    
 *    A.class ->Map{List.class:l(new ArrayList()),T:a}
 *  
 *  保存这种关系后，我们就可以不用在A类中声明这样的变量。
 * ---------------------------------------
 * 数据结构：
 * Map{ A.class:Map{List.class:new ArrayList(),T:a},
 *      B.class:Map{TimeCacheMap.class:new TimeCacheMap(),List.class:new LinkedList()},
 *      C.class:Map{Hashtable.class:new Hashtable()}
 *     }
 * ---------------------------------------
 * 
 * 通过使用缓存来提高代码的复用率，同时方便管理每个Class类中对应的变量的变化
 * 
 * 
 * @author dengzh
 */
public interface CacheFactory extends Serializable{

	/**
	 * 根据key为clazzKey 找出 存储值Class类型为clazzValue的缓存对象
	 * 如果clazzValue对应的值为List，则得到这个list的实例
	 * @param clazzKey
	 * @param clazzValue
	 * @return 符合条件的缓存对象 or null 
	 */
    public <T> T getObject(Class<?> clazzKey,Class<T> clazzValue);
    
    /**
     * 取出 key对应的缓存值 value， 且（value.class == T.class）条件判断为真
     * 这在clazzValue为TimeCacheMap.class或者Map.class时，显得更为方便地取出对应的
     * value值
     * @param clazzKey
     * @param clazzValue Collection,TimeCacheMapS
     * @param key
     * @return 符合条件的缓存对象 or null
     */
    public <T> T getValue(Class<?> clazzKey,Class<?> clazzValue,Class<T> storeType, Object key);
    
    /**
     * 找出该Class缓存的所有对象
     * @param clazzKey
     * @return 该Class缓存的所有对象，
     */
    public Map getAllObjectsCached(Class clazzKey);
    
    /**
     * 查看缓存中是否存在 key为clazzKey， clazzValue 下是否包含compareObject对象
     * 有以下场景：
     *   1. clazzValue 为list时， 调用 list.contains()
     *   2. clazzValue 为map时， 判断compareObject是否为MapAware，如果是则得到对象的key值，判断map中是否包含key；
     *      否则，则将compareObject对象当成key值来判断是否存在。
     *   3. clazzValue为普通的java类时，则直接调用 equals方法判断
     * 
     * @param clazzKey
     * @param clazzValue
     * @param compareObject
     * @return
     */
    public boolean contains(Class clazzKey,Class clazzValue,Object compareObject);
    /**
     * 查看缓存中是否存在 key为clazzKey，所有缓存的对象是否包含compareObject对象
     * @param clazzKey
     * @param clazzValue
     * @param compareObject
     * @return
     */
    public boolean contains(Class clazzKey,Object compareObject);
    
    /**
     * 移除key为clazzKey的所有缓存对象
     * @param clazzKey
     */
   
    public void removeAll(Class clazzKey);
    
    /**
     * clazzKey对象缓存类型为clazzValue的value对象
     * clazzValue 分为多种，如List Map 等，每个不同的clazzValue执行
     * 添加的操作不尽相同
     * @param clazzKey
     * @param clazzValue
     * @param value
     */
    public void add(Class clazzKey,Class clazzValue, Object value);
    
    /**
     * 删除clazzKey缓存类型为clazzValue的toRemove对象
     * clazzValue 分为多种，如List Map 等，每个不同的clazzValue执行
     * 添加的操作不尽相同
     * @param clazzKey
     * @param clazzValue
     * @param value
     */
    public void remove(Class clazzKey, Class clazzValue, Object toRemove);
    
    /**
     * 删除clazzKey缓存的toRemove对象
     * clazzValue 分为多种，如List Map 等，每个不同的clazzValue执行
     * 添加的操作不尽相同
     * @param clazzKey
     * @param clazzValue
     * @param value
     */
    public void remove(Class clazzKey,Object toRemove);
    	
    /**
     * 删除ClazzKey中包含的 clazzValue
     * @param clazzKey
     * @param clazzValue
     */
    
    public void remove(Class clazzKey, Class clazzValue);
    
    /**
     * 缓存clazzKey的大小
     * @param clazzKey
     * @return
     */
    public int size(Class clazzKey);
   
    /**
     * 缓存clazzKey,value Class类型为clazzValue的大小
     * @param clazzKey
     * @param clazzValue
     * @return
     */
    public int size(Class clazzKey,Class clazzValue);
    /**
     * 缓存总大小
     * @return
     */
    public int size();
    
    
    /**
     * 将缓存的实例t 移除缓存
     * @param t
     */
    public <T> void remove(T t);
    
}
