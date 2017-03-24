package com.kuxun.kxlog.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kuxun.kxlog.IContext;
import com.kuxun.kxlog.common.Input;

/**
 * 优先级为可以序列化的值 inputs 次之为 transient map
 * 
 * @author dengzh
 */
public class InputImpl implements Input {

	/**
	 * 当前 tuple storm组件之间传递
	 */
	private transient List tupleInfo;

	/**
	 * 
	 */
	private IContext context;

	private Map inputs = new HashMap();

	private transient Map transientInputs;

	/**
	 * 当该key对应的变量值为true时，表示已经执行过jar对应的文件解析器
	 */
	public final static String EXECUTEDPARSER = "_parser";
	/**
	 * 当该key对应的变量值为true时，表示已经执行过jar对应的拦截链
	 */
	public final static String EXECUTEDFILTERCHAIN = "_chain";

	public InputImpl() {
	}

	public void setTupleInfo(List tuple) {
		tupleInfo = tuple;
	}

	public void setContext(IContext context) {
		this.context = context;
	}

	@Override
	public List getTupleInfo() {
		// TODO Auto-generated method stub
		return tupleInfo;
	}

	@Override
	public Object getParameter(Object key) {
		// TODO Auto-generated method stub
		ensureTransientMap();
		Object retObj = inputs.get(key);
		if (retObj == null)
			retObj = transientInputs.get(key);
		return retObj;
	}

	private void ensureTransientMap() {
		if (transientInputs == null)
			transientInputs = new HashMap();
	}

	@Override
	public Iterator keyIterator() {
		// TODO Auto-generated method stub
		Set set = new HashSet(inputs.keySet());
		ensureTransientMap();
		set.addAll(transientInputs.keySet());
		return set.iterator();
	}

	@Override
	public IContext getThisServiceContext() {
		// TODO Auto-generated method stub
		return context;
	}

	@Override
	public void storePermanentData(Object key, Object value) {
		// TODO Auto-generated method stub
		inputs.put(key, value);
	}

	@Override
	public void storeTransientData(Object key, Object value) {
		// TODO Auto-generated method stub
		ensureTransientMap();
		transientInputs.put(key, value);
	}

	@Override
	public Object getParameter(Object key, boolean serializable) {
		// TODO Auto-generated method stub
		if (serializable)
			return inputs.get(key);
		ensureTransientMap();
		return transientInputs.get(key);
	}

	@Override
	public String getStringParameter(Object key) {
		// TODO Auto-generated method stub
		Object obj = getParameter(key);
		if (obj != null) {
			return String.valueOf(obj).trim();
		}
		return null;
	}

	@Override
	public void putAll(Map map) {
		// TODO Auto-generated method stub
		inputs.putAll(map);
	}

	@Override
	public void putAllTransientValues(Map map) {
		ensureTransientMap();
		transientInputs.putAll(map);
	}

	@Override
	public void removeKey(Object key) {
		// TODO Auto-generated method stub
		if (inputs.containsKey(key))
			inputs.remove(key);
		
		ensureTransientMap();
		if (transientInputs.containsKey(key))
			transientInputs.remove(key);
	}

	@Override
	public Map getInputValue() {
		// TODO Auto-generated method stub
		ensureTransientMap();
		Map map = new HashMap();
		map.putAll(transientInputs);
		map.putAll(inputs);
		return map;
	}

}
