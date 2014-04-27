package Compression;


import java.text.DecimalFormat;
import java.util.ArrayList;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * Distance based error bound compression scheme which is based on logical grid.
 *
 * Here I define the coding for 4 bit combination I use to store different
 * states in case of adaptive approximation algorithm: 0 => stays in the same
 * hexagon 1 => moves to the right hexagon 2 - 6 => move to the neighboring
 * hexagon next to labeled as i in clock-wise fashion, 1 being the immediate
 * right neighbor 9 => indicates the start/finish of doubling the approximation
 * error in subsequent hexagons 10 => indicates the start/finish of halving the
 * approximation error in subsequent hexagons 12 => indicates the start/finish of
 * quadrupling the approximation error in subsequent hexagons 13 => indicates
 * the start/finish of quartering the approximation error in subsequent hexagons
 * 11 => indicates the use of simple approximation error. 14 would be used
 * for time tracking in the same manner as approximation error
 *
 * @author ghulammurtaza
 */
import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
import Constants.Constants;
import Helper.IOHelper;
import Helper.Utility;
import Helper.Pair;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public class KGonCompression {
    
    /*
     * This functions implements the adaptive KGon compression technique
     * 
     */
    
    public ArrayList<Integer> performAdaptiveKGonCompression(ArrayList<GPSPoint> source, int epsilon, String distanceType, ArrayList<GPSPoint> whileConstructing, String kGonType) {
        ArrayList<Integer> resultantPoints = new ArrayList<Integer>();
        GPSPoint currentCentre = new GPSPoint();
        float allowedEpsilon = epsilon;
        int holdsCode = 0;

        for (int i = 0; i < source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            String checker = "Null";
            if (i == 0) {
                currentCentre = source.get(i);
                //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);
                whileConstructing.add(currentCentre);
            } else {//in this case either approximation on current centre is going to happen or new centre calculation

                float distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                int k = 0;
                if (i < source.size()-2) 
                    k = (int)howManyTimesDoublingRequired(epsilon, i, source, currentCentre);
                
                if(distance >getSideLengthToUse(epsilon, angle, distanceType) && distance < ((2*getSideLengthToUse(epsilon, angle, distanceType))+epsilon)) {
                    checker = "Same";
                    if (holdsCode != Constants.CURRENT_EPSILON_START_OR_FINISH) {

                        if (holdsCode != Constants.SAME_KGON) {
                            resultantPoints.add(new Integer(holdsCode));
                        }
                        holdsCode = Constants.CURRENT_EPSILON_START_OR_FINISH;
                        resultantPoints.add(new Integer(holdsCode));
                    }
                     GPSPoint tempCurrent = currentCentre;
                    currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                    addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

                    whileConstructing.add(currentCentre);

                        //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                }else if(distance < getSideLengthToUse(epsilon, angle, distanceType)) {
                    checker = "One Increment";
                    if (holdsCode != Constants.SAME_KGON) {
                        resultantPoints.add(new Integer(holdsCode));
                        holdsCode = Constants.SAME_KGON;
                        resultantPoints.add(new Integer(holdsCode));
                    } else {
                        resultantPoints.add(new Integer(holdsCode));
                    }
                }else{

                    if (k>0) {
                        checker = "K Greater";
                        /************************/

                            if (holdsCode != Constants.SAME_KGON) {
                                resultantPoints.add(new Integer(holdsCode));
                            }

                            holdsCode = Constants.DOUBLING_EPSILON_START_OR_FINISH;
                            resultantPoints.add(new Integer(holdsCode));
                            int totalIncrements = 0;
                            while (totalIncrements<k) {

                                epsilon = 2 * epsilon;

                                whileConstructing.add(currentCentre);
                                //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);


                                GPSPoint tempCurrent = currentCentre;
                                currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);

                                addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);
                                System.out.println("Ehe epsilon is: "+epsilon+" distance from current centre is: "+GeoHelper.getDistance(currentCentre, source.get(i)));

                                whileConstructing.add(currentCentre);

                                //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);


                                //handleDoublingRequired(distance, angle, epsilon, distanceType, kGonType, source.get(i), currentCentre, resultantPoints, whileConstructing);
                                totalIncrements++;
                            }
                            k = (int)howManyTimesDoublingRequired(epsilon, i, source, currentCentre);
                            distance = GeoHelper.getDistance(currentCentre, source.get(i));
                            boolean isHalvingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);
                            System.out.println("The K is calculated again after doubling: "+k);
                            System.out.println("Is halving required: "+isHalvingRequired);

                            if (isHalvingRequired) {
                                if (holdsCode != Constants.SAME_KGON) {
                                    System.out.println("WRITING: " + holdsCode);
                                    resultantPoints.add(new Integer(holdsCode));
                                }
                                holdsCode = Constants.HALVING_EPSILON_START_OR_FINISH;
                                resultantPoints.add(new Integer(holdsCode));
                                System.out.println("Going in 88");
                                while (isHalvingRequired) {




                                    whileConstructing.add(currentCentre);

                                    //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                                    GPSPoint tempCurrent = currentCentre;
                                    currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                                    addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

                                    whileConstructing.add(currentCentre);
                                    System.out.println("Ehe epsilon is: "+epsilon+" distance from current centre is: "+GeoHelper.getDistance(currentCentre, source.get(i)));
                                    epsilon = epsilon / 2;
                                    distance = GeoHelper.getDistance(currentCentre, source.get(i));
                                    isHalvingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);
                                    System.out.println("Is halving required: "+isHalvingRequired);
                                    //System.out.println("current epsilon is: " + epsilon);
                                    //System.out.println("Distance is: " + distance);
                                    //System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                                }
                            }



                        /************************/


                        //holdsCode = recordPointsOutsideTheKGon(distance, angle,allowedEpsilon, epsilon, distanceType, kGonType, source.get(i), holdsCode, currentCentre, resultantPoints, whileConstructing, k);

                    } else if (k<0) {
                        
                        checker = "K Lesser";
                        
                        if (holdsCode != Constants.SAME_KGON) {

                            resultantPoints.add(new Integer(holdsCode));
                        }
                        holdsCode = Constants.HALVING_EPSILON_START_OR_FINISH;
                        resultantPoints.add(new Integer(holdsCode));
                        while (k<0) {
                            epsilon = epsilon / 2;

                            whileConstructing.add(currentCentre);

                            //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                           GPSPoint tempCurrent = currentCentre;
                            currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                            addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

                            whileConstructing.add(currentCentre);

                            //HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                            k++;
//-16.474336331806878,145.42563891910737
//-16.474336331806878,145.47061518574932
//-16.492848458506774,145.459469644895            

                        }
                    }
                    System.out.println("This one took path: "+checker);
            }
            }

        }
        return resultantPoints;
    }
    
    /*
     * This function handles the case when new point lies outside of current hexagon.
     */
     
    int recordPointsOutsideTheKGon(float distance, double angle, float allowedEpsilon, float epsilon, String distanceType, String kGonType, GPSPoint sourcePoint, int holdsCode, GPSPoint currentCentre, ArrayList<Integer> resultantPoints, ArrayList<GPSPoint> whileConstructing, int k){
        
        boolean doublingRequired = isDoublingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType));
                    
        if (doublingRequired) {

            if (holdsCode != Constants.SAME_KGON) {
                resultantPoints.add(new Integer(holdsCode));
            }

            holdsCode = Constants.DOUBLING_EPSILON_START_OR_FINISH;
            resultantPoints.add(new Integer(holdsCode));
            int i = 0;
            while (i<k) {

                handleDoublingRequired(distance, angle, epsilon, distanceType, kGonType, sourcePoint, currentCentre, resultantPoints, whileConstructing);
                i++;
            }
            
            angle = GeoHelper.getGPSAngle(currentCentre, sourcePoint);
            distance = GeoHelper.getDistance(currentCentre, sourcePoint);
            
            boolean halvingingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);
            if (halvingingRequired) {
                if (holdsCode != Constants.SAME_KGON) {
                    System.out.println("WRITING: " + holdsCode);
                    resultantPoints.add(new Integer(holdsCode));
                }
                holdsCode = Constants.HALVING_EPSILON_START_OR_FINISH;
                resultantPoints.add(new Integer(holdsCode));
                System.out.println("Going in 88");
                while (halvingingRequired) {
                    epsilon = epsilon / 2;

                    whileConstructing.add(currentCentre);

                    HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                    GPSPoint tempCurrent = currentCentre;
                    currentCentre = calculateNewCentre(tempCurrent, sourcePoint, epsilon, distanceType, kGonType);
                    addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

                    whileConstructing.add(currentCentre);

                    HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                    angle = GeoHelper.getGPSAngle(currentCentre, sourcePoint);
                    distance = GeoHelper.getDistance(currentCentre, sourcePoint);
                    halvingingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);
                    //System.out.println("current epsilon is: " + epsilon);
                    //System.out.println("Distance is: " + distance);
                    //System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                }
            }

        } else {
            if (holdsCode != Constants.CURRENT_EPSILON_START_OR_FINISH) {

                if (holdsCode != Constants.SAME_KGON) {
                    resultantPoints.add(new Integer(holdsCode));
                }
                holdsCode = Constants.CURRENT_EPSILON_START_OR_FINISH;
                resultantPoints.add(new Integer(holdsCode));
            }
             GPSPoint tempCurrent = currentCentre;
            currentCentre = calculateNewCentre(tempCurrent, sourcePoint, epsilon, distanceType, kGonType);
            addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

            whileConstructing.add(currentCentre);

            HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

            distance = GeoHelper.getDistance(currentCentre, sourcePoint);
        }
        return holdsCode;
    }
    /*
     * In case the new point is outside the hexagon, this function handles the case where we have to double the size of hexagon.
     */
    void handleDoublingRequired(float distance, double angle, float epsilon, String distanceType, String kGonType, GPSPoint sourcePoint, GPSPoint currentCentre, ArrayList<Integer> resultantPoints, ArrayList<GPSPoint> whileConstructing){
        epsilon = 2 * epsilon;

        whileConstructing.add(currentCentre);
        HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);


        GPSPoint tempCurrent = currentCentre;
        currentCentre = calculateNewCentre(tempCurrent, sourcePoint, epsilon, distanceType, kGonType);

        addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);

        whileConstructing.add(currentCentre);
        HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

        
    }
    
    
    /*
     * This is a functionw which calculates the value of K in improvement of adaptive compression
     */
    float howManyTimesDoublingRequired(float epsilon, int index, ArrayList<GPSPoint> source, GPSPoint currentCentre ){
        float firstDistance = GeoHelper.getDistance(currentCentre, source.get(index));
        float secondDistance = GeoHelper.getDistance(source.get(index), source.get(index+1));
        System.out.println("The first distance is: "+firstDistance+" Second Distance is: "+secondDistance+" Epsilon is: "+epsilon);
        System.out.println((IOHelper.log((((firstDistance+secondDistance)/epsilon)+1)/3, 2)) - 1);
        return (IOHelper.log((((firstDistance+secondDistance)/epsilon)+1)/3, 2)) - 1;
    }
    
    /*
     * This is a functionw which calculates the value of m in improvement of adaptive compression
     */
    float howManyTimesHalvingRequired(float epsilon, int index, ArrayList<GPSPoint> source, GPSPoint currentCentre ){
        float firstDistance = GeoHelper.getDistance(currentCentre, source.get(index));
        float secondDistance = GeoHelper.getDistance(source.get(index), source.get(index+1));
        return IOHelper.log((firstDistance+secondDistance)/epsilon, 2);
    }
    
    
   /* 
     * This function implements the adaptive GridCompression technique.
     */

    public ArrayList<Integer> performAdaptiveGridCompression(ArrayList<GPSPoint> source, int epsilon, String distanceType, ArrayList<GPSPoint> whileConstructing, String kGonType) {
        ArrayList<Integer> resultantPoints = new ArrayList<Integer>();
        GPSPoint currentCentre = new GPSPoint();
        GPSPoint lastPoint = new GPSPoint();
        GPSPoint currentPoint = new GPSPoint();
        GPSPoint firstPoint ;
        GPSPoint firstPointForCalibration = null;
        float allowedEpsilon = epsilon;
        int holdsCode = 0;

        for (int i = 0; i < source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                currentCentre = source.get(i);

                HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                whileConstructing.add(currentCentre);
                
                firstPoint = source.get(i);
                firstPointForCalibration = new GPSPoint(firstPoint.getLongitude(), firstPoint.getLatitude());
            } else {//in this case either approximation on current centre is going to happen or new centre calculation

                float distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));

                if (distance > getSideLengthToUse(epsilon, angle, distanceType)) {
                    boolean doublingRequired = isDoublingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType));
                    boolean halvingingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);

                    if (doublingRequired) {

                        if (holdsCode != Constants.SAME_KGON) {
                            
                            resultantPoints.add(new Integer(holdsCode));
                        }

                        holdsCode = Constants.DOUBLING_EPSILON_START_OR_FINISH;
                        resultantPoints.add(new Integer(holdsCode));
                        

                        while (doublingRequired) {




                            epsilon = 2 * epsilon;

                            whileConstructing.add(currentCentre);
                            HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);


                            GPSPoint tempCurrent = currentCentre;
                            currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);

                            addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);
                            
                            whileConstructing.add(currentCentre);
                            HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                            angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                            distance = GeoHelper.getDistance(currentCentre, source.get(i));
                            doublingRequired = isDoublingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType));
                            System.out.println("current epsilon is: " + epsilon);
                            System.out.println("Distance is: " + distance);
                            System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                        }

                        //System.out.println("Going OUTTTTTT 77 - 11111");
