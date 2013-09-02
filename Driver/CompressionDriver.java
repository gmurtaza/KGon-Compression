package Driver;

/**
 * @author Ghulam Murtaza <gmurtaza@cse.unsw.edu.au>
 * @date 13, March 2013
 */

import Compression.GDouglasPeuker;
import Compression.SummarisedData;
import GeoHelper.GeoHelper;
import GeoHelper.GPSPoint;
import Decoder.KGonCompressionDecoder;
import Compression.KGonCompression;
import Helper.IOHelper;
import java.io.*;
import java.util.*;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormatter;
//import org.joda.time.format.DateTimeFormat;

public class CompressionDriver {

    
       
  public GPSPoint getPositionDataForThisTimeStamp( ArrayList<GPSPoint> streamingData, int pointNumber){
      
    return streamingData.get(pointNumber);
  }


 
  
  
  private static void usage() {
    System.err.println("usage: java GridCompression [-e <epsilon> <grid/douglas/both> <exact/partial> <Hexa/Octa>] ");
  }
  
  public static void main(String[] args) throws Exception {
    CompressionDriver gridCompression = new CompressionDriver();
    int allowedEpsilon = 10; //error allowed in meters (default value is 10)
    int totalEnergyForTransfer = 300;
    String distanceType = new String("exact");
    String kGonType = new String("Hexa");
    int thresholdForPointRecord = 2000;
    ArrayList<GPSPoint> allStreamingValues = new ArrayList<GPSPoint>();
    ArrayList<Date> allDateTimeValues = new ArrayList<Date>();
    if (args.length >= 3){
        
  	  if (args[0].equals("-e")){
	      try {
           allowedEpsilon = Integer.parseInt(args[1]);
           /************************ Core compression technique ************************/
            //allStreamingValues = gridCompression.getPositionData("NN_Position.dat", " ");
            allStreamingValues = IOHelper.getPositionData("NN_Position.dat", " ");
            allDateTimeValues = IOHelper.getTimeData( "tag1938_gps.txt", ","); 
            if (args[2].equals("grid")){
              gridCompression.runSimpleGridCase(allStreamingValues, allowedEpsilon);
              //results = gridCompression.performGridCompression(allStreamingValues, allowedEpsilon);
              //gridCompression.writeGridPositionData(allStreamingValues.get(0), results);
              
            }else if(args[2].equals("douglas")){

              gridCompression.runSimpleDouglasCase(allStreamingValues, allowedEpsilon);

            }else if (args[2].equals("both")){
              
              gridCompression.runBothCase(allStreamingValues, allowedEpsilon, distanceType, kGonType);
                                
            }else if (args[2].equals("grid-adaptive")){
              /*
               * This part implements adaptive grid compression tecnique for the case when the sampling rate is not 
               * constant. We require the different technique as the same size of hexagon would not cater for the 
               * consecutive points where the distance between them is too large.
               */
               gridCompression.runBothAdaptiveCase(allStreamingValues, allowedEpsilon, distanceType, kGonType);
                //gridCompression.runBothCasesWithInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType);
               // gridCompression.runForGettingTheVariation(allStreamingValues, allowedEpsilon);

            }else if (args[2].equals("grid-threshold-adaptive")){
              /*
               * This part implements adaptive grid compression tecnique for the case when the sampling rate is not 
               * constant. However, it also assumes that we need to record the point itself in the cases where the ditance
               * for many points is too high.
               */
                if(args.length >= 4){
                    if (args[3].equals("-th")){
                        thresholdForPointRecord = Integer.parseInt(args[4]);
                    }
                }
                if(args.length >= 6){
                    if (args[5].equals("-energy")){
                        totalEnergyForTransfer = Integer.parseInt(args[6]);
                    }
                }
               //gridCompression.runBothAdaptiveWithThreshold(thresholdForPointRecord, allStreamingValues, allowedEpsilon, distanceType, kGonType, totalEnergyForTransfer, allDateTimeValues);
               //gridCompression.runBothCasesWithCodedInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType);
               //gridCompression.runBothCasesWithInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType);
               // gridCompression.runForGettingTheVariation(allStreamingValues, allowedEpsilon);
                gridCompression.runBothCase(allStreamingValues, allowedEpsilon, distanceType, kGonType);

            }else if (args[2].equals("grid-interpolated")){
              /*
               * This part implements adaptive grid compression tecnique with the interpolation for very long distanes when the sampling rate is not 
               * constant. 
               */
               //gridCompression.runBothCasesWithInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType);
               gridCompression.runBothCasesWithCodedInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType, allDateTimeValues);

            }else if (args[2].equals("time-distance-frequency")){

              //datetime.datetime(year, month, day[, hour[, minute[, second[, microsecond[, tzinfo]]]]])
              gridCompression.runForGettingTheVariation(allStreamingValues, allowedEpsilon);
             
            }else{
               CompressionDriver.usage();
            }

            } catch (NumberFormatException e) {
  		          System.err.println("Epsilon" + " must be an integer");
  		          System.exit(1);
  		      }
  	  }else
  		  CompressionDriver.usage();
    }else{
  	  CompressionDriver.usage();
    } 
  }

  /*
   * This function is run in order to get the differences in time and distance between consecutive GPS points.
   */
  void runForGettingTheVariation(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon){
    ArrayList<Date> allDateTimeValues = new ArrayList<Date>();
    String valuesToWrite = "";
    for (int i=1; i<allStreamingValues.size();i++){
        if (GeoHelper.getDistance(allStreamingValues.get(i-1), allStreamingValues.get(i))<2000)
            valuesToWrite +=GeoHelper. getDistance(allStreamingValues.get(i-1), allStreamingValues.get(i))+"\n";
    }
    IOHelper.writeToFile(valuesToWrite, "change-in-distance-without-variation.txt");

    valuesToWrite = "";
    for (int i=1; i<allDateTimeValues.size();i++){
     valuesToWrite += allDateTimeValues.get(i).getTime()- allDateTimeValues.get(i-1).getTime()+"\n";
    }
    IOHelper.writeToFile(valuesToWrite, "change-in-time.txt");
  }

  /*
   * This function implements the approximation by interpolation the points between the far away points
   */
  void runBothCasesWithInterpolation(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder gridCompressionDecoder = new KGonCompressionDecoder();
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> interpolatedSource;

     String resultOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistance = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    interpolatedSource = kgonCompressionPerformer.performInterpolation(allStreamingValues, allowedEpsilon, distanceType, resultantValues, kGonType);
    for (int k = 0; k<1500; k+=10){
      resultsLoose= new ArrayList<Integer>();
      results = kgonCompressionPerformer.performGridCompression(interpolatedSource, k, distanceType, kGonType);
      resultsLoose = kgonCompressionPerformer.performGridCompression(interpolatedSource, k, "loose", kGonType);
      IOHelper.writeGridPositionData(allStreamingValues.get(0), results, "strict-grid-"+k+".txt");
      //resultsLoose = performGridCompression(allStreamingValues, k, "loose");
      //writeGridPositionData(allStreamingValues.get(0), resultsLoose, "loose-grid-"+k+".txt");
      ArrayList<GPSPoint> convertedPoints = gridCompressionDecoder.getConvertedGPSPointList(allStreamingValues.get(0), results, k, distanceType);
      ArrayList<GPSPoint> looseConvertedPoints = gridCompressionDecoder.getConvertedGPSPointList(allStreamingValues.get(0), resultsLoose, k, "loose");
      IOHelper.writeGridConvertedGPS(convertedPoints, "data/converted-"+k+".txt");
      
      resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
      IOHelper.writeDouglasPositionData(resultantValues, "data/douglas-"+k+".txt");
      resultOfBoth += k+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
      resultSizeOfBoth += k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n";
      hausDorffDistance += k+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues)+"\n";
    }
    IOHelper.writeToFile(resultOfBoth, "interpolation-total-points-both.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "interpolation-total-size-both.txt");
    IOHelper.writeToFile(hausDorffDistance, "interpolation-hausdorff-both.txt");
  }
  
  /*
   * This function implements the approximation by interpolation the points between the far away points alongwith codes for how far
   */
  void runBothCasesWithCodedInterpolation(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, ArrayList<Date> allDateTimeValues){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder gridCompressionDecoder = new KGonCompressionDecoder();
    ArrayList<Double> results;
    ArrayList<Double> resultsLoose;
    ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> interpolatedSource;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();

    String resultOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistance = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    //interpolatedSource = kgonCompressionPerformer.performInterpolation(allStreamingValues, allowedEpsilon, distanceType, resultantValues, kGonType);
    for (int k = 10; k<1500; k+=10){
      results = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, k, distanceType, kGonType, allDateTimeValues);
      resultsLoose = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, k, "loose", kGonType, allDateTimeValues);
      IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), results, "data/strict-grid-coded-"+k+".txt");
      //resultsLoose = performGridCompression(allStreamingValues, k, "loose");
      //writeGridPositionData(allStreamingValues.get(0), resultsLoose, "loose-grid-"+k+".txt");
      ArrayList<GPSPoint> convertedPoints = gridCompressionDecoder.getGPSPointListInterpolated(allStreamingValues.get(0), results, k, distanceType);
      ArrayList<GPSPoint> looseConvertedPoints = gridCompressionDecoder.getGPSPointListInterpolated(allStreamingValues.get(0), resultsLoose, k, "loose");
      IOHelper.writeGridConvertedGPS(convertedPoints, "data/converted-coded"+k+".txt");
      IOHelper.writeGridConvertedGPS(whileConstructing, "data/example-grid-converted-coded-"+k+".txt");
      resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
      IOHelper.writeDouglasPositionData(resultantValues, "data/douglas-"+k+".txt");
      resultOfBoth += k+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
      resultSizeOfBoth += k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n";
      hausDorffDistance += k+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues)+"\n";
    }
    IOHelper.writeToFile(resultOfBoth, "coded-interpolation-total-points-both.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "coded-interpolation-total-size-both.txt");
    IOHelper.writeToFile(hausDorffDistance, "coded-interpolation-hausdorff-both.txt");
  }
  
  /*
   * This function implements the case where a particular threshold is set and if the distance between two consecutive points is more than that
   * we are going to record literal points. In all other cases it would be KGon based approximation.
   */
  void runBothAdaptiveWithThreshold(int thresholdForPointRecord,ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, int totalEnergyForTransfer, ArrayList<Date> allDateTimeValues){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder kgonCompressionDecoder = new KGonCompressionDecoder();
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<GPSPoint> resultantValues;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> gpsPointForCalibration = new ArrayList<GPSPoint>();
    
    String resultOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistance = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    IOHelper.writeListToFile(allStreamingValues, "original.txt");
    for (int k = 10; k<15; k+=50){
      
      //resultsLoose= new ArrayList<Integer>();
      results = kgonCompressionPerformer.performFixedBinBasedCompression(allStreamingValues, gpsPointForCalibration, k, distanceType, whileConstructing, kGonType, allDateTimeValues);
      resultsLoose = kgonCompressionPerformer.performFixedBinBasedCompression(allStreamingValues, gpsPointForCalibration, k, "loose", whileConstructing, kGonType, allDateTimeValues);
      IOHelper.writeGridPositionData(allStreamingValues.get(0), results, "data/strict-grid-fixedbin"+k+".txt");
      //resultsLoose = performAdaptiveGridCompression(allStreamingValues, k, "loose");
      //writeGridPositionData(allStreamingValues.get(0), resultsLoose, "data/loose-grid-"+k+".txt");
      //ArrayList<GPSPoint> convertedPoints = getConvertedGPSPointListForAdaptive(allStreamingValues.get(0), resultsLoose, k, "e");
      //writeGridConvertedGPS(allStreamingValues.get(0),convertedPoints, "data/loose-grid-converted-"+k+".txt");
      
      /**************start Checking k and m values***********/
      //System.out.println("The value of calculated k is: "+kgonCompressionPerformer.howManyTimesDoublingRequired(k, 5, allStreamingValues));
     // System.out.println("The value of calculated m is: "+kgonCompressionPerformer.howManyTimesHalvingRequired(k, 5, allStreamingValues));
      /**************end Checking k and m values***********/
      
      
      ArrayList<GPSPoint> convertedPoints = kgonCompressionDecoder.getConvertedGPSPointsForFixedBinBasedCompression(results, gpsPointForCalibration, k, distanceType);
      ArrayList<GPSPoint> looseConvertedPoints = kgonCompressionDecoder.getConvertedGPSPointsForFixedBinBasedCompression(resultsLoose, gpsPointForCalibration, k, "loose");
      IOHelper.writeGridConvertedGPS(convertedPoints, "data/strict-grid-converted-fixedbin-"+k+".txt");
      //ArrayList<GPSPoint> convertedPoints = kgonCompressionPerformer.performInterpolation(allStreamingValues, k, distanceType, whileConstructing, kGonType);
      IOHelper.writeGridConvertedGPS(whileConstructing, "data/example-grid-converted-fixedbin-"+k+".txt");
      
      resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
      IOHelper.writeDouglasPositionData(resultantValues, "data/douglas-"+k+".txt");
      resultOfBoth += k+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
      resultSizeOfBoth += k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n";
      hausDorffDistance += k+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues)+"\n";
      
    }
    IOHelper.writeToFile(resultOfBoth, "threshold-total-points-both.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "threshold-total-size-both.txt");
    IOHelper.writeToFile(hausDorffDistance, "threshold-hausdorff-both.txt");
    
  }
  
  /*
   * This function implements adaptive grid compression tecnique for the case when the sampling rate is not 
   * constant. The reason for different technque for this case is 
   */
  void runBothAdaptiveCase(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder kgonCompressionDecoder = new KGonCompressionDecoder();
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<GPSPoint> resultantValues;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();
    
    String resultOfBoth = "Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    IOHelper.writeListToFile(allStreamingValues, "original.txt");
    for (int k = 100; k<125; k+=50){
      
      resultsLoose= new ArrayList<Integer>();
      results = kgonCompressionPerformer.performAdaptiveKGonCompression(allStreamingValues, k, distanceType, whileConstructing, kGonType);
      
      IOHelper.writeGridPositionData(allStreamingValues.get(0), results, "data/strict-grid-"+k+".txt");
      resultsLoose = kgonCompressionPerformer.performAdaptiveGridCompression(allStreamingValues, k, "loose", whileConstructing, kGonType);
      //writeGridPositionData(allStreamingValues.get(0), resultsLoose, "data/loose-grid-"+k+".txt");
      //ArrayList<GPSPoint> convertedPoints = getConvertedGPSPointListForAdaptive(allStreamingValues.get(0), resultsLoose, k, "e");
      //writeGridConvertedGPS(allStreamingValues.get(0),convertedPoints, "data/loose-grid-converted-"+k+".txt");
      
      /**************start Checking k and m values***********/
      //System.out.println("The value of calculated k is: "+kgonCompressionPerformer.howManyTimesDoublingRequired(k, 5, allStreamingValues));
     // System.out.println("The value of calculated m is: "+kgonCompressionPerformer.howManyTimesHalvingRequired(k, 5, allStreamingValues));
      /**************end Checking k and m values***********/
      
      
      ArrayList<GPSPoint> convertedPoints = kgonCompressionDecoder.getConvertedGPSPointListForAdaptive(allStreamingValues.get(0), results, k, "exa");
      IOHelper.writeGridConvertedGPS(convertedPoints, "data/strict-grid-converted-"+k+".txt");
      //ArrayList<GPSPoint> convertedPoints = kgonCompressionPerformer.performInterpolation(allStreamingValues, k, distanceType, whileConstructing, kGonType);
      IOHelper.writeGridConvertedGPS( whileConstructing, "data/example-grid-converted-"+k+".txt");
      
      resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
      IOHelper.writeDouglasPositionData(resultantValues, "data/douglas-"+k+".txt");
      resultOfBoth += (resultsLoose.size())+"\t"+(results.size())+"\t"+(resultantValues.size())+"\n";
      resultSizeOfBoth += (float)(new File("data/loose-grid-"+k+".txt").length())+"\t"+(float)(new File("data/strict-grid-"+k+".txt").length())+"\t"+(float)(new File("data/douglas-"+k+".txt").length())+"\n";
    }
    IOHelper.writeToFile(resultOfBoth, "bothResults-adaptive-k.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "bothResultsSize-adaptive-k.txt");
  }

  /*
   * This function is called when argument is simpe "both"
   */
  void runBothCase(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<GPSPoint> resultantValues;

    String resultOfBoth = "Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    
    for (int k = 10; k<1500; k+=10){
      resultsLoose= new ArrayList<Integer>();
      results = kgonCompressionPerformer.performGridCompression(allStreamingValues, k, distanceType, kGonType);
      IOHelper.writeGridPositionData(allStreamingValues.get(0), results, "data/strict-grid-"+k+".txt");
      resultsLoose = kgonCompressionPerformer.performGridCompression(allStreamingValues, k, "loose", kGonType);
      IOHelper.writeGridPositionData(allStreamingValues.get(0), resultsLoose, "data/loose-grid-"+k+".txt");
      //ArrayList<GPSPoint> convertedPoints = gridCompression.getConvertedGPSPointList(allStreamingValues.get(0), results, k, "e");
      //gridCompression.writeGridConvertedGPS(allStreamingValues.get(0),convertedPoints, "grid-"+allowedEpsilon+".txt");
      
      resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
      IOHelper.writeDouglasPositionData(resultantValues, "data/douglas-"+k+".txt");
      resultOfBoth += (resultsLoose.size())+"\t"+(results.size())+"\t"+(resultantValues.size())+"\n";
      resultSizeOfBoth += (float)(new File("data/loose-grid-"+k+".txt").length())+"\t"+(float)(new File("data/strict-grid-"+k+".txt").length())+"\t"+(float)(new File("data/douglas-"+k+".txt").length())+"\n";
    }
    IOHelper.writeToFile(resultOfBoth, "simple-bothResults.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "simple-bothResultsSize.txt");
  }

  /*
   * This function is called when argument is simpe "grid"
   */
  void runSimpleGridCase(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon){
    ArrayList<Integer> results= new ArrayList<Integer>();
    KGonCompressionDecoder kgonCompressionDecoder = new KGonCompressionDecoder();
    ArrayList<GPSPoint> convertedPoints = kgonCompressionDecoder.getConvertedGPSPointList(allStreamingValues.get(0), results, allowedEpsilon, "e");
    IOHelper.writeGridConvertedGPS(convertedPoints, "grid-"+allowedEpsilon+".txt");
    System.out.println("Total collected data is: "+allStreamingValues.size());
    System.out.println("The total stored readings are: "+results.size());
    System.out.println("The File Size: "+new File("grid.txt").length());
  }

  /*
   * This function is called when argument is simpe "douglas"
   */
  void runSimpleDouglasCase(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon){
    ArrayList<GPSPoint> resultantValues;
    ArrayList<Integer> results= new ArrayList<Integer>();
    resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, allowedEpsilon);
    IOHelper.writeDouglasPositionData(resultantValues, "douglas-"+allowedEpsilon+".txt");
    System.out.println("Total collected data is: "+allStreamingValues.size());
    System.out.println("The total stored readings are: "+resultantValues.size());
    System.out.println("The File Size: "+new File("douglas-"+allowedEpsilon+".txt").length());
  }

  //This function takes the total flash size in bytes and returns the maximum
  int maximumpossibleError(int totalFlashSize, int oneUnitSize, int totalInMemory){
      int totalWindows = (totalFlashSize/oneUnitSize)/totalInMemory;
      return (int)Math.pow(2.0, ((double)totalWindows-1));
  }
    

  /*
   * Gathers data for running Usman's analysis
   */

  void getTwoDimensionalData (ArrayList<GPSPoint> source){
    ArrayList<SummarisedData> summarisedSensorData = new ArrayList<SummarisedData>();
    for (int i = 0; i < source.size()-1; i++){
      summarisedSensorData.add(new SummarisedData( GeoHelper.getGPSAngle(source.get(i), source.get(i+1)),GeoHelper. getDistance(source.get(i), source.get(i+1))));
    }
    IOHelper.writeSummaisedData(summarisedSensorData, "summarisedSensorData.txt");
  }
  
}



