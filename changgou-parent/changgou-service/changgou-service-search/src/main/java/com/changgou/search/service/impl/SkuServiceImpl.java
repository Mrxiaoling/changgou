package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /**
     * Elasticsearchtemplate ： 可以实现索引库的增删改查[高级搜索]
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate ;


    @Override
    public Map search(Map<String, String> searchMap) {
        //1.获取关键字的值
        String keywords = searchMap.get("keywords");

        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }
        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

//        //3.设置查询的条件
//
//        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));
//
//        //4.构建查询对象
//        NativeSearchQuery query = nativeSearchQueryBuilder.build();
//
//        //5.执行查询
//        /**
//         * 执行搜索，返回响应结果
//         * 1）搜索条件封装对象
//         * 2）搜索的结果集需要转换的类型
//         * 3)AggregatedPage<SkuInfo> : 搜索结果集的封装
//         */
//        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class);
//
//
//        //6.返回结果
//        Map resultMap = new HashMap<>();
//        resultMap.put("rows", skuPage.getContent());
//        resultMap.put("total", skuPage.getTotalElements());
//        resultMap.put("totalPages", skuPage.getTotalPages());
        //3.设置查询的条件

        //设置分组条件  商品分类
        /**
         * addAggregation ; 添加一个聚合操作
         * 1)terms ； 取别名
         * 2)field ; 表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));

        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询
        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class);

        //获取分组结果
        /**
         * getAggregation： 获取的是集合，可以根据多个域进行分组
         * skuCategorygroup : 获取指定域的集合数[手机，家用电器，手机配件]
         */
        StringTerms stringTerms = (StringTerms) skuPage.getAggregation("skuCategorygroup");

        List<String> categoryList = new ArrayList<>();

        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//分组的值
                categoryList.add(keyAsString);
            }
        }
        //6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("categoryList", categoryList);
        resultMap.put("rows", skuPage.getContent());
        resultMap.put("total", skuPage.getTotalElements());
        resultMap.put("totalPages", skuPage.getTotalPages());

        return resultMap;
    }

    @Override
    public void importData() {
        //feign调用查询List<Sku>
        Result<List<Sku>> skuResult = skuFeign.findAll();
        //将List<Sku>转成List<SkuInfo>
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()),SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList){
            //获取spec-》Map(String)->Map类型
            Map<String,Object> specMap = JSON.parseObject(skuInfo.getSpec(),Map.class);
            //如果需要生成动态的域，只需要将该域存入到一个Map<String,Object>对象中，该Map的key会生成一个域，域的名字为key
            //当前Map后面的Object的值会作为当前SKU对象域（key）对应的值
            skuInfo.setSpecMap(specMap);
        }
        //调用dao实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);
    }
}
