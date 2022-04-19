package com.damirvandic.sparker.Clarans;

import com.damirvandic.sparker.core.ClusteringProcedure;
import com.damirvandic.sparker.core.Clusters;
import com.damirvandic.sparker.core.ClustersBuilder;
import com.damirvandic.sparker.core.ProductDesc;
import com.damirvandic.sparker.util.IntPair;
import gnu.trove.map.TIntObjectMap;
//import jdk.jfr.internal.LogLevel;
//import jdk.jfr.internal.LogTag;
//import jdk.jfr.internal.Logger;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * implementation from https://github.com/haifengl/smile/blob/master/core/src/main/java/smile/clustering/CLARANS.java
 */
public class ClaransClustering implements ClusteringProcedure {
    private int k;
    private int numlocal;
    private int maxneighbor;
    private TIntObjectMap<ProductDesc> index;
    private Map<IntPair, Double> distances;
    private Random random = new Random();

    @Override
    public Clusters createClusters(Map<String, Object> conf, Map<IntPair, Double> similarities, TIntObjectMap<ProductDesc> index) {
        this.k=(int)conf.getOrDefault("k",10);
        this.numlocal=(int)conf.getOrDefault("numlocal",10);
        this.maxneighbor=(int)conf.getOrDefault("maxneighbor",10);
        this.index=index;
        this.distances=this.similaritiesToDistances(similarities);
        return this.fit();

    }

    private Clusters fit(){
        int n = index.size();
        if (k >= n) {
//            throw new IllegalArgumentException("Too large k: " + k);
            k=(int)Math.floor(n*0.8);
        }
        if (this.maxneighbor > n) {
            throw new IllegalArgumentException("Too large maxNeighbor: " + this.maxneighbor);
        }
        int minmax = 100;
        if (this.k * (n - this.k) < minmax) {
            minmax = this.k * (n - this.k);
        }
        if (this.maxneighbor < minmax) {
            this.maxneighbor = minmax;
        }
        ProductDesc[] medoids = (ProductDesc[]) java.lang.reflect.Array.newInstance(ProductDesc.class, k);
        ProductDesc[] newMedoids = medoids.clone();
        int[] y = new int[n];
        int[] newY = new int[n];
        double[] newD = new double[n];
        ProductDesc[] data = index.valueCollection().toArray(new ProductDesc[0]);
        double[] d = seed(data, medoids, y);
        double distortion = Arrays.stream(d).sum();
        System.arraycopy(medoids, 0, newMedoids, 0, k);
        System.arraycopy(y, 0, newY, 0, n);
        System.arraycopy(d, 0, newD, 0, n);
        for (int neighborCount = 1; neighborCount <= this.maxneighbor; neighborCount++) {
            double randomNeighborDistortion = getRandomNeighbor(data, newMedoids, newY, newD);
            if (randomNeighborDistortion < distortion) {
                neighborCount = 0;
                distortion = randomNeighborDistortion;
                System.arraycopy(newMedoids, 0, medoids, 0, k);
                System.arraycopy(newY, 0, y, 0, n);
                System.arraycopy(newD, 0, d, 0, n);
            } else {
                System.arraycopy(medoids, 0, newMedoids, 0, k);
                System.arraycopy(y, 0, newY, 0, n);
                System.arraycopy(d, 0, newD, 0, n);
            }
        }
        return this.assign(data,medoids,y);

    }

    private Clusters assign(ProductDesc[] data, ProductDesc[] medoids, int[] y){
        ClustersBuilder clusterBuilder = new ClustersBuilder();
        double sum = 0;
        for(int i=0;i< data.length;i++){
            double nearest = Double.MAX_VALUE;
            for(int j =0; j<k;j++){
                IntPair pair = new IntPair(data[i].ID,medoids[j].ID);
                double dist = this.distances.getOrDefault(pair,0.0);
                if (nearest > dist) {
                    nearest = dist;
                    y[i] = j;
                }
            }
            clusterBuilder.assignToCluster(data[i],y[i]);
//            sum+=Math.pow(nearest,2);
        }
//        System.out.println("k="+ this.k +",dis="+Math.sqrt(sum));
        return clusterBuilder.build();
    }

    private double[] seed(ProductDesc[] data,ProductDesc[] medoids, int[] y){
        int n = data.length;
        int k = medoids.length;
        double[] d = new double[n];
        medoids[0] = data[random.nextInt(n)];
        Arrays.fill(d, Double.MAX_VALUE);
        for (int j = 1; j <= k; j++) {
            final int prev = j - 1;
            final ProductDesc medoid = medoids[prev];
            IntStream.range(0,n).parallel().forEach(i -> {
                IntPair pair = new IntPair(data[i].ID,medoid.ID);
                double dist = this.distances.getOrDefault(pair,0.0);
                if (dist < d[i]) {
                    d[i] = dist;
                    y[i] = prev;
                }
            });
            if(j<this.k){
                double cost = 0.0;
                double cutoff= random.nextDouble() * Arrays.stream(d).sum();
                for (int ind=0; ind<n;ind++){
                    cost += d[ind];
                    if(cost>=cutoff){
                        medoids[j]=data[ind];
                        break;
                    }
                }
            }
        }
        return d;

    }




    /**
     * Since the similarity is between 1 and 0, referenced in the paper
     *   distance=1-similarity
     */
    private Map<IntPair,Double> similaritiesToDistances(Map<IntPair, Double> similaritites){
        Map<IntPair, Double> distances= new HashMap<>();
        for(Map.Entry<IntPair,Double> entry: similaritites.entrySet()){
            distances.put(entry.getKey(),1-entry.getValue());
        }
        return distances;
    }

    private double getRandomNeighbor (ProductDesc[] data, ProductDesc[] medoids, int[] y, double[] d){
        int n = data.length;
        int k = medoids.length;
        int cluster = this.random.nextInt(k);
        ProductDesc medoid = getRandomMedoid(data, medoids);
        medoids[cluster] = medoid;
        IntStream.range(0, n).parallel().forEach(i -> {
            IntPair pair = new IntPair(data[i].ID,medoid.ID);
            double dist = this.distances.getOrDefault(pair,0.0);
            if (d[i] > dist) {
                y[i] = cluster;
                d[i] = dist;
            }else if (y[i] == cluster) {
                d[i] = dist;
                for (int j = 0; j < k; j++) {
                    if (j != cluster) {
                        IntPair pairIn = new IntPair(data[i].ID,medoids[j].ID);
                        dist = this.distances.getOrDefault(pairIn,0.0);
                        if (d[i] > dist) {
                            d[i] = dist;
                            y[i] = j;
                        }
                    }
                }
            }
        });
        return Arrays.stream(d).sum();
    }

    private ProductDesc getRandomMedoid (ProductDesc[] data, ProductDesc[] medoids){
        int n = data.length;
        ProductDesc medoid = data[this.random.nextInt(n)];
        while (contains(medoids, medoid)) {
            medoid = data[this.random.nextInt(n)];
        }
        return medoid;
    }

    private boolean contains(ProductDesc[] medoids, ProductDesc medoid) {
        for (ProductDesc m : medoids) {
            if (m == medoid) return true;
        }
        return false;
    }

    @Override
    public String componentID() {
        return null;
    }
}
