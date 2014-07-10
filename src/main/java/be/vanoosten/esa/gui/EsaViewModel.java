/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa.gui;

import java.beans.PropertyChangeSupport;

/**
 *
 * @author user
 */
public class EsaViewModel {
        
    private String inputText;

    @SuppressWarnings("LeakingThisInConstructor")
    public EsaViewModel() {

    }
    public String getInputText(){
        return inputText;
    }
    public void setInputText(String inputText){
        this.inputText = inputText;
    }
    
    private String outputText;
    public String getOutputText(){
        return outputText;
    }
    public void setOutputText(String outputText){
        String oldOutputText = this.outputText;
    }
    

    void runSelectedTool() {
#        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
