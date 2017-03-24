package com.kuxun.kxtopology.xml;

/**
 * 定义xml文件中出现的节点名称
 */
public class XmlConfigNodeDefinition {
	public final static int SERVICENAME =0;
	
	public final static int LOGICSERVICE = 1;
	
	public final static int LOGPARSER = 2;
	
	public final static int FILTER = 3;
	
	public final static int DATASOURCE =4;
	
	public final static int LOG = 5;
	
	public final static String ROOTNODE = "kooxoo";

	public final static class ServiceName {
		public final static String ROOTNODE = "service-name";
	}

	public final static class LogParser {
		public final static String ROOTNODE = "parser";
		public final static String NAME = "name";
		public final static String CLASS = "class";
		public final static String SECONDNODE = "patterns";
		public final static String PATTERN = "pattern";
		public final static String PATTERNNAME = "name";
	}

	public final static class Filter {

		public final static String ROOTNODE = "filter";

		public final static String NAME = "name";

		public final static String CLASS = "class";
	}

	public final static class Service {

		public final static String ROOTNODE = "service";

		public final static String NAME = "name";

		public final static String CLASS = "class";

		public final static String SECONDNODE = "params";

		public final static String PARAMNAME = "name";

		public final static String PARAMVALUE = "value";

	}

	public final static class Spout {

		public final static String ROOTNODE = "spout";

		public final static String NAME = "name";

		public final static String CLASS = "class";

		public final static String PARALLELISM = "parallelism";

		public final static String SECONDNODE = "params";

		public final static String PARAMNAME = "name";

		public final static String PARAMVALUE = "value";

	}

	public final static class Log {
		public final static String ROOTNODE = "logger";
		
		public final static String LEVEL ="level";
		
		public final static String ENABLE = "enable";
		
		public final static String APPENDER = "appender";
	}
}
