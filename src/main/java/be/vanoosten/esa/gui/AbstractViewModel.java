package be.vanoosten.esa.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Philip van Oosten
 */
public abstract class AbstractViewModel {

    protected AbstractViewModel() {
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void firePropertyChange(String propertyName, int oldValue, int newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    protected final void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    protected final void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        support.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    protected final void firePropertyChange(PropertyChangeEvent event) {
        support.firePropertyChange(event);
    }

    public final PropertyChangeListener[] getPropertyChangeListeners() {
        return support.getPropertyChangeListeners();
    }

    public final PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return support.getPropertyChangeListeners(propertyName);
    }

    public final boolean hasListeners(String propertyName) {
        return support.hasListeners(propertyName);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }
}