//                 
                        //System.out.println("Going OUTTTTTT 88");
                    } else {
                        if (holdsCode != Constants.CURRENT_EPSILON_START_OR_FINISH) {

                            if (holdsCode != Constants.SAME_KGON) {
                                System.out.println("WRITING: " + holdsCode);
                                resultantPoints.add(new Integer(holdsCode));
                            }
                            System.out.println("Going in 11111");
                            holdsCode = Constants.CURRENT_EPSILON_START_OR_FINISH;
                            resultantPoints.add(new Integer(holdsCode));
                        }
                        System.out.println("1111111111111111111 " + holdsCode);
                        GPSPoint tempCurrent = currentCentre;
                        currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                        addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

                        whileConstructing.add(currentCentre);
                        
                        HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                        distance = GeoHelper.getDistance(currentCentre, source.get(i));
                        System.out.println("current epsilon is: " + epsilon);
                        System.out.println("Distance is: " + distance);
                        System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                    }
                } else {
                    boolean halvingingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);
                    if (halvingingRequired) {
                        if (holdsCode != Constants.SAME_KGON) {
                            System.out.println("WRITING: " + holdsCode);
                            resultantPoints.add(new Integer(holdsCode));
                        }
                        holdsCode = Constants.HALVING_EPSILON_START_OR_FINISH;
                        resultantPoints.add(new Integer(holdsCode));
                        System.out.println("Going in 88");
                        while (halvingingRequired) {
                            epsilon = epsilon / 2;

                            whileConstructing.add(currentCentre);
                            
                            HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                            GPSPoint tempCurrent = currentCentre;
                            currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                            addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

                            whileConstructing.add(currentCentre);
                            
                            HexaGon.addHexaGonPointsToConstructions(whileConstructing, epsilon, currentCentre);

                            angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                            distance = GeoHelper.getDistance(currentCentre, source.get(i));
                            halvingingRequired = isHalvingTheEpsilonRequired(distance, getSideLengthToUse(epsilon, angle, distanceType), allowedEpsilon);
                            System.out.println("current epsilon is: " + epsilon);
                            System.out.println("Distance is: " + distance);
                            System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                        }
//-16.474336331806878,145.42563891910737
//-16.474336331806878,145.47061518574932
//-16.492848458506774,145.459469644895            
                        System.out.println("Going OUTTTTTT 88");
                    } else {
                        System.out.println("current epsilon is: " + epsilon);
                        System.out.println("Distance is: " + distance);
                        if (holdsCode != Constants.SAME_KGON) {
                            System.out.println("WRITING: " + holdsCode);
                            resultantPoints.add(new Integer(holdsCode));
                            System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                            holdsCode = Constants.SAME_KGON;
                            resultantPoints.add(new Integer(holdsCode));
                        } else {
                            System.out.println("WRITING: " + holdsCode);
                            resultantPoints.add(new Integer(holdsCode));
                            System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                        }
                    }
                }
            }

        }
        return resultantPoints;
    }

    /*
     * This finction checks if there is a need to 
     * halve the allowed epsilon.
     */
    boolean isQuadruplingTheEpsilonRequired(float distance, float epsilonToBeUsed, float allowedEpsilon) {
        if ((((4 * epsilonToBeUsed) + epsilonToBeUsed) > distance) && distance > (((4 * allowedEpsilon) + allowedEpsilon))) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * This finction checks if there is a need to 
     * double the allowed epsilon.
     */
    boolean isQuarteringTheEpsilonRequired(float distance, float epsilonToBeUsed) {
        if (distance > ((4 * epsilonToBeUsed) + epsilonToBeUsed)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * This finction checks if there is a need to 
     * halve the allowed epsilon.
     */
    boolean isHalvingTheEpsilonRequired(float distance, float epsilonToBeUsed, float allowedEpsilon) {
        if ((((2 * epsilonToBeUsed) + epsilonToBeUsed) > distance) && epsilonToBeUsed > allowedEpsilon){
            return true;
        } else {
            return false;
        }
    }

    /*
     * This finction checks if there is a need to 
     * double the allowed epsilon.
     */
    boolean isDoublingTheEpsilonRequired(float distance, float epsilonToBeUsed) {
        if (distance > ((2 * epsilonToBeUsed) + epsilonToBeUsed)) {
            return true;
        } else {
            return false;
        }
    }

    
    public Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> performGridCompressionCodedInterpolation(ArrayList<GPSPoint> source,ArrayList<GPSPoint> whileConstructing, int epsilon, String distanceType, String kGonType, ArrayList<WorstBinCounter> binCounterArray, int timeEpsilon, ArrayList<Double> looseIndividualDistanceList) {
        
        ArrayList<Integer> resultantPoints = new ArrayList<Integer>();
        int minJumpSize = 999999999;
        int bitCounter = 0;
        int chaseCounter = 0;
        HashMap<Integer, Integer> jumpSizes = new HashMap<Integer, Integer>();
        //Pair<ArrayList<Double>, HashMap<Integer, Integer>> returningPair;
        GPSPoint currentCentre = new GPSPoint();
        GPSPoint firstPoint;
        DecimalFormat df = new DecimalFormat("#.##");
        Date currentTime = new Date();
        int simpleDoubleBenefitCounter = 0;
        HashMap<Integer, Integer> worstCaseMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> worstCaseConsecutiveMap = new HashMap<Integer, Integer>();
        int consecutiveCount = 0;
        boolean checkConsecutiveCount = false;
        float timeDifference;
        for (int i = 0; i < source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                currentCentre = source.get(i);
                firstPoint = source.get(i);
                currentTime = source.get(i).getTimeStamp();
                
            } else {
                
                if (i==760){
                    System.out.println("in 10th place");
                }
                
                double distance = 0;
                distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = 0.0;
                angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                
                
                timeDifference =  (source.get(i).getTimeStamp().getTime() - currentTime.getTime());
                
                if (distance > getSideLengthToUse(epsilon, angle, distanceType)) {
                    GPSPoint tempCurrent = currentCentre;
                    
                    if (distance > 3*getSideLengthToUse(epsilon, angle, distanceType)){
                        
                        int quadrant = (int)returnQuadrantCode(angle);
                        //System.out.println("Distance is: "+distance);
                        int multipleOfEpsilon = (int)(distance/getSideLengthToUse(epsilon, angle, distanceType));
                        resultantPoints.add(Constants.START_FINISH_OF_STEP_COUNT);
                        
                        resultantPoints.add(returnQuadrantCode(angle));
                        
                        int xJump =  calculateHorizontalJump(distance, angle,epsilon);
                        int yJump =  calculateVerticalJump(distance, angle,epsilon, xJump);
                        int jumpsize = jumpSize(new Jump(xJump, yJump, 0), epsilon);
                        
                        if (jumpSizes.containsKey(jumpsize)){
                            jumpSizes.put(jumpsize,jumpSizes.get(jumpsize)+1);
                        }else{
                            jumpSizes.put(jumpsize,+1);
                        }
                        
                        if (jumpsize<minJumpSize)
                            minJumpSize = jumpSize(new Jump(xJump, yJump, 0), epsilon);
                        
                        bitCounter += calcualteBitsForCodingJumpEmpty(xJump);
                        bitCounter += calcualteBitsForCodingJumpEmpty(yJump);
      
                        resultantPoints.add(xJump);
                        resultantPoints.add(yJump);
                        currentCentre = calculateNewCentreWithHexagonMultiples(currentCentre, epsilon, xJump, yJump, quadrant);
                        
//                        if (GeoHelper.getDistance(currentCentre, source.get(i))>epsilon){
//                            System.out.println("x jumps is: "+xJump);
//                            System.out.println("y jump is: "+yJump);
//                            System.out.println("Current Centre: "+currentCentre.getLongitude()+", "+currentCentre.getLatitude());
//                            System.out.println("Current Point: "+source.get(i).getLongitude()+", "+source.get(i).getLatitude());
//                            System.out.println("New Centre: "+currentCentre.getLongitude()+", "+currentCentre.getLatitude());
//                            System.out.println("Distance is: "+GeoHelper.getDistance(currentCentre, source.get(i)));
//                        }
                        recordEmptyBinCount(multipleOfEpsilon, worstCaseMap);
                        if (consecutiveCount > 0){
                            recordConsecutivePointsCount(consecutiveCount, worstCaseConsecutiveMap);
                            consecutiveCount = 0;
                        }
                        
                        

                    }else{
                        chaseCounter +=1;
                        bitCounter += 3;
                        checkConsecutiveCount = true;
                        currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                        
                        addCurrentPointDouble(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection
                        
//                        System.out.println("Resultant code is: "+resultantPoints.get(resultantPoints.size()-1));
                        
                        simpleDoubleBenefitCounter++;
                        //worstCaseMap.put(new Integer(1), simpleDoubleBenefitCounter);
                        if (consecutiveCount == 0)
                            consecutiveCount+=2;
                        else
                            consecutiveCount++;
                    }
                    
                    //resultantPoints.add(new Double(getTimeCode(timeDifference)));
                    
                    
                }
                if (timeDifference >= timeEpsilon && timeDifference < 2*timeEpsilon){
                    resultantPoints.add(07);
                    long newDateTime = currentTime.getTime()+ 2*timeEpsilon;
                    currentTime = new Date(newDateTime);
                    currentCentre.setTimeStamp(currentTime);
                    //currentTime = new Date(source.get(i).getTimeStamp().getTime());
                    bitCounter += 6;
                }else if (timeDifference > 2*timeEpsilon){
                    resultantPoints.add(70);
                    resultantPoints.add((int)Math.round(timeDifference /timeEpsilon));
                    
                    long timeToSet = currentTime.getTime()+ (int)Math.round(timeDifference /timeEpsilon)*timeEpsilon;
                    currentTime = new Date(timeToSet) ;
                    currentCentre.setTimeStamp(currentTime);
                    bitCounter += 9;
                }
            }
            looseIndividualDistanceList.add((double)GeoHelper.getDistance(currentCentre, source.get(i)));
            GPSPoint toAdd = (GPSPoint)Utility.copy(currentCentre);
            whileConstructing.add(toAdd);
        }
        //printHashMap(jumpSizes);
        //System.out.println("Chases = "+chaseCounter);
        resultantPoints.add(minJumpSize);
        resultantPoints.add(bitCounter);
        //System.out.println("Total number of empty bins is: "+worstCaseMap.keySet().size());
        return new Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>>( worstCaseConsecutiveMap,new Pair<ArrayList<Integer>, HashMap<Integer, Integer>>(resultantPoints, worstCaseMap)); //using Pair helper, I am sending multiple data structures as return
    }
    
    void printHashMap(HashMap<Integer, Integer> jumpSizes){
        Iterator it = jumpSizes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
        
    public Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> performGridCompressionCodedJumps(ArrayList<GPSPoint> source,ArrayList<GPSPoint> whileConstructing, int epsilon, String distanceType, String kGonType, ArrayList<Date> allDateTimeValues, ArrayList<WorstBinCounter> binCounterArray) {
        
        ArrayList<Integer> resultantPoints = new ArrayList<Integer>();
        int bitCounter = 0;
        //Pair<ArrayList<Double>, HashMap<Integer, Integer>> returningPair;
        GPSPoint currentCentre = new GPSPoint();
        GPSPoint firstPoint;
        GPSPoint firstPointForCalibration = null;
        DecimalFormat df = new DecimalFormat("#.##");
        Date currentTime = new Date();
        int simpleDoubleBenefitCounter = 0;
        HashMap<Integer, Integer> worstCaseMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> worstCaseConsecutiveMap = new HashMap<Integer, Integer>();
        int consecutiveCount = 0;
        boolean checkConsecutiveCount = false;
        float timeDifference;
        for (int i = 0; i < source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                currentCentre = source.get(i);
                firstPoint = source.get(i);
                firstPointForCalibration = new GPSPoint(firstPoint.getLongitude(), firstPoint.getLatitude());
                currentTime = allDateTimeValues.get(i);
            } else {//in this case either approximation on current centre is going to happen or new centre calculation
                //if ("145.3997379 x -16.3983188".equals(source.get(i).getLongitude()+" x "+source.get(i).getLatitude()))
                    //System.out.println("Current centre Longitude x Latitude is: "+currentCentre.getLongitude()+" x "+currentCentre.getLatitude());
                double distance = 0;
                distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = 0;
                angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                
                
                timeDifference =  (allDateTimeValues.get(i).getTime() - currentTime.getTime())/1000;

                if (distance > getSideLengthToUse(epsilon, angle, distanceType)) {
                    GPSPoint tempCurrent = currentCentre;
                    int quadrant = (int)returnQuadrantCode(angle);
                    resultantPoints.add(quadrant);
//                    if (quadrant == 1 || quadrant == 4)
//                        resultantPoints.add(1);
//                    else
//                        resultantPoints.add(0);
                    //addHorizontalJump(resultantPoints, distance, angle, epsilon);
                    int horizontalJump = calculateHorizontalJump(distance, angle, epsilon);
                    bitCounter += calcualteBitsForCodingJump(horizontalJump);
                    int verticalJump = calculateVerticalJump(distance, angle, epsilon, horizontalJump);
                    bitCounter += calcualteBitsForCodingJump(verticalJump);
                    resultantPoints.add(horizontalJump);
                    resultantPoints.add(verticalJump);
                    currentCentre = calculateNewCentreWithHexagonMultiples(tempCurrent, epsilon,horizontalJump,verticalJump,quadrant);//calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);

                    //addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection

//                        System.out.println("Resultant code is: "+resultantPoints.get(resultantPoints.size()-1));

                    simpleDoubleBenefitCounter++;
                    //worstCaseMap.put(new Integer(1), simpleDoubleBenefitCounter);
                    if (consecutiveCount == 0)
                        consecutiveCount+=2;
                    else
                        consecutiveCount++;
                    
                    
                    //resultantPoints.add(new Double(getTimeCode(timeDifference)));
                    currentTime = allDateTimeValues.get(i);
                }
            }
            
            whileConstructing.add(currentCentre);
        }
        resultantPoints.add(bitCounter);
        //System.out.println("Total number of empty bins is: "+worstCaseMap.keySet().size());
        return new Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>>( worstCaseConsecutiveMap,new Pair<ArrayList<Integer>, HashMap<Integer, Integer>>(resultantPoints, worstCaseMap)); //using Pair helper, I am sending multiple data structures as return
    }
    
    
//    int addHorizontalJump(ArrayList<Integer> resultantPoints,double distance,double angle,double epsilon){
//        
//        int horizontalJump = calculateHorizontalJump(distance, angle, epsilon);
//        
//        int totalBits = calcualteBitsForCodingJump(horizontalJump);
//        
//        
//        return 0;
//        
//    }
    /*
     * This is for only jumped encoding
     */
    int calcualteBitsForCodingJump(int jumpStep){
        if (jumpStep <= 2)
            return 3;
        else if(jumpStep <= 65)
            return 9;
        else if (jumpStep <= 576)
            return 12;
        else 
            return 15;
                
    }
    
    /*
     * This is for the case where jumps are used for emty hexagon case
     */
    int calcualteBitsForCodingJumpEmpty(int numberJumps){
        if (numberJumps <= 0)
            return 0;
        else if (numberJumps <= 8)
            return 6;
        else if(numberJumps <= 72)
            return 12;
        else if (numberJumps <= 584)
            return 18;
        else 
            return 24;
                
    }
    
    /*
     * This is for the case where jumps are used for emty hexagon case in Code+Jump
     */
    int calcualteBitsForCodingplusJump(int numberJumps){
        if (numberJumps <= 8)
            return 9;
        else if(numberJumps <= 72)
            return 12;
        else if (numberJumps <= 584)
            return 15;
        else if (numberJumps <= 4608)
            return 18;
        else
            return 21;
                
    }
    
    /*
     * This fucntion gets the code for x number of hexagons
     */
    public int calculateHorizontalJump(double distance, double theeta, double epsilon){
        //GeoHelper.getBaseLength(theeta, distance);
        int horizontalHexagon = 0;
        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
        //System.out.println("Converted Theeta: "+convertedTheeta);
        //System.out.println("Horizontal Distance: "+calculateAndReturnHorizontalDistance(distance, theeta));
        int horizontalEpsilonMultiple = (int)Math.floor(calculateAndReturnHorizontalDistance(distance, theeta)/epsilon)-1;
        // case where it is odd number of hexagons
        if (horizontalEpsilonMultiple < 0)
            return 0;
        if (horizontalEpsilonMultiple%3 == 0) {
            horizontalHexagon = (2*((horizontalEpsilonMultiple/3)+1))-1;
        }else{
            horizontalHexagon = (2*((horizontalEpsilonMultiple+(3-(horizontalEpsilonMultiple%3)))/3));
        }
        return horizontalHexagon;
    }
    
    /*
     * This fucntion gets the code for x number of hexagons while re-compression
     */
    public int calculateHorizontalJumpRecompression(int horizontalEpsilonMultiple){
        //GeoHelper.getBaseLength(theeta, distance);
        int horizontalHexagon = 0;
        //System.out.println("Converted Theeta: "+convertedTheeta);
        //System.out.println("Horizontal Distance: "+calculateAndReturnHorizontalDistance(distance, theeta));
        // case where it is odd number of hexagons
        if (horizontalEpsilonMultiple < 0)
            return 0;
        if (horizontalEpsilonMultiple%3 == 0) {
            horizontalHexagon = (2*((horizontalEpsilonMultiple/3)+1))-1;
        }else{
            horizontalHexagon = (2*((horizontalEpsilonMultiple+(3-(horizontalEpsilonMultiple%3)))/3));
        }
        return horizontalHexagon;
    }
    
    
    
    /*
     * This fucntion gets the code for y number of hexagons
     */
    public int calculateVerticalJump(double distance, double theeta, double epsilon, int horizontalJump){
        //GeoHelper.getBaseLength(theeta, distance);
        
        epsilon = (epsilon * Math.sin(Math.toRadians(60.0)));
        int verticalHexagon = 0;
        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
        
        //System.out.println("Vertical Distance: "+calculateAndReturnVerticalDistance(distance, theeta));
        
       int verticalEpsilonMultiple = (int)Math.floor(calculateAndReturnVerticalDistance(distance, theeta)/epsilon);
       if (verticalEpsilonMultiple <= 0)
            return 0;
       //verticalEpsilonMultiple = horizontalJump%2 == 0?verticalEpsilonMultiple:verticalEpsilonMultiple+1; Now I am handling this at the time of calculation
       if (horizontalJump%2 ==0){
           verticalHexagon = verticalEpsilonMultiple%2 == 0?(int)verticalEpsilonMultiple/2:(verticalEpsilonMultiple+1)/2;
       }else{
           verticalHexagon = (int)(verticalEpsilonMultiple/2);
       }
       //:(verticalEpsilonMultiple+1)/2;
       return verticalHexagon;
        //return 0;
    }
    
    /*
     * This fucntion gets the code for y-axis number of hexagons while re-compression
     */
    public int calculateVerticalJumpRecompression(int horizontalJump, int verticalEpsilonMultiple){
        //GeoHelper.getBaseLength(theeta, distance);
        int verticalHexagon = 0;
        //System.out.println("Converted Theeta: "+convertedTheeta);
        //System.out.println("Horizontal Distance: "+calculateAndReturnHorizontalDistance(distance, theeta));
        // case where it is odd number of hexagons
        if (verticalEpsilonMultiple <= 0)
            return 0;
       //verticalEpsilonMultiple = horizontalJump%2 == 0?verticalEpsilonMultiple:verticalEpsilonMultiple+1; Now I am handling this at the time of calculation
       if (horizontalJump%2 ==0){
           verticalHexagon = verticalEpsilonMultiple%2 == 0?(int)verticalEpsilonMultiple/2:(verticalEpsilonMultiple+1)/2;
       }else{
           verticalHexagon = (int)verticalEpsilonMultiple/2;
       }
       //:(verticalEpsilonMultiple+1)/2;
       return verticalHexagon;
    }    
//    /*
//     * This fucntion gets the code for y number of hexagons
//     */
//    public double calculateVerticalJumpOddHorizontal(double distance, double theeta, double epsilon){
//        //GeoHelper.getBaseLength(theeta, distance);
//        epsilon = (epsilon * Math.sin(Math.toRadians(60.0)));
//        int verticalHexagon = 0;
//        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
//        
//       int verticalEpsilonMultiple = ((int)Math.floor(GeoHelper.getBaseLength(convertedTheeta, distance)/epsilon))+1;
//       verticalHexagon = verticalEpsilonMultiple%2 == 0?verticalEpsilonMultiple/2:(verticalEpsilonMultiple+1)/2;
//       return verticalHexagon;
//        //return 0;
//    }
    
    
    /*
     * This fucntion gets the code for x number of hexagons
     */
    public double calculateAndReturnHorizontalMultiple(double distance, double theeta){
        //GeoHelper.getBaseLength(theeta, distance);
        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
        //System.out.println("Converted Theeta: "+convertedTheeta);
        //System.out.println("Converted Theeta: "+(180-(90+convertedTheeta)));
       return GeoHelper.getBaseLength((180-(90+convertedTheeta)), distance);
        //return 0;
    }
    
    /*
     * This fucntion gets the code for y number of hexagons
     */
    public double calculateAndReturnVerticalMultiple(double distance, double theeta){
        //GeoHelper.getBaseLength(theeta, distance);
        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
        
       return GeoHelper.getBaseLength(convertedTheeta, distance);
        //return 0;
    }
    
    
    /*
     * This fucntion gets the code for x number of hexagons
     */
    public double calculateAndReturnHorizontalDistance(double distance, double theeta){
        //GeoHelper.getBaseLength(theeta, distance);
        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
        //System.out.println("Converted Theeta: "+convertedTheeta);
        //System.out.println("Converted Theeta: "+(180-(90+convertedTheeta)));
        
       double distanToReturn = Math.abs(theeta) <90? GeoHelper.getBaseLength((180-(90+convertedTheeta)), distance):GeoHelper.getBaseLength(convertedTheeta, distance);
       return distanToReturn;
    }
    
    /*
     * This fucntion gets the code for y number of hexagons
     */
    public double calculateAndReturnVerticalDistance(double distance, double theeta){
        //GeoHelper.getBaseLength(theeta, distance);
        double convertedTheeta = Math.abs(theeta)>90?Math.abs(theeta)-90:Math.abs(theeta);
       double distanToReturn = Math.abs(theeta) > 90? GeoHelper.getBaseLength((180-(90+convertedTheeta)), distance):GeoHelper.getBaseLength(convertedTheeta, distance); 
       return distanToReturn;
        //return 0;
    }
    
    
    public static GPSPoint calculateNewCentreWithHexagonMultiples(GPSPoint currentCentre, double allowedEpsilon, int horizontalCode, int verticalCode, int quadrantCode){
        double xAngle = 0.0;
        double yAngle = 0.0;
        if (quadrantCode == 1){
            xAngle = 0;
            yAngle = 90;
        }else if (quadrantCode == 2){ 
            xAngle = 180;
            yAngle = 90;
        }else if (quadrantCode == 3){ 
            xAngle = -180;
            yAngle = -90;
        }else if (quadrantCode == 4){
            xAngle = 0;
            yAngle = -90;
        }      
        double verticalEpsilon = (allowedEpsilon * Math.sin(Math.toRadians(60.0))); // this is epsilon used for vetical jump as that is from the middle of side
        
        double totalHorizontalDistance;
        if (horizontalCode == 1){
            totalHorizontalDistance = allowedEpsilon;
        }else{
            totalHorizontalDistance = (((int)horizontalCode/2)*(2*allowedEpsilon))+(((int)horizontalCode/2)*allowedEpsilon);
        }
        //System.out.println("Total distance added: "+totalHorizontalDistance);
        
        if (horizontalCode%2 != 0)
            totalHorizontalDistance += allowedEpsilon/2;
            
        //double totalVerticalDistance;
        //we use verticalcode-1 because of the fact that 
        double totalVerticalDistance = horizontalCode%2 == 0? (verticalCode)*(2*verticalEpsilon):((verticalCode)*(2*verticalEpsilon))+verticalEpsilon;
        
        GPSPoint pointWithHorizontal = GeoHelper.getPointWithPolarDistance(currentCentre, totalHorizontalDistance,xAngle);
        //System.out.println(pointWithHorizontal.getLongitude()+", "+pointWithHorizontal.getLatitude());
        return GeoHelper.getPointWithPolarDistance(pointWithHorizontal, totalVerticalDistance,yAngle);
        //return null;
    }
    
    
    public static GPSPoint calculateNewCentreWithMultiples(GPSPoint currentCentre, double allowedEpsilon, double firstCode, double secondCode, double quadrantCode){
        double xAngle = 0.0;
        double yAngle = 0.0;
        if (quadrantCode == 1){
            xAngle = 0;
            yAngle = 90;
        }else if (quadrantCode == 2){ // The reason for seemingly reverse thing is mapping on the first qudrant while multple calculatoin
            xAngle = 90;
            yAngle = 180;
        }else if (quadrantCode == 3){ // The reason for seemingly reverse thing is mapping on the first qudrant while multple calculatoin
            xAngle = -90;
            yAngle = -180;
        }else if (quadrantCode == 4){
            xAngle = 0;
            yAngle = -90;
        }
        GPSPoint pointWithHorizontal = GeoHelper.getPointWithPolarDistance(currentCentre, firstCode*allowedEpsilon,xAngle);
        return GeoHelper.getPointWithPolarDistance(pointWithHorizontal, secondCode*allowedEpsilon,yAngle);
        //return null;
    }
    
    /*
     * This function returns the quadtrant code for angle
     */
    public int returnQuadrantCode(double angle){
        if (angle > 90){
            return 2;
        }else if (angle > 0 && angle <= 90){
            return 1;
        }else if (angle <= 0 && angle > -90){
            return 4;
        }else if (angle < -90 ){
            return 3;
        }
        return 0;
    }
    
    public boolean addOrSubtractHorizontal(int firstCode, int secondCode){
        if ((firstCode==1||firstCode==4)&&(secondCode==1||secondCode==4)){
            return true;
        }else if((firstCode==2||firstCode==3)&&(secondCode==2||secondCode==3)){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean addOrSubtractVertical(int firstCode, int secondCode){
        if ((firstCode==1||firstCode==2)&&(secondCode==1||secondCode==2)){
            return true;
        }else if((firstCode==4||firstCode==3)&&(secondCode==4||secondCode==3)){
            return true;
        }else{
            return false;
        }
    }
    

    GPSPoint getExistingCentre(GPSPoint firstPositionHere, GPSPoint currentCentre, ArrayList<Integer> resultantPoints, float epsilon, String distanceType) {
        GPSPoint constructedPoint = null;
        int j = 0;
        for (int i = 0; i < resultantPoints.size(); i++) {
            HexaGon.getNeighbouringHexagon(resultantPoints.get(i), firstPositionHere, epsilon);
            j++;
            if (GeoHelper.getDistance(currentCentre, constructedPoint) < epsilon) {

                return constructedPoint;
            }
            firstPositionHere = constructedPoint;
        }
        return null;
    }

    boolean isCalculatedCenterNew(GPSPoint firstPositionHere, GPSPoint currentCentre, ArrayList<Integer> resultantPoints, float epsilon, String distanceType) {
        GPSPoint constructedPoint = null;
        int j = 0;
        for (int i = 0; i < resultantPoints.size(); i++) {
            
            HexaGon.getNeighbouringHexagon(resultantPoints.get(i), firstPositionHere, epsilon);
            j++;
            if (currentCentre.equals(constructedPoint)) {
                System.out.println("I is: " + j);
                return false;
            }
            firstPositionHere = constructedPoint;
        }
        return true;
    }

    /*
     * This function returns the appropriate length of epsilon
     */
    static public float getSideLengthToUse(float epsilon, double theeta, String distanceType) {
        if (distanceType.equals("exact")) {
            float ab;
            if (((int) (Math.abs(theeta) % 30)) == 0 && (int) ((Math.abs(theeta) / 30.0) % 2) != 0) {
                //System.out.println(epsilon);
                return epsilon;
            } else {
                // first calculate (AB) then calculate hypotteneous
                ab = (float) (epsilon * Math.sin(Math.toRadians(60.0)));
                if ((Math.abs(theeta) % 30.0) == 0) {
                    return ab;
                } else if (Math.abs(theeta) < 30) {
                    return (float) (ab / Math.sin(Math.toRadians(180 - (90 + Math.abs(theeta)))));
                } else {
                    double calc = 180 - (90 - (Math.abs(theeta) % 30));
                    double divisor = Math.sin(Math.toRadians(calc));
                    return (float) (ab / divisor);
                }
            }
        } else {
            return epsilon;
        }
    }

    /*
     * Calculate new centre in case difference is bigger than epsilon
     */
    public GPSPoint calculateNewCentre(GPSPoint currentCentre, GPSPoint currentPoint, float epsilon, String distanceType, String kGonType) {
        double angle = GeoHelper.getGPSAngle(currentCentre, currentPoint);
        //System.out.println("angle is: "+angle);
        //System.out.println(2*(float)(epsilon*Math.sin(Math.toRadians(45.0))));
        if (kGonType.equals("Hexa")) {
            return HexaGon.newCenterOfNeighbouringHexagon(angle, epsilon, currentCentre);
        } else if (kGonType.equals("Octa")) {
            return OctaGon.newCenterOfNeighbouringHexagon(angle, epsilon, currentCentre);
        }
        return null;
    }

    /*
     * This function returns the distance which should be used for next center
     * It can either be 2*epsilon or exact amount
     */
    static public float distanceToBeUsed(String distanceType, float epsilon) {
        if (distanceType.equals("exact")) {
            float calcAmount = (float) (epsilon * Math.sin(Math.toRadians(60.0)));
            return calcAmount;
        } else {
            return epsilon;
        }
    }

    /*
     * This function assigns the value between 0 and 5 to the currrent point movement
     */
    void addCurrentPoint(ArrayList<Integer> resultantPoints, GPSPoint tempCurrent, GPSPoint currentCentre, String kGonType) {
        double angle = GeoHelper.getGPSAngle(tempCurrent, currentCentre);

        if (kGonType.equals("Hexa")) {
            resultantPoints.add(HexaGon.transitionCodeForSide(angle));
        } else if (kGonType.equals("Octa")) {
            resultantPoints.add(OctaGon.transitionCodeForSide(angle));
        }

    }
    
    /*
     * This function assigns the value between 0 and 5 to the currrent point movement with double variant
     */
    void addCurrentPointDouble(ArrayList<Integer> resultantPoints, GPSPoint tempCurrent, GPSPoint currentCentre, String kGonType) {
        double angle = GeoHelper.getGPSAngle(tempCurrent, currentCentre);

        if (kGonType.equals("Hexa")) {
            resultantPoints.add(HexaGon.transitionCodeForSide(angle));
        } else if (kGonType.equals("Octa")) {
            resultantPoints.add(OctaGon.transitionCodeForSide(angle));
        }

    }
    
    
    Pair<Integer, GPSPoint> handleCodeBasedSteps(ArrayList<Integer> resultantPoints,GPSPoint sourcePoint, GPSPoint currentCentre, float epsilonTouse,String distanceType,String kGonType, int holdsSizeCode, int currentCode){
        if(holdsSizeCode == currentCode){
            GPSPoint tempCurrent = currentCentre;
            currentCentre = calculateNewCentre(tempCurrent, sourcePoint, epsilonTouse, distanceType, kGonType);
            addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection
        //}else if(holdsSizeCode == Constants.CODE_FOR_THIRD_SIZE){
          //  resultantPoints.add(new Integer(0));
        }else{
            resultantPoints.add(new Integer(holdsSizeCode));
            holdsSizeCode = currentCode;
            resultantPoints.add(new Integer(holdsSizeCode));
            GPSPoint tempCurrent = currentCentre;
            currentCentre = calculateNewCentre(tempCurrent, sourcePoint, epsilonTouse, distanceType, kGonType);
            addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);
            //resultantPoints.add(new Integer(0));

        }
        return new Pair<Integer, GPSPoint>(holdsSizeCode, currentCentre);
    }
    
    
     public ArrayList<GPSPoint> performInterpolation(ArrayList<GPSPoint> source, int epsilon, String distanceType, ArrayList<GPSPoint> whileConstructing, String kGonType) {
        ArrayList<GPSPoint> resultantPoints = new ArrayList<GPSPoint>();
        GPSPoint currentCentre = new GPSPoint();
        
        for (int i = 0; i < source.size(); i++) {//source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                currentCentre = source.get(i);
                resultantPoints.add(source.get(i));
            } else {//in this case either approximation on current centre is going to happen or new centre calculation

                float distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));

                if (distance > getSideLengthToUse(epsilon, angle, distanceType)) {
                    while(distance > getSideLengthToUse(epsilon, angle, distanceType)){
                       currentCentre = GeoHelper.getPointWithPolarDistance(currentCentre, getSideLengthToUse(epsilon, angle, distanceType), angle);
                       resultantPoints.add(currentCentre);
                       
                       distance = GeoHelper.getDistance(currentCentre, source.get(i));
                       angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                    }   

                    } else {
                    resultantPoints.add(source.get(i));
                }
            }
        
        }
        
        
        
        return resultantPoints;
     }
     
     int getTimeCode(float distance){
         
         if (Utility.roundedNumber((int)distance) == Constants.LIMIT_FOR_FIRST_TIME_STEP){
            return Constants.CODE_FOR_FIRST_TIME_SIZE;
        }else if(Utility.roundedNumber((int)distance) == Constants.LIMIT_FOR_SECOND_TIME_STEP){
            return Constants.CODE_FOR_SECOND_TIME_SIZE;
        }else if(Utility.roundedNumber((int)distance) == Constants.LIMIT_FOR_THIRD_TIME_STEP){
            return Constants.CODE_FOR_THIRD_TIME_SIZE;
        }else if(Utility.roundedNumber((int)distance) == Constants.LIMIT_FOR_FOURTH_TIME_STEP){
            return Constants.CODE_FOR_FOURTH_TIME_SIZE;
        }else if(Utility.roundedNumber((int)distance) == Constants.LIMIT_FOR_FIFTH_TIME_STEP){
            return Constants.CODE_FOR_FIFTH_TIME_SIZE;
        }else if(Utility.roundedNumber((int)distance) == Constants.LIMIT_FOR_SIXTH_TIME_STEP){
            return Constants.CODE_FOR_SIXTH_TIME_SIZE;
        }
         
        
         return 0;
     }
    
     int getNewTimeCode(float distance, float timeEpsilon){
         
         return 0;
     }
     
     boolean isItThisCode(int timeCode, float distance){
        if (distance >= timeCode-100 && distance <= timeCode+100){
            return true;
        }else{
            return false;
        }
     }
     
     /*
      * This is bin counter for recording the points which have empty hexagons
      * in between two points.
      */
     void recordEmptyBinCount(int multipleOfEpsilon, HashMap<Integer, Integer> worstCaseMap){
         int evenClosestBin;
        evenClosestBin = (((multipleOfEpsilon)%2)==0)?multipleOfEpsilon:multipleOfEpsilon+1;
         if (worstCaseMap.containsKey(new Integer(evenClosestBin))){
             int temp = worstCaseMap.get(evenClosestBin);
             worstCaseMap.put(new Integer(evenClosestBin), new Integer(temp+1));
         }else{
             worstCaseMap.put(new Integer(evenClosestBin), new Integer(1));
         }
         
     }
     
     /*
      * This counter helps counting the gain by consecutive bin counts
      */
     void recordConsecutivePointsCount(int consecutiveCount, HashMap<Integer, Integer> worstCaseMap){
         int binDivisionStep = 2;
         int binHeadStep = 2;
         while(true){
             int binDivisionResult = consecutiveCount/binDivisionStep;
             if (binDivisionResult>0){
                 binDivisionResult = (consecutiveCount%binDivisionStep)!=0?(consecutiveCount/binDivisionStep)+consecutiveCount%binDivisionStep:consecutiveCount/binDivisionStep;
                 if (worstCaseMap.containsKey(new Integer(binHeadStep))){
                    int temp = worstCaseMap.get(binHeadStep);
                    worstCaseMap.put(new Integer(binHeadStep), new Integer(temp+(int)binDivisionResult));
                }else{
                    worstCaseMap.put(new Integer(binHeadStep), new Integer((int)binDivisionResult));
                }
                 binHeadStep +=2;
                 binDivisionStep++;
                         
             }else{
                 break;
             }
                 
         }
         
     }
     
     
     public int jumpSize(Jump focusedJump, int allowedEpsilon){
//         double verticalEpsilon = (allowedEpsilon * Math.sin(Math.toRadians(60.0))); // this is epsilon used for vetical jump as that is from the middle of side
//        
//        double totalHorizontalDistance;
//        if (focusedJump.getXjump() == 1){
//            totalHorizontalDistance = allowedEpsilon;
//        }else{
//            totalHorizontalDistance = (((int)focusedJump.getXjump()/2)*(2*allowedEpsilon))+(((int)focusedJump.getXjump()/2)*allowedEpsilon);
//        }
//        //System.out.println("Total distance added: "+totalHorizontalDistance);
//        
//        if (focusedJump.getXjump()%2 != 0)
//            totalHorizontalDistance += allowedEpsilon/2;
//            
//        //double totalVerticalDistance;
//        //we use verticalcode-1 because of the fact that 
//        double totalVerticalDistance = focusedJump.getXjump()%2 == 0? (focusedJump.getYjump())*(2*focusedJump.getYjump()):((focusedJump.getYjump())*(2*verticalEpsilon))+verticalEpsilon;
//        
//        return (int)Math.sqrt((totalHorizontalDistance*totalHorizontalDistance)+(totalVerticalDistance*totalVerticalDistance));
         return focusedJump.getXjump()+focusedJump.getYjump();
     }
     
     public Jump getEpsilonMultipleForRecompression(Jump firstJump){
        int firstHorizontalMultiple = firstJump.xJump%2 == 0?((int)firstJump.xJump/2)*(2)+(int)firstJump.xJump/2:((int)firstJump.xJump/2)*(2)+(int)firstJump.xJump/2+1;
        int firstVerticalMultiple = firstJump.xJump%2 == 0? (firstJump.yJump)*(2):((firstJump.yJump)*(2))-1;
        return new Jump(firstHorizontalMultiple,firstVerticalMultiple,firstJump.getQuadrant());
     }
     /*
      * This function merges two jumps
      */
     public Jump mergeJumps(Jump firstJump, Jump secondJump){
         Jump resultantJump = new Jump();
         
         int resultantQuadrant = 0;
        int firstHorizontalMultiple = firstJump.xJump%2 == 0?((int)firstJump.xJump/2)*(2)+(int)firstJump.xJump/2:((int)firstJump.xJump/2)*(2)+(int)firstJump.xJump/2+1;
        int firstVerticalMultiple = firstJump.xJump%2 == 0? (firstJump.yJump)*(2):((firstJump.yJump)*(2))-1;
        int secondHorizontalMultiple = secondJump.xJump%2 == 0?((int)secondJump.xJump/2)*(2)+(int)secondJump.xJump/2:((int)secondJump.xJump/2)*(2)+(int)secondJump.xJump/2+1;
        int secondVerticalMultiple = secondJump.xJump%2 == 0? (secondJump.yJump)*(2):((secondJump.yJump)*(2))-1;
        int resultantHorizontalMultiple = 0;
        if(addOrSubtractHorizontal(firstJump.quadrant,secondJump.quadrant)){
            resultantHorizontalMultiple = firstHorizontalMultiple+secondHorizontalMultiple;
          }else{
            if(firstHorizontalMultiple>secondHorizontalMultiple){
                resultantHorizontalMultiple = firstHorizontalMultiple-secondHorizontalMultiple;
            }else{
                resultantHorizontalMultiple = secondHorizontalMultiple-firstHorizontalMultiple;
            }
        }
        int resultantVerticalMultiple;
        if (addOrSubtractVertical(firstJump.quadrant,secondJump.quadrant)){
            resultantVerticalMultiple = firstVerticalMultiple+secondVerticalMultiple;
        }else{
            if (firstVerticalMultiple>secondVerticalMultiple){
                resultantVerticalMultiple = firstVerticalMultiple-secondVerticalMultiple;
            }else{
                resultantVerticalMultiple = secondVerticalMultiple-firstVerticalMultiple;
            }
        }
        
        if(firstJump.quadrant==1&&secondJump.quadrant==1){
            resultantQuadrant = 1;
        }else if(firstJump.quadrant==2&&secondJump.quadrant==2){
            resultantQuadrant = 2;
        }else if(firstJump.quadrant==3&&secondJump.quadrant==3){
            resultantQuadrant = 3;
        }else if(firstJump.quadrant==4&&secondJump.quadrant==4){
            resultantQuadrant = 4;
        }else if(firstJump.quadrant==1&&secondJump.quadrant==2){
            if (firstHorizontalMultiple > secondHorizontalMultiple){
                resultantQuadrant = 1;
            }else{
                resultantQuadrant = 2;
            }
        }else if(firstJump.quadrant==1&&secondJump.quadrant==3){
            if (firstHorizontalMultiple > secondHorizontalMultiple){
                    resultantQuadrant = 1;
                
            } else{
                    resultantQuadrant = 3;
                
            }
        }else if(firstJump.quadrant==1&&secondJump.quadrant==4){
            if (firstVerticalMultiple>secondVerticalMultiple){
                resultantQuadrant = 1;
            }else{
                resultantQuadrant = 4;
            }

        }else if(firstJump.quadrant==2&&secondJump.quadrant==1){
            if (firstHorizontalMultiple > secondHorizontalMultiple){
                resultantQuadrant = 2;
        }else {
                resultantQuadrant = 1;
            }   
        }else if(firstJump.quadrant==2&&secondJump.quadrant==3){
            if (firstVerticalMultiple > secondVerticalMultiple){
                    resultantQuadrant = 2;
            }else{
                    resultantQuadrant = 3;
                
            }
        }else if(firstJump.quadrant==2&&secondJump.quadrant==4){
            if (firstHorizontalMultiple > secondHorizontalMultiple){
                    resultantQuadrant = 2;
            }else{
                    resultantQuadrant = 4;
                
            }
        }else if(firstJump.quadrant==3&&secondJump.quadrant==1){
            if (firstHorizontalMultiple > secondHorizontalMultiple){
                    resultantQuadrant = 3;
                
            }else{   
                resultantQuadrant = 1;
                
            }
        }else if(firstJump.quadrant==3&&secondJump.quadrant==2){
            if (firstVerticalMultiple>secondVerticalMultiple){
                resultantQuadrant = 3;
            }else{
                resultantQuadrant = 2;
            }

        }else if(firstJump.quadrant==3&&secondJump.quadrant==4){
            if (firstHorizontalMultiple>secondHorizontalMultiple){
                resultantQuadrant = 3;
            }else{
                resultantQuadrant = 4;
            }

        }else if(firstJump.quadrant==4&&secondJump.quadrant==1){
            if (firstVerticalMultiple>secondVerticalMultiple){
                resultantQuadrant = 4;
            }else{
                resultantQuadrant = 1;
            }

        }else if(firstJump.quadrant==4&&secondJump.quadrant==2){
            if (firstHorizontalMultiple > secondHorizontalMultiple){
                    resultantQuadrant = 4;
                
            }else{
                    resultantQuadrant = 2;
                
            }
        }else if(firstJump.quadrant==4&&secondJump.quadrant==3){
            if (firstHorizontalMultiple>secondHorizontalMultiple){
                resultantQuadrant = 4;
            }else{
                resultantQuadrant = 3;
            }

        }int xJump = calculateHorizontalJumpRecompression(resultantHorizontalMultiple);
        resultantJump.setXjump(xJump);
        resultantJump.setYjump(calculateVerticalJumpRecompression(xJump,resultantVerticalMultiple));
        resultantJump.setQuadrant(resultantQuadrant);
        return resultantJump;
     }
     
     public Jump chaseToJump(int chase){
         return new Jump(1,1,returnQuadrantCode(HexaGon.getAngleFromSide(chase)));
     }
     
     public Jump convertSimpleJump(Jump simpleJump, int epsilonTimes)
     {
         Jump resultantJump = new Jump();
         int newHorizontalHexagons = simpleJump.getXjump()/epsilonTimes;
         if(simpleJump.getXjump()%2 != 0){
            if (newHorizontalHexagons%2 !=0){
                resultantJump.setXjump(newHorizontalHexagons+1);
            }else{
                resultantJump.setXjump(newHorizontalHexagons);
            }
         }else{
             resultantJump.setXjump(newHorizontalHexagons);
         }
         int newVerticalHexagon = simpleJump.getYjump()/epsilonTimes;
         if (resultantJump.getXjump()%2 != 0){
             if (simpleJump.getYjump()%2 != 0){
                 resultantJump.setYjump(newVerticalHexagon+1);
             }else{
                 resultantJump.setYjump(newVerticalHexagon);
             }
         }else{
             if(simpleJump.getXjump()!= 1){
                 resultantJump.setYjump(newVerticalHexagon);
             }else{
                 resultantJump.setYjump(0);
                 
             }
         }
         
         //simpleJump.setYjump(newVerticalHexagon);
         resultantJump.setQuadrant(simpleJump.getQuadrant());
         return resultantJump;
         
     } 
     
     public int chaseForJump(Jump resultantJump){
         if((resultantJump.getXjump()==1)&&(resultantJump.getYjump()==0)){
             if (resultantJump.getQuadrant() == 1 || resultantJump.getQuadrant() == 4){
                 return 1;
             }else{
                 return 3;
             }
         }else if ((resultantJump.getXjump()==0)&&(resultantJump.getYjump()==1)){
             if (resultantJump.getQuadrant() == 1 || resultantJump.getQuadrant() == 2){
                 return 2;
             }else{
                 return 5;
             }
         }else if ((resultantJump.getXjump()==1)&&(resultantJump.getYjump()==1)){
             if (resultantJump.getQuadrant() == 1){
                 return 1;
             }else if (resultantJump.getQuadrant() == 2){
                 return 3;
             }else if (resultantJump.getQuadrant() == 3){
                 return 4;
             }else if (resultantJump.getQuadrant() == 4){
                 return 6;
             }
         }
         return 0;
     }
     
     public ArrayList<Integer> convertToDesierdLimit(int trajectorySize, int allowableBytes, int oldEpsilon, ArrayList<Integer> approximatedPoints){
        
         while(trajectorySize>allowableBytes){
             approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
             oldEpsilon=2*oldEpsilon;
             trajectorySize = approximatedPoints.remove(approximatedPoints.size()-1);
         }
         approximatedPoints.add(trajectorySize);
         approximatedPoints.add(oldEpsilon);
         return approximatedPoints;
//         if (trajectorySize < 2*allowableBytes ){
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints.add(2*oldEpsilon);
//            return approximatedPoints;
//        }else if(trajectorySize < 4*allowableBytes ){
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(4*oldEpsilon, 2*oldEpsilon, approximatedPoints);
//            approximatedPoints.add(4*oldEpsilon);
//            return approximatedPoints;
//        }else if(trajectorySize < 8*allowableBytes ){
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(4*oldEpsilon, 2*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(8*oldEpsilon, 4*oldEpsilon, approximatedPoints);
//            approximatedPoints.add(8*oldEpsilon);
//            return approximatedPoints;
//        }else if(trajectorySize < 16*allowableBytes ){
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(4*oldEpsilon, 2*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(8*oldEpsilon, 4*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(16*oldEpsilon, 8*oldEpsilon, approximatedPoints);
//            approximatedPoints.add(16*oldEpsilon);
//            return approximatedPoints;
//        }else if(trajectorySize < 32*allowableBytes ){
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(4*oldEpsilon, 2*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(8*oldEpsilon, 4*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(16*oldEpsilon, 8*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(32*oldEpsilon, 16*oldEpsilon, approximatedPoints);
//            approximatedPoints.add(32*oldEpsilon);
//            return approximatedPoints;
//        }else if(trajectorySize < 64*allowableBytes ){
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(4*oldEpsilon, 2*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(8*oldEpsilon, 4*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(16*oldEpsilon, 8*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(32*oldEpsilon, 16*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(64*oldEpsilon, 32*oldEpsilon, approximatedPoints);
//            approximatedPoints.add(64*oldEpsilon);
//            return approximatedPoints;
//        }else{
//            approximatedPoints = convertToDesiredEpsilon(2*oldEpsilon, oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(4*oldEpsilon, 2*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(8*oldEpsilon, 4*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(16*oldEpsilon, 8*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(32*oldEpsilon, 16*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(64*oldEpsilon, 32*oldEpsilon, approximatedPoints);
//            approximatedPoints = convertToDesiredEpsilon(128*oldEpsilon, 64*oldEpsilon, approximatedPoints);
//            approximatedPoints.add(128*oldEpsilon);
//            return approximatedPoints;
//        }
        //return null;    
     }
     
     /*
      * This function does the re-compression with new chase increasing recompression
      */
     public ArrayList<Integer> convertToDesiredEpsilon(int newEpsilon, int oldEpsilon, ArrayList<Integer> approximatedPoints){
         Jump nearestJump = null;
         boolean jumpFromCentre = true;
         boolean chaseFromCentre = true;
         int reverseChaseCode=0;
         int reCompressionSize = 0;
         int nextMinJump = 999999999;
         ArrayList<Integer> mergedArrayList = new ArrayList<Integer>();
         for (int i = 0; i < approximatedPoints.size(); i++){
             int curentValue = approximatedPoints.get(i);
             if (curentValue>=1 && curentValue < 7 ){
                 if (chaseFromCentre){
                     reverseChaseCode = (curentValue+3)%6;
                     reverseChaseCode = reverseChaseCode+6;
                     chaseFromCentre = false;
                     jumpFromCentre = false;
                 }else{
                     
                     if(curentValue == (reverseChaseCode%6==0?6:reverseChaseCode%6)){
                         chaseFromCentre = true;
                         jumpFromCentre = true;
                     }else if(curentValue == ((reverseChaseCode+1)%6==0?6:(reverseChaseCode+1)%6)){
                         reverseChaseCode =((reverseChaseCode-1)%6==0?6:(reverseChaseCode-1)%6);
                         chaseFromCentre = false;
                         jumpFromCentre = false;
                     }else if(curentValue == ((reverseChaseCode-1)%6==0?6:(reverseChaseCode-1)%6)){
                         reverseChaseCode =(reverseChaseCode+1)%6==0?6:(reverseChaseCode+1)%6;
                         chaseFromCentre = false;
                         jumpFromCentre = false;
                     }else if(curentValue == ((reverseChaseCode+2)%6==0?6:(reverseChaseCode+2)%6)){
                         mergedArrayList.add(((reverseChaseCode+3)%6==0?6:(reverseChaseCode+3)%6));
                         reverseChaseCode = ((reverseChaseCode-2)%6==0?6:(reverseChaseCode-2)%6);
                         chaseFromCentre = false;
                         jumpFromCentre = false;
                         reCompressionSize +=3;
                     }else if(curentValue == ((reverseChaseCode-2)%6==0?6:(reverseChaseCode-2)%6)){
                         mergedArrayList.add(((reverseChaseCode+3)%6==0?6:(reverseChaseCode+3)%6));
                         reverseChaseCode = ((reverseChaseCode+2)%6==0?6:(reverseChaseCode+2)%6);
                         chaseFromCentre = false;
                         jumpFromCentre = false;
                         reCompressionSize +=3;
                     }else if(curentValue == ((reverseChaseCode+3)%6==0?6:(reverseChaseCode+3)%6)){
                         mergedArrayList.add(((reverseChaseCode+3)%6==0?6:(reverseChaseCode+3)%6));
                         chaseFromCentre = true;
                         jumpFromCentre = true;
                         reCompressionSize +=3;
                     }
                 }
                 
             }else if(curentValue == Constants.START_FINISH_OF_STEP_COUNT){
                 Jump newJump = new Jump((int)approximatedPoints.get(i+2), (int)approximatedPoints.get(i+3),(int)approximatedPoints.get(i+1));
                 i+=3;
                 if(jumpFromCentre == true){// This is the case where it is a simple jump conversion of one size to another
                     //Jump epsilonMultipleJump = getEpsilonMultipleForRecompression(newJump);
                     Jump resultantJump = convertSimpleJump(newJump, newEpsilon/oldEpsilon);
                     int chaseJump = chaseForJump(resultantJump);
                     if(chaseJump!=0){
                         mergedArrayList.add(chaseJump);
                         reCompressionSize +=3;
                     }else{
                         mergedArrayList.add(Constants.START_FINISH_OF_STEP_COUNT);
                         mergedArrayList.add(resultantJump.getQuadrant());
                         mergedArrayList.add(resultantJump.getXjump());
                         mergedArrayList.add(resultantJump.getYjump());
                         reCompressionSize +=calcualteBitsForCodingJumpEmpty(resultantJump.getXjump());
                         reCompressionSize +=calcualteBitsForCodingJumpEmpty(resultantJump.getYjump());
                     }
                     
                 }else{
                     if (reverseChaseCode==6){
                         if (newJump.getQuadrant() == 1){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 2){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 3){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 4){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }
                         
                     }else if(reverseChaseCode==5){
                         if (newJump.getQuadrant() == 1){
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 2){
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 3){
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 4){
                            newJump.setYjump(newJump.getYjump()-1);
                         }
                     }else if(reverseChaseCode==4){
                         if (newJump.getQuadrant() == 1){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 2){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 3){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 4){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }
                     }else if(reverseChaseCode==3){
                         if (newJump.getQuadrant() == 1){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 2){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 3){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 4){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }
                     }else if(reverseChaseCode==2){
                         if (newJump.getQuadrant() == 1){
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 2){
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 3){
                            newJump.setYjump(newJump.getYjump()+1);
                         }else if (newJump.getQuadrant() == 4){
                            newJump.setYjump(newJump.getYjump()+1);
                         }
                     }else if(reverseChaseCode==1){
                         if (newJump.getQuadrant() == 1){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 2){
                            newJump.setXjump(newJump.getXjump()+1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 3){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()-1);
                         }else if (newJump.getQuadrant() == 4){
                            newJump.setXjump(newJump.getXjump()-1);
                            newJump.setYjump(newJump.getYjump()+1);
                         }
                     }
//                    newJump.setXjump(newJump.getXjump()+1);
//                    newJump.setYjump(newJump.getYjump()+1);
                    //Jump epsilonMultipleJump = getEpsilonMultipleForRecompression(newJump);
                    Jump resultantJump = convertSimpleJump(newJump, newEpsilon/oldEpsilon);
                    int chaseJump = chaseForJump(resultantJump);
                    if(chaseJump!=0){
                        mergedArrayList.add(chaseJump);
                        reCompressionSize +=3;
                    }else{
                        mergedArrayList.add(Constants.START_FINISH_OF_STEP_COUNT);
                        mergedArrayList.add(resultantJump.getQuadrant());
                        mergedArrayList.add(resultantJump.getXjump());
                        mergedArrayList.add(resultantJump.getYjump());
                        reCompressionSize +=calcualteBitsForCodingJumpEmpty(resultantJump.getXjump());
                        reCompressionSize +=calcualteBitsForCodingJumpEmpty(resultantJump.getYjump());
                    }
                 }
                 jumpFromCentre = true;
                 chaseFromCentre = true;
             }
         }
         mergedArrayList.add(reCompressionSize);
         return mergedArrayList;
     }
     
     public ArrayList<Integer> dataApproximatedToLimits(int allowableBytes, int minJumpSize, ArrayList<Integer> approximatedPoints, int totalSize, int allowedEpsilon){
         Pair<ArrayList<Integer>, Integer> approximatedPointsChases = dataApproximationChases( allowableBytes, minJumpSize, approximatedPoints, totalSize, allowedEpsilon);
         approximatedPoints = approximatedPointsChases.getFirst();
         minJumpSize = approximatedPointsChases.getSecond();
         int recompressionSize = approximatedPoints.get(approximatedPoints.size()-1);
         //int recompressionSize = totalSize;
         //approximatedPoints.remove(approximatedPoints.size()-1);
         if (recompressionSize>allowableBytes*8){
             while (recompressionSize>allowableBytes*8){
                 approximatedPoints.remove(approximatedPoints.size()-1);
                 Pair<ArrayList<Integer>, Integer> approximatedPointsJumps = dataApproximationJumps( allowableBytes, minJumpSize, approximatedPoints, recompressionSize, allowedEpsilon);
                 approximatedPoints = approximatedPointsJumps.getFirst();
                 minJumpSize = approximatedPointsJumps.getSecond();
                 recompressionSize = approximatedPoints.get(approximatedPoints.size()-1);
             }
         }else{
             return approximatedPoints;
         }
         return approximatedPoints;
     }
     
     /*
      * Energy based epsilon calculation for jumps
      * 
      */
     
     public Pair<ArrayList<Integer>, Integer> dataApproximationChases(int allowableBytes, int minJumpSize, ArrayList<Integer> approximatedPoints, int totalSize, int allowedEpsilon){
         Jump nearestJump = null;
         int reCompressionSize = 0;
         int nextMinJump = 999999999;
         ArrayList<Integer> mergedArrayList = new ArrayList<Integer>();
         for (int i = 0; i < approximatedPoints.size(); i++){
             if (approximatedPoints.get(i)>=1 && approximatedPoints.get(i) < 7 ){
                 totalSize -= 3;
                 Jump jumpForChase = chaseToJump(approximatedPoints.get(i));
                 if (nearestJump!=null){
                     nearestJump = mergeJumps(nearestJump,jumpForChase);
                     
                 }else{
                     nearestJump = jumpForChase;
                 }
                 if (totalSize+reCompressionSize<allowableBytes*8){
                     mergedArrayList.add(Constants.START_FINISH_OF_STEP_COUNT);
                     mergedArrayList.add(nearestJump.getQuadrant());
                     mergedArrayList.add(nearestJump.getXjump());
                     mergedArrayList.add(nearestJump.getYjump());
                     mergedArrayList.addAll(approximatedPoints.subList(i+1, approximatedPoints.size()-1));
                     break;
                 }
             }else if(approximatedPoints.get(i) == Constants.START_FINISH_OF_STEP_COUNT){
                 if (nearestJump!=null){
                     mergedArrayList.add(Constants.START_FINISH_OF_STEP_COUNT);
                     mergedArrayList.add(nearestJump.getQuadrant());
                     mergedArrayList.add(nearestJump.getXjump());
                     mergedArrayList.add(nearestJump.getYjump());
                     reCompressionSize +=calcualteBitsForCodingJumpEmpty(nearestJump.getXjump());
                     reCompressionSize +=calcualteBitsForCodingJumpEmpty(nearestJump.getYjump());
                     int jumpSize = jumpSize(nearestJump, allowedEpsilon);
//                     if (jumpSize == 3)
//                         System.out.println("here");
                     if (nextMinJump > jumpSize){
                        nextMinJump = nearestJump.getXjump()+nearestJump.getYjump();
                     }
                 }
                 
                 nearestJump = new Jump((int)approximatedPoints.get(i+2), (int)approximatedPoints.get(i+3),(int)approximatedPoints.get(i+1));
                 totalSize -= calcualteBitsForCodingJumpEmpty((int)approximatedPoints.get(i+2));
                 totalSize -= calcualteBitsForCodingJumpEmpty((int)approximatedPoints.get(i+3));
                 i+=3;
                 
                 
             }
         }
         mergedArrayList.add(reCompressionSize);
         return new Pair<ArrayList<Integer>, Integer>(mergedArrayList, nextMinJump);
     }
     
     /*
      * Energy based epsilon calculation for chases
      * 
      */
     
     public Pair<ArrayList<Integer>, Integer> dataApproximationJumps(int allowableBytes, int minJumpSize, ArrayList<Integer> approximatedPoints, int totalSize, int allowedEpsilon){
         //Jump nearestJump = null;
         int reCompressionSize = 0;
         int nextMinJump = 999999999;
         Jump currentJump= null;
         boolean firstCheck = true;
         
         ArrayList<Integer> mergedArrayList = new ArrayList<Integer>();
         
         
         for (int i = 0; i < approximatedPoints.size()-3; i++){
             if(approximatedPoints.get(i) == Constants.START_FINISH_OF_STEP_COUNT){
                 if (firstCheck == true){
                    currentJump = new Jump(approximatedPoints.get(i+2),approximatedPoints.get(i+3), approximatedPoints.get(i+1));
                    firstCheck = false;
                    i+=3;
                 }else{
                    Jump nearestJump = new Jump(approximatedPoints.get(i+2),approximatedPoints.get(i+3), approximatedPoints.get(i+1));
                    int jumpSize = jumpSize(currentJump, allowedEpsilon);
                    int nearestJumpSize = jumpSize(nearestJump, allowedEpsilon);
//                    if(i==48)
//                       System.out.println("here");
                    if((jumpSize == minJumpSize) || (nearestJumpSize == minJumpSize)){
                        
                        if(mergeJumps(nearestJump,currentJump).getQuadrant()==0)
                            System.out.println("here");
                        
                        currentJump = mergeJumps(nearestJump,currentJump);
                        
                        i+=3;
                        if (totalSize<allowableBytes*8){
                            mergedArrayList.add(Constants.START_FINISH_OF_STEP_COUNT);
                            mergedArrayList.add(currentJump.getQuadrant());
                            mergedArrayList.add(currentJump.getXjump());
                            mergedArrayList.add(currentJump.getYjump());
                            reCompressionSize +=calcualteBitsForCodingJumpEmpty(currentJump.getXjump());
                            reCompressionSize +=calcualteBitsForCodingJumpEmpty(currentJump.getYjump());
                            if (approximatedPoints.size()-1 > i+1){
                               mergedArrayList.addAll(approximatedPoints.subList(i+1, approximatedPoints.size()));
                               reCompressionSize += returnSizeOfList(new ArrayList<Integer>(approximatedPoints.subList(i+1, approximatedPoints.size())));
                            }
                            break;
                        }
                    }else{
                        mergedArrayList.add(Constants.START_FINISH_OF_STEP_COUNT);
                        mergedArrayList.add(currentJump.getQuadrant());
                        mergedArrayList.add(currentJump.getXjump());
                        mergedArrayList.add(currentJump.getYjump());
                        reCompressionSize +=calcualteBitsForCodingJumpEmpty(currentJump.getXjump());
                        reCompressionSize +=calcualteBitsForCodingJumpEmpty(currentJump.getYjump());
                        if (nextMinJump > jumpSize(currentJump, allowedEpsilon)){
                           nextMinJump = jumpSize(currentJump, allowedEpsilon);
                        }
                        currentJump  = nearestJump;

                        //nearestJump = new Jump((int)approximatedPoints.get(i+2), (int)approximatedPoints.get(i+3),(int)approximatedPoints.get(i+1));

                        i+=3;
                    }
                 }
             }
             
         }
         mergedArrayList.add(reCompressionSize);
         return new Pair<ArrayList<Integer>, Integer>(mergedArrayList, nextMinJump);
     }
     
     public int returnSizeOfList(ArrayList<Integer> approximateList){
         int size = 0;
         for (int i = 0; i < approximateList.size(); i++){
             size +=calcualteBitsForCodingJumpEmpty(approximateList.get(i+2));
             size +=calcualteBitsForCodingJumpEmpty(approximateList.get(i+3));
             i+=3;
         }
         return size;
     }
     
     /*
      * Energy based calculation based on B_n
      */
     
     public int epsilonForData(Pair<HashMap<Integer, Integer>, Pair<ArrayList<Double>, HashMap<Integer, Integer>>> returningBinsWithPointsPair, int allowableBytes){
         int totalAllowedPoints = (allowableBytes*8)/4;
         //System.out.println("total number of allowed points: "+totalAllowedPoints);
         if (returningBinsWithPointsPair.getSecond().getFirst().size()<= totalAllowedPoints){
             return 1;
         }else{
            HashMap<Integer, Integer> binsForAccurateCounting = returningBinsWithPointsPair.getFirst();
            HashMap<Integer, Integer> binsForEmptyCounting= returningBinsWithPointsPair.getSecond().getSecond();
            ArrayList<Integer> allKeys = new ArrayList<Integer>();
            allKeys.addAll(binsForAccurateCounting.keySet());
            Collections.sort(allKeys);
            int epsilonMultiple = 0;
            int i = 0;
            int step = 0;
            while (true){
                int pointForThisMultiple = 0;
                if (i < allKeys.size())
                    pointForThisMultiple = binsForAccurateCounting.get(allKeys.get(i));
                int emptyCount = 0;
                ArrayList<Integer> allEmptyBinKeys = new ArrayList<Integer>();
                allEmptyBinKeys.addAll(binsForEmptyCounting.keySet());
                Collections.sort(allEmptyBinKeys);
                for (int j = 0; j< allEmptyBinKeys.size(); j++){
                    if (i < allKeys.size()){
                       if (allEmptyBinKeys.get(j)>allKeys.get(i))
                          emptyCount += binsForEmptyCounting.get(allEmptyBinKeys.get(j));
                    }else{
                        if (allEmptyBinKeys.get(j)>step)
                          emptyCount += binsForEmptyCounting.get(allEmptyBinKeys.get(j));
                    }
                }
                if ((pointForThisMultiple+emptyCount)<= totalAllowedPoints){
                    if (i<(allKeys.size()-1))
                       epsilonMultiple = allKeys.get(i);
                    else
                        epsilonMultiple = step;
                    break;
                }
                i++;
                step += 2;
            }
            return epsilonMultiple;
         }
     }
     /*
      * Enegy based epsilon calculation for B_n for DP
      */
     public Pair<ArrayList<GPSPoint>, Integer> allowedEpsilonAndPointsDouglas(ArrayList<GPSPoint> source, int allowedBytes, int allowedEpsilon){
         int totalAllowedPoints = (allowedBytes*8)/(12*8);
         //System.out.println("total number of allowed points douglas: "+totalAllowedPoints);
         int i = 1;
         ArrayList<GPSPoint> approximatedPoints;
         while (true){
             approximatedPoints = GDouglasPeuker.douglasPeucker (source,allowedEpsilon*i);
             if (approximatedPoints.size()<=totalAllowedPoints)
                 break;
             i++;
         }
         return new Pair<ArrayList<GPSPoint>, Integer>(approximatedPoints, allowedEpsilon*i);
     }
     
     
    /*
     * This funcrtion implements fixed sized based compression technique.
     */
    
    public ArrayList<Integer> performFixedBinBasedCompression(ArrayList<GPSPoint> source, ArrayList<GPSPoint> gpsPointsForCalibration, int epsilon, String distanceType, ArrayList<GPSPoint> whileConstructing, String kGonType, ArrayList<Date> allDateTimeValues) {
        ArrayList<Integer> resultantPoints = new ArrayList<Integer>();
        GPSPoint currentCentre = new GPSPoint();
        Date currentTime = new Date();
        int holdsSizeCode=0;
        float timeDifference;
        int timeCode=0;
        boolean checkCodeChange = false;
        int timeDifferenceInInt;
        
        for (int i = 0; i < source.size(); i++) {//source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                resultantPoints.add(new Integer(Constants.CODE_FOR_CALIBRATION_POINT));
                gpsPointsForCalibration.add(source.get(i));
                currentCentre = source.get(i);
                currentTime = allDateTimeValues.get(i);
                //resultantPoints.add(source.get(i));
            } else {//in this case either approximation on current centre is going to happen or new centre calculation
                Pair<Integer, GPSPoint> returningPair;
                timeDifference =  (allDateTimeValues.get(i).getTime() - currentTime.getTime())/1000;
                float distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                
                if (distance < getSideLengthToUse(Constants.LIMIT_FOR_FIRST_STEP, angle, distanceType)){
                    if(holdsSizeCode != Constants.CODE_FOR_FIRST_SIZE){
                        if (checkCodeChange)
                            resultantPoints.add(new Integer(holdsSizeCode));
                        holdsSizeCode = Constants.CODE_FOR_FIRST_SIZE;
                        resultantPoints.add(new Integer(holdsSizeCode));
                        resultantPoints.add(new Integer(Constants.SAME_KGON));
                        checkCodeChange = true;
                        
                    }else {
                        resultantPoints.add(new Integer(Constants.SAME_KGON));
                        //resultantPoints.add(new Integer(0));
                        
                    }
                } else if ((distance > getSideLengthToUse(Constants.LIMIT_FOR_FIRST_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_FIRST_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_FIRST_STEP, angle, distanceType)))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_FIRST_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_FIRST_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_SECOND_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_SECOND_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_SECOND_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_SECOND_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_SECOND_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_THIRD_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_THIRD_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_THIRD_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_THIRD_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_THIRD_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_FOURTH_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_FOURTH_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_FOURTH_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_FOURTH_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_FOURTH_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_FIFTH_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_FIFTH_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_FIFTH_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_FIFTH_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_FIFTH_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_SIXTH_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_SIXTH_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_SIXTH_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_SIXTH_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_SIXTH_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_SEVENTH_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_SEVENTH_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_SEVENTH_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_SEVENTH_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_SEVENTH_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else if (distance > getSideLengthToUse(Constants.LIMIT_FOR_EIGHTH_STEP, angle, distanceType) && distance < ((2*getSideLengthToUse(Constants.LIMIT_FOR_EIGHTH_STEP, angle, distanceType))+getSideLengthToUse(Constants.LIMIT_FOR_EIGHTH_STEP, angle, distanceType))) {
                    if (checkCodeChange){
                        returningPair = handleCodeBasedSteps(resultantPoints,source.get(i), currentCentre, getSideLengthToUse(Constants.LIMIT_FOR_EIGHTH_STEP, angle, distanceType),distanceType,kGonType, holdsSizeCode, Constants.CODE_FOR_EIGHTH_SIZE);
                        holdsSizeCode = returningPair.getFirst();
                        currentCentre = returningPair.getSecond();
                    }
                    checkCodeChange = true;
                } else {// This is the case where we record the GPS point itself
                    resultantPoints.add(new Integer(Constants.CODE_FOR_CALIBRATION_POINT));
                    gpsPointsForCalibration.add(source.get(i));
                    currentCentre = source.get(i);
                }
                System.out.println("current epsilon is: " + epsilon);
                System.out.println("Distance is: " + distance);
                System.out.println("Current Centre: " + currentCentre.getLatitude() + "," + currentCentre.getLongitude());
                timeDifferenceInInt = Utility.roundedNumber((int)timeDifference);
                
                //resultantPoints.add(new Integer(getTimeCode(timeDifference)));
                currentTime = allDateTimeValues.get(i);
                //resultantPoints.add();
            }
            
            whileConstructing.add(currentCentre);
        }
        return resultantPoints;
    }

    /* 
     * This function implements the basic GridCompression technique. 
     * 
     */
    public ArrayList<Integer> performGridCompression(ArrayList<GPSPoint> source, int epsilon, String distanceType, String kGonType) {
        ArrayList<Integer> resultantPoints = new ArrayList<Integer>();
        GPSPoint currentCentre = new GPSPoint();
        GPSPoint firstPoint;
        GPSPoint firstPointForCalibration = null;

        for (int i = 0; i < source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                currentCentre = source.get(i);
                firstPoint = source.get(i);
                firstPointForCalibration = new GPSPoint(firstPoint.getLongitude(), firstPoint.getLatitude());
            } else {//in this case either approximation on current centre is going to happen or new centre calculation

                float distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                

                if (distance > getSideLengthToUse(epsilon, angle, distanceType)) {
                    GPSPoint tempCurrent = currentCentre;
                    currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                    //System.out.println(GeoHelper.getDistance(tempCurrent, currentCentre));
                    addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection
                    
                }
            }
        }

        return resultantPoints;
    }
    
    
}
