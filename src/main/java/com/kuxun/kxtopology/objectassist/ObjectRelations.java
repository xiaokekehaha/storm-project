package com.kuxun.kxtopology.objectassist;

import com.kuxun.kxlog.IContext;
import com.kuxun.kxlog.IDataSource;
import com.kuxun.kxlog.IFilter;
import com.kuxun.kxlog.IParser;
import com.kuxun.kxlog.IService;
import com.kuxun.kxtopology.cache.MapAware;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * 记录service -> jarconfig ---> classloader之间的关系
 * 维护jar各个属性
 * 记录各种属性
 * 一个service同一个Jar版本可以被进程内其他线程共享
 * 一个service对应的jar发生更新时，将共享的旧jar文件移到垃圾箱中，
 * 创建一个新的classloader，加载jar文件
 * @author dengzh
 */
public class ObjectRelations implements MapAware<String, ObjectRelations>{

	private String service;

	private JarConfig config;
	
	private ClassLoader classloader;
	
	private IDataSource dataSource;
	
	private IFilter[] filters;
	
	private IParser logParser;
	
	private IService logic;
	/**
	 * 该jar服务的上下文环境
	 */
	private IContext context;
	
	boolean hasDataSourceInited = false;
	
	boolean hasFilterInited = false;
	
	boolean hasParserInited = false;
	
	boolean hasServiceInited = false;
	
	
	private int version;
	
	
	
	public ObjectRelations(String service,JarConfig conf,ClassLoader classloader){
		this.service = service;
		this.config = conf;
		this.classloader = classloader;
	    
	}
	public String getService() {
		return service;
	}


	public void setService(String service) {
		this.service = service;
	}


	public JarConfig getConfig() {
		return config;
	}


	public void setConfig(JarConfig config) {
		this.config = config;
	}


	public ClassLoader getClassloader() {
		return classloader;
	}


	public void setClassloader(ClassLoader classloader) {
		this.classloader = classloader;
	}


	public IDataSource getDataSource() {
		return dataSource;
	}


	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}


	public IFilter[] getFilters() {
		return filters;
	}


	public void setFilters(IFilter[] filters) {
		this.filters = filters;
	}


	public IParser getLogParser() {
		return logParser;
	}


	public void setLogParser(IParser logParser) {
		this.logParser = logParser;
	}


	public IService getLogic() {
		return logic;
	}


	public void setLogic(IService logic) {
		this.logic = logic;
	}


	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return service;
	}

	@Override
	public ObjectRelations getValue() {
		// TODO Auto-generated method stub
		return this;
	}
	public IContext getContext() {
		return context;
	}
	public void setContext(IContext context) {
		this.context = context;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
}
