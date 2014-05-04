package Driver;

/**
 * @author Ghulam Murtaza <gmurtaza@cse.unsw.edu.au>
 * @date 13, March 2013
 */

import Compression.GDouglasPeuker;
import Compression.IndividualDistance;
import Compression.SummarisedData;
import Compression.WorstBinCounter;
import GeoHelper.GeoHelper;
import GeoHelper.GPSPoint;
import Decoder.KGonCompressionDecoder;
import Compression.KGonCompression;
import Helper.IOHelper;
import Helper.Pair;
import Compression.Jump;
import Compression.MainClustering;
import GeoHelper.ClusterStructure;
import Helper.Utility;
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
    int allowedEpsilon = 50; //error allowed in meters (default value is 10)
    int timeEpsilon = 1000000; // time epsilon in milliseconds
    int totalEnergyForTransfer = 300;
    int thresholdForPointSize = 50; // This is the amount of data that canbe transferred in bytes
    String distanceType = new String("exact");
    String kGonType = new String("Hexa");
    int thresholdForPointRecord = 2000;
    ArrayList<GPSPoint> allStreamingValues = new ArrayList<GPSPoint>();
    ArrayList<Date> allDateTimeValues = new ArrayList<Date>();
    ArrayList<String> fileNameList = new ArrayList<String>();
//    fileNameList.add("tag1937_gps.txt");
    fileNameList.add("tag1938_gps.txt");
    fileNameList.add("tag1936_gps.txt");
    fileNameList.add("tag1937_gps.txt");
    
//    fileNameList = IOHelper.getGeoLifeFiles("../../SensorClustering/Geo-Data/000/Trajectory");
    fileNameList.add("162-gps-Sept.txt");
    fileNameList.add("163-gps-Sept.txt");
    if (args.length >= 3){
        
  	  if (args[0].equals("-e")){
	      try {
           allowedEpsilon = Integer.parseInt(args[1]);
           /************************ Core compression technique ************************/
            //allStreamingValues = gridCompression.getPositionData("NN_Position.dat", " ");
            
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
                for (int f = 0; f<fileNameList.size(); f++){
                    String fileName = fileNameList.get(f);
                    String[] tokens = fileName.split("/");
                    String folderAppender = fileName.split("/")[tokens.length-1];
                    
//                    allStreamingValues = IOHelper.getPositionDataGeoLife(fileName, ",");
//                    allStreamingValues = IOHelper.getPositionDataLatest(fileName, ",");
                    if (fileName.equals("162-gps-Sept.txt")||fileName.equals("163-gps-Sept.txt"))
                        allStreamingValues = IOHelper.getPositionDataLatest(fileName, ",");
                    else if (fileName.equals("tag1938_gps.txt")||fileName.equals("tag1936_gps.txt")||fileName.equals("tag1937_gps.txt"))
                        allStreamingValues = IOHelper.getPositionData(fileName, ",");
                    else
                        allStreamingValues = IOHelper.getPositionDataGeoLife(fileName, ",");
                    //allDateTimeValues = IOHelper.getTimeData(fileName, ","); 
                    System.out.println(fileName);
                    System.out.println(allStreamingValues.size());
                    
                    allStreamingValues = new ArrayList<GPSPoint>(allStreamingValues.subList(0, allStreamingValues.size()));
                    ArrayList<Double> speedTrip = GeoHelper.speedWholeTrip(allStreamingValues);
                    IOHelper.writeSpeedDataDouble(allStreamingValues,speedTrip, "speedData-"+folderAppender+".txt");
                    IOHelper.writeListToFile(allStreamingValues, "original-"+folderAppender+".txt");
                    
                    
                    //gridCompression.runBothAdaptiveWithThreshold(thresholdForPointRecord, allStreamingValues, allowedEpsilon, distanceType, kGonType, totalEnergyForTransfer, allDateTimeValues, folderAppender);
                    //gridCompression.runBothCasesWithCodedInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType, allDateTimeValues);
                    if (allStreamingValues.size()>100)
                        gridCompression.runBothCasesWithCodedInterpolationSizeLimited(allStreamingValues, allowedEpsilon, distanceType, kGonType, thresholdForPointSize, folderAppender, timeEpsilon);
                    
                    //gridCompression.runBothCasesWithJumpsSize(allStreamingValues, allowedEpsilon, distanceType, kGonType, allDateTimeValues, thresholdForPointSize, folderAppender);
                    //gridCompression.testDistanceFunction();
                    //gridCompression.runForGettingTheVariation(allStreamingValues, allowedEpsilon, allDateTimeValues, folderAppender);
                    //gridCompression.runBothCasesWithInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType, folderAppender);
                }
               // gridCompression.runForGettingTheVariation(allStreamingValues, allowedEpsilon);
               //gridCompression.runBothCase(allStreamingValues, allowedEpsilon, distanceType, kGonType);

            }else if (args[2].equals("grid-interpolated")){
              /*
               * This part implements adaptive grid compression tecnique with the interpolation for very long distanes when the sampling rate is not 
               * constant. 
               */
                if(args.length >= 4){
                    if (args[3].equals("-th")){
                        thresholdForPointSize = Integer.parseInt(args[4]);
                    }
                }
                if(args.length >= 6){
                    if (args[5].equals("-energy")){
                        totalEnergyForTransfer = Integer.parseInt(args[6]);
                    }
                }
               //gridCompression.runBothCasesWithInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType);
               //gridCompression.runBothCasesWithCodedInterpolation(allStreamingValues, allowedEpsilon, distanceType, kGonType, allDateTimeValues, thresholdForPointSize, folderAppender);

            }else if (args[2].equals("time-distance-frequency")){

              //datetime.datetime(year, month, day[, hour[, minute[, second[, microsecond[, tzinfo]]]]])
              //gridCompression.runForGettingTheVariation(allStreamingValues, allowedEpsilon, allDateTimeValues, fileName);
             
            }else{
               CompressionDriver.usage();
            }

            } catch (NumberFormatException e) {
                e.printStackTrace();
  		System.out.println("Epsilon" + " must be an integer");
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
  void runForGettingTheVariation(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, ArrayList<Date> allDateTimeValues, String foladerNameAppender){
    //ArrayList<Date> allDateTimeValues = new ArrayList<Date>();
    String valuesToWrite = "";
    String valuesToWriteTime = "";
    for (int i=1; i<allStreamingValues.size();i++){
        if (GeoHelper.getDistance(allStreamingValues.get(i-1), allStreamingValues.get(i))<2000){
            valuesToWrite +=GeoHelper. getDistance(allStreamingValues.get(i-1), allStreamingValues.get(i))+"\n";
            valuesToWriteTime += (allStreamingValues.get(i).getTimeStamp().getTime()- allStreamingValues.get(i-1).getTimeStamp().getTime())/1000+"\n";
        }
    }
    IOHelper.writeToFile(valuesToWrite, "data-"+foladerNameAppender+"/change-in-distance");

//    valuesToWrite = "";
//    for (int i=1; i<allDateTimeValues.size();i++){
//     valuesToWrite += (allDateTimeValues.get(i).getTime()- allDateTimeValues.get(i-1).getTime())/1000+"\n";
//    }
    IOHelper.writeToFile(valuesToWriteTime, "data-"+foladerNameAppender+"/change-in-time");
  }

  /*
   * This function implements the approximation by interpolation the points between the far away points
   */
  void runBothCasesWithInterpolation(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, String foladerNameAppender){
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
    for (int k = 10; k<1500; k+=10){
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
      System.out.println(k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n");
      resultSizeOfBoth += k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n";
      hausDorffDistance += k+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues)+"\n";
    }
    IOHelper.writeToFile(resultOfBoth, "data-"+foladerNameAppender+"/interpolation-total-points-both.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "data-"+foladerNameAppender+"/interpolation-total-size-both.txt");
    IOHelper.writeToFile(hausDorffDistance, "data-"+foladerNameAppender+"/interpolation-hausdorff-both.txt");
  }
  
  /*
   * This function implements the approximation by mix of chases and jumps
   */
  void runBothCasesWithCodedInterpolationSizeLimited(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, int thresholdForTransferData, String foladerNameAppender, int timeEpsilon){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    MainClustering mainClustering = new MainClustering();
    ArrayList<Integer> unClusteredPointsCounter = new ArrayList<Integer>();
      ArrayList<Integer> removedPointsCounter = new ArrayList<Integer>();
    KGonCompressionDecoder gridCompressionDecoder = new KGonCompressionDecoder();
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPair;
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPairLoose;
    Pair<ArrayList<GPSPoint>, Integer> douglasPeukerResult;
    Pair<ArrayList<Integer>, HashMap<Integer, Integer>> returningEmptyBinsPair;
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<Integer> resultsLimited;
    ArrayList<Integer> resultsLooseLimited;
    ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> interpolatedSource;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();
    ArrayList<WorstBinCounter> binCounterArray = new ArrayList<WorstBinCounter>();
    ArrayList<ClusterStructure> clusteredData = new ArrayList<ClusterStructure>();
    ArrayList<Double> looseIndividualDistanceList = new ArrayList<Double>();
    ArrayList<IndividualDistance> clusteringIndividualDistanceList = new ArrayList<IndividualDistance>();
    IOHelper.writeListToFile(allStreamingValues, "original-"+foladerNameAppender+".txt");
    String resultOfBothLimited = "";//Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\t"+"Cluster"+"\n";
    String resultSizeOfBothLimited = "";//Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\t"+"Cluster"+"\n";
    String hausDorffDistanceLimited = "";//Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\t"+"Cluster"+"\n"; 
    String hausDorffDistanceLimitedTime = "";//Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\t"+"Cluster"+"\n"; 
    String synchronizedEuclidDistance = "";
    allowedEpsilon = 10; // base epsilon
    //interpolatedSource = kgonCompressionPerformer.performInterpolation(allStreamingValues, allowedEpsilon, distanceType, resultantValues, kGonType);
    for (int k = 50; k<201; k+=50){
        
        clusteredData = new ArrayList<ClusterStructure>();
        unClusteredPointsCounter = new ArrayList<Integer>();
        removedPointsCounter = new ArrayList<Integer>();
        //k = (int)(k/2);
        returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, k, distanceType, kGonType, binCounterArray, timeEpsilon, looseIndividualDistanceList);
        returningBinsWithPointsPairLoose = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, k, "loose", kGonType, binCounterArray, timeEpsilon, looseIndividualDistanceList);
        clusteredData = mainClustering.LightClustering(allStreamingValues, 100, k, 100, 10, unClusteredPointsCounter, removedPointsCounter, clusteringIndividualDistanceList);
        

        //int strictNewEpsilonMultipleToUse = kgonCompressionPerformer.epsilonForData(returningBinsWithPointsPair, thresholdForTransferData);

        //strictNewEpsilonMultipleToUse = strictNewEpsilonMultipleToUse*allowedEpsilon;

        //System.out.println("The proposed epsilon for strict Hexagon should be: "+(strictNewEpsilonMultipleToUse));

        

        //int looseNewEpsilonMultipleToUse = kgonCompressionPerformer.epsilonForData(returningBinsWithPointsPairLoose, thresholdForTransferData);

        //looseNewEpsilonMultipleToUse = looseNewEpsilonMultipleToUse*allowedEpsilon;

        //System.out.println("The proposed epsilon for loose Hexagon should be: "+(looseNewEpsilonMultipleToUse));

        results = returningBinsWithPointsPair.getSecond().getFirst();
        resultsLoose = returningBinsWithPointsPairLoose.getSecond().getFirst();
        int totalResultBits = results.remove(results.size()-1);
        //results.remove(results.size()-1);
        int totalResultBitsLoose = resultsLoose.remove(resultsLoose.size()-1);
        //resultsLoose.remove(resultsLoose.size()-1);
        ArrayList<GPSPoint> convertedPoints = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), results, k, distanceType, timeEpsilon);
        ArrayList<GPSPoint> looseConvertedPoints = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLoose, k, "loose", timeEpsilon);
