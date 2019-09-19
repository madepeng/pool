package com.mp.tool.pool.service;

public interface ITargetPool<T> {
    /**
     * 初始化对接尺
     * @throws Throwable
     */
    void initPool() throws Throwable;

    /**
     * 获得池中的对象
     * @return
     * @throws Throwable
     */
    T getTarget()  throws Throwable;

    /**
     * 当前池中对象的数量
     * @return
     * @throws Throwable
     */
    int getCurrentPoolObjDeep();

    /**
     * 将对象从池中移除
     * @param t
     * @throws Throwable
     */
    void removeTarget(T t) throws Throwable;

    /**
     * 释放对象池
     * @throws Throwable
     */
    void destroyPool()  throws Throwable;

    /**
     * 扩充池中对象
     * @return
     * @throws Throwable
     */
    T extend()  throws Throwable;

    /**
     * 释放一个对象
     * @param t
     */
    void freeTarget(T t);


}
