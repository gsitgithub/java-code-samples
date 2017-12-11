package dev.gsitgithub.db;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// {{start:pools}}
public class ConnectionPools {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPools.class);
    /*
     * Normally we would be using the app config but since this is an example
     * we will be using a localized example config.
     */
    /*
     *  This pool is made for short quick transactions that the web application uses.
     *  Using enum singleton pattern for lazy singletons
     */
    private enum Transactional {
        INSTANCE(ConnectionPool.getDataSourceFromConfig("transactional", 10,2));
        private final DataSource dataSource;
        private Transactional(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        public DataSource getDataSource() {
            return dataSource;
        }
    }
    public static DataSource getTransactional() {
        return Transactional.INSTANCE.getDataSource();
    }

    /*
     *  This pool is designed for longer running transactions / bulk inserts / background jobs
     *  Basically if you have any multithreading or long running background jobs
     *  you do not want to starve the main applications connection pool.
     *
     *  EX.
     *  You have an endpoint that needs to insert 1000 db records
     *  This will queue up all the connections in the pool
     *
     *  While this is happening a user tries to log into the site.
     *  If you use the same pool they may be blocked until the bulk insert is done
     *  By splitting pools you can give transactional queries a much higher chance to
     *  run while the other pool is backed up.
     */
    private enum Processing {
        INSTANCE(ConnectionPool.getDataSourceFromConfig("processing", 30, 2));
        private final DataSource dataSource;
        private Processing(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        public DataSource getDataSource() {
            return dataSource;
        }
    }

    public static DataSource getProcessing() {
        return Processing.INSTANCE.getDataSource();
    }

    public static void main(String[] args) {
        logger.debug("starting");
        DataSource processing = ConnectionPools.getProcessing();
        logger.debug("processing started");
        DataSource transactional = ConnectionPools.getTransactional();
        logger.debug("transactional started");
        logger.debug("done");
    }
}
// {{end:pools}}

