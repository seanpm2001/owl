package edu.stanford.smi.protegex.owl.jena.parser;

import java.util.Collection;
import java.util.HashSet;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.SimpleFrameStore;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLIndividual;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLOntology;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFList;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSNamedClass;

public class FrameCreatorUtility {
	private static SimpleFrameStore simpleFrameStore;
		
	
	public static Frame createFrameWithType(OWLModel owlModel, FrameID id, String frameUri, String typeUri, boolean isSubjAnon) {
		Frame frame = owlModel.getFrame(frameUri);
		
		if (frame != null)
			return frame;

		Frame type = owlModel.getFrame(typeUri);
		
		if (type == null)
			return null;
		
		//write here all the java class names
			
		//maybe remove the anon condition
		if (typeUri.equals(OWL.Ontology.getURI())) {
			frame = new DefaultOWLOntology(owlModel, id );				
		}
		else if (typeUri.equals(OWL.Class.getURI())) {
			frame = new DefaultOWLNamedClass(owlModel, id );				
		}
		else if (typeUri.equals(OWL.DatatypeProperty.getURI())) {
			frame = new DefaultOWLDatatypeProperty(owlModel, id);
		}
		else if (typeUri.equals(OWL.ObjectProperty.getURI())) {
			frame = new DefaultOWLObjectProperty(owlModel, id);
		}
		else if (typeUri.equals(OWL.TransitiveProperty.getURI())) {
			frame = new DefaultOWLObjectProperty(owlModel, id);			
			((DefaultOWLObjectProperty)frame).setTransitive(true);			
			//hack - because otherwise the type is set twice
			removeInstanceType((Instance)frame, owlModel.getCls(OWLNames.Cls.TRANSITIVE_PROPERTY));			
		}
		else if (typeUri.equals(OWL.SymmetricProperty.getURI())) {
			frame = new DefaultOWLObjectProperty(owlModel, id);			
			((DefaultOWLObjectProperty)frame).setSymmetric(true);
			//hack - because otherwise the type is set twice
			removeInstanceType((Instance)frame, owlModel.getCls(OWLNames.Cls.SYMMETRIC_PROPERTY));
		}
		else if (typeUri.equals(OWL.AnnotationProperty.getURI())) {
			frame = new DefaultRDFProperty(owlModel, id);  //should this be abstract owl prop?
			//((DefaultRDFProperty)frame).setAnnotationProperty(true); //do something here!	
		}
		else if (typeUri.equals(OWL.InverseFunctionalProperty.getURI())) {
			frame = new DefaultOWLObjectProperty(owlModel, id);			
			((DefaultOWLObjectProperty)frame).setInverseFunctional(true);
			//hack - because otherwise the type is set twice
			removeInstanceType((Instance)frame, owlModel.getCls(OWLNames.Cls.INVERSE_FUNCTIONAL_PROPERTY));
		}
		else if (typeUri.equals(OWL.FunctionalProperty.getURI())) {
			frame = new DefaultRDFProperty(owlModel, id);			
			((DefaultRDFProperty)frame).setFunctional(true);		
		}
		else if (typeUri.equals(RDF.Property.getURI())) {
			frame = new DefaultRDFProperty(owlModel, id);
		} else if (typeUri.equals(RDF.List.getURI())) {
			frame = new DefaultRDFList(owlModel, id);
		} else if (typeUri.equals(RDFS.Class.getURI())) {
			frame = new DefaultRDFSNamedClass(owlModel, id);
		} else {
			//maybe this is an RDF individual
			frame = new DefaultOWLIndividual(owlModel, id);
		}
		
		setFrameName(frame, frameUri);
		addInstanceType((Instance)frame, (Cls)type);

		return frame;
		
	}
	
	
	public static Frame createFrameWithType(OWLModel owlModel, FrameID id, String frameUri, Cls type, boolean isSubjAnon) {
		return createFrameWithType(owlModel, id, frameUri, type.getName(), isSubjAnon);		
	}
	
	
	public static boolean addInstanceType(Instance inst, Cls type) {
		if (inst == null || type == null) {			
			return false;
		}
		
		
		//This will call the OWLJavaFactory to make sure that the Java objects 
		//are of the right Java type 
		//simpleFrameStore.addDirectType(inst, type);
		
		Slot typeSlot = inst.getKnowledgeBase().getSystemFrames().getDirectTypesSlot();
		
		/*
		Collection types = simpleFrameStore.getOwnSlotValues(inst, typeSlot);		
		HashSet typesSet = new HashSet(types);
		typesSet.add(type);
		
		simpleFrameStore.setDirectOwnSlotValues(inst, typeSlot, typesSet);
		
		Slot instancesSlot = inst.getKnowledgeBase().getSystemFrames().getDirectInstancesSlot();
		Collection instances = simpleFrameStore.getOwnSlotValues(type, instancesSlot);		
		HashSet instancesSet = new HashSet(instances);
		instancesSet.add(inst);				
		simpleFrameStore.setDirectOwnSlotValues(type, instancesSlot, instancesSet);
		*/
	  //  if (!simpleFrameStore.getOwnSlotValues(inst, typeSlot).contains(type)) {
	    	addOwnSlotValue(inst, typeSlot , type);
	    	addOwnSlotValue(type, inst.getKnowledgeBase().getSystemFrames().getDirectInstancesSlot(), inst);
	   // }
		
		return true;
	}

