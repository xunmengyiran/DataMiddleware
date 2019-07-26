package com.akazam.idc.aop;

import com.akazam.idc.constants.Constants;
import com.akazam.idc.constants.WebSecurityConfig;
import com.akazam.idc.service.DataMiddlewareService;
import com.akazam.idc.vo.SysLogVO;
import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;


@Aspect
@Component
public class SysLogService {

    private Logger log = LoggerFactory.getLogger(SysLogService.class);

    @Autowired
    private DataMiddlewareService dataMiddlewareService;

    @Pointcut("@annotation(com.akazam.idc.annotation.SysLog)")
    public void serviceAspect() {
        log.info("serviceAspect...");
    }

    @After("serviceAspect()")
    public void after(JoinPoint joinPoint){
        log.info("after=》开始记录日志");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        com.akazam.idc.annotation.SysLog sysLog = method.getAnnotation(com.akazam.idc.annotation.SysLog.class);
        String operationName = sysLog.value();
        log.info("操作名:" + operationName);
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        log.info("类名.方法名:" + className + "." + methodName);
        Object[] args = joinPoint.getArgs();
        String params = "";
        try {
            params = JSONArray.fromObject(args).toString();
        }catch (Exception e){
            for(Object arg: args){
                params = params+arg+",";
            }
        }

        log.info("参数:" + params);
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("==servletRequestAttributes==" + servletRequestAttributes);
        String userName = "定时任务运行";
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            userName = (String) request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY);
        }
        log.info("操作用户名" + userName);
        SysLogVO sysLogVO = new SysLogVO();
        sysLogVO.setOperationUserName(userName == null ? "未登录" : userName);
        sysLogVO.setOperationDate(Constants.DataFormat.sdf2.format(new Date()));
        sysLogVO.setOperationMethod(className + "." + methodName);
        sysLogVO.setOperationName(operationName);
        sysLogVO.setOperationParams(params);
        log.info("====>" + sysLogVO.toString());
        try {
            dataMiddlewareService.saveSysLog(sysLogVO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
