package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

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
        for (Map.Entry<String, String> entry: searchMap.entrySet()
             ) {
            System.out.println(entry.getKey() + "...." + entry.getValue());
        }
        //1.获取关键字的值
        String keywords = searchMap.get("keywords");
        System.out.println(keywords);

        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }
        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //设置分组条件  商品分类
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
        //设置分组条件  商品品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(100));
        //设置分组条件  商品的规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(100));
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //构建排序查询
        String sortRule = searchMap.get("sortRule");
        String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equals("DESC") ? SortOrder.DESC : SortOrder.ASC));
        }
        //构建分页查询
        Integer pageNum = 1;
        if (!StringUtils.isEmpty(searchMap.get("pageNum"))) {
            try {
                pageNum = Integer.valueOf(searchMap.get("pageNum"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                //如果用户不传分页参数，则默认第一页
                pageNum=1;
            }
        }
        //默认查询的数据条数
        Integer pageSize = 3;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, pageSize));
        //当用户选择了品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据是用于显示品牌搜索条件的
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
        }
        //同理，当用户选择了分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
        }
        //规格过滤：spec_网络=联通3G&spec_颜色=红
        for (Map.Entry<String, String> entry : searchMap.entrySet()){
            String key = entry.getKey();
            //如果key以spec_开始，则表示规格筛选查询
            if (key.startsWith("spec_")){
                //规格条件的值
                String value =entry.getValue();
                boolQueryBuilder.filter(QueryBuilders.termQuery("specMap."+key.substring(5) + ".keyword", value));
            }
        }
        //价格过滤查询
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");
            if (!split[1].equalsIgnoreCase("*")) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }
        //设置高亮条件
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));

        //设置主关键字查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"name","brandName","categoryName"));
        //构建过滤查询
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询
        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class,new SearchResultMapperImpl());

        //获取分组结果
        /**
         * getAggregation： 获取的是集合，可以根据多个域进行分组
         * skuCategorygroup : 获取指定域的集合数[手机，家用电器，手机配件]
         */
        StringTerms stringTerms = (StringTerms) skuPage.getAggregation("skuCategorygroup");
        //获取分组结果  商品品牌
        StringTerms stringTermsBrand = (StringTerms) skuPage.getAggregation("skuBrandgroup");
        //获取分组结果，商品规格数据
        StringTerms stringTermSpec  = (StringTerms) skuPage.getAggregation("skuSpecgroup");
        List<String> categoryList = getStringsCategoryList(stringTerms);
        List<String> brandList = getStringsBrandList(stringTermsBrand);
        Map<String, Set<String>> specMap = getStringSetMap(stringTermSpec);
        //6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("specMap", specMap);
        resultMap.put("categoryList", categoryList);
        resultMap.put("brandList", brandList);
        resultMap.put("rows", skuPage.getContent());
        resultMap.put("total", skuPage.getTotalElements());
        resultMap.put("totalPages", skuPage.getTotalPages());

        return resultMap;
    }

    /**
     * 获取规格列表数据
     *
     * @param stringTermsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(StringTerms stringTermsSpec) {
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specList = new HashSet<>();
        if (stringTermsSpec != null) {
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()) {
                specList.add(bucket.getKeyAsString());
            }
        }
        for (String specjson : specList) {
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {//
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();    //规格选项值
                //获取当前规格名字对应的规格数据
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }

    private List<String> getStringsCategoryList(StringTerms stringTerms) {
        List<String> categoryList = new ArrayList<>();

        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//分组的值
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }

    /**
     * 获取品牌列表
     *
     * @param stringTermsBrand
     * @return
     */
    private List<String> getStringsBrandList(StringTerms stringTermsBrand) {
        List<String> brandList = new ArrayList<>();
        if (stringTermsBrand != null) {
            for (StringTerms.Bucket bucket : stringTermsBrand.getBuckets()) {
                brandList.add(bucket.getKeyAsString());
            }
        }
        return brandList;
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
