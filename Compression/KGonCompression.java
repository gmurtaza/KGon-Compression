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
import java.util.Date;
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
                    currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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
                                currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);

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
                                    currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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
                            currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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
                    currentCentre = calculateNewCentre(tempCurrent, sourcePoint, getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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
            currentCentre = calculateNewCentre(tempCurrent, sourcePoint, getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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
        currentCentre = calculateNewCentre(tempCurrent, sourcePoint, getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);

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
                            currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);

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
                        currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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
                            currentCentre = calculateNewCentre(tempCurrent, source.get(i), getSideLengthToUse(epsilon, angle, distanceType), distanceType, kGonType);
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

    
    public ArrayList<Double> performGridCompressionCodedInterpolation(ArrayList<GPSPoint> source,ArrayList<GPSPoint> whileConstructing, int epsilon, String distanceType, String kGonType, ArrayList<Date> allDateTimeValues, ArrayList<WorstBinCounter> binCounterArray) {
        
        ArrayList<Double> resultantPoints = new ArrayList<Double>();
        GPSPoint currentCentre = new GPSPoint();
        GPSPoint firstPoint;
        GPSPoint firstPointForCalibration = null;
        DecimalFormat df = new DecimalFormat("#.##");
        Date currentTime = new Date();
        int simpleDoubleBenefitCounter = 0;
        float timeDifference;
        for (int i = 0; i < source.size(); i++) {
            //This if condition nominates the first centre point to be first point
            if (i == 0) {
                currentCentre = source.get(i);
                firstPoint = source.get(i);
                firstPointForCalibration = new GPSPoint(firstPoint.getLongitude(), firstPoint.getLatitude());
                currentTime = allDateTimeValues.get(i);
            } else {//in this case either approximation on current centre is going to happen or new centre calculation

                float distance = GeoHelper.getDistance(currentCentre, source.get(i));
                double angle = GeoHelper.getGPSAngle(currentCentre, source.get(i));
                timeDifference =  (allDateTimeValues.get(i).getTime() - currentTime.getTime())/1000;

                if (distance > getSideLengthToUse(epsilon, angle, distanceType)) {
                    GPSPoint tempCurrent = currentCentre;
                    if (distance > (2*getSideLengthToUse(epsilon, angle, distanceType))+getSideLengthToUse(epsilon, angle, distanceType)){
                        int multipleOfEpsilon = (int)(distance/getSideLengthToUse(epsilon, angle, distanceType));
                        resultantPoints.add(new Double(Constants.START_FINISH_OF_STEP_COUNT));
                        resultantPoints.add(new Double(multipleOfEpsilon));
                        resultantPoints.add(new Double((df.format(angle))));
                        currentCentre = GeoHelper.getPointWithPolarDistance(currentCentre, ((int)(distance/getSideLengthToUse(epsilon, angle, distanceType)))*getSideLengthToUse(epsilon, angle, distanceType), angle);
                        //addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);
                    }else{
                        currentCentre = calculateNewCentre(tempCurrent, source.get(i), epsilon, distanceType, kGonType);
                        System.out.println(GeoHelper.getDistance(tempCurrent, currentCentre));
                        addCurrentPointDouble(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection
                        simpleDoubleBenefitCounter++;
                    }
                    
                    //resultantPoints.add(new Double(getTimeCode(timeDifference)));
                    currentTime = allDateTimeValues.get(i);
                }
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
                    System.out.println(GeoHelper.getDistance(tempCurrent, currentCentre));
                    addCurrentPoint(resultantPoints, tempCurrent, currentCentre, kGonType);//This adds the current point to the compressed collection
                    
                }
            }
        }

        return resultantPoints;
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
                ab = (float) (epsilon * Math.sin(Math.toRadians(45.0)));
                if ((Math.abs(theeta) % 30.0) == 0) {
                    return ab;
                } else if (Math.abs(theeta) < 30) {
                    return (float) (ab / Math.sin(Math.toRadians(180 - (90 + Math.abs(theeta)))));
                } else {
                    return (float) (ab / Math.sin(Math.toRadians(180 - (90 + (45 - (Math.abs(theeta) % 30))))));
                }
            }
        } else {
            return epsilon;
        }
    }

    /*
     * Calculate new centre in case difference is bigger than epsilon
     */
    GPSPoint calculateNewCentre(GPSPoint currentCentre, GPSPoint currentPoint, float epsilon, String distanceType, String kGonType) {
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
            return 2 * (float) (epsilon * Math.sin(Math.toRadians(45.0)));
        } else {
            return 2 * epsilon;
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
    void addCurrentPointDouble(ArrayList<Double> resultantPoints, GPSPoint tempCurrent, GPSPoint currentCentre, String kGonType) {
        double angle = GeoHelper.getGPSAngle(tempCurrent, currentCentre);

        if (kGonType.equals("Hexa")) {
            resultantPoints.add((double)HexaGon.transitionCodeForSide(angle));
        } else if (kGonType.equals("Octa")) {
            resultantPoints.add((double)OctaGon.transitionCodeForSide(angle));
        }

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
     
}
