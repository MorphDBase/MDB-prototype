/*
 * Created by Roman Baum on 26.02.15.
 * Last modified by Roman Baum on 18.05.15.
 */

package mdb.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


/**
 * MDBAgent mdb.vocabulary class for namespace http://www.morphdbase.de/Ontologies/MDB/MDBCore0v1#
 */
public class MDBCore {

    protected static final String uri ="http://www.morphdbase.de/Ontologies/MDB/MDBCore0v1#";

    /** returns the URI for MDBCore
     * @return the URI for MDBCore
     */
    public static String getURI() {
        return uri;
    }

    private static Model m = ModelFactory.createDefaultModel();

    public static final Resource mdbEntryChangeLog                  = m.createResource(uri + "MDB_CORE_0000000168" );

    public static final Resource publishEntryAccessRight            = m.createResource(uri + "MDB_CORE_0000000183" );
    public static final Resource readOnlyEntryAccessRight           = m.createResource(uri + "MDB_CORE_0000000184" );
    public static final Resource editEntryAccessRight               = m.createResource(uri + "MDB_CORE_0000000185" );
    public static final Resource createNewVersionEntryAccessRight   = m.createResource(uri + "MDB_CORE_0000000186" );
    public static final Resource assignRightsEntryAccessRight       = m.createResource(uri + "MDB_CORE_0000000187" );
    public static final Resource commentEntryAccessRight            = m.createResource(uri + "MDB_CORE_0000000188" );
    public static final Resource deleteEntryEntryAccessRight        = m.createResource(uri + "MDB_CORE_0000000189" );

    public static final Property hasChangeLog                       = m.createProperty(uri, "MDB_CORE_0000000213" );
    public static final Property hasEntrySpecificAccessRights 	    = m.createProperty(uri, "MDB_CORE_0000000378" );
    public static final Property hasHeader 		                    = m.createProperty(uri, "MDB_CORE_0000000379" );
    public static final Property hasVersionsAndProvenance		    = m.createProperty(uri, "MDB_CORE_0000000382" );
    public static final Property hasNamedGraphChangeLog             = m.createProperty(uri, "MDB_CORE_0000000383" );
    public static final Property hasMDBCoreIDComposition	        = m.createProperty(uri, "MDB_CORE_0000000473" );
    public static final Property hasCoreIDIndividuals   	        = m.createProperty(uri, "MDB_CORE_0000000489" );
    public static final Property hasCurrentDraftVersion		        = m.createProperty(uri, "MDB_CORE_0000000155" );

}