///////////////////
//        int minimumJump = results.get(results.size()-1);
//        results.remove(results.size()-1);
//        int minimumJumpLoose = resultsLoose.get(resultsLoose.size()-1);
//        resultsLoose.remove(resultsLoose.size()-1);
//        resultsLimited = (k*8)<totalResultBits?kgonCompressionPerformer.convertToDesierdLimit( totalResultBits, k*8, allowedEpsilon, results):results;
//        resultsLooseLimited = (k*8)<totalResultBitsLoose?kgonCompressionPerformer.convertToDesierdLimit( totalResultBitsLoose, k*8, allowedEpsilon, resultsLoose):resultsLoose;
//        // after getting updated epsilon for strict hexagon, use that to get the points
//        //returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedInterpolation(convertedPoints, whileConstructing, strictNewEpsilonMultipleToUse, distanceType, kGonType, allDateTimeValues, binCounterArray);
//        allowedEpsilon = (k*8)>totalResultBits?allowedEpsilon:resultsLimited.remove(resultsLimited.size()-1);
//        System.out.println("Allowed epsilon: "+allowedEpsilon);
//        allowedEpsilon = (k*8)>totalResultBitsLoose?allowedEpsilon:resultsLooseLimited.remove(resultsLooseLimited.size()-1);
//        System.out.println("Allowed epsilon: "+allowedEpsilon);
//        totalResultBits = (k*8)>totalResultBits?totalResultBits:resultsLimited.remove(resultsLimited.size()-1);
//        totalResultBitsLoose = (k*8)>totalResultBitsLoose?totalResultBitsLoose:resultsLooseLimited.remove(resultsLooseLimited.size()-1);
///////////////////        
        
        if (!new File("Geo-life/data-"+foladerNameAppender+"/limited/codes/").exists()){
              new File("Geo-life/data-"+foladerNameAppender+"/limited/codes/").mkdirs();
              new File("Geo-life/data-"+foladerNameAppender+"/limited/location/").mkdirs();
          }
        IOHelper.writeClusterGPSPointsWithsize(clusteredData, "Geo-life/data-"+foladerNameAppender+"/limited/location/total-clustering-"+100+"-"+100+"-"+20+"-"+k+".txt"); 
        IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), results, "Geo-life/data-"+foladerNameAppender+"/limited/codes/strict-grid-coded-"+k+".txt");
        //resultsLoose = performGridCompression(allStreamingValues, k, "loose");
        IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), resultsLoose, "Geo-life/data-"+foladerNameAppender+"/limited/codes/loose-grid-coded-"+k+".txt");



//        ArrayList<GPSPoint> strictConvertedPointsLimited = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLimited, allowedEpsilon, distanceType);
//        ArrayList<GPSPoint> looseConvertedPointsLimited = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLooseLimited, allowedEpsilon, "loose");

