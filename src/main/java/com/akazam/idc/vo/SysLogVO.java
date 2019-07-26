package com.akazam.idc.vo;

import lombok.Data;

@Data
public class SysLogVO {
    private Long id;
    private String operationUserName;
    private String operationName;
    private String operationMethod;
    private String operationParams;
    private String operationDate;
}
