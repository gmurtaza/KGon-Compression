package Constants;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class Constants {
    
    
    /*
     * These are the constants defined for the 4 bits used for encoding
     */
    
    public static final int SAME_KGON = 0;
    public static final int FIRST_NEIGHBOURING_KGON = 1;
    public static final int SECOND_NEIGHBOURING_KGON = 2;
    public static final int THIRD_NEIGHBOURING_KGON = 3;
    public static final int FOURTH_NEIGHBOURING_KGON = 4;
    public static final int FIFTH_NEIGHBOURING_KGON = 5;
    public static final int SIXTH_NEIGHBOURING_KGON = 6;
    public static final int SEVENTH_NEIGHBOURING_KGON = 7;
    public static final int EIGHTH_NEIGHBOURING_KGON = 8;
    public static final int DOUBLING_EPSILON_START_OR_FINISH = 9;
    public static final int HALVING_EPSILON_START_OR_FINISH = 10;
    public static final int CURRENT_EPSILON_START_OR_FINISH = 11;
    public static final int DOUBLING_TIME_START_OR_FINISH = 12;
    public static final int HALVING_TIME_START_OR_FINISH = 13;
    
    /*
     * These are the constants for the codes of epsilon sizes to be used
     */
    
    public static final int CODE_FOR_FIRST_SIZE = 21;
    public static final int CODE_FOR_SECOND_SIZE = 22;
    public static final int CODE_FOR_THIRD_SIZE = 23;
    public static final int CODE_FOR_FOURTH_SIZE = 24;
    public static final int CODE_FOR_FIFTH_SIZE = 25;
    public static final int CODE_FOR_SIXTH_SIZE = 26;
    public static final int CODE_FOR_SEVENTH_SIZE = 27;
    public static final int CODE_FOR_EIGHTH_SIZE = 28;
    public static final int CODE_FOR_CALIBRATION_POINT = 29;
    
    /*
     * These are the actual limits of the steps
     */
    public static final int LIMIT_FOR_FIRST_STEP = 8;
    public static final int LIMIT_FOR_SECOND_STEP = 16;
    public static final int LIMIT_FOR_THIRD_STEP = 32;
    public static final int LIMIT_FOR_FOURTH_STEP = 64;
    public static final int LIMIT_FOR_FIFTH_STEP = 128;
    public static final int LIMIT_FOR_SIXTH_STEP = 256;
    public static final int LIMIT_FOR_SEVENTH_STEP = 512;
    public static final int LIMIT_FOR_EIGHTH_STEP = 1024;
    public static final int LIMIT_FOR_NINTH_STEP = 2048;
    
    /*
     * Intervals with respect to timestamp difference
     */
    
    public static final int LIMIT_FOR_FIRST_TIME_STEP = 300;
    public static final int LIMIT_FOR_SECOND_TIME_STEP = 600;
    public static final int LIMIT_FOR_THIRD_TIME_STEP = 900;
    public static final int LIMIT_FOR_FOURTH_TIME_STEP = 1200;
    public static final int LIMIT_FOR_FIFTH_TIME_STEP = 1500;
    public static final int LIMIT_FOR_SIXTH_TIME_STEP = 1800;
    
    /*
     * These are the codes of timestamping
     */
    public static final int CODE_FOR_FIRST_TIME_SIZE = 30;
    public static final int CODE_FOR_SECOND_TIME_SIZE = 31;
    public static final int CODE_FOR_THIRD_TIME_SIZE = 32;
    public static final int CODE_FOR_FOURTH_TIME_SIZE = 33;
    public static final int CODE_FOR_FIFTH_TIME_SIZE = 34;
    public static final int CODE_FOR_SIXTH_TIME_SIZE = 35;
    
    /*
     * Here, we define the codes for coded interpolation compression
     */
     
    public static final int START_FINISH_OF_STEP_COUNT = 9;
    
    /*
     * Energy constants
     */
    public static final float ONE_BYTE_RADIO_COST = 9;
    public static final float ONE_BYTE_READ_COST_FROM_INTERNAL = 9;
    public static final float UNITS_TO_READ_FROM_EXTERNAL = 9;
    public static final float NUMBER_OF_PAGES_IN_BLOCK = 9;
    public static final float NUMBER_OF_BLOCKS_IN_FLASH = 9;
    
}
