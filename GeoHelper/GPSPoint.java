package GeoHelper;

import java.util.Date;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class GPSPoint implements java.io.Serializable {

  Double longitude;
  Double latitude;
  Date timestamp;

  public GPSPoint(){
  }

  public GPSPoint(Double longitude, Double latitude){
    this.longitude = longitude;
    this.latitude = latitude;
  }
  
  public GPSPoint(Double longitude, Double latitude, Date timestamp){
    this.longitude = longitude;
    this.latitude = latitude;
    this.timestamp = timestamp;
  }
  
  public Date getTimeStamp(){
      return this.timestamp;
  }
  
  public Double getLatitude(){
    return latitude;
  }

  public Double getLongitude(){
    return longitude;
  }
  
  public void setTimeStamp(Date timestamp){
      this.timestamp = timestamp;
  }
  public void setLatitude(Double latitude){
    this.latitude = latitude;
  }

  public void setLongitude(Double longitude){
    this.longitude = longitude;
  }
}
