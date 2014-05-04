/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Compression;

import GeoHelper.ClusterStructure;
import GeoHelper.GPSPoint;
import Helper.Utility;
import java.util.ArrayList; 

/**
 *
 * @author ghulammurtaza
 */
public class MainClustering {
    
    /*
     * Calls the actual clustering algorithm
     */
    public ArrayList <ClusterStructure> LightClustering(ArrayList<GPSPoint> sourceGPSPoints, int n, int thresholdDistance, int bufferSize, int minClusteringPoints, ArrayList<Integer> unClusteredPointsCounter, ArrayList<Integer> removedPointsCounter, ArrayList<IndividualDistance> clusteringIndividualDistanceList){
        ArrayList <ClusterStructure> allClusters = new ArrayList <ClusterStructure>();
        //ArrayList<GPSPoint> initialPoints = new ArrayList<GPSPoint>();
        ArrayList<GPSPoint> currentBuffer = new ArrayList<GPSPoint>();
//        ArrayList<Integer> unClusteredPointsCounter = new ArrayList<Integer>();
//        ArrayList<Integer> removedPointsCounter = new ArrayList<Integer>();
        for (int i = 0; i < n; i++){
            currentBuffer.add( sourceGPSPoints.get(i));
        }
        bufferClusteringPhase(allClusters,  thresholdDistance, minClusteringPoints, currentBuffer, clusteringIndividualDistanceList);
        unClusteredPointsCounter.add(currentBuffer.size());
        int currentUnclustered = currentBuffer.size();
        //System.out.println("Initial clusters: "+allClusters.size());
        for (int i = n; i < sourceGPSPoints.size(); i++){
            insertPoint(allClusters, sourceGPSPoints.get(i), currentBuffer, thresholdDistance, bufferSize, removedPointsCounter);
            if (currentBuffer.size()==bufferSize){
                bufferClusteringPhase(allClusters, thresholdDistance, minClusteringPoints, currentBuffer, clusteringIndividualDistanceList);
            }
            if (currentBuffer.size()>currentUnclustered)
                currentUnclustered+=currentBuffer.size()-currentUnclustered;
            unClusteredPointsCounter.add(currentUnclustered);
        }
        removedPointsCounter.add(currentBuffer.size());
        return allClusters;
    }
    
    public float GeoDistance(GPSPoint sourcePoint, GPSPoint destinationPoint){
        double radius = 6371; // km
  
    double dLat = Math.toRadians(sourcePoint.getLatitude() - destinationPoint.getLatitude());
    double dLon = Math.toRadians(sourcePoint.getLongitude() - destinationPoint.getLongitude());
    double lat1 = Math.toRadians(sourcePoint.getLatitude());
    double lat2 = Math.toRadians(destinationPoint.getLatitude());

    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    double d = radius * c;
    int meterConversion = 1000;
    return new Float(d * meterConversion).floatValue();
        
    }
    
    /*
   * This function gives the angle between two GPS points
   */
  static public double getGPSAngle(GPSPoint currentCentre, GPSPoint currentPoint){
    double dy = currentPoint.getLatitude() - currentCentre.getLatitude();
    double dx = Math.cos(Math.toRadians(currentCentre.getLatitude()))*(currentPoint.getLongitude() - currentCentre.getLongitude());
    double angle = Math.atan2(dy, dx);
    return Math.toDegrees(angle);
  }
    
    void insertPoint(ArrayList <ClusterStructure> allClusters, GPSPoint currentPoint, ArrayList<GPSPoint> currentBuffer, int thresholdDistance, int bufferSize,ArrayList<Integer> removedPointsCounter){
        boolean checkClustered = false;
        for (int i = 0; i < allClusters.size(); i++){
            if (GeoDistance(currentPoint, allClusters.get(i).clusterCentre) <= thresholdDistance){
                updateCluster(allClusters.get(i), currentPoint);
                checkClustered = true;
                break;
            }
        }
        //TODO: add what is the negation doing here
        if(!checkClustered){
            if (currentBuffer.size()==bufferSize){
                currentBuffer.remove(0);
                currentBuffer.add(currentPoint);
                removedPointsCounter.add(1);
            }else{
                currentBuffer.add(currentPoint);
                removedPointsCounter.add(0);
            }
            
        }
    }
    
