/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Helper;

import GeoHelper.ClusterStructure;
import GeoHelper.GPSPoint;
import GeoHelper.GeoHelper;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author ghulammurtaza
 */
public class Utility {
    public static int roundedNumber(int number){
        return (number + 50) / 100 * 100;
    }
    
    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static Object copy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }
    
    public static ArrayList<GPSPoint> clusterStructureToGPS(ArrayList<ClusterStructure> clusterList){
        ArrayList<GPSPoint> gpsList = new ArrayList<GPSPoint>();
        for (int i = 0; i < clusterList.size(); i++){
            gpsList.add(clusterList.get(i).clusterCentre);
        }
        return gpsList;
    }
    
    public static ArrayList<GPSPoint> getEquivalentListCompressed(ArrayList<GPSPoint> originalPoints, ArrayList<GPSPoint> approximatedPoints){
      ArrayList<GPSPoint> equivalentSych = new ArrayList<GPSPoint>();
      for (int i = 0; i < approximatedPoints.size(); i++){
          float minDistance = 100000000;
          GPSPoint currentPoint = new GPSPoint();
          GPSPoint appPoint = approximatedPoints.get(i);
          for (int j = 0; j < originalPoints.size(); j++){
              float distance = GeoHelper.getDistance(appPoint, originalPoints.get(j));
              if (distance<minDistance){
                  minDistance = distance;
                  currentPoint = (GPSPoint)Utility.copy(originalPoints.get(j));
              }
          }
          equivalentSych.add(currentPoint);
      }
      return equivalentSych;
  }
}
