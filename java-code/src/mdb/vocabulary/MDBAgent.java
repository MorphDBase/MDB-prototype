/*
 * Created by Roman Baum on 26.02.15.
 * Last modified by Roman Baum on 18.05.15.
 */

package mdb.vocabulary;

import org.apache.jena.rdf.model.*;

/**
 * MDBAgent mdb.vocabulary class for namespace http://www.morphdbase.de/Ontologies/MDB/MDBAgent0v1#
 */

public class MDBAgent {

    protected static final String uri ="http://www.morphdbase.de/Ontologies/MDB/MDBAgent0v1#";

    /** returns the URI for this schema
     * @return the URI for this schema
     */
   public static String getURI() {
        return uri;
    }

    private static Model m = ModelFactory.createDefaultModel();

    public static final Resource mdbUser            = m.createResource(uri + "mdbUser" );
    public static final Property postalAddress 		= m.createProperty(uri, "postalAddress" );
    public static final Property departmentName 	= m.createProperty(uri, "departmentName" );
    public static final Property instituteName 		= m.createProperty(uri, "instituteName" );
    public static final Property researchInterest 	= m.createProperty(uri, "researchInterest" );
    public static final Property mdbPassword 		= m.createProperty(uri, "mdbPassword" );
    public static final Property expertForTaxon 	= m.createProperty(uri, "expertForTaxon" );
    public static final Property mdbLoginName 		= m.createProperty(uri, "mdbLoginName" );
    public static final Property firstName	 		= m.createProperty(uri, "firstName" );
    public static final Property lastName	 		= m.createProperty(uri, "lastName" );

}
