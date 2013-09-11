package Compression;


import Compression.KGonCompression;
import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
import Constants.Constants;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * 
 * 
 */
/**
 *
 * @author ghulammurtaza
 */
public class HexaGon {

    /*
     * This function returns the code to be usd for transition to neighbouring hexagon.
     * This code is based on the side on the label of neighbouring hexagon.
     */
    static public Integer transitionCodeForSide(double angle) {
        if (angle > -30 && angle <= 30) {
            return new Integer(Constants.FIRST_NEIGHBOURING_KGON);
        } else if (angle > 30 && angle <= 90) {
            return new Integer(Constants.SECOND_NEIGHBOURING_KGON);
        } else if (angle > 90 && angle <= 150) {
            return new Integer(Constants.THIRD_NEIGHBOURING_KGON);
        } else if ((angle > 150 && angle <= 180) || (angle >= -180 && angle <= -150)) {
            return new Integer(Constants.FOURTH_NEIGHBOURING_KGON);
        } else if (angle > -150 && angle <= -90) {  
            return new Integer(Constants.FIFTH_NEIGHBOURING_KGON);
        } else if (angle > -90 && angle <= -30) {
            return new Integer(Constants.SIXTH_NEIGHBOURING_KGON);
        }
        return null;
    }
    
        static public GPSPoint returnPointBasedOnCode(int targetCode, GPSPoint firstPosition, String distanceType, float epsilon){
        if (targetCode == Constants.FIRST_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, 2*KGonCompression.getSideLengthToUse(epsilon, 0.0, distanceType), 0.0);
          }else if (targetCode == Constants.SECOND_NEIGHBOURING_KGON){
            return GeoHelper.getPointWithPolarDistance(firstPosition, 2*KGonCompression.getSideLengthToUse(epsilon, 60.0,distanceType), 60.0);
          }else if (targetCode == Constants.THIRD_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, 2*KGonCompression.getSideLengthToUse(epsilon, 120.0,distanceType), 120.0);
          }else if (targetCode == Constants.FOURTH_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, 2*KGonCompression.getSideLengthToUse(epsilon, 180.0,distanceType), 180.0);
          }else if (targetCode == Constants.FIFTH_NEIGHBOURING_KGON){
            return GeoHelper.getPointWithPolarDistance(firstPosition, 2*KGonCompression.getSideLengthToUse(epsilon, -120.0,distanceType), -120.0);
          }else if (targetCode == Constants.SIXTH_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, 2*KGonCompression.getSideLengthToUse(epsilon, -60.0,distanceType), -60.0);
          }
        return firstPosition;
    }
    
    static public GPSPoint returnPointBasedOnCodeDouble(double targetCode, GPSPoint firstPosition, String distanceType, float epsilon){
        if (targetCode == Constants.FIRST_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, KGonCompression.getSideLengthToUse( epsilon, 0.0 ,distanceType), 0.0);
          }else if (targetCode == Constants.SECOND_NEIGHBOURING_KGON){
            return GeoHelper.getPointWithPolarDistance(firstPosition, KGonCompression.getSideLengthToUse( epsilon, 60.0 ,distanceType), 60.0);
          }else if (targetCode == Constants.THIRD_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, KGonCompression.getSideLengthToUse( epsilon, 120.0 ,distanceType), 120.0);
          }else if (targetCode == Constants.FOURTH_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, KGonCompression.getSideLengthToUse( epsilon, 180.0 ,distanceType), 180.0);
          }else if (targetCode == Constants.FIFTH_NEIGHBOURING_KGON){
            return GeoHelper.getPointWithPolarDistance(firstPosition, KGonCompression.getSideLengthToUse( epsilon, -120.0 ,distanceType), -120.0);
          }else if (targetCode == Constants.SIXTH_NEIGHBOURING_KGON){
             return GeoHelper.getPointWithPolarDistance(firstPosition, KGonCompression.getSideLengthToUse( epsilon, -60.0 ,distanceType), -60.0);
          }
        return firstPosition;
    }

    /*
     * This function returns the new center according to the angle and allowed error.
     */
    static public GPSPoint newCenterOfNeighbouringHexagon(double angle, float epsilon, GPSPoint currentCentre) {
        if (angle > -30 && angle <= 30) {
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2 * epsilon, 0.0);
        } else if (angle > 30 && angle <= 90) {
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2 * epsilon, 60.0);
        } else if (angle > 90 && angle <= 150) {
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2 * epsilon, 120.0);
        } else if ((angle > 150 && angle <= 180) || (angle >= -180 && angle <= -150)) {
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2 * epsilon, 180.0);
        } else if (angle > -150 && angle <= -90) {
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2 * epsilon, -120.0);
        } else if (angle > -90 && angle <= -30) {
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2 * epsilon, -60.0);
        }
        return null;
    }

    /*
     * This function gives the centre point of neighbouring hexagon based on the neighbouring hexagon code
     */
    static public GPSPoint getNeighbouringHexagon(int code, GPSPoint firstPositionHere, float epsilon) {
        if (code == 1) {
            return GeoHelper.getPointWithPolarDistance(firstPositionHere, epsilon, 0.0);
        } else if (code == 2) {
            return GeoHelper.getPointWithPolarDistance(firstPositionHere, epsilon, 60.0);
        } else if (code == 3) {
            return GeoHelper.getPointWithPolarDistance(firstPositionHere, epsilon, 120.0);
        } else if (code == 4) {
            return GeoHelper.getPointWithPolarDistance(firstPositionHere, epsilon, 180.0);
        } else if (code == 5) {
            return GeoHelper.getPointWithPolarDistance(firstPositionHere, epsilon, -120.0);
        } else if (code == 6) {
            return GeoHelper.getPointWithPolarDistance(firstPositionHere, epsilon, -60.0);
        }
        return null;
    }

    static public void addHexaGonPointsToConstructions(ArrayList<GPSPoint> whileConstructing, float epsilon, GPSPoint currentCentre) {
        whileConstructing.add(currentCentre);
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, 30.0));
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, 90.0));
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, 150.0));
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, -150.0));
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, -90.0));
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, -30.0));
        whileConstructing.add(GeoHelper.getPointWithPolarDistance(currentCentre, epsilon, 30.0));
        whileConstructing.add(currentCentre);
    }
}
