
package edu.stanford.smi.protegex.owl.swrl.bridge.builtins.abox;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.swrl.model.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.exceptions.*;

import edu.stanford.smi.protegex.owl.swrl.util.SWRLOWLUtil;
import edu.stanford.smi.protegex.owl.swrl.exceptions.SWRLOWLUtilException;

import java.util.*;

/**
 ** Implementations library for SWRL ABox built-in methods. See <a
 ** href="http://protege.cim3.net/cgi-bin/wiki.pl?SWRLABoxBuiltIns">here</a> for documentation on this library.
 **
 ** See <a href="http://protege.cim3.net/cgi-bin/wiki.pl?SWRLBuiltInBridge">here</a> for documentation on defining SWRL built-in libraries.
 */
public class SWRLBuiltInLibraryImpl extends SWRLBuiltInLibrary
{
  private static String SWRLABoxLibraryName = "SWRLABoxBuiltIns";
  private static String SWRLABoxPrefix = "abox:";

  public SWRLBuiltInLibraryImpl() { super(SWRLABoxLibraryName); }

  public void reset() {}

  /**
   ** Returns true if the individual named by the first parameter has at least one value for the property named by the second parameter. If
   ** a third parameter is supplied, match only property values that are equal to that parameter.
   */
  public boolean hasPropertyValue(List<Argument> arguments) throws BuiltInException
  {
    String individualName, propertyName;
    Object propertyValue = null;
    boolean propertyValueSupplied = (arguments.size() == 3);
    boolean result = false;

    SWRLBuiltInUtil.checkNumberOfArgumentsInRange(2, 3, arguments.size());
    SWRLBuiltInUtil.checkThatArgumentIsAnIndividual(0, arguments);
    SWRLBuiltInUtil.checkThatArgumentIsAProperty(1, arguments);

    individualName = SWRLBuiltInUtil.getArgumentAsAnIndividualName(0, arguments);
    propertyName = SWRLBuiltInUtil.getArgumentAsAPropertyName(1, arguments);

    try {
      if (propertyValueSupplied) {
        propertyValue = SWRLBuiltInUtil.getArgumentAsAPropertyValue(2, arguments);
        result = SWRLOWLUtil.getNumberOfPropertyValues(getInvokingBridge().getOWLModel(), individualName, propertyName, propertyValue, true) != 0;
      } else
        result = SWRLOWLUtil.getNumberOfPropertyValues(getInvokingBridge().getOWLModel(), individualName, propertyName, true) != 0;
    } catch (SWRLOWLUtilException e) {
      throw new BuiltInException(e.getMessage());
    } // try
    return result;
  } // hasPropertyValue

  /**
   ** Returns true if the individual named by the second parameter has the number of values specified by the first parameter for the
   ** property named by the third parameter. If a fourth parameter is supplied, match only property values that are equal to that
   ** parameter. If the first parameter is unbound when the built-in is called, it is bound to the actual number of property values for the
   ** property for the specified individual.
   */
  public boolean hasNumberOfPropertyValues(List<Argument> arguments) throws BuiltInException
  {
    String individualName, propertyName;
    Object propertyValue = null;
    int numberOfPropertyValues;
    boolean propertyValueSupplied = (arguments.size() == 4);
    boolean result = false;

    SWRLBuiltInUtil.checkNumberOfArgumentsInRange(3, 4, arguments.size());
    SWRLBuiltInUtil.checkThatArgumentIsAnIndividual(1, arguments);
    SWRLBuiltInUtil.checkThatArgumentIsAProperty(2, arguments);

    individualName = SWRLBuiltInUtil.getArgumentAsAnIndividualName(1, arguments);
    propertyName = SWRLBuiltInUtil.getArgumentAsAPropertyName(2, arguments);

    try {
      if (propertyValueSupplied) {
        propertyValue = SWRLBuiltInUtil.getArgumentAsAPropertyValue(3, arguments);
        numberOfPropertyValues = SWRLOWLUtil.getNumberOfPropertyValues(getInvokingBridge().getOWLModel(), individualName, propertyName, propertyValue, true);
      } else 
        numberOfPropertyValues = SWRLOWLUtil.getNumberOfPropertyValues(getInvokingBridge().getOWLModel(), individualName, propertyName, true);

      if (SWRLBuiltInUtil.isUnboundArgument(0, arguments)) {
        arguments.set(0, new LiteralInfo(numberOfPropertyValues)); // Bind the result to the first parameter      
        result = true;
      } else {
        result = (numberOfPropertyValues == SWRLBuiltInUtil.getArgumentAsAnInteger(0, arguments));
      } // if
    } catch (SWRLOWLUtilException e) {
      throw new BuiltInException(e.getMessage());
    } // try
    return result;
  } // hasNumberOfPropertyValues

  /**
   ** Returns true if the class named by the first parameter has at least one individual.
   */
  public boolean hasIndividuals(List<Argument> arguments) throws BuiltInException
  {
    String className;
    boolean result = false;

    SWRLBuiltInUtil.checkNumberOfArgumentsEqualTo(1, arguments.size());
    SWRLBuiltInUtil.checkThatArgumentIsAClass(0, arguments);

    className = SWRLBuiltInUtil.getArgumentAsAClassName(0, arguments);

    try {
      result = (SWRLOWLUtil.getNumberOfIndividualsOfClass(getInvokingBridge().getOWLModel(), className, true) != 0);
    } catch (SWRLOWLUtilException e) {
      throw new BuiltInException(e.getMessage());
    } // try
    return result;
  } // hasIndividuals

  /**
   ** Returns true if the class named by the second parameter has the number of individuals specified by the first parameter. If the first
   ** parameter is unbound when the built-in is called, it is bound to the actual number of individuals of the specified class.
   */
  public boolean hasNumberOfIndividuals(List<Argument> arguments) throws BuiltInException
  {
    String className;
    int numberOfIndividuals;
    boolean result = false;

    SWRLBuiltInUtil.checkNumberOfArgumentsEqualTo(2, arguments.size());
    SWRLBuiltInUtil.checkThatArgumentIsAClass(1, arguments);

    className = SWRLBuiltInUtil.getArgumentAsAClassName(1, arguments);

    try {
      if (SWRLBuiltInUtil.isUnboundArgument(0, arguments)) {
        numberOfIndividuals = SWRLOWLUtil.getNumberOfIndividualsOfClass(getInvokingBridge().getOWLModel(), className, true);
        arguments.set(0, new LiteralInfo(numberOfIndividuals)); // Bind the result to the first parameter      
        result = true;
      } else {
        numberOfIndividuals = SWRLBuiltInUtil.getArgumentAsAnInteger(0, arguments);
        result = (numberOfIndividuals == SWRLOWLUtil.getNumberOfIndividualsOfClass(getInvokingBridge().getOWLModel(), className, true));
      } // if
    } catch (SWRLOWLUtilException e) {
      throw new BuiltInException(e.getMessage());
    } // try
    return result;
  } // hasNumberOfIndividuals

} // SWRLBuiltInLibraryImpl
