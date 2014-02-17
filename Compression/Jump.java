/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Compression;

/**
 *
 * @author ghulammurtaza
 */
public class Jump {
    int xJump;
    int yJump;
    int quadrant;
    public Jump(){
        
    }
    public Jump(int xJump, int yJump, int quad){
        this.xJump = xJump;
        this.yJump = yJump;
        this.quadrant = quad;
    }
    
    public int getXjump(){
        return this.xJump;
    }
    
    public int getYjump(){
        return this.yJump;
    }
    
    public int getQuadrant(){
        return this.quadrant;
    }
    
    public void setXjump(int xJump){
        this.xJump = xJump;
    }
    
    public void setYjump(int yJump){
        this.yJump = yJump;
    }
    
    public void setQuadrant(int quadrant){
        this.quadrant = quadrant;
    }
    
}
