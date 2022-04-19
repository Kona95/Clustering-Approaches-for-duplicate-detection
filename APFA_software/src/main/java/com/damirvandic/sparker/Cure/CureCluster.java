package com.damirvandic.sparker.Cure;

import com.damirvandic.sparker.core.ProductDesc;
import com.damirvandic.sparker.util.IntPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CureCluster {
    private CureCluster leftParent;
    private CureCluster rightParent;

    private List<ProductDesc> products;

    public CureCluster(List<ProductDesc> products) {
        this.products = products;
    }

    public CureCluster() {
        this.products = new ArrayList<ProductDesc>();
    }

    public CureCluster(ProductDesc product) {
        this.products = new ArrayList<ProductDesc>();
        this.products.add(product);
    }

    public int getSize() {
        return this.products.size();
    }

    public List<ProductDesc> getProducts() {
        return products;
    }

    public double distance(CureCluster otherCureCluster, Map<IntPair,Double> distances){
        double distance=0;
        int count =0 ;
        for(ProductDesc p1:this.products){
            for(ProductDesc p2: otherCureCluster.getProducts()){
                IntPair key = new IntPair(p1.ID, p2.ID);
                distance+=distances.get(key);
                count++;
            }
        }
        return distance/count; // their average distance for all possible pairs
    }

    public CureCluster merge(CureCluster cureCluster) {
        List<ProductDesc> mergedProducts = new ArrayList<ProductDesc>();
        mergedProducts.addAll(cureCluster.getProducts());
        mergedProducts.addAll(this.getProducts());
        CureCluster newCureCluster = new CureCluster(mergedProducts);

        newCureCluster.leftParent = this;
        newCureCluster.rightParent = cureCluster;

        return newCureCluster;
    }

    @Override
    public String toString() {
        String s="C[";
        for(ProductDesc p1:this.products){
            s+=p1.ID+",";
        }
        s= (String) s.subSequence(0,s.length()-1);
        return s+"]";
    }
}
