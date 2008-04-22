package edu.stanford.smi.protegex.owl.jena.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.ProtegeNames;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.AbstractOWLModel;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;

class TriplePostProcessor extends AbstractStatefulTripleProcessor {
	private static final transient Logger log = Log.getLogger(TriplePostProcessor.class);
	
	public TriplePostProcessor(TripleProcessor processor) {
		super(processor);
	}
	
	public void doPostProcessing() {
		//undef triples handling
		processor.processUndefTriples();
		
		//swizzling
		processMetaclasses();
		processSubclassesOfRdfList();		
		processInstancesWithMultipleTypes();

		//create untyped resources if needed
		if (isCreateUntypedResourcesEnabled()) {
			processor.createUntypedResources();			
		}
		
		//classes
		processInferredSuperclasses(); //this should be done after create untyped resources
		processClsesWithoutSupercls(); //this should be done after create untyped resources
		processGeneralizedConceptInclusions();
		processAbstractClasses();
		
		//properties
		processDomainAndRange();
		
		processPossiblyTypedResources();
	}
	
	
	private boolean isCreateUntypedResourcesEnabled() {
		return ApplicationProperties.getBooleanProperty(ProtegeOWLParser.CREATE_UNTYPED_RESOURCES, true); 
	}
	

	@SuppressWarnings("deprecation")
	public void processMetaclasses() {		
		RDFSNamedClass rdfsClass = owlModel.getRDFSNamedClassClass();
		RDFSNamedClass rdfPropClass = owlModel.getRDFPropertyClass();
		
		log.info("Postprocess: Process metaclasses (" + rdfsClass.getSubclassCount() +
				rdfPropClass.getSubclassCount() + " classes) ... ");
		long time0 = System.currentTimeMillis();
		
		processMetaclasses(rdfsClass);
		processMetaclasses(rdfPropClass);
		
		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}
	
	
	private void processMetaclasses(Cls superMetaclass) {
		for (Iterator iterator = superMetaclass.getSubclasses().iterator(); iterator.hasNext();) {
			RDFSNamedClass metaclass = (RDFSNamedClass) iterator.next();
		
			if (!metaclass.isSystem()) {
				for (Iterator iterator2 = metaclass.getInstances(false).iterator(); iterator2.hasNext();) {					
					Instance inst = (Instance) iterator2.next();
					//this should be fine, because swizzling does not change anything in the NFS
					ParserUtil.getSimpleFrameStore(owlModel).swizzleInstance(inst);
				}
			}
		}
	}
	

	public void processSubclassesOfRdfList(){
		RDFSNamedClass rdfListCls = owlModel.getRDFListClass();
				
		log.info("Postprocess: Process subclasses of rdf:List (" + rdfListCls.getSubclassCount() + " classes) ... ");
		long time0 = System.currentTimeMillis();
		
		for (Iterator iterator = rdfListCls.getSubclasses(true).iterator(); iterator.hasNext();) {
			RDFSNamedClass listCls = (RDFSNamedClass) iterator.next();
		
			if (!listCls.isSystem()) {
				for (Iterator iterator2 = listCls.getInstances(false).iterator(); iterator2.hasNext();) {
					Instance inst = (Instance) iterator2.next();
					//this should be fine, because swizzling does not change anything in the NFS
					ParserUtil.getSimpleFrameStore(owlModel).swizzleInstance(inst);
				}
			}
		}
		
		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}
	
	
	@SuppressWarnings({"deprecation"})
	public void processClsesWithoutSupercls() {
		SuperClsCache superClsCache = undefTripleManager.getSuperClsCache();

		log.info("Postprocess: Process classes without superclasses (" + superClsCache.getCachedFramesWithNoSuperclass().size() + " classes) ... ");
		long time0 = System.currentTimeMillis();

		Collection classes = new ArrayList<Frame>(superClsCache.getCachedFramesWithNoSuperclass());
		classes.addAll(((AbstractOWLModel)owlModel).getRDFExternalClassClass().getInstances());
		
		for (Iterator<Frame> iter = classes.iterator(); iter.hasNext();) {
			Frame frame = (Frame) iter.next();
			if (log.isLoggable(Level.FINE)) {
				log.fine("processClsesWithoutSupercls: No declared supercls: " + frame);
			}

			if (frame instanceof RDFSClass) {
				//create subclass in the home triplestore of the cls
				TripleStore homeTs = owlModel.getTripleStoreModel().getHomeTripleStore((RDFSClass)frame);				
				FrameCreatorUtility.createSubclassOf((RDFSClass)frame, owlModel.getOWLThingClass(),homeTs);
				superClsCache.removeFrame(frame);
			} else {
				log.warning("Wrong java type for " + frame + " Expected: RDFSClass");
			}
		}	

		classes = superClsCache.getCachedFramesWithNoSuperclass();
		
		if (classes.size() > 0) {
			//This should not be the case
			log.warning("There are classes without explicit superclass: " + classes);
		}
		
		//superClsCache.clearCache();

		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}


