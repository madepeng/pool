package com.mp.tool.pool.service;

public interface ITarget<T> {

    /**
     * 获得对象
     * @return
     */
    T getObj();

    /**
     * 释放对象
     */
    void destroy() ;

}
