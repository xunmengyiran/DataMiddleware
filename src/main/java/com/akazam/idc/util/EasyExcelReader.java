package com.akazam.idc.util;

import com.akazam.idc.constants.Constants;
import com.akazam.idc.dao.CommonDao;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel读取工具类
 */
public class EasyExcelReader {

    private static final Logger logger = LoggerFactory.getLogger(EasyExcelReader.class);

    private CommonDao commonDao;

    public EasyExcelReader(CommonDao commonDao) {
        this.commonDao = commonDao;
    }

    /**
     * StringList 解析监听器
     */
    private class StringExcelListener extends AnalysisEventListener {

        /**
         * 自定义用于暂时存储data
         * 可以通过实例获取该值
         */
        private List<Object[]> dataList = new ArrayList<>();

        private CommonDao commonDao;

        private String tableName;

        private int lineNo = 0;

        private StringBuilder insertTableSql;

        private int batchSize;

        StringExcelListener(CommonDao commonDao, String tableName, int batchSize) {
            this.commonDao = commonDao;
            this.tableName = tableName;
            this.batchSize = batchSize;
        }

        /**
         * 每解析一行都会回调invoke()方法
         *
         * @param object  行
         * @param context 上下文
         */
        @Override
        public void invoke(Object object, AnalysisContext context) {
            List nullList = new ArrayList();
            nullList.add(null);
            if (lineNo == 0) {
                lineNo++;
                List<String> columnNameList = new ArrayList<>();
                columnNameList.addAll((List<String>) object);
                columnNameList.removeAll(nullList);
                try {
                    createTable(columnNameList);
                } catch (SQLException e) {
                    logger.error("Create table failed.", e);
                }
            } else {
                List<Object> dataTemp = (List<Object>) object;
                dataTemp.removeAll(nullList);
                if ((lineNo++ % batchSize) == 0) {
                    dataList.add(dataTemp.toArray());
                    logger.info("Commit datas to database,size:{}", dataList.size());
                    commonDao.batchUpdate(insertTableSql.toString(), dataList);
                    dataList.clear();
                } else {
                    dataList.add(dataTemp.toArray());
                }
            }
        }

        private void createTable(List<String> columnNameList) throws SQLException {
            StringBuilder createTableSql = new StringBuilder("CREATE TABLE data_excel.`" + tableName + "` (");
            insertTableSql = new StringBuilder("INSERT INTO data_excel.`" + tableName + "`(");
            for (String columnName : columnNameList) {
                createTableSql.append("`")
                        .append(columnName)
                        .append("` varchar(255),");//excel中的每一列都认为是字符串，长度默认最大255
                insertTableSql.append("`")
                        .append(columnName)
                        .append("`,");
            }
            createTableSql.setCharAt(createTableSql.length() - 1, ')');
            createTableSql.append("ENGINE=InnoDB DEFAULT CHARSET=utf8");
            //该表名不存在创建新的表格
            if (!commonDao.isTableExist("data_excel", tableName)) {
                logger.info("A table {} is created.", tableName);
                commonDao.update(createTableSql.toString());
            }
            insertTableSql.setCharAt(insertTableSql.length() - 1, ')');
            insertTableSql.append("VALUES(");
            for (int j = 0; j < columnNameList.size(); j++) {
                insertTableSql.append("?,");
            }
            insertTableSql.setCharAt(insertTableSql.length() - 1, ')');
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            //解析结束销毁不用的资源
            //注意不要调用datas.clear(),否则getDatas为null
        }

        List<Object[]> getDataList() {
            return dataList;
        }

        StringBuilder getInsertTableSql() {
            return insertTableSql;
        }
    }

    /**
     * 读取Excel并对Excel中的内容进行入库操作
     *
     * @param excelFile excel文件对象
     * @param batchSize 批量处理数据大小
     */
    public void readExcelWithStringList(File excelFile, int batchSize) {
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(excelFile));
        } catch (FileNotFoundException e) {
            logger.error("File is not found.", e);
            return;
        }
        String fileName = excelFile.getName();
        if (fileName.endsWith(Constants.XLSX_EXCEL)) {
            fileName = fileName.substring(0, fileName.indexOf(Constants.XLSX_EXCEL));
        } else {
            fileName = fileName.substring(0, fileName.indexOf(Constants.XLS_EXCEL));
        }
        StringExcelListener listener = new StringExcelListener(this.commonDao, camelToUnderline(fileName), batchSize);
        ExcelReader excelReader = new ExcelReader(bis, null, listener);
        excelReader.read();
        // 剩余或者未达到批量提交的数据最后一次性提交到数据库
        List<Object[]> dataList = listener.getDataList();
        if (!dataList.isEmpty()) {
            logger.info("Commit datas to database,size:{}", dataList.size());
            commonDao.batchUpdate(listener.getInsertTableSql().toString(), dataList);
        }
    }

    /**
     * 驼峰格式字符串转换为下划线格式字符串，字母小写
     *
     * @param param 字符串
     * @return 下划线小写字母字符串
     */
    private String camelToUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(Constants.UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }
}

