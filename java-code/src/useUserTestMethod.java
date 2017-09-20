/**
 * Created by rbaum on 28.01.15.
 */

// import the user defined package "mdb_packages"
import java.io.IOException;

import static mdb_packages.JenaIOTDBFactory.pullDataFromTDB;
import static mdb_packages.JenaIOTDBFactory.pushDataInTDB;

public class useUserTestMethod {

    public static void main(String[] args) throws IOException {

        String inputFileName = "output.owl";
        String outputFileName = "method-output.owl";
        String triplestore_directory  = "../../Dokumente/sample_tbd/";

        // define CONSTRUCT-SPARQL-Query
        String sparqlQueryString = "CONSTRUCT {\n" +
                "  ?s ?p ?o\n" +
                "} \n" +
                "WHERE { \n" +
                "    ?s ?p ?o \n" +
                "}";

        // define ASK-SPARQL-Query
        /*String sparqlQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "\n" +
                "PREFIX mdbcore: <http://www.morphdbase.de/Ontologies/MDB/MDBCore0v1#>\n" +
                "PREFIX mdbagent: <http://www.morphdbase.de/Ontologies/MDB/MDBAgent0v1#>\n" +
                "PREFIX mdbap: <http://www.morphdbase.de/Ontologies/MDB/MDBAnnotationProperty0v1#>\n" +
                "PREFIX mdbtaxonomy: <http://www.morphdbase.de/Ontologies/MDB/MDBTaxonomy0v1#>\n" +
                "PREFIX mdbentry: <http://www.morphdbase.de/Ontologies/MDB/MDBEntry0v1#>\n" +
                "PREFIX mdbguicomposition: <http://www.morphdbase.de/Ontologies/MDB/MDB_GUIComposition0v1#>\n" +
                "PREFIX mdbeqcl: <http://www.morphdbase.de/Ontologies/MDB/CrossOntologyEquivalentClass0v1#>\n" +
                "PREFIX mdbgui: <http://www.morphdbase.de/Ontologies/MDB/MDB_GUI0v1#>\n" +
                "PREFIX mdbguielements: <http://www.morphdbase.de/Ontologies/MDB/MDB_GUIElements0v1#>\n" +
                "" +
                "ASK {\n" +
                "  ?subject mdbagent:mdbLoginName 'TheFrog'^^<http://www.w3.org/2000/01/rdf-schema#Literal>\n" +
                "}";*/

        // define SELECT-SPARQL-Query
        /*String sparqlQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "\n" +
                "PREFIX mdbcore: <http://www.morphdbase.de/Ontologies/MDB/MDBCore0v1#>\n" +
                "PREFIX mdbagent: <http://www.morphdbase.de/Ontologies/MDB/MDBAgent0v1#>\n" +
                "PREFIX mdbap: <http://www.morphdbase.de/Ontologies/MDB/MDBAnnotationProperty0v1#>\n" +
                "PREFIX mdbtaxonomy: <http://www.morphdbase.de/Ontologies/MDB/MDBTaxonomy0v1#>\n" +
                "PREFIX mdbentry: <http://www.morphdbase.de/Ontologies/MDB/MDBEntry0v1#>\n" +
                "PREFIX mdbguicomposition: <http://www.morphdbase.de/Ontologies/MDB/MDB_GUIComposition0v1#>\n" +
                "PREFIX mdbeqcl: <http://www.morphdbase.de/Ontologies/MDB/CrossOntologyEquivalentClass0v1#>\n" +
                "PREFIX mdbgui: <http://www.morphdbase.de/Ontologies/MDB/MDB_GUI0v1#>\n" +
                "PREFIX mdbguielements: <http://www.morphdbase.de/Ontologies/MDB/MDB_GUIElements0v1#>\n" +
                "" +
                "SELECT ?predicate ?object\n" +
                "WHERE {\n" +
                "   ?subject mdbagent:mdbLoginName 'TheFrog'^^<http://www.w3.org/2000/01/rdf-schema#Literal> .\n" +
                "   ?subject ?predicate ?object FILTER (?predicate NOT IN ( rdf:type))\n" +
                "}";*/

        //pushDataInTDB(triplestore_directory, inputFileName);

        pullDataFromTDB(triplestore_directory, outputFileName, sparqlQueryString);


    }
}
