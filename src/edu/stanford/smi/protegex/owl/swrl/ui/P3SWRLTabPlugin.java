
package edu.stanford.smi.protegex.owl.swrl.ui;

import java.awt.Container;

import javax.swing.Icon;

import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Each plugin should register itself with the bridge using the registerPlugin method of the P3SWRLTabPluginManager class. In addition to a plugin display name,
 * a tool tip string, and a set of icons, this method is expecting an instance of a class that implements this interface. The plugin manager uses this
 * implementation to get the GUI for the plugin, which will be displayed in the SWRLTab when the plugin is activated.
 * <p>
 * This interface must thus be implemented by a SWRLTab plugin to create a Java Component that represents the plugin's GUI. Thus GUI will be activated in the
 * SWRLTab the provide a means of interacting with the plugin.
 * <p>
 * The createSWRLPluginGUI method is called once when the plugin is registered and is expecting a Java Swing component that is a subclass of a Java AWT
 * Container class. The getSWRLPluginGUI may be called repeatedly after registration and should return the Container instance created on initialization.
 * <p>
 * See <a href="http://protege.cim3.net/cgi-bin/wiki.pl?SWRLRuleEngineBridgeFAQ#nid6QJ">here</a> for a discussion on using this interface.
 */
public interface P3SWRLTabPlugin
{
	Container createSWRLPluginGUI(OWLModel owlModel, String pluginName, String ruleEngineName, Icon ruleEngineIcon, Icon reasonerIcon);

	Container getSWRLPluginGUI();
}
