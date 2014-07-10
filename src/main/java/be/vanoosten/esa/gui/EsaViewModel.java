/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa.gui;

/**
 *
 * @author user
 */
public final class EsaViewModel extends AbstractViewModel {

    @SuppressWarnings("LeakingThisInConstructor")
    public EsaViewModel() {
        setInputText("Type the input here...");
        setOutputText("Here comes the output");
    }

    private String inputText;

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        if(this.inputText == null ? inputText == null : this.inputText.equals(inputText)) return;
        String old = this.inputText;
        this.inputText = inputText;
        firePropertyChange("inputText", old, inputText);
    }

    private String outputText;

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        if(this.outputText == null ? outputText == null : this.outputText.equals(outputText))return;
        String old = this.outputText;
        this.outputText = outputText;
        firePropertyChange("outputText", old, outputText);
    }

    void runSelectedTool() {
#        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
De data binding werkt niet.
        setOutputText(getInputText());

    }

}
