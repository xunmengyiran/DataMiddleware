package com.akazam.idc.service;

import com.akazam.idc.annotation.SysLog;
import com.akazam.idc.constants.ConfigureProperties;
import com.akazam.idc.constants.Constants;
import com.akazam.idc.constants.DataSource;
import com.akazam.idc.constants.WebSecurityConfig;
import com.akazam.idc.dao.*;
import com.akazam.idc.model.*;
import com.akazam.idc.util.*;
import com.akazam.idc.vo.*;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

@Service
public class DataMiddlewareService {

    private static final Logger logger = LoggerFactory.getLogger(DataMiddlewareService.class);

    @Autowired
    private ConnConfigDao connConfigDao;

    @Autowired
    private ImportTaskConfigDao importTaskConfigDao;

    @Autowired
    private ExportTaskConfigDao exportTaskConfigDao;

    @Autowired
    private ShellTaskConfigDao shellTaskConfigDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommonDao commonDao;

    @Autowired
    private ScheduleTaskExecutor scheduleTaskExecutor;

    @Autowired
    private ConfigureProperties properties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SysLogDao sysLogDao;

    @Value("${spring.profiles.active}")
    private String profiles;

    @Value("${akazam.excelSavaPath}")
    private String excelFileSavePath;

    private OracleConnectManager oracleConnectManager = new OracleConnectManager();

    private ConnConfig getConnConfig(String datasource) {
        DataSource dataSource = DataSource.valueOf(datasource.toUpperCase());
        return connConfigDao.loadConnConfig(dataSource.ordinal());
    }


    /**
     * private List<ImportTaskConfig> getImportTaskConfig(DataSource dataSource) {
     * return importTaskConfigDao.loadImportTaskConfig(dataSource.ordinal());
     * }
     **/

    private void executeExcelImportTask() throws SQLException {
        long start = System.currentTimeMillis();
        logger.info("executeExcelImportTask begin.");
        ConnConfig connConfig = this.getConnConfig(Constants.DATA_SOURCE_EXCEL);
        //excel存放的文件路径
        String filePath = connConfig.getUrl();
        File fileTemp = new File(filePath);
        if (!fileTemp.isDirectory()) {
            logger.error("file path is not a directory!");
            return;
        }
        File[] excelFiles = fileTemp.listFiles(new ExcelFileFilter());
        if (null == excelFiles) {
            logger.warn("excelFiles is null.");
            return;
        }
        for (File excelFile : excelFiles) {
            EasyExcelReader excelReader = new EasyExcelReader(commonDao);
            excelReader.readExcelWithStringList(excelFile, properties.getExcelBatchSize());
            //读取完毕后删除文件
            excelFile.delete();
        }
        logger.info("executeExcelImportTask end,cost time:{} ms.", System.currentTimeMillis() - start);
        //更新导入时间
        ImportTaskConfig importTaskConfig = new ImportTaskConfig();
        //importTaskConfig.setDatasource(DataSource.EXCEL.ordinal());
        importTaskConfig.setLasttime(new Date().getTime());
        importTaskConfigDao.updateImportTaskLastTime(importTaskConfig);
    }

    /**
     * 批量执行导入定时任务
     */
    @SysLog("批量执行导入定时任务")
    public String batchExecuteImportTask() {
        List<ImportConfigRequest> ImportTaskConfig_List = importTaskConfigDao.loadAllImportTaskConfig();
        String msg = "执行定时导入任务成功";
        for (ImportConfigRequest importConfigRequest : ImportTaskConfig_List) {
            String cronStr = importConfigRequest.getFrequency();
            Runnable task = () -> {
                try {
                    executeOracleMySqlImportTask(importConfigRequest);
                } catch (SQLException e) {
                    logger.error("执行定时导入任务出错" + cronStr);
                    e.printStackTrace();
                }
            };
            try {
                scheduleTaskExecutor.startCron(task, cronStr, importConfigRequest.getDatasource());
            } catch (Exception e) {
                logger.error("执行定时导入表达式出错" + cronStr);
                msg = "执行定时导入任务出错" + cronStr;
                e.printStackTrace();
            }
        }
        return msg;
    }

