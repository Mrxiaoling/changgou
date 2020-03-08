package com.changgou.goods.dao;
import com.changgou.goods.pojo.Category;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:shenkunlin
 * @Description:Category的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface CategoryMapper extends Mapper<Category> {
//    @Select("SELECT tb.* FROM tb_brand tb,tb_category_brand tcb  WHERE tb.id=tcb.brand_id AND tvb.categogy_id=#{pid}")
//    List<Category> findByParentId(Integer pid);
}
