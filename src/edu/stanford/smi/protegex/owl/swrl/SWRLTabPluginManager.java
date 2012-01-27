// TODO: Jess rule engine and SWRL query tab are loaded explicitly here. We need a discovery mechanism using the manifest.
// TODO: A bit sloppy. GUI code should be refactored to ui subdirectory.

package edu.stanford.smi.protegex.owl.swrl;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.stanford.smi.protege.event.ProjectAdapter;
import edu.stanford.smi.protege.event.ProjectEvent;
import edu.stanford.smi.protege.event.ProjectListener;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLNames;
import edu.stanford.smi.protegex.owl.swrl.ui.SWRLPluginGUIAdapter;
import edu.stanford.smi.protegex.owl.swrl.ui.tab.SWRLTab;

/**
 * This class provides mechanisms for Protege-OWL SWRLTab plugins to register themselves and to get screen real estate under the SWRL editor in the SWRL tab.
 * <p>
 * Documentation on using this class can be found <a
 * href="http://protege.stanford.edu/download/prerelease_javadoc_owl/edu/stanford/smi/protegex/owl/swrl/bridge/BridgePluginManager.html">here</a>.
 */
public class SWRLTabPluginManager
{
	private static transient final Logger log = Log.getLogger(SWRLTabPluginManager.class);

	private static HashMap<String, SWRLTabPluginRegistrationRecord> registeredPlugins = new HashMap<String, SWRLTabPluginRegistrationRecord>();
	private static String visiblePluginName = "";
	private static String selectedRuleName = "";

	private static ProjectListener projectListener = new ProjectAdapter() {
		@Override
		public void projectClosed(ProjectEvent event)
		{
			Project project = (Project)event.getSource();
			project.removeProjectListener(projectListener);
			visiblePluginName = "";
			selectedRuleName = "";
		}
	};

	static {
		loadRuleEngines();
	}

	public static void loadRuleEngines()
	{
		boolean ruleEngineFound = false;

		try { // TODO: Hack until we can do a proper class load with the manifest
			Class.forName("jess.Rete");
			Class.forName("org.protege.swrlapi.jess.ui.P3JessSubTab");
			ruleEngineFound = true;
		} catch (ClassNotFoundException e) {
			log.info("Jess rule engine load failed: could not find jess.Rete - or an error occured on initialization: " + e.getMessage());
		}

		try { // TODO: Hack until we can do a proper class load with the manifest
			Class.forName("org.protege.swrlapi.drools.ui.P3DroolsSubTab");
			ruleEngineFound = true;
		} catch (ClassNotFoundException e) {
			log.info("Drools rule engine load failed: could not find Drools JARs - or an error occured on initialization: " + e.getMessage());
		}

		if (ruleEngineFound) {
			try { // TODO: Hack until we can do a proper class load with the manifest
				Class.forName("org.protege.swrltab.p3.ui.P3SQWRLQueryTab");
			} catch (ClassNotFoundException e) {
				log.info("SQWRLQueryTab load failed: an error occured on initialization: " + e.getMessage());
			}
		}
	}
	
	public static String getSelectedRuleName()
	{
		return selectedRuleName;
	}

	public static boolean hasSelectedRule()
	{
		return selectedRuleName.length() != 0;
	}

	public static void setSelectedRuleName(String ruleName)
	{
		selectedRuleName = ruleName;
	}

	public static Collection<SWRLTabPluginRegistrationRecord> getRegisteredPlugins()
	{
		return registeredPlugins.values();
	}

	public static void hideVisiblePlugin()
	{
		hidePlugin(visiblePluginName, true);
	}

	public static boolean hidePlugin(String pluginName)
	{
		return hidePlugin(pluginName, false);
	}

	public static boolean isVisible(String pluginName)
	{
		return visiblePluginName.length() != 0 && pluginName.equals(visiblePluginName);
	}

	/**
	 *  This method is called by each plugin as it is loaded to inform the SWRLTab of its presence. 
	 */
	public static void registerPlugin(String pluginName, String ruleEngineName, String toolTip, ImageIcon icon, SWRLPluginGUIAdapter guiAdapter)
	{
		if (registeredPlugins.containsKey(pluginName))
			registeredPlugins.remove(pluginName);
		registeredPlugins.put(pluginName, new SWRLTabPluginRegistrationRecord(pluginName, ruleEngineName, toolTip, icon, guiAdapter));
		log.info("Plugin '" + pluginName + "' registered with the SWRLTab plugin manager.");
	}

