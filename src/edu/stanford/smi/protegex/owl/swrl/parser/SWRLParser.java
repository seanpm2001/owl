package edu.stanford.smi.protegex.owl.swrl.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtomList;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLBuiltin;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLBuiltinAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLClassAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDataRangeAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDatavaluedPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDifferentIndividualsAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLIndividualPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLSameIndividualAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLVariable;

public class SWRLParser
{
	private final SWRLParserSupport swrlParserSupport;
	private final SWRLFactory swrlFactory;

	public SWRLParser(OWLModel owlModel)
	{
		this.swrlParserSupport = new SWRLParserSupport(owlModel);
		this.swrlFactory = new SWRLFactory(owlModel);
	}

	/**
	 * This parser will throw a {@link SWRLParseException} if it finds errors in the supplied rule. If the rule is correct
	 * but incomplete, a {@link SWRLIncompleteRuleException} (which is a subclass of {@link SWRLParseException}) will be
	 * thrown.
	 * <p>
	 * If {@link #parseOnly} is true, only checking is performed - no SWRL rules are created; if it is false, individuals
	 * are created.
	 */
	public SWRLImp parseSWRLRule(String ruleText, boolean parseOnly) throws SWRLParseException
	{
		return parseSWRLRule(ruleText, null, parseOnly);
	}

	public SWRLImp parseSWRLRule(String ruleText, SWRLImp imp, boolean parseOnly) throws SWRLParseException
	{
		SWRLTokenizer tokenizer = new SWRLTokenizer(ruleText.trim(), parseOnly);
		String currentToken, message;
		SWRLAtomList head = null, body = null;
		boolean atLeastOneAtom = false, justProcessedAtom = true, isInHead = false;

		if (!tokenizer.isParseOnly()) {
			head = getSWRLFactory().createAtomList();
			head.setInHead(true);
			body = getSWRLFactory().createAtomList();
			body.setInHead(false);
		}

		if (!tokenizer.isParseOnly() && !tokenizer.hasMoreTokens())
			throw new SWRLParseException("Empty rule");

		do {
			if (justProcessedAtom) {
				if (isInHead)
					message = "Expecting " + SWRLTokenizer.AND_CHAR;
				else
					message = "Expecting " + SWRLTokenizer.IMP_CHAR + " or " + SWRLTokenizer.AND_CHAR + " or "
							+ SWRLTokenizer.RING_CHAR;
			} else {
				if (isInHead)
					message = "Expecting atom";
				else
					message = "Expecting atom or " + SWRLTokenizer.IMP_CHAR;
			}

			currentToken = tokenizer.getNextNonSpaceToken(message);

			if (currentToken.equals("" + SWRLTokenizer.IMP_CHAR) || currentToken.equals("->")) { // A rule can have an empty
																																														// body.
				if (isInHead)
					throw new SWRLParseException("Second occurence of " + SWRLTokenizer.IMP_CHAR);
				isInHead = true;
				justProcessedAtom = false;
			} else if (currentToken.equals("-")) {
				continue; // Ignore "->" while we build up IMP_CHAR.
			} else if (currentToken.equals("" + SWRLTokenizer.AND_CHAR) || currentToken.equals("^")) {
				if (!justProcessedAtom)
					throw new SWRLParseException(SWRLTokenizer.AND_CHAR + " may occur only after an atom");
				justProcessedAtom = false;
			} else if (currentToken.equals("" + SWRLTokenizer.RING_CHAR) || currentToken.equals(".")) {
				if (isInHead || !justProcessedAtom)
					throw new SWRLParseException(SWRLTokenizer.RING_CHAR + " may occur only in query body");
				justProcessedAtom = false;
			} else {
				String predicate = currentToken;
				SWRLAtom atom = parseSWRLAtom(predicate, tokenizer, isInHead);
				atLeastOneAtom = true;
				if (!tokenizer.isParseOnly()) {
					if (isInHead)
						head.append(atom);
					else
						body.append(atom);
				}
				justProcessedAtom = true;
			}
		} while (tokenizer.hasMoreTokens());

		if (!tokenizer.isParseOnly()) {
			if (!atLeastOneAtom)
				throw new SWRLParseException("Incomplete SWRL rule - no antecedent or consequent");
			if (imp == null)
				imp = getSWRLFactory().createImp(head, body);
			else {
				imp.setHead(head);
				imp.setBody(body);
			}
			return imp;
		} else
			return null;
	}

