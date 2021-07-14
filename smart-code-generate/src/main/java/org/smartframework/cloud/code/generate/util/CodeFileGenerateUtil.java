package org.smartframework.cloud.code.generate.util;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.smartframework.cloud.code.generate.bo.template.BaseMapperBO;
import org.smartframework.cloud.code.generate.bo.template.BaseRespVOBO;
import org.smartframework.cloud.code.generate.bo.template.CommonBO;
import org.smartframework.cloud.code.generate.bo.template.EntityBO;
import org.smartframework.cloud.code.generate.config.Config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码文件生成工具类
 *
 * @author liyulin
 * @date 2019-07-15
 */
@UtilityClass
@Slf4j
public class CodeFileGenerateUtil {

	/** maven工程源码目录 */
	private static final String SRC_MAIN_JAVA = "/src/main/java/";
	/** java文件名后缀 */
	private static final String JAVA_FILE_SUFFIX = ".java";
	/** @date注释标记 */
	private static final String DATE_ANNOTATION_TAG = "@date";

	/**
	 * 生成Mapper
	 * 
	 * @param baseMapperBO
	 * @param basePath
	 * @throws IOException
	 */
	public static void generateBaseMapper(BaseMapperBO baseMapperBO, String basePath) throws IOException {
		generateCodeFile(baseMapperBO, basePath, Config.Template.BASE_MAPPER);
	}

	/**
	 * 生成Entity
	 * 
	 * @param entityBO
	 * @param basePath
	 * @throws IOException
	 */
	public static void generateEntity(EntityBO entityBO, String basePath) throws IOException {
		generateCodeFile(entityBO, basePath, Config.Template.ENTITY);
	}

	/**
	 * 生成RespBody
	 * 
	 * @param baseRespVOBO
	 * @param basePath
	 * @throws IOException
	 */
	public static void generateBaseRespVO(BaseRespVOBO baseRespVOBO, String basePath) throws IOException {
		generateCodeFile(baseRespVOBO, basePath, Config.Template.BASE_RESPBODY);
	}

	/**
	 * 生成代码文件
	 * 
	 * @param bo
	 * @param basePath
	 * @param templateName
	 * @throws IOException
	 */
	private static void generateCodeFile(CommonBO bo, String basePath, String templateName) throws IOException {
		String newCode = FreeMarkerUtil.freeMarkerRender(bo, templateName);
		String filePath = getClassFilePath(basePath, bo.getPackageName(), bo.getClassName());

		File codeFile = new File(filePath);
		boolean override = isOverride(codeFile, newCode);
		if (override) {
			log.info(filePath);
			FileUtils.writeStringToFile(codeFile, newCode, Config.DEFAULT_ENCODING);
		} else {
			log.info("[{}]生成内容与原有内容相同，不覆盖！！！", codeFile.getName());
		}
	}

	/**
	 * 获取类文件路径
	 * 
	 * @param basePath
	 * @param classPackage
	 * @param className
	 * @return
	 */
	private static String getClassFilePath(String basePath, String classPackage, String className) {
		return basePath + SRC_MAIN_JAVA + classPackage.replaceAll("\\.", "/") + "/" + className + JAVA_FILE_SUFFIX;
	}

	/**
	 * 是否需要覆盖原有的文件（会去除@date后比较）
	 * 
	 * @param codeFile 代码文件
	 * @param newCode  新生成的代码内容
	 * @return
	 * @throws IOException
	 */
	private static boolean isOverride(File codeFile, String newCode) throws IOException {
		if (!codeFile.exists()) {
			return true;
		}

		String oldCode = FileUtils.readFileToString(codeFile, Config.DEFAULT_ENCODING);
		String oldCodeAfterRemoveDate = getCodeAfterRemoveDate(oldCode);
		String newCodeAfterRemoveDate = getCodeAfterRemoveDate(newCode);

		return !Objects.equals(newCodeAfterRemoveDate, oldCodeAfterRemoveDate);
	}

	/**
	 * 获取移除@date后的code内容
	 * 
	 * @param code
	 * @return
	 */
	private static String getCodeAfterRemoveDate(String code) {
		int codeDateIndex = code.indexOf(DATE_ANNOTATION_TAG);
		String codeAfterRemoveDate = null;
		if (codeDateIndex != -1) {
			// 需要移除的字符串长度
			int removeLength = DATE_ANNOTATION_TAG.length() + Config.CODE_CREATE_DATE_FORMAT.length() + 1;
			codeAfterRemoveDate = code.substring(0, codeDateIndex)
					+ code.substring(codeDateIndex + removeLength, code.length());
		} else {
			codeAfterRemoveDate = code;
		}
		return codeAfterRemoveDate;
	}

}