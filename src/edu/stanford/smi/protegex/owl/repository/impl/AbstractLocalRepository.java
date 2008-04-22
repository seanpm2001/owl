package edu.stanford.smi.protegex.owl.repository.impl;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.repository.Repository;
import edu.stanford.smi.protegex.owl.repository.util.OntologyNameExtractor;
import edu.stanford.smi.protegex.owl.repository.util.RepositoryUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: matthewhorridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Sep 21, 2005<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public abstract class AbstractLocalRepository implements Repository {

    private File file;

    private boolean forceReadOnly;

    private Map ontologies;


    public AbstractLocalRepository(File file, boolean forceReadOnly) {
        this.file = file;
        this.forceReadOnly = forceReadOnly;
        ontologies = new HashMap();
    }


    protected File getFile() {
        return file;
    }


    public void refresh() {
        ontologies = new HashMap();
    }


    protected void putOntology(URI ontologyName, File file) {
        ontologies.put(ontologyName, file);
    }


    public boolean contains(URI ontologyName) {
        return ontologies.keySet().contains(ontologyName);
    }


    public Collection getOntologies() {
        return Collections.unmodifiableCollection(ontologies.keySet());
    }


    public InputStream getInputStream(URI ontologyName)
            throws IOException {
        File f = (File) ontologies.get(ontologyName);
        if (f != null) {
            return new FileInputStream(f);
        }
        else {
            return null;
        }
    }


    public OutputStream getOutputStream(URI ontologyName)
            throws IOException {
        if (isWritable(ontologyName)) {
            File f = (File) ontologies.get(ontologyName);
            return new FileOutputStream(f);
        }
        else {
            return null;
        }
    }


    public boolean isSystem() {
        return false;
    }


    public String getOntologyLocationDescription(URI ontologyName) {
        File f = (File) ontologies.get(ontologyName);
        if (f != null) {
            return f.getAbsolutePath();
        }
        else {
            return "";
        }
    }


    public String getRepositoryDescriptor() {
        try {
            return file.toURI().toURL().toString() + "?" +
                    RepositoryUtil.FORCE_READ_ONLY_FLAG + "=" + Boolean.toString(forceReadOnly);
        }
        catch (MalformedURLException e) {
            return null;
        }
    }


    public boolean isWritable(URI ontologyName) {
        if (forceReadOnly == false) {
            File f = (File) ontologies.get(ontologyName);
            if (f != null) {
                return f.canWrite();
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }


    public boolean isForceReadOnly() {
        return forceReadOnly;
    }


    public void setForceReadOnly(boolean forceReadOnly) {
        this.forceReadOnly = forceReadOnly;
    }


    protected URI processFile(File file) {
        URI ontologyName = null;
        try {
            PrintStream oldErr = System.err;
            System.setErr(new PrintStream(new OutputStream() {
                public void write(int b)
                        throws IOException {
                }
            }));
            FileInputStream fis = new FileInputStream(file);
            OntologyNameExtractor extractor = new OntologyNameExtractor(fis, file.toURI().toURL());
            ontologyName = extractor.getOntologyName();
            System.setErr(oldErr);
        } catch (Exception e) {
          Log.emptyCatchBlock(e);
        }

        return ontologyName;
    }
}

