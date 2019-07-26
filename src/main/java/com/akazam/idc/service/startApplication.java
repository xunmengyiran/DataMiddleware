package com.akazam.idc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
public class startApplication implements ApplicationRunner{
	
	@Autowired
    private DataMiddlewareService dataMiddlewareService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		dataMiddlewareService.batchExecuteImportTask();
	}

}
