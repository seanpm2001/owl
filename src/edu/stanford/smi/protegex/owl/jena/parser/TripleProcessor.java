package edu.stanford.smi.protegex.owl.jena.parser;

import java.util.Collection;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.impl.AbstractOWLModel;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;

// - verify - not hard --
//TODO: Load Birnlex -> class cast (rdf:List) OWLJavaFactory doesn't do the right thing
//TODO: When to do post processing?
//TODO: Check Jena writer - it writes out frames classes (:PAL-CONSTRAINT)

//TODO: Each post process in a try catch
//TODO: Check if the untyped types are written out
//TODO: Add owl:Thing also for untyped classes
//TODO: Use StringBuffer in place of String for all namespace methods

//-- later --
//TODO: Postprocessing GCI - refactor in their own class
//TODO: Process each triple in a try catch
//TODO: Timing logger for the parser


public class TripleProcessor {

	private OWLModel owlModel;

	private TripleProcessorForResourceObjects processorResourceObjs;
	private TripleProcessorForLiteralObjects processorLiteralObjs;
	private TripleProcessorForUntypedResources untypedProcessor;
	private TriplePostProcessor postProcessor;
	
	private UndefTripleManager undefTripleManager;


	public TripleProcessor(OWLModel owlModel) {
		this.owlModel = owlModel;

		this.undefTripleManager = ((AbstractOWLModel)owlModel).getUndefTripleManager();
		
		//should come as the last in the initialization
		this.processorResourceObjs = new TripleProcessorForResourceObjects(this);
		this.processorLiteralObjs = new TripleProcessorForLiteralObjects(this);
		this.untypedProcessor = new TripleProcessorForUntypedResources(this);
		this.postProcessor = new TriplePostProcessor(this);	
	}


	public boolean processTriple(AResource subj, AResource pred, AResource obj, TripleStore ts, boolean alreadyInUndef) {
		return processorResourceObjs.processTriple(subj, pred, obj, ts, alreadyInUndef);
	}

	public boolean processTriple(AResource subj, AResource pred, ALiteral lit, TripleStore ts, boolean alreadyInUndef) {
		return processorLiteralObjs.processTriple(subj, pred, lit, ts, alreadyInUndef);
	}

	
	public OWLModel getOWLModel() {
		return owlModel;
	}
	
	public void addUndefTriple(AResource subj, AResource pred, AResource obj, String undefName, boolean alreadyInUndef, TripleStore ts) {
		if (!alreadyInUndef) {		  
			undefTripleManager.addUndefTriple(new UndefTriple(subj, pred, obj, undefName, ts));
		}
	}
	
	protected void checkUndefinedResources(String uri) {		
		Collection<UndefTriple> undefTriples = undefTripleManager.getUndefTriples(uri); 

		for (Iterator<UndefTriple> iter = undefTriples.iterator(); iter.hasNext();) {
			UndefTriple undefTriple = (UndefTriple) iter.next();
			Object obj = undefTriple.getTripleObj();

			TripleStore undefTripleStore = undefTriple.getTripleStore();
			
			boolean success = false;

			if (obj instanceof AResource) {	
				success = processTriple(undefTriple.getTripleSubj(), undefTriple.getTriplePred(), (AResource) undefTriple.getTripleObj(), undefTripleStore, true);
			} else if (obj instanceof ALiteral) {
				success = processTriple(undefTriple.getTripleSubj(), undefTriple.getTriplePred(), (ALiteral) undefTriple.getTripleObj(), undefTripleStore, true);
			}

			if (success) {			
				iter.remove();
				undefTripleManager.removeUndefTriple(uri, undefTriple);
			}
		}
	}
	

	public UndefTripleManager getUndefTripleManager() {
		return undefTripleManager;
	}	

	

	public void doPostProcessing() {
		processUndefTriples();
		postProcessor.doPostProcessing();
	}


	public void processUndefTriples() {		
		untypedProcessor.processUndefTriples();		
	}
	
	public void createUntypedResources() {
		untypedProcessor.createUntypedResources();
	}
	

}
