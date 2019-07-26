package com.akazam.idc.vo;

import com.akazam.idc.model.ImportTaskConfig;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class ImportConfigResponse extends BaseResponse {

    private int frequency;

    private String time;

    private String lasttime;

    private String sqlstr;

    private String datasource;

    public static ImportConfigResponse getImportConfigResponse(ImportTaskConfig config)
    {
        ImportConfigResponse response = new ImportConfigResponse();

       // response.setFrequency(config.getFrequency());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lasttime = dateFormat.format(new Date(config.getLasttime()));
        response.setLasttime(lasttime);
        response.setSqlstr(config.getSqlstr());
        response.setTime(config.getTime());

        return response;
    }

}
