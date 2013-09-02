package Compression;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */

import Constants.Constants;
import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
public class OctaGon {
    
    
    /*
     * This function returns the code to be usd for transition to neighbouring hexagon.
     * This code is based on the side on the label of neighbouring hexagon.
     */
    static public Integer transitionCodeForSide(double angle){
        if (angle > -22.5 && angle <=22.5){
            return new Integer(Constants.FIRST_NEIGHBOURING_KGON);
        }else if (angle > 22.5 && angle <= 67.5){
            return new Integer(Constants.SECOND_NEIGHBOURING_KGON);
        }else if (angle > 67.5 && angle <= 112.5){
            return new Integer(Constants.THIRD_NEIGHBOURING_KGON);
        }else if (angle > 112.5 && angle <= 157.5){
            return new Integer(Constants.FOURTH_NEIGHBOURING_KGON);
        }else if ((angle > 157.5 && angle <= 180) || (angle >= -180 && angle <= -157.5)){
            return new Integer(Constants.FIFTH_NEIGHBOURING_KGON);
        }else if (angle > -157.5 && angle <= -112.5){
            return new Integer(Constants.SIXTH_NEIGHBOURING_KGON);
        }else if (angle > -112.5 && angle <= -67.5){
            return new Integer(Constants.SEVENTH_NEIGHBOURING_KGON);
        }else if (angle > -67.5 && angle <= -22.5){
            return new Integer(Constants.EIGHTH_NEIGHBOURING_KGON);
        }
        return null;
    }
    
    
    /*
     * This function returns the new center according to the angle and allowed error.
     */
    
    static public GPSPoint newCenterOfNeighbouringHexagon(double angle, float epsilon, GPSPoint currentCentre){
        if (angle > -22.5 && angle <=22.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, 0.0);
        }else if (angle > 22.5 && angle <= 67.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, 45.0);
        }else if (angle > 67.5 && angle <= 112.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, 90.0);
        }else if (angle > 112.5 && angle <= 157.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, 135.0);
        }else if ((angle > 157.5 && angle <= 180) || (angle >= -180 && angle <= -157.5)){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, 180.0);
        }else if (angle > -157.5 && angle <= -112.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, -135.0);
        }else if (angle > -112.5 && angle <= -67.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, -90.0);
        }else if (angle > -67.5 && angle <= -22.5){
            return GeoHelper.getPointWithPolarDistance(currentCentre, 2*epsilon, -45.0);
        }
        return null;
    }
    
}
