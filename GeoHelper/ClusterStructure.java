/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GeoHelper;

import Helper.Utility;
import java.util.Date;

/**
 *
 * @author ghulammurtaza
 */
public class ClusterStructure {
    public int totalPoints;
    public float clusterRadius;
    public GPSPoint clusterCentre;
    public Date firstTimeStamp;
    public Date lastTimeStamp;
    
    public ClusterStructure(int totalPoints,float clusterRadius, GPSPoint clusterCentre, Date lastTimeStamp, Date firstTimeStamp){
        this.totalPoints = totalPoints;
        this.clusterCentre = clusterCentre;
        this.clusterRadius = clusterRadius;
        this.lastTimeStamp = lastTimeStamp;
        this.firstTimeStamp = firstTimeStamp;
    }
    
    public void setTotalPoints(int totalPoints){
        this.totalPoints = totalPoints;
    }
    
    public void setClusterRadius(float clusterRadius){
        this.clusterRadius = clusterRadius;
    }
    
    public void setClusterCentre(GPSPoint centre){
        this.clusterCentre = (GPSPoint)Utility.copy(centre);
    }
    
    public void setFirstTimeStamp(Date firstTime){
        this.firstTimeStamp = (Date)Utility.copy(firstTime);
    }
    
    public void setLastTimeStamp(Date lastTime){
        this.lastTimeStamp = (Date)Utility.copy(lastTime);;
    }
}

