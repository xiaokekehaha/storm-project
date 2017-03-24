package com.kuxun.kxtopology.xml;

import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.IParserConfig;
import com.kuxun.kxlog.IServiceConfig;
import com.kuxun.kxlog.impl.IServiceLoggerConf;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * 组建一个新的JarConfig
 */ 
public class JarConfigWrapper {


	
	public static JarConfig wrap(XmlConfigParserFactory factory) {
		
		Object obj = null;
		final String service = 
				(obj =factory.parse(XmlConfigNodeDefinition.SERVICENAME))==null?
						null:String.valueOf(obj);
		final IServiceConfig serviceConf=
				(obj=factory.parse(XmlConfigNodeDefinition.LOGICSERVICE))==null?
						null:(IServiceConfig)obj;
		final IParserConfig parserConfig=
				(obj=factory.parse(XmlConfigNodeDefinition.LOGPARSER))==null?
						null:(IParserConfig)obj;
		final IFilterConfig[] filtersConfig=
				(obj=factory.parse(XmlConfigNodeDefinition.FILTER))==null?
						null:(IFilterConfig[])obj;
		final IDataSourceConfig dataSourceConfig=
				(obj=factory.parse(XmlConfigNodeDefinition.DATASOURCE))==null?
						null:(IDataSourceConfig)obj;
		final IServiceLoggerConf sloggerConf = 
				(obj=factory.parse(XmlConfigNodeDefinition.LOG))==null?null:
					(IServiceLoggerConf)obj;
		obj = null;
		
		return new JarConfig() {
			@Override
			public JarConfig getValue() {
				// TODO Auto-generated method stub
				return this;
			}
			@Override
			public String getKey() {
				// TODO Auto-generated method stub
				return service;
			}
			@Override
			public String getThisJarService() {
				// TODO Auto-generated method stub
				return service;
			}
			
			@Override
			public IServiceConfig getThisJarLogicService() {
				// TODO Auto-generated method stub
				return serviceConf;
			}
			
			@Override
			public IParserConfig getThisJarLogParser() {
				// TODO Auto-generated method stub
				return parserConfig;
			}
			
			@Override
			public IFilterConfig[] getThisJarFilter() {
				// TODO Auto-generated method stub
				return filtersConfig;
			}
			
			@Override
			public IDataSourceConfig getThisJarDataSource() {
				// TODO Auto-generated method stub
				return dataSourceConfig;
			}
			
			@Override
			public long getThisJarConfigFlag() {
				// TODO Auto-generated method stub
				return System.currentTimeMillis();
			}
			
			@Override
			public IServiceLoggerConf getThisJarLogConfig() {
				return sloggerConf;
			}
			
		};
	}

}
