package com.kuxun.kxtopology.resources;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.jarservice.IJarManager;
import com.kuxun.kxtopology.jarservice.JarManagerImpl;
/**
 * 读取sys.properties配置文件
 * @author dengzh
 */
public class Resources implements IResourceObtainer {
	public final static String JARSERVER = "jar.server";
	public final static String JARCHANGEDLIST = "jar.changed.list";
	public final static String JARRESOURCE = "jar.resource";
	public final static String JARGET ="jar.get";
	
	private final static Logger logger = LoggerFactory.getLogger(Resources.class);
	private final static Resources resources = new Resources();

	private Resources() {
		bundle = ResourceBundle.getBundle("sys");
		if (bundle == null) {
			throw new RuntimeException("lose sys.properties");
		}
	}

	private ResourceBundle bundle;

	public String getResourceValue(String key) {
		// TODO Auto-generated method stub
		return bundle.getString(key);
	}

	public static Resources getSingleInstance() {
		return resources;
	}

	@Override
	public String getJarChangedListAddr(String... params) {
		StringBuffer buffer = new StringBuffer(getResourceValue(JARSERVER));
		return MessageFormat.format(new String(buffer.append(getResourceValue(JARCHANGEDLIST))), params);
	}

	@Override
	public String getJarResourceAddr(String... params) {
		StringBuffer buffer = new StringBuffer(getResourceValue(JARSERVER));
		return MessageFormat.format(new String(buffer.append(getResourceValue(JARRESOURCE))), params);
	}

	@Override
	public IJarManager getJarManager() {
		// TODO Auto-generated method stub
	    String value = getResourceValue(JARGET);
	    // 默认返回包含有文件服务
	    if(!Utils.isNotBlank(value))
	    	      return new JarManagerImpl();
	    
	    IJarManager jarManager = null;
	    try {
			Class<?> clazz = Class.forName(value);
			if(IJarManager.class.isAssignableFrom(clazz)){
				   jarManager =  (IJarManager) clazz.newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    if(jarManager==null)
	    	  jarManager = new JarManagerImpl();
	    
		return jarManager;
	}
	
	
}
