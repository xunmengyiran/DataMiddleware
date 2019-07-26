package com.akazam.idc.controller;

import com.akazam.idc.annotation.SysLog;
import com.akazam.idc.constants.ConfigureProperties;
import com.akazam.idc.constants.Constants;
import com.akazam.idc.constants.WebSecurityConfig;
import com.akazam.idc.model.ExportTaskConfig;
import com.akazam.idc.model.ImportTaskConfig;
import com.akazam.idc.model.ShellTaskConfig;
import com.akazam.idc.service.DataMiddlewareService;
import com.akazam.idc.util.DateHelper;
import com.akazam.idc.util.FileUtils;
import com.akazam.idc.util.RandomValidateCodeUtil;
import com.akazam.idc.vo.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class DataRestController {

    private static final Logger logger = LoggerFactory.getLogger(DataRestController.class);
    
    @Autowired
    private DataMiddlewareService dataMiddlewareService;

    @Autowired
    private ConfigureProperties properties;

    /**
     * 编辑或者新增导入任务
     * @param importConfigRequest 导入任务请求体
     * @return 请求结果
     */
    @PostMapping(path = {"/import"})
    public JSONObject configImportTask(@RequestBody ImportConfigRequest importConfigRequest,HttpServletRequest request) {
        logger.info("configImportTask start:{}", importConfigRequest);
        importConfigRequest.setLastUpdater(DateHelper.currentDatetime());
        importConfigRequest.setLastUpdater(request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString());
        JSONObject jsonObject = new JSONObject();
        importConfigRequest.setSchema(importConfigRequest.getUrl().substring(importConfigRequest.getUrl().lastIndexOf(":")+1));
        if(importConfigRequest.getId() != 0 ){
            jsonObject = dataMiddlewareService.updateConfigImport(importConfigRequest);
        }else{
            jsonObject = dataMiddlewareService.configImport(importConfigRequest);//插入该条记录
        }
        if (jsonObject.getString("status").equals("1")){  //配置完后即开始执行定时任务
            String msg = dataMiddlewareService.executeImportTask(importConfigRequest);
           // dataMiddlewareService.executeImportTask(DataSource.EXCEL);
            jsonObject.put("msg",jsonObject.getString("msg")+" "+msg);
        }
        logger.info("===jsonObject==="+jsonObject);
        return jsonObject;
    }
    
    /**
     * 编辑或者新增导出任务
     * @param exportConfigRequest 导入任务请求体
     * @return 请求结果
     */
    @PostMapping(path = {"/export"})
    public String configExportTask(@RequestBody ExportConfigRequest exportConfigRequest,HttpServletRequest request) {
        logger.info("configExportTask start:{}", exportConfigRequest);
        exportConfigRequest.setLasttime(DateHelper.currentDatetime());
        exportConfigRequest.setLastUpdater(request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString());
        String configResult = null;
        exportConfigRequest.setSchema(exportConfigRequest.getUrl().substring(exportConfigRequest.getUrl().lastIndexOf(":")+1));
        if(exportConfigRequest.getId() != 0 ){
        	configResult = dataMiddlewareService.updateConfigExport(exportConfigRequest);
        }else{
        	configResult = dataMiddlewareService.configExport(exportConfigRequest);//插入该条记录
        }
        if (configResult.equalsIgnoreCase(Constants.SUCCESS)){  //配置完后即开始执行定时任务
           // dataMiddlewareService.executeImportTask(importConfigRequest);
           // dataMiddlewareService.executeImportTask(DataSource.EXCEL);
        }
        return configResult;
    }

    /**
     * 编辑或者新增shell任务
     * @param shellTaskConfig
     * @param request
     * @return
     */
    @PostMapping(path = {"/shell"})
    public String configShellTask(@RequestBody ShellTaskConfig shellTaskConfig,HttpServletRequest request) {
        logger.info("shellTaskConfig start:{}", shellTaskConfig);

        shellTaskConfig.setLasttime(DateHelper.currentDatetime());
        shellTaskConfig.setLastUpdater(request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString());
        String configResult = null;
        if(shellTaskConfig.getId() != 0 ){
        	configResult = dataMiddlewareService.updateConfigShell(shellTaskConfig);
        }else{
        	configResult = dataMiddlewareService.configShell(shellTaskConfig);//插入该条记录
        }
        if (configResult.equalsIgnoreCase(Constants.SUCCESS)){  //配置完后即开始执行定时任务
           // dataMiddlewareService.executeImportTask(importConfigRequest);
           // dataMiddlewareService.executeImportTask(DataSource.EXCEL);
        }
        return configResult;
    }
   /* public String configShellTask(@RequestParam("frequency") String frequency, @RequestParam("shellText") String shellText, @RequestParam("label") String label, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        logger.info("shellTaskConfig start:{}", "configShellTask");
        ShellTaskConfig shellTaskConfig = new ShellTaskConfig();
        shellTaskConfig.setLasttime(DateHelper.currentDatetime());
        shellTaskConfig.setLastUpdater(request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString());
        String configResult = null;
        if (shellTaskConfig.getId() != 0) {
            configResult = dataMiddlewareService.updateConfigShell(shellTaskConfig);
        } else {
            configResult = dataMiddlewareService.configShell(shellTaskConfig);//插入该条记录
        }
        if (configResult.equalsIgnoreCase(Constants.SUCCESS)) {  //配置完后即开始执行定时任务
            // dataMiddlewareService.executeImportTask(importConfigRequest);
            // dataMiddlewareService.executeImportTask(DataSource.EXCEL);
        }
        return configResult;
    }*/
    
    
    /**
     * 查询导入任务详情
     *
     * @param dataSource 数据源：all,idc,excel,...
     * @return 导入任务详情响应体
     */
    @GetMapping(path = "/import")
    public JSONObject getImportTask(@RequestParam String dataSource,HttpServletRequest request) {
        logger.info("getImportTask start :{}", dataSource);
        return dataMiddlewareService.getImportConfig(dataSource,request);
    }

    @GetMapping("/getExcelImportLog")
    public JSONObject getExcelImportLog(HttpServletRequest request){
        logger.info("getExcelImportLog start...");
        JSONObject json = null;
        try {
            json = dataMiddlewareService.getExcelImportLog(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    @PostMapping("/deleteExcelImportLogById")
    public JSONObject deleteExcelImportLogById(@RequestParam("ids") String ids){
        logger.info("==delete=="+ids);
        try {
            return dataMiddlewareService.deleteExcelImportLogById(ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 查询导出任务详情
     * @param dataSource 数据源：all,idc,excel,...
     * @return 导入任务详情响应体
     */
    @GetMapping(path = "/export")
    public JSONObject getExportTask(@RequestParam String dataSource,HttpServletRequest request) {
        logger.info("getExportTask start :{}", dataSource);
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        return dataMiddlewareService.getExportConfig(dataSource,request);
    }
    
    
    /**
     * 查询shell任务详情
     * @param dataSource 数据源：all,idc,excel,...
     * @return 导入任务详情响应体
     */
    @GetMapping(path = "/shell")
    public JSONObject getShellTask(@RequestParam String dataSource,HttpServletRequest request) {
        logger.info("getShellTask start :{}", dataSource);
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        return dataMiddlewareService.getShellConfig(dataSource,request);
    }

    @PostMapping(path = {"/cancelImportTask"})
    public JSONObject cancelImportTask(@RequestParam String ids) {
        JSONObject jsonObject  = dataMiddlewareService.cancelImportTask(ids);
        String msg = dataMiddlewareService.restartExecuteImportTask();
        jsonObject.put("msg",jsonObject.getString("msg")+" "+msg);
        return jsonObject;
    }
    
    @PostMapping(path = {"/cancelExportTask"})
    public JSONObject cancelExportTask(@RequestParam String ids) {
        JSONObject jsonObject = dataMiddlewareService.cancelExportTask(ids);
        String msg = dataMiddlewareService.restartExecuteImportTask();
        jsonObject.put("msg",jsonObject.getString("msg")+" "+msg);
        return jsonObject;
    }
    
    @PostMapping(path = {"/cancelShellTask"})
    public JSONObject cancelShellTask(@RequestParam String ids) {
        JSONObject jsonObject = dataMiddlewareService.cancelShellTask(ids);
        return jsonObject;
    }

    /**
     * 执行sql语句查询所要的数据列表
     *
     * @param request 请求
     * @return 数据列表
     */
    @PostMapping(path = {"/query"})
    public SqlQueryResponse executeSql(HttpServletRequest request,@RequestBody SqlQueryRequest sqlQueryRequest) {
        logger.info("executeSql start:{}", request);
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        if("sqlImport".equals(sqlQueryRequest.getOption())){
            sqlQueryRequest.setSqlStr("SELECT label,id,sqlstr,frequency,url,username,pwd,schema_ ,tableName ,datasource FROM import_task_config where lastUpdater ='"+username+"'");
        }else if("excelImport".equals(sqlQueryRequest.getOption())){
            sqlQueryRequest.setSqlStr("SELECT * FROM excel_import_record where importUser ='"+username+"'");
        }else{
            sqlQueryRequest.setSqlStr("select * from excel_import_record");
        }
        SqlQueryResponse response = new SqlQueryResponse();
        try {
            response = dataMiddlewareService.executeSql(sqlQueryRequest);
        } catch (Exception e) {
            logger.error("", e);
            response.setCode(Constants.FAILED);
            response.setMsg(e.getMessage());
            return response;
        }
        return response;
    }

    /**
     * 生成验证码
     */
    @RequestMapping(value = "/getVerify")
    public void getVerify(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("image/jpeg");//设置相应类型,告诉浏览器输出的内容为图片
            response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);
            RandomValidateCodeUtil randomValidateCode = new RandomValidateCodeUtil();
            randomValidateCode.getRandomcode(request, response);//输出验证码图片方法
        } catch (Exception e) {
            logger.error("获取验证码失败>>>> ", e);
        }
    }

    /**
     * 获取公钥
     */
    @RequestMapping(value = "/getPublicKey")
    public String getPublicKey(){
        return dataMiddlewareService.getPublicKey();
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param authcode 验证码
     * @return 结果
     */
    @PostMapping(path = {"/login"})
    public String login(String username, String password, String authcode, HttpSession session) {
        try {
            //从session中获取随机数
            String random = (String) session.getAttribute(RandomValidateCodeUtil.RANDOMCODEKEY);
            if (random == null) {
               // return Constants.AUTH_CODE_ERROR;
            }
            if (!random.equalsIgnoreCase(authcode)) {
               // return Constants.AUTH_CODE_ERROR;
            }
        } catch (Exception e) {
            logger.error("验证码校验失败", e);
            return Constants.AUTH_CODE_CHECK_ERROR;
        }
        logger.info("login:{}", username);
        String result = dataMiddlewareService.login(username, password);
        if (result.equalsIgnoreCase(Constants.SUCCESS)) {
            session.setAttribute(WebSecurityConfig.SESSION_KEY, username);
            return Constants.SUCCESS;
        } else {
            return result;
        }
    }

    /**
     * @param excelFile 上传的文件
     * @return  上传结果
     */
    @RequestMapping("/fileUpload")
    @ResponseBody
    public String uploadExcelFile(HttpServletRequest request, @RequestParam("file") MultipartFile excelFile) {
        String result = null;
        try {
            result = dataMiddlewareService.uploadExcelFile(request,excelFile);
        } catch (Exception e) {
            logger.error("上传文件出错！");
            e.printStackTrace();
        }
        logger.info("==上传excel结果==" + result);
        return result;
        /*//1定义要上传文件 的存放路径
        //2获得文件名字
        String fileName = file.getOriginalFilename();
        if ((fileName == null) || fileName.trim().equalsIgnoreCase("")) {
            return "no_file";
        } else if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            return "file_format_error";
        }
        //2上传失败提示
        String warning;
        if (FileUtils.upload(file, properties.getExcelPath(), fileName)) {
            //上传成功
            warning = "success";
        } else {
            warning = "failed";
        }
        return warning;*/
    }
    
    @GetMapping(path = {"/import/edit2"})
    public ImportTaskConfig configImportTask2(HttpServletRequest request) {
    	ImportTaskConfig result =  (ImportTaskConfig)request.getSession().getAttribute("importTaskConfig");
    	return result;
    }
    
    @GetMapping(path = {"/export/edit2"})
    public ExportTaskConfig configExportTask2(HttpServletRequest request) {
    	ExportTaskConfig result =  (ExportTaskConfig)request.getSession().getAttribute("exportTaskConfig");
    	return result;
    }
    
    @GetMapping(path = {"/shell/edit2"})
    public ShellTaskConfig configShellTask2(HttpServletRequest request) {
    	ShellTaskConfig result =  (ShellTaskConfig)request.getSession().getAttribute("shellTaskConfig");
    	return result;
    }

    @RequestMapping("/querySysLog")
    public JSONObject querySysLog(HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = dataMiddlewareService.querySysLog(request);
        } catch (Exception e) {
            logger.error("查询系统日志出错");
            e.printStackTrace();
        }
        return jsonObject;
    }
}

