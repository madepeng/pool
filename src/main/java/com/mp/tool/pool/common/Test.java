package com.mp.tool.pool.common;

import com.mp.tool.pool.model.TargetPoolResource;
import com.mp.tool.pool.service.ITarget;
import com.mp.tool.pool.service.impl.TargetImpl;
import com.mp.tool.pool.service.impl.TargetPoolImpl;

import java.util.Random;

public class Test {

    public static void main(String[] aa) throws Throwable{
        //对象池参数设置
        TargetPoolResource resource = new TargetPoolResource();
        resource.setMaxActive(10);
        resource.setMaxStayTime(1000);
        resource.setNextSize(2);
        resource.setPollMaxWait(1);
        resource.setUseOutTime(1000);
        resource.setScheduleDelay(0);
        resource.setSchedulePeriod(50);
        resource.setAllowInterrupt(true);
        //对象池初始化
        final TargetPoolImpl pool = new TargetPoolImpl(HH.class,resource);

        for(int x = 0; x<30; x++) {
            Thread.sleep(10 + new Random().nextInt(10));
            final int finalX = x;
            new Thread() {
                @Override
                public void run() {
                    TargetImpl target = null;
                    try {
                        //从池中获取对象
                        target = (TargetImpl) pool.getTarget();
                        HH h = (HH)target.getObj();
                        h.value -= 1;
                        System.out.println("开始 X:"+finalX +"   ID:" + target.getTargetId() + "   value:" + ((HH) target.getObj()).value);
                        Thread.sleep(100 + new Random().nextInt(100));
                    } catch (Throwable e) {
                        System.out.println("[异常] X:"+finalX +"   "+e.getMessage());
                    } finally {
                        if(target!=null) {
                            //将对象归还池中
                            pool.freeTarget(target);
                        }
                        System.out.println("结束 X:"+finalX);
                    }

                }
            }.start();
        }
    }
}
