package org.smartframework.cloud.starter.configure.properties;

import java.nio.charset.StandardCharsets;

import org.smartframework.cloud.common.pojo.Base;

import lombok.Getter;
import lombok.Setter;

/**
 * 多语言配置信息
 * 
 * @author liyulin
 * @date 2019-07-15
 */
@Getter
@Setter
public class LocaleProperties extends Base {

	private static final long serialVersionUID = 1L;
	/** 编码（默认utf-8） */
	private String encodeing = StandardCharsets.UTF_8.name();
	/** 缓存时间（默认7天；单位秒） */
	private int cacheSeconds = 60 * 60 * 24 * 7;

}