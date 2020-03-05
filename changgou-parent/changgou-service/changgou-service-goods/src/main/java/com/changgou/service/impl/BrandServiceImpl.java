package com.changgou.service.impl;

import com.changgou.dao.BrandMapper;
import com.changgou.goods.pojo.Brand;
import com.changgou.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService{

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 条件+分页
     * @param brand 条件
     * @param page 当前页
     * @param size 每页大小
     * @return 结果集
     */
    @Override
    public PageInfo<Brand> findPage(Brand brand, Integer page, Integer size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索数据
        Example example = createExample(brand);
        List<Brand> brands = brandMapper.selectByExample(example );
        //封装PageInfo<Brand>
        return new PageInfo<Brand>(brands);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Brand> findPage(Integer page, Integer size) {
        PageHelper.startPage(page,size);
        //查询集合
        List<Brand> brands = brandMapper.selectAll();
        //封装pageInfo
        return new PageInfo<Brand>(brands);
    }

    @Override
    public List<Brand> findList(Brand brand) {
        //自定义条件搜索对象Example
        Example example = createExample(brand);
        return brandMapper.selectByExample(example);
    }

    /**
     * 查询条件代码抽取
     * @param brand
     * @return
     */
    public Example createExample (Brand brand){
        Example example = new Example(Brand.class);
        //条件构造器
        Example.Criteria criteria = example.createCriteria();
        if (brand!=null){
            if (!StringUtil.isEmpty(brand.getName())){
                criteria.andLike("name", "%"+brand.getName()+"%");
            }

            if (!StringUtil.isEmpty(brand.getLetter())){
                criteria.andEqualTo("letter", brand.getLetter());
            }
        }
        return example;
    }

    /**
     * 根据ID删除品牌
     */
    @Override
    public void delete(Integer id) {
        brandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据ID修改品牌
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 根据ID查询结果
     * @param id 商品ID
     * @return 商品
     */
    @Override
    public Brand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 全部数据
     * @return
     */
    @Override
    public List<Brand> findAll(){
        return brandMapper.selectAll();
    }

    @Override
    public void add(Brand brand) {
        //方法中但凡带有selective，会忽略空值
        brandMapper.insertSelective(brand);
    }
}
