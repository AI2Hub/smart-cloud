package org.smartframework.cloud.starter.mybatis.autoconfigure.tool;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.smartframework.cloud.starter.core.constants.SymbolConstant;
import org.smartframework.cloud.starter.mybatis.autoconfigure.InitTransactionalValue;
import org.smartframework.cloud.starter.mybatis.constant.MultipleDataSourceConstant;
import org.smartframework.cloud.starter.mybatis.plugin.MybatisSqlLogInterceptor;
import org.smartframework.cloud.starter.mybatis.properties.ShardingJdbcDatasourceProperties;
import org.smartframework.cloud.starter.mybatis.properties.SingleDatasourceProperties;
import org.smartframework.cloud.starter.mybatis.properties.SmartDatasourceProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.Assert;

import com.zaxxer.hikari.HikariDataSource;

import io.seata.rm.datasource.DataSourceProxy;
import lombok.extern.slf4j.Slf4j;

/**
 * 多数据源bean初始化
 * 
 * @author liyulin
 * @date 2019-05-28
 * @since DataSourceInitializerInvoker
 */
@Slf4j
public abstract class AbstractInitializerInvoker {

	private final SmartDatasourceProperties multipleDatasourceProperties;
	private final ConfigurableBeanFactory beanFactory;
	protected MybatisSqlLogInterceptor mybatisSqlLogInterceptor;
	/** jdbc url默认参数 */
	private static final String DEFAULT_JDBCURL_PARAMS = "serverTimezone=Asia/Shanghai&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true";

	public AbstractInitializerInvoker(final SmartDatasourceProperties smartDatasourceProperties,
			final ConfigurableBeanFactory beanFactory) {
		this.multipleDatasourceProperties = smartDatasourceProperties;
		this.beanFactory = beanFactory;
		
		this.mybatisSqlLogInterceptor = new MybatisSqlLogInterceptor();
		
		dynamicCreateSmartDataSourceBeans();
	}

	/**
	 * 动态创建多数据源的bean，并注册到容器中
	 */
	private void dynamicCreateSmartDataSourceBeans() {
		initDatasources(multipleDatasourceProperties.getDatasources());
		initShardingJdbcDatasources(multipleDatasourceProperties.getShardingDatasources());
	}
	
	/**
	 * 初始化普通（非sharding jdbc）数据源
	 * @param dataSources
	 */
	private void initDatasources(Map<String, SingleDatasourceProperties> dataSources) {
		if (dataSources == null || dataSources.isEmpty()) {
			return;
		}
		
		// 1、校验SingleDataSourceProperties的属性值
		for (Map.Entry<String, SingleDatasourceProperties> entry : dataSources.entrySet()) {
			SingleDatasourceProperties properties = entry.getValue();

			// 所有属性都不能为空
			boolean isAnyBlank = StringUtils.isAnyBlank(properties.getUrl(), properties.getUsername(),
					properties.getPassword(), properties.getMapperInterfaceLocation(),
					properties.getMapperXmlLocation());
			Assert.state(!isAnyBlank, SingleDatasourceProperties.class.getCanonicalName() + " attriutes存在未配置的！");
		}

		// 2、创建所有需要的bean，并加入到容器中
		dataSources.forEach((serviceName, properties)->{
			DataSource dataSource = createDataSource(serviceName, properties);
			
			initOtherBeanOfDatasource(serviceName, dataSource, properties);
		});
	}
	
	private DataSource createDataSource(String serviceName, SingleDatasourceProperties properties) {
		DataSource dataSource = createHikariDataSource(serviceName, properties);
		
		DataSource finalDataSource = dataSource;
		if (properties.isSeataEnable()) {
			finalDataSource = new DataSourceProxy(dataSource);
			// 注册bean
			registerBean(serviceName+"DataSourceProxy", finalDataSource);
		}
		
		return finalDataSource;
	}
	
	/**
	 * 初始化数据源的其他bean
	 * @param serviceName
	 * @param dataSource
	 * @param properties
	 */
	private void initOtherBeanOfDatasource(String serviceName, DataSource dataSource,
			SingleDatasourceProperties properties) {
		// 2.1、SqlSessionFactoryBean
		String sqlSessionFactoryBeanName = generateBeanName(serviceName,
				MultipleDataSourceConstant.SQL_SESSIONFACTORY_BEAN_NAME_SUFFIX);
		registerSqlSessionFactoryBean(sqlSessionFactoryBeanName, properties, dataSource);

		// 2.2、MapperScannerConfigurer
		registerMapperScannerConfigurer(serviceName, sqlSessionFactoryBeanName, properties);

		// 2.3、DataSourceTransactionManager
		String transactionManagerBeanName = generateBeanName(serviceName,
				MultipleDataSourceConstant.TRANSACTION_MANAGER_NAME_SUFFIX);
		registerDataSourceTransactionManager(transactionManagerBeanName, dataSource);

		// 2.4、cache transaction info
		cacheTransactionManagerInfo(properties.getTransactionBasePackages(), transactionManagerBeanName);
	}
	
