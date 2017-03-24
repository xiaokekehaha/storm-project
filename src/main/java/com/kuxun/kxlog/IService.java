package com.kuxun.kxlog;

import java.io.Serializable;

import com.kuxun.kxlog.common.Input;
import com.kuxun.kxlog.common.KxException;
/**
 * 业务逻辑入口
 * 默认为单例模式,也即在一个JVM进程中，这个类是单例的
 * 以整个storm集群来看，可能存在多个类的实例，这种情况
 * 可以理解成多个JVM进程在类似的环境中并行地跑相同的业务
 * 区别于一个请求对应一个IService实例而言
 * 定义的IService有三个对应的生命周期：
 *   1. 初始化   2. 执行业务逻辑   3. 销毁该实例
 */
public interface IService extends Serializable{
     /**
      *  初始化方法 初始化时调用
      *  配置中包含参数 
      *  也可以往conf中设置所需要的变量
      *  在一个JVM中IServiceConfig 是单例的
      * @param conf
      * @throws KxException
      */
     public void init(IServiceConfig conf) throws KxException;
     /**
      * 入口方法,可以在本方法中实现你的具体逻辑
      * 可以在本方法中和数据库交互，管理多线程等等
      * 对于比较耗时间的操作，一种比较好的解决方法是
      * 开启另一个线程进行操作
      * @param input 获取框架生成的数据
      */
     public void service(Input input) throws KxException;
     
     /**
      * 销毁 实例
      */
     public void destroy() throws KxException;
     
}
