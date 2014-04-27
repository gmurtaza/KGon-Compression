/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Compression;

import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
import Helper.Utility;
import java.util.ArrayList;

/**
 *
 * @author ghulammurtaza
 */
public class DeadReckoning {
    
    public ArrayList<GPSPoint> DeadReckoningTechnique(ArrayList<GPSPoint> sourceTrajectory, float minimumError){
        
        ArrayList<GPSPoint> approximatedTrajectory = new ArrayList<GPSPoint>();
        GPSPoint firstPoint = sourceTrajectory.get(0);
        approximatedTrajectory.add((GPSPoint)Utility.copy(firstPoint));
        GPSPoint secondPoint = sourceTrajectory.get(1);
        approximatedTrajectory.add((GPSPoint)Utility.copy(secondPoint));
        float distance = GeoHelper.getDistance(firstPoint, secondPoint);
        double angle = GeoHelper.getGPSAngle(firstPoint, secondPoint);
        float speed = distance/(secondPoint.getTimeStamp().getTime() - secondPoint.getTimeStamp().getTime());
        
        for (int i =2; i < sourceTrajectory.size(); i++){
            
            GPSPoint approximatePoint = approximatedPoint(speed, secondPoint ,sourceTrajectory.get(i), angle);
            float distanceApproximated = GeoHelper.getDistance(secondPoint,approximatePoint);
            if (distanceApproximated>minimumError){
                approximatedTrajectory.add((GPSPoint)Utility.copy(sourceTrajectory.get(i)));
                firstPoint = (GPSPoint)Utility.copy(secondPoint);
                secondPoint = (GPSPoint)Utility.copy(sourceTrajectory.get(i));
                distance = GeoHelper.getDistance(firstPoint, secondPoint);
                angle = GeoHelper.getGPSAngle(firstPoint, secondPoint);
                speed = distance/(secondPoint.getTimeStamp().getTime() - secondPoint.getTimeStamp().getTime());
            }
            
        }
            
        return approximatedTrajectory;
    }
    
    GPSPoint approximatedPoint(float speed, GPSPoint lastPoint ,GPSPoint newPoint, double angle){
        return GeoHelper.getPointWithPolarDistance(lastPoint, speed*(newPoint.getTimeStamp().getTime()-lastPoint.getTimeStamp().getTime()), angle);
    }
    
}
