package GeoHelper;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class GPSPoint {

  Double longitude;
  Double latitude;

  public GPSPoint(){
  }

  public GPSPoint(Double longitude, Double latitude){
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public Double getLatitude(){
    return latitude;
  }

  public Double getLongitude(){
    return longitude;
  }

  public void setLatitude(Double latitude){
    this.latitude = latitude;
  }

  public void setLongitude(Double longitude){
    this.longitude = longitude;
  }
}
