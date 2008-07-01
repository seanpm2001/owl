package edu.stanford.smi.protegex.owl.ui.cls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JPanel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protege.widget.Widget;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.ResourceDisplay;
import edu.stanford.smi.protegex.owl.ui.widget.MultiWidgetPropertyWidget;
import edu.stanford.smi.protegex.owl.ui.widget.PropertyWidget;

/**
 * A MultiWidgetPropertyWidget used as main widget on the OWLClassesTab,
 * showing one of various switchable displays (Logic view etc).
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class SwitchableClassDefinitionWidget extends MultiWidgetPropertyWidget {

    private Widget activeWidget;

    private static List registry = new ArrayList();


    static {
        registry.add(new LogicClassDefinitionWidgetType());
        // registry.add(new ClassFormSwitchableType());
        registry.add(new PropertiesClassDefinitionWidgetType());
    }


    protected void createNestedWidgets() {
        String[] slotNames = new String[]{
                Model.Slot.DIRECT_TEMPLATE_SLOTS,
                Model.Slot.DIRECT_SUPERCLASSES,
                Model.Slot.DIRECT_INSTANCES,
                Model.Slot.DIRECT_SUBCLASSES
        };

        // get plugins and add them to the register
        Collection<Class> clses = new ArrayList(PluginUtilities.getClassesWithAttribute("ClassView", "True"));
        for(Class cls : clses){
            try {
                registerSwitchableType((SwitchableType)cls.newInstance());
            } catch (InstantiationException e) {
                Log.getLogger().log(Level.SEVERE, "Exception caught", e);
            } catch (IllegalAccessException e) {
                Log.getLogger().log(Level.SEVERE, "Exception caught", e);
            } catch (ClassCastException e){
                System.err.println("[SwitchableClassDefinitionWidget] plugin " + cls.getName() +
                " must implement SwitchableType - ignored");
            }
        }

        int s = 0;
        for (Iterator it = registry.iterator(); it.hasNext();) {
            SwitchableType type = (SwitchableType) it.next();
            Class cls = type.getWidgetClassType();
            try {
                PropertyWidget widget = (PropertyWidget) cls.newInstance();
                addNestedWidget(widget, slotNames[s++], type.getButtonText(), type.getButtonText());
            }
            catch (Exception ex) {
                System.err.println("[SwitchableClassDefinitionWidget] " + ex);
                Log.getLogger().log(Level.SEVERE, "Exception caught", ex);
            }
        }
    }





    public Class getActiveWidgetClass() {
        if (activeWidget != null) {
            return activeWidget.getClass();
        }
        else {
            SwitchableType type = (SwitchableType) registry.get(0);
            return type.getWidgetClassType();
        }
    }


    protected void initAllPanel(JPanel allPanel, java.util.List widgets) {
        allPanel.setLayout(new BorderLayout());
        if (activeWidget != null) {
            allPanel.add(BorderLayout.CENTER, (Component) activeWidget);
        }
        else {
            allPanel.add(BorderLayout.CENTER, (Component) listWidgets().next());
        }
    }


    public void initialize() {
        super.initialize();
        setAllMode(true);
    }


    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        if (cls.getKnowledgeBase() instanceof OWLModel) {
            RDFSNamedClass namedClsMetaCls = ((OWLModel) cls.getKnowledgeBase()).getOWLNamedClassClass();
            return slot.getName().equals(Model.Slot.DIRECT_SUPERCLASSES) && facet == null &&
                    (namedClsMetaCls.equals(cls) || cls.hasSuperclass(namedClsMetaCls));
        }
        else {
            return false;
        }
    }


    public static Iterator listSwitchableTypes() {
        return registry.iterator();
    }


    public static void registerSwitchableType(SwitchableType type) {
        for (Iterator it = registry.iterator(); it.hasNext();) {
            SwitchableType switchableType = (SwitchableType) it.next();
            if (switchableType.getClass() == type.getClass()) {
                return; // Already there
            }
        }
        registry.add(type);
    }


    public void setActiveType(Class widgetClassType) {
        Iterator it = listWidgets();
        while (it.hasNext()) {
            PropertyWidget widget = (PropertyWidget) it.next();
            if (widget.getClass() == widgetClassType) {
                activeWidget = widget;
                reinitAllPanel();
                revalidate();
                repaint();
                Component comp = this;
                while (comp != null && !(comp instanceof ResourceDisplay)) {
                    comp = comp.getParent();
                }
                if (comp instanceof ResourceDisplay) {
                    ((ResourceDisplay) comp).updateInferredModeOfWidgets();
                }
                return;
            }
        }
    }
    
    @Override
    public void setInstance(Instance newInstance) {
        if (instance != null) {
            instance.removeFrameListener(valueListener);
        }
        for (Iterator it = listWidgets(); it.hasNext();) {
            SlotWidget widget = (SlotWidget) it.next();
            if (getActiveWidgetClass().equals(widget.getClass())) {
            	widget.setInstance(newInstance);
            }
        }
        instance = newInstance;
        
        if (instance != null) {
            instance.addFrameListener(valueListener);
        }
    }
    
    
    
}
