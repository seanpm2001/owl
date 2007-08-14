package edu.stanford.smi.protegex.owl.tests;

import com.hp.hpl.jena.Jena;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProtegeOWLVersionDependencyFailedTestCase extends TestCase {

    private static Properties props;

    private static String buildFile = "resource/files/build.properties";

    private static String jena_major = "prowl.dependencies.jena.major_version";

    private static String jena_minor = "prowl.dependencies.jena.major_version";

    private static String protege_version = "build.version";


    protected void setUp() throws Exception {
        super.setUp();
        try {
            props = new Properties();
            InputStream stream = FileUtilities.getResourceStream(ProtegeOWL.class, buildFile);
            props.load(stream);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }
    }


    /**
     * @throws Exception
     * @todo Compare actual vs. expected versions
     */
    public void testShowVersionDependencies() throws Exception {
    	//TT - the Jena minor and major are not used anymore
        //assertEquals(jena_major, Jena.MAJOR_VERSION, props.getProperty(jena_major));
        //assertEquals(jena_minor, Jena.MINOR_VERSION, props.getProperty(jena_minor));
        assertEquals(protege_version, Text.getVersion(), props.getProperty(protege_version));
    }

}
