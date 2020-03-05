package com.changgou.controller;

import com.changgou.goods.pojo.Brand;
import com.changgou.service.BrandService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/brand")
@CrossOrigin//跨域
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 分页+条件
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Brand> > findPage(@RequestBody Brand brand,
                                             @PathVariable(value = "page")Integer page,
                                             @PathVariable(value = "size")Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(brand,page, size);
        return new Result<PageInfo<Brand> >(true,StatusCode.OK,"分页查询成功",pageInfo);
    }

    /**
     * 分页查询
     * @param page 当前页
     * @param size 每页显示的条数
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Brand> > findPage(@PathVariable(value = "page")Integer page,
                                    @PathVariable(value = "size")Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(page, size);
        return new Result<PageInfo<Brand> >(true,StatusCode.OK,"分页查询成功",pageInfo);
    }

    /**
     * 根据条件查询
     */
    @PostMapping(value = "/search")
    public Result<List<Brand>> findList(@RequestBody Brand brand){
        List<Brand> list = brandService.findList(brand);
        return new Result<List<Brand>>(true,StatusCode.OK,"查询成功",list);


    }

    /**
     * 根据ID删除
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable(value = "id") Integer id){
        brandService.delete(id);
        return new Result(true,StatusCode.OK,"删除品牌成功");
    }

    /**
     * 根据ID修改品牌
     * @param id 商品ID
     * @param brand 品牌
     * @return 信息
     */
    @PutMapping(value = "/{id}")
    public Result update(@PathVariable(value = "id") Integer id,@RequestBody Brand brand){
        brand.setId(id);
        brandService.update(brand);
        return new Result(true,StatusCode.OK,"修改品牌成功");
    }

    /***
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result<Brand> findAll(){
        List<Brand> brandList = brandService.findAll();
        return new Result<Brand>(true, StatusCode.OK,"查询成功",brandList) ;
    }

    /**
     * 根据ID查询品牌
     * @param id 品牌ID
     * @return 品牌
     */
    @GetMapping(value = "/{id}")
    public Result<Brand> findById(@PathVariable(value = "id")Integer id){
        Brand brand = brandService.findById(id);
        return new Result<Brand>(true, StatusCode.OK,"查询成功",brand);
    }

    /**
     * 增加品牌
     * @param brand
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Brand brand){
        brandService.add(brand);
        return new Result(true, StatusCode.OK,"查询成功");
    }
}
