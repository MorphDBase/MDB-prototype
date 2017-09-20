/*
 * Created by Roman Baum on 18.05.15.
 * Last modified by Roman Baum on 18.05.15.
 */

package mdb.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


/**
 * MDBAgent mdb.vocabulary class for namespace http://www.morphdbase.de/Ontologies/MDB/MDBEntry0v1#
 */

public class MDBEntry {

    protected static final String uri ="http://www.morphdbase.de/Ontologies/MDB/MDBEntry0v1#";

    /** returns the URI for MDBCore
     * @return the URI for MDBCore
     */
    public static String getURI() {
        return uri;
    }

    private static Model m = ModelFactory.createDefaultModel();

    public static final Resource mdbCore                = m.createProperty(uri, "MDB_ENTRY_0000000029");
    public static final Resource mdbEntryID             = m.createProperty(uri, "MDB_ENTRY_0000000030");

    public static final Property coreIDHasAccessRight   = m.createProperty(uri, "MDB_ENTRY_0000000015" );

}