//        IOHelper.writeGridConvertedGPS(whileConstructing, "Geo-life/data-"+foladerNameAppender+"/limited/location/loose-while-converted-coded"+k+".txt");
        //IOHelper.writeGridConvertedGPS(strictConvertedPointsLimited, "data/strict/strict-converted-coded"+k+".txt");
        //IOHelper.writeGridConvertedGPS(looseConvertedPointsLimited, "data/loose/loose-converted-coded"+k+".txt");
        System.out.println("Geo-life/data-"+foladerNameAppender+"/limited/location/loose-converted-coded"+k+".txt");
        IOHelper.writeGridConvertedGPS(convertedPoints, "Geo-life/data-"+foladerNameAppender+"/limited/location/strict-converted-coded"+k+".txt");
        IOHelper.writeGridConvertedGPS(looseConvertedPoints, "Geo-life/data-"+foladerNameAppender+"/limited/location/loose-converted-coded"+k+".txt");
//        IOHelper.writeGridConvertedGPS(whileConstructing, "Geo-life/data-"+foladerNameAppender+"/limited/location/example-grid-converted-coded-"+k+".txt");

//        resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
        //douglasPeukerResult = kgonCompressionPerformer.allowedEpsilonAndPointsDouglas(allStreamingValues, k, allowedEpsilon);
        //System.out.println("The epsilon required for douglas peuker is: "+douglasPeukerResult.getSecond());
        //resultantValues = douglasPeukerResult.getFirst();
//        IOHelper.writeDouglasPositionData(resultantValues, "Geo-life/data-"+foladerNameAppender+"/limited/location/douglas-"+k+".txt");
//        resultOfBothLimited += k+"\t"+(looseConvertedPoints.size())/*+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())*/+"\t"+clusteredData.size()+"\n";
        //System.out.println(k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n");
//        resultSizeOfBothLimited += k+"\t"+(float)(totalResultBitsLoose/8)/*+"\t"+(float)(totalResultBits/8)+"\t"+(float)((resultantValues.size()*12))*/+"\t"+clusteredData.size()*16+"\n";
        ArrayList<GPSPoint> clusteredGpsPointList = Utility.clusterStructureToGPS(clusteredData);
        if (!new File("Geo-life/data-"+foladerNameAppender+"/limited/inter/").exists())
              new File("Geo-life/data-"+foladerNameAppender+"/limited/inter/").mkdirs();
        IOHelper.writeSEDDataDouble(GeoHelper.getDistanceList(clusteredGpsPointList), "Geo-life/data-"+foladerNameAppender+"/limited/inter/cluster-inter-"+k+".txt");
        IOHelper.writeSEDDataDouble(GeoHelper.getDistanceList(looseConvertedPoints), "Geo-life/data-"+foladerNameAppender+"/limited/inter/loose-inter-"+k+".txt");
        
//        float looseHausdorff= GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints);
//        float strictHausdorff= GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints);
//        float douglasHausdorff = GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues);
//        float clusterHausdorff = GeoHelper.getHausdorffDistance(allStreamingValues, clusteredGpsPointList);
//        float looseHausdorffTime= GeoHelper.getHausdorffTimeDistance(allStreamingValues, looseConvertedPoints);
//        float strictHausdorffTime= GeoHelper.getHausdorffTimeDistance(allStreamingValues, convertedPoints);
//        float douglasHausdorffTime = GeoHelper.getHausdorffTimeDistance(allStreamingValues, resultantValues);
//        ArrayList<GPSPoint> equivalentGPSList = Utility.getEquivalentListCompressed(allStreamingValues, looseConvertedPoints);
//        IOHelper.writeGridConvertedGPSThis(equivalentGPSList, "Geo-life/data-"+foladerNameAppender+"/limited/location/loose-converted-equivalent"+k+".txt");
//        double looseSED = GeoHelper.totalSynchEuclideanDistance(allStreamingValues, equivalentGPSList);
//        double clusteredSED = GeoHelper.totalSynchEuclideanDistance(allStreamingValues, clusteredGpsPointList);
//        ArrayList<Double> looseSEDList = GeoHelper.pointWiseSynchEuclideanDistance(allStreamingValues, equivalentGPSList);
//        ArrayList<Double> clusteredSEDList = GeoHelper.pointWiseSynchEuclideanDistance(allStreamingValues, clusteredGpsPointList);
//        if (!new File("Geo-life/data-"+foladerNameAppender+"/limited/sed/").exists())
//              new File("Geo-life/data-"+foladerNameAppender+"/limited/sed/").mkdirs();
//        IOHelper.writeSEDDataDouble(looseSEDList, "Geo-life/data-"+foladerNameAppender+"/limited/sed/loose-grid-SED-"+k+".txt");
//        IOHelper.writeSEDDataDouble(clusteredSEDList, "Geo-life/data-"+foladerNameAppender+"/limited/sed/clustering-SED-"+k+".txt");
        
        if (!new File("Geo-life/data-"+foladerNameAppender+"/limited/ind/").exists())
              new File("Geo-life/data-"+foladerNameAppender+"/limited/ind/").mkdirs();
        IOHelper.writeSEDDataDouble(GeoHelper.getDistanceListConverted(looseConvertedPoints,allStreamingValues,k), "Geo-life/data-"+foladerNameAppender+"/limited/ind/loose-grid-ind-"+k+".txt");
        IOHelper.writeSEDDataDouble(GeoHelper.getDistanceListConvertedCluster(clusteringIndividualDistanceList,allStreamingValues,k), "Geo-life/data-"+foladerNameAppender+"/limited/ind/clustering-ind-"+k+".txt");
        IOHelper.writeSEDDataDouble(GeoHelper.getDateClusterList(clusteredData,allStreamingValues), "Geo-life/data-"+foladerNameAppender+"/limited/ind/clustering-step-wise-"+k+".txt");
        
        //System.out.println(k+"\t"+looseHausdorff+"\t"+strictHausdorff+"\t"+douglasHausdorff+"\n");
//        hausDorffDistanceLimited += k+"\t"+looseHausdorff/*+"\t"+strictHausdorff+"\t"+douglasHausdorff*/+"\t"+clusterHausdorff+"\n";
//        
//        hausDorffDistanceLimitedTime += timeEpsilon/1000+"\t"+looseHausdorffTime+"\t"+strictHausdorffTime+"\t"+douglasHausdorffTime+"\n";
//        synchronizedEuclidDistance += k+"\t"+looseSED/*+"\t"+strictHausdorff+"\t"+douglasHausdorff*/+"\t"+clusteredSED+"\n";
    }
    System.out.println("************ Total Points ************");
    System.out.println(resultOfBothLimited);
    System.out.println("************ Total Size ************");
    System.out.println(resultSizeOfBothLimited);
    System.out.println("************ Hausdorff Distance ************");
    System.out.println(hausDorffDistanceLimited);
    System.out.println("************ Time Hausdorff Distance ************");
    System.out.println(hausDorffDistanceLimitedTime);
    System.out.println("************ SED ************");
    System.out.println(synchronizedEuclidDistance);
    
    if (!new File("Geo-life/data-"+foladerNameAppender).exists()){
              new File("Geo-life/data-"+foladerNameAppender).mkdirs();
          }
    IOHelper.writeToFile(resultOfBothLimited, "Geo-life/data-"+foladerNameAppender+"/coded-interpolation-total-points-both-limited.txt");
    IOHelper.writeToFile(resultSizeOfBothLimited, "Geo-life/data-"+foladerNameAppender+"/coded-interpolation-total-size-both-limited.txt");
