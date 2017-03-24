package com.kuxun.kxtopology.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * 采用 dom4j方式解析 读取配置信息,是整个sdk解析xml配置入口 
 *     解析完成后，通过getConfig()即可获取xml数据配置信息
 */
public class XmlConfigDefinitionReader {


	public XmlConfigDefinitionReader() {

	}

	public XmlConfigParserFactory construct(InputStream input) throws KxException {
		try {
			Document doc = createDocument(input);
			return new XmlConfigParserFactory(doc);
		} catch (Exception e) {
		//	e.printStackTrace();
			throw Utils.wrapperException(e, "调用construct()出现异常，异常信息为:"+e.getMessage());
		}
	}

	// 得到一份该jar的配置
	public JarConfig parse(byte[] configFile) throws KxException {
		try {
			XmlConfigParserFactory factory = construct(new ByteArrayInputStream(configFile));
			return JarConfigWrapper.wrap(factory);
		} catch (Exception e) {
			//e.printStackTrace();
			throw Utils.wrapperException(e, "调用parse(byte[])出现异常，异常信息为:"+e.getMessage());
		}

	}

	public JarConfig parse(File file) throws KxException {
		try {
			XmlConfigParserFactory factory = construct(new FileInputStream(file));
			return JarConfigWrapper.wrap(factory);
		} catch (Exception e) {
			//e.printStackTrace();
			throw Utils.wrapperException(e, "调用parse(File)时出现异常，异常信息为:"+e.getMessage());
		}
	}

	public JarConfig parse(InputStream is) throws KxException {
		try {
			XmlConfigParserFactory factory = construct(is);
			return JarConfigWrapper.wrap(factory);
		} catch (Exception e) {
			//e.printStackTrace();
            throw Utils.wrapperException(e, "调用parse(InputStream)时出现异常，异常信息为:"+e.getMessage());
		}
	}

	private Document createDocument(InputStream input) throws KxException {
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(input);
			return doc;
		} catch (Exception e) {
			//e.printStackTrace();
			throw Utils.wrapperException(e, "调用createDocument()时出现异常，异常信息为"+e.getMessage());
		}
	}
	
	
}
