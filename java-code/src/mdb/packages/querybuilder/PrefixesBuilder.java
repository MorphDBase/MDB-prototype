/*
 * Created by Roman Baum on 18.01.16.
 * Last modified by Roman Baum on 10.11.16.
 */

package mdb.packages.querybuilder;

import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * This class contains methods to add some filter to a SPARQL Query.
 */
public class PrefixesBuilder {


    JSONObject pfxJSONObject = new JSONObject();


    public PrefixesBuilder() {

        this.pfxJSONObject = generatePfxJSONObject(this.pfxJSONObject);
    }


    /**
     * This method add(s) prefixes to an askBuilder.
     * @param askBuilder the askBuilder
     * @return the updated input askBuilder
     */
    public AskBuilder addPrefixes (AskBuilder askBuilder) {

        Iterator<String> pfxIterator = pfxJSONObject.keys();

        while (pfxIterator.hasNext()) {

            String currPrefix = pfxIterator.next();

            askBuilder.addPrefix(currPrefix, pfxJSONObject.getString(currPrefix));

        }


        return askBuilder;
    }


    /**
     * This method add(s) prefixes to a constructBuilder.
     * @param constructBuilder the constructBuilder
     * @return the updated input constructBuilder
     */
    public ConstructBuilder addPrefixes (ConstructBuilder constructBuilder) {

        Iterator<String> pfxIterator = pfxJSONObject.keys();

        while (pfxIterator.hasNext()) {

            String currPrefix = pfxIterator.next();

            constructBuilder.addPrefix(currPrefix, pfxJSONObject.getString(currPrefix));

        }


        return constructBuilder;
    }

    /**
     * This method add(s) prefixes to a selectBuilder.
     * @param selectBuilder the selectBuilder
     * @return the updated input selectBuilder
     */
    public SelectBuilder addPrefixes (SelectBuilder selectBuilder) {

        Iterator<String> pfxIterator = pfxJSONObject.keys();

        while (pfxIterator.hasNext()) {

            String currPrefix = pfxIterator.next();

            selectBuilder.addPrefix(currPrefix, pfxJSONObject.getString(currPrefix));

        }

        return selectBuilder;
    }



    /**
     * This method generates an JSONObject which contains many known prefixes.
     * @return an JSONObject with known prefixes
     */
    public JSONObject generatePfxJSONObject (JSONObject pfxJSONObject) {

        pfxJSONObject.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        pfxJSONObject.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        pfxJSONObject.put("owl", "http://www.w3.org/2002/07/owl#");
        pfxJSONObject.put("text", "http://jena.apache.org/text#");
        pfxJSONObject.put("dcterms", "http://purl.org/dc/terms/");
        pfxJSONObject.put("foaf", "http://xmlns.com/foaf/0.1/");
        pfxJSONObject.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        pfxJSONObject.put("skos", "http://www.w3.org/2004/02/skos/core#");

        pfxJSONObject.put("mdbcore", "http://www.morphdbase.de/Ontologies/MDB/MDBCore#");
        pfxJSONObject.put("mdbdatascheme", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#");
        pfxJSONObject.put("mdbagent", "http://www.morphdbase.de/Ontologies/MDB/MDBAgent#");
        pfxJSONObject.put("mdbap", "http://www.morphdbase.de/Ontologies/MDB/MDBAnnotationProperty#");
        pfxJSONObject.put("mdbuiap", "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#");
        pfxJSONObject.put("mdbtaxonomy", "http://www.morphdbase.de/Ontologies/MDB/MDBTaxonomy#");
        pfxJSONObject.put("mdbentry", "http://www.morphdbase.de/Ontologies/MDB/MDBEntry#");
        pfxJSONObject.put("mdbguicomposition", "http://www.morphdbase.de/Ontologies/MDB/MDB_GUIComposition#");
        pfxJSONObject.put("mdbeqcl", "http://www.morphdbase.de/Ontologies/MDB/CrossOntologyEquivalentClass#");
        pfxJSONObject.put("mdbgui", "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#");
        pfxJSONObject.put("mdbguielements", "http://www.morphdbase.de/Ontologies/MDB/MDB_GUIElements#");

        return pfxJSONObject;
    }
}
