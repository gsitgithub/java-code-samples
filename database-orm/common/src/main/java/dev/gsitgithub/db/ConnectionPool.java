package dev.gsitgithub.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

// {{start:poolFactory}}
class ConnectionPool {
    private ConnectionPool() {

    }
    /*
     * Expects a config in the following format
     *
     * poolName = "test pool"
     * jdbcUrl = ""
     * maximumPoolSize = 10
     * minimumIdle = 2
     * username = ""
     * password = ""
     * cachePrepStmts = true
     * prepStmtCacheSize = 256
     * prepStmtCacheSqlLimit = 2048
     * useServerPrepStmts = true
     *
     * Let HikariCP bleed out here on purpose
     */
    public static HikariDataSource getDataSourceFromConfig( String poolName, int maximumPoolSize,
                                                            int minimumIdle) {

        HikariConfig jdbcConfig = new HikariConfig();
        jdbcConfig.setPoolName(poolName);
        jdbcConfig.setMaximumPoolSize(maximumPoolSize);
        jdbcConfig.setMinimumIdle( minimumIdle );
        jdbcConfig.setJdbcUrl("jdbcurl");
        jdbcConfig.setUsername("username");
        jdbcConfig.setPassword("password");

        jdbcConfig.addDataSourceProperty("cachePrepStmts", true);
        jdbcConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        jdbcConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        jdbcConfig.addDataSourceProperty("useServerPrepStmts", true);

        // Add HealthCheck
        //jdbcConfig.setHealthCheckRegistry(healthCheckRegistry);

        // Add Metrics
        //jdbcConfig.setMetricRegistry(metricRegistry);
        return new HikariDataSource(jdbcConfig);
    }
}
// {{end:poolFactory}}
