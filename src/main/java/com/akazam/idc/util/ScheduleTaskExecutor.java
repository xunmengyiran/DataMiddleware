package com.akazam.idc.util;

import com.akazam.idc.constants.Constants;
import com.akazam.idc.constants.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务执行类
 */
@Component
public class ScheduleTaskExecutor {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private List<ScheduledFuture<?>> oracleTaskFutureList = new ArrayList<>();

    private List<ScheduledFuture<?>> mysqlTaskFutureList = new ArrayList<>();

    private List<ScheduledFuture<?>> exportTaskFutureList = new ArrayList<>();

    public void startCron(Runnable task, String cronExp, String dataSource) throws Exception{
        System.out.println("cronExp:"+cronExp);
        ScheduledFuture<?> future =
                threadPoolTaskScheduler.schedule(task, new CronTrigger(cronExp));
        if (null == dataSource) {
            exportTaskFutureList.add(future);
        } else if (dataSource.equals(Constants.DataSource_Oracle)) {
            oracleTaskFutureList.add(future);
        } else {
            mysqlTaskFutureList.add(future);
        }
    }
    
    public void stopAllCron(){
    	 for (ScheduledFuture<?> future : exportTaskFutureList) {
             future.cancel(true);
         }
         for (ScheduledFuture<?> future : oracleTaskFutureList) {
             future.cancel(true);
         }
         for (ScheduledFuture<?> future : mysqlTaskFutureList) {
             future.cancel(true);
         }
         exportTaskFutureList.clear();
         oracleTaskFutureList.clear();
         mysqlTaskFutureList.clear();
    }

    public void stopCron(String dataSource) {
        if ((null == dataSource) && !exportTaskFutureList.isEmpty()) {
            for (ScheduledFuture<?> future : exportTaskFutureList) {
                future.cancel(true);
            }
            exportTaskFutureList.clear();
        } else if ((null != dataSource) && dataSource.equals(Constants.DataSource_Oracle) && !oracleTaskFutureList.isEmpty()) {
            for (ScheduledFuture<?> future : oracleTaskFutureList) {
                future.cancel(true);
            }
            oracleTaskFutureList.clear();
        } else if ((null != dataSource) && dataSource.equals(Constants.DataSource_MySql) && !mysqlTaskFutureList.isEmpty()) {
            for (ScheduledFuture<?> future : mysqlTaskFutureList) {
                future.cancel(true);
            }
            mysqlTaskFutureList.clear();
        }
    }
}
