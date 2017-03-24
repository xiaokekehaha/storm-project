package com.kuxun.kxtopology.jarservice;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

/**
 * 封装jar文件一些可用的基本信息
 * 
 * @author dengzh
 *
 */
public class Jar implements Serializable {
	/**
	 * jar 可用
	 */
	public final static int USABLE = 0;
	/**
	 * jar 删除
	 */
	public final static int DELETE = 1;
	/**
	 * jar 废弃
	 */
	public final static int DEPROCATED = 2;

	/**
	 * jar 标识
	 */
	private int id;
	/**
	 * jar 文件对应的服务名称
	 */
	private String service;
	/**
	 * jar文件对应的地址
	 */
	private transient String path;

	/**
	 * jar 文件对应的状态
	 */
	private int status;

	/**
	 * jar 文件对应的版本号
	 */

	private int version;

	public int getId() {
		return id;
	}

	public String getService() {
		return service;
	}

	public String getPath() {
		return path;
	}

	public int getStatus() {
		return status;
	}

	public int getVersion() {
		return version;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * 将jar实例转化成json串
	 */
	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof Jar) {
			Jar other = (Jar) obj;
			return service.equals(other.getService());
		}
		return false;
	}

}