	@SuppressWarnings({"unchecked", "deprecation"})
	public void processInferredSuperclasses(){
		SuperClsCache superClsCache = undefTripleManager.getSuperClsCache();
		
		OWLNamedClass owlClassClass = owlModel.getOWLNamedClassClass();

		log.info("Postprocess: Add inferred superclasses (" + owlModel.getInstanceCount(owlClassClass) + " classes) ... ");
		long time0 = System.currentTimeMillis();

		for (Iterator iterator = owlClassClass.getInstances().iterator(); iterator.hasNext();) {
			Object obj = iterator.next();

			try {
				OWLNamedClass namedClass = (OWLNamedClass) obj;
				
				if (!namedClass.isSystem()) {
					Collection<Cls> inferredSuperclasses = getInferredSuperClasses(namedClass);

					if (inferredSuperclasses.size() > 0) {
						superClsCache.removeFrame(namedClass);
					}

					for (Cls inferredSupercls : inferredSuperclasses) {
						if (!FrameCreatorUtility.hasSuperclass(namedClass, inferredSupercls)) {
							//create the inferred superclass in the same TS and NFS as the class							
							TripleStore homeTs = owlModel.getTripleStoreModel().getHomeTripleStore(namedClass);						
							FrameCreatorUtility.createSubclassOf(namedClass, inferredSupercls, homeTs);
						}
					}
				}

			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, " Error at post processing " + obj, e);
			}			
		}
		