	public static boolean removeInstanceType(Instance inst, Cls type) {
		Slot typeSlot = inst.getKnowledgeBase().getSystemFrames().getDirectTypesSlot();
		simpleFrameStore.removeDirectOwnSlotValue(inst, typeSlot , type);
    	simpleFrameStore.removeDirectOwnSlotValue(type, inst.getKnowledgeBase().getSystemFrames().getDirectInstancesSlot(), inst);
    	return true;
	}
	
	public static boolean setFrameName(Frame frame, String name) {
		if (frame == null)
			return false;
		
		simpleFrameStore.setFrameName(frame, name);
		
		return true;
	}
	
	public static boolean createSubclassOf(Cls cls, Cls superCls) {
		if (cls == null || superCls == null) {
			//Log.getLogger().warning("Error at creating subclass of relationship. Cls: " + cls + " Superclass: " + superCls);
			return false;
		}
	
		//if (!simpleFrameStore.getDirectSuperclasses(cls).contains(superCls)) {
			simpleFrameStore.addDirectSuperclass(cls, superCls);
		//}
		
		return true;
	}

	public static boolean createSubpropertyOf(Slot slot, Slot superSlot) {
		if (slot == null || superSlot == null) {
			return false;
		}
		
		//if (!simpleFrameStore.getDirectSuperslots(slot).contains(superSlot)) {
			simpleFrameStore.addDirectSuperslot(slot, superSlot);
		//}
		
		return true;
	}
	
	
	public static SimpleFrameStore getSimpleFrameStore() {
		return simpleFrameStore;
	}

	public static void setSimpleFrameStore(SimpleFrameStore simpleFrameStore) {
		FrameCreatorUtility.simpleFrameStore = simpleFrameStore;
	}
	
	
	public static boolean addOwnSlotValue(Frame frame, Slot slot, Object value) {
		if (frame == null || slot == null)
			return false;

	//	if (!simpleFrameStore.getOwnSlotValues(frame, slot).contains(value)) {
			simpleFrameStore.addDirectOwnSlotValue(frame, slot, value);
	//	}
		
		return true;
	}
	
	public static Collection<Cls> getDirectTypes(Instance instance) {
		return simpleFrameStore.getDirectTypes(instance);
	}
	
	public static boolean addDirectTypeAndSwizzle(Instance instance, Cls type) {
		simpleFrameStore.addDirectType(instance, type);
		
		return true;
	}

}
