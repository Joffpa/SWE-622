/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pa2;

/**
 *
 * @author BBoss
 */
public enum Enums {
    SERVER("Server");     
    
    private final String value;
    
    public String val(){
        return this.value;
    }
    
    Enums(String val){
        this.value = val;
    }
}
