/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri;

import di.uniba.it.tri.vectors.Vector;

/**
 *
 * @author pierpaolo
 */
public class Clusters {

    /**
     * Array of ints mapping each of a list of object vectors to a cluster.
     */
    private int[] clusterMappings;
    /**
     * Centroids of the clusters in question.
     */
    private Vector[] centroids;

    public Clusters() {
    }

    public Clusters(int[] clusterMappings, Vector[] centroids) {
        this.clusterMappings = clusterMappings;
        this.centroids = centroids;
    }

    public int[] getClusterMappings() {
        return clusterMappings;
    }

    public void setClusterMappings(int[] clusterMappings) {
        this.clusterMappings = clusterMappings;
    }

    public Vector[] getCentroids() {
        return centroids;
    }

    public void setCentroids(Vector[] centroids) {
        this.centroids = centroids;
    }

}
