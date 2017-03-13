package io.transwarp.hbasees;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import java.net.InetAddress;
import java.util.ArrayList;

/*
create table h39a (
key string,
content string
) STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' WITH SERDEPROPERTIES
("hbase.columns.mapping"=":key,c1:content")
TBLPROPERTIES ("hbase.table.name"="h39a");

insert into h39a values("1","this is mysql");
insert into h39a values("2","this is mysql");
insert into h39a values("3","this is mysql");
insert into h39a values("4","this is oracle");
insert into h39a values("5","this is oracle");
insert into h39a values("6","this is sqlserver");
insert into h39a values("7","this is hive");
insert into h39a values("8","this is postgresql");

create table e39a (
key string,
content string
) stored as es;

insert into e39a values("01","mysql");
insert into e39a values("02","hive");
insert into e39a values("03","sqlserver");
insert into e39a values("04","oracle");
insert into e39a values("05","db2");
insert into e39a values("06","postgresql");
insert into e39a values("07","odps");
 */

public class search {
    // 使用ES进行模糊查询
    public static ArrayList<String> wildcardQuery() throws Exception {
        // 设置ES集群名，IP
        Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch1").build();
        TransportClient transportClient = TransportClient.builder().
                settings(settings).build().addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName("172.16.1.34"), 9300));
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch("sqd.e39a");
        // ES模糊查询语句
        SearchResponse searchResponse = searchRequestBuilder.
                setQuery(QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery("content","*sql*")))
                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();
        SearchHits searchHits = searchResponse.getHits();
        ArrayList<String> arrayList = new ArrayList<>();
        System.out.println();
        System.out.println("Total Hits is " + searchHits.totalHits());
        System.out.println();
        // 返回ES查询内容
        for (int i = 0; i < searchHits.getHits().length; ++i) {
//            System.out.println("content is " + searchHits.getHits()[i].getSource().get("content"));
            arrayList.add(String.valueOf(searchHits.getHits()[i].getSource().get("content")));
        }
        return arrayList;
    }

    // Hyperbase查询
    public static void hbaseSearch(ArrayList<String> arrayList) throws Exception {
        // 加载配置
        Configuration conf = new Configuration();
        conf.set("hbase.zookeeper.quorum", "tdh1,tdh2,tdh3");
        conf.set("zookeeper.znode.parent", "/hyperbase1");
        HTable hTable = new HTable(conf, "h39a");
        System.out.println("Scanning table... ");
        // 遍历通过ES查询得到的数据
        for (String a : arrayList) {
            ArrayList<Filter> filters = new ArrayList<>();
            // 设置Hyperbase中表的列簇，列
            SingleColumnValueFilter colValFilter = new SingleColumnValueFilter(Bytes.toBytes("c1"),
                    Bytes.toBytes("content"), CompareFilter.CompareOp.EQUAL,
                    new SubstringComparator(a));
            /*
            CompareFilter.CompareOp的操作符
            LESS,
            LESS_OR_EQUAL,
            EQUAL,
            NOT_EQUAL,
            GREATER_OR_EQUAL,
            GREATER,
            NO_OP;
             */
            colValFilter.setFilterIfMissing(false);
            filters.add(colValFilter);

            FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL, filters);

            Scan scan = new Scan();
            scan.setFilter(fl);
            scan.addColumn(Bytes.toBytes("c1"), Bytes.toBytes("content"));

            ResultScanner scanner = hTable.getScanner(scan);
            String key = new String("~");
            String keyFlag = new String("~");

            // 输出查询结果
            for (Result result : scanner) {
                key = "~";
                for (KeyValue kv : result.raw()) {
                    if (key.compareTo(keyFlag) == 0) {
                        key = Bytes.toString(kv.getRow());
                        System.out.println("Rowkey: " + key +
                                ", " + Bytes.toString(kv.getFamily()) + "." + Bytes.toString(kv.getQualifier()) +
                                " = " + Bytes.toString(kv.getValue()));
                    }
                }
            }
            scanner.close();
        }
        System.out.println("Completed ");
    }

    public static void main(String[] args) throws Exception {
        ArrayList<String> arrayList = wildcardQuery();
        hbaseSearch(arrayList);
    }
}
