package com.kuxun.kxtopology.xml;

import org.dom4j.Node;

import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.common.KxException;
/**
 * 解析 filter
 *  <filter name="" class ="" />
 *  <filter name="" class =""/>
 */
public class FilterConf implements Nodelet<IFilterConfig> {

	public IFilterConfig process(Path path,Node node) throws KxException {
		final String thisFilterName = node.valueOf("@name");
		final String thisFilterClass = node.valueOf("@class");
		
		return new IFilterConfig() {
			public String getThisFilterName() {
				// TODO Auto-generated method stub
				return thisFilterName;
			}
			
			public String getThisFilterClassName() {
				// TODO Auto-generated method stub
				return thisFilterClass;
			}
		};
	}
	
}
