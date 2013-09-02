package Compression;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class SummarisedData{
  Double angle;
  float distance;
  public SummarisedData(Double angle, float distance){
    this.angle = angle;
    this.distance = distance;
  }
  public Double getAngle(){
    return this.angle;
  }
  public float getDistance(){
    return this.distance;
  }

  public void setAngle(Double angle){
    this.angle = angle;
  }

  public void setDistance(float distance){
    this.distance = distance;
  }
}
