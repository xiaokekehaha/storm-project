package com.kuxun.kxtopology.xml;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Node;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.impl.IServiceLoggerConf;

/**
 * 解析service日志配置
 * 
 *       <logger> 
 *          <enable>true|false</enable>
 *          <level> </level>
 *          <appender> </appender> 
 *        </logger>
 */
public class LoggerConf implements Nodelet<IServiceLoggerConf> {

	static final Map<String, Level> LEVELS = new HashMap<String, Level>();

	static {
		LEVELS.put("all", Level.ALL);
		LEVELS.put("debug", Level.DEBUG);
		LEVELS.put("error", Level.ERROR);
		LEVELS.put("info", Level.INFO);
		LEVELS.put("trace", Level.TRACE);
		LEVELS.put("warn", Level.WARN);
		LEVELS.put("off", Level.OFF);
	}

	@Override
	public IServiceLoggerConf process(Path path, Node node) throws KxException {
		// TODO Auto-generated method stub

		boolean enable = true;
		path.add(XmlConfigNodeDefinition.Log.ENABLE);
		Node enableNode = node.getDocument().selectSingleNode(path.toString());
		String text = null;
		if (enableNode != null) {
               text = enableNode.getText();
               enable =  Boolean.parseBoolean(text);
		}
		path.remove();

		final boolean logEnable = enable;
		
		Level tmp = null;
		path.add(XmlConfigNodeDefinition.Log.LEVEL);
		Node levelNode = node.getDocument().selectSingleNode(path.toString());
		if(levelNode!=null){
			text = levelNode.getText().toLowerCase();
			tmp = LEVELS.get(text);
		}
		path.remove();
		
		if(tmp==null){
			tmp = Level.INFO;
		}
		
		final Level level = tmp;
		
		Appender appender = null;
		path.add(XmlConfigNodeDefinition.Log.APPENDER);
		Node appenderNode = node.getDocument().selectSingleNode(path.toString());
		if(appenderNode!=null){
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		   try {
		        String appenderXML = appenderNode.asXML();
				GenericConfigurator configurator = new MyJoranConfigrator();
				configurator.setContext(context);
				configurator.doConfigure(new ByteArrayInputStream(appenderXML.getBytes()));
				appender = ((MyJoranConfigrator)configurator).getAppender();
			} catch (JoranException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		   StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}
		path.remove();
		
		final Appender retAppender = appender;
		
		return new IServiceLoggerConf() {
			
			@Override
			public Level getLevel() {
				// TODO Auto-generated method stub
				return level;
			}
			
			@Override
			public Appender getAppender() {
				// TODO Auto-generated method stub
				return retAppender;
			}
			
			@Override
			public boolean enable() {
				// TODO Auto-generated method stub
				return logEnable;
			}
		};
	}

}
