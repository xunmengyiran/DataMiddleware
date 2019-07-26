package com.akazam.idc.controller;

import com.akazam.idc.constants.WebSecurityConfig;
import com.akazam.idc.model.ExportTaskConfig;
import com.akazam.idc.model.ImportTaskConfig;
import com.akazam.idc.model.ShellTaskConfig;
import com.akazam.idc.service.DataMiddlewareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class DataMiddlewareController {
	
	 @Autowired
	 private DataMiddlewareService dataMiddlewareService;

    /**
     * @return 跳转到SQL导入配置页面
     */
    @RequestMapping("/sqlImport/index")
    public String sqlImportPage(HttpServletRequest request) {
    	request.getSession().removeAttribute("importTaskConfig");
        return "sql_import";
    }

    /**
     * 跳转到EXCEL导入页面
     * @param request
     * @return
     */
    @RequestMapping("/excelImport/index")
    public String excelImportPage(HttpServletRequest request) {
        return "excel_import";
    }
    
    /**
     * @return 跳转到导出配置页面
     */
    @RequestMapping("/export/index")
    public String exportPage(HttpServletRequest request) {
    	request.getSession().removeAttribute("exportTaskConfig");
        return "export";
    }
    
    /**
     * @return 跳转到定时shell配置页面
     */
    @RequestMapping("/shell/index")
    public String shellPage(HttpServletRequest request) {
    	request.getSession().removeAttribute("shellTaskConfig");
        return "shell";
    }
    

    /**
     * @return 跳转到导出配置页面
     */
    @RequestMapping("/query/index")
    public String queryPage() {
        return "query";
    }

    /**
     * @return 跳转到登录页面
     */
    @RequestMapping("/main")
    public String indexPage() {
        return "main";
    }

    /**
     * @return 跳转到登录页面
     */
    @RequestMapping("/shel1")
    public String shell1Index() {
        return "shell";
    }

    @RequestMapping("/query")
    public String query1Index() {
        return "query";
    }

    @RequestMapping("/sqlImport")
    public String sqlImport() {
        return "sql_import";
    }
    @RequestMapping("/excelImport")
    public String excelImport() {
        return "excel_import";
    }

    @RequestMapping("/export2Excel")
    public String export() {
        return "export";
    }

    @RequestMapping("/operationLog")
    public String operationLog(){
        return "operation_log";
    }
    /**
     * @return 跳转到登录页面
     */
    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 登出
     *
     * @return 登出系统
     */
    @RequestMapping(path = {"/logout"})
    public String logout(HttpSession session) {
        // 移除session
        session.removeAttribute(WebSecurityConfig.SESSION_KEY);
        return "redirect:/login";
    }
    
    @RequestMapping("/import/edit")
    public String importEditPage(@RequestParam String id,HttpServletRequest request) {
    	ImportTaskConfig config = dataMiddlewareService.loadImportTaskConfigByID(id);
    	request.getSession().setAttribute("importTaskConfig", config);
        return "sql_import";
    }
    
    @RequestMapping("/export/edit")
    public String exportEditPage(@RequestParam String id,HttpServletRequest request) {
    	ExportTaskConfig config  = dataMiddlewareService.loadExportTaskConfigByID(id);
    	request.getSession().setAttribute("exportTaskConfig", config);
        return "export";
    }
    
    @RequestMapping("/shell/edit")
    public String shellEditPage(@RequestParam String id,HttpServletRequest request) {
    	ShellTaskConfig config  = dataMiddlewareService.loadShellTaskConfigByID(id);
    	request.getSession().setAttribute("shellTaskConfig", config);
        return "shell";
    }
}
