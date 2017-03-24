package com.kuxun.kxtopology.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.DefaultNestedComponentRules;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class MyJoranConfigrator extends SiftingJoranConfiguratorBase {
	  
	  @Override
	  protected Pattern initialPattern() {
	    return new Pattern("");
	  }
	  
	  @Override
	  protected void addInstanceRules(RuleStore rs) {
	    rs.addRule(new Pattern("appender"), new AppenderAction());
	  }


	  @Override
	  protected void addDefaultNestedComponentRegistryRules(
	      DefaultNestedComponentRegistry registry) {
	    DefaultNestedComponentRules.addDefaultNestedComponentRegistryRules(registry);
	  }
	    
	  @Override
	  protected void buildInterpreter() {
	    super.buildInterpreter();
	    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
	    omap.put(ActionConst.APPENDER_BAG, new HashMap());
	    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
	    Map<String, String> propertiesMap = new HashMap<String, String>();
	    //propertiesMap.put(key, value);
	    interpreter.setInterpretationContextPropertiesMap(propertiesMap);
	  }

	  @SuppressWarnings("unchecked")
	  public Appender<ILoggingEvent> getAppender() {
	    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
	    HashMap appenderMap = (HashMap) omap.get(ActionConst.APPENDER_BAG);
	    oneAndOnlyOneCheck(appenderMap);
	    Collection values = appenderMap.values();
	    if(values.size() == 0) {
	      return null;
	    }
	    return (Appender<ILoggingEvent>) values.iterator().next();
	  }
}
