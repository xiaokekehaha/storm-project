package com.kuxun.kxtopology.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Node;

import com.kuxun.kxlog.IParserConfig;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.Utils;
/**
 *  解析 日志解析器
 *  <parser name=" " class ="">
 *     <patterns>
 *        <pattern name=" " > </pattern>
 *      </patterns>
 *   </parser>   
 * 
 */
public class ParserConf implements Nodelet<IParserConfig> {


	@Override
	public IParserConfig process(Path path, Node node) throws KxException {
		// TODO Auto-generated method stub
		final String thisParserName = node.valueOf("@name");
		final String thisParserClass = node.valueOf("@class");
		path.add("patterns");
        path.add("pattern");	
        List<Node> nodes =  node.getDocument().selectNodes(path.toString());
        final Map<String,Object>  params = new HashMap<String,Object>();
        for(Node n:nodes){
        	  String thisPatternName =  n.valueOf("@name");
        	  String thisPattern = n.getText();
        	  if(!Utils.isNotBlank(thisPatternName)){
        		  thisPatternName = IParserConfig.DEFAULTPATTERN;
        	  }
        	  params.put(thisPatternName, thisPattern);
        }
        
        path.remove();
        path.remove();
		return new IParserConfig() {
			
		    private Map<String,Object> getParserContainer(){
		    	  return params;
		    }  
		    @Override
		    public void destory(){
		    	getParserContainer().clear();
		    }
		    @Override
		    public Object getInitialValue(String key){
		    	return getParserContainer().get(key);
		    }
			@Override
			public String getThisParserName() {
				// TODO Auto-generated method stub
				return thisParserName;
			}
			
			@Override
			public String getThisLogPatternByName(String name) {
				return String.valueOf(getInitialValue(name));
			}
			
			@Override
			public String getThisLogParserClassName() {
				// TODO Auto-generated method stub
				return thisParserClass;
			}

			@Override
			public void setInitialValue(String key, Object obj) {
				getParserContainer().put(key, obj);
			}

			@Override
			public Object getValue(String key) {
				// TODO Auto-generated method stub
				return getParserContainer().get(key);
			}
		};
	}
}
