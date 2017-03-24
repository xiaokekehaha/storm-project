package com.kuxun.kxlog.common;

import com.kuxun.kxlog.IService;
import com.kuxun.kxlog.IServiceConfig;

public abstract class AbsService implements IService {
	
    private IServiceConfig serviceConf;
	@Override
	public final void init(IServiceConfig conf)  {
		// TODO Auto-generated method stub
          serviceConf = conf;
          initService(serviceConf);
	}

	public abstract void initService(IServiceConfig conf);
	
	@Override
	public void destroy() throws KxException{
		// TODO Auto-generated method stub
          serviceConf.destory();
	}

	public String getParamByName(String name) {
		Object obj = serviceConf.getInitialValue(name);
		if(obj == null)
			return null;
		return String.valueOf(obj).trim();
	}
}
