
package edu.stanford.smi.protegex.owl.swrl.ddm.impl;

import edu.stanford.smi.protegex.owl.swrl.ddm.*;
import edu.stanford.smi.protegex.owl.swrl.portability.OWLPropertyReference;


public class OWLDatatypePropertyMapImpl extends OWLPropertyMapImpl implements OWLDatatypePropertyMap
{
  private Column valueColumn;

  public OWLDatatypePropertyMapImpl(OWLPropertyReference owlProperty, PrimaryKey primaryKey, Column valueColumn)
  {
    super(owlProperty, primaryKey);
    this.valueColumn = valueColumn;
  } // OWLDatatypePropertyMapImpl

  public Column getValueColumn() { return valueColumn; }
} // OWLDatatypePropertyMap
