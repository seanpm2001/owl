package edu.stanford.smi.protegex.owl.ui.search.tests;

import edu.stanford.smi.protegex.owl.model.OWLComplementClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.tests.AbstractJenaTestCase;
import edu.stanford.smi.protegex.owl.ui.search.FindUsage;
import edu.stanford.smi.protegex.owl.ui.search.AbstractFindUsageTableItem;
import edu.stanford.smi.protegex.owl.ui.search.FindUsageTableItem;
import edu.stanford.smi.protegex.owl.ui.search.FindUsageTableModel;

import java.util.Collection;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class FindUsageChangedTestCase extends AbstractJenaTestCase {

    public void testDeleteUsageClass() {
        OWLNamedClass hostCls = owlModel.createOWLNamedClass("Host");
        OWLNamedClass findCls = owlModel.createOWLNamedClass("Find");
        OWLComplementClass complementCls = owlModel.createOWLComplementClass(findCls);
        hostCls.addSuperclass(complementCls);
        Collection items = FindUsage.getItems(findCls);
        assertSize(1, items);
        AbstractFindUsageTableItem item = (AbstractFindUsageTableItem) items.iterator().next();
        assertEquals(hostCls, item.getHost());
        assertEquals(complementCls, item.getUsage());
        assertEquals(FindUsageTableItem.SUPERCLASS, item.getType());
        FindUsageTableModel tableModel = new FindUsageTableModel(owlModel, items);
        assertEquals(1, tableModel.getRowCount());
        hostCls.removeSuperclass(complementCls);
        assertEquals(0, tableModel.getRowCount());
    }
}
