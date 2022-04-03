package tech.iresponse.orm;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSource {

    //public final ComboPooledDataSource dtsources = new ComboPooledDataSource();
    public final ComboPooledDataSource dtsources;

    public DataSource(String driver, String url, String username, String password) throws Exception {

        dtsources = new ComboPooledDataSource();
        this.dtsources.setDriverClass(driver);
        this.dtsources.setJdbcUrl(url);
        this.dtsources.setUser(username);
        this.dtsources.setPassword(password);
        this.dtsources.setInitialPoolSize(1);
        this.dtsources.setMinPoolSize(1);
        this.dtsources.setMaxPoolSize(10);
        this.dtsources.setAcquireIncrement(1);
        this.dtsources.setMaxStatements(500);
        this.dtsources.setMaxStatementsPerConnection(100);

    }
}