    /*
     * This function performs initialization by dividing n points
     */
    public void initializationPhase(ArrayList <ClusterStructure> allClusters, ArrayList<GPSPoint> initialPoints, int thresholdDistance, int minClusteringPoints, ArrayList<GPSPoint> currentBuffer,ArrayList<Double> clusteringIndividualDistanceList){
        
        for (int i = 0; i < initialPoints.size(); i++){
             boolean notWithEarlyCluster = true;
            ArrayList<Integer> pointsToRemove = new ArrayList<Integer>();
            GPSPoint currentPoint = (GPSPoint)Utility.copy(initialPoints.get(i));
            for (int k = 0; k<allClusters.size();k++){
                if (GeoDistance(currentPoint, allClusters.get(k).clusterCentre) <= thresholdDistance){
                    updateCluster(allClusters.get(k), currentPoint);
                    notWithEarlyCluster = false;
                    initialPoints.remove(i);
                    break;
                }
            }
            if(notWithEarlyCluster){
            ClusterStructure newCluster = new ClusterStructure(1, 0, currentPoint, currentPoint.getTimeStamp(), currentPoint.getTimeStamp());
            for(int j = 0; j<initialPoints.size(); j++){
                if(i!=j){
                    GPSPoint thisPoint = (GPSPoint)Utility.copy(initialPoints.get(j));
                    if (GeoDistance(currentPoint, thisPoint)<=thresholdDistance){
                        updateCluster(newCluster, thisPoint);
                        pointsToRemove.add(j);
                        //initialPoints.remove(j);
                    }
                }
            }
            if (newCluster.totalPoints<minClusteringPoints){
                currentBuffer.add(currentPoint);
                initialPoints.remove(i);
            }else{
                
                allClusters.add(newCluster);
                for (int h = 0; h<allClusters.size(); h++){
                    if(GeoHelper.GeoHelper.getDistance(allClusters.get(h).clusterCentre, newCluster.clusterCentre) < thresholdDistance){
                        System.out.println("initializationPhase: Distance is small");
                    }
                }
                for (int k = 0; k < pointsToRemove.size(); k++){
                    clusteringIndividualDistanceList.add((double)GeoHelper.GeoHelper.getDistance(newCluster.clusterCentre, initialPoints.get(pointsToRemove.get(k))));
                    initialPoints.remove(pointsToRemove.get(k));
                }
                initialPoints.remove(i);
            }
          }
        }
    }
    
    
    /*
     * This function performs buffer clustering when size goes higher than limit
     */
    public void bufferClusteringPhase(ArrayList <ClusterStructure> allClusters, int thresholdDistance, int minClusteringPoints, ArrayList<GPSPoint> currentBuffer, ArrayList<IndividualDistance> clusteringIndividualDistanceList){
        
        for (int i = 0; i < currentBuffer.size(); i++){
            boolean notWithEarlyCluster = true;
            ArrayList<Integer> pointsToRemove = new ArrayList<Integer>();
            GPSPoint currentPoint = (GPSPoint)Utility.copy(currentBuffer.get(i));
            for (int k = 0; k<allClusters.size();k++){
                if (GeoDistance(currentPoint, allClusters.get(k).clusterCentre) <= thresholdDistance){
                    updateCluster(allClusters.get(k), currentPoint);
                    double dist = (double)GeoHelper.GeoHelper.getDistance(allClusters.get(k).clusterCentre, currentPoint);
                    clusteringIndividualDistanceList.add(new IndividualDistance(currentPoint.getTimeStamp(),dist));
                    notWithEarlyCluster = false;
                    currentBuffer.remove(i);
                    break;
                }
            }
            if(notWithEarlyCluster){
                ClusterStructure newCluster = new ClusterStructure(1, 0, currentPoint, currentPoint.getTimeStamp(), currentPoint.getTimeStamp());
                for(int j = 0; j<currentBuffer.size(); j++){
                    if (i!=j){
                        GPSPoint thisPoint = (GPSPoint)Utility.copy(currentBuffer.get(j));
                        if (GeoDistance(currentPoint, thisPoint)<=thresholdDistance){
                            updateCluster(newCluster, thisPoint);
                            pointsToRemove.add(j);
                        }
                    }
                }
                if (newCluster.totalPoints>minClusteringPoints){
                    
                    allClusters.add(newCluster);
                    for (int k = 0; k < pointsToRemove.size(); k++){
                        double dist = (double)GeoHelper.GeoHelper.getDistance(newCluster.clusterCentre, currentBuffer.get(pointsToRemove.get(k)));
                        clusteringIndividualDistanceList.add(new IndividualDistance(currentBuffer.get(pointsToRemove.get(k)).getTimeStamp(),dist));
                        currentBuffer.remove(pointsToRemove.get(k));
                    }
                    currentBuffer.remove(i);
                }
            }   
        }
    }
    
    /*
     * This function updates the cluster statistics if a point is added to the cluster
     */
    void updateCluster(ClusterStructure currentCluster, GPSPoint currentPoint){
        float distance = GeoDistance(currentPoint, currentCluster.clusterCentre);
        if (distance>currentCluster.clusterRadius)
            currentCluster.setClusterRadius(distance);
        currentCluster.totalPoints +=1;
        currentCluster.setLastTimeStamp ( currentPoint.getTimeStamp());
    }
    
}