//    IOHelper.writeToFile(hausDorffDistanceLimited, "Geo-life/data-"+foladerNameAppender+"/coded-interpolation-hausdorff-both-limited.txt");
//    IOHelper.writeToFile(synchronizedEuclidDistance, "Geo-life/data-"+foladerNameAppender+"/coded-interpolation-SED-both-limited.txt");
  }
  
  
  /*
   * This function implements the approximation by mix of chases and jumps
   */
  void runBothCasesWithCodedInterpolationSize(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, int thresholdForTransferData, String foladerNameAppender, int timeEpsilon){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder gridCompressionDecoder = new KGonCompressionDecoder();
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPair;
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPairLoose;
    Pair<ArrayList<GPSPoint>, Integer> douglasPeukerResult;
    Pair<ArrayList<Integer>, HashMap<Integer, Integer>> returningEmptyBinsPair;
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<Integer> resultsLimited;
    ArrayList<Integer> resultsLooseLimited;
    ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> interpolatedSource;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();
    ArrayList<WorstBinCounter> binCounterArray = new ArrayList<WorstBinCounter>();
    ArrayList<Double> looseIndividualDistanceList = new ArrayList<Double>();
    ArrayList<Double> clusteringIndividualDistanceList = new ArrayList<Double>();
    IOHelper.writeListToFile(allStreamingValues, "original-"+foladerNameAppender+".txt");
    String resultOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistance = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    //interpolatedSource = kgonCompressionPerformer.performInterpolation(allStreamingValues, allowedEpsilon, distanceType, resultantValues, kGonType);
    for (int k = 50; k<551; k+=50){
        //k = (int)(k/2);
        returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, k, distanceType, kGonType, binCounterArray, timeEpsilon, looseIndividualDistanceList);
        returningBinsWithPointsPairLoose = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, k, "loose", kGonType, binCounterArray, timeEpsilon, looseIndividualDistanceList);
        
        

        //int strictNewEpsilonMultipleToUse = kgonCompressionPerformer.epsilonForData(returningBinsWithPointsPair, thresholdForTransferData);

        //strictNewEpsilonMultipleToUse = strictNewEpsilonMultipleToUse*allowedEpsilon;

        //System.out.println("The proposed epsilon for strict Hexagon should be: "+(strictNewEpsilonMultipleToUse));

        

        //int looseNewEpsilonMultipleToUse = kgonCompressionPerformer.epsilonForData(returningBinsWithPointsPairLoose, thresholdForTransferData);

        //looseNewEpsilonMultipleToUse = looseNewEpsilonMultipleToUse*allowedEpsilon;

        //System.out.println("The proposed epsilon for loose Hexagon should be: "+(looseNewEpsilonMultipleToUse));

        results = returningBinsWithPointsPair.getSecond().getFirst();
        resultsLoose = returningBinsWithPointsPairLoose.getSecond().getFirst();
        int totalResultBits = results.get(results.size()-1);
        results.remove(results.size()-1);
        int totalResultBitsLoose = resultsLoose.get(resultsLoose.size()-1);
        resultsLoose.remove(resultsLoose.size()-1);
        ArrayList<GPSPoint> convertedPoints = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), results, k, distanceType, timeEpsilon);
        ArrayList<GPSPoint> looseConvertedPoints = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLoose, k, "loose", timeEpsilon);

//        int minimumJump = results.get(results.size()-1);
//        results.remove(results.size()-1);
//        int minimumJumpLoose = resultsLoose.get(resultsLoose.size()-1);
//        resultsLoose.remove(resultsLoose.size()-1);
//        resultsLimited = kgonCompressionPerformer.dataApproximatedToLimits(thresholdForTransferData, minimumJump, results, totalResultBits);
//        resultsLooseLimited = kgonCompressionPerformer.dataApproximatedToLimits(thresholdForTransferData, minimumJumpLoose, resultsLoose, totalResultBitsLoose);
        // after getting updated epsilon for strict hexagon, use that to get the points
        //returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedInterpolation(convertedPoints, whileConstructing, strictNewEpsilonMultipleToUse, distanceType, kGonType, allDateTimeValues, binCounterArray);

        //whileConstructing = new ArrayList<GPSPoint>();

        // after getting updated epsilon for loose hexagon, use that to get the points
        //returningBinsWithPointsPairLoose = kgonCompressionPerformer.performGridCompressionCodedInterpolation(looseConvertedPoints, whileConstructing, looseNewEpsilonMultipleToUse, "loose", kGonType, allDateTimeValues, binCounterArray);

        //resultsLimited = returningBinsWithPointsPair.getSecond().getFirst();
        //resultsLooseLimited = returningBinsWithPointsPairLoose.getSecond().getFirst();


        IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), results, "data-"+foladerNameAppender+"/codes/strict-grid-coded-"+k+".txt");
        //resultsLoose = performGridCompression(allStreamingValues, k, "loose");
        IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), resultsLoose, "data-"+foladerNameAppender+"/codes/loose-grid-coded-"+k+".txt");



//        ArrayList<GPSPoint> strictConvertedPointsLimited = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLimited, k, distanceType);
//        ArrayList<GPSPoint> looseConvertedPointsLimited = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLooseLimited, k, "loose");

        IOHelper.writeGridConvertedGPS(whileConstructing, "data-"+foladerNameAppender+"/location/loose-while-converted-coded"+k+".txt");
