package com.kuxun.kxtopology.xml;

import java.io.InputStream;

import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.IParserConfig;
import com.kuxun.kxlog.IServiceConfig;
import com.kuxun.kxtopology.pojo.JarConfig;

public class Test {
	
	public static void main(String[] args) throws Exception{
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
		JarConfig conf = new XmlConfigDefinitionReader().parse(is);
		String service = conf.getThisJarService();
		System.out.println("service:"+service);
		IParserConfig parserConfig = conf.getThisJarLogParser();
		System.out.println("********parserConfig**********");
		System.out.println(parserConfig.getThisParserName()+","+parserConfig.getThisLogParserClassName());
		System.out.println(parserConfig.getThisLogPatternByName(IParserConfig.DEFAULTPATTERN));
		System.out.println(parserConfig.getThisLogPatternByName("hello, parser1"));
		System.out.println("******************************");
		IFilterConfig[] filterConf = conf.getThisJarFilter();
		
		System.out.println("********filter****************");
		for(IFilterConfig filter:filterConf){
			System.out.println(filter.getThisFilterName()+","+filter.getThisFilterClassName());
		}
		System.out.println("******************************");
		System.out.println("********service***************");
		IServiceConfig serviceConf = conf.getThisJarLogicService();
		System.out.println(serviceConf.getThisServiceClassName()+","+serviceConf.getThisServiceAlias());
		System.out.println(serviceConf.getInitialValue("serviceParam2"));
		System.out.println(serviceConf.getInitialValue("serviceParam1"));
		System.out.println("******************************");
		System.out.println("********datasource************");
		IDataSourceConfig sourceConf = conf.getThisJarDataSource();
		System.out.println(sourceConf.getThisDataSourceAlias()+","+sourceConf.getThisDataSourceClassName()+","+sourceConf.getThisDataSourceParallelism());
		System.out.println(sourceConf.getInitialValue("spoutParam1"));
		System.out.println(sourceConf.getInitialValue("hello,spout"));
		System.out.println("******************************");
	}
}
