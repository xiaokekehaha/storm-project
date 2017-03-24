package com.kuxun.kxtopology.topology;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

/**
 * 执行情况
 * @author dengzh
 */
public class ExecutorInf implements Serializable{

	/**
	 * 成功数
	 */
    private long success;
    /**
     * 失败数
     */
    private long fail;
    

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return JSONObject.toJSONString(this);
	}

	public long getSuccess() {
		return success;
	}

	public void setSuccess(long success) {
		this.success = success;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

}