		//TODO: this should be done only at the very end
		//if at the end there are classes that do not have a parent, add them under owl:Thing
		//this is repetitive with the previous method. Shouldn'e we call better that method?
		for (Iterator iterator = superClsCache.getCachedFramesWithNoSuperclass().iterator(); iterator.hasNext();) {
			Frame cls = (Frame) iterator.next();
			
			if (cls instanceof RDFSNamedClass) {
				try {
					if (!FrameCreatorUtility.hasSuperclass((RDFSNamedClass)cls, owlModel.getOWLThingClass())) {
						TripleStore homeTs = owlModel.getTripleStoreModel().getHomeTripleStore((RDFSClass)cls);
						FrameCreatorUtility.createSubclassOf((RDFSNamedClass)cls, owlModel.getOWLThingClass(), homeTs);
						iterator.remove();					
					}
				} catch(Exception e) {
					Log.getLogger().log(Level.WARNING, " Error at post processing (adding owl:Thing as parent): " + cls, e);
				}
			} else {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Class without parent: " + cls);
				}
			}			
		}

		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");		
	}


	private Collection<Cls> getInferredSuperClasses(OWLNamedClass namedClass) {
		Collection<Cls> inferredSuperClasses = new ArrayList<Cls>();

		//make this into a recursive function
		Collection<RDFSClass> equivClasses = namedClass.getEquivalentClasses();
		for (RDFSClass equivClass : equivClasses) {
			if (equivClass instanceof RDFSNamedClass) {
				inferredSuperClasses.add(equivClass);
			} else if (equivClass instanceof OWLIntersectionClass) {
				//add operands if defined
				Collection<RDFSClass> operands = ((OWLIntersectionClass)equivClass).getOperands();

				for (RDFSClass operand : operands) {
					if (operand instanceof RDFSNamedClass) {
						inferredSuperClasses.add(operand);
					}					
				}				
			}
		}

		return inferredSuperClasses;
	}


	public void processInstancesWithMultipleTypes() {
		MultipleTypesInstanceCache multipleTypesInstanceCache = undefTripleManager.getMultipleTypesInstanceCache();
		
		Set<Instance> instancesWithMultipleTypes = multipleTypesInstanceCache.getInstancesWithMultipleTypes();

		log.info("Postprocess: Instances with multiple types (" + instancesWithMultipleTypes.size() + " instances) ... ");
		long time0 = System.currentTimeMillis();

		for (Instance instance : instancesWithMultipleTypes) {
			Set<Cls> typesSet = multipleTypesInstanceCache.getTypesForInstanceAsSet(instance);
			adjustTypesOfInstance(instance, typesSet);
			if (log.isLoggable(Level.FINE)) {
				log.fine("process instance with multiple types" + instance + ": " + typesSet);
			}
		}
		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}

	private void adjustTypesOfInstance(Instance instance, Set<Cls> typesSet) {
		Collection<Cls> existingTypes = FrameCreatorUtility.getDirectTypes(instance);
		typesSet.removeAll(existingTypes); // types to add

		for (Cls type : typesSet) {
			/*
			 * This is kind of painful. We have to find out where the type 
			 * triple came from and add the type in the same TS.
			 * (What should happen if the same type comes from different TS-es?
			 * Which is very likely...)
			 */
			
			TripleStoreModel tsm = owlModel.getTripleStoreModel();			
			TripleStore initialActiveTs = tsm.getActiveTripleStore();
			
			try {
				TripleStore homeTs = getHomeTripleStore(instance, owlModel.getRDFTypeProperty(), type);
				if (homeTs != null) {			
					tsm.setActiveTripleStore(homeTs);
					FrameCreatorUtility.addInstanceType(instance, type, homeTs);	
					//this should not be necessary because the rdf:type triple was previously added
					//FrameCreatorUtility.addOwnSlotValue(instance, owlModel.getRDFTypeProperty(), type, homeTs);
					ParserUtil.getSimpleFrameStore(owlModel).swizzleInstance(instance);
				}
				else {
					log.warning("Could not find home triple store of type triple for " + instance);
				}
			} finally {
				tsm.setActiveTripleStore(initialActiveTs);
			}
			
			
			
		}
	}

	//TODO: should be moved to a utility class or to the triple store model
    private TripleStore getHomeTripleStore(Instance subject, Slot predicate, Object object) {
        //object = DefaultRDFSLiteral.getPlainValueIfPossible(object);
        Iterator it = owlModel.getTripleStoreModel().listUserTripleStores();
        while (it.hasNext()) {
            TripleStore ts = (TripleStore) it.next();
            NarrowFrameStore nfs = ts.getNarrowFrameStore();
            Collection values = nfs.getValues(subject, predicate, null, false);
            
            if (values.contains(object)) {
                return ts;
            }
        }
        return null;
    }
	

	@SuppressWarnings("deprecation")
	public void processDomainAndRange() {
		for (Iterator iterator = owlModel.getUserDefinedRDFProperties().iterator(); iterator.hasNext();) {
			RDFProperty property = (RDFProperty) iterator.next();	
			
			// Do this postprocessing in the TS of the property 			
			TripleStoreModel tsm = owlModel.getTripleStoreModel();
			
			TripleStore initialActiveTs = tsm.getActiveTripleStore();
			
			try {
				TripleStore homeTs = tsm.getHomeTripleStore(property);
				tsm.setActiveTripleStore(homeTs);
				//domain
				owlModel.getFrameStoreManager().getDomainUpdateFrameStore().synchronizeRDFSDomainWithProtegeDomain(property);
				//range
				owlModel.getFrameStoreManager().getRangeUpdateFrameStore().synchronizeRDFSRangeWithProtegeAllowedValues(property);
			} finally {
				tsm.setActiveTripleStore(initialActiveTs);
			}
		}
	}

	//this method will be refactored
	public void processGeneralizedConceptInclusions() {
		Collection<RDFSClass> gciAxioms = undefTripleManager.getGciAxioms();
		
		log.info("Postprocess: Generalized Concept Inclusion (" + gciAxioms.size() + " axioms) ... ");
		long time0 = System.currentTimeMillis();

		// now try to give them a good name
		NamespaceManager names = owlModel.getNamespaceManager();
		String namespace = names.getDefaultNamespace();
		if (namespace == null && owlModel.getDefaultOWLOntology() != null) {
			namespace = owlModel.getDefaultOWLOntology().getName() + "#";
		}
		String axiomPrefix = namespace + "Axiom";
		int counter = 0;

		if (namespace != null) {
			for (RDFSClass gci : gciAxioms) {
				while (getFrame(axiomPrefix + counter) != null) {
					counter++;
				}
				gci = owlModel.getOWLNamedClass(gci.getName());
				gci.rename(axiomPrefix + counter);
			}
		}
		
		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}


	@SuppressWarnings("deprecation")
	public void processAbstractClasses() {
		log.info("Postprocess: Abstract classes... ");
		long time0 = System.currentTimeMillis();
		
		RDFProperty abstractProp = owlModel.getRDFProperty(ProtegeNames.Slot.ABSTRACT);
		
		if (abstractProp == null) {
			return;
		}
		
		Collection abstractClses = owlModel.getFramesWithValue(abstractProp, null, false, Boolean.TRUE);
		
		for (Iterator iterator = abstractClses.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			
			if (object instanceof RDFSClass) {
				// Do this postprocessing in the TS of the property 			
				TripleStoreModel tsm = owlModel.getTripleStoreModel();
				
				TripleStore initialActiveTs = tsm.getActiveTripleStore();
				
				try {
					TripleStore homeTs = tsm.getHomeTripleStore((RDFSClass)object);
					tsm.setActiveTripleStore(homeTs);
					((Cls) object).setAbstract(true);
				} finally {
					tsm.setActiveTripleStore(initialActiveTs);
				}		
			}			
		}
		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}
	
	
	@SuppressWarnings("deprecation")
	public void processPossiblyTypedResources() {
		RDFSClass untypedClass = ((AbstractOWLModel)owlModel).getRDFExternalClassClass();
		RDFSClass untypedProp = ((AbstractOWLModel)owlModel).getRDFExternalPropertyClass();
		RDFSClass untypedRes = ((AbstractOWLModel)owlModel).getRDFExternalResourceClass();
		
		int count = untypedClass.getDirectInstanceCount() + 
		  			untypedProp.getDirectInstanceCount() +
		  			untypedRes.getDirectInstanceCount(); 
		
		log.info("Postprocess: Possibly typed entities ("  + count  + " resources) ... ");
		long time0 = System.currentTimeMillis();
		
		processPossiblyTypedResources(untypedClass);
		processPossiblyTypedResources(untypedProp);
		processPossiblyTypedResources(untypedRes);
		
		log.info("(" + (System.currentTimeMillis() - time0) + " ms)");
	}
	
	private void processPossiblyTypedResources(Cls untypedCls) {
		for (Iterator iterator = untypedCls.getDirectInstances().iterator(); iterator.hasNext();) {
			Instance untypedEntity = (Instance) iterator.next();
			
			if (untypedEntity.getDirectTypes().size() > 1) {
				untypedEntity.removeDirectType(untypedCls); //it will also swizzle
			}			
		}
	}
	
}
