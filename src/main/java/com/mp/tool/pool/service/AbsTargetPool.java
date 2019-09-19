package com.mp.tool.pool.service;

import com.mp.tool.pool.model.TargetPoolResource;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbsTargetPool<T extends ITarget> implements ITargetPool<T> {

    protected TargetPoolResource resource;
    /** 空闲链接  **/
    protected volatile LinkedBlockingQueue<T> freeQueue;
    /** 使用链接  **/
    protected volatile LinkedBlockingQueue<T> busyQueue;
    /** 当前活动连接数 **/
    protected volatile AtomicInteger currentObjDeep;

    /**
     * 对象状态探查
     */
    protected void statusProbe(){
        if(resource.isProbe()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    timeoutProbe();
                    connIdleProbe();
                    printStatus();
                }
            }, resource.getScheduleDelay(), resource.getSchedulePeriod());
        }
    }

    protected void printStatus(){
        System.out.println("freeQueue size:" + freeQueue.size() + "   " + "busyQueue size:" + busyQueue.size());
    }

    /**
     * @description 对象使用超时状态探查 ...
     */
    protected void timeoutProbe(){
        try{
            if(busyQueue == null) {
                return;
            }
            while(true){
                //探查对象使用队列，对队列上锁，以免在探查期间有其他的出队操作
                synchronized(busyQueue){
                    if(busyQueue == null || busyQueue.isEmpty()) {
                        break;
                    }
                    if(checkSingleObjState(busyQueue.peek())) {
                        this.freeQueue.offer(busyQueue.poll());
                    } else {
                        break;
                    }
                }
            }
        } catch(Throwable e){

        }
    }

    protected abstract boolean checkSingleObjState(ITarget t) throws Throwable;

    /**
     *  链接空闲状态探查 ....
     *
     */
    protected void connIdleProbe(){
        try{
            if(freeQueue == null) {
                return;
            }
            freeNotUseObj();
        } catch(Throwable e){

        }
    }
    /**
     * 释放空闲链接
     */
    protected abstract void freeNotUseObj() throws Throwable;

    @Override
    public void initPool() {
        currentObjDeep = new AtomicInteger(0);
        this.freeQueue = new LinkedBlockingQueue<T>(resource.getMaxActive());
        this.busyQueue = new LinkedBlockingQueue<T>(resource.getMaxActive());
    }

    @Override
    public int getCurrentPoolObjDeep() {
        return this.currentObjDeep.get();
    }

    /**
     *
     */
    protected int getCurrentObjNum(){
        return this.freeQueue.size() + this.busyQueue.size();
    }

    @Override
    public T getTarget() throws Throwable {
        T t;
        synchronized (this) {
            if (freeQueue.size() == 0 && (this.currentObjDeep.get() < resource.getMaxActive())) {
                t = extend();
                if(t != null){
                    return t;
                }
            }
        }
        t = this.freeQueue.poll(resource.getPollMaxWait(), TimeUnit.MILLISECONDS);
        if (t == null) {
            throw new Exception("waiting timeout");
        }
        this.busyQueue.offer(t);
        return t;
    }
    @Override
    public void freeTarget(T t) {
        synchronized(busyQueue){
            this.busyQueue.remove(t);
        }
        this.freeQueue.offer(t);
    }

}