    @SysLog("重新启动定时任务")
    public String restartExecuteImportTask() {
        scheduleTaskExecutor.stopAllCron();
        return batchExecuteImportTask();
    }

    /**
     * 执行导入定时任务
     *
     * @param importConfigRequest
     */
    @SysLog("执行导入定时任务")
    public String executeImportTask(ImportConfigRequest importConfigRequest) {
        scheduleTaskExecutor.stopAllCron();
        String msg = batchExecuteImportTask();
        return msg;
    }

    @SysLog("执行Oracle,Mysql导入")
    private void executeOracleMySqlImportTask(ImportConfigRequest importConfigRequest) throws SQLException {
        String singleTable = importConfigRequest.getTableName() + DateHelper.currentDatetime2();
        String insert = "insert into " + singleTable + "(";
        String values = "values(";
        String driver = null;
        if (importConfigRequest.getDatasource().equals(Constants.DataSource_MySql)) {
            driver = Constants.MySql_Driver;
        } else if (importConfigRequest.getDatasource().equals(Constants.DataSource_Oracle)) {
            driver = Constants.Oracle_Driver;
        }
        List<Map<String, Object>> results = commonDao.jdbcTemplateQuery(driver, importConfigRequest.getUrl(), importConfigRequest.getUsername(), importConfigRequest.getPwd(), importConfigRequest.getSqlstr());
        if (results != null && results.size() != 0) {
            Map<String, Object> MetaData = results.get(0);
            String table = "create table " + singleTable + "(";
            for (Map.Entry<String, Object> entry : MetaData.entrySet()) {
                String key = entry.getKey();
                if (Constants.Key_Words.contains(key.toUpperCase())) {
                    key = "`" + key + "`";
                }
                table += key + "  LONGTEXT,";
                insert += key + ",";
                values += "?,";
            }

            table += "last_update_time_10 LONGTEXT)";
            insert += "last_update_time_10)";
            //values += DateHelper.currentDatetime() + ")";
            values += "?)";

            commonDao.update(table);

            String insertSql = insert + " " + values;
            logger.info("insertSql:" + insertSql);
            jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int j = 1;
                    Map<String, Object> MetaData_2 = results.get(i);
                    for (Map.Entry<String, Object> entry : MetaData_2.entrySet()) {
                        ps.setString(j++, entry.getValue() == null ? "" : entry.getValue().toString());
                    }
                    ps.setString(j, DateHelper.currentDatetime());
                }

                public int getBatchSize() {
                    return results.size();
                }
            });
        }
    }

    private void executeOracleImportTask_old(String sql, String tableName) throws SQLException {
        long start = System.currentTimeMillis();
        logger.info("executeOracleImportTask begin.sql:{}", sql);
        ConnConfig connConfig = this.getConnConfig(Constants.DATA_SOURCE_IDC);
        Connection conn = oracleConnectManager.getConn(connConfig);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData meta = rs.getMetaData();
        String insertSql = createTable(meta, tableName);
        List<Object[]> dataList = new ArrayList<>();

        logger.debug("1111111111111111");
        Map<String, Object[]> dataMap = new HashMap<>();
        while (rs.next()) {
            StringBuilder originStr = new StringBuilder();
            Object[] dataArray = new Object[meta.getColumnCount() + 1];
            for (int i = 0; i < meta.getColumnCount(); i++) {
                dataArray[i] = rs.getObject(i + 1);
                originStr.append(meta.getColumnName(i + 1))
                        .append(":")
                        .append(dataArray[i] == null ? "" : dataArray[i].toString())
                        .append(",");
            }
            String key = SHA256Util.getSHA256StrJava(originStr.toString());
            dataArray[meta.getColumnCount()] = key;
            dataMap.put(key, dataArray);
            dataList.add(dataArray);
        }
        logger.debug("2222222222222");
        String querySql = "select `key` from data_oracle." + tableName + " t where t.`key` in (:keyList)";
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("keyList", dataMap.keySet());
        List<Map<String, Object>> resultList = commonDao.query(querySql, paramsMap);
        logger.debug("3333333333333");
        for (Map<String, Object> result : resultList) {
            String temp = result.get("key").toString();
            Object[] data = dataMap.get(temp);
            if (data != null) {
                dataList.remove(data);
            }
        }
        //关闭Oracle连接
        rs.close();
        stmt.close();
        conn.close();
        if (!dataList.isEmpty()) {
            commonDao.batchUpdate(insertSql, dataList);
        }
        logger.info("executeOracleImportTask finish,cost time:{} ms.", System.currentTimeMillis() - start);
        //更新导入时间
        ImportTaskConfig importTaskConfig = new ImportTaskConfig();
        //importTaskConfig.setDatasource(DataSource.IDC.ordinal());
        importTaskConfig.setLasttime(new Date().getTime());
        importTaskConfigDao.updateImportTaskLastTime(importTaskConfig);
    }

    @SysLog("创建表")
    private String createTable(ResultSetMetaData meta, String tableName) throws SQLException {
        StringBuilder createTableSql = new StringBuilder("CREATE TABLE data_oracle.`" + tableName + "` (");
        StringBuilder insertTableSql = new StringBuilder("INSERT INTO data_oracle.`" + tableName + "`(");
        for (int i = 0; i < meta.getColumnCount(); i++) {
            String mysqlType = OracleType2Mysql.parse(meta.getColumnTypeName(i + 1));
            createTableSql.append("`")
                    .append(meta.getColumnName(i + 1))
                    .append("` ")
                    .append(mysqlType);
            if (!OracleType2Mysql.TYPE_NO_LENGTH.contains(mysqlType)) {
                createTableSql.append("(")
                        .append(meta.getColumnDisplaySize(i + 1))
                        .append("),");
            } else {
                createTableSql.append(",");
            }
            insertTableSql.append("`")
                    .append(meta.getColumnName(i + 1))
                    .append("`,");
        }
        createTableSql.append("`key` varchar(100))");
        createTableSql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8");
        insertTableSql.append("`key`)");
        insertTableSql.append("VALUES(");
        for (int j = 0; j < meta.getColumnCount(); j++) {
            insertTableSql.append("?,");
        }
        insertTableSql.append("?)");
        if (!commonDao.isTableExist("data_oracle", tableName)) {
            commonDao.update(createTableSql.toString());
        }
        return insertTableSql.toString();
    }

    /**
     * 执行sql语句查询
     *
     * @param request 请求
     * @return 响应
     */
    public SqlQueryResponse executeSql(SqlQueryRequest request) {
        SqlQueryResponse response = new SqlQueryResponse();
        int offset = (request.getPageIndex() - 1) * request.getPageSize();
        String sql = request.getSqlStr().split(";")[0];
        String countSql = "SELECT count(*) count from (" + sql + ") temp";
        String pageSql = "SELECT * FROM ( " + sql + " ) temp LIMIT " + request.getPageSize() + " OFFSET " + offset;
        List<Map<String, Object>> countResult = commonDao.query(countSql);
        String dataCount = countResult.get(0).get("count").toString();
        response.setTotalCount(Integer.parseInt(dataCount));
        Double totalPages = Math.ceil(Integer.parseInt(dataCount) / (double) request.getPageSize());
        response.setTotalPages(totalPages.intValue());
        List<Map<String, Object>> pageResult = commonDao.query(pageSql);
        if (!pageResult.isEmpty()) {
            List<String> columnList = new ArrayList<>();
            columnList.addAll(pageResult.get(0).keySet());
            response.setColumnNameList(columnList);
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> temp : pageResult) {
                List<Object> data = new ArrayList<>();
                data.addAll(temp.values());
                dataList.add(data);
            }
            response.setDataList(dataList);
        }
        response.setCode(Constants.SUCCESS);
        return response;
    }

    @SysLog("删除SQL导入任务")
    public JSONObject cancelImportTask(String ids) {
        logger.info("===ids===" + ids);
        int num = importTaskConfigDao.cancelImportTaskConfig(ids);
        JSONObject jsonObject = new JSONObject();
        if (num == 1) {
            jsonObject.put("status", 1);
            jsonObject.put("msg", "删除成功");
        } else {
            jsonObject.put("status", 0);
            jsonObject.put("msg", "删除失败");
        }
        return jsonObject;
    }

    @SysLog("删除导出任务")
    public JSONObject cancelExportTask(String ids) {
        int num = exportTaskConfigDao.cancelExportTaskConfig(ids);
        JSONObject jsonObject = new JSONObject();
        if (num == 1) {
            jsonObject.put("status", 1);
            jsonObject.put("msg", "删除成功");
        } else {
            jsonObject.put("status", 0);
            jsonObject.put("msg", "删除失败");
        }
        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }

    @SysLog("删除Shell任务")
    public JSONObject cancelShellTask(String ids) {
        int num = shellTaskConfigDao.cancelShellTaskConfig(ids);
        JSONObject jsonObject = new JSONObject();
        if (num == 1) {
            jsonObject.put("status", 1);
            jsonObject.put("msg", "删除成功");
        } else {
            jsonObject.put("status", 0);
            jsonObject.put("msg", "删除失败");
        }
        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }

    /**
     * 根据数据源查询导入配置
     *
     * @param datasource idc、Excel、all。。。
     * @return 响应
     */
    @SysLog("根据数据源查询导入配置")
    public JSONObject getImportConfig(String datasource, HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        int rows = Integer.parseInt(request.getParameter("rows"));
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        DataSource dataSource = DataSource.valueOf(datasource.toUpperCase());
        List<ImportTaskConfig> importTaskConfig = importTaskConfigDao.loadImportTaskConfig(DataSource.IDC.ordinal(), username, page, rows);//TODO
        int count = importTaskConfigDao.getImportTaskConfigCount(username);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", count);
        jsonObject.put("rows", importTaskConfig);
        return jsonObject;
    }

    @SysLog("获取Excel导入记录")
    public JSONObject getExcelImportLog(HttpServletRequest request) throws Exception {
        int page = Integer.parseInt(request.getParameter("page"));
        int rows = Integer.parseInt(request.getParameter("rows"));
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        int count = importTaskConfigDao.getExcelImportLogCount(username);
        List<ExcelImportLog> excelImportLogList = importTaskConfigDao.getExcelImportLog(username, page, rows);//TODO
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", count);
        jsonObject.put("rows", excelImportLogList);

        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }

    @SysLog("根据ID删除EXCEL导入记录")
    public JSONObject deleteExcelImportLogById(String ids) throws Exception {
        int num = importTaskConfigDao.deleteExcelImportLogById(ids);
        JSONObject jsonObject = new JSONObject();
        if (num == 1) {
            jsonObject.put("status", 1);
            jsonObject.put("msg", "删除成功");
        } else {
            jsonObject.put("status", 0);
            jsonObject.put("msg", "删除失败");
        }
        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }

    /**
     * 根据数据源查询导出配置
     *
     * @param datasource idc、Excel、all。。。
     * @return 响应
     */
    @SysLog("根据数据源查询导出配置")
    public JSONObject getExportConfig(String datasource, HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        int rows = Integer.parseInt(request.getParameter("rows"));
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        int count = exportTaskConfigDao.getExportTaskConfigCount(username);
        DataSource dataSource = DataSource.valueOf(datasource.toUpperCase());
        List<ExportTaskConfig> exportTaskConfig = exportTaskConfigDao.loadExportTaskConfig(DataSource.IDC.ordinal(), username, page, rows);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", count);
        jsonObject.put("rows", exportTaskConfig);
        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }

    /**
     * 根据数据源查询导出配置
     *
     * @param datasource idc、Excel、all。。。
     * @return 响应
     */
    @SysLog("根据数据源查询导出配置_Shell方式")
    public JSONObject getShellConfig(String datasource, HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        int rows = Integer.parseInt(request.getParameter("rows"));
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        int count = shellTaskConfigDao.getShellTaskConfigCount(username);
        DataSource dataSource = DataSource.valueOf(datasource.toUpperCase());
        List<ShellTaskConfig> shellTaskConfig = shellTaskConfigDao.loadShellTaskConfig(DataSource.IDC.ordinal(), username, page, rows);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", count);
        jsonObject.put("rows", shellTaskConfig);

        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }

    /**
     * 配置导入
     *
     * @param request 请求
     * @return 响应
     */
    @SysLog("新增SQL定时导入任务")
    public JSONObject configImport(ImportConfigRequest request) {
        int num = importTaskConfigDao.insertImportTask(request);
        Map<String, Object> resultMap = new HashMap<>();
        if (num == 1) {
            resultMap.put("status", 1);
            resultMap.put("msg", "配置保存成功。");
        } else {
            resultMap.put("status", 1);
            resultMap.put("msg", "配置保存失败。");
        }
        return JSONObject.fromObject(resultMap);
    }

    @SysLog("新增SQL定时导出任务")
    public String configExport(ExportConfigRequest request) {
        exportTaskConfigDao.insertExportTask(request);
        return Constants.SUCCESS;
    }

    @SysLog("新增Shell任务")
    public String configShell(ShellTaskConfig request) {
        shellTaskConfigDao.insertShellTask(request);
        return Constants.SUCCESS;
    }

    @SysLog("更新导入任务")
    public JSONObject updateConfigImport(ImportConfigRequest request) {
        int num = importTaskConfigDao.updateImportTask(request);
        JSONObject resultJsonObject = new JSONObject();
        if (num == 1) {
            resultJsonObject.put("status", 1);
            resultJsonObject.put("msg", "配置更新成功。");
        } else {
            resultJsonObject.put("status", 1);
            resultJsonObject.put("msg", "配置更新失败。");
        }
        return resultJsonObject;
    }

    @SysLog("更新导出任务")
    public String updateConfigExport(ExportConfigRequest request) {
        exportTaskConfigDao.updateExportTask(request);
        return Constants.SUCCESS;
    }

    @SysLog("更新Shell脚本任务")
    public String updateConfigShell(ShellTaskConfig request) {
        shellTaskConfigDao.updateShellTask(request);
        return Constants.SUCCESS;
    }

    @SysLog("上传文件")
    public String uploadExcelFile(HttpServletRequest request, MultipartFile excelFile) throws Exception {
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        Map<String, Object> resultMap = new HashMap<>();
        if (excelFile.isEmpty()) {
            resultMap.put("status", 0);
            resultMap.put("msg", "请选择文件");
        } else {
            long excelFileSize = excelFile.getSize();
            logger.info("上传文件的大小为:" + excelFileSize);
            String excelFileName = excelFile.getOriginalFilename();
            if ((excelFileName == null) || excelFileName.trim().equalsIgnoreCase("")) {
                resultMap.put("status", 0);
                resultMap.put("msg", "文件名不能为空");
            } else if (!excelFileName.endsWith(".xls") && !excelFileName.endsWith(".xlsx")) {
                resultMap.put("status", 0);
                resultMap.put("msg", "文件格式只能为xls或xlsx");
            } else {
                logger.info("上传文件的名称为:" + excelFileName);
                String subffix = excelFileName.substring(excelFileName.lastIndexOf(".") + 1, excelFileName.length());
                logger.info("上传文件的格式为:" + subffix);
                String filePath = excelFileSavePath + Constants.DataFormat.sdf3.format(new Date()) + excelFileName;
                logger.info("excel文件保存位置:" + filePath);
                File file = new File(filePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                excelFile.transferTo(file);
                String tableName = excelFileName.replace("." + subffix, "");
                List<String> fieldNameList = ExcelUtils.getExcelTitleList(filePath);
                if (!importTaskConfigDao.tableIsExist(tableName)) {
                    logger.info(tableName + "表不存在，开始创建");
                    importTaskConfigDao.createTableByFieldNames(tableName, fieldNameList);
                    logger.info(tableName + "表创建成功。");
                } else {
                    logger.info(tableName + "表已存在，开始Alter字段。");
                    for (String fieldName : fieldNameList) {
                        int num = importTaskConfigDao.alterTable(tableName, fieldName);
                        if (num == 1) {
                            logger.info("字段" + fieldName + "增加成功。");
                        } else {
                            logger.info("字段" + fieldName + "增加失败。");
                        }
                    }

                }

                List<String> sqlList = ExcelUtils.readExcelContent2DB(tableName, filePath);
                for (String s : sqlList) {
                    logger.info("sql:" + s);
                    importTaskConfigDao.excute(s);
                    /*TestCreate testCreate = new TestCreate();
                    testCreate.setSex("1");
                    testCreate.setName("4444444445555555555");
                    importTaskConfigDao.insertObject(tableName,testCreate);*/
                    // 保存记录
                    String saveRecordSql = "insert into excel_import_record (id,excelFileName,excelSavePath,importUser,importDate) values('" + Constants.DataFormat.sdf3.format(new Date()) + "','" + excelFileName + "','" + filePath + "','" + username + "','" + Constants.DataFormat.sdf2.format(new Date()) + "')";
                    logger.info("保存记录sql:" + saveRecordSql);
                    importTaskConfigDao.excute(saveRecordSql);
                }
                resultMap.put("status", 1);
                resultMap.put("msg", "上传成功");
            }
        }
        JSONObject json = JSONObject.fromObject(resultMap);
        return json.toString();
    }

    /**
     * 导出至Excel文件
     */
    private void exportExcel(String filePath, String fileName, List<Map<String, Object>> dataList) {
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook(500);
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("sheet1");
            // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            Row hssfRow = sheet.createRow(0);
            // 第四步，创建单元格，并设置值表头 设置表头居中
            CellStyle hssfCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("黑体");
            font.setBold(true);
            //居中样式
            hssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
            hssfCellStyle.setWrapText(true);
            hssfCellStyle.setFont(font);

            Cell hssfCell;
            List<String> titles = new ArrayList<>();
            titles.addAll(dataList.get(0).keySet());
            for (int i = 0; i < titles.size(); i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles.get(i));//列名1
                hssfCell.setCellStyle(hssfCellStyle);//列居中显示
            }
            // 第五步，写入实体数据
            Iterator<Map<String, Object>> it = dataList.iterator();
            int line = 0;
            while (it.hasNext()) {
                hssfRow = sheet.createRow(line + 1);
                line++;
                Map<String, Object> data = it.next();
                for (int i = 0; i < titles.size(); i++) {
                    hssfRow.createCell(i).setCellValue(data.get(titles.get(i)) == null ? "" : data.get(titles.get(i)).toString());
                }
            }
            // 第七步，将文件输出到客户端浏览器
            try {
                try {
                    filePath = filePath + File.separator + DateHelper.currentDate().replace("-", "");
                    File dic = new File(filePath);
                    if (dic.exists()) {
                        if (dic.isDirectory())
                            ;
                        else
                            dic.mkdirs();
                    } else {
                        dic.mkdirs();
                    }
                    fileName = fileName + DateHelper.currentTime().replace(":", "");
                    String xlsxName = filePath + File.separator + fileName + Constants.XLSX_EXCEL;
                    FileOutputStream fileOutputStream = new FileOutputStream(xlsxName);
                    workbook.write(fileOutputStream);//将数据写出去
                    fileOutputStream.close();//关闭输出流
                    logger.info(xlsxName + "已导出完成");

                } catch (Exception e) {
                    logger.error("Export excel failed.", e);
                }
            } catch (Exception e) {
                logger.error("Export excel failed.", e);
            }
        } catch (Exception e) {
            logger.error("Export excel failed.", e);
        }
    }

    @SysLog("登录")
    public String login(String userName, String password) {
        logger.info("环境:" + profiles);
        if (password == null) {
            return Constants.USERNAME_OR_pwd_ERROR;
        }
        // 1.使用内存中的私钥解密客户端传过来的密文，该密文在上一步中被服务端传给客户端的公钥加密过
        try {
            logger.debug("login:decryptDataOnJava");
            password = RSAUtils.decryptDataOnJava(password, RSAUtils.keyPairMap.get(RSAUtils.PRIVATE_KEY));
        } catch (Exception e) {
            logger.error("login failed.", e);
            return Constants.FAILED;
        }
        // 2.将明文密码使用md5加密后与保存在数据库中的密码比较
        String plainPwd = password;
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        logger.debug("login:loadUserByUserName");
        User user = userDao.loadUserByUserName(userName);
        if (null == user) {
            return Constants.USERNAME_OR_pwd_ERROR;
        } else if ("dev".equals(profiles)) {
            return Constants.SUCCESS;
        } else if (user.getPassword().equals(password)) {
            // 3.判断密码复杂度是否满足要求
            if (!plainPwd.matches(Constants.PWD_REG)) {
                plainPwd = null;
                return Constants.PWD_IS_UNSAFE;
            }
            plainPwd = null;
            logger.debug("login:success.");
            return Constants.SUCCESS;
        } else {
            return Constants.USERNAME_OR_pwd_ERROR;
        }
    }

    /**
     * 获取公钥，并将公钥、私钥存储在内存中
     *
     * @return 公钥
     */
    public String getPublicKey() {
        try {
            Map<String, Object> keyPair = RSAUtils.genKeyPair();
            RSAUtils.keyPairMap.put(RSAUtils.PRIVATE_KEY, RSAUtils.getPrivateKey(keyPair));
            String publicKey = RSAUtils.getPublicKey(keyPair);
            RSAUtils.keyPairMap.put(RSAUtils.PUBLIC_KEY, publicKey);
            return publicKey;
        } catch (Exception e) {
            logger.error("getPublicKey failed.", e);
            return Constants.FAILED;
        }
    }

    public ImportTaskConfig loadImportTaskConfigByID(String id) {
        return importTaskConfigDao.loadImportTaskConfigByID(id);
    }

    public ExportTaskConfig loadExportTaskConfigByID(String id) {
        return exportTaskConfigDao.loadExportTaskConfigByID(id);
    }

    public ShellTaskConfig loadShellTaskConfigByID(String id) {
        return shellTaskConfigDao.loadShellTaskConfigByID(id);
    }

    public void saveSysLog(SysLogVO sysLog) throws Exception {
        sysLogDao.saveSysLog(sysLog);
    }

    @SysLog("获取系统操作日志")
    public JSONObject querySysLog(HttpServletRequest request) throws Exception{
        int page = Integer.parseInt(request.getParameter("page"));
        int rows = Integer.parseInt(request.getParameter("rows"));
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String username = request.getSession().getAttribute(WebSecurityConfig.SESSION_KEY).toString();
        int count = sysLogDao.getSysLogCount(startDate,endDate,username);
        List<SysLogVO> sysLogVOList = sysLogDao.querySysLog(username, page, rows,startDate,endDate);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", count);
        jsonObject.put("rows", sysLogVOList);
        logger.info("==jsonObject==" + jsonObject);
        return jsonObject;
    }
}
