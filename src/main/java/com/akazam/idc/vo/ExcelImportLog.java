package com.akazam.idc.vo;

import lombok.Data;

@Data
public class ExcelImportLog {
    private String id;
    private String excelFileName;
    private String excelSavePath;
    private String importUser;
    private String importDate;
}
