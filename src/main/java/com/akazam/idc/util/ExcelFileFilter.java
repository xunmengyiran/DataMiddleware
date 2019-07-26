package com.akazam.idc.util;


import com.akazam.idc.constants.Constants;

import java.io.File;
import java.io.FileFilter;

public class ExcelFileFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
        if (file.isDirectory())
            return false;
        else {
            String name = file.getName();
            if (name.endsWith(Constants.XLS_EXCEL) || name.endsWith(Constants.XLSX_EXCEL))
                return true;
            else
                return false;
        }

    }

}

