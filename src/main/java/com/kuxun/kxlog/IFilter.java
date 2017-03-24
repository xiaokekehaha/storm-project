package com.kuxun.kxlog;

import com.kuxun.kxlog.common.Input;
import com.kuxun.kxlog.common.KxException;

/**
 * 过滤器
 * @author dengzh
 */
public interface IFilter {
   /**
   *  初始化IFilter
   * @param config
   * @throws KxException
   */
   public void init(IFilterConfig config) throws KxException;

   /**
    *
    * @param input
    * @param chain
    */
   public void doFilter(Input input,IFilterChain chain) throws KxException;
   
   public void destory() throws KxException;
}
