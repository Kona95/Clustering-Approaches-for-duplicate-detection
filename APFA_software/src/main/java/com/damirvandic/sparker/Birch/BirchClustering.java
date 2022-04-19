package com.damirvandic.sparker.Birch;

import com.damirvandic.sparker.core.ClusteringProcedure;
import com.damirvandic.sparker.core.Clusters;
import com.damirvandic.sparker.core.ClustersBuilder;
import com.damirvandic.sparker.core.ProductDesc;
import com.damirvandic.sparker.util.IntPair;
import gnu.trove.map.TIntObjectMap;

import java.util.*;

public class BirchClustering implements ClusteringProcedure {
    /**
     * Branching factor. Maximum number of children nodes.
     */
    private int B;
    /**
     * Maximum radius of a sub-cluster.
     */
    private double T;
    /**
     * The dimensionality of data.
     */
    private int d;
    /**
     * The root of CF tree.
     */
    private Node root;
    /**
     * Object containing all the clusters
     */
    private Clusters clusters;
    /**
     * Leaves of CF tree as representatives of all data points.
     */
    private ClustersBuilder clusterBuilder;
    /**
     * Distances
     */
    private Map<IntPair, Double> distances;
    /**
     * Contains the products
     */
    private TIntObjectMap<ProductDesc> index;


    @Override
    public Clusters createClusters(Map<String, Object> conf, Map<IntPair, Double> similarities, TIntObjectMap<ProductDesc> index) {
        this.index = index;
        this.B=(int)conf.getOrDefault("B",10);
        this.T=(double)conf.getOrDefault("T",0.2); //distances are 0 to 1, reverse from the similarities
        this.distances=similaritiesToDistances(similarities);
        this.clusterBuilder=new ClustersBuilder();
        root=null;
        createCFTree();
        partition();
        return clusters;
    }

