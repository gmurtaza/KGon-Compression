/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Compression;

import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author ghulammurtaza
 */
public class SquishCompression {
    
    public ArrayList<GPSPoint> SquishCompressionTechnique(ArrayList<GPSPoint> sourceTrajectory, float compressionRatio, float minimumError){
        
        HashMap <GPSPoint,Double> queueOfPoints = new HashMap<GPSPoint, Double>(); // actualy queue for compressed trajectory
        HashMap <GPSPoint,Double> piQueueOfPoints = new HashMap<GPSPoint, Double>(); // Map storing, for each p_i the maximum of priority that neighbouring points of p_i had when removed from Q
        HashMap <GPSPoint,GPSPoint> predQueueOfPoints = new HashMap<GPSPoint, GPSPoint>();
        HashMap <GPSPoint,GPSPoint> succQueueOfPoints = new HashMap<GPSPoint, GPSPoint>();
        
        int queCapacity = 4; //This is initial capacity of priority queue
        
        int minPriority = 99999;
        
        for (int i = 0; i < sourceTrajectory.size(); i++){
            
            // this keeps tab on queue size
            if ((i+1)>compressionRatio){
                queCapacity += 1;
            }
            
            setPriority(sourceTrajectory.get(i), 99999, queueOfPoints);
            piQueueOfPoints.put(sourceTrajectory.get(i), 0.0);
            
            if (i>1){
                succQueueOfPoints.put(sourceTrajectory.get(i-1),sourceTrajectory.get(i));
                predQueueOfPoints.put(sourceTrajectory.get(i),sourceTrajectory.get(i-1));
                adjustPriority(sourceTrajectory.get(i-1),queueOfPoints,predQueueOfPoints,succQueueOfPoints,piQueueOfPoints);
            }
            
            if (queueOfPoints.size() == queCapacity){
                reduceQueue(queueOfPoints, predQueueOfPoints,succQueueOfPoints,piQueueOfPoints);
            }
            
        }
        
        minPriority = minPriority(queueOfPoints);
        
        while(minPriority<=minimumError){
            reduceQueue(queueOfPoints, predQueueOfPoints,succQueueOfPoints,piQueueOfPoints);
            minPriority = minPriority(queueOfPoints);
        }
        
        return (ArrayList<GPSPoint>)queueOfPoints.keySet();
    }
    
    public void setPriority(GPSPoint currentPoint, double currentPriority, HashMap <GPSPoint,Double> queueOfPoints){
        queueOfPoints.put(currentPoint, currentPriority);
    }
    
    public void adjustPriority(GPSPoint currentPoint,HashMap <GPSPoint,Double> queueOfPoints,HashMap <GPSPoint,GPSPoint> predGPSPoints,HashMap <GPSPoint,GPSPoint> succGPSPoints,HashMap <GPSPoint,Double> piQueueOfPoints){
        if (predGPSPoints.get(currentPoint)!=null && succGPSPoints.get(currentPoint)!=null){
            double currentPriority = piQueueOfPoints.get(queueOfPoints)+singleSynchEuclideanDistance(currentPoint,predGPSPoints.get(currentPoint), succGPSPoints.get(currentPoint));
            setPriority(currentPoint, currentPriority, queueOfPoints);
        }
    }
    
    public void reduceQueue(HashMap <GPSPoint,Double> queueOfPoints,HashMap <GPSPoint,GPSPoint> predGPSPoints,HashMap <GPSPoint,GPSPoint> succGPSPoints,HashMap <GPSPoint,Double> piQueueOfPoints){
        GPSPoint removedPoint = minPriorityPointInQueue(queueOfPoints);
        double removedPriority = queueOfPoints.remove(removedPoint);
        piQueueOfPoints.put(succGPSPoints.get(removedPoint), Math.max(removedPriority, piQueueOfPoints.get(succGPSPoints.get(removedPoint))));
        piQueueOfPoints.put(predGPSPoints.get(removedPoint), Math.max(removedPriority, piQueueOfPoints.get(predGPSPoints.get(removedPoint))));
        adjustPriority(predGPSPoints.get(removedPoint),queueOfPoints,predGPSPoints,succGPSPoints,piQueueOfPoints);
        adjustPriority(succGPSPoints.get(removedPoint),queueOfPoints,predGPSPoints,succGPSPoints,piQueueOfPoints);
        succGPSPoints.remove(removedPoint);
        predGPSPoints.remove(removedPoint);
        piQueueOfPoints.remove(removedPoint);
    }
    
    public int minPriority(HashMap <GPSPoint,Double> queueOfPoints){
        int minPriority = 99999;
        
        Iterator it = queueOfPoints.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            if ((Integer)pairs.getValue()<minPriority){
                minPriority = (Integer)pairs.getValue();
            }
            //pairs.getKey()
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        return minPriority;
    }
    
    public GPSPoint minPriorityPointInQueue(HashMap <GPSPoint,Double> queueOfPoints){
        int minPriority = 99999;
        GPSPoint minPriorityPoint = new GPSPoint();
        Iterator it = queueOfPoints.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            if ((Integer)pairs.getValue()<minPriority){
                minPriorityPoint = (GPSPoint)pairs.getKey();
            }
            //pairs.getKey()
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        return minPriorityPoint;
    }
    
    public double singleSynchEuclideanDistance(GPSPoint currentPoint,GPSPoint predGPSPoints,GPSPoint succGPSPoints){
        long diff = predGPSPoints.getTimeStamp().getTime() - succGPSPoints.getTimeStamp().getTime();
        float distance= GeoHelper.getDistance(predGPSPoints, succGPSPoints);
        double angle = GeoHelper.getGPSAngle(predGPSPoints, succGPSPoints);
        double speed = distance/diff;
        double equivalentPointDistance = speed*(currentPoint.getTimeStamp().getTime() - predGPSPoints.getTimeStamp().getTime());
        GPSPoint equivalentPoint = GeoHelper.getPointWithPolarDistance(predGPSPoints, equivalentPointDistance, angle);
        return GeoHelper.getDistance(equivalentPoint, currentPoint);
    }
    
    
    
    
    
            
}