	/**
	 * 初始化sharding jdbc数据源配置
	 * @param shardingDatasources
	 */
	private void initShardingJdbcDatasources(Map<String, ShardingJdbcDatasourceProperties> shardingDatasources) {
		if (shardingDatasources == null || shardingDatasources.isEmpty()) {
			return;
		}

		shardingDatasources.forEach((serviceName, config) -> {
			Map<String, SingleDatasourceProperties> shardingJdbcDatasourcesMap = config.getDataSources();
			Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(shardingJdbcDatasourcesMap.size());
			shardingJdbcDatasourcesMap.forEach((name, properties) -> {
				DataSource dataSource = createDataSource(name, properties);
				
				dataSourceMap.put(name, dataSource);
			});

			try {
				DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap,
						new ShardingRuleConfigurationYamlSwapper().swap(config.getShardingRule()), config.getProps());

				String dataSourceBeanName = generateBeanName(serviceName, ShardingDataSource.class.getSimpleName());
				registerBean(dataSourceBeanName, dataSource);
				
				shardingJdbcDatasourcesMap
						.forEach((name, properties) -> initOtherBeanOfDatasource(name, dataSource, properties));
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
		});
	}

	/**
	 * 缓存事务信息
	 * @param transactionBasePackages
	 * @param transactionManagerBeanName
	 */
	private void cacheTransactionManagerInfo(String transactionBasePackages, String transactionManagerBeanName) {
		if (StringUtils.isBlank(transactionBasePackages)) {
			return;
		}

		String[] transactionBasePackageArray = transactionBasePackages.split(SymbolConstant.COMMA);
		for (String transactionBasePackage : transactionBasePackageArray) {
			InitTransactionalValue.getMultipleTransactionManagerInfoCache().putIfAbsent(transactionBasePackage,
					transactionManagerBeanName);
		}
	}

	/**
	 * 创建并注册<code>HikariDataSource</code>
	 * 
	 * @param serviceName
	 * @param dataSourceProperties
	 * @return
	 */
	private DataSource createHikariDataSource(String serviceName, SingleDatasourceProperties dataSourceProperties) {
		String dataSourceBeanName = generateBeanName(serviceName, HikariDataSource.class.getSimpleName());
		// 构建bean对象
		HikariDataSource dataSource = new HikariDataSource();
		String jdbcUrl = dataSourceProperties.getUrl();
		// 如果jdbcUrl没有设置参数，则用默认设置
		if (StringUtils.containsNone(jdbcUrl, SymbolConstant.QUESTION_MARK)) {
			jdbcUrl += SymbolConstant.QUESTION_MARK + DEFAULT_JDBCURL_PARAMS;
		}
		dataSource.setJdbcUrl(jdbcUrl);
		dataSource.setUsername(dataSourceProperties.getUsername());
		dataSource.setPassword(dataSourceProperties.getPassword());
		dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
		// 注册bean
		registerBean(dataSourceBeanName, dataSource);

		return dataSource;
	}

	/**
	 * 创建并注册“SqlSessionFactoryBean”
	 * 
	 * @param beanName
	 * @param dataSourceProperties
	 * @param dataSource
	 * @return
	 */
	protected abstract void registerSqlSessionFactoryBean(String beanName,
			SingleDatasourceProperties dataSourceProperties, DataSource dataSource);

	/**
	 * 创建并注册“MapperScannerConfigurer”
	 * 
	 * @param serviceName
	 * @param sqlSessionFactoryBeanName
	 * @param dataSourceProperties
	 * @return
	 */
	protected abstract void registerMapperScannerConfigurer(String serviceName,
			String sqlSessionFactoryBeanName, SingleDatasourceProperties dataSourceProperties);

	/**
	 * 创建并注册<code>DataSourceTransactionManager</code>
	 * 
	 * @param transactionManagerBeanName
	 * @param dataSource
	 * @return
	 */
	private void registerDataSourceTransactionManager(String transactionManagerBeanName, DataSource dataSource) {
		// 构建bean对象
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
		// 注册bean
		registerBean(transactionManagerBeanName, dataSourceTransactionManager);
	}

	/**
	 * 将bean注册到容器
	 * 
	 * @param beanName
	 * @param singletonObject
	 */
	protected void registerBean(String beanName, Object singletonObject) {
		beanFactory.registerSingleton(beanName, singletonObject);
	}

	/**
	 * 生成bean名称
	 * 
	 * @param serviceName   服务名
	 * @param beanClassName bean类名
	 * @return
	 */
	protected String generateBeanName(String serviceName, String beanClassName) {
		return serviceName + beanClassName;
	}

}