package com.changgou.service;

import com.changgou.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BrandService {

    /**
     * 条件+分页查询
     */
    PageInfo<Brand> findPage(Brand brand,Integer page, Integer size);

    /**
     * 条件查询：分页
     */
    PageInfo<Brand> findPage(Integer page, Integer size);



    /**
     * 根据品牌信息多条件查询
     */
    List<Brand> findList(Brand brand);

    /**
     * 根据ID删除品牌
     */
    void delete(Integer id);
    /**
     * 根据ID修改品牌
     */
    void update(Brand brand);

    /**
     * 根据ID查询
     */
    Brand findById(Integer id);

    /***
     * 查询所有品牌
     * @return
     */
    List<Brand> findAll();

    /**
     * 增加品牌
     */
    void add(Brand brand);
}
