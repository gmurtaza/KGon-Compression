/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Compression;

import java.util.Date;

/**
 *
 * @author ghulammurtaza
 */
public class IndividualDistance {
    Date timePointAdded;
    double distance;
    
    public IndividualDistance(Date timePointAdded, double distance){
        this.timePointAdded = timePointAdded;
        this.distance = distance;
    }
    
    public Date getDate(){
        return this.timePointAdded;
    }
    public double getDistance(){
        return this.distance;
    }
}
