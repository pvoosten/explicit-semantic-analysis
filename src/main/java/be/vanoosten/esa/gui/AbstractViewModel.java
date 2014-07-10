/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author user
 */
public abstract class AbstractViewModel {
    
    protected AbstractViewModel(){
    }
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    public void addPropertyChangeListener(PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }
    
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue){
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, int oldValue, int newValue){
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue){
        support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
    protected void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue){
        support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
    protected void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue){
        support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
    protected void firePropertyChange(PropertyChangeEvent event){
        support.firePropertyChange(event);
    }
    
    public PropertyChangeListener[] getPropertyChangeListeners(){
        return support.getPropertyChangeListeners();
    }
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName){
        return support.getPropertyChangeListeners(propertyName);
    }
    public boolean hasListeners(String propertyName){
        return support.hasListeners(propertyName);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener){
        support.removePropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener){
        support.removePropertyChangeListener(propertyName, listener);
    }
}