//        IOHelper.writeGridConvertedGPS(strictConvertedPointsLimited, "data/strict/strict-converted-coded"+k+".txt");
//        IOHelper.writeGridConvertedGPS(looseConvertedPointsLimited, "data/loose/loose-converted-coded"+k+".txt");
        
        IOHelper.writeGridConvertedGPS(convertedPoints, "data-"+foladerNameAppender+"/location/strict-converted-coded"+k+".txt");
        IOHelper.writeGridConvertedGPS(looseConvertedPoints, "data-"+foladerNameAppender+"/location/loose-converted-coded"+k+".txt");
        IOHelper.writeGridConvertedGPS(whileConstructing, "data-"+foladerNameAppender+"/location/example-grid-converted-coded-"+k+".txt");

        resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
        //System.out.println("The epsilon required for douglas peuker is: "+douglasPeukerResult.getSecond());
        //resultantValues = douglasPeukerResult.getFirst();
        IOHelper.writeDouglasPositionData(resultantValues, "data-"+foladerNameAppender+"/location/douglas-"+k+".txt");
        resultOfBoth += k+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
        //System.out.println(k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n");
        resultSizeOfBoth += k+"\t"+(float)(totalResultBitsLoose/8)+"\t"+(float)(totalResultBits/8)+"\t"+(float)((resultantValues.size()*12))+"\n";
        float looseHausdorff= GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints);
        float strictHausdorff= GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints);
        float douglasHausdorff = GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues);
        //System.out.println(k+"\t"+looseHausdorff+"\t"+strictHausdorff+"\t"+douglasHausdorff+"\n");
        hausDorffDistance += k+"\t"+looseHausdorff+"\t"+strictHausdorff+"\t"+douglasHausdorff+"\n";
    }
    System.out.println("************ Total Points ************");
    System.out.println(resultOfBoth);
    System.out.println("************ Total Size ************");
    System.out.println(resultSizeOfBoth);
    System.out.println("************ Hausdorff Distance ************");
    System.out.println(hausDorffDistance);
    IOHelper.writeToFile(resultOfBoth, "data-"+foladerNameAppender+"/coded-interpolation-total-points-both.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "data-"+foladerNameAppender+"/coded-interpolation-total-size-both.txt");
    IOHelper.writeToFile(hausDorffDistance, "data-"+foladerNameAppender+"/coded-interpolation-hausdorff-both.txt");
  }
  
  
  
  /*
   * This function implements the approximation by coding only Jumps
   */
  void runBothCasesWithJumpsSize(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, ArrayList<Date> allDateTimeValues, int thresholdForTransferData, String foladerNameAppender){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder gridCompressionDecoder = new KGonCompressionDecoder();
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPair;
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPairLoose;
    Pair<ArrayList<GPSPoint>, Integer> douglasPeukerResult;
    Pair<ArrayList<Integer>, HashMap<Integer, Integer>> returningEmptyBinsPair;
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<Integer> resultsLimited;
    ArrayList<Integer> resultsLooseLimited;
    ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> interpolatedSource;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();
    ArrayList<WorstBinCounter> binCounterArray = new ArrayList<WorstBinCounter>();
    IOHelper.writeListToFile(allStreamingValues, "original-"+foladerNameAppender+".txt");
    String resultOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistance = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    //interpolatedSource = kgonCompressionPerformer.performInterpolation(allStreamingValues, allowedEpsilon, distanceType, resultantValues, kGonType);
    for (int k = 100; k<550; k+=50){
        //int k = 100;
        returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedJumps(allStreamingValues, whileConstructing, k, distanceType, kGonType, allDateTimeValues, binCounterArray);
        returningBinsWithPointsPairLoose = kgonCompressionPerformer.performGridCompressionCodedJumps(allStreamingValues, whileConstructing, k, "loose", kGonType, allDateTimeValues, binCounterArray);
        
        results = returningBinsWithPointsPair.getSecond().getFirst();
        resultsLoose = returningBinsWithPointsPairLoose.getSecond().getFirst();
        int totalResultBits = results.get(results.size()-1);
        results.remove(results.size()-1);
        int totalResultBitsLoose = resultsLoose.get(resultsLoose.size()-1);
        resultsLoose.remove(resultsLoose.size()-1);
        ArrayList<GPSPoint> convertedPoints = gridCompressionDecoder.getGPSPointListCodedJumps(allStreamingValues.get(0), results, k, distanceType);
        ArrayList<GPSPoint> looseConvertedPoints = gridCompressionDecoder.getGPSPointListCodedJumps(allStreamingValues.get(0), resultsLoose, k, "loose");

        
        IOHelper.writeGridConvertedGPS(whileConstructing, "data-"+foladerNameAppender+"/location/loose-while-converted-coded"+k+".txt");
        //IOHelper.writeGridConvertedGPS(strictConvertedPointsLimited, "data/strict/strict-converted-coded"+strictNewEpsilonMultipleToUse+".txt");
        //IOHelper.writeGridConvertedGPS(looseConvertedPointsLimited, "data/loose/loose-converted-coded"+looseNewEpsilonMultipleToUse+".txt");
        
        IOHelper.writeGridConvertedGPS(convertedPoints, "data-"+foladerNameAppender+"/location/strict-converted-coded"+k+".txt");
        IOHelper.writeGridConvertedGPS(looseConvertedPoints, "data-"+foladerNameAppender+"/location/loose-converted-coded"+k+".txt");
        IOHelper.writeGridConvertedGPS(whileConstructing, "data-"+foladerNameAppender+"/location/example-grid-converted-coded-"+k+".txt");

        resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
        //System.out.println("The epsilon required for douglas peuker is: "+douglasPeukerResult.getSecond());
        //resultantValues = douglasPeukerResult.getFirst();
        IOHelper.writeDouglasPositionData(resultantValues, "data-"+foladerNameAppender+"/location/douglas-"+k+".txt");
        resultOfBoth += k+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
        //System.out.println(k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n");
        resultSizeOfBoth += k+"\t"+(float)(totalResultBitsLoose/8)+"\t"+(float)(totalResultBits/8)+"\t"+(float)((resultantValues.size()*12))+"\n";
        float looseHausdorff= GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints);
        float strictHausdorff= GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints);
        float douglasHausdorff = GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues);
        //System.out.println(k+"\t"+looseHausdorff+"\t"+strictHausdorff+"\t"+douglasHausdorff+"\n");
        hausDorffDistance += k+"\t"+looseHausdorff+"\t"+strictHausdorff+"\t"+douglasHausdorff+"\n";
    }
    System.out.println("************ Total Points ************");
    System.out.println(resultOfBoth);
    System.out.println("************ Total Size ************");
    System.out.println(resultSizeOfBoth);
    System.out.println("************ Hausdorff Distance ************");
    System.out.println(hausDorffDistance);
    IOHelper.writeToFile(resultOfBoth, "data-"+foladerNameAppender+"/coded-interpolation-total-points-both.txt");
    IOHelper.writeToFile(resultSizeOfBoth, "data-"+foladerNameAppender+"/coded-interpolation-total-size-both.txt");
    IOHelper.writeToFile(hausDorffDistance, "data-"+foladerNameAppender+"/coded-interpolation-hausdorff-both.txt");
  }
  
  
  /*
   * This function implements the approximation by interpolation the points between the far away points alongwith codes for how far
   * And runs it for energy based ocnstraints
   */
  void runBothCasesWithCodedInterpolation(ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, ArrayList<Date> allDateTimeValues, int thresholdForTransferData, String foladerNameAppender, int timeEpsilon){
    KGonCompression kgonCompressionPerformer = new KGonCompression();
    KGonCompressionDecoder gridCompressionDecoder = new KGonCompressionDecoder();
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPair;
    Pair<HashMap<Integer, Integer>, Pair<ArrayList<Integer>, HashMap<Integer, Integer>>> returningBinsWithPointsPairLoose;
    Pair<ArrayList<GPSPoint>, Integer> douglasPeukerResult;
    Pair<ArrayList<Integer>, HashMap<Integer, Integer>> returningEmptyBinsPair;
    ArrayList<Integer> results;
    ArrayList<Integer> resultsLoose;
    ArrayList<Integer> resultsLimited;
    ArrayList<Integer> resultsLooseLimited;
    ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
    ArrayList<GPSPoint> interpolatedSource;
    ArrayList<GPSPoint> whileConstructing = new ArrayList<GPSPoint>();
     ArrayList<WorstBinCounter> binCounterArray = new ArrayList<WorstBinCounter>();
     IOHelper.writeListToFile(allStreamingValues, "original.txt");
    String resultOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBoth = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistance = "Error Threshold"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    
    String resultOfBothLimit = "Allowed Size"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String resultSizeOfBothLimit = "Allowed Size"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n";
    String hausDorffDistanceLimit = "Allowed Size"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    String epsilonRequiredLimit = "Allowed Size"+"\t"+"Loose Hexagons"+"\t"+"Strict Hexagons"+"\t"+"Douglas"+"\n"; 
    ArrayList<Double> looseIndividualDistanceList = new ArrayList<Double>();
    //interpolatedSource = kgonCompressionPerformer.performInterpolation(allStreamingValues, allowedEpsilon, distanceType, resultantValues, kGonType);
    for (int k = 50; k<=150; k+=10){
        returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, allowedEpsilon, distanceType, kGonType, binCounterArray, timeEpsilon, looseIndividualDistanceList);

        //int strictNewEpsilonMultipleToUse = kgonCompressionPerformer.epsilonForData(returningBinsWithPointsPair, k);

        //strictNewEpsilonMultipleToUse = strictNewEpsilonMultipleToUse*allowedEpsilon;

        //System.out.println("The proposed epsilon for strict Hexagon should be: "+(strictNewEpsilonMultipleToUse));

        returningBinsWithPointsPairLoose = kgonCompressionPerformer.performGridCompressionCodedInterpolation(allStreamingValues, whileConstructing, allowedEpsilon, "loose", kGonType, binCounterArray, timeEpsilon, looseIndividualDistanceList);
        
        

        //int looseNewEpsilonMultipleToUse = kgonCompressionPerformer.epsilonForData(returningBinsWithPointsPairLoose, k);

        //looseNewEpsilonMultipleToUse = looseNewEpsilonMultipleToUse*allowedEpsilon;

        //System.out.println("The proposed epsilon for loose Hexagon should be: "+(looseNewEpsilonMultipleToUse));

        results = returningBinsWithPointsPair.getSecond().getFirst();
        resultsLoose = returningBinsWithPointsPairLoose.getSecond().getFirst();
        
        int totalResultBits = results.get(results.size()-1);
        results.remove(results.size()-1);
        int totalResultBitsLoose = resultsLoose.get(resultsLoose.size()-1);
        resultsLoose.remove(resultsLoose.size()-1);
        
        int minJumpSize = results.get(results.size()-1);
        results.remove(results.size()-1);
        int minJumpSizeLoose = resultsLoose.get(resultsLoose.size()-1);
        resultsLoose.remove(resultsLoose.size()-1);

        ArrayList<GPSPoint> convertedPoints = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), results, allowedEpsilon, distanceType, timeEpsilon);
        ArrayList<GPSPoint> looseConvertedPoints = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLoose, allowedEpsilon, "loose", timeEpsilon);

        // after getting updated epsilon for strict hexagon, use that to get the points
        //returningBinsWithPointsPair = kgonCompressionPerformer.performGridCompressionCodedInterpolation(convertedPoints, whileConstructing, strictNewEpsilonMultipleToUse, distanceType, kGonType, allDateTimeValues, binCounterArray, timeEpsilon);

        whileConstructing = new ArrayList<GPSPoint>();

        // after getting updated epsilon for loose hexagon, use that to get the points
        //returningBinsWithPointsPairLoose = kgonCompressionPerformer.performGridCompressionCodedInterpolation(looseConvertedPoints, whileConstructing, looseNewEpsilonMultipleToUse, "loose", kGonType, allDateTimeValues, binCounterArray, timeEpsilon);

        resultsLimited = returningBinsWithPointsPair.getSecond().getFirst();
        resultsLooseLimited = returningBinsWithPointsPairLoose.getSecond().getFirst();


        IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), results, "data/strict/strict-grid-coded-"+allowedEpsilon+".txt");
        //resultsLoose = performGridCompression(allStreamingValues, k, "loose");
        IOHelper.writeGridPositionDataDouble(allStreamingValues.get(0), resultsLoose, "data/loose/loose-grid-coded-"+allowedEpsilon+".txt");



        //ArrayList<GPSPoint> strictConvertedPointsLimited = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLimited, strictNewEpsilonMultipleToUse, distanceType);
        //ArrayList<GPSPoint> looseConvertedPointsLimited = gridCompressionDecoder.getGPSPointListCodedInterpolated(allStreamingValues.get(0), resultsLooseLimited, looseNewEpsilonMultipleToUse, "loose");

        //IOHelper.writeGridConvertedGPS(whileConstructing, "data/loose-while-converted-coded"+allowedEpsilon+".txt");
        //IOHelper.writeGridConvertedGPS(strictConvertedPointsLimited, "data/strict/strict-converted-coded"+strictNewEpsilonMultipleToUse+".txt");
        //IOHelper.writeGridConvertedGPS(looseConvertedPointsLimited, "data/loose/loose-converted-coded"+looseNewEpsilonMultipleToUse+".txt");

        IOHelper.writeGridConvertedGPS(convertedPoints, "data/strict/strict-converted-coded"+allowedEpsilon+".txt");
        IOHelper.writeGridConvertedGPS(looseConvertedPoints, "data/loose/loose-converted-coded"+allowedEpsilon+".txt");
        IOHelper.writeGridConvertedGPS(whileConstructing, "data/loose/example-grid-converted-coded-"+allowedEpsilon+".txt");

        douglasPeukerResult = kgonCompressionPerformer.allowedEpsilonAndPointsDouglas(allStreamingValues, k, allowedEpsilon);
        //System.out.println("The epsilon required for douglas peuker is: "+douglasPeukerResult.getSecond());
        resultantValues = GDouglasPeuker.douglasPeucker (allStreamingValues,allowedEpsilon);
        IOHelper.writeDouglasPositionData(douglasPeukerResult.getFirst(), "data/douglas/douglas-"+douglasPeukerResult.getSecond()+".txt");
        resultOfBoth += allowedEpsilon+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
        resultSizeOfBoth += allowedEpsilon+"\t"+(float)(totalResultBitsLoose/8)+"\t"+(float)(totalResultBits/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n";
        hausDorffDistance += allowedEpsilon+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues)+"\n";
        
        //This part shows the calculations for new epsilon
        //resultOfBothLimit += k+"\t"+(looseConvertedPointsLimited.size())+"\t"+(strictConvertedPointsLimited.size())+"\t"+(douglasPeukerResult.getFirst().size())+"\n";
        //resultSizeOfBothLimit += k+"\t"+(float)(totalResultBitsLoose/8)+"\t"+(float)(totalResultBits/8)+"\t"+(float)((douglasPeukerResult.getFirst().size()*64)/8)+"\n";
        //hausDorffDistanceLimit += k+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPointsLimited)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, strictConvertedPointsLimited)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, douglasPeukerResult.getFirst())+"\n";
        //epsilonRequiredLimit += k+"\t"+looseNewEpsilonMultipleToUse+"\t"+strictNewEpsilonMultipleToUse+"\t"+douglasPeukerResult.getSecond()+"\n";
    }
