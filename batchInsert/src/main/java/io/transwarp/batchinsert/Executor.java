package io.transwarp.batchinsert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Executor {
    // 查询SQL语句
    public static void testHBaseBatchInsertWithStructRowKey(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String tmp = Constant.BATCHINSERT_WITH_STRUCT_ROWKEY;
            tmp = tmp.replace("@","<");
            tmp = tmp.replace("#",">");
            String[] sql = tmp.split(";");
            int n = sql.length;
            for (int i = 0 ; i < n-1; ++i) {
                statement.execute(sql[i]);
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql[n-1]);
            for(int i = 1; i <= 100; i++) {
                preparedStatement.setString(1, "string-" + i);
                preparedStatement.setInt(2, i);
                preparedStatement.setInt(3, i+1);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询SQL语句
    public static void testHBaseBatchInsertWithoutStructRowKey(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String[] sql = Constant.BATCHINSERT_WITHOUT_STRUCT_ROWKEY.split(";");
            int n = sql.length;
            for (int i = 0 ; i < n-1; ++i) {
                statement.execute(sql[i]);
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql[n-1]);
            for(int i = 1; i <= 100; i++) {
                for (int j = 1; j <= 2; j++) {
                    preparedStatement.setInt(j, i);
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