    private void createCFTree(){
        for(ProductDesc p : index.valueCollection()){
            add(p);
        }
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


    @Override
    public String componentID() {
        return null;
    }

    class Node {
        /**
         * The number of observations
         */
        int n;
        /**
         * The center of the cluster
         */
        ProductDesc centroid;
        /**
         *
         */
        HashSet<ProductDesc> points;
        /**
         * The number of children.
         */
        int numChildren;
        /**
         * Children nodes.
         */
        Node[] children;
        /**
         * Parent node.
         */
        Node parent;
        /**
         * Constructor of root node
         */
        Node() {
            n = 0;
            parent = null;
            numChildren = 0;
            children = new Node[B];
            points = new HashSet<>();
        }

        /**
         * Finds the product that is closer to all points
         */
        void updateCentroid(){
            double min = Double.MAX_VALUE;
            if(points.isEmpty()){
                this.centroid=null;
                return;
            }
            for(ProductDesc p1: points){
                double dist = 0.0;
                for(ProductDesc p2: points){
                    if(p1.ID!=p2.ID){
                        IntPair key = new IntPair(p1.ID, p2.ID);
                        dist+=distances.get(key);
                    }
                }
                if(dist<min){
                    min=dist;
                    centroid=p1;
                }
            }
        }

        void update(ProductDesc p){
            this.n++;
            points.add(p);
            updateCentroid();
        }

        double distance(ProductDesc p){
            if(centroid == null){
                updateCentroid();
            }else if(centroid.ID==p.ID){
                return 0;
            }
            IntPair key = new IntPair(this.centroid.ID, p.ID);
            return distances.get(key);
        }

        double distance(Node node){
            IntPair key = new IntPair(this.centroid.ID, node.centroid.ID);
            if(node.centroid.ID==this.centroid.ID){
                return 0;
            }
            return distances.get(key);
        }

        void add(ProductDesc p){
            update(p);
            int index=0;
            double smallest = this.children[0].distance(p);
            for (int i=1;i <this.numChildren; i++){
                double dist = this.children[i].distance(p);
                if(dist< smallest){
                    index=i;
                    smallest=dist;
                }
            }
            if(this.children[index] instanceof Leaf){
                if(smallest>T){
                    add(new Leaf(p));
                }else{
                    children[index].add(p);
                }
            }else{
                children[index].add(p);
            }
        }

        /**
         * Add a node as children. Split this node if the number of children
         * reach the Branch Factor.
         */
        void add(Node node){
            if(this.numChildren < B){
                this.children[numChildren++]=node;
            }else{ //require split
                if(this.parent==null){
                    parent=new Node();
                    parent.add(this);
                    root=parent;
                }else{
                    parent.n=0;
                    parent.points.clear();
                    parent.updateCentroid();
                }

                parent.add(split(node));
                for(int i =0;i<parent.numChildren;i++){
                    parent.n+=parent.children[i].n;
                    parent.points.addAll(parent.children[i].points);
                }
                parent.updateCentroid();
            }
        }

        Leaf search(ProductDesc p){
            int index=0;
            double smallest=children[0].distance(p);
            for(int i =1; i <numChildren;i++){
                double dist = children[i].distance(p);
                if(dist<smallest){
                    index=i;
                    smallest=dist;
                }
            }
            if(children[index].numChildren ==0){
                return (Leaf) children[index];
            }else{
                return children[index].search(p);
            }
        }

        Node split(Node node){
            double farest = 0.0;
            int c1 = 0, c2 = 0;
            double[][] dist = new double[numChildren + 1][numChildren + 1];
            for (int i = 0; i < numChildren; i++) {
                for (int j = i + 1; j < numChildren; j++) {
                    dist[i][j] = children[i].distance(children[j]);
                    dist[j][i] = dist[i][j];
                    if (farest < dist[i][j]) {
                        c1 = i;
                        c2 = j;
                        farest = dist[i][j];
                    }
                }
                dist[i][numChildren] = children[i].distance(node);
                dist[numChildren][i] = dist[i][numChildren];
                if (farest < dist[i][numChildren]) {
                    c1 = i;
                    c2 = numChildren;
                    farest = dist[i][numChildren];
                }
            }
            int nc = numChildren;
            Node[] child = children;
            //clean up this node
            numChildren = 0;
            n = 0;
            points.clear();
            updateCentroid();

            Node brother = new Node();
            for (int i = 0; i < nc; i++) {
                if (dist[i][c1] < dist[i][c2]) {
                    add(child[i]);
                } else {
                    brother.add(child[i]);
                }
            }

            if (dist[nc][c1] < dist[nc][c2]) {
                add(node);
            } else {
                brother.add(node);
            }

            for (int i = 0; i < numChildren; i++) {
                n += children[i].n;
                points.addAll(children[i].points);
            }
            updateCentroid();

            for (int i = 0; i < brother.numChildren; i++) {
                brother.n += brother.children[i].n;
                brother.points.addAll(children[i].points);
            }
            brother.updateCentroid();
            return brother;
        }
    }

    class Leaf extends Node {
        /**
         * The cluster label of the leaf node.
         */
        int y;
        /**
         * Constructor2
         * @param p
         */
        Leaf(ProductDesc p){
            super();
            n = 1;
            this.centroid=p;
            this.points.add(p);
        }

        @Override
        void add(ProductDesc p) {
            this.update(p);
        }
    }

    void add(ProductDesc p){
        if (root == null) {
            root = new Node();
            root.add(new Leaf(p));
            root.update(p);
        } else {
            root.add(p);
        }
    }

    void partition(){
        Queue<Node> queue = new LinkedList<>();
        int identifier =0;
        queue.offer(root);
        for (Node node = queue.poll(); node != null; node = queue.poll()) {
            if (node instanceof Leaf) {
                for(ProductDesc p:node.points){ //add points in the same leaf to the same cluster
                    clusterBuilder.assignToCluster(p,identifier);
                }
                identifier+=1;
            }else{
                for (int i = 0; i < node.numChildren; i++) {
                    queue.offer(node.children[i]);
                }
            }
        }
        clusters=clusterBuilder.build();

    }

}
