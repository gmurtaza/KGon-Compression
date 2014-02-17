package Decoder;


import Compression.HexaGon;
import Compression.KGonCompression;
import java.util.ArrayList;
import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
import Constants.Constants;
import java.util.Date;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class KGonCompressionDecoder {
    
    
    public ArrayList<GPSPoint> getConvertedGPSPointList(GPSPoint firstPosition, ArrayList<Integer> positionData, float epsilon, String distanceType){
    
        ArrayList<GPSPoint> convertedListOfGPSPoints = new ArrayList<GPSPoint>();
        GPSPoint constructedPoint = null;
        convertedListOfGPSPoints.add(firstPosition);
        for (int i = 0; i< positionData.size(); i++){

          constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), firstPosition, distanceType, epsilon);
          convertedListOfGPSPoints.add(constructedPoint);
          firstPosition = constructedPoint;
          
        }
        return convertedListOfGPSPoints;
  }
    
    
    public ArrayList<GPSPoint> getGPSPointListCodedInterpolated(GPSPoint firstPosition, ArrayList<Integer> positionData, double epsilon, String distanceType, long timeEpsilon){
    
        ArrayList<GPSPoint> convertedListOfGPSPoints = new ArrayList<GPSPoint>();
        //GPSPoint constructedPoint = null;
        double epsilonToUse = 0;
        int goForCoded = 0;
        boolean timeCheck = true;
        convertedListOfGPSPoints.add(firstPosition);
        Date currentTimeStamp = new Date(firstPosition.getTimeStamp().getTime());
        GPSPoint constructedPoint = new GPSPoint(firstPosition.getLongitude(), firstPosition.getLatitude(), firstPosition.getTimeStamp());
        for (int i = 0; i< positionData.size()-2; i++){
            if(goForCoded == 0){
                if (positionData.get(i)==Constants.START_FINISH_OF_STEP_COUNT){
                    goForCoded = 3;
                    //constructedPoint = KGonCompression.calculateNewCentreWithMultiples(firstPosition, epsilon, positionData.get(i+2), positionData.get(i+3), positionData.get(i+1));
                    GPSPoint newConstructedPoint = KGonCompression.calculateNewCentreWithHexagonMultiples(firstPosition, epsilon, (int)positionData.get(i+2), (int)positionData.get(i+3), (int)positionData.get(i+1));
                    //epsilonToUse = KGonCompression.getSideLengthToUse((float)epsilon, positionData.get(i+2), distanceType)*positionData.get(i+1);
                    constructedPoint = new GPSPoint(newConstructedPoint.getLongitude(), newConstructedPoint.getLatitude());
                    //constructedPoint.setTimeStamp(currentTimeStamp);
                    //constructedPoint = GeoHelper.getPointWithPolarDistance(firstPosition, (float)epsilonToUse, positionData.get(i+2));
                    convertedListOfGPSPoints.add(constructedPoint);
                    firstPosition = new GPSPoint(constructedPoint.getLongitude(), constructedPoint.getLatitude(), constructedPoint.getTimeStamp());
                    //timeCheck = false;
                }else if (positionData.get(i)>=1 && positionData.get(i) < 7){
                    epsilonToUse = epsilon;
                    constructedPoint = HexaGon.returnPointBasedOnCodeDouble(positionData.get(i), firstPosition, "exact", (float)epsilonToUse);
                    constructedPoint.setTimeStamp(currentTimeStamp);
                    convertedListOfGPSPoints.add(constructedPoint);
                    firstPosition = new GPSPoint(constructedPoint.getLongitude(), constructedPoint.getLatitude(), constructedPoint.getTimeStamp());
                    //timeCheck = false;
                }else if(positionData.get(i)==07){
                    long newDateTime = currentTimeStamp.getTime()+ 2*timeEpsilon;
                    currentTimeStamp = new Date(newDateTime);
                    if (timeCheck){
                        constructedPoint.setTimeStamp(currentTimeStamp);
                        convertedListOfGPSPoints.add(constructedPoint);
                        //constructedPoint = new GPSPoint();
                    }
                    timeCheck = true;
                }else if(positionData.get(i)==70){
                    long timeToSet = currentTimeStamp.getTime()+ positionData.get(i+1)*timeEpsilon;
                    currentTimeStamp = new Date(timeToSet);
                    i+=1;
                    if (timeCheck){
                        constructedPoint.setTimeStamp(currentTimeStamp);
                        convertedListOfGPSPoints.add(constructedPoint);
                        //constructedPoint = new GPSPoint();
                    }
                    timeCheck = true;
                }
                
                
            }else{
                goForCoded--;
            }
        }
        return convertedListOfGPSPoints;
  }
    
    /*
     * This is decoder for only jumped encoding
     */
    public ArrayList<GPSPoint> getGPSPointListCodedJumps(GPSPoint firstPosition, ArrayList<Integer> positionData, double epsilon, String distanceType){
    
        ArrayList<GPSPoint> convertedListOfGPSPoints = new ArrayList<GPSPoint>();
        GPSPoint constructedPoint = null;
        double epsilonToUse = 0;
        int goForCoded = 0;
        convertedListOfGPSPoints.add(firstPosition);
        for (int i = 0; i< positionData.size(); i++){
            if(goForCoded == 0){
                
                goForCoded = 2;
                constructedPoint = KGonCompression.calculateNewCentreWithHexagonMultiples(firstPosition, epsilon, positionData.get(i+1), positionData.get(i+2), positionData.get(i));
   
                convertedListOfGPSPoints.add(constructedPoint);
                firstPosition = constructedPoint;
                
            }else{
                goForCoded--;
            }
        }
        return convertedListOfGPSPoints;
  }

  public ArrayList<GPSPoint> getConvertedGPSPointListForAdaptive(GPSPoint firstPosition, ArrayList<Integer> positionData, float epsilon, String distanceType){
    ArrayList<GPSPoint> convertedListOfGPSPoints = new ArrayList<GPSPoint>();
    GPSPoint constructedPoint = firstPosition;
    int holdsCode = 0;
    boolean sevenChecker = false;
    boolean eightChecker = false;
    convertedListOfGPSPoints.add(firstPosition);
    for (int i = 0; i< positionData.size(); i++){

      if (positionData.get(i) == Constants.DOUBLING_EPSILON_START_OR_FINISH && holdsCode != Constants.DOUBLING_EPSILON_START_OR_FINISH){
        holdsCode = positionData.get(i);
      }else if (positionData.get(i) == Constants.DOUBLING_EPSILON_START_OR_FINISH && holdsCode == Constants.DOUBLING_EPSILON_START_OR_FINISH){
        holdsCode = Constants.SAME_KGON;
        sevenChecker = false;
      }

      if (positionData.get(i) == Constants.HALVING_EPSILON_START_OR_FINISH && holdsCode != Constants.HALVING_EPSILON_START_OR_FINISH){
        holdsCode = positionData.get(i);
      }else if (positionData.get(i) == Constants.HALVING_EPSILON_START_OR_FINISH && holdsCode == Constants.HALVING_EPSILON_START_OR_FINISH){
        holdsCode = Constants.SAME_KGON;
        eightChecker = false;
      }

      if (positionData.get(i) == Constants.CURRENT_EPSILON_START_OR_FINISH && holdsCode != Constants.CURRENT_EPSILON_START_OR_FINISH){
        holdsCode = positionData.get(i);
      }else if (positionData.get(i) == Constants.CURRENT_EPSILON_START_OR_FINISH && holdsCode == Constants.CURRENT_EPSILON_START_OR_FINISH){
        holdsCode = Constants.SAME_KGON;
        sevenChecker = false;
        eightChecker = false;
      }

      if (holdsCode == Constants.DOUBLING_EPSILON_START_OR_FINISH){
        if (sevenChecker){
          epsilon = epsilon*2;  
        }else{
          sevenChecker = true;
        }
        
      }else if (holdsCode == Constants.HALVING_EPSILON_START_OR_FINISH){
        if (eightChecker){
          epsilon = epsilon/2;  
        }else{
          eightChecker = true;
        }
      }

      
      constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), firstPosition, distanceType, epsilon);

      
      if (constructedPoint != null){
        System.out.println("Epsilon is: "+epsilon);
        System.out.println("Current Centre: "+constructedPoint.getLatitude() + ","+constructedPoint.getLongitude());
        convertedListOfGPSPoints.add(constructedPoint);
        firstPosition = constructedPoint;
      }
    }
    return convertedListOfGPSPoints;
  }

  
  public ArrayList<GPSPoint> getConvertedGPSPointsForFixedBinBasedCompression( ArrayList<Integer> positionData, ArrayList<GPSPoint> gpsPointsForCalibration, float epsilon, String distanceType){
    ArrayList<GPSPoint> convertedListOfGPSPoints = new ArrayList<GPSPoint>();
    GPSPoint constructedPoint = new GPSPoint();
    int calibrationPointIndex = 0;
    int holdsCode = 0;
    boolean checkIndex = false;
    for (int i = 0; i< positionData.size(); i++){

      if (positionData.get(i) >= Constants.CODE_FOR_FIRST_SIZE && positionData.get(i) <= Constants.CODE_FOR_EIGHTH_SIZE){
        holdsCode = positionData.get(i);
      }else if (positionData.get(i) == Constants.CODE_FOR_CALIBRATION_POINT){
          constructedPoint = gpsPointsForCalibration.get(calibrationPointIndex);  
          convertedListOfGPSPoints.add(constructedPoint);
          calibrationPointIndex++;
          checkIndex = true;
      }else if (positionData.get(i) >= Constants.CODE_FOR_FIRST_TIME_SIZE && positionData.get(i) <= Constants.CODE_FOR_SIXTH_TIME_SIZE){
        continue;
      }else if (positionData.get(i) == 0){
        continue;
      }else{
          
          switch(holdsCode){
              case Constants.CODE_FOR_FIRST_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_FIRST_STEP);
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.CODE_FOR_SECOND_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_SECOND_STEP); 
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.CODE_FOR_THIRD_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_THIRD_STEP);
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.CODE_FOR_FOURTH_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_FOURTH_STEP);
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.CODE_FOR_FIFTH_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_FIFTH_STEP);
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.CODE_FOR_SIXTH_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_SIXTH_STEP);
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.
                  CODE_FOR_SEVENTH_SIZE:
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_SEVENTH_STEP);
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
              case Constants.CODE_FOR_EIGHTH_SIZE: 
                  constructedPoint = HexaGon.returnPointBasedOnCode(positionData.get(i), constructedPoint, distanceType, Constants.LIMIT_FOR_EIGHTH_STEP);    
                  convertedListOfGPSPoints.add(constructedPoint);
                  break;
          }
      }
    }
    return convertedListOfGPSPoints;
  }
    
}