	/**
	 * If the rule is correct and incomplete return 'true'; if the rule has errors or is correct and complete, return
	 * 'false'.
	 */
	public boolean isSWRLRuleCorrectAndIncomplete(String ruleText)
	{
		boolean result = false;

		try {
			parseSWRLRule(ruleText, true);
		} catch (SWRLParseException e) {
			if (e instanceof SWRLIncompleteRuleException)
				result = true;
		}

		return result;
	}

	private SWRLAtom parseSWRLAtom(String predicate, SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{
		if (isSWRLSameAsPredicate(predicate)) {
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for sameAs atom");
			return parseSWRLSameAsAtomArguments(tokenizer, isInHead);
		} else if (isSWRLDifferentFromPredicate(predicate)) {
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for differentFrom atom");
			return parseSWRLDifferentFromAtomArguments(tokenizer, isInHead);
		} else if (isSWRLClassPredicate(predicate)) {
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for class atom");
			return parseSWRLClassAtomArguments(predicate, tokenizer, isInHead);
		} else if (isSWRLObjectPropertyPredicate(predicate)) {
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for object property atom");
			return parseSWRLIndividualPropertyAtomArguments(predicate, tokenizer, isInHead);
		} else if (isSWRLDataPropertyPredicate(predicate)) {
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for data property atom");
			return parseSWRLDataPropertyAtomArguments(predicate, tokenizer, isInHead);
		} else if (isSWRLBuiltInPredicate(predicate)) {
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for built-in atom");
			return parseSWRLBuiltinAtomArguments(predicate, tokenizer, isInHead);
		} else if (isSWRLDataRangePredicate(predicate)) {
			List<RDFObject> enumeratedList = parseDObjectList(tokenizer, isInHead);
			tokenizer.checkAndSkipToken("(", "Expecting parentheses-enclosed arguments for data range atom");
			return parseSWRLDataRangeAtomArguments(enumeratedList, tokenizer, isInHead);
		} else
			throw new SWRLParseException("Invalid predicate " + predicate);
	}

	private SWRLSameIndividualAtom parseSWRLSameAsAtomArguments(SWRLTokenizer tokenizer, boolean isInHead)
			throws SWRLParseException
	{
		RDFResource iObject1 = parseIObject(tokenizer, isInHead);
		tokenizer.checkAndSkipToken(",", "Expecting comma-separated second argument for same as atom");
		RDFResource iObject2 = parseIObject(tokenizer, isInHead);

		tokenizer.checkAndSkipToken(")", "Expecting closing parenthesis after second argument to same as atom");

		return tokenizer.isParseOnly() ? null : getSWRLFactory().createSameIndividualAtom(iObject1, iObject2);
	}

	private SWRLDifferentIndividualsAtom parseSWRLDifferentFromAtomArguments(SWRLTokenizer tokenizer, boolean isInHead)
			throws SWRLParseException
	{
		RDFResource iObject1 = parseIObject(tokenizer, isInHead);
		tokenizer.checkAndSkipToken(",", "Expecting comma-separated second argument for different from atom");
		RDFResource iObject2 = parseIObject(tokenizer, isInHead);

		tokenizer.checkAndSkipToken(")", "Only two arguments allowed in different from atom");

		return tokenizer.isParseOnly() ? null : getSWRLFactory().createDifferentIndividualsAtom(iObject1, iObject2);
	}

	private SWRLClassAtom parseSWRLClassAtomArguments(String predicate, SWRLTokenizer tokenizer, boolean isInHead)
			throws SWRLParseException
	{
		RDFResource iObject = parseIObject(tokenizer, isInHead);

		tokenizer.checkAndSkipToken(")", "Expecting closing parenthesis for argument for class atom " + predicate);

		if (!tokenizer.isParseOnly()) {
			OWLNamedClass cls = swrlParserSupport.getOWLClass(predicate);
			if (cls == null)
				throw new SWRLParseException("no OWL class " + predicate + " found for class atom");
			return getSWRLFactory().createClassAtom(cls, iObject);
		} else
			return null;
	}

	private SWRLIndividualPropertyAtom parseSWRLIndividualPropertyAtomArguments(String predicate,
			SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{
		RDFResource iObject1 = parseIObject(tokenizer, isInHead);
		tokenizer.checkAndSkipToken(",", "Expecting comma-separated second argument for object property atom " + predicate);
		RDFResource iObject2 = parseIObject(tokenizer, isInHead);

		tokenizer.checkAndSkipToken(")", "Expecting closing parenthesis after second argument of object property atom "
				+ predicate);

		if (!tokenizer.isParseOnly()) {
			OWLObjectProperty objectProperty = swrlParserSupport.getOWLObjectProperty(predicate);
			if (objectProperty == null)
				throw new SWRLParseException("no object property " + predicate + " found for object property atom");
			return getSWRLFactory().createIndividualPropertyAtom(objectProperty, iObject1, iObject2);
		} else
			return null;
	}

	private SWRLDatavaluedPropertyAtom parseSWRLDataPropertyAtomArguments(String predicate, SWRLTokenizer tokenizer,
			boolean isInHead) throws SWRLParseException
	{
		String errorMessage = "Expecting literal qualification symbol '#' or closing parenthesis after second argument of data property argument ";

		RDFResource iObject = parseIObject(tokenizer, isInHead);
		tokenizer.checkAndSkipToken(",", "Expecting comma-separated second parameter for data property atom " + predicate);
		RDFObject dObject = parseDObject(tokenizer, isInHead);

		String token = tokenizer.getNextNonSpaceToken(errorMessage + predicate);

		if (token.equals("#")) { // Literal qualification
			String datatypeToken = tokenizer.getNextNonSpaceToken("Expecting XML Schema datatype");
			if (tokenizer.hasMoreTokens() && !isXSDDatatypePredicate(datatypeToken))
				throw new SWRLParseException("Invalid XML Schema datatype " + datatypeToken);
			if (!tokenizer.isParseOnly()) {
				String datatypeName = datatypeToken;
				String rawLiteralValue = dObject.getBrowserText();
				dObject = swrlParserSupport.getOWLLiteral(rawLiteralValue, datatypeName);
			}
			tokenizer.checkAndSkipToken(")", "Expecting closing parenthesis after second argument of data property atom");
		} else if (!token.equals(")"))
			throw new SWRLParseException(errorMessage + predicate);

		if (!tokenizer.isParseOnly()) {
			OWLDatatypeProperty datatypeProperty = swrlParserSupport.getOWLDataProperty(predicate);
			return getSWRLFactory().createDatavaluedPropertyAtom(datatypeProperty, iObject, dObject);
		} else
			return null;
	}

	private SWRLBuiltinAtom parseSWRLBuiltinAtomArguments(String builtInName, SWRLTokenizer tokenizer, boolean isInHead)
			throws SWRLParseException
	{
		List<RDFObject> arguments = parseObjectList(tokenizer, isInHead); // Swallows ')'

		if (!tokenizer.isParseOnly()) {
			SWRLBuiltin builtin = swrlParserSupport.getSWRLBuiltIn(builtInName);
			return getSWRLFactory().createBuiltinAtom(builtin, arguments.iterator());
		} else
			return null;
	}

	private SWRLDataRangeAtom parseSWRLDataRangeAtomArguments(List<RDFObject> enumeratedList, SWRLTokenizer tokenizer,
			boolean isInHead) throws SWRLParseException
	{
		RDFObject dObject = parseDObject(tokenizer, isInHead);
		tokenizer.checkAndSkipToken(")", "Expecting closing parenthesis after argument in data range atom");

		if (!tokenizer.isParseOnly()) {
			OWLDataRange dataRange = swrlParserSupport.getOWLDataRange();
			RDFProperty oneOfProperty = swrlParserSupport.getOWLOneOfProperty();

			Iterator<RDFObject> iterator = enumeratedList.iterator();
			while (iterator.hasNext()) {
				Object literalValue = iterator.next();
				dataRange.addPropertyValue(oneOfProperty, literalValue);
			}
			return getSWRLFactory().createDataRangeAtom(dataRange, dObject);
		} else
			return null;
	}

	private SWRLVariable parseSWRLVariable(SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{
		String variableName = tokenizer.getNextNonSpaceToken("Expecting variable name");
		swrlParserSupport.checkThatSWRLVariableNameIsValid(variableName);

		if (tokenizer.hasMoreTokens()) {
			if (!isInHead)
				tokenizer.addVariable(variableName);
			else if (!tokenizer.hasVariable(variableName))
				throw new SWRLParseException("Variable ?" + variableName + " used in consequent is not present in antecedent");
		}

		if (!tokenizer.isParseOnly())
			return swrlParserSupport.getSWRLVariable(variableName);
		else
			return null;
	}

	private RDFObject parseOWLLiteral(String rawLiteral, SWRLTokenizer tokenizer) throws SWRLParseException
	{
		if (rawLiteral.equals("\"")) { // The parsed entity is a string
			String stringValue = tokenizer.getNextStringToken("Expected a string");
			if (!tokenizer.isParseOnly())
				return swrlParserSupport.getOWLXSDStringLiteral(stringValue);
			else
				return null;
		} else if (rawLiteral.startsWith("t") || rawLiteral.startsWith("T") || rawLiteral.startsWith("f")
				|| rawLiteral.startsWith("F")) {
			// According to the XSD Specification, xsd:boolean's have the lexical space: {true, false, 1, 0}. We don't allow
			// {1, 0} since these are parsed as xsd:ints.
			if (tokenizer.hasMoreTokens()) {
				if (rawLiteral.equalsIgnoreCase("true") || rawLiteral.equalsIgnoreCase("false")) {
					if (!tokenizer.isParseOnly())
						return swrlParserSupport.getOWLXSDBooleanLiteral(rawLiteral);
					else
						return null;
				} else
					throw new SWRLParseException("Invalid OWL literal " + rawLiteral);
			} else
				return null;
		} else { // Is it an integer, float, long or double then?
			try {
				if (rawLiteral.contains(".")) {
					Double.parseDouble(rawLiteral); // Check it
					if (!tokenizer.isParseOnly())
						return swrlParserSupport.getOWLXSDDoubleLiteral(rawLiteral);
					else
						return null;
				} else {
					Integer.parseInt(rawLiteral); // Check it
					if (!tokenizer.isParseOnly())
						return swrlParserSupport.getOWLXSDIntLiteral(rawLiteral);
					else
						return null;
				}
			} catch (NumberFormatException e) {
				String errorMessage = "Invalid OWL literal " + rawLiteral;
				if (tokenizer.isParseOnly())
					throw new SWRLIncompleteRuleException(errorMessage);
				else
					throw new SWRLParseException(errorMessage);
			}
		}
	}

	private List<RDFObject> parseDObjectList(SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{ // Parse a list of variables and literals
		List<RDFObject> dObjects = null;

		if (!tokenizer.isParseOnly())
			dObjects = new ArrayList<RDFObject>();

		RDFObject dObject = parseDObject(tokenizer, isInHead);
		if (!tokenizer.isParseOnly())
			dObjects.add(dObject);

		String token = tokenizer
				.getNextNonSpaceToken("Expecting additional comma-separated variables or literals or closing parenthesis");
		while (token.equals(",")) {
			dObject = parseDObject(tokenizer, isInHead);
			if (!tokenizer.isParseOnly())
				dObjects.add(dObject);
			token = tokenizer.getNextNonSpaceToken("Expecting ',' or ')'");
			if (!(token.equals(",") || token.equals(")")))
				throw new SWRLParseException("Expecting ',' or ')', got " + token);
		}
		return dObjects;
	}

	private List<RDFObject> parseObjectList(SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{ // Parse a list of SWRL variables, OWL literals and OWL named individuals
		List<RDFObject> objects = null;

		if (!tokenizer.isParseOnly())
			objects = new ArrayList<RDFObject>();

		RDFObject object = parseObject(tokenizer, isInHead);
		if (!tokenizer.isParseOnly())
			objects.add(object);

		String token = tokenizer
				.getNextNonSpaceToken("Expecting additional comma-separated variables, literals or individual names or closing parenthesis");
		while (token.equals(",")) {
			object = parseObject(tokenizer, isInHead);
			if (!tokenizer.isParseOnly())
				objects.add(object);
			token = tokenizer.getNextNonSpaceToken("Expecting ',' or ')'");
			if (!(token.equals(",") || token.equals(")")))
				throw new SWRLParseException("Expecting ',' or ')', got'" + token);
		}
		return objects;
	}

	private RDFObject parseObject(SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{ // Parse a SWRL variable, OWL literal or an OWL named individual
		RDFObject parsedEntity = null;
		String parsedString = tokenizer.getNextNonSpaceToken("Expecting variable or individual name or literal");

		if (parsedString.equals("?"))
			parsedEntity = parseSWRLVariable(tokenizer, isInHead);
		else { // The entity is an individual name or literal
			if (swrlParserSupport.isOWLNamedIndividual(parsedString)) {
				if (!tokenizer.isParseOnly())
					parsedEntity = swrlParserSupport.getOWLNamedIndividual(parsedString);
			} else if (swrlParserSupport.isOWLClassName(parsedString)) {
				if (!tokenizer.isParseOnly())
					parsedEntity = swrlParserSupport.getOWLClass(parsedString);
			} else if (swrlParserSupport.isOWLProperty(parsedString)) {
				if (!tokenizer.isParseOnly())
					parsedEntity = swrlParserSupport.getOWLProperty(parsedString);
			} else
				parsedEntity = parseOWLLiteral(parsedString, tokenizer);
		}
		return parsedEntity;
	}

	private RDFResource parseIObject(SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{ // Parse a SWRL variable or an OWL named individual. For SWRL Full, also allow OWL class, property and datatype
		// names.
		String token = tokenizer.getNextNonSpaceToken("Expecting variable or OWL entity");

		if (token.equals("?"))
			return parseSWRLVariable(tokenizer, isInHead);
		else {
			String entityName = token;

			if (swrlParserSupport.isOWLNamedIndividual(entityName)) {
				return tokenizer.isParseOnly() ? null : swrlParserSupport.getOWLNamedIndividual(entityName);
			} else if (swrlParserSupport.isOWLClassName(entityName)) { // SWRL Full
				return tokenizer.isParseOnly() ? null : swrlParserSupport.getOWLClass(entityName);
			} else if (swrlParserSupport.isOWLObjectProperty(entityName)) { // SWRL Full
				return tokenizer.isParseOnly() ? null : swrlParserSupport.getOWLObjectProperty(entityName);
			} else if (swrlParserSupport.isOWLDataProperty(entityName)) { // SWRL Full
				return tokenizer.isParseOnly() ? null : swrlParserSupport.getOWLDataProperty(entityName);
			} else {
				if (tokenizer.hasMoreTokens())
					throw new SWRLParseException("Invalid OWL entity name " + entityName);
				else
					throw new SWRLIncompleteRuleException("Incomplete rule - OWL entity name " + entityName + " not valid");
			}
		}
	}

	private RDFObject parseDObject(SWRLTokenizer tokenizer, boolean isInHead) throws SWRLParseException
	{ // Parse a SWRL variable or an OWL literal
		String token = tokenizer.getNextNonSpaceToken("Expecting variable or literal");

		if (token.equals("?"))
			return parseSWRLVariable(tokenizer, isInHead);
		else {
			String rawLiteralValue = token;
			return parseOWLLiteral(rawLiteralValue, tokenizer);
		}
	}

	private boolean isSWRLClassPredicate(String identifier)
	{
		return swrlParserSupport.isOWLClass(identifier);
	}

	private boolean isSWRLObjectPropertyPredicate(String identifier)
	{
		return swrlParserSupport.isOWLObjectProperty(identifier);
	}

	private boolean isSWRLDataPropertyPredicate(String identifier)
	{
		return swrlParserSupport.isOWLDataProperty(identifier);
	}

	private boolean isSWRLBuiltInPredicate(String identifier)
	{
		return swrlParserSupport.isSWRLBuiltIn(identifier);
	}

	private boolean isSWRLDifferentFromPredicate(String predicate)
	{
		return predicate.equalsIgnoreCase("differentFrom");
	}

	private boolean isSWRLSameAsPredicate(String predicate)
	{
		return predicate.equalsIgnoreCase("sameAs");
	}

	private boolean isSWRLDataRangePredicate(String predicate)
	{
		return predicate.startsWith("[");
	}

	private boolean isXSDDatatypePredicate(String predicate)
	{
		return swrlParserSupport.isXSDDatatype(predicate);
	}

	private SWRLFactory getSWRLFactory()
	{
		return this.swrlFactory;
	}
}
