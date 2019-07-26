package com.akazam.idc.model;

import lombok.Data;

@Data
public class ShellTaskConfig {

	private int id;

	private String frequency;

	private String lasttime;

	private String shellText;

	private String lastUpdater;

	private String label;// 标签

}
