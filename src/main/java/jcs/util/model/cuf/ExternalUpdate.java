package jcs.util.model.cuf;

/**
 * This interface can be used by ValueModels that support external
 * notifications about changes.<br/>
 * This is mainly usefull to avoid that everything is a wrapped in
 * a ValueModel on its own.
 */
public interface ExternalUpdate
{
    /**
     * Signal this object that portions of its data changed.
     */
    public void signalExternalUpdate();
}
