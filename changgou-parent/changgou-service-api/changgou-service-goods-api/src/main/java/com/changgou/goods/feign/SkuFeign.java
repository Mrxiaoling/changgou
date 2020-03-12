package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
//goods在application.yml可以查看，application：name； goods
@FeignClient(value = "goods")
@RequestMapping("/sku")
public interface SkuFeign {
    /**
     * 查询SKU所有数据
     * @return
     */
    @GetMapping
    Result<List<Sku>> findAll();
}
