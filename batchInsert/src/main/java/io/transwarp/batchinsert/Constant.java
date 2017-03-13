package io.transwarp.batchinsert;

import org.apache.hadoop.conf.Configuration;

public class Constant {
    private static Configuration configuration = new Configuration();

    // 加载配置
    static {
        try {
            configuration.addResource("setup.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 配置常量
    static String DRIVER_NAME = configuration.get("driver_name");
    static String MODE = configuration.get("mode");
    static String SIMPLE_JDBC_URL = configuration.get("simple_jdbc_url");
    static String LDAP_JDBC_URL = configuration.get("ldap_jdbc_url");
    static String LDAP_NAME = configuration.get("ldap_name");
    static String LDAP_PASSWD = configuration.get("ldap_passwd");
    static String KERBEROS_JDBC_URL = configuration.get("kerberos_jdbc_url");
    static String BATCHINSERT_WITH_STRUCT_ROWKEY = configuration.get("batchinsert_with_struct_rowkey");
    static String BATCHINSERT_WITHOUT_STRUCT_ROWKEY = configuration.get("batchinsert_without_struct_rowkey");
    static String THREAD_POOL_SIZE = configuration.get("thread_pool_size");
    static String THREAD_NUM = configuration.get("thread_num");
}