//    System.out.println(resultOfBoth);
//    System.out.println(resultSizeOfBoth);
//    System.out.println(hausDorffDistance+"\n\n\n\n\n\n");
    
    System.out.println("************ Total Points ************");
    System.out.println(resultOfBoth);
    System.out.println("************ Total Size ************");
    System.out.println(resultSizeOfBoth);
    System.out.println("************ Hausdorff Distance ************");
    System.out.println(hausDorffDistance);
//    System.out.println("************ Epsilon Required ************");
//    System.out.println(epsilonRequiredLimit);
//    IOHelper.writeToFile(resultOfBoth, "data-"+foladerNameAppender+"/coded-interpolation-total-points-both.txt");
//    IOHelper.writeToFile(resultSizeOfBoth, "data-"+foladerNameAppender+"/coded-interpolation-total-size-both.txt");
//    IOHelper.writeToFile(hausDorffDistance, "data-"+foladerNameAppender+"/coded-interpolation-hausdorff-both.txt");
  }
  
  /*
   * This function implements the case where a particular threshold is set and if the distance between two consecutive points is more than that
   * we are going to record literal points. In all other cases it would be KGon based approximation.
   */
  void runBothAdaptiveWithThreshold(int thresholdForPointRecord,ArrayList<GPSPoint> allStreamingValues, int allowedEpsilon, String distanceType, String kGonType, int totalEnergyForTransfer, ArrayList<Date> allDateTimeValues, String foladerNameAppender){
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
    for (int k = 10; k<1500; k+=10){
      
      //resultsLoose= new ArrayList<Integer>();
      results = kgonCompressionPerformer.performFixedBinBasedCompression(allStreamingValues, gpsPointForCalibration, k, distanceType, whileConstructing, kGonType, allDateTimeValues);
      resultsLoose = kgonCompressionPerformer.performFixedBinBasedCompression(allStreamingValues, gpsPointForCalibration, k, "loose", whileConstructing, kGonType, allDateTimeValues);
      IOHelper.writeGridPositionData(allStreamingValues.get(0), results, "data/strict/strict-grid-fixedbin"+k+".txt");
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
      IOHelper.writeGridConvertedGPS(convertedPoints, "data/strict/strict-grid-converted-fixedbin-"+k+".txt");
      //ArrayList<GPSPoint> convertedPoints = kgonCompressionPerformer.performInterpolation(allStreamingValues, k, distanceType, whileConstructing, kGonType);
      //IOHelper.writeGridConvertedGPS(whileConstructing, "data/example-grid-converted-fixedbin-"+k+".txt");
      
      resultantValues = GDouglasPeuker.douglasPeucker(allStreamingValues, k);
      IOHelper.writeDouglasPositionData(resultantValues, "data/douglas-"+k+".txt");
      resultOfBoth += k+"\t"+(looseConvertedPoints.size())+"\t"+(convertedPoints.size())+"\t"+(resultantValues.size())+"\n";
      resultSizeOfBoth += k+"\t"+(float)((resultsLoose.size()*4)/8)+"\t"+(float)((results.size()*4)/8)+"\t"+(float)((resultantValues.size()*64)/8)+"\n";
      hausDorffDistance += k+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, looseConvertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, convertedPoints)+"\t"+GeoHelper.getHausdorffDistance(allStreamingValues, resultantValues)+"\n";
      
    }
    System.out.println(resultOfBoth);
    System.out.println(resultSizeOfBoth);
    System.out.println(hausDorffDistance);
