package com.akazam.idc.constants;

import java.text.SimpleDateFormat;

public interface Constants {
    interface DataFormat {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    }

    String DATA_SOURCE_ALL = "all";

    String DATA_SOURCE_IDC = "idc";

    String DATA_SOURCE_EXCEL = "excel";

    String UNDERLINE = "_";

    String XLSX_EXCEL = ".xlsx";

    String XLS_EXCEL = ".xls";

    String SUCCESS = "success";

    String FAILED = "failed";

    /**
     * 登录错误码，用户名密码错误
     */
    String USERNAME_OR_pwd_ERROR = "username_or_pwd_error";


    /**
     * 登录错误码，密码复杂度不满足要求
     */
    String PWD_IS_UNSAFE = "pwd_is_unsafe";

    /**
     * 登录错误码，验证码校验失败
     */
    String AUTH_CODE_CHECK_ERROR = "check_auth_code_failed";

    /**
     * 登录错误码，验证码错误
     */
    String AUTH_CODE_ERROR = "auth_code_error";

    /**
     * 密码复杂度正则表达式（最小8位，必须同时包括字母、数字与特殊字符，至少包括一个大写字母）
     */
    String PWD_REG = "^.*(?=.{8,})(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=])[a-zA-Z0-9@#$%^&+=]*$";


    public static final String Oracle_Driver = "oracle.jdbc.driver.OracleDriver";

    public static final String MySql_Driver = "com.mysql.jdbc.Driver";

    public static final String DataSource_MySql = "MySql";
    public static final String DataSource_Oracle = "Oracle";

    public static final String Key_Words = "KEY,VALUE";

}
