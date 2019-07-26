package com.akazam.idc.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OracleType2Mysql {

    /**
     * 数据类型转换映射表
     */
    public static final Map<String,String> TYPE_MAP = new HashMap<>();

    /**
     * 没有长度的数据类型集合
     */
    public static final Set<String> TYPE_NO_LENGTH = new HashSet<>();

    static
    {
        TYPE_MAP.put("NUMBER","BIGINT");
        TYPE_MAP.put("VARCHAR2","VARCHAR");
        TYPE_MAP.put("NCHAR","VARCHAR");

        TYPE_NO_LENGTH.add("DATE");
        TYPE_NO_LENGTH.add("TIMESTAMP");
    }

    /**
     * 将Oracle的数据类型转换为MySQL的数据类型
     * @param oracleType    Oracle数据类型名
     * @return               对应的MySQL数据类型名
     */
    public static String parse(String oracleType)
    {
        String mysqlType = TYPE_MAP.get(oracleType);
        if (mysqlType == null)
        {
            return oracleType;
        }
        else
        {
            return mysqlType;
        }
    }
}
