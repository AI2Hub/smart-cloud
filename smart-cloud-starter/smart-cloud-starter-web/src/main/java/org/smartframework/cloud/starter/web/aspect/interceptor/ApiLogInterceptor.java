package org.smartframework.cloud.starter.web.aspect.interceptor;

import java.lang.reflect.Method;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.smartframework.cloud.common.pojo.vo.RespHeadVO;
import org.smartframework.cloud.common.pojo.vo.RespVO;
import org.smartframework.cloud.starter.configure.constants.OrderConstant;
import org.smartframework.cloud.starter.core.business.SmartReqContext;
import org.smartframework.cloud.starter.core.business.util.AspectInterceptorUtil;
import org.smartframework.cloud.starter.core.business.util.WebUtil;
import org.smartframework.cloud.starter.core.constants.SymbolConstant;
import org.smartframework.cloud.starter.log.util.LogUtil;
import org.smartframework.cloud.starter.web.aspect.pojo.LogAspectDO;
import org.smartframework.cloud.starter.web.exception.ExceptionHandlerContext;
import org.smartframework.cloud.utility.ObjectUtil;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

/**
 * 接口日志切面
 *
 * @author liyulin
 * @date 2019-04-08
 */
@Slf4j
public class ApiLogInterceptor implements MethodInterceptor, Ordered {

	@Override
	public int getOrder() {
		return OrderConstant.API_LOG;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// 请求前
		if (ObjectUtil.isNull(RequestContextHolder.getRequestAttributes())) {
			return invocation.proceed();
		}
		LogAspectDO logDO = new LogAspectDO();
		logDO.setReqStartTime(new Date());

		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		Method method = invocation.getMethod();
		String apiDesc = AspectInterceptorUtil.getControllerMethodDesc(method, request.getServletPath());
		logDO.setApiDesc(apiDesc);

		logDO.setReqParams(WebUtil.getRequestArgs(invocation.getArguments()));
		logDO.setReqHttpHeaders(SmartReqContext.getReqHttpHeadersBO());

		logDO.setUrl(request.getRequestURL().toString());
		logDO.setIp(WebUtil.getRealIP(request));
		logDO.setOs(request.getHeader("User-Agent"));
		logDO.setHttpMethod(request.getMethod());

		String classMethod = method.getDeclaringClass().getSimpleName() + SymbolConstant.DOT + method.getName();
		logDO.setClassMethod(classMethod);

		// 处理请求
		Object result = null;
		try {
			result = invocation.proceed();
			// 正常请求后
			logDO.setReqEndTime(new Date());
			logDO.setReqDealTime(getReqDealTime(logDO));
			logDO.setRespData(result);

			log.info(LogUtil.truncate("api.logDO.info=>{}", logDO));
			return result;
		} catch (Exception e) {
			logDO.setReqEndTime(new Date());
			logDO.setReqDealTime(getReqDealTime(logDO));

			log.error(LogUtil.truncate("api.logDO.error=>{}", logDO), e);

			RespHeadVO head = ExceptionHandlerContext.transRespHead(e);
			return new RespVO<>(head);
		}
	}

	private final int getReqDealTime(LogAspectDO logDto) {
		return (int) (logDto.getReqEndTime().getTime() - logDto.getReqStartTime().getTime());
	}

}