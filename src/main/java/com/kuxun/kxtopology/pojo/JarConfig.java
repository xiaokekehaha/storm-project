package com.kuxun.kxtopology.pojo;

import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.IParserConfig;
import com.kuxun.kxlog.IServiceConfig;
import com.kuxun.kxlog.impl.IServiceLoggerConf;
import com.kuxun.kxtopology.cache.MapAware;

/**
 * 对于一个jar文件，都包含一个jar的配置文件
 * 通过这个配置 文件，我们可以取得一个jar对应的
 * 数据发射源，日志解析器，过滤器以及业务的程序配置
 * 入口
 * MapAware key jar文件对应的服务
 * @author dengzh
 */
public interface JarConfig extends MapAware<String, JarConfig>{

	/**
	 * 获取该jar对应的业务名称
	 * @return 业务名称
	 */
	public String getThisJarService();
	/**
	 * 获取该jar对应的数据源配置
	 * @return jar数据源配置
	 */
    public IDataSourceConfig getThisJarDataSource();
    /**
     * 获取过滤拦截器配置
     * @return jar过滤拦截器配置
     */
    public IFilterConfig[] getThisJarFilter();
    
    /**
     * 获取该jar的日志解析器配置
     * @return jar的日志解析器配置
     */
    public IParserConfig getThisJarLogParser();
    /**
     * 获取该jar业务逻辑程序入口配置
     * @return jar业务逻辑程序入口配置
     */
    public IServiceConfig getThisJarLogicService();
    /**
     * 获取该jar的日志输出配置
     * @return jar的日志输出配置
     */
    public IServiceLoggerConf getThisJarLogConfig();
    /**
     * 获取当前JarConfig配置的标识信息，用以区分是否发生更新操作
     * @return
     */
    public long getThisJarConfigFlag();
    
}
