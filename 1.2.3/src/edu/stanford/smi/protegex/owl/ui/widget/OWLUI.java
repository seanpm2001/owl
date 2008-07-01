package edu.stanford.smi.protegex.owl.ui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.net.NoRouteToHostException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.action.Copy;
import edu.stanford.smi.protege.action.Cut;
import edu.stanford.smi.protege.action.InsertUnicodeCharacterAction;
import edu.stanford.smi.protege.action.Paste;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.Finder;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ExtensionFilter;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.PopupMenuMouseListener;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.widget.ClsesTab;
import edu.stanford.smi.protege.widget.FormWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protege.widget.SlotsTab;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.owl.database.OWLDatabaseModel;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.project.OWLProject;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.actions.AbstractOWLModelAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.results.HostResourceDisplay;

/**
 * A collection of global utilities for OWL user interface components.
 * <p/>
 * Note: as of 3.2 beta, much of the functionality was moved into ProtegeUI and its
 * helper classes.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class OWLUI {

    /**
     * @deprecated use constant from AbstractOWLModelAction
     */
    public static String CODE_MENU = AbstractOWLModelAction.CODE_MENU;

    public final static String CONSTRAINT_CHECKING = "OWL-CONSTRAINT-CHECKING";

    private final static String DRAG_AND_DROP = "OWL-DRAG-AND-DROP-DISABLED";

    private static final String EXTERNAL_RESOURCES = "OWL-EXTERNAL-RESOURCES-ENABLED";

    /**
     * @deprecated use constant from AbstractOWLModelAction
     */
    public static String OWL_MENU = AbstractOWLModelAction.OWL_MENU;

    /**
     * @deprecated use constant from AbstractOWLModelAction
     */
    public static final String TOOLS_MENU = AbstractOWLModelAction.TOOLS_MENU;

    private static OWLToolTipGenerator toolTipGenerator;

    public final static int WIZARD_HELP_HEIGHT = 160;


    /**
     * Select a class or other resource - works even when the caller is in a dialog.
     * If no host is supplied, the TabWidgets will be searched for an appropriate host.
     *
     * @param r    the resource to select in the interface
     * @param host (optional) the host resource display on which you want
     *             to select the Resource
     */
    public static void selectResource(RDFResource r, HostResourceDisplay host) {
        ProjectView view = ProtegeUI.getProjectView(r.getProject());
        if (host == null) {
            for (Iterator it = view.getTabs().iterator(); it.hasNext();) {
                Object tab = it.next();
                if (tab instanceof HostResourceDisplay) {
                    if (((HostResourceDisplay) tab).displayHostResource(r)) {
                        view.setSelectedTab((TabWidget) tab);
                        ((JComponent) tab).requestFocusInWindow();
                        return;
                    }
                }
            }
        }
        else if (host.displayHostResource(r)) {
            if (host instanceof TabWidget) {
                view.setSelectedTab((TabWidget) host);
                ((JComponent) host).requestFocusInWindow();
            }
        }
    }


    /**
     * Add a context menu containing Cut, Copy, Paste and InsertUnicode
     *
     * @param textComponent Add a context menu to this component
     */
    public static void addCopyPastePopup(JTextComponent textComponent) {
        textComponent.addMouseListener(new PopupMenuMouseListener(textComponent) {
            protected JPopupMenu getPopupMenu() {
                JPopupMenu popup = new JPopupMenu();
                popup.add(new Cut(false));
                popup.add(new Copy(false));
                popup.add(new Paste(false));
                popup.add(new InsertUnicodeCharacterAction());
                return popup;
            }


            protected void setSelection(JComponent jComponent, int i, int i1) {
                jComponent.requestFocus();
            }
        });
    }

    public static JButton addFrameTreeFinderButton(Finder finder, Action action) {
        JToolBar toolBar = (JToolBar) finder.getComponent(1);
        if (toolBar.getComponentCount() == 1) {
            toolBar.addSeparator();
        }
        return ComponentFactory.addToolBarButton(toolBar, action);
    }


    public static JFileChooser createJFileChooser(String fileDescription, String suffix) {
        JFileChooser chooser = ComponentFactory.createFileChooser(fileDescription, suffix);
        chooser.setFileFilter(new ExtensionFilter(suffix, fileDescription));
        return chooser;
    }


    public static JComponent createHelpPanel(String text, String title) {
        return createHelpPanel(text, title, -1);
    }


    public static JComponent createHelpPanel(String text, String title, int maxHeight) {
        Icon icon = OWLIcons.getImageIcon(OWLIcons.HELP_LOGO);
        return createHelpPanel(text, title, maxHeight, icon);
    }


    public static JComponent createHelpPanel(String text, String title, int maxHeight, Icon icon) {
        JPanel helpPanel = new JPanel(new BorderLayout());
        String prefix = "<HTML><BODY>";
        if (title != null) {
            prefix += "<h4>" + title + "</h4>";
        }
        final JLabel label = new JLabel(prefix + text, icon, JLabel.LEFT);
        //JScrollPane scrollPane = new JScrollPane(label, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPane.getViewport().setBackground(new Color(255, 255, 180));
        //scrollPane.getViewport().setMaximumSize(new Dimension(200, 1000));
        label.setIconTextGap(16);
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        helpPanel.setBackground(new Color(255, 255, 180));
        helpPanel.add(BorderLayout.CENTER, label);
        helpPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        int width = label.getPreferredSize().width;
        int height = label.getPreferredSize().height + 30;
        if (maxHeight > 0) {
            height = maxHeight;
        }
        helpPanel.setPreferredSize(new Dimension(width, height));
        helpPanel.setMaximumSize(new Dimension(width, height));
        return helpPanel;
    }


    public static JToolBar createToolBar() {
        JToolBar bar = new JToolBar();
        bar.setOpaque(false);
        bar.setRollover(true);
        bar.setFloatable(false);
        bar.setBorderPainted(false);
        bar.setBorder(null);
        return bar;
    }


    /**
     * Descends into the component tree of a given Container and returns the first
     * sub-component of a given Java class.
     *
     * @param container      the Container to start looking into
     * @param componentClass the component class to look for
     * @return an instanceof componentClass or null if none was found
     */
    public static Component findComponent(Container container, Class componentClass) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component comp = container.getComponent(i);
            if (componentClass.isAssignableFrom(comp.getClass())) {
                return comp;
            }
            if (comp instanceof Container) {
                Component found = findComponent((Container) comp, componentClass);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }


    public static int getConfirmationThreshold(OWLModel owlModel) {
        return 200;
    }


    private static OWLModel getOWLModel(Collection allowedClses) {
        Cls cls = (Cls) CollectionUtilities.getFirstItem(allowedClses);
        return (OWLModel) cls.getKnowledgeBase();
    }


    public static String getOWLToolTipText(RDFSClass aClass) {
        return getOWLToolTipText((RDFResource) aClass);
    }

    public static String getOWLToolTipText(RDFResource res) {
        if (toolTipGenerator != null) {
            return toolTipGenerator.getToolTipText(res);
        }
        System.out.println("LOST GENERATOR");
        return null;
    }

    public static OWLToolTipGenerator getOWLToolTipGenerator() {
        return toolTipGenerator;
    }


    public static SlotWidget getSiblingSlotWidget(Component widget, Slot slot) {
        Component c = widget.getParent();
        while (c != null && !(c instanceof FormWidget)) {
            c = c.getParent();
        }
        if (c instanceof FormWidget) {
            FormWidget formWidget = (FormWidget) c;
            return formWidget.getSlotWidget(slot);
        }
        return null;
    }


    public static String getWidgetLabel(Slot slot) {
        if (slot instanceof RDFProperty) {
            String name = slot.getBrowserText();
            return name;
        }
        return StringUtilities.symbolToLabel(slot.getBrowserText());
    }


    /**
     * @deprecated
     */
    public static void handleError(Throwable t) {
        handleError(null, t);
    }


    public static void handleError(OWLModel owlModel, Throwable t) {  
        if (!(handleSQLException(owlModel, t)  ||
                handleConnectionLostException(owlModel, t))) {
            handleDefaultException(owlModel, t);
        }
    }
    
    private static void handleDefaultException(OWLModel owlModel, Throwable t) {
        Log.getLogger().log(Level.SEVERE, "Exception caught", t);
        ProtegeUI.getModalDialogFactory().
            showErrorMessageDialog(owlModel,
                                   "Internal Error: " + t +
                                   "\nPlease see Java console for details, and possibly report" +
                                   "\nthis on our OWL mailing lists." +
                                   "\nhttp://protege.stanford.edu/community/lists.html" +
                                   "\nYour ontology may now no longer be in a consistent state, and" +
                                   "\nyou may want to save this version under a different name.",
                                   "Internal Protege-OWL Error");
    }


    private static boolean handleSQLException(OWLModel owlModel, Throwable t) {
    	//TODO: find a nicer way to handle SQL exceptions  
    	Throwable sqlEx = t;
   	 	boolean foundSqlEx = false;
    	
    	while (!foundSqlEx && sqlEx != null) {
    		if (sqlEx instanceof SQLException) {
             	if (sqlEx.getMessage()!=null && sqlEx.getMessage().contains("Lock")) {
             		ProtegeUI.getModalDialogFactory().showErrorMessageDialog(owlModel,
             				"Database table is currently locked by a different user." +
             				"\nPlease retry the operation later.", "Locked ontology");
             	} else {
             		ProtegeUI.getModalDialogFactory().showErrorMessageDialog(owlModel,
     						"A database error has occured: " + sqlEx +
     						"\nPlease see Java console for details, and possibly report" +
                             "\nthis on our OWL mailing lists." +
                             "\nhttp://protege.stanford.edu/community/lists.html",
                             "Database Error");
             	}
    			
             	foundSqlEx = true;
             	break;
    		}
    		sqlEx = sqlEx.getCause();
    	}

    	return foundSqlEx;	
    }
    
    private static long lastConnectFailureTime = -1;
    private static long CONNECT_NOTIFICATION_TIMEOUT = 30000; // 30 seconds
    private static boolean handleConnectionLostException(OWLModel owlModel, Throwable t) {
        for (; t!= null; t = t.getCause()) {
            if (t instanceof java.net.ConnectException || 
                    t instanceof java.rmi.ConnectException || 
                    t instanceof NoRouteToHostException) {
                if (lastConnectFailureTime == -1 || 
                        lastConnectFailureTime - System.currentTimeMillis() < CONNECT_NOTIFICATION_TIMEOUT) {
                    ProtegeUI.getModalDialogFactory().showErrorMessageDialog(owlModel,
                                                                            "Connection Problem - could be that the server is down or a problem with the network.\n" +
                                                                            "See the console for details", 
                                                                            "Connection failure");
                    Log.getLogger().log(Level.WARNING, "Connection failure", t);
                }
                else {
                    Log.getLogger().warning("Recurring connection problem" + t);
                }
                lastConnectFailureTime = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }
    
    public static boolean isConfirmationNeeded(OWLModel owlModel) {
        return owlModel instanceof OWLDatabaseModel || owlModel.getProject().isMultiUserClient();
    }


    /**
     * @deprecated
     */
    public static boolean isConfirmed(Project project, boolean value) {
        return isConfirmed((OWLModel) project.getKnowledgeBase(), value);
    }


    public static boolean isConfirmed(OWLModel owlModel, boolean value) {
        if (value) {
            return ProtegeUI.getModalDialogFactory().showConfirmDialog(owlModel,
                                                                       "Warning: This operation could potentially take very long.\nAre you sure you want to perform it?",
                                                                       "Confirm Action");
        }
        return true;
    }


    public static boolean isConstraintChecking(OWLModel owlModel) {
        return !Boolean.FALSE.equals(owlModel.getOWLProject().getSettingsMap().getBoolean(CONSTRAINT_CHECKING));
    }


    public static boolean isDragAndDropSupported(OWLModel owlModel) {
        OWLProject project = owlModel.getOWLProject();
        return !Boolean.FALSE.equals(project.getSettingsMap().getBoolean(DRAG_AND_DROP));
    }


    public static boolean isExternalResourcesSupported(OWLModel owlModel) {
        OWLProject project = owlModel.getOWLProject();
        return Boolean.TRUE.equals(project.getSettingsMap().getBoolean(EXTERNAL_RESOURCES));
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFSClass pickConcreteClass(OWLModel owlModel, String label) {
        return ProtegeUI.getSelectionDialogFactory().selectClass(null, owlModel, label);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFSClass pickConcreteClass(OWLModel owlModel, Collection allowedClasses) {
        return ProtegeUI.getSelectionDialogFactory().selectClass(null, owlModel, allowedClasses);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFSClass pickConcreteClass(OWLModel owlModel, Collection allowedClasses, String label) {
        return ProtegeUI.getSelectionDialogFactory().selectClass(null, owlModel, allowedClasses, label);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static Collection pickRDFProperties(Collection properties, String title) {
        if (properties.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        else {
            OWLModel owlModel = ((RDFResource) properties.iterator().next()).getOWLModel();
            return ProtegeUI.getSelectionDialogFactory().selectResourcesFromCollection(null, owlModel, properties, title);
        }
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFProperty pickRDFProperty(Collection allowedProperties, String title) {
        if (allowedProperties.isEmpty()) {
            return null;
        }
        else {
            OWLModel owlModel = ((RDFResource) allowedProperties.iterator().next()).getOWLModel();
            return ProtegeUI.getSelectionDialogFactory().selectProperty(null, owlModel, allowedProperties, title);
        }
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFResource pickRDFResource(Collection allowedClasses) {
        if (allowedClasses.isEmpty()) {
            return null;
        }
        else {
            OWLModel owlModel = ((RDFResource) allowedClasses.iterator().next()).getOWLModel();
            return ProtegeUI.getSelectionDialogFactory().selectResourceByType(null, owlModel, allowedClasses);
        }
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static Collection pickRDFResources(Collection allowedClasses) {
        return pickRDFResources(allowedClasses, true);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static Collection pickRDFResources(Collection allowedClasses, boolean allowsMultipleSelection) {
        if (allowedClasses.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        else {
            OWLModel owlModel = ((RDFResource) allowedClasses.iterator().next()).getOWLModel();
            if (allowsMultipleSelection) {
                return ProtegeUI.getSelectionDialogFactory().selectResourcesByType(null, owlModel, allowedClasses);
            }
            else {
                RDFResource result = ProtegeUI.getSelectionDialogFactory().selectResourceByType(null, owlModel, allowedClasses);
                if (result == null) {
                    return Collections.EMPTY_LIST;
                }
                else {
                    return Collections.singleton(result);
                }
            }
        }
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFResource pickRDFResourceFromCollection(Collection allowedResources, String label) {
        if (allowedResources.isEmpty()) {
            return null;
        }
        else {
            OWLModel owlModel = ((RDFResource) allowedResources.iterator().next()).getOWLModel();
            return ProtegeUI.getSelectionDialogFactory().selectResourceFromCollection(null, owlModel, allowedResources, label);
        }
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFSNamedClass pickRDFSNamedClass(OWLModel owlModel, String label) {
        return ProtegeUI.getSelectionDialogFactory().selectClass(null, owlModel, label);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static RDFSNamedClass pickRDFSNamedClass(OWLModel owlModel, Collection cs, String label) {
        return ProtegeUI.getSelectionDialogFactory().selectClass(null, owlModel, cs, label);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static Collection pickRDFSNamedClasses(OWLModel owlModel, String label) {
        return ProtegeUI.getSelectionDialogFactory().selectClasses(null, owlModel, label);
    }


    /**
     * @deprecated use ProtegeUI.getSelectionDialogFactory()...
     */
    public static OWLNamedClass pickOWLNamedClass(OWLModel owlModel, Collection classes, String label) {
        RDFSNamedClass namedClass = ProtegeUI.getSelectionDialogFactory().selectClass(null, owlModel, classes, label);
        if (namedClass instanceof OWLNamedClass) {
            return (OWLNamedClass) namedClass;
        }
        else {
            return null;
        }
    }


    public static Component searchComponentOfType(Container c, Class type) {

        if (type.isAssignableFrom(c.getClass())) {
            return c;
        }

        if (c.getComponents().length > 0) {
            Component[] comps = c.getComponents();
            for (int i = 0; i < comps.length; i++) {
                Component child = searchComponentOfType((Container) comps[i], type);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }


    public static void setConstraintChecking(OWLModel owlModel, boolean value) {
        owlModel.getOWLProject().getSettingsMap().setBoolean(CONSTRAINT_CHECKING, value);
    }


    public static void setDragAndDropSupported(OWLModel owlModel, boolean value) {
        if (value) {
            owlModel.getOWLProject().getSettingsMap().remove(DRAG_AND_DROP);
        }
        else {
            owlModel.getOWLProject().getSettingsMap().setBoolean(DRAG_AND_DROP, Boolean.FALSE);
        }
    }


    public static void setExternalResourcesSupported(OWLModel owlModel, boolean value) {
        if (value) {
            owlModel.getOWLProject().getSettingsMap().setBoolean(EXTERNAL_RESOURCES, Boolean.TRUE);
        }
        else {
            owlModel.getOWLProject().getSettingsMap().remove(EXTERNAL_RESOURCES);
        }
    }


    public static void setOWLToolTipGenerator(OWLToolTipGenerator ttg) {
        toolTipGenerator = ttg;
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showErrorMessageDialog(String message) {
        showErrorMessageDialog((Project) null, message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showErrorMessageDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showErrorMessageDialog(Project project, String message) {
        ProtegeUI.getModalDialogFactory().showMessageDialog((OWLModel) project.getKnowledgeBase(), message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showErrorMessageDialog(OWLModel owlModel, String message) {
        ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showErrorMessageDialog(String message, String title) {
        ProtegeUI.getModalDialogFactory().showMessageDialog((OWLModel) null, message, title);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showErrorMessageDialog(Project project, String message, String title) {
        ProtegeUI.getModalDialogFactory().showMessageDialog((OWLModel) project.getKnowledgeBase(), message, title);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(String message) {
        ProtegeUI.getModalDialogFactory().showMessageDialog((OWLModel) null, message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(Component parent, String message) {
        ProtegeUI.getModalDialogFactory().showMessageDialog(parent, message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(OWLModel owlModel, String message) {
        ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(Project project, String message) {
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, message);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(String message, String title, int type) {
        ProtegeUI.getModalDialogFactory().showMessageDialog((OWLModel) null, message, title);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(OWLModel owlModel, String message, String title, int type) {
        ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, message, title);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static void showMessageDialog(Project project, String message, String title, int type) {
        ProtegeUI.getModalDialogFactory().showMessageDialog((OWLModel) project.getKnowledgeBase(), message, title);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static boolean showConfirmDialog(String message, String title) {
        return ProtegeUI.getModalDialogFactory().showConfirmDialog((OWLModel) null, message, title);
    }


    /**
     * @deprecated use ProtegeUI.getModalDialogFactory()...
     */
    public static boolean showConfirmDialog(Project project, String message, String title) {
        OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
        return ProtegeUI.getModalDialogFactory().showConfirmDialog(owlModel, message, title);
    }


    private static Set unsuitableTabs = new HashSet();


    static {
        unsuitableTabs.add(ClsesTab.class.getName());
        unsuitableTabs.add(SlotsTab.class.getName());
        unsuitableTabs.add(InstancesTab.class.getName());
    }


    public static boolean isUnsuitableTab(String className) {
        return unsuitableTabs.contains(className);
    }
}