//    IOHelper.writeToFile(resultOfBoth, "data-"+foladerNameAppender+"/threshold-total-points-both.txt");
//    IOHelper.writeToFile(resultSizeOfBoth, "data-"+foladerNameAppender+"/threshold-total-size-both.txt");
//    IOHelper.writeToFile(hausDorffDistance, "data-"+foladerNameAppender+"/threshold-hausdorff-both.txt");
    
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
  
  void testDistanceFunction(){
//      Current Centre: 145.46137971738474, -16.485532391005144
//Current Point: 145.4626505, -16.4878846
//Distance is: 294.566162109375
//Horizontal Distance: 135.49558075273632
//Total distance added: 0.0
//145.4618481308386, -16.485532391005144
//New Centre: 145.4618481308386, -16.48942221028842
//      145.33985226214452, -16.41254142122592
//Current Point: 145.3632549, -16.4139046
//Distance is: 2500.8076171875
//New Centre: 145.36232765280877, -16.41254142122592
//Distance is: 180.99162
      ArrayList<GPSPoint> resultantValues = new ArrayList<GPSPoint>();
      //GPSPoint currentCentre = new GPSPoint(-16.450754609499484,145.40568972804195);
      GPSPoint currentCentre = new GPSPoint(145.46137971738474, -16.485532391005144);
      //145.40943636335138 x -16.444530898646242
      float allowedEpsilon=10;
      float newEpsilon=20;
      KGonCompression kgonCompression = new KGonCompression();
      GPSPoint nextPoint = GeoHelper.getPointWithPolarDistance(currentCentre, 100, 45);//new GPSPoint(145.40943636335138, -16.64812415978272);
      //GPSPoint secondNextPoint = //new GPSPoint(145.6826505, -16.2978846);
      //GPSPoint afterSecondNextPoint = GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, 90.0);
      double angle = GeoHelper.getGPSAngle( currentCentre, nextPoint);
      double distance = GeoHelper.getDistance( currentCentre, nextPoint);
      System.out.println("The distance is: "+distance+" & angle is: "+angle);
      
      
//      System.out.println("x distance: "+kgonCompression.calculateAndReturnHorizontalMultiple(distnace, angle));
//      System.out.println("y distance: "+kgonCompression.calculateAndReturnVerticalMultiple(distnace, angle));
      int xMultiple =  kgonCompression.calculateHorizontalJump(distance, angle, allowedEpsilon);
      int yMultiple =  kgonCompression.calculateVerticalJump(distance, angle, allowedEpsilon, xMultiple);
      System.out.println("1st x multiple: "+ xMultiple);
      System.out.println("1st y multiple: "+yMultiple);
      System.out.println("The quadrant is: "+(int)kgonCompression.returnQuadrantCode(angle));
      
      Jump firstJump = new Jump(xMultiple, yMultiple, (int)kgonCompression.returnQuadrantCode(angle));
      GPSPoint newCentre = kgonCompression.calculateNewCentreWithHexagonMultiples(currentCentre, allowedEpsilon, xMultiple, yMultiple, (int)kgonCompression.returnQuadrantCode(angle));
//      angle = GeoHelper.getGPSAngle(newCentre, secondNextPoint);
//      distance = GeoHelper.getDistance(newCentre, secondNextPoint);
//      System.out.println("The distance is: "+distance+" & angle is: "+angle);
//      System.out.println("New centre is: "+ newCentre.getLongitude()+", "+newCentre.getLatitude());
////      angle = GeoHelper.getGPSAngle(thirdNewCentre, secondNextPoint);
////      distance = GeoHelper.getDistance(thirdNewCentre, secondNextPoint);
////      System.out.println("The distance is: "+distance+" & angle is: "+angle);
//      xMultiple =  kgonCompression.calculateHorizontalJump(distance, angle, allowedEpsilon);
//      yMultiple =  kgonCompression.calculateVerticalJump(distance, angle, allowedEpsilon, xMultiple);
//      System.out.println("2nd x multiple: "+ xMultiple);
//      System.out.println("2nd y multiple: "+yMultiple);
//      System.out.println("The quadrant is: "+(int)kgonCompression.returnQuadrantCode(angle));
//      Jump secondJump = new Jump(xMultiple, yMultiple, (int)kgonCompression.returnQuadrantCode(angle));
//      
//      GPSPoint secondNewCentre = KGonCompression.calculateNewCentreWithHexagonMultiples(newCentre, allowedEpsilon, xMultiple, yMultiple, (int)kgonCompression.returnQuadrantCode(angle));
//      
//      angle = GeoHelper.getGPSAngle(secondNewCentre, secondNextPoint);
//      distance = GeoHelper.getDistance(secondNewCentre, secondNextPoint);
//      System.out.println("The second to second point distance is: "+distance+" & angle is: "+angle);
//      
//      System.out.println("Second new centre is: "+ secondNewCentre.getLongitude()+", "+secondNewCentre.getLatitude());
      
      Jump firstJumpRecompressed = kgonCompression.convertSimpleJump(firstJump, 2);
      
      System.out.println("1st x multiple rec: "+ firstJumpRecompressed.getXjump());
      System.out.println("1st y multiple rec: "+firstJumpRecompressed.getYjump());
      System.out.println("The quadrant is rec: "+firstJumpRecompressed.getQuadrant());
      
      GPSPoint newCentreRecomp = kgonCompression.calculateNewCentreWithHexagonMultiples(currentCentre, newEpsilon, firstJumpRecompressed.getXjump(), firstJumpRecompressed.getYjump(), firstJumpRecompressed.getQuadrant());
      
      System.out.println("New centre Recomp is: "+ newCentreRecomp.getLongitude()+", "+newCentreRecomp.getLatitude());
//      double recompAngle = GeoHelper.getGPSAngle(newCentreRecomp, secondNextPoint);
//      double recompDistance = GeoHelper.getDistance(newCentreRecomp, secondNextPoint);
//      System.out.println("The distance for re-compressioin is: "+recompDistance+" & angle is: "+recompAngle);
//      xMultiple =  kgonCompression.calculateHorizontalJump(recompDistance, recompAngle, allowedEpsilon);
//      yMultiple =  kgonCompression.calculateVerticalJump(recompDistance, recompAngle, allowedEpsilon, xMultiple);
//      System.out.println("nor 2nd x multiple: "+ xMultiple);
//      System.out.println("nor 2nd y multiple: "+yMultiple);
//      System.out.println("The rec quadrant is: "+(int)kgonCompression.returnQuadrantCode(recompAngle));
//      Jump secondRecompJump = new Jump(xMultiple, yMultiple, (int)kgonCompression.returnQuadrantCode(recompAngle));
//      Jump secondJumpRecompressed = kgonCompression.convertSimpleJump(secondRecompJump, 2);
//      
//      System.out.println("rec 1st x multiple: "+ secondJumpRecompressed.getXjump());
//      System.out.println("Rec 1st y multiple: "+secondJumpRecompressed.getYjump());
//      System.out.println("The quadrant is: "+secondJumpRecompressed.getQuadrant());
//      
//      GPSPoint secondNewCentreRecomp = KGonCompression.calculateNewCentreWithHexagonMultiples(newCentreRecomp, newEpsilon, secondJumpRecompressed.getXjump(), secondJumpRecompressed.getYjump(), secondJumpRecompressed.getQuadrant());
//      
//      System.out.println("Second new centre Recomp is: "+ secondNewCentreRecomp.getLongitude()+", "+secondNewCentreRecomp.getLatitude());
//      
//      angle = GeoHelper.getGPSAngle(secondNewCentreRecomp, secondNextPoint);
//      distance = GeoHelper.getDistance(secondNewCentreRecomp, secondNextPoint);
//      System.out.println("The second rec to second point distance is: "+distance+" & angle is: "+angle);
//      
//      
//      angle = GeoHelper.getGPSAngle(secondNewCentreRecomp, secondNewCentre);
//      distance = GeoHelper.getDistance(secondNewCentreRecomp, secondNewCentre);
//      System.out.println("The distance between jumped points is: "+distance+" & angle is: "+angle);
      
//      Jump newJump = new Jump();
//      newJump = kgonCompression.mergeJumps(firstJump, secondJump);
//      System.out.println("2nd x multiple: "+ newJump.getXjump());
//      System.out.println("2nd y multiple: "+newJump.getYjump());
//      System.out.println("The quadrant is: "+newJump.getQuadrant());
//      GPSPoint thirdNewCentre = kgonCompression.calculateNewCentreWithHexagonMultiples(currentCentre, allowedEpsilon, newJump.getXjump(), newJump.getYjump(), newJump.getQuadrant());
      //System.out.println("The distance is: "+GeoHelper.getDistance(currentCentre, newCentre)+" & angle is: "+GeoHelper.getGPSAngle(currentCentre, newCentre));
      //System.out.println("The distance is: "+GeoHelper.getDistance(nextPoint, newCentre)+" & angle is: "+GeoHelper.getGPSAngle(nextPoint, newCentre));
      //System.out.println("The distance b/w x-multiple and dest is: "+GeoHelper.getDistance(nextPoint, secondNextPoint)+" & angle is: "+GeoHelper.getGPSAngle( secondNextPoint,nextPoint));
      
//      angle = GeoHelper.getGPSAngle(currentCentre, secondNextPoint);
//      distance = GeoHelper.getDistance(currentCentre, secondNextPoint);
//      //System.out.println(currentCentre.getLatitude()+", "+currentCentre.getLongitude());
//      System.out.println(currentCentre.getLongitude()+", "+currentCentre.getLatitude());
//      System.out.println(nextPoint.getLongitude()+", "+nextPoint.getLatitude());
//      System.out.println(secondNextPoint.getLongitude()+", "+secondNextPoint.getLatitude());
//      System.out.println(newCentre.getLongitude()+", "+newCentre.getLatitude());
//      
//      System.out.println(secondNewCentre.getLongitude()+", "+secondNewCentre.getLatitude());
//      System.out.println(thirdNewCentre.getLongitude()+", "+thirdNewCentre.getLatitude());
//      
//      angle = GeoHelper.getGPSAngle(thirdNewCentre, secondNextPoint);
//      distance = GeoHelper.getDistance(thirdNewCentre, secondNextPoint);
//      System.out.println("The distance is: "+distance+" & angle is: "+angle);
      //System.out.println(afterSecondNextPoint.getLatitude()+"\t"+afterSecondNextPoint.getLongitude());
      //System.out.println(secondNextPoint.getLatitude()+"\t"+secondNextPoint.getLongitude());
      //System.out.println(nextPoint.getLongitude()+", "+nextPoint.getLatitude());
      //System.out.println("The distance with new centre is: "+distnace+" & angle is: "+angle+"\n\n");
//      
//      angle = GeoHelper.getGPSAngle(newCentre, secondNextPoint);
//      distnace = GeoHelper.getDistance(newCentre, secondNextPoint);
//      System.out.println("Current centre Longitude x Latitude is: "+newCentre.getLongitude()+" x "+newCentre.getLatitude());
//      System.out.println("Current point Longitude x Latitude is: "+secondNextPoint.getLongitude()+" x "+secondNextPoint.getLatitude());
//      System.out.println("The distance with new centre is: "+distnace+" & angle is: "+angle+"\n\n");
//      
//      angle = GeoHelper.getGPSAngle( currentCentre, newCentre);
//      distnace = GeoHelper.getDistance(currentCentre, newCentre);
//      System.out.println("New centre Longitude x Latitude is: "+newCentre.getLongitude()+" x "+newCentre.getLatitude());
//      System.out.println("Current centre Longitude x Latitude is: "+currentCentre.getLongitude()+" x "+currentCentre.getLatitude());
//      System.out.println("The distance b/w current and new centre is: "+distnace+" & angle is: "+angle);
//      
//      angle = GeoHelper.getGPSAngle(currentCentre, afterSecondNextPoint);
//      distnace = GeoHelper.getDistance(currentCentre, afterSecondNextPoint);
//      resultantValues.add(currentCentre);
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, 90.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, 150.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, -150.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, -90.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, -30.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, 30.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(currentCentre, allowedEpsilon, 90.0));
//      resultantValues.add(currentCentre);
//      
//      resultantValues.add(newCentre);
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, 90.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, 150.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, -150.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, -90.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, -30.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, 30.0));
//      resultantValues.add(GeoHelper.getPointWithPolarDistance(newCentre, allowedEpsilon, 90.0));
//      resultantValues.add(newCentre);
      
       //IOHelper.writeGridConvertedGPS( resultantValues, "data-tag1937/example-evaluation.txt");
      
      //writeDouglasPositionData(resultantValues, "six-hexagons-s.txt");
      
      //System.out.println("The distance with new centre is: "+distnace+" & angle is: "+angle);
      //resultantValues.add(currentCentre);
      
  }
  
}


//145.40568972804195 x -16.450754609499484
//145.40943624320613 x -16.444530898646242
//145.4083961 x -16.448172
//145.4083961 x -16.448172
//145.4082897 x -16.4481433
//145.4082897 x -16.4481433
//145.4083362 x -16.4481449
//145.4083516 x -16.4481388
//145.4083437 x -16.4481369


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
                
