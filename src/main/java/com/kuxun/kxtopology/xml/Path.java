package com.kuxun.kxtopology.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
/**
 * xpath  路径
 */
public class Path {  
    private List nodeList = new ArrayList();  
    public Path() {  
    }  
    public Path(String xpath) {  
      StringTokenizer parser = new StringTokenizer(xpath, "/", false);  
      while (parser.hasMoreTokens()) {  
        nodeList.add(parser.nextToken());  
      }  
    }  
    public void add(String node) {  
      nodeList.add(node);  
    }  
    
    public void remove() {  
      nodeList.remove(nodeList.size() - 1);  
    }  
    
    public String toString() {  
      StringBuffer buffer = new StringBuffer("");  
      for (int i = 0; i < nodeList.size(); i++) {  
        buffer.append(nodeList.get(i));  
        if (i < nodeList.size() - 1) {  
          buffer.append("/");  
        }  
      }  
      return buffer.toString();  
    }  
  }  