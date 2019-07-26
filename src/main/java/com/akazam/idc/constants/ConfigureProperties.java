package com.akazam.idc.constants;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = ConfigureProperties.PREFIX)
@Component
public class ConfigureProperties {

    public static final String PREFIX = "akazam";

    /**
     * 本地Excel上传到服务器的路径配置，从该路径下读取Excel数据进行导入
     */
    private String exceSavePath;

    /**
     * 导入导出任务线程池大小
     */
    private int poolSize;

    /**
     * Excel批量导入大小
     */
    private int excelBatchSize;

}