/************************ Code that might be of use at some later stage ******************************/
// resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(original);
                // original = gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*gridCompression.getSideLengthToUse(allowedEpsilon, 0.0), 0.0);
                // resultantValues.add(original);
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(original);
                // original = gridCompression.GeoHelper.getPointWithPolarDistance(new GPSPoint(-33.918417802,150.99099084), 2*gridCompression.getSideLengthToUse(allowedEpsilon, 60.0), 60.0);
                // resultantValues.add(original);
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(original);
                // original = gridCompression.GeoHelper.getPointWithPolarDistance(new GPSPoint(-33.918417802,150.99099084), 2*gridCompression.getSideLengthToUse(allowedEpsilon, 120.0), 120.0);
                // resultantValues.add(original);
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(original);
                // original = gridCompression.GeoHelper.getPointWithPolarDistance(new GPSPoint(-33.918417802,150.99099084), 2*gridCompression.getSideLengthToUse(allowedEpsilon, 180.0), 180.0);
                // resultantValues.add(original);
                //  resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(original);
                // original = gridCompression.GeoHelper.getPointWithPolarDistance(new GPSPoint(-33.918417802,150.99099084), 2*gridCompression.getSideLengthToUse(allowedEpsilon, -120.0), -120.0);
                // resultantValues.add(original);
                //  resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(original);
                // original = gridCompression.GeoHelper.getPointWithPolarDistance(new GPSPoint(-33.918417802,150.99099084), 2*gridCompression.getSideLengthToUse(allowedEpsilon, -60.0), -60.0);
                // resultantValues.add(original);
                //  resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -150.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -90.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, -30.0));
                // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, allowedEpsilon, 30.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, 0.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, 60.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, 120.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, 180.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, -120.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, -60.0));
                // // resultantValues.add(gridCompression.GeoHelper.getPointWithPolarDistance(original, 2*allowedEpsilon, 0.0));
                // gridCompression.writeDouglasPositionData(resultantValues, "six-hexagons-s.txt");
                /************************ Data Generation for Usman ******************************/
                // allStreamingValues = gridCompression.getPositionData("NN_Position.dat");
                // gridCompression.getTwoDimensionalData(allStreamingValues);
                // System.out.println(gridCompression.getGPSAngle(original, original));
                // System.out.println(gridCompression.getDistance(original, original));
                // GPSPoint check = gridCompression.GeoHelper.getPointWithPolarDistance(original, 100, 180);
                // System.out.println(gridCompression.getDistance(original, check));
                
