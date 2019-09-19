package com.mp.tool.pool.model;

import lombok.Data;

@Data
public class TargetPoolResource {

    private int scheduleDelay = 0;
    private long schedulePeriod = 10;
    //最大活跃数
    private int maxActive = 10;
    //阻塞队列出队最大等待时间
    private int pollMaxWait = 1000;
    //每次对象集合增量
    private int nextSize = 2;
    //对象使用超时时间
    private int useOutTime = 1000 * 10;
    //对象闲置最大时间
    private int maxStayTime = 1000 * 60;
    //外部线程是否允许中断
    private boolean isAllowInterrupt = true;
    //是否允许探查
    private boolean isProbe = true;
}
