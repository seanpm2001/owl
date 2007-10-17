
package edu.stanford.smi.protegex.owl.swrl.bridge.impl;

import edu.stanford.smi.protegex.owl.swrl.bridge.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.exceptions.*;

import edu.stanford.smi.protegex.owl.swrl.model.SWRLVariable;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLIndividualsAtom;

/*
** Class representing a SWRL individuals atom
*/
public abstract class IndividualsAtomImpl extends AtomImpl implements IndividualsAtom
{
  private AtomArgument argument1, argument2;
  
  public IndividualsAtomImpl(SWRLIndividualsAtom atom) throws SWRLRuleEngineBridgeException
  {
    if (atom.getArgument1() instanceof SWRLVariable) {
      SWRLVariable variable = (SWRLVariable)atom.getArgument1();
      AtomArgument argument = BridgeFactory.createVariableAtomArgument(variable.getName());
      addReferencedVariableName(variable.getName());
      argument1 = argument;
    } else if (atom.getArgument1() instanceof OWLIndividual) {
      edu.stanford.smi.protegex.owl.model.OWLIndividual individual = (edu.stanford.smi.protegex.owl.model.OWLIndividual)atom.getArgument1();
      OWLIndividual argument = BridgeFactory.createOWLIndividual(individual.getName());
      addReferencedIndividualName(individual.getName());
      argument1 = argument;
    } else throw new SWRLRuleEngineBridgeException("unexpected first argument to atom '" + atom.getBrowserText() + 
                                                   "' - expecting variable or individual, got instance of " + atom.getArgument1().getClass() + ".");

    if (atom.getArgument2() instanceof SWRLVariable) {
      SWRLVariable variable = (SWRLVariable)atom.getArgument2();
      AtomArgument argument = BridgeFactory.createVariableAtomArgument(variable.getName());
      addReferencedVariableName(variable.getName());
      argument2 = argument;
    } else if (atom.getArgument2() instanceof OWLIndividual) {
      edu.stanford.smi.protegex.owl.model.OWLIndividual individual = (edu.stanford.smi.protegex.owl.model.OWLIndividual)atom.getArgument2();
      OWLIndividual argument = BridgeFactory.createOWLIndividual(individual.getName());
      addReferencedIndividualName(individual.getName());
      argument2 = argument;
    } else throw new SWRLRuleEngineBridgeException("unexpected second argument to atom '" + atom.getBrowserText() + 
                                                   "' - expecting variable or individual, got instance of " + atom.getArgument2().getClass() + ".");

  } // IndividualsAtomImpl
  
  public AtomArgument getArgument1() { return argument1; }
  public AtomArgument getArgument2() { return argument2; }
} // IndividualsAtomImpl
