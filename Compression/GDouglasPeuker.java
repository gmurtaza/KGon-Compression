package Compression;


import GeoHelper.GPSPoint;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class GDouglasPeuker {
    
   /* source[] Input coordinates in GLatLngs   */
   /* kink in metres, kinks above this depth kept  */
   /* kink depth is the height of the triangle abc where a-b and b-c are two consecutive line segments */
    static public ArrayList<GPSPoint> douglasPeucker (ArrayList<GPSPoint> source,float kink)
    {
        Integer n_source, n_stack, n_dest, start, end, i, sig;    
        double dev_sqr, max_dev_sqr, band_sqr;
        double x12, y12, d12, x13, y13, d13, x23, y23, d23;
        double F = ((Math.PI / 180.0) * 0.5 );
        ArrayList index = new ArrayList(); /* aray of indexes of source points to include in the reduced line */
        ArrayList sig_start = new ArrayList(); /* indices of start & end of working section */
        ArrayList sig_end = new ArrayList();  

        /* check for simple cases */

        if ( source.size() < 3 ) 
            return(source);    /* one or two points */

        /* more complex case. initialize stack */

        n_source = source.size();
        band_sqr = kink * 360.0 / (2.0 * Math.PI * 6378137.0);  /* Now in degrees */
        band_sqr *= band_sqr;
        n_dest = 0;
        sig_start.add(0);
        sig_end.add( n_source-1);
        n_stack = 1;

        /* while the stack is not empty  ... */
        while ( n_stack > 0 ){

            /* ... pop the top-most entries off the stacks */

            start = (Integer)sig_start.get(n_stack-1);
            end = (Integer)sig_end.get(n_stack-1);
            n_stack--;

            if ( (end - start) > 1 ){  /* any intermediate points ? */        

                    /* ... yes, so find most deviant intermediate point to
                           either side of line joining start & end points */                                   

                x12 = (source.get(end).getLongitude() - source.get(start).getLongitude());
                y12 = (source.get(end).getLatitude() - source.get(start).getLatitude());
                if (Math.abs(x12) > 180.0) 
                    x12 = 360.0 - Math.abs(x12);
                x12 *= Math.cos(F * (source.get(end).getLatitude()+ source.get(start).getLatitude()));/* use avg lat to reduce lng */
                d12 = (x12*x12) + (y12*y12);

                for ( i = start + 1, sig = start, max_dev_sqr = -1.0; i < end; i++ ){                                    

                    x13 = (source.get(i).getLongitude() - source.get(start).getLongitude());
                    y13 = (source.get(i).getLatitude() - source.get(start).getLatitude());
                    if (Math.abs(x13) > 180.0) 
                        x13 = 360.0 - Math.abs(x13);
                    x13 *= Math.cos (F * (source.get(i).getLatitude() - source.get(start).getLatitude()));
                    d13 = (x13*x13) + (y13*y13);

                    x23 = (source.get(i).getLongitude() - source.get(start).getLongitude());
                    y23 = (source.get(i).getLatitude() - source.get(start).getLatitude());
                    if (Math.abs(x23) > 180.0) 
                        x23 = 360.0 - Math.abs(x23);
                    x23 *= Math.cos(F * (source.get(i).getLatitude() - source.get(end).getLatitude()));
                    d23 = (x23*x23) + (y23*y23);

                    if ( d13 >= ( d12 + d23 ) )
                        dev_sqr = d23;
                    else if ( d23 >= ( d12 + d13 ) )
                        dev_sqr = d13;
                    else
                        dev_sqr = (x13 * y12 - y13 * x12) * (x13 * y12 - y13 * x12) / d12;// solve triangle

                    if ( dev_sqr > max_dev_sqr  ){
                        sig = i;
                        max_dev_sqr = dev_sqr;
                    }
                }

                if ( max_dev_sqr < band_sqr ){   /* is there a sig. intermediate point ? */
                    /* ... no, so transfer current start point */
                    if(index.size()>n_dest)
                      index.set(n_dest, start);
                    else
                      index.add(start);
                    n_dest++;
                }
                else{
                    /* ... yes, so push two sub-sections on stack for further processing */
                    n_stack++;
                    if(sig_start.size()>n_stack-1)
                      sig_start.set(n_stack-1, sig);
                    else
                      sig_start.add(sig);
                    if(sig_end.size()>n_stack-1)
                      sig_end.set(n_stack-1, end);
                    else
                      sig_end.add(end);
                    n_stack++;
                    if(sig_start.size()>n_stack-1)
                      sig_start.set(n_stack-1, start);
                    else
                      sig_start.add(start);
                    if(sig_end.size()>n_stack-1)
                      sig_end.set(n_stack-1, sig);
                    else
                      sig_end.add(sig);

                }
            }
            else{
                    /* ... no intermediate points, so transfer current start point */
                    if(index.size()>n_dest)
                      index.set(n_dest, start);
                    else
                      index.add(start);

                    n_dest++;
            }
        }

        /* transfer last point */
        if(index.size()>n_dest)
          index.set(n_dest, n_source-1);
        else
          index.add(n_source-1);
        n_dest++;

        /* make return array */
        ArrayList<GPSPoint> r = new ArrayList<GPSPoint>();
        for( i=0; i < n_dest; i++)
            r.add(source.get((Integer)index.get(i)));
        return r;
    
    }

    
}
