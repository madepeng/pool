package com.mp.tool.pool.service;

import lombok.Data;

import java.util.Date;

@Data
public abstract class AbsTarget<T> implements ITarget<T> {
    /**
     * 对象ID
     */
    private String targetId;
    /**
     * 出池时间
     */
    private Date outPoolTime;
    /**
     * 入池时间
     */
    private Date inPoolTime;
    /**
     * 对象
     */
    private T targetObj;
    /**
     * 当前操作线程
     */
    private Thread currThread;
    /**
     * 关联连接池
     */
    private ITargetPool<ITarget> targetPool;

    /**
     * 【对象使用完成，放回对象池】设置入池时间
     * @param inPoolTime 入池时间
     * @param isInterrupt Timer线程检查是会触发中断对象所在执行线程
     * @return
     */
    public AbsTarget<T> setInPoolTime(Date inPoolTime,boolean isInterrupt) {
        this.inPoolTime = inPoolTime;
        this.outPoolTime = null;
        if(currThread != null && isInterrupt) {
            this.currThread.interrupt();
        }
        this.currThread = null;
        return this;
    }

    /**
     * 【对象开始被使用，从对象池中拿走】设置出池时间
     * @param outPoolTime
     * @return
     */
    public AbsTarget<T> setOutPoolTime(Date outPoolTime) {
        this.outPoolTime = outPoolTime;
        this.inPoolTime = null;
        this.currThread = Thread.currentThread();
        return this;
    }

    /**
     * 将对象消亡
     * @throws Throwable
     */
    @Override
    public void destroy() {
        if(targetObj != null && targetObj instanceof IObj){
            ((IObj) targetObj).destroy();
        }
        targetObj = null;
    }
}
