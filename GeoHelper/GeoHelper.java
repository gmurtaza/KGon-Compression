package GeoHelper;


import GeoHelper.GPSPoint;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * This class has got all the Geo Helper functions
 */

/**
 *
 * @author ghulammurtaza
 */
public class GeoHelper {
    
    /*
   * This function calculates the distance (in meters) between two given GPS points.
   */
   static public float getDistance(GPSPoint currentCentre, GPSPoint currentPoint){
  	double radius = 6371; // km
  
    double dLat = Math.toRadians(currentCentre.getLatitude() - currentPoint.getLatitude());
    double dLon = Math.toRadians(currentCentre.getLongitude() - currentPoint.getLongitude());
    double lat1 = Math.toRadians(currentCentre.getLatitude());
    double lat2 = Math.toRadians(currentPoint.getLatitude());

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
  
  /* 
   * Adding a certain distance according to angle
   */
  static public GPSPoint getPointWithPolarDistance(GPSPoint currentCentre, double r, double theeta){
    GPSPoint newCentre = new GPSPoint();
    newCentre.setLatitude(currentCentre.getLatitude() + Math.toDegrees((r*Math.sin(Math.toRadians(theeta)))/6378137));
    newCentre.setLongitude(currentCentre.getLongitude() + Math.toDegrees((r*Math.cos(Math.toRadians(theeta)))/6378137/Math.cos(Math.toRadians(currentCentre.getLatitude()))));
    return newCentre;
  }
  
  /*
   * This function calculates the length of the base of right triangle
   */
  
  static public double getBaseLength(double theeta, double hypot){
      return hypot*Math.sin(Math.toRadians(theeta));
      
  }
  
  /*
   * This function returns the tangent point on the k-gon side to an outside point 
   * 
   * Consider the sides 
   * source - target1 = a
   * source - target2 = b
   * target1 - target2 = c
   * 
   */
  
  static public double getTheDistanceFromTangent(GPSPoint source_point, GPSPoint target_point1, GPSPoint target_point2){
    float a = GeoHelper. getDistance(source_point, target_point1);
    float b = GeoHelper. getDistance(source_point, target_point2);
    float c =GeoHelper. getDistance(target_point1, target_point2);
    float s = (a+b+c)/2;
    return (2*Math.sqrt(s*(s-1)*(s-b)*(s-c)))/c; //sends the height back
//    if (GeoHelper. getDistance(target_point1, target_point2) < 2){
//      //System.out.println(GeoHelper. getDistance(targetPoint1, targetPoint2));
//      return target_point1;
//    }else{
//      double angle = GeoHelper.getGPSAngle(target_point1, target_point2);
//      float distance =GeoHelper. getDistance(target_point1, target_point2);
//      //System.out.println(distance);
//      if (GeoHelper. getDistance(source_point, target_point1) <GeoHelper. getDistance(source_point, target_point2)){
//        return getTheTangentPoint(source_point,target_point1,GeoHelper.getPointWithPolarDistance(target_point1, (distance/2), angle));
//      }else{
//        return getTheTangentPoint(source_point, GeoHelper.getPointWithPolarDistance(target_point1, (distance/2), angle), target_point2);
//      }
//    }
    // System.out.println(GeoHelper. getDistance(source_point, targetPoint1));
    // System.out.println(GeoHelper. getDistance(source_point, targetPoint2));
    // System.out.println(GeoHelper. getDistance(targetPoint1, targetPoint2));
    //return null;
  }
  
  /*
   * This function checks if we need to calculate the slope
   */
  static public boolean isSlopeCalculationNeeded(double firstAngle, double secondAngle){
    if (((firstAngle>0 && secondAngle<90)||(firstAngle>=90 && secondAngle<180)||(firstAngle>-90 && secondAngle<=0)||(firstAngle> -180 && secondAngle<=-90)) 
      && (Math.abs(firstAngle - secondAngle)<90)){
      return false;
    }else {
      return true;
    }
  }
  
  /*
   * This function finds one-sided Hausdorff Distance between two trajectories
   */
  static public float getOneSidedHausdorffDistance(ArrayList<GPSPoint> firstSet, ArrayList<GPSPoint> secondSet){
    float hausdorffDistance = -1;
    for (int i = 0; i < firstSet.size(); i++){
      GPSPoint source_point = firstSet.get(i);
      GPSPoint targetPoint1 = null;
      GPSPoint targetPoint2 = null;
      float mDistance = 10000000;
      for (int j = 0; j < secondSet.size()-2; j++){
        double distanceBetweenSourceAndTangent = 0;
        targetPoint1 = secondSet.get(j);
        targetPoint2 = secondSet.get(j+1);
        //System.out.println(j);
        double firstAngle = GeoHelper.getGPSAngle(source_point, targetPoint1);
        double secondAngle = GeoHelper.getGPSAngle(source_point, targetPoint2);
        double interAngle = GeoHelper.getGPSAngle(targetPoint1, targetPoint2);
        //System.out.println(j);
        //System.out.println(firstAngle);
        //System.out.println(secondAngle);
        //System.out.println(GeoHelper. getDistance(source_point, targetPoint1));
//        if (i == 51&&j==12 )
//            System.out.println(GeoHelper. getDistance(source_point, targetPoint2));
//        if (j == 51&&i==12 )
//            System.out.println(GeoHelper. getDistance(source_point, targetPoint2));
//        if (GeoHelper.isSlopeCalculationNeeded(firstAngle, secondAngle)){
//          //System.out.println("")
//          if((firstAngle == 180 || secondAngle == 180)&&(GeoHelper. getDistance(source_point, targetPoint1) < 2 ||GeoHelper. getDistance(source_point, targetPoint2) < 2)){
//            distanceBetweenSourceAndTangent = Math.min(GeoHelper. getDistance(source_point, targetPoint1),GeoHelper. getDistance(source_point, targetPoint2));
//          }else if((firstAngle == 0 || secondAngle == 0)&&(GeoHelper. getDistance(source_point, targetPoint1) < 2 ||GeoHelper. getDistance(source_point, targetPoint2) < 2)){
//            distanceBetweenSourceAndTangent = Math.min(GeoHelper. getDistance(source_point, targetPoint1),GeoHelper. getDistance(source_point, targetPoint2));
//          }else{
            
            distanceBetweenSourceAndTangent =Math.min(GeoHelper. getDistance(source_point, targetPoint1),GeoHelper.getTheDistanceFromTangent(source_point, targetPoint1, targetPoint2));
            distanceBetweenSourceAndTangent =Math.min(GeoHelper. getDistance(source_point, targetPoint2),distanceBetweenSourceAndTangent);
//          }
//          }else{
//          distanceBetweenSourceAndTangent = Math.min(GeoHelper. getDistance(source_point, targetPoint1),GeoHelper. getDistance(source_point, targetPoint2));
//        }

        if (distanceBetweenSourceAndTangent<mDistance){
            //System.out.println(i+"   "+j+"       "+distanceBetweenSourceAndTangent);
            mDistance = (float)distanceBetweenSourceAndTangent;
          }
        
      }
      if (mDistance>hausdorffDistance){
//          System.out.println(mDistance);
//          System.out.println("source: "+source_point.getLongitude()+" x "+source_point.getLatitude());
//          System.out.println("target1: "+targetPoint1.getLongitude()+" x "+targetPoint1.getLatitude());
//          System.out.println("source: "+targetPoint2.getLongitude()+" x "+targetPoint2.getLatitude());
        hausdorffDistance = mDistance;
      }
    }
    return hausdorffDistance;
  }
  
  
  /*
   * Ths function returns the hausdorff distance between two sets
   */

  static public float getHausdorffDistance(ArrayList<GPSPoint> firstSet, ArrayList<GPSPoint> secondSet){
      float firstSide = GeoHelper.getOneSidedHausdorffDistance(firstSet, secondSet);
      float secondSide = GeoHelper.getOneSidedHausdorffDistance(secondSet, firstSet);
    return Math.max(firstSide, secondSide);
  }
  
  static public float getHausdorffTimeDistance(ArrayList<GPSPoint> firstSet, ArrayList<GPSPoint> secondSet){
      float maxDist = -1;
      for (int i = 0; i<firstSet.size(); i++){
          int minDistance = 999999999;
          for (int j = 0; j<secondSet.size(); j++){
              int diff = (int) Math.abs(firstSet.get(i).getTimeStamp().getTime()-secondSet.get(j).getTimeStamp().getTime());
              diff = diff/1000;
              if(diff < minDistance){
                  minDistance = diff;
              }
          }
          if(minDistance>maxDist){
              maxDist = minDistance;
          } 
      }
      return maxDist;
  }

    
}
