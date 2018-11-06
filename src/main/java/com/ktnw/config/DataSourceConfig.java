package com.ktnw.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@MapperScan(basePackages = "com.ktnw.mapper", sqlSessionTemplateRef  = "sqlSessionTemplate")
public class DataSourceConfig {

    /**
     * 配置分库分表策略
     *
     * @return
     * @throws SQLException
     */
    @Bean(name = "dataSource")
    DataSource shardingDataSource() throws SQLException {
        // 构建读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理. masterDataSource0, slaveDataSource00, slaveDataSource01等为使用DBCP等连接池配置的真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("dataSourceMaster", dataSourceMaster());
        dataSourceMap.put("dataSourceSalve0", dataSourceSalve0());
        dataSourceMap.put("dataSourceSalve1", dataSourceSalve1());

        // 构建读写分离配置
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setName("ds");
        masterSlaveRuleConfig.setMasterDataSourceName("dataSourceMaster");
        masterSlaveRuleConfig.getSlaveDataSourceNames().add("dataSourceSalve0");
        masterSlaveRuleConfig.getSlaveDataSourceNames().add("dataSourceSalve1");
        masterSlaveRuleConfig.setLoadBalanceAlgorithmType(MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN);

        // 配置Order表规则
        TableRuleConfiguration userTableRuleConfig = new TableRuleConfiguration();
        userTableRuleConfig.setLogicTable("t_user");
        userTableRuleConfig.setActualDataNodes("ds.t_user${0..1}");
        // 配置分库策略
        //orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
        // 配置分表策略
        userTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "t_user${user_id % 2}"));

        /// 通过ShardingSlaveDataSourceFactory继续创建ShardingDataSource
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(userTableRuleConfig);

        // 获取数据源对象
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), new Properties());
        return dataSource;
    }




    /**
     * 配置数据源0，数据源的名称最好要有一定的规则，方便配置分库的计算规则
     * @return
     */
//    spring.datasource.salve1.driver-class-name=com.mysql.jdbc.Driver
//    spring.datasource.salve1.url=jdbc:mysql://127.0.0.1:3306/salve1?useUnicode=true&characterEncoding=UTF-8
//    spring.datasource.salve1.username=root
//    spring.datasource.salve1.password=mysql123456
//    spring.datasource.salve1.type=com.alibaba.druid.pool.DruidDataSource
//    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource dataSourceMaster(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/db-master?useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456qwert");
        return druidDataSource;

    }
    /**
     * 配置数据源1，数据源的名称最好要有一定的规则，方便配置分库的计算规则
     * @return
     */
//    @ConfigurationProperties(prefix = "spring.datasource.salve0")
    public DataSource dataSourceSalve0(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/db-salve0?useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456qwert");
        return druidDataSource;
    }

    /**
     * 配置数据源2，数据源的名称最好要有一定的规则，方便配置分库的计算规则
     * @return
     */
//    @ConfigurationProperties(prefix = "spring.datasource.salve1")
    public DataSource dataSourceSalve1(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/db-salve1?useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456qwert");
        return druidDataSource;
    }



    /**
     * 需要手动配置事务管理器
     * @param dataSource
     * @return
     */
    @Bean
    public DataSourceTransactionManager transactitonManager(@Qualifier("dataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTypeAliasesPackage("com.ktnw.entity");//实体对应包路径
//        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}