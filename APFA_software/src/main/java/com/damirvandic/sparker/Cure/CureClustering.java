package com.damirvandic.sparker.Cure;

import com.damirvandic.sparker.core.ClusteringProcedure;
import com.damirvandic.sparker.core.Clusters;
import com.damirvandic.sparker.core.ClustersBuilder;
import com.damirvandic.sparker.core.ProductDesc;
import com.damirvandic.sparker.util.IntPair;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class CureClustering implements ClusteringProcedure {
    private Map<IntPair, Double> distances;
    private Map<IntPair, Double> similarities;

    @Override
    public Clusters createClusters(Map<String, Object> conf, Map<IntPair, Double> similarities, TIntObjectMap<ProductDesc> index) {
        this.similarities = similarities;
        this.distances = this.similaritiesToDistances(similarities);
        TIntObjectMap<CureCluster> ret = new TIntObjectHashMap<>(index.size());
        for (ProductDesc product : index.valueCollection()) {
            ret.put(product.ID, new CureCluster(product));
        }
        double threshold = (double) conf.getOrDefault("T", 0.2);
        AtomicInteger firstCluster = new AtomicInteger(0);
        AtomicInteger secondCluster = new AtomicInteger(0);
        double minDistance = findMinDistClusters(ret, firstCluster, secondCluster, distances);
        while (minDistance <= threshold) {
            CureCluster Ck = ret.get(firstCluster.get());
            CureCluster Cl = ret.get(secondCluster.get());
            CureCluster Cm = Ck.merge(Cl);
            ret.put(firstCluster.get(), Cm);
            ret.remove(secondCluster.get());
            minDistance = findMinDistClusters(ret, firstCluster, secondCluster, distances);
        }
        ClustersBuilder clusterBuilder = new ClustersBuilder();
        for (int clusterId : ret.keys()) {
            for (ProductDesc p : ret.get(clusterId).getProducts()) {
                clusterBuilder.assignToCluster(p, clusterId);
            }
        }

        return clusterBuilder.build();
    }

    @Override
    public String componentID() {
        return null;
    }

    /**
     * Assuming that similarities are between 0 and 1
     * distance=1-similarity
     */
    private Map<IntPair, Double> similaritiesToDistances(Map<IntPair, Double> similaritites) {
        Map<IntPair, Double> distances = new HashMap<>();
        for (Map.Entry<IntPair, Double> entry : similaritites.entrySet()) {
            if (Double.isInfinite(entry.getValue())) {
                distances.put(entry.getKey(),Double.POSITIVE_INFINITY);
            } else {
                distances.put(entry.getKey(),1 - entry.getValue());
            }
        }
        return distances;
    }

    private static double findMinDistClusters(TIntObjectMap<CureCluster> clusters, AtomicInteger idFirst, AtomicInteger idSecond, Map<IntPair, Double> distances) {
        double minDistance = Double.POSITIVE_INFINITY;
        for (int id1 : clusters.keys()) {
            for (int id2 : clusters.keys()) {
                if (id1 == id2) {
                    continue;
                }
                double d = clusters.get(id1).distance(clusters.get(id2), distances);
                if (minDistance > d) {
                    minDistance = d;
                    idFirst.set(id1);
                    idSecond.set(id2);
                }
            }
        }
        return minDistance;
    }
}
