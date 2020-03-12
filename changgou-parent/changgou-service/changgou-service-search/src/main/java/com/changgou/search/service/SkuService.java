package com.changgou.search.service;

import java.util.Map;

public interface SkuService {

    /***
     * 条件搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);
    /**
     * 导入数据到索引库中
     */
    void importData();
}
