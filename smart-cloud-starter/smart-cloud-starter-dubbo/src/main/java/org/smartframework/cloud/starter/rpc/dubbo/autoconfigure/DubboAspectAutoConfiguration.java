package org.smartframework.cloud.starter.rpc.dubbo.autoconfigure;

import org.apache.dubbo.config.annotation.DubboService;
import org.smartframework.cloud.starter.core.business.util.AspectInterceptorUtil;
import org.smartframework.cloud.starter.rpc.dubbo.interceptor.DubboLogInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * dubbo切面配置
 * 
 * @author liyulin
 * @date 2019-09-19
 */
@Configuration
@ConditionalOnProperty(name="smart.aspect.dubbolog", havingValue = "true")
public class DubboAspectAutoConfiguration {

	/**
	 * dubbo切面
	 * 
	 * @return
	 */
	@Bean
	public AspectJExpressionPointcut dubboServicePointcut() {
		AspectJExpressionPointcut dubboServicePointcut = new AspectJExpressionPointcut();
		String dubboExpression = AspectInterceptorUtil
				.getWithinExpression(Arrays.asList(DubboService.class));
		dubboServicePointcut.setExpression(dubboExpression);
		return dubboServicePointcut;
	}

	@Bean
	public DubboLogInterceptor dubboLogInterceptor() {
		return new DubboLogInterceptor();
	}

	@Bean
	public Advisor dubboLogAdvisor(final DubboLogInterceptor dubboLogInterceptor,
			final AspectJExpressionPointcut dubboServicePointcut) {
		DefaultBeanFactoryPointcutAdvisor dubboAdvisor = new DefaultBeanFactoryPointcutAdvisor();
		dubboAdvisor.setAdvice(dubboLogInterceptor);
		dubboAdvisor.setPointcut(dubboServicePointcut);

		return dubboAdvisor;
	}

}