package com.changgou.search.dao;

import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuEsMapper extends ElasticsearchCrudRepository<SkuInfo,Long> {
}
