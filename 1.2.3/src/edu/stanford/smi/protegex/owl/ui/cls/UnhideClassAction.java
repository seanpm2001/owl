package edu.stanford.smi.protegex.owl.ui.cls;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A ResourceAction to set the selected class to visible.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class UnhideClassAction extends ResourceAction {

    public UnhideClassAction() {
        super("Make class visible", Icons.getBlankIcon(), AddSubclassAction.GROUP);
    }


    public void actionPerformed(ActionEvent e) {
        getResource().setVisible(true);
        getComponent().repaint();
    }


    public boolean isSuitable(Component component, RDFResource resource) {
        return component instanceof ClassTreePanel &&
                resource instanceof RDFSNamedClass &&
                !resource.isVisible();
    }
}
