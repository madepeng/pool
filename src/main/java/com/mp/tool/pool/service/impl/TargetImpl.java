package com.mp.tool.pool.service.impl;

import com.mp.tool.pool.service.AbsTarget;
import com.mp.tool.pool.service.AbsTargetPool;
import com.mp.tool.pool.service.ITarget;
import java.util.Date;
import java.util.UUID;

public class TargetImpl<T> extends AbsTarget<T> {

    @Override
    public T getObj(){
        return this.getTargetObj();
    }

    public TargetImpl(AbsTargetPool<ITarget> pool,Class<T> tClass) throws Throwable {
        this.setTargetId(UUID.randomUUID().toString().replace("-", ""));
        System.out.println("创建新对象 ID:"+this.getTargetId());
        this.setTargetPool(pool);
        this.setTargetObj(tClass.newInstance());
        this.setInPoolTime(new Date(), false);
    }

}
