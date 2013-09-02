package EnergyModel;
import Constants.Constants;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghulammurtaza
 */
public class EnergyUtility {
    /*
     * This function returns the amount of data that can be transferred in thi energy budget in bytes
     */
    float totalDataTransferLimit(float totalEnergy){
        
        return totalEnergy/energyToSendOneByte();
    }
    /*
     * This function returns the remaining energy after some enrgy usage operation
     */
    float updateEnergyUsage(float totalEnergy, float usage){
        return totalEnergy - usage;
    }
    /*
     * This function calculates the energy used in a particular operatoin given the amount of data involved
     */
    float calculateEnergyUsage(float data, String operation){
        if (operation.equals("radio")){
            return data*Constants.ONE_BYTE_RADIO_COST;
        }else if(operation.equals("read")){
            return data*Constants.ONE_BYTE_READ_COST_FROM_INTERNAL;
        }
        return 0;
    }
    
    float energyToSendOneByte(){
        return Constants.ONE_BYTE_RADIO_COST+ Constants.ONE_BYTE_READ_COST_FROM_INTERNAL;
    }
}
