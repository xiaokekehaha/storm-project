package com.kuxun.kxlog;

/**
 * 判断调用类是否已经结束
 * 当判断一个类是否已经完成某件事情时，
 * 可以实现该接口的类
 * @author dengzh
 */
public interface Finishable {

	/**
	 * @return 完成某件事情时返回true, 否则， 返回false
	 */
	public boolean finished();
	
}
