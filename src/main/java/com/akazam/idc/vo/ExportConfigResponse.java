package com.akazam.idc.vo;

import com.akazam.idc.model.ExportTaskConfig;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class ExportConfigResponse extends BaseResponse {

    private int frequency;

    private String time;

    private String lasttime;

    private String sqlstr;

    private String path;

    public static ExportConfigResponse getImportConfigResponse(ExportTaskConfig config)
    {
        ExportConfigResponse response = new ExportConfigResponse();
/**
        response.setFrequency(config.getFrequency());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lasttime = dateFormat.format(new Date(config.getLasttime()));
        response.setLasttime(lasttime);
        response.setSqlstr(config.getSqlstr());
        response.setTime(config.getTime());
        response.setPath(config.getPath());
**/
        return response;
    }

}
