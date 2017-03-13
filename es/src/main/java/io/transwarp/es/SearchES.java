package io.transwarp.es;

import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;

import java.net.InetAddress;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class SearchES {
    /*
    create table es_start (key string,
    fieldA string,
    fieldB int)
    stored as es;

    create external table es_start_ex (
    key string,
    fieldA string,
    fieldB int)
    stored as es
    TBLPROPERTIES('elasticsearch.tablename'='sqd.es_start');

    insert into es_start_ex values ("1","A",2);
    insert into es_start_ex values ("2","A",3);
    insert into es_start_ex values ("3","B",13);
    insert into es_start_ex values ("4","C",1);
    */
    // 获取ES表的某些字段的count,sum,avg,max值等,基于类似SQL的group by
    // ES统计查询
    public static void statsQuery() {
        try {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch1").build();
            TransportClient transportClient = TransportClient.builder().
                    settings(settings).build().addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName("172.16.2.95"), 9300));
            SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch("sqd.es_start");

            TermsBuilder termsBuilder = AggregationBuilders.terms("my_fieldA").field("fielda").size(100);
            termsBuilder.subAggregation(AggregationBuilders.sum("my_sum_fieldB").field("fieldb"));
            termsBuilder.subAggregation(AggregationBuilders.avg("my_avg_fieldB").field("fieldb"));
            termsBuilder.subAggregation(AggregationBuilders.max("my_max_fieldB").field("fieldb"));

            searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery()).addAggregation(termsBuilder);
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

            Terms terms = searchResponse.getAggregations().get("my_fieldA");
            for (Terms.Bucket bucket : terms.getBuckets()) {
                StringBuilder stringBuilder = new StringBuilder();
                String key = (String) bucket.getKey();
//                System.out.println("key is " + key);

                long count = bucket.getDocCount();
//                System.out.println("count is " + count);

                Sum sumAgg = bucket.getAggregations().get("my_sum_fieldB");
                long sum = (long) sumAgg.getValue();
//                System.out.println("sum is " + sum);

                Avg avgAgg = bucket.getAggregations().get("my_avg_fieldB");
                double avg = avgAgg.getValue();
//                System.out.println("avg is " + avg);

                Max maxAgg = bucket.getAggregations().get("my_max_fieldB");
                double max = maxAgg.getValue();
//                System.out.println("max is " + max);

                stringBuilder.append("key is " + key + ", count is " + count +
                        ", sum is " + sum + ", avg is " + avg + ", max is " + max);
                System.out.println(stringBuilder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // wildcard查询/or条件/and条件
    public static void wildcardQuery() {
        try {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch1").build();
            TransportClient transportClient = TransportClient.builder().
                    settings(settings).build().addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName("172.16.2.94"), 9300));
            SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch("sqd.es_start");

//            SearchResponse searchResponse = searchRequestBuilder.
//                    setQuery(QueryBuilders.wildcardQuery("content","*oracle*"))
//                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();

//            SearchResponse searchResponse = searchRequestBuilder.
//                    setQuery(QueryBuilders.boolQuery()
//                    .filter(QueryBuilders.wildcardQuery("content","*oracle*"))
//                    .filter(QueryBuilders.wildcardQuery("tint","1")))
//                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();

//            SearchResponse searchResponse = searchRequestBuilder.
//                    setQuery(QueryBuilders.boolQuery()
//                    .must(QueryBuilders.wildcardQuery("content","*oracle*"))
//                    .must(QueryBuilders.wildcardQuery("tint","1")))
//                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();


//{"query": {"bool": {"must": [{"or": [{"term": {"content": "oracle"}},{"term": {"content": "oracle is database"}}]}],"must_not": [],"should": []}},"from": 0,"size": 10,"sort": [],"aggs": {}}
//            SearchResponse searchResponse = searchRequestBuilder.
//                    setQuery(QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termsQuery("content", "oracle", "oracle is database")))
//                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();

//{"query": {"bool": {"must": [{"or": [{"wildcard": {"content": "*oracle*"}},{"wildcard": {"content": "*mysql*"}}]}],"must_not": [],"should": []}},"from": 0, "size": 10, "sort": [],"aggs": {}}
//            SearchResponse searchResponse = searchRequestBuilder.
//                    setQuery(QueryBuilders.boolQuery()
//                    .must(QueryBuilders.orQuery(QueryBuilders.wildcardQuery("content","*mysql*"),
//                            QueryBuilders.wildcardQuery("content","*oracle*")))
//                    .must(QueryBuilders.termQuery("tbool","false")))
//                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();
            SearchResponse searchResponse = searchRequestBuilder.
                    setQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchPhraseQuery("content","*oracle*")))
                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();

            SearchHits searchHits = searchResponse.getHits();
            System.out.println();
            System.out.println("Total Hits is " + searchHits.totalHits());
            System.out.println();
            for (int i = 0; i < searchHits.getHits().length; ++i) {
                System.out.println("content is "
                        + searchHits.getHits()[i].getSource().get("content"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void multisearch() {
        try {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch1").build();
            TransportClient transportClient = TransportClient.builder().
                    settings(settings).build().addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName("172.16.2.93"), 9300));
            SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch("service2","clients");
            SearchResponse searchResponse = searchRequestBuilder.
                    setQuery(QueryBuilders.boolQuery()
                            .should(QueryBuilders.termQuery("id","5"))
                            .should(QueryBuilders.prefixQuery("content","oracle")))
                    .setFrom(0).setSize(100).setExplain(true).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            System.out.println();
            System.out.println("Total Hits is " + searchHits.totalHits());
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void jsonquery() {
        try {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch1").build();
            TransportClient transportClient = TransportClient.builder().
                    settings(settings).build().addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName("172.16.2.93"), 9300));
            SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch("service2");
            SearchResponse searchResponse = searchRequestBuilder.setSource("{\n" +
                    "\"query\": {\n" +
                    "\"bool\": {\n" +
                    "\"must\": [\n" +
                    "{\n" +
                    "\"prefix\": {\n" +
                    "\"content\": \"oracle\"\n" +
                    "}\n" +
                    "}\n" +
                    "],\n" +
                    "\"must_not\": [ ],\n" +
                    "\"should\": [ ]\n" +
                    "}\n" +
                    "},\n" +
                    "\"from\": 0,\n" +
                    "\"size\": 10,\n" +
                    "\"sort\": [ ],\n" +
                    "\"aggs\": { }\n" +
                    "}")
                    .get();
            SearchHits searchHits = searchResponse.getHits();
            System.out.println();
            System.out.println("Total Hits is " + searchHits.totalHits());
            System.out.println();
            for (int i = 0; i < searchHits.getHits().length; ++i) {
                System.out.println("content is "
                        + searchHits.getHits()[i].getSource().get("content"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createByJson() {
        try {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch1").build();
            TransportClient client = TransportClient.builder().
                    settings(settings).build().addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName("172.16.2.93"), 9300));
            XContentBuilder mapping = jsonBuilder()
                    .startObject()
                    .startObject("general")
                    .startObject("properties")
                    .startObject("message")
                    .field("type", "string")
                    .field("index", "not_analyzed")
                    .endObject()
                    .startObject("source")
                    .field("type","string")
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();
            client.admin().indices().prepareCreate("t2")
                    .setSettings(Settings.builder()
                            .put("index.number_of_shards", 3)
                            .put("index.number_of_replicas", 2)
                    )
                    .get();
            client.admin().indices().preparePutMapping("t2")
                    .setType("general")
                    .setSource(mapping)
                    .execute().actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        multisearch();
//        statsQuery();
//        wildcardQuery();
//        jsonquery();
//        createByJson();
    }
}
