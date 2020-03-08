package com.changgou.goods.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 商品信息组合对象
 *
 */
public class Goods implements Serializable {
   private Spu spu;
   private List<Sku> skus;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }
}
