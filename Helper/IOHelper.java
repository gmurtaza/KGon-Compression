package Helper;


import Compression.SummarisedData;
import GeoHelper.ClusterStructure;
import GeoHelper.GPSPoint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class IOHelper {
    
    static public boolean writeToFile(String data, String fileName){
    try{
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
    
      out.write(data);
      
      out.close();
      return true;
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeToFile: " + e.getMessage());
    }
    return false;
  }
    
  static public void writeDouglasPositionData(ArrayList<GPSPoint> positionData, String fileName){
    try{
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
    
      for (int i = 0; i< positionData.size(); i++){
        out.write(positionData.get(i).getLatitude()+","+positionData.get(i).getLongitude()+"\n");
      }
      out.close();
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeDouglasPositionData: " + e.getMessage());
    }
  }
  
  static public ArrayList<GPSPoint> getPositionDataGeoLife( String fileName, String separator) throws ParseException {
    ArrayList<GPSPoint> testMessageList = new ArrayList<GPSPoint>();
    int counter = 0;
    
    try {
      InputStreamReader inp = new InputStreamReader(new FileInputStream(new File(fileName)));
      BufferedReader bReader = new BufferedReader(inp);
      String eachLine = bReader.readLine();
      int i = 0;
      while (eachLine != null) {
        GPSPoint payload = new GPSPoint();
        //System.out.println(eachLine);
      	String[] lineTokens = eachLine.replaceAll(" +", " ").split(separator);
        if (lineTokens.length > 2 && i>5){
        //System.out.println(lineTokens.length);
            Double latitude = (Double.parseDouble(lineTokens[0]));
            Double longitude = (Double.parseDouble(lineTokens[1]));
            Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lineTokens[5]+" " +lineTokens[6]);
            if (!lineTokens[0].equals("0.0000000") && !lineTokens[1].equals("0.0000000")){
              payload.setLongitude(longitude);
              payload.setLatitude(latitude);
              //System.out.println(dt.getTime());
              payload.setTimeStamp(dt);
              testMessageList.add(payload);
            }
            counter++;
        }
      	i++;
        eachLine = bReader.readLine();
      }
    }
    catch (IOException exception) {
      System.err.println(exception);
    }
    return testMessageList;
  }
  
    
  static public void writeClusterGPSPointsWithsize(ArrayList<ClusterStructure> positionData, String fileName){
    try{
      
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      //System.out.println("total number of points: "+positionData.size());
      for (int i = 0; i< positionData.size(); i++){
        out.write(positionData.get(i).clusterCentre.getLatitude()+","+positionData.get(i).clusterCentre.getLongitude()+","+positionData.get(i).totalPoints+","+positionData.get(i).firstTimeStamp.getTime()+","+positionData.get(i).lastTimeStamp.getTime()+','+positionData.get(i).totalPoints+","+positionData.get(i).clusterRadius+"\n");   
      }
      out.close();
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridConvertedGPS: " + e.getMessage());
    }
  }
  
  static public ArrayList<String> getGeoLifeFiles( String folderPath) throws ParseException {
      ArrayList<String> listFiles = new ArrayList<String>();
      try{
          File folder = new File(folderPath);
          File[] listOfFiles = folder.listFiles(); 
          for (int i = 0; i < listOfFiles.length; i++){
              if (listOfFiles[i].isFile()) 
              {
                  if(!(folderPath+"/"+listOfFiles[i].getName()).equals("../../SensorClustering/Geo-Data/000/Trajectory/.DS_Store"))
                    listFiles.add(folderPath+"/"+listOfFiles[i].getName());
              }
          }
      }catch(Exception exception) {
        System.err.println(exception);
      }
      return listFiles;
  }
  
  static public void writeGridConvertedGPS(ArrayList<GPSPoint> positionData, String fileName){
    try{
      
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      //System.out.println("total number of points: "+positionData.size());
      for (int i = 0; i< positionData.size(); i++){
        out.write(positionData.get(i).getLatitude()+","+positionData.get(i).getLongitude()+"\n");   
      }
      out.close();
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridConvertedGPS: " + e.getMessage());
    }
  }
  
  static public void writeGridConvertedGPSThis(ArrayList<GPSPoint> positionData, String fileName){
    try{
      
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      //System.out.println("total number of points: "+positionData.size());
      for (int i = 0; i< positionData.size(); i++){
        out.write(positionData.get(i).getLatitude()+","+positionData.get(i).getLongitude()+","+positionData.get(i).getTimeStamp().getTime()+"\n");   
      }
      out.close();
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridConvertedGPS: " + e.getMessage());
    }
  }
  
  static public void writeGridPositionData(GPSPoint firstPosition, ArrayList<Integer> positionData, String fineName){
    try{
      FileWriter fstream = new FileWriter(fineName);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(firstPosition.getLatitude()+","+firstPosition.getLongitude()+"\n");
      for (int i = 0; i< positionData.size(); i++){
        out.write(positionData.get(i)+"\n");
      }
      out.close();
    } catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridPositionData: " + e.getMessage());
    }
  }
  
  static public void writeSpeedDataDouble(ArrayList<GPSPoint> positionData, ArrayList<Double> speedData,  String fineName){
    try{
      FileWriter fstream = new FileWriter(fineName);
      BufferedWriter out = new BufferedWriter(fstream);
      DecimalFormat df = new DecimalFormat("#");
      for (int i = 0; i< speedData.size(); i++){
              Date currentDate = positionData.get(i).getTimeStamp();
              out.write(speedData.get(i)+"\t"+currentDate.getTime() +"\n");
          
      }
      out.close();
    } catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridPositionData: " + e.getMessage());
    }
  }
  static public void writeGridPositionDataDouble(GPSPoint firstPosition, ArrayList<Integer> positionData, String fineName){
    try{
      FileWriter fstream = new FileWriter(fineName);
      BufferedWriter out = new BufferedWriter(fstream);
      DecimalFormat df = new DecimalFormat("#");
      out.write(firstPosition.getLatitude()+","+firstPosition.getLongitude()+"\n");
      for (int i = 0; i< positionData.size(); i++){
          if(positionData.get(i)>0 && positionData.get(i)<10){
            out.write(df.format(positionData.get(i))+"\n");
          }else{
              out.write(positionData.get(i)+"\n");
          }
      }
      out.close();
    } catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridPositionData: " + e.getMessage());
    }
  }
  
  
  static public void writeSEDDataDouble(ArrayList<Double> positionData, String fineName){
    try{
      FileWriter fstream = new FileWriter(fineName);
      BufferedWriter out = new BufferedWriter(fstream);
      DecimalFormat df = new DecimalFormat("#");
      for (int i = 0; i< positionData.size(); i++){
          
              out.write(positionData.get(i)+"\n");
          
      }
      out.close();
    } catch (Exception e){//Catch exception if any
      System.err.println("Error writeGridPositionData: " + e.getMessage());
    }
  }
  
  static public boolean writeListToFile(ArrayList<GPSPoint> positionData, String fileName){
    try{
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      
      for (int i = 0; i< positionData.size(); i++){
        out.write(positionData.get(i).getLatitude()+","+positionData.get(i).getLongitude()+"\n");
      }
      
      out.close();
      return true;
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeListToFile: " + e.getMessage());
    }
    return false;
  }

  static public boolean writeSummaisedData(ArrayList<SummarisedData> summarisedPositionData, String fileName){
    try{
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
    
      for (int i = 0; i< summarisedPositionData.size(); i++){
        out.write(summarisedPositionData.get(i).getDistance()+","+summarisedPositionData.get(i).getAngle()+"\n");
      }
      out.close();
      return true;
    }catch (Exception e){//Catch exception if any
      System.err.println("Error writeSummaisedData:  " + e.getMessage());
    }
    return false;
  }

  static public ArrayList<GPSPoint> getPositionData( String fileName, String separator) {
    ArrayList<GPSPoint> testMessageList = new ArrayList<GPSPoint>();
    int counter = 0;
    
    try {
      InputStreamReader inp = new InputStreamReader(new FileInputStream(new File(fileName)));
      BufferedReader bReader = new BufferedReader(inp);
      String eachLine = bReader.readLine();
      while (eachLine != null) {
        GPSPoint payload = new GPSPoint();
        //System.out.println(eachLine);
      	String[] lineTokens = eachLine.replaceAll(" +", " ").split(separator);
        if (lineTokens.length > 6){
        //System.out.println(lineTokens.length);
            Double longitude = (Double.parseDouble(lineTokens[5]));
            Double latitude = (Double.parseDouble(lineTokens[6]));
            Date dt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(lineTokens[13]+" "+lineTokens[15]);
            if (!lineTokens[5].equals("0.0000000") && !lineTokens[6].equals("0.0000000")){
              payload.setLongitude(longitude);
              payload.setLatitude(latitude);
              payload.setTimeStamp(dt);
              testMessageList.add(payload);
            }
            counter++;
        }
      	
        eachLine = bReader.readLine();
      }
    }
    catch (Exception exception) {
      System.err.println("Exception thrown when sending packets. Exiting.");
      System.err.println(exception);
    }
    return testMessageList;
  }
  
  
  static public ArrayList<GPSPoint> getPositionDataLatest( String fileName, String separator) throws ParseException {
    ArrayList<GPSPoint> testMessageList = new ArrayList<GPSPoint>();
    int counter = 0;
    
    try {
      InputStreamReader inp = new InputStreamReader(new FileInputStream(new File(fileName)));
      BufferedReader bReader = new BufferedReader(inp);
      String eachLine = bReader.readLine();
      while (eachLine != null) {
        GPSPoint payload = new GPSPoint();
        //System.out.println(eachLine);
      	String[] lineTokens = eachLine.replaceAll(" +", " ").split(separator);
        if (lineTokens.length > 2){
        //System.out.println(lineTokens.length);
            Double longitude = (Double.parseDouble(lineTokens[0]));
            Double latitude = (Double.parseDouble(lineTokens[1]));
            Date dt = new SimpleDateFormat("yyyyMMddHHmmss").parse(lineTokens[2]);
            if (!lineTokens[0].equals("0.0000000") && !lineTokens[1].equals("0.0000000")){
              payload.setLongitude(longitude);
              payload.setLatitude(latitude);
              //System.out.println(dt.getTime());
              payload.setTimeStamp(dt);
              testMessageList.add(payload);
            }
            counter++;
        }
      	
        eachLine = bReader.readLine();
      }
    }
    catch (IOException exception) {
      System.err.println("Exception thrown when sending packets. Exiting.");
      System.err.println(exception);
    }
    return testMessageList;
  }

  
  
  static public ArrayList<Date> getTimeData( String fileName, String separator) {
    ArrayList<Date> testDateTimeList = new ArrayList<Date>();
    
    try {
      InputStreamReader inp = new InputStreamReader(new FileInputStream(new File(fileName)));
      BufferedReader bReader = new BufferedReader(inp);
      String eachLine = bReader.readLine();
      while (eachLine != null) {
        //DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        String[] lineTokens = eachLine.replaceAll(" +", " ").split(separator);
        //DateTime dt = formatter.parseDateTime(lineTokens[2]+" "+lineTokens[4]);
        Date dt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(lineTokens[13]+" "+lineTokens[15]);
        //System.out.println(dt.getTime());
        if (!lineTokens[5].equals("0.0000000") && !lineTokens[6].equals("0.0000000")){
            testDateTimeList.add(dt);
        }
        eachLine = bReader.readLine();
      }
    }
    catch (Exception exception) {
      System.err.println("Exception thrown when sending packets. Exiting.");
      System.err.println(exception);
    }
    return testDateTimeList;
  }
  
  static public ArrayList<Date> getTimeDataLatest( String fileName, String separator) {
    ArrayList<Date> testDateTimeList = new ArrayList<Date>();
    
    try {
      InputStreamReader inp = new InputStreamReader(new FileInputStream(new File(fileName)));
      BufferedReader bReader = new BufferedReader(inp);
      String eachLine = bReader.readLine();
      while (eachLine != null) {
        //DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        String[] lineTokens = eachLine.replaceAll(" +", " ").split(separator);
        //DateTime dt = formatter.parseDateTime(lineTokens[2]+" "+lineTokens[4]);
        Date dt = new SimpleDateFormat("yyyyMMddHHmmss").parse(lineTokens[2]);
        //System.out.println(dt.getTime());
        if (!lineTokens[0].equals("0.0000000") && !lineTokens[1].equals("0.0000000")){
            testDateTimeList.add(dt);
        }
        eachLine = bReader.readLine();
      }
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    return testDateTimeList;
  }

public static int log(float x, int base)
{
    return (int)(Math.log(x) / Math.log(base));
}
  
    
}