	/**
	 *  This method is called by each plugin as it is loaded to inform the SWRLTab of its presence. The application-default rule engine is picked. 
	 */
	public static void registerPlugin(String pluginName, String toolTip, ImageIcon icon, SWRLPluginGUIAdapter guiAdapter)
	{
		String defaultRuleEngineName = ApplicationProperties.getString(SWRLNames.DEFAULT_RULE_ENGINE, "Jess");
		
		registerPlugin(pluginName, defaultRuleEngineName, toolTip, icon, guiAdapter);
	}
	
	
	public static void unregisterPlugin(String pluginName)
	{
		if (registeredPlugins.containsKey(pluginName)) {
			if (isVisible(pluginName))
				hidePlugin(pluginName, true);
			registeredPlugins.remove(pluginName);
		}
	}

	public static void showPlugin(String pluginName, SWRLTab swrlTab, OWLModel owlModel)
	{
		if (!isVisible(pluginName)) {
			if (hidePlugin(visiblePluginName)) { // Hide may fail if user does not confirm it.

				if (registeredPlugins.containsKey(pluginName)) {
					SWRLTabPluginRegistrationRecord registrationRecord = registeredPlugins.get(pluginName);
					Container pluginGUI = registrationRecord.getGUIAdapter().createPluginContainer(owlModel, pluginName, registrationRecord.getRuleEngineName());

					registrationRecord.setOWLModel(owlModel); // Set the owlModel so that we can unregister ourselves on deactivation.

					if (pluginGUI != null) {
						swrlTab.setVisible(false);
						swrlTab.setLayout(new GridLayout(2, 1));
						swrlTab.add(pluginGUI);
						swrlTab.setVisible(true);
						visiblePluginName = pluginName;
					} else
						makeTextPanel(swrlTab, "Unable to activate the '" + pluginName + "' plugin.");
				}
			}
		}
		owlModel.getProject().addProjectListener(projectListener);
	}

	private static boolean hidePlugin(String pluginName, boolean force)
	{
		if (isVisible(pluginName)) {

			if (!force
					&& (JOptionPane.showConfirmDialog(null, "Do you really want to close the " + pluginName + " plugin?", "Disable " + pluginName,
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION))
				return false;

			if (registeredPlugins.containsKey(pluginName)) {
				SWRLTabPluginRegistrationRecord registration = registeredPlugins.get(pluginName);
				Container pluginGUI = registration.getGUIAdapter().getPluginContainer();
				SWRLTab swrlTab = (SWRLTab)pluginGUI.getParent();
				if (swrlTab != null) {
					swrlTab.setVisible(false);
					swrlTab.remove(pluginGUI);
					swrlTab.reconfigure();
					swrlTab.setVisible(true);
				}
				if (registration.hasOWLModel())
					registration.getOWLModel().getProject().removeProjectListener(projectListener);
				visiblePluginName = "";
			}
		}
		return true;
	}

	public static class SWRLTabPluginRegistrationRecord
	{
		private String pluginName;
		private String ruleEngineName;
		private String toolTip;
		private SWRLPluginGUIAdapter guiAdapter;
		private Icon icon;
		private OWLModel owlModel;

		public SWRLTabPluginRegistrationRecord(String pluginName, String ruleEngineName, String toolTip, Icon icon, SWRLPluginGUIAdapter guiAdapter)
		{
			this.pluginName = pluginName;
			this.ruleEngineName = ruleEngineName;
			this.toolTip = toolTip;
			this.guiAdapter = guiAdapter;
			this.icon = icon;
			this.owlModel = null; // An OWL model is supplied when a GUI associated with the plugin is activated.
		}

		public void setOWLModel(OWLModel owlModel)
		{
			this.owlModel = owlModel;
		}

		public String getPluginName()
		{
			return pluginName;
		}

		public String getRuleEngineName()
		{
			return ruleEngineName;
		}

		public String getToolTip()
		{
			return toolTip;
		}

		public SWRLPluginGUIAdapter getGUIAdapter()
		{
			return guiAdapter;
		}

		public Icon getIcon()
		{
			return icon;
		}

		public OWLModel getOWLModel()
		{
			return owlModel;
		}

		public boolean hasOWLModel()
		{
			return owlModel != null;
		}
	}

	private static void makeTextPanel(SWRLTab swrlTab, String text)
	{
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);

		swrlTab.add(panel);
	}
}