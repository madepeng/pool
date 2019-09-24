package com.mp.tool.pool.service.impl;

import com.mp.tool.pool.model.TargetPoolResource;
import com.mp.tool.pool.service.AbsTargetPool;
import com.mp.tool.pool.service.IObj;
import com.mp.tool.pool.service.ITarget;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

    public class TargetPoolImpl<T extends ITarget> extends AbsTargetPool<ITarget> {
    private Class c;

    @Override
    protected boolean checkSingleObjState(ITarget t) throws Throwable {
        TargetImpl tc = (TargetImpl) t;
        //是否超时使用
        if(tc.getOutPoolTime() == null || System.currentTimeMillis() - tc.getOutPoolTime().getTime() < resource.getUseOutTime()) {
            return false;
        }
        //已经超时了
        try{
            //入池，终断对象所在的执行线程
            tc.setInPoolTime(new Date(), resource.isAllowInterrupt());
        }catch(Throwable e){
            //对象执行线程中断过程异常，放弃该对象的管理
            this.busyQueue.poll();
            this.currentObjDeep.decrementAndGet();
            return false;
        }

        if(!resource.isAllowInterrupt()) {
            //不允许中断，放弃该对象的管理
            this.busyQueue.poll();
            this.currentObjDeep.decrementAndGet();
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void freeNotUseObj() throws Throwable {
        List<ITarget> ts = new ArrayList<ITarget>();
        int limit = (resource.getMaxActive()/2)+1;
        if(this.freeQueue.size() <= limit)
            return;
        int num = 0;
        for(Iterator<ITarget> it = this.freeQueue.iterator(); it.hasNext();){
            TargetImpl tc = (TargetImpl) it.next();
            if(tc.getInPoolTime() != null && System.currentTimeMillis() - tc.getInPoolTime().getTime() > resource.getMaxStayTime()){
                ts.add(tc);
                num ++;
                if((this.freeQueue.size() - num) <= limit) {
                    break;
                }
            }
        }
        if(ts.size() <= 0)  {
            return ;
        }
        for(ITarget t: ts){
            freeQueue.remove(t);
            this.currentObjDeep.decrementAndGet();
            t.destroy();
        }
    }

    @Override
    public void removeTarget(ITarget t) throws Throwable {
        t.destroy();
    }

    @Override
    public void destroyPool() throws Throwable {
        if(freeQueue != null)  {
            for(ITarget t:this.freeQueue) {
                t.destroy();
            }
            freeQueue.clear();
        }
        if(busyQueue != null){
            for(ITarget t:this.busyQueue){
                t.destroy();
            }
            busyQueue.clear();
        }
        currentObjDeep = null;
    }

    @Override
    public ITarget extend() throws Throwable {
        if(this.currentObjDeep.get() + resource.getNextSize() <= resource.getMaxActive()){
            for(int i = 0; i< resource.getNextSize() - 1; i++){
                this.currentObjDeep.incrementAndGet();
                freeQueue.offer(new TargetImpl(this,this.c));
            }
            return createOneBusy();
        }else if(this.currentObjDeep.get() + resource.getNextSize() > resource.getMaxActive() && this.currentObjDeep.get() < resource.getMaxActive()){
            for(int i = 0; i< (resource.getMaxActive() - this.getCurrentObjNum())-1; i++ ){
                this.currentObjDeep.incrementAndGet();
                freeQueue.offer(new TargetImpl(this,this.c));
            }
            return createOneBusy();
        }else{
            return null;
        }
    }


    public synchronized void initPool(Class c) throws Throwable {
        super.initPool();
        this.c = c;
        int initSize = resource.getMaxActive() / 2 + 1;
        for (int i = 0; i < initSize; i++) {
            freeQueue.offer(new TargetImpl(this,c));
            this.currentObjDeep.incrementAndGet();
        }
    }

    public TargetPoolImpl(Class c, TargetPoolResource resource) throws Throwable {
        this.resource = resource;
        initPool(c);
        statusProbe();
    }

    private ITarget createOneBusy() throws Throwable{
        ITarget t = new TargetImpl(this,this.c);
        this.currentObjDeep.incrementAndGet();
        this.busyQueue.offer(((TargetImpl)t).setOutPoolTime(new Date()));
        return t;
    }
}
