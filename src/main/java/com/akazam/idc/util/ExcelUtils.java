package com.akazam.idc.util;

import com.akazam.idc.constants.Constants;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

public class ExcelUtils {
    private static XSSFWorkbook xb;
    private static XSSFSheet xs;
    private static XSSFRow xr;

    /**
     * 读取Excel表格表头的内容
     *
     * @param is
     * @return 表头内容的数组String类型
     */
    public static String[] readExcelTitle(InputStream is) {
        try {
            xb = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Sheet工作表
        xs = xb.getSheetAt(0);
        // 获取首行标题
        xr = xs.getRow(0);
        // 标题总列数
        int colNum = xr.getPhysicalNumberOfCells();
        String[] title = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            title[i] = getCellFormatValue(xr.getCell((short) i));
        }
        return title;
    }

    /**
     * 根据XSSFCell类型设置数据
     *
     * @param xssfCell
     * @return
     */
    private static String getCellFormatValue(XSSFCell xssfCell) {
        String cellvalue = "";
        if (xssfCell != null) {
            // 判断当前Cell的Type
            switch (xssfCell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case XSSFCell.CELL_TYPE_NUMERIC:
                case XSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    // 如果是Date类型则，转化为Data格式
                    if (HSSFDateUtil.isCellDateFormatted(xssfCell)) {
                        // 方法1：data格式是带时分秒的：2011-10-12 0:00:00
                        // 方法2：格式是不带带时分秒的：2011-10-12
                        Date date = xssfCell.getDateCellValue();
                        cellvalue = Constants.DataFormat.sdf1.format(date);
                    } else {
                        // 如果是纯数字,取得当前Cell的数值
                        DecimalFormat df = new DecimalFormat("0");
                        cellvalue = df.format(xssfCell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRING
                case XSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = xssfCell.getRichStringCellValue().getString();
                    break;
                default:
                    // 默认的Cell值
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }

    public static List<Map> readExcelContent(InputStream is, String[] title) {
        try {
            xb = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Map> list = new ArrayList<Map>();
        // Sheet工作表
        xs = xb.getSheetAt(0);
        // 总行数
        int rowNum = xs.getLastRowNum();
        System.out.println("总行数" + rowNum);
        // 获取标题
        if (title != null && title.length > 0) {
            String str = null;
            // 总列数
            int colNum = xr.getPhysicalNumberOfCells();
            System.out.println("总列数" + colNum);
            Map<String, String> content = null;
            // 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                content = new HashMap<String, String>();
                xr = xs.getRow(i);
                for (int j = 0; j < colNum; j++) {
                    str = getCellFormatValue(xr.getCell((short) j)).trim();
                    content.put(title[j], str);
                }
                list.add(content);
            }
        }
        return list;
    }

    public static List<String> getExcelTitleList(String excelFilePath) throws Exception {
        List<String> titleList = new ArrayList<>();
        File file = new File(excelFilePath);
        InputStream is = new FileInputStream(file);
        String[] title = ExcelUtils.readExcelTitle(is);
        for (String string : title) {
            titleList.add(string);
        }
        return titleList;
    }

    /**
     * 读取excel，将数据插入到数据库
     * @param excelFilePath 待读取的excel路径
     * @return
     * @throws Exception
     */
    public static List<String> readExcelContent2DB(String tableName, String excelFilePath) throws Exception {
        List<String> sqlList = new ArrayList<>();
        File file = new File(excelFilePath);
        String[] title = ExcelUtils.readExcelTitle(new FileInputStream(file));
        StringBuilder columnNames = new StringBuilder();
        for (int i = 0; i < title.length; i++) {
            columnNames.append(title[i]);
            if (i != title.length - 1) {
                columnNames.append(",");
            }
        }
        List<Map> list = ExcelUtils.readExcelContent(new FileInputStream(file), title);
        for (Map map : list) {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < title.length; i++) {
                values.append("'"+map.get(title[i])+"'");
                if (i != title.length - 1) {
                    values.append(",");
                }
            }
            String sqlStr = "insert into "+tableName+"(" + columnNames.toString() + ") values" + "(" + values.toString() + ")";
            sqlList.add(sqlStr);
        }
        return sqlList;
    }

    public static void exportDBData2Excel(String[] headers, Collection<T> dataset, HttpServletResponse response) {
        // 声明一个工作薄
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 生成一个表格
        XSSFSheet sheet = workbook.createSheet();
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 20);
        // 产生表格标题行
        XSSFRow row = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            XSSFCell cell = row.createCell(i);
            XSSFRichTextString text = new XSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }
        try {
            // 遍历集合数据，产生数据行
            Iterator<T> it = dataset.iterator();
            int index = 0;
            while (it.hasNext()) {
                index++;
                row = sheet.createRow(index);
                T t = (T) it.next();
                // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
                Field[] fields = t.getClass().getDeclaredFields();
                for (short i = 0; i < headers.length; i++) {
                    XSSFCell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Class tCls = t.getClass();
                    Method getMethod = tCls.getMethod(getMethodName, new Class[] {});
                    Object value = getMethod.invoke(t, new Object[] {});
                    // 判断值的类型后进行强制类型转换
                    String textValue = null;
                    // 其它数据类型都当作字符串简单处理
                    if(value != null && value != ""){
                        textValue = value.toString();
                    }
                    if (textValue != null) {
                        XSSFRichTextString richString = new XSSFRichTextString(textValue);
                        cell.setCellValue(richString);
                    }
                }
            }
            getExportFile(workbook, Constants.DataFormat.sdf3.format(new Date()),response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getExportFile(XSSFWorkbook workbook, String name, HttpServletResponse response) throws Exception {
        BufferedOutputStream fos = null;
        try {
            String fileName = "C:\\Users\\DELL\\Desktop\\tmp\\"+name + ".xlsx";
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String( fileName.getBytes("gb2312"), "ISO8859-1" ));
            fos = new BufferedOutputStream(response.getOutputStream());
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        readExcelTitleTest();//测试表头信息
        List<String> sqlList = readExcelContent2DB("user","C:\\Users\\DELL\\Desktop\\tmp\\IDC_DATA_test.xlsx");
        for (String s:sqlList){
            System.out.println("sql:"+s);
        }
    }
}