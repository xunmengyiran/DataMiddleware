package com.akazam.idc.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SqlText {
    private String sql;
    private String name;

    public static List<SqlText> parseSqlText(String input) {
        if (null == input || input.trim().equalsIgnoreCase("")) {
            return null;
        }
        String[] a = input.split(";");
        if (a.length % 2 != 0){
            return null;
        }
        List<SqlText> sqlTexts = new ArrayList<>();
        SqlText sqlText = null;
        for (int i = 0; i < a.length; i++) {
            if (i % 2 == 0) {
                sqlText = new SqlText();
                sqlText.setSql(a[i]);
            } else {
                if (a[i].trim().equalsIgnoreCase("")){
                    return null;
                }
                sqlText.setName(a[i]);
                sqlTexts.add(sqlText);
            }
        }
        return sqlTexts;
    }
}
