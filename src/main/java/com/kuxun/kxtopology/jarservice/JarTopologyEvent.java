package com.kuxun.kxtopology.jarservice;

/**
 * jar文件服务与storm接口
 * 
 * @author dengzh
 */
public class JarTopologyEvent {

	//public final static int ALLTIME = -1;

	//public final static int ONLYONCE = 1;
	
  public enum Frequency{
		AllTIME,ONLYONCE
	}
	/**
	 * 需要Jar文件服务执行次数 
	 *  -1 表示始终 1 表示只有一次
	 */
	private Frequency frequency;
	/**
	 * 执行服务间隔时间
	 */
	private int interval;

	/**
	 * 执行jar文件服务url所需要的参数
	 */
	private String[] params;
	/**
	 * 服务名字
	 */
	private String serviceName;
	
	/**
	 * 操作类型， 在JarService中定义
	 */
	private String operationType;
	
	/**
	 * 获取worker内可用的jar文件列表
	 */
	private ITopologyUsableJarList usableJarList;
	
	/**
	 * 通知注册者发生改变的信息
	 *  1. 发生变更的jar文件列表
	 *  2. 获得指定服务对应的jar文件配置
	 */
	private JarAware aware;
	
	/**
	 * 该参数来源
	 */
	private String source;
	
	/**
	 * 是否需要文件服务
	 */
	private boolean enableFileService;
	
	/**
	 * jar 版本号
	 */
	private int version;
	
	
	public JarTopologyEvent version(int version){
		setVersion(version);
		return this;
	}
	
	public JarTopologyEvent enableFileService(boolean enable){
		setEnableFileService(enable);
		return this;
	}
	
	public String getSource(){
		return source;
	}
	
	public JarTopologyEvent(Class<?> clazz){
		this.source = clazz.getName();
	}
    
	public JarTopologyEvent buildSource(Class<?> clazz){
		  this.source = clazz.getName();
		  return this;
	}
	
	
	public JarTopologyEvent buildServiceName(String serviceName){
		this.serviceName = serviceName;
		return this;
	}
	
	public JarTopologyEvent buildFrequency(Frequency frequency){
		this.frequency = frequency;
		return this;
	}
	
	public JarTopologyEvent buildInterval(int interval){
		this.interval  = interval;
		return this;
	}

	
	public JarTopologyEvent buildJarAware(JarAware aware){
		this.aware = aware;
		return this;
	}
	
	public JarTopologyEvent buildTopologyUsableList(ITopologyUsableJarList usable){
		this.usableJarList = usable;
		return this;
	}
	
	public JarTopologyEvent buildParams(String...param){
		this.params = param;
		return this;
	}
	
	
	public JarTopologyEvent buildOperationType(String operationType){
		this.operationType = operationType;
		return this;
	}
	
	
	public Frequency getFrequency() {
		return frequency;
	}

	

	public int getTimeIntervals() {
		return interval;
	}
	

	public String[] getParams() {
		return params;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof JarTopologyEvent))
			return false;
		JarTopologyEvent params = (JarTopologyEvent) obj;
		if (serviceName!=null&&!serviceName.equals(params.getServiceName()))
			return false;
		String[] parameters = params.getParams();
		int len = this.params.length;
		if (parameters.length != len)
			return false;
		for (int i = 0; i < len; i++) {
			if (!parameters[i].equals(this.params[i]))
				return false;

		}
		if (frequency == params.getFrequency()
				&& interval == params.getTimeIntervals()) {
			return true;
		}
		return false;

	}

	public String getServiceName() {
		return serviceName;
	}

	public ITopologyUsableJarList getUsableJarList() {
		return usableJarList;
	}

	public void setUsableJarList(ITopologyUsableJarList usableJarList) {
		this.usableJarList = usableJarList;
	}

	public JarAware getAware() {
		return aware;
	}

	public void setAware(JarAware aware) {
		this.aware = aware;
	}


	public String getOperationType() {
		return operationType;
	}


	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public boolean isEnableFileService() {
		return enableFileService;
	}

	public void setEnableFileService(boolean enableFileService) {
		this.enableFileService = enableFileService;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
