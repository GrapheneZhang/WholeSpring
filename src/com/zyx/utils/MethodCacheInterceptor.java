package com.zyx.utils;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 方法缓存拦截器
 * 
 * @author ZhangYaxu
 * @date Mar 27, 2015
 */
public class MethodCacheInterceptor implements MethodInterceptor,InitializingBean {

	private static final Logger log = Logger.getLogger(MethodCacheInterceptor.class);
	
    private Cache cache;
 
    public void setCache(Cache cache) {
        this.cache = cache;
    }
 
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(cache + " A cache is required. Use setCache(Cache) to provide one.");
    }
 
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String targetName = invocation.getThis().getClass().getName();
        String methodName = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        Object result;
 
        String cacheKey = getCacheKey(targetName, methodName, arguments);
        Element element = null;
        synchronized (this) {
            element = cache.get(cacheKey);
            if (element == null) {
                log.info(cacheKey + "加入到缓存： " + cache.getName());
                result = invocation.proceed(); // 调用实际的方法
                element = new Element(cacheKey, (Serializable) result);
                cache.put(element);
            } else {
                log.info(cacheKey + "使用缓存： " + cache.getName());
            }
        }
        return element.getValue();
    }
 
    /**
     * <b>function:</b> 返回具体的方法全路径名称 参数
     * @param targetName 全路径
     * @param methodName 方法名称
     * @param arguments 参数
     * @return 完整方法名称
     * @author ZhangYaxu
	 * @date Mar 27, 2015
     */
    private String getCacheKey(String targetName, String methodName, Object[] arguments) {
        StringBuffer sb = new StringBuffer();
        sb.append(targetName).append(".").append(methodName);
        if ((arguments != null) && (arguments.length != 0)) {
            for (int i = 0; i < arguments.length; i++) {
                sb.append(".").append(arguments[i]);
            }
        }
        return sb.toString();
    }
}