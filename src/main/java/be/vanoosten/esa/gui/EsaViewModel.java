/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa.gui;

import be.vanoosten.esa.EnwikiFactory;
import be.vanoosten.esa.WikiFactory;
import be.vanoosten.esa.tools.ConceptVector;
import be.vanoosten.esa.tools.Vectorizer;
import java.io.IOException;
import java.util.Iterator;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author user
 */
public final class EsaViewModel extends AbstractViewModel {

    private final WikiFactory factory;

    @SuppressWarnings("LeakingThisInConstructor")
    public EsaViewModel() {
        setInputText("Type the input here...");
        setOutputText("Here comes the output");
        factory = new EnwikiFactory();
    }

    private String inputText;

    public String getInputText() {
        return inputText == null ? "" : inputText;
    }

    public void setInputText(String inputText) {
        if (this.inputText == null ? inputText == null : this.inputText.equals(inputText)) {
            return;
        }
        String old = this.inputText;
        this.inputText = inputText;
        firePropertyChange("inputText", old, inputText);
    }

    private String outputText;

    public String getOutputText() {
        return outputText == null ? "" : outputText;
    }

    public void setOutputText(String outputText) {
        if (this.outputText == null ? outputText == null : this.outputText.equals(outputText)) {
            return;
        }
        String old = this.outputText;
        this.outputText = outputText;
        firePropertyChange("outputText", old, outputText);
    }

    void runSelectedTool() {
        // eerst voor enwiki, later andere wikis toevoegen
        // Eerst echo, dan andere tools toevoegen, met factory.

        // conceptvector tonen
        setOutputText("Even geduld...");
        try {
            Vectorizer vectorizer = factory.getOrCreateVectorizer();
            StringBuilder out = new StringBuilder();
            ConceptVector vector = vectorizer.vectorize(getInputText());
            for (Iterator<String> it = vector.topConcepts(100); it.hasNext();) {
                String ccpt = it.next();
                out.append(ccpt).append("\n");
            }
            setOutputText(out.toString());
        } catch (ParseException | IOException ex) {
            setOutputText(ex.toString());
        }
    }
}
