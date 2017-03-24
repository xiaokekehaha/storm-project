package com.kuxun.kxlog.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxlog.Finishable;
import com.kuxun.kxlog.IFilter;
import com.kuxun.kxlog.IFilterChain;
import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.common.Input;
import com.kuxun.kxlog.common.KxException;

/**
 * 
 * @author dengzh
 */
public class CachedChain implements IFilterChain, Finishable {

	final static Logger logger = LoggerFactory.getLogger(CachedChain.class);

	private IFilter[] filters;

	private int next;

	private int len;

	private boolean finished;

	private String service;

	public CachedChain(IFilter[] filters, String service) {
		if (filters == null)
			return;
		len = filters.length + 1;
		this.filters = new IFilter[len];
		for (int i = 0; i < len - 1; i++) {
			this.filters[i] = filters[i];
		}
		this.filters[len - 1] = new LastFilterInChain();
		next = 0;
		finished = false;
		this.service = service;
	}

	@Override
	public void doFilter(Input programData) throws KxException {
		// TODO Auto-generated method stub
		if (next < len) {
			IFilter filter = filters[next++];
			if (logger.isInfoEnabled()) {
				logger.info("调用服务'" + service + "'的第" + (next - 1) + "个过滤器，过滤器为" + filter.getClass());
			}
			filter.doFilter(programData, this);
		}
		if (next == len)
			finished = true;
	}

	private class LastFilterInChain implements IFilter {
		@Override
		public void init(IFilterConfig config) {
			// TODO Auto-generated method stub

		}

		@Override
		public void doFilter(Input input, IFilterChain chain) throws KxException {
			// TODO Auto-generated method stub
			chain.doFilter(input);
		}

		@Override
		public void destory() {
			// TODO Auto-generated method stub
		}

	}

	@Override
	public boolean finished() {
		// TODO Auto-generated method stub
		return finished;
	}

}
