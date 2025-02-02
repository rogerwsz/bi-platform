package com.baidu.rigel.biplatform.ma.ds.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
/**
 * 数据源连接信息之关系型数据库连接实现类
 * @author jiangyichao
 *
 */
public class RelationDBConnectionServiceImpl implements
		DataSourceConnectionService<Connection> {

	/**
	 * 日志对象
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RelationDBConnectionServiceImpl.class);
	/**
	 * @{inheritDoc}
	 */
	@Override
	public Connection createConnection(DataSourceDefine ds, String securityKey) throws DataSourceConnectionException {
		String dbUser = ds.getDbUser();
		String dbPwd = ds.getDbPwd();
		DataSourceType type = ds.getDataSourceType();
		String connUrl = this.getDataSourceConnUrl(ds);
		Connection conn = null;
        try {
            Class.forName(type.getDriver());
            String pwd = AesUtil.getInstance().decodeAnddecrypt(dbPwd, securityKey);
            LOG.info("[INFO]--- --- --- --- connect to database with user : {}", dbUser);
            StringBuilder pwdStr = new StringBuilder();
            for (char c : pwd.toCharArray()) {
                pwdStr.append(c >> 1);
            }
            // 设置数据库连接超时
            DriverManager.setLoginTimeout(10);
            LOG.info("[INFO]--- --- --- --- connect to database with pwd : {}", pwdStr.toString());
            conn = DriverManager.getConnection(connUrl, dbUser, pwd);
        } catch (ClassNotFoundException e) {
            LOG.error("[ERROR] --- --- --- --- connection to database error : {}", e.getMessage());
            LOG.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DataSourceConnectionException("ClassNotFoundException when create Relation Database DataSouceConnection! ", e);
        } catch (Exception e) {
        	LOG.error("[ERROR] --- --- --- --- connection to db error : {}", e.getMessage());
        	LOG.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DataSourceConnectionException("SQLException when create Relation Database DataSouceConnection! ", e);
        }
		return conn;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public boolean closeConnection(Connection conn) throws DataSourceConnectionException {
		if(conn != null) {
			try {
				conn.close();
				LOG.info("[INFO]--- --- --- --- close connection success");
				return true;
			} catch (SQLException e) {
				LOG.error("[ERROR] --- --- --- --- close connection: {}", e.getMessage());
			}
		}
		return false;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public String getDataSourceConnUrl(DataSourceDefine ds) throws DataSourceConnectionException {
        if (ds == null) {
            throw new DataSourceConnectionException("Datasource can not be null! ");
        }
        DataSourceType type = ds.getDataSourceType();
        String connUrl = type.getPrefix() + ds.getHostAndPort() + type.getDiv() + ds.getDbInstance();
        if (StringUtils.hasText(ds.getEncoding())) {
            if (type == DataSourceType.MYSQL || type == DataSourceType.MYSQL_DBPROXY) {
                connUrl = connUrl + "?useUniCode=true&characterEncoding=" + ds.getEncoding();
            }
        }
        LOG.debug("Conn URL: " + connUrl);
        return connUrl;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public boolean isValidateDataSource(DataSourceDefine ds, String securityKey) {
		Connection conn = null;
		try {
			conn = this.createConnection(ds, securityKey);	
			if (conn != null) {
				return true;
			}
			return false;
		} catch (DataSourceConnectionException e) {
			LOG.error("fail to create ds connection");
		} finally {
			if (conn != null) {
				try {
					this.closeConnection(conn);
				} catch (DataSourceConnectionException e) {
					LOG.error("fail to create ds connection");
				}
			}			
		}
		return false;
	}
	/**
	 * @{inheritDoc}
	 */
	@Override
	public SqlDataSourceInfo parseToDataSourceInfo(DataSourceDefine ds,
			String securityKey) throws DataSourceConnectionException {
        SqlDataSourceInfo dsInfo = new SqlDataSourceInfo(ds.getId());
        dsInfo.setDataBase(this.parseToDataBase(ds.getDataSourceType()));
        dsInfo.setDBProxy(true);
        try {
            dsInfo.setPassword(AesUtil.getInstance().decodeAnddecrypt(ds.getDbPwd(), securityKey));
        } catch (Exception e) {
            LOG.error("Encrypt password Fail !!", e);
            throw new RuntimeException(e);
        }
        dsInfo.setUsername(ds.getDbUser());
        dsInfo.setProductLine(ds.getProductLine());
        dsInfo.setInstanceName(ds.getDbInstance());
//        dsInfo.setDataSourceKey(dsDefine.getName());
        dsInfo.setDBProxy(true);
        List<String> urls = Lists.newArrayList();
        urls.add(this.getDataSourceConnUrl(ds));
        dsInfo.setJdbcUrls(urls);
        List<String> hosts = Lists.newArrayList();
        hosts.add(ds.getHostAndPort());
        dsInfo.setHosts(hosts);
        dsInfo.setDbPoolInfo(Maps.newHashMap());
        return dsInfo;
	}
	
    
    /**
     * 将silkroad数据源类型，向tesseract层数据库转换
     * @param dsType 数据源类型
     * @return 数据库类型
     */
    private DataBase parseToDataBase(DataSourceType dsType) {
        switch (dsType) {
            case MYSQL:
                return DataBase.MYSQL;
            case PALO:
                return DataBase.PALO;
            case ORACLE:
                return DataBase.ORACLE;
            case H2:
                return DataBase.H2;
            default:
                return DataBase.OTHER;
        }
    }

}
