// TODO: remove Protege-OWL dependencies
// TODO: needs to be more intelligent and not create duplicate objects for identical entities

package edu.stanford.smi.protegex.owl.swrl.bridge;

import edu.stanford.smi.protegex.owl.swrl.bridge.impl.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.xsd.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.exceptions.*;

import edu.stanford.smi.protegex.owl.swrl.sqwrl.exceptions.*;

import edu.stanford.smi.protegex.owl.swrl.model.*;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 ** Factory to create bridge instances of OWL entities that reflect OWL entities in a source OWL model or entities created inside the bridge
 ** (that may or may not ultimately be transferred to the source OWL model).
 */
public class OWLFactory
{
  // SWRL atoms
  public static BuiltInAtom createBuiltInAtom(OWLModel owlModel, SWRLBuiltinAtom atom) throws OWLFactoryException, DatatypeConversionException  { return new BuiltInAtomImpl(owlModel, atom); } 
  public static BuiltInAtom createBuiltInAtom(String builtInName, List<BuiltInArgument> arguments) { return new BuiltInAtomImpl(builtInName, arguments); } 
  public static ClassAtom createClassAtom(SWRLClassAtom atom) throws OWLFactoryException { return new ClassAtomImpl(atom); }
  public static DataRangeAtom createDataRangeAtom(SWRLDataRangeAtom atom) throws OWLFactoryException { return new DataRangeAtomImpl(atom); }
  public static DatavaluedPropertyAtom createDatavaluedPropertyAtom(OWLModel owlModel, SWRLDatavaluedPropertyAtom atom) throws OWLFactoryException, DatatypeConversionException { return new DatavaluedPropertyAtomImpl(owlModel, atom); }
  public static DifferentIndividualsAtom createDifferentIndividualsAtom(SWRLDifferentIndividualsAtom atom) throws OWLFactoryException { return new DifferentIndividualsAtomImpl(atom); }
  public static IndividualPropertyAtom createIndividualPropertyAtom(SWRLIndividualPropertyAtom atom) throws OWLFactoryException { return new IndividualPropertyAtomImpl(atom); }
  public static SameIndividualAtom createSameIndividualAtom(SWRLSameIndividualAtom atom) throws OWLFactoryException { return new SameIndividualAtomImpl(atom); }

  // SWRL and basic OWL entities
  public static SWRLRule createSWRLRule(String ruleName, List<Atom> bodyAtoms, List<Atom> headAtoms) throws SQWRLException, BuiltInException { return new SWRLRuleImpl(ruleName, bodyAtoms, headAtoms); }

  public static OWLClass createOWLClass(OWLModel owlModel, String className) throws OWLFactoryException { return new OWLClassImpl(owlModel, className); }
  public static OWLClass createOWLClass(String className) { return new OWLClassImpl(className); }

  public static OWLIndividual createOWLIndividual(String individualName) { return new OWLIndividualImpl(individualName); }
  public static OWLIndividual createOWLIndividual(OWLModel owlModel, String individualName) throws OWLFactoryException { return new OWLIndividualImpl(owlModel, individualName); }
  public static OWLIndividual createOWLIndividual(edu.stanford.smi.protegex.owl.model.OWLIndividual individual) throws OWLFactoryException { return new OWLIndividualImpl(individual); }
  public static OWLIndividual createOWLIndividual(String individualName, String className) { return new OWLIndividualImpl(individualName, className); }

  public static OWLObjectProperty createOWLObjectProperty(String propertyName) { return new OWLObjectPropertyImpl(propertyName); }

  public static OWLDatatypeProperty createOWLDatatypeProperty(String propertyName) { return new OWLDatatypePropertyImpl(propertyName); }

  public static OWLDatatypeValue createOWLDatatypeValue(OWLModel owlModel, RDFSLiteral literal) throws DatatypeConversionException { return new OWLDatatypeValueImpl(owlModel, literal); }
  public static OWLDatatypeValue createOWLDatatypeValue(String s) { return new OWLDatatypeValueImpl(s); }
  public static OWLDatatypeValue createOWLDatatypeValue(Number n) { return new OWLDatatypeValueImpl(n); }
  public static OWLDatatypeValue createOWLDatatypeValue(boolean b){ return new OWLDatatypeValueImpl(b); }
  public static OWLDatatypeValue createOWLDatatypeValue(int i) { return new OWLDatatypeValueImpl(i); }
  public static OWLDatatypeValue createOWLDatatypeValue(long l) { return new OWLDatatypeValueImpl(l); }
  public static OWLDatatypeValue createOWLDatatypeValue(float f) { return new OWLDatatypeValueImpl(f); }
  public static OWLDatatypeValue createOWLDatatypeValue(double d){ return new OWLDatatypeValueImpl(d); }
  public static OWLDatatypeValue createOWLDatatypeValue(short s) { return new OWLDatatypeValueImpl(s); }
  public static OWLDatatypeValue createOWLDatatypeValue(Byte b) { return new OWLDatatypeValueImpl(b); }
  public static OWLDatatypeValue createOWLDatatypeValue(BigDecimal bd) { return new OWLDatatypeValueImpl(bd); }
  public static OWLDatatypeValue createOWLDatatypeValue(BigInteger bi) { return new OWLDatatypeValueImpl(bi); }
  public static OWLDatatypeValue createOWLDatatypeValue(PrimitiveXSDType xsd) { return new OWLDatatypeValueImpl(xsd); }

  // OWL axioms
  public static OWLDatatypePropertyAssertionAxiom createOWLDatatypePropertyAssertionAxiom(OWLIndividual subject, OWLProperty property, OWLDatatypeValue object) { return new OWLDatatypePropertyAssertionAxiomImpl(subject, property, object); }
  public static OWLObjectPropertyAssertionAxiom createOWLObjectPropertyAssertionAxiom(OWLIndividual subject, OWLProperty property, OWLIndividual object)  { return new OWLObjectPropertyAssertionAxiomImpl(subject, property, object); }
  public static OWLDifferentIndividualsAxiom createOWLDifferentIndividualsAxiom(OWLIndividual individual1, OWLIndividual individual2) { return new OWLDifferentIndividualsAxiomImpl(individual1, individual2); }
  public static OWLDifferentIndividualsAxiom createOWLDifferentIndividualsAxiom(Set<OWLIndividual> individuals) { return new OWLDifferentIndividualsAxiomImpl(individuals); }
  public static OWLSameIndividualsAxiom createOWLSameIndividualsAxiom(OWLIndividual individual1, OWLIndividual individual2) { return new OWLSameIndividualsAxiomImpl(individual1, individual2); }

  // Arguments
  public static MultiArgument createMultiArgument(String variableName) { return new MultiArgumentImpl(variableName); }
  public static MultiArgument createMultiArgument(String variableName, List<BuiltInArgument> arguments) { return new MultiArgumentImpl(variableName, arguments); }
  public static VariableAtomArgument createVariableAtomArgument(String variableName) { return new VariableAtomArgumentImpl(variableName); }
  public static VariableBuiltInArgument createVariableBuiltInArgument(String variableName) { return new VariableBuiltInArgumentImpl(variableName); }
  public static BuiltInArgument createBuiltInArgument(String variableName) { return new BuiltInArgumentImpl(variableName); }

} // OWLFactory