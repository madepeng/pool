package com.mp.tool.pool.common;

import com.mp.tool.pool.service.IObj;

public class HH implements IObj {
    int value;
    @Override
    public void destroy() {
        value = -1;
        System.out.println("释放："+value);
    }

    public HH(){
        value = 100;
        System.out.println("初始化："+value);
    }

}
