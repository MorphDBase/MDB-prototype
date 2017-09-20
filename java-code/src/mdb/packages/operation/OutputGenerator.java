/*
 * Created by Roman Baum on 11.12.15.
 * Last modified by Roman Baum on 20.09.17.
 */

package mdb.packages.operation;

import mdb.basic.MDBURLEncoder;
import mdb.basic.TDBPath;
import mdb.mongodb.MongoDBConnection;
import mdb.packages.JenaIOTDBFactory;
import mdb.packages.MDBJSONObjectFactory;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.SPARQLFilter;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class OutputGenerator {

    private String pathToOntologies = OntologiesPath.pathToOntology;

    private String mdbCoreID = "", mdbEntryID = "", mdbUEID = "";

    private MongoDBConnection mongoDBConnection = new MongoDBConnection("localhost", 27017);

    private JSONArray resourcesToCheck = new JSONArray();


    /**
     * Default constructor
     */
    public OutputGenerator() {

    }


    /**
     * A constructor which provide a specific MDBUserEntryID for further calculations
     * @param mdbUEID contains the uri of the MDBUserEntryID
     */
    public OutputGenerator(String mdbUEID) {

        this.mdbUEID = mdbUEID;

    }


    /**
     * A constructor which provide a specific MDBCoreID, MDBEntryID and MDBUserEntryID for further calculations
     * @param mdbCoreID contains the uri of the MDBCoreID
     * @param mdbEntryID contains the uri of the MDBEntryID
     * @param mdbUEID contains the uri of the MDBUserEntryID
     */
    public OutputGenerator(String mdbCoreID, String mdbEntryID, String mdbUEID) {

        this.mdbCoreID = mdbCoreID;

        this.mdbEntryID = mdbEntryID;

        this.mdbUEID = mdbUEID;

    }


    /**
     * This method calculate the root parent URI of an input
     * @param calculateFromURI contains an start URI to find the root parent URI
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return calculates the fhe root parent URI
     */
    public String calculateRootResource(String calculateFromURI, JenaIOTDBFactory connectionToTDB) {

        FilterBuilder filterBuilder = new FilterBuilder();

        SelectBuilder selectBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO.addWhere("?s", "?p", "?o");

        selectBuilder.addVar(selectBuilder.makeVar("?o"));

        selectBuilder.addGraph("?g", tripleSPO);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, "?s", "<" + calculateFromURI + ">");

        filterItems = filterBuilder.addItems(filterItems, "?p", "mdbgui:MDB_GUI_0000000039");

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

        filterItems.clear();

        String sparqlQueryString = selectBuilder.buildString();

        return connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?o");

    }

    /**
     * This method get the corresponding properties for a subject class resource from the jena tdb and save the
     * corresponding statements in an JSONObject. This method checks transitive annotation properties to find the
     * wanted statements.
     * @param resourceSubject is the URI of a individual or class resource
     * @param givenStatement contains a subject(class or individual), a property and an object for calculation
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkAnnotationAnnotationProperties (String resourceSubject, Statement givenStatement, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

        FilterBuilder filterBuilder = new FilterBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        ConstructBuilder constructBuilder = new ConstructBuilder();

        constructBuilder = prefixesBuilder.addPrefixes(constructBuilder);

        constructBuilder.addConstruct("?s", "?p", "?o");

        SelectBuilder tripleSPOConstruct = new SelectBuilder();

        tripleSPOConstruct.addWhere("?s", "?p", "?o");
        tripleSPOConstruct.addWhere("?s", "?p1", "?o1");
        tripleSPOConstruct.addWhere("?s", "?p2", "?o2");

        constructBuilder.addGraph("?g", tripleSPOConstruct);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, "?p1", "owl:annotatedSource");

        filterItems = filterBuilder.addItems(filterItems, "?o1", "<" + givenStatement.getSubject().toString() + ">");

        filterItems = filterBuilder.addItems(filterItems, "?p2", "owl:annotatedProperty");

        filterItems = filterBuilder.addItems(filterItems, "?o2", "<" + givenStatement.getPredicate().toString() + ">");

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

        filterItems.clear();

        String currNS = ResourceFactory.createResource(resourceSubject).getNameSpace();

        currNS = currNS.substring(0, currNS.length()-1);

        if (((!this.mdbCoreID.isEmpty()) && (currNS.equals(this.mdbCoreID))) ||
                ((!this.mdbEntryID.isEmpty()) && (currNS.equals(this.mdbEntryID))) ||
                ((!this.mdbUEID.isEmpty()) && (currNS.equals(this.mdbUEID)))) {

            ArrayList<String> filterRegExItems = new ArrayList<>();

            filterRegExItems.add(currNS);

            filter = sparqlFilter.getRegexSTRFilter("?g", filterRegExItems);

            constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

        }

        String sparqlQueryString = constructBuilder.buildString();

        Model constructResult = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

        StmtIterator resultIterator = constructResult.listStatements();

        while (resultIterator.hasNext()) {

            Statement currStatement = resultIterator.next();

            entryComponents = managePropertyOld(resourceSubject, currStatement, entryComponents, connectionToTDB);

        }


        return entryComponents;

    }


    /**
     * This method get the corresponding properties for a subject class resource from the jena tdb and save the
     * corresponding statements in an JSONObject.
     * @param classSubject is the URI of a ontology class
     * @param resourceSubject is the URI of a resource
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkClassProperties (String classSubject, String resourceSubject, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

        FilterBuilder filterBuilder = new FilterBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        ConstructBuilder constructBuilder = new ConstructBuilder();

        constructBuilder = prefixesBuilder.addPrefixes(constructBuilder);

        constructBuilder.addConstruct("?s", "?p", "?o");

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO.addWhere("?s", "?p", "?o");

        constructBuilder.addGraph("?g", tripleSPO);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, "?s", "<" + classSubject + ">");

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

        filterItems.clear();

        String currNS = ResourceFactory.createResource(resourceSubject).getNameSpace();

        currNS = currNS.substring(0, currNS.length() - 1);

        if (((!this.mdbCoreID.isEmpty()) && (currNS.equals(this.mdbCoreID))) ||
                ((!this.mdbEntryID.isEmpty()) && (currNS.equals(this.mdbEntryID))) ||
                ((!this.mdbUEID.isEmpty()) && (currNS.equals(this.mdbUEID)))) {

            ArrayList<String> filterRegExItems = new ArrayList<>();

            filterRegExItems.add(currNS);

            filter = sparqlFilter.getRegexSTRFilter("?g", filterRegExItems);

            constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

        }


        String sparqlQueryString = constructBuilder.buildString();

        Model constructResult = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

        StmtIterator resultIterator = constructResult.listStatements();

        while (resultIterator.hasNext()) {

            Statement currStatement = resultIterator.next();

            entryComponents = managePropertyOld(resourceSubject, currStatement, entryComponents, connectionToTDB);

        }


        return entryComponents;
    }


    /**
     * This method finds all adjacent neighbours of a resource and save them in a JSONObject
     * @param resourceToCheck contains the URI of an resource which should be checked
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with all components of an entry resource
     */
    public JSONObject checkResource (JSONObject resourceToCheck, JenaIOTDBFactory connectionToTDB) {

        FilterBuilder filterBuilder = new FilterBuilder();

        SelectBuilder selectBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO.addWhere("?s", "?p", "?o");

        selectBuilder.addVar(selectBuilder.makeVar("?o"));

        selectBuilder.addGraph("?g", tripleSPO);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, "?s", "<" + resourceToCheck.getString("uri") + ">");

        filterItems = filterBuilder.addItems(filterItems, "?p", "rdf:type");

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

        filterItems.clear();

        filterItems = filterBuilder.addItems(filterItems, "?o", "owl:NamedIndividual");

        filter = sparqlFilter.getNotINFilter(filterItems);

        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

        // todo add graph filter use ids and current resource for namespace

        String currNS = ResourceFactory.createResource(resourceToCheck.getString("uri")).getNameSpace();

        currNS = currNS.substring(0, currNS.length()-1);

        if (((!this.mdbCoreID.isEmpty()) && (currNS.equals(this.mdbCoreID))) ||
                ((!this.mdbEntryID.isEmpty()) && (currNS.equals(this.mdbEntryID))) ||
                ((!this.mdbUEID.isEmpty()) && (currNS.equals(this.mdbUEID)))) {

            ArrayList<String> filterRegExItems = new ArrayList<>();

            filterRegExItems.add(currNS);

            filter = sparqlFilter.getRegexSTRFilter("?g", filterRegExItems);

            selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

        }

        String sparqlQueryString = selectBuilder.buildString();

        String resourceSubject = resourceToCheck.getString("uri");

        String classSubject = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?o");

        TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

        if (classSubject.isEmpty() && this.pathToOntologies.equals(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494"))) {

            this.pathToOntologies = tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503");
            // MDB_WORKSPACE_DIRECTORY: MDB draft workspace directory

            classSubject = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?o");

        } else if (classSubject.isEmpty() && this.pathToOntologies.equals(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"))) {

            this.pathToOntologies = tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494");
            // MDB_WORKSPACE_DIRECTORY: MDB core workspace directory

            classSubject = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?o");

        }

        JSONObject entryComponents = new JSONObject();

        entryComponents = checkResourceProperties(resourceSubject, entryComponents, connectionToTDB);

        entryComponents = checkClassProperties(classSubject, resourceSubject, entryComponents, connectionToTDB);

        entryComponents = reorderEntryComponentsValuesOld(entryComponents);

        //System.out.println("entryComponents: " + entryComponents);

        return entryComponents;

    }


    /**
     * This method get the corresponding properties for a subject class resource from the jena tdb and save the
     * corresponding statements in an JSONObject.
     * @param resourceSubject is the URI of a individual or class resource
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkResourceProperties (String resourceSubject, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {


        FilterBuilder filterBuilder = new FilterBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        ConstructBuilder constructBuilder = new ConstructBuilder();

        constructBuilder = prefixesBuilder.addPrefixes(constructBuilder);

        constructBuilder.addConstruct("?s", "?p", "?o");

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO.addWhere("?s", "?p", "?o");

        constructBuilder.addGraph("?g", tripleSPO);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, "?s", "<" + resourceSubject + ">");

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

        filterItems.clear();

        String currNS = ResourceFactory.createResource(resourceSubject).getNameSpace();

        currNS = currNS.substring(0, currNS.length() - 1);

        if (((!this.mdbCoreID.isEmpty()) && (currNS.equals(this.mdbCoreID))) ||
                ((!this.mdbEntryID.isEmpty()) && (currNS.equals(this.mdbEntryID))) ||
                ((!this.mdbUEID.isEmpty()) && (currNS.equals(this.mdbUEID)))) {

            ArrayList<String> filterRegExItems = new ArrayList<>();

            filterRegExItems.add(currNS);

            filter = sparqlFilter.getRegexSTRFilter("?g", filterRegExItems);

            constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

        }

        String sparqlQueryString = constructBuilder.buildString();

        Model constructResult = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

        StmtIterator resultIterator = constructResult.listStatements();

        while (resultIterator.hasNext()) {

            Statement currStatement = resultIterator.next();

            entryComponents = managePropertyOld(resourceSubject, currStatement, entryComponents, connectionToTDB);

        }

        return entryComponents;

    }


    /**
     * This method creates an JSONArray with the local identifier of the identified Resources and removes the URIs od the
     * hidden elements from the output
     * @param JSONToCheckForResources contains a JSONArray with potential identified Resources
     * @return an JSONArray with identified Resources as values
     */
    private JSONArray getIdentifiedResources(JSONArray JSONToCheckForResources, JSONArray identifiedResources) {

        int startLength = JSONToCheckForResources.length();

        for (int i = (startLength - 1); i >= 0; i--) {

            if (JSONToCheckForResources.get(i) instanceof JSONObject) {

                if (JSONToCheckForResources.getJSONObject(i).has("MDB_GUI_0000000040")) {
                    // has MDB entry component

                    identifiedResources = getIdentifiedResources(JSONToCheckForResources.getJSONObject(i).getJSONArray("MDB_GUI_0000000040"), identifiedResources);

                } else if(JSONToCheckForResources.getJSONObject(i).has("MDB_CORE_0000000727")) {
                    // has editable label in named graph

                    identifiedResources = getIdentifiedResources(JSONToCheckForResources.getJSONObject(i).getJSONArray("MDB_CORE_0000000727"), identifiedResources);

                } else {

                    JSONObject objectToInsert = new JSONObject();

                    if (JSONToCheckForResources.getJSONObject(i).has("MDB_UIAP_0000000278")) {
                        // input value/resource defines keyword resource

                        objectToInsert.put("keyword", JSONToCheckForResources.getJSONObject(i).getString("MDB_UIAP_0000000278"));

                    }

                    if (JSONToCheckForResources.getJSONObject(i).has("MDB_UIAP_0000000523")) {
                        // input label value defines keyword resource

                        objectToInsert.put("keywordLabel", JSONToCheckForResources.getJSONObject(i).getString("MDB_UIAP_0000000523"));

                    }

                    if (JSONToCheckForResources.getJSONObject(i).has("classID")) {

                        objectToInsert.put("classID", JSONToCheckForResources.getJSONObject(i).getString("classID"));

                    }

                    if (JSONToCheckForResources.getJSONObject(i).has("individualID")) {

                        objectToInsert.put("individualID", JSONToCheckForResources.getJSONObject(i).getString("individualID"));

                    }

                    if (JSONToCheckForResources.getJSONObject(i).has("localID")) {

                        objectToInsert.put("localID", JSONToCheckForResources.getJSONObject(i).getString("localID"));

                    }

                    identifiedResources.put(objectToInsert);

                }

                // remove internal information from output
                JSONToCheckForResources.getJSONObject(i).remove("MDB_UIAP_0000000278");
                JSONToCheckForResources.getJSONObject(i).remove("MDB_UIAP_0000000523");
                JSONToCheckForResources.getJSONObject(i).remove("classID");
                JSONToCheckForResources.getJSONObject(i).remove("individualID");

            } else {
                // delete URIs of hidden components

                JSONToCheckForResources.remove(i);

            }



        }

        return identifiedResources;

    }


    /**
     * This method gets the path of current the work directory
     * @return the path to the current ontology workspace
     */
    public String getPathToOntologies() {
        return this.pathToOntologies;
    }


    /**
     * This method reads and coordinates the output data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param uri contains the root resource for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject getOutputJSONObjectOld(JSONObject jsonInputObject, JSONObject outputObject, String uri, JenaIOTDBFactory connectionToTDB) {

        JSONArray outputDataJSON = new JSONArray();

        JSONObject currResourceToCheck = new JSONObject();

        String parentURI = calculateRootResource(uri, connectionToTDB);

        if (parentURI.equals("")) {
            // input uri did not has "has MDB entry composition"

            parentURI = uri;

        }

        currResourceToCheck.put("uri", parentURI);

        this.resourcesToCheck.put(currResourceToCheck);

        while (!this.resourcesToCheck.isNull(0)) {

            // save calculated data in an JSON array
            outputDataJSON.put(checkResource(this.resourcesToCheck.getJSONObject(0), connectionToTDB));

            // remove the old key
            this.resourcesToCheck.remove(0);

        }

        outputDataJSON = orderOutputJSONOld(outputDataJSON);

        outputDataJSON.put(0, outputDataJSON.getJSONObject(0).getJSONObject(parentURI));

        System.out.println("before identifiedResources");

        JSONArray identifiedResources = getIdentifiedResources(outputDataJSON, new JSONArray());

        System.out.println("identifiedResources: " + identifiedResources);

        String mongoDBKey = "";

        UrlValidator keyURLValidator = new UrlValidator();

        // get a MDB url Encoder to encode the uri with utf-8
        MDBURLEncoder mdburlEncoder = new MDBURLEncoder();

        if (keyURLValidator.isValid(mdburlEncoder.encodeUrl(uri, "UTF-8"))) {

            try {

                URL url = new URL(uri);

                mongoDBKey = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

            } catch (MalformedURLException e) {

                System.out.println("INFO: the variable 'mongoDBKey' contains no valid URL.");

            }

        } else {

            mongoDBKey = uri;

        }

        if (!identifiedResources.isNull(0)) {

            if (this.mongoDBConnection.collectionExist("mdb-prototyp", "sessions")) {

                System.out.println("Collection already exist");

                if (!this.mongoDBConnection.documentExist("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"))) {

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"));

                    this.mongoDBConnection.createCollection("mdb-prototyp", jsonInputObject.getString("connectSID"));

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                } else {

                    if (this.mongoDBConnection.documentExistNew("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey)) {

                        System.out.println("There exist a document for this key!");

                        if (!this.mongoDBConnection.documentWithDataExist("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources)) {

                            this.mongoDBConnection.putDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                        } else {

                            System.out.println("The document already exist in the collection");
                        }

                    } else {

                        this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                    }

                }

            } else {

                this.mongoDBConnection.createCollection("mdb-prototyp", "sessions");

                this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"));

                this.mongoDBConnection.createCollection("mdb-prototyp", jsonInputObject.getString("connectSID"));

                this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

            }

            System.out.println("Connect SID: " + jsonInputObject.getString("connectSID"));

        }

        outputObject.put("data", outputDataJSON);

        return outputObject;

    }


    /**
     * This method reads and coordinates the output data for a panel
     * @param root contains an URI
     * @param jsonInputObject contains the information for the calculation
     * @param outputDataJSON contains the output information
     */
    public void getOutputJSONObject(String root, JSONObject jsonInputObject, JSONArray outputDataJSON) {

        JSONArray identifiedResources = getIdentifiedResources(outputDataJSON, new JSONArray());

        //System.out.println("identifiedResources: " + identifiedResources);

        String mongoDBKey = root;

        try {

            URL url = new URL(mongoDBKey);

            mongoDBKey = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

        } catch (MalformedURLException e) {

            System.out.println("INFO: the variable 'mongoDBKey' contains no valid URL.");

        }

        if (!identifiedResources.isNull(0)) {

            if (this.mongoDBConnection.collectionExist("mdb-prototyp", "sessions")) {

                System.out.println("Collection already exist");

                if (!this.mongoDBConnection.documentExist("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"))) {

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"));

                    this.mongoDBConnection.createCollection("mdb-prototyp", jsonInputObject.getString("connectSID"));

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                } else {

                    if (this.mongoDBConnection.documentExistNew("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey)) {

                        System.out.println("There exist a document for this key!");

                        if (!this.mongoDBConnection.documentWithDataExist("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources)) {

                            this.mongoDBConnection.putDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                        } else {

                            System.out.println("The document already exist in the collection");
                        }

                    } else {

                        this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                    }

                }

            } else {

                this.mongoDBConnection.createCollection("mdb-prototyp", "sessions");

                this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"));

                this.mongoDBConnection.createCollection("mdb-prototyp", jsonInputObject.getString("connectSID"));

                this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonInputObject.getString("connectSID"), mongoDBKey, identifiedResources);

            }

            System.out.println("Connect SID: " + jsonInputObject.getString("connectSID"));

        }

    }


    /**
     * This method fills the JSONObject with data of an entry component corresponding to a specific property.
     * @param resourceSubject is the URI of a individual resource
     * @param currStatement contains a subject(class or individual), a property and an object for calculation
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject managePropertyOld(String resourceSubject, Statement currStatement, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

        String propertyToCheck = currStatement.getPredicate().toString();

        JSONObject currComponentObject = new JSONObject();

        switch (propertyToCheck) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000040" :
                // has MDB entry component

                JSONObject newResourceToCheck = new JSONObject();

                newResourceToCheck.put("uri", currStatement.getObject().asResource().toString());

                this.resourcesToCheck.put(newResourceToCheck);

                currComponentObject.append(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString());

                for (int i = 0; i < entryComponents.length(); i++) {

                    if (entryComponents.getJSONArray(currStatement.getSubject().toString()).getJSONObject(i).has(currStatement.getPredicate().getLocalName())) {

                        entryComponents.getJSONArray(currStatement.getSubject().toString()).getJSONObject(i).accumulate(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString());

                        return entryComponents;

                    }

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);



            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000037" :
                // MDB entry component of
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000418" :
                // has MDB CSS class
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000019" :
                // has GUI input type
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000278" :
                // input value/resource defines keyword resource
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000523" :
                // input label value defines keyword resource

                if (currStatement.getObject().isLiteral()) {
                    // todo remove this then case when the switch to page object is no longer missing

                    break;

                } else {

                    currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().getLocalName());

                    return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);
                }

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000193" :
                // has GUI representation

                // calculate MDB_UIAP_0000000220
                entryComponents = checkAnnotationAnnotationProperties(resourceSubject, currStatement, entryComponents, connectionToTDB );

                //change the subject of the current statement
                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().getLocalName());

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000042" :
                // has position in MDB entry component
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000386" :
                // required input (BOOLEAN)

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                String currObject = currStatement.getObject().asLiteral().getValue().toString();

                if (currObject.contains("^^")) {

                    currObject = currObject.substring(0, currObject.indexOf("^^"));

                }

                currComponentObject.put(currStatement.getPredicate().getLocalName(), currObject);

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000716" :
                // has user/GUI input [URI]
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000445" :
                // has selected resource
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000413" :
                // autocomplete for ontology

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString());

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000460" :
                // has user/GUI input [input_A] (data property)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000461" :
                // has user/GUI input [value_B] (data property)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000462" :
                // has user/GUI input [value_C] (data property)
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000287" :
                // has visible label 2
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000417" :
                // new row [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000018" :
                // tooltip text
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000201" :
                // label 1
                // todo maybe this property MDB_UIAP_0000000201 must switch with MDB_GUI_0000000088 in the future
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000204" :
                // hidden [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000220" :
                // with information text
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000260" :
                // sign up comment
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000399" :
                // new row [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000457" :
                // has default placeholder value

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                if (currStatement.getObject().isResource()) {

                    if (currStatement.getObject().toString().equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000161")) {
                        // KEYWORD: this MDB core ID

                        currComponentObject.put(currStatement.getPredicate().getLocalName(), this.mdbCoreID);

                    } else {

                        currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().toString());

                    }

                } else {

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asLiteral().getLexicalForm());

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000454" :
                // has user/GUI input [input_A] (object property)

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                if (currStatement.getObject().asResource().toString().startsWith("mailto:")) {
                    // special case mail

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString().substring(7));

                } else {

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().getLocalName());

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000023" :
                // input restricted to subclasses of

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                JSONArray selectClassData = new JSONArray();

                // create query to find value in specific composition
                String resultVarClass = "?s";

                PrefixesBuilder prefixesBuilderClass = new PrefixesBuilder();

                SelectBuilder selectBuilderClass = new SelectBuilder();

                selectBuilderClass = prefixesBuilderClass.addPrefixes(selectBuilderClass);

                SelectBuilder tripleSPOConstructClass = new SelectBuilder();

                tripleSPOConstructClass.addWhere(resultVarClass, "<http://www.w3.org/2000/01/rdf-schema#subClassOf>", "<" + currStatement.getObject() + ">");

                selectBuilderClass.addVar(selectBuilderClass.makeVar(resultVarClass));

                selectBuilderClass.addGraph("?g", tripleSPOConstructClass);

                String sparqlQueryStringClass = selectBuilderClass.buildString();

                JSONArray classJA = connectionToTDB.pullMultipleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringClass, resultVarClass);

                for (int i = 0; i < classJA.length(); i++) {

                    String resultVarLabel = "?s";

                    PrefixesBuilder prefixesBuilderLabel = new PrefixesBuilder();

                    SelectBuilder selectBuilderLabel = new SelectBuilder();

                    selectBuilderLabel = prefixesBuilderLabel.addPrefixes(selectBuilderLabel);

                    SelectBuilder tripleSPOConstructLabel = new SelectBuilder();

                    tripleSPOConstructLabel.addWhere( "<" + classJA.get(i) + ">", "<http://www.w3.org/2000/01/rdf-schema#label>", resultVarLabel);

                    selectBuilderLabel.addVar(selectBuilderLabel.makeVar(resultVarLabel));

                    selectBuilderLabel.addGraph("?g", tripleSPOConstructLabel);

                    String sparqlQueryStringLabel = selectBuilderLabel.buildString();

                    String label = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringLabel, resultVarLabel);

                    // find tooltip text

                    String resultVarTooltip = "?s";

                    PrefixesBuilder prefixesBuilderTooltip = new PrefixesBuilder();

                    SelectBuilder selectBuilderTooltip = new SelectBuilder();

                    selectBuilderTooltip = prefixesBuilderTooltip.addPrefixes(selectBuilderTooltip);

                    SelectBuilder tripleSPOConstructTooltip = new SelectBuilder();

                    tripleSPOConstructTooltip.addWhere("<" + classJA.get(i) + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000018>", resultVarTooltip);
                    // tooltip text

                    selectBuilderTooltip.addVar(selectBuilderTooltip.makeVar(resultVarTooltip));

                    selectBuilderTooltip.addGraph("?g", tripleSPOConstructTooltip);

                    String sparqlQueryStringTooltip = selectBuilderTooltip.buildString();

                    String tooltipText = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringTooltip, resultVarTooltip);

                    // save date in output stream

                    JSONObject currSelectIndividual = new JSONObject();

                    currSelectIndividual.put("selValue", classJA.get(i));
                    currSelectIndividual.put("selLabel", label);

                    if (!tooltipText.isEmpty()) {

                        currSelectIndividual.put("MDB_UIAP_0000000018", tooltipText);

                    }

                    selectClassData.put(currSelectIndividual);

                }

                currComponentObject.put(currStatement.getPredicate().getLocalName(), selectClassData);

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000118" :
                // input restricted to individuals of

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                JSONArray selectIndividualData = new JSONArray();

                // create query to find value in specific composition
                String resultVarValues = "?s";

                PrefixesBuilder prefixesBuilderValues = new PrefixesBuilder();

                SelectBuilder selectBuilderValues = new SelectBuilder();

                selectBuilderValues = prefixesBuilderValues.addPrefixes(selectBuilderValues);

                SelectBuilder tripleSPOConstructValues = new SelectBuilder();

                tripleSPOConstructValues.addWhere(resultVarValues , "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<" + currStatement.getObject() + ">");

                selectBuilderValues.addVar(selectBuilderValues.makeVar(resultVarValues));

                selectBuilderValues.addGraph("?g", tripleSPOConstructValues);

                String sparqlQueryStringValues = selectBuilderValues.buildString();

                JSONArray valuesJA = connectionToTDB.pullMultipleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringValues, resultVarValues);

                for (int i = 0; i < valuesJA.length(); i++) {

                    String resultVarLabel = "?s";

                    PrefixesBuilder prefixesBuilderLabel = new PrefixesBuilder();

                    SelectBuilder selectBuilderLabel = new SelectBuilder();

                    selectBuilderLabel = prefixesBuilderLabel.addPrefixes(selectBuilderLabel);

                    SelectBuilder tripleSPOConstructLabel = new SelectBuilder();

                    tripleSPOConstructLabel.addWhere( "<" + valuesJA.get(i) + ">", "<http://www.w3.org/2000/01/rdf-schema#label>", resultVarLabel);

                    selectBuilderLabel.addVar(selectBuilderLabel.makeVar(resultVarLabel));

                    selectBuilderLabel.addGraph("?g", tripleSPOConstructLabel);

                    String sparqlQueryStringLabel = selectBuilderLabel.buildString();

                    String label = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringLabel, resultVarLabel);

                    // find tooltip text

                    String resultVarTooltip = "?s";

                    PrefixesBuilder prefixesBuilderTooltip = new PrefixesBuilder();

                    SelectBuilder selectBuilderTooltip = new SelectBuilder();

                    selectBuilderTooltip = prefixesBuilderTooltip.addPrefixes(selectBuilderTooltip);

                    SelectBuilder tripleSPOConstructTooltip = new SelectBuilder();

                    tripleSPOConstructTooltip.addWhere("<" + valuesJA.get(i) + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000018>", resultVarTooltip);
                    // tooltip text

                    selectBuilderTooltip.addVar(selectBuilderTooltip.makeVar(resultVarTooltip));

                    selectBuilderTooltip.addGraph("?g", tripleSPOConstructTooltip);

                    String sparqlQueryStringTooltip = selectBuilderTooltip.buildString();

                    String tooltipText = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringTooltip, resultVarTooltip);

                    // save date in output stream

                    JSONObject currSelectIndividual = new JSONObject();

                    currSelectIndividual.put("selValue", valuesJA.get(i));
                    currSelectIndividual.put("selLabel", label);

                    if (!tooltipText.isEmpty()) {

                        currSelectIndividual.put("MDB_UIAP_0000000018", tooltipText);

                    }

                    selectIndividualData.put(currSelectIndividual);

                }

                currComponentObject.put(currStatement.getPredicate().getLocalName(), selectIndividualData);

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000268" :
                // component status [BOOLEAN]

                String property;

                if (currStatement.getObject().asLiteral().getLexicalForm().equals("true")) {

                    property = "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000266";
                    // label status 'true'


                } else {

                    property = "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000267";
                    // label status 'false'

                }

                // create query to find value in specific composition
                String resultVar = "?o";

                PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                SelectBuilder selectBuilder = new SelectBuilder();

                selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                SelectBuilder tripleSPOConstruct = new SelectBuilder();

                tripleSPOConstruct.addWhere("?bNode", "<http://www.w3.org/2002/07/owl#annotatedSource>", "<" + resourceSubject + ">");
                tripleSPOConstruct.addWhere("?bNode", "<" + property + ">", "?o");

                selectBuilder.addVar(selectBuilder.makeVar(resultVar));

                selectBuilder.addGraph("?g", tripleSPOConstruct);

                String sparqlQueryString = selectBuilder.buildString();

                String value = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, resultVar);

                currComponentObject.put(ResourceFactory.createProperty(property).getLocalName(), value);

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000274" :
                // execution step trigger
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000279" :
                // execution step: MDB hyperlink


                // calculate MDB_UIAP_0000000019 + MDB_UIAP_0000000278 and return entryComponents
                return checkAnnotationAnnotationProperties(resourceSubject, currStatement, entryComponents, connectionToTDB);

            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" :

                switch (currStatement.getObject().toString()) {

                    case "http://www.w3.org/2002/07/owl#NamedIndividual" :
                    case "http://www.w3.org/2002/07/owl#Axiom" :
                    case "http://www.w3.org/2002/07/owl#Class" :

                        break;

                    default:

                        // save the class URI of the individual
                        currComponentObject.put("classID", currStatement.getObject().asResource().toString());

                        // save the individual URI of the individual
                        currComponentObject.put("individualID", currStatement.getSubject().asResource().toString());

                        // save the local identifier of the individual
                        currComponentObject.put("localID", currStatement.getSubject().asResource().getLocalName());

                        return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

                }

            default:

                // differ potential interesting properties and uninteresting properties
                if (unknownProperty(currStatement.getPredicate().toString())) {

                    System.out.println("potential Statement to process: " + currStatement);

                }

        }

        return entryComponents;

    }


    /**
     * This method fills the JSONObject with data of an entry component corresponding to a specific property.
     * @param resourceSubject is the URI of a individual resource
     * @param currStatement contains a subject(class or individual), a property and an object for calculation
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject manageProperty (String resourceSubject, Statement currStatement, JSONObject entryComponents,
                                      JSONObject jsonInputObject , JenaIOTDBFactory connectionToTDB) {

        //System.out.println("currStatement = " + currStatement);

        String propertyToCheck = currStatement.getPredicate().toString();

        JSONObject currComponentObject = new JSONObject();

        switch (propertyToCheck) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000040" :
                // has MDB entry component

                currComponentObject.append(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString());

                for (int i = 0; i < entryComponents.length(); i++) {

                    if (entryComponents.has(currStatement.getSubject().toString())) {

                        JSONArray innerJSONArray = entryComponents.getJSONArray(currStatement.getSubject().toString());

                        for (int j = 0; j < innerJSONArray.length(); j++) {

                            if (innerJSONArray.getJSONObject(j).has(currStatement.getPredicate().getLocalName())) {

                                entryComponents.getJSONArray(currStatement.getSubject().toString()).getJSONObject(i).accumulate(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString());

                                return entryComponents;

                            }

                        }

                    }

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000037" :
                // MDB entry component of
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000418" :
                // has MDB CSS class
            case "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000002740":
                // has label input MDB entry component
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000019" :
                // has GUI input type
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000193" :
                // has GUI representation
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000278" :
                // input value/resource defines keyword resource
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000523" :
                //input label value defines keyword resource

                if (currStatement.getObject().isLiteral()) {
                    // todo remove this then case when the switch to page object is no longer missing

                    break;

                } else {

                    currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().getLocalName());

                    return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);
                }

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000042" :
                // has position in MDB entry component
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000386" :
                // required input (BOOLEAN)

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                String currObject = currStatement.getObject().asLiteral().getValue().toString();

                if (currObject.contains("^^")) {

                    currObject = currObject.substring(0, currObject.indexOf("^^"));

                }

                currComponentObject.put(currStatement.getPredicate().getLocalName(), currObject);

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000716" :
                // has user/GUI input [URI]
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000445" :
                // has selected resource
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000413" :
                // autocomplete for ontology

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString());

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000460" :
                // has user/GUI input [input_A] (data property)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000461" :
                // has user/GUI input [value_B] (data property)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000462" :
                // has user/GUI input [value_C] (data property)
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000287" :
                // has visible label 2
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000417" :
                // new row [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000018" :
                // tooltip text
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000201" :
                // label 1
                // todo maybe this property MDB_UIAP_0000000201 must switch with MDB_GUI_0000000088 in the future
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000204" :
                // hidden [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000220" :
                // with information text
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000260" :
                // sign up comment
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000399" :
                // new row [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000447" :
                // show expanded (this entry's specific individual of)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000457" :
                // has default placeholder value
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000527" :
                // has partonomy label

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                if (currStatement.getObject().isResource()) {

                    if (currStatement.getObject().toString().equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000161")) {
                        // KEYWORD: this MDB core ID

                        currComponentObject.put(currStatement.getPredicate().getLocalName(), this.mdbCoreID);

                    } else {

                        currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().toString());

                    }

                } else {

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asLiteral().getLexicalForm());

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);


            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000454" :
                // has user/GUI input [input_A] (object property)

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                if (currStatement.getObject().asResource().toString().startsWith("mailto:")) {
                    // special case mail

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().toString().substring(7));

                } else {

                    currComponentObject.put(currStatement.getPredicate().getLocalName(), currStatement.getObject().asResource().getLocalName());

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000727" :
                // has editable label in named graph

                String mdRootElementLabelNG = currStatement.getObject().toString();

                // create query to find value in specific composition
                String rootResultVar = "?o";

                PrefixesBuilder prefixesBuilderRoot = new PrefixesBuilder();

                SelectBuilder selectBuilderRoot = new SelectBuilder();

                selectBuilderRoot = prefixesBuilderRoot.addPrefixes(selectBuilderRoot);

                SelectBuilder tripleSPOConstructRoot = new SelectBuilder();

                tripleSPOConstructRoot.addWhere("<" + mdRootElementLabelNG + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000652>", rootResultVar);
                // composition in this named graph has root entry component

                selectBuilderRoot.addVar(selectBuilderRoot.makeVar(rootResultVar));

                selectBuilderRoot.addGraph("<" + mdRootElementLabelNG + ">", tripleSPOConstructRoot);

                String sparqlQueryStringRoot = selectBuilderRoot.buildString();

                TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                String directory = tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503");
                // MDB_WORKSPACE_DIRECTORY: MDB draft workspace directory

                String root = connectionToTDB.pullSingleDataFromTDB(directory, sparqlQueryStringRoot, rootResultVar);

                if (!root.isEmpty()) {

                    boolean calculateSubGraph = false;

                    if (jsonInputObject.has("partID")) {

                        if (root.contains(jsonInputObject.getString("partID"))) {

                            calculateSubGraph = true;

                        }

                    } else if (jsonInputObject.has("localID")) {
                        // special case if the user click the 'go to description button' from an entry

                        if (jsonInputObject.getString("localID").contains("MDB_DATASCHEME_0000002134")) {
                            // go to description button item

                            calculateSubGraph = true;

                        }

                    }

                    if (calculateSubGraph) {

                        MDBJSONObjectFactory mdbjsonObjectFactory = new MDBJSONObjectFactory();

                        JSONArray ngs = new JSONArray();

                        ngs.put(mdRootElementLabelNG);

                        JSONArray subGraph = mdbjsonObjectFactory.getCompositionFromStoreForOutput(root, ngs,  directory, jsonInputObject, connectionToTDB);

                        currComponentObject.put(currStatement.getPredicate().getLocalName(), subGraph);

                    }

                }

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);


            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000023" :
                // input restricted to subclasses of

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                JSONArray selectClassData = new JSONArray();

                // create query to find value in specific composition
                String resultVarClass = "?s";

                PrefixesBuilder prefixesBuilderClass = new PrefixesBuilder();

                SelectBuilder selectBuilderClass = new SelectBuilder();

                selectBuilderClass = prefixesBuilderClass.addPrefixes(selectBuilderClass);

                SelectBuilder tripleSPOConstructClass = new SelectBuilder();

                tripleSPOConstructClass.addWhere(resultVarClass, "<http://www.w3.org/2000/01/rdf-schema#subClassOf>", "<" + currStatement.getObject() + ">");

                selectBuilderClass.addVar(selectBuilderClass.makeVar(resultVarClass));

                selectBuilderClass.addGraph("?g", tripleSPOConstructClass);

                String sparqlQueryStringClass = selectBuilderClass.buildString();

                JSONArray classJA = connectionToTDB.pullMultipleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringClass, resultVarClass);

                for (int i = 0; i < classJA.length(); i++) {

                    String resultVarLabel = "?s";

                    PrefixesBuilder prefixesBuilderLabel = new PrefixesBuilder();

                    SelectBuilder selectBuilderLabel = new SelectBuilder();

                    selectBuilderLabel = prefixesBuilderLabel.addPrefixes(selectBuilderLabel);

                    SelectBuilder tripleSPOConstructLabel = new SelectBuilder();

                    tripleSPOConstructLabel.addWhere( "<" + classJA.get(i) + ">", "<http://www.w3.org/2000/01/rdf-schema#label>", resultVarLabel);

                    selectBuilderLabel.addVar(selectBuilderLabel.makeVar(resultVarLabel));

                    selectBuilderLabel.addGraph("?g", tripleSPOConstructLabel);

                    String sparqlQueryStringLabel = selectBuilderLabel.buildString();

                    String label = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringLabel, resultVarLabel);

                    // find tooltip text

                    String resultVarTooltip = "?s";

                    PrefixesBuilder prefixesBuilderTooltip = new PrefixesBuilder();

                    SelectBuilder selectBuilderTooltip = new SelectBuilder();

                    selectBuilderTooltip = prefixesBuilderTooltip.addPrefixes(selectBuilderTooltip);

                    SelectBuilder tripleSPOConstructTooltip = new SelectBuilder();

                    tripleSPOConstructTooltip.addWhere("<" + classJA.get(i) + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000018>", resultVarTooltip);
                    // tooltip text

                    selectBuilderTooltip.addVar(selectBuilderTooltip.makeVar(resultVarTooltip));

                    selectBuilderTooltip.addGraph("?g", tripleSPOConstructTooltip);

                    String sparqlQueryStringTooltip = selectBuilderTooltip.buildString();

                    String tooltipText = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringTooltip, resultVarTooltip);

                    // save date in output stream

                    JSONObject currSelectIndividual = new JSONObject();

                    currSelectIndividual.put("selValue", classJA.get(i));
                    currSelectIndividual.put("selLabel", label);

                    if (!tooltipText.isEmpty()) {

                        currSelectIndividual.put("MDB_UIAP_0000000018", tooltipText);

                    }

                    selectClassData.put(currSelectIndividual);

                }

                currComponentObject.put(currStatement.getPredicate().getLocalName(), selectClassData);

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000037" :
                // hidden for users without right or role

                System.out.println("schnap: " + currStatement);
                System.out.println("schnap: " + jsonInputObject);


                // todo create interface to ask right or role in user named graph http://www.morphdbase.de/resource/de46dd64#MDB_CORE_0000000407_1

                return entryComponents;

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000118" :
                // input restricted to individuals of

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                JSONArray selectIndividualData = new JSONArray();

                // create query to find value in specific composition
                String resultVarValues = "?s";

                PrefixesBuilder prefixesBuilderValues = new PrefixesBuilder();

                SelectBuilder selectBuilderValues = new SelectBuilder();

                selectBuilderValues = prefixesBuilderValues.addPrefixes(selectBuilderValues);

                SelectBuilder tripleSPOConstructValues = new SelectBuilder();

                tripleSPOConstructValues.addWhere(resultVarValues , "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<" + currStatement.getObject() + ">");

                selectBuilderValues.addVar(selectBuilderValues.makeVar(resultVarValues));

                selectBuilderValues.addGraph("?g", tripleSPOConstructValues);

                String sparqlQueryStringValues = selectBuilderValues.buildString();

                JSONArray valuesJA = connectionToTDB.pullMultipleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringValues, resultVarValues);

                for (int i = 0; i < valuesJA.length(); i++) {

                    String resultVarLabel = "?s";

                    PrefixesBuilder prefixesBuilderLabel = new PrefixesBuilder();

                    SelectBuilder selectBuilderLabel = new SelectBuilder();

                    selectBuilderLabel = prefixesBuilderLabel.addPrefixes(selectBuilderLabel);

                    SelectBuilder tripleSPOConstructLabel = new SelectBuilder();

                    tripleSPOConstructLabel.addWhere( "<" + valuesJA.get(i) + ">", "<http://www.w3.org/2000/01/rdf-schema#label>", resultVarLabel);

                    selectBuilderLabel.addVar(selectBuilderLabel.makeVar(resultVarLabel));

                    selectBuilderLabel.addGraph("?g", tripleSPOConstructLabel);

                    String sparqlQueryStringLabel = selectBuilderLabel.buildString();

                    String label = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringLabel, resultVarLabel);

                    // find tooltip text

                    String resultVarTooltip = "?s";

                    PrefixesBuilder prefixesBuilderTooltip = new PrefixesBuilder();

                    SelectBuilder selectBuilderTooltip = new SelectBuilder();

                    selectBuilderTooltip = prefixesBuilderTooltip.addPrefixes(selectBuilderTooltip);

                    SelectBuilder tripleSPOConstructTooltip = new SelectBuilder();

                    tripleSPOConstructTooltip.addWhere("<" + valuesJA.get(i) + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000018>", resultVarTooltip);
                    // tooltip text

                    selectBuilderTooltip.addVar(selectBuilderTooltip.makeVar(resultVarTooltip));

                    selectBuilderTooltip.addGraph("?g", tripleSPOConstructTooltip);

                    String sparqlQueryStringTooltip = selectBuilderTooltip.buildString();

                    String tooltipText = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryStringTooltip, resultVarTooltip);

                    // save date in output stream

                    JSONObject currSelectIndividual = new JSONObject();

                    currSelectIndividual.put("selValue", valuesJA.get(i));
                    currSelectIndividual.put("selLabel", label);

                    if (!tooltipText.isEmpty()) {

                        currSelectIndividual.put("MDB_UIAP_0000000018", tooltipText);

                    }

                    selectIndividualData.put(currSelectIndividual);

                }

                currComponentObject.put(currStatement.getPredicate().getLocalName(), selectIndividualData);

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000268" :
                // component status [BOOLEAN]

                String property;

                if (currStatement.getObject().asLiteral().getLexicalForm().equals("true")) {

                    property = "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000266";
                    // label status 'true'


                } else {

                    property = "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000267";
                    // label status 'false'

                }

                // create query to find value in specific composition
                String resultVar = "?o";

                PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                SelectBuilder selectBuilder = new SelectBuilder();

                selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                SelectBuilder tripleSPOConstruct = new SelectBuilder();

                tripleSPOConstruct.addWhere("?bNode", "<http://www.w3.org/2002/07/owl#annotatedSource>", "<" + resourceSubject + ">");
                tripleSPOConstruct.addWhere("?bNode", "<" + property + ">", "?o");

                selectBuilder.addVar(selectBuilder.makeVar(resultVar));

                selectBuilder.addGraph("?g", tripleSPOConstruct);

                String sparqlQueryString = selectBuilder.buildString();

                String value = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, resultVar);

                currComponentObject.put(ResourceFactory.createProperty(property).getLocalName(), value);

                currStatement = ResourceFactory.createStatement(ResourceFactory.createResource(resourceSubject), currStatement.getPredicate(), currStatement.getObject());

                return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000274" :
                // execution step trigger
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000279" :
                // execution step: MDB hyperlink


                // calculate MDB_UIAP_0000000019 + MDB_UIAP_0000000278 and return entryComponents
                return checkAnnotationAnnotationProperties(resourceSubject, currStatement, entryComponents, connectionToTDB);

            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" :

                switch (currStatement.getObject().toString()) {

                    case "http://www.w3.org/2002/07/owl#NamedIndividual" :
                    case "http://www.w3.org/2002/07/owl#Axiom" :
                    case "http://www.w3.org/2002/07/owl#Class" :

                        break;

                    default:

                        // save the class URI of the individual
                        currComponentObject.put("classID", currStatement.getObject().asResource().toString());

                        // save the individual URI of the individual
                        currComponentObject.put("individualID", currStatement.getSubject().asResource().toString());

                        // save the local identifier of the individual
                        currComponentObject.put("localID", currStatement.getSubject().asResource().getLocalName());

                        return entryComponents.append(currStatement.getSubject().toString(), currComponentObject);

                }

            default:

                // differ potential interesting properties and uninteresting properties
                if (unknownProperty(currStatement.getPredicate().toString())) {

                    System.out.println("potential Statement to process: " + currStatement);

                }

        }

        return entryComponents;

    }


    /**
     * This method orders an JSONArray related to their position resource
     * @param entryComponentJSONArray this JSONArray contains entry component specific data
     * @param JSONArrayData contains unordered data
     * @param inputPosition the input position of the JSONArray
     * @param parentURI the URI of the parent Resource
     * @return a JSONArray with the data of an entry component
     */
    public JSONArray orderEntryComponents(JSONArray entryComponentJSONArray, JSONArray JSONArrayData, int inputPosition, String parentURI) {

        ArrayList<Integer> entriesToDelete = new ArrayList<>();

        JSONArray entriesOrder = new JSONArray();

        for (int i = 0; i < entryComponentJSONArray.length(); i++) {

            entriesOrder.put("");

        }

        JSONObject alreadyFoundURIIndex = new JSONObject();

        for (int i = 0; i < JSONArrayData.length(); i++) {

            Iterator keyIter = JSONArrayData.getJSONObject(i).keys();

            boolean keyNotFound = true;

            while (keyIter.hasNext() && keyNotFound) {

                String currKey = keyIter.next().toString();

                for (int j = (entryComponentJSONArray.length()-1); j >= 0; j--) {

                    if ((currKey.equals(entryComponentJSONArray.get(j).toString())) &&
                            (!(entryComponentJSONArray.get(j).toString()).equals(parentURI)) &&
                            (!alreadyFoundURIIndex.has(currKey))) {

                        if (JSONArrayData.getJSONObject(i).getJSONObject(currKey).has("MDB_UIAP_0000000204")) {

                            boolean hidden = Boolean.parseBoolean(JSONArrayData.getJSONObject(i).getJSONObject(currKey).getString("MDB_UIAP_0000000204"));

                            if (!hidden) {
                                // show only not hidden parts

                                entriesOrder.put((Integer.parseInt(JSONArrayData.getJSONObject(i).getJSONObject(currKey).getString("MDB_GUI_0000000042")) - 1), JSONArrayData.getJSONObject(i).getJSONObject(currKey));

                                entriesToDelete.add(i);

                                alreadyFoundURIIndex.put(entryComponentJSONArray.get(j).toString(), entriesToDelete.indexOf(i));

                                keyNotFound = false;

                            } else {

                                entriesToDelete.add(i);

                            }

                        } else {

                            entriesOrder.put((Integer.parseInt(JSONArrayData.getJSONObject(i).getJSONObject(currKey).getString("MDB_GUI_0000000042")) - 1), JSONArrayData.getJSONObject(i).getJSONObject(currKey));

                            entriesToDelete.add(i);

                            alreadyFoundURIIndex.put(entryComponentJSONArray.get(j).toString(), entriesToDelete.indexOf(i));

                            keyNotFound = false;

                        }

                    } else if ((currKey.equals(entryComponentJSONArray.get(j).toString())) &&
                            (!(entryComponentJSONArray.get(j).toString()).equals(parentURI))) {
                        // if there are multiple occurrence of a resource find the last index in the delete arraylist

                        entriesToDelete.set(alreadyFoundURIIndex.getInt(currKey), i);

                    }

                }

            }

        }

        for (int i = (entriesOrder.length()-1); i >= 0; i--) {

            if (entriesOrder.get(i).equals("")) {

                entriesOrder.remove(i);

            } else if (entriesOrder.isNull(i)) {

                entriesOrder.remove(i);

            }

        }

        //System.out.println("entriesOrder " + entriesOrder);

        JSONArrayData.getJSONObject(inputPosition).getJSONObject(parentURI).put("MDB_GUI_0000000040", entriesOrder);

        // sort the array from small to large for the case of multiple occurrence of a resource in the arraylist
        Collections.sort(entriesToDelete, Integer::compareTo);

        int arraySizeBeforeDelete = JSONArrayData.length();

        // delete deprecated information
        for (int i = (entriesToDelete.size()-1); i >= 0; i--) {

            System.out.println("entriesToDelete.get(" + i + ") = " + entriesToDelete.get(i));

            if (arraySizeBeforeDelete >= JSONArrayData.length()) {

                JSONArrayData.remove(entriesToDelete.get(i));

            }

        }

        return entryComponentJSONArray;

    }


    /**
     * This method organize an input JSONArray in a nested and ordered JSONArray
     * @param JSONArrayData contains unordered data
     * @return a nested and ordered JSONArray
     */
    public JSONArray orderOutputJSONOld(JSONArray JSONArrayData) {

        for (int i = (JSONArrayData.length()-1); i >= 0; i--) {

            Iterator allKeys = JSONArrayData.getJSONObject(i).keys();

            while (allKeys.hasNext()) {

                String currKey = allKeys.next().toString();

                if (JSONArrayData.getJSONObject(i).has(currKey)) {

                    if (JSONArrayData.getJSONObject(i).getJSONObject(currKey).has("MDB_GUI_0000000040")) {

                        //System.out.println("key value = " + JSONArrayData.getJSONObject(i).getJSONObject(currKey));

                        JSONArray entryComponentJSONArray = JSONArrayData.getJSONObject(i).getJSONObject(currKey).getJSONArray("MDB_GUI_0000000040");

                        orderEntryComponents(entryComponentJSONArray, JSONArrayData, i, currKey);

                    }

                }

            }

        }

        return JSONArrayData;

    }


    /**
     * This method organize an input JSONArray in a nested and ordered JSONArray
     * @param rootURI contains the URI of the root element
     * @param JSONArrayData contains unordered flat JSONArray
     * @return a nested and ordered JSONArray
     */
    public JSONArray orderOutputJSON(String rootURI, JSONArray JSONArrayData) {

        JSONArray outputTreeDataJSON = new JSONArray();

        boolean wasNotFound = true;// todo remove this part if the user database was reset

        for (int i = (JSONArrayData.length()-1); i >= 0; i--) {

            if (JSONArrayData.getJSONObject(i).has(rootURI)) {

                wasNotFound = false;// todo remove this part if the user database was reset

                int position;
                // has position in MDB entry componentURI

                if (!JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000042")) {

                    System.out.println("Error: the following component has no position: " + rootURI);

                    position = 1;// todo remove the then branch if the user database was reset

                } else {

                    position = Integer.parseInt(JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getString("MDB_GUI_0000000042"));

                }

                JSONArray childrenOfComponent = new JSONArray();

                if (JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000040")) {

                    childrenOfComponent = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040");

                    int numberOfChildren = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040").length();

                    if (numberOfChildren >= 0) {

                        JSONArray childrenOfComponentPlaceholder = new JSONArray();

                        for (int j = 0; j < numberOfChildren; j++) {

                            childrenOfComponentPlaceholder.put(j);

                        }

                        JSONArrayData.getJSONObject(i).getJSONObject(rootURI).put("MDB_GUI_0000000040", childrenOfComponentPlaceholder);

                    }

                }

                outputTreeDataJSON = putComponentInTree(rootURI, JSONArrayData, childrenOfComponent, position, outputTreeDataJSON);

                if (outputTreeDataJSON.getJSONObject(position - 1).has("MDB_GUI_0000000040")) {

                    // check if some root children are hidden, if true remove the placeholder from the output tree
                    JSONArray rootChildren = outputTreeDataJSON.getJSONObject(position - 1).getJSONArray("MDB_GUI_0000000040");

                    for (int j = (rootChildren.length()-1); j >= 0; j--) {

                        if (!(rootChildren.get(j) instanceof JSONObject)) {

                            rootChildren.remove(j);

                        }

                    }

                }

            }

        }

        if (wasNotFound && rootURI.contains("MDB_DATASCHEME_0000000703_1")) { // todo remove this if-case if the user database was reset

            rootURI = ResourceFactory.createResource(rootURI).getNameSpace() + "MDB_DATASCHEME_0000000733_1";

            for (int i = (JSONArrayData.length()-1); i >= 0; i--) {

                if (JSONArrayData.getJSONObject(i).has(rootURI)) {

                    int position;
                    // has position in MDB entry componentURI

                    if (!JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000042")) {

                        System.out.println("Error: the following component has no position: " + rootURI);

                        position = 1;

                    } else {

                        position = Integer.parseInt(JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getString("MDB_GUI_0000000042"));

                    }

                    JSONArray childrenOfComponent = new JSONArray();

                    if (JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000040")) {

                        childrenOfComponent = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040");

                        int numberOfChildren = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040").length();

                        if (numberOfChildren >= 0) {

                            JSONArray childrenOfComponentPlaceholder = new JSONArray();

                            for (int j = 0; j < numberOfChildren; j++) {

                                childrenOfComponentPlaceholder.put(j);

                            }

                            JSONArrayData.getJSONObject(i).getJSONObject(rootURI).put("MDB_GUI_0000000040", childrenOfComponentPlaceholder);

                        }

                    }

                    outputTreeDataJSON = putComponentInTree(rootURI, JSONArrayData, childrenOfComponent, position, outputTreeDataJSON);

                    if (outputTreeDataJSON.getJSONObject(position - 1).has("MDB_GUI_0000000040")) {

                        // check if some root children are hidden, if true remove the placeholder from the output tree
                        JSONArray rootChildren = outputTreeDataJSON.getJSONObject(position - 1).getJSONArray("MDB_GUI_0000000040");

                        for (int j = (rootChildren.length()-1); j >= 0; j--) {

                            if (!(rootChildren.get(j) instanceof JSONObject)) {

                                rootChildren.remove(j);

                            }

                        }

                    }

                }

            }

        } else if (wasNotFound && rootURI.contains("MDB_DATASCHEME_0000000205_1")) {// todo remove this if-case if the draft database was reset

            rootURI = ResourceFactory.createResource(rootURI).getNameSpace() + "MDB_DATASCHEME_0000000206_1";

            for (int i = (JSONArrayData.length()-1); i >= 0; i--) {

                if (JSONArrayData.getJSONObject(i).has(rootURI)) {

                    int position;
                    // has position in MDB entry componentURI

                    if (!JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000042")) {

                        System.out.println("Error: the following component has no position: " + rootURI);

                        position = 1;

                    } else {

                        position = Integer.parseInt(JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getString("MDB_GUI_0000000042"));

                    }

                    JSONArray childrenOfComponent = new JSONArray();

                    if (JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000040")) {

                        childrenOfComponent = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040");

                        int numberOfChildren = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040").length();

                        if (numberOfChildren >= 0) {

                            JSONArray childrenOfComponentPlaceholder = new JSONArray();

                            for (int j = 0; j < numberOfChildren; j++) {

                                childrenOfComponentPlaceholder.put(j);

                            }

                            JSONArrayData.getJSONObject(i).getJSONObject(rootURI).put("MDB_GUI_0000000040", childrenOfComponentPlaceholder);

                        }

                    }

                    outputTreeDataJSON = putComponentInTree(rootURI, JSONArrayData, childrenOfComponent, position, outputTreeDataJSON);

                    if (outputTreeDataJSON.getJSONObject(position - 1).has("MDB_GUI_0000000040")) {

                        // check if some root children are hidden, if true remove the placeholder from the output tree
                        JSONArray rootChildren = outputTreeDataJSON.getJSONObject(position - 1).getJSONArray("MDB_GUI_0000000040");

                        for (int j = (rootChildren.length()-1); j >= 0; j--) {

                            if (!(rootChildren.get(j) instanceof JSONObject)) {

                                rootChildren.remove(j);

                            }

                        }

                    }

                }

            }

        }

        return outputTreeDataJSON;

    }


    /**
     * This method organize an input JSONArray in a nested and ordered JSONArray
     * @param rootURI contains the URI of the root element
     * @param JSONArrayData contains unordered flat JSONArray
     * @return a nested and ordered JSONArray
     */
    public JSONArray orderSubCompositionOutputJSON(String rootURI, JSONArray JSONArrayData) {

        JSONArray outputTreeDataJSON = new JSONArray();


        for (int i = (JSONArrayData.length()-1); i >= 0; i--) {

            if (JSONArrayData.getJSONObject(i).has(rootURI)) {

                int position = 1; // there is only one position for each sub composition tree

                JSONArray childrenOfComponent = new JSONArray();

                if (JSONArrayData.getJSONObject(i).getJSONObject(rootURI).has("MDB_GUI_0000000040")) {

                    childrenOfComponent = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040");

                    int numberOfChildren = JSONArrayData.getJSONObject(i).getJSONObject(rootURI).getJSONArray("MDB_GUI_0000000040").length();

                    if (numberOfChildren >= 0) {

                        JSONArray childrenOfComponentPlaceholder = new JSONArray();

                        for (int j = 0; j < numberOfChildren; j++) {

                            childrenOfComponentPlaceholder.put(j);

                        }

                        JSONArrayData.getJSONObject(i).getJSONObject(rootURI).put("MDB_GUI_0000000040", childrenOfComponentPlaceholder);

                    }

                }

                outputTreeDataJSON = putComponentInTree(rootURI, JSONArrayData, childrenOfComponent, position, outputTreeDataJSON);

                // check if some root children are hidden, if true remove the placeholder from the output tree
                JSONArray rootChildren = outputTreeDataJSON.getJSONObject(position - 1).getJSONArray("MDB_GUI_0000000040");

                for (int j = (rootChildren.length()-1); j >= 0; j--) {

                    if (!(rootChildren.get(j) instanceof JSONObject)) {

                        rootChildren.remove(j);

                    }

                }

            }

        }

        return outputTreeDataJSON;

    }


    /**
     * This method constructs a nested and ordered JSONArray from a flat input JSON Array
     * @param componentURI contains the URI of the current tree component
     * @param JSONArrayData contains unordered flat JSONArray
     * @param childrenOfComponent contains the URIs of the children
     * @param position contains the position of the component
     * @param outputTreeDataJSON contains a nested and ordered JSONArray
     * @return a nested and ordered JSONArray
     */
    private JSONArray putComponentInTree(String componentURI, JSONArray JSONArrayData, JSONArray childrenOfComponent,
                                        int position, JSONArray outputTreeDataJSON) {

        // put the current data in the output tree JSON and remove the data afterwards from the flat JSON
        for (int i = 0; i < JSONArrayData.length(); i++) {

            if (JSONArrayData.getJSONObject(i).has(componentURI)) {

                outputTreeDataJSON.put(position - 1, JSONArrayData.getJSONObject(i).getJSONObject(componentURI));

            }

        }

        if (childrenOfComponent.length() > 0) {

            for (int i = 0; i < childrenOfComponent.length(); i++) {

                if (childrenOfComponent.get(i) instanceof String) {

                    String newComponentURI = childrenOfComponent.getString(i);

                    int currPosition = -1;

                    JSONArray currChildrenOfComponent = new JSONArray();

                    boolean hidden = false;

                    for (int j = 0; j < JSONArrayData.length(); j++) {

                        if (JSONArrayData.getJSONObject(j).has(newComponentURI)) {

                            if (!JSONArrayData.getJSONObject(j).getJSONObject(newComponentURI).has("MDB_GUI_0000000042")) {

                                System.out.println("Error: the following component has no position: " + newComponentURI);

                            } else {

                                currPosition = Integer.parseInt(JSONArrayData.getJSONObject(j).getJSONObject(newComponentURI).getString("MDB_GUI_0000000042"));

                            }

                            if (JSONArrayData.getJSONObject(j).getJSONObject(newComponentURI).has("MDB_GUI_0000000040")) {

                                currChildrenOfComponent = JSONArrayData.getJSONObject(j).getJSONObject(newComponentURI).getJSONArray("MDB_GUI_0000000040");

                            }

                            if (JSONArrayData.getJSONObject(j).getJSONObject(newComponentURI).has("MDB_UIAP_0000000204")) {

                                hidden = Boolean.parseBoolean(JSONArrayData.getJSONObject(j).getJSONObject(newComponentURI).getString("MDB_UIAP_0000000204"));

                            }

                        }

                    }

                    if (!hidden) {

                        // it is important to clone the current JSONArray
                        JSONArray currOutputTreeDataJSON = new JSONArray(outputTreeDataJSON.getJSONObject(position - 1).getJSONArray("MDB_GUI_0000000040").toString());

                        currOutputTreeDataJSON = putComponentInTree(newComponentURI, JSONArrayData, currChildrenOfComponent, currPosition, currOutputTreeDataJSON);

                        outputTreeDataJSON.getJSONObject(position - 1).put("MDB_GUI_0000000040", currOutputTreeDataJSON);

                    }

                }

            }

        }

        return outputTreeDataJSON;

    }



    /**
     * This method reordered the entry component JSONObject
     * @param entryComponents contains the data of an entry resource
     * @return a reordered JSONObject for later calculation
     */
    public JSONObject reorderEntryComponentsValuesOld(JSONObject entryComponents) {

        Iterator entryComponentsIter = entryComponents.keys();

        String key = entryComponentsIter.next().toString();

        JSONArray entryComponentsArray = entryComponents.getJSONArray(key);

        JSONObject allComponentsInOneObject = new JSONObject();

        for (int i = 0; i < entryComponentsArray.length(); i++) {

            Iterator entryInnerComponentsIter = entryComponentsArray.getJSONObject(i).keys();

            while (entryInnerComponentsIter.hasNext()) {

                String currInnerKey = entryInnerComponentsIter.next().toString();

                Object currInnerValue = entryComponentsArray.getJSONObject(i).get(currInnerKey);

                if (allComponentsInOneObject.has(currInnerKey)) {

                    if (currInnerValue instanceof JSONArray) {

                        JSONArray currInnerValueJSONArray = (JSONArray) currInnerValue;

                        allComponentsInOneObject.append(currInnerKey, currInnerValueJSONArray.get(0));

                    }

                } else {

                    allComponentsInOneObject.put(currInnerKey, currInnerValue);

                }

            }

        }

        return entryComponents.put(key, allComponentsInOneObject);

    }


    /**
     * This method reordered the entry component JSONObject
     * @param entryComponents contains the data of an entry resource
     * @return a reordered JSONObject for later calculation
     */
    public JSONObject reorderEntryComponentsValues(JSONObject entryComponents) {

        Iterator entryComponentsIter = entryComponents.keys();

        while (entryComponentsIter.hasNext()) {

            String key = entryComponentsIter.next().toString();

            JSONArray entryComponentsArray = entryComponents.getJSONArray(key);

            JSONObject allComponentsInOneObject = new JSONObject();

            for (int i = 0; i < entryComponentsArray.length(); i++) {

                Iterator entryInnerComponentsIter = entryComponentsArray.getJSONObject(i).keys();

                while (entryInnerComponentsIter.hasNext()) {

                    String currInnerKey = entryInnerComponentsIter.next().toString();

                    Object currInnerValue = entryComponentsArray.getJSONObject(i).get(currInnerKey);

                    if (allComponentsInOneObject.has(currInnerKey)) {

                        if (currInnerValue instanceof JSONArray) {

                            JSONArray allChildComponentsInOneArray = (JSONArray) currInnerValue;

                            allComponentsInOneObject.accumulate(currInnerKey, allChildComponentsInOneArray.get(0));

                        }

                    } else {

                        allComponentsInOneObject.put(currInnerKey, currInnerValue);

                    }

                }

            }

            entryComponents.put(key, allComponentsInOneObject);

        }

        return entryComponents;

    }



    /**
     * This method sets the path of the current work directory
     * @param pathToOntologies contains the path to the ontology workspace
     */
    public void setPathToOntologies(String pathToOntologies) {
        this.pathToOntologies = pathToOntologies;
    }


    /**
     * This method checks the not processed properties.
     * @param property contains the data to check.
     * @return true if the property is a potential candidate for processing otherwise false
     */
    private boolean unknownProperty(String property) {

        switch (property) {

            case "http://purl.obolibrary.org/obo/IAO_0000115" :
                // definition
            case "http://www.geneontology.org/formats/oboInOwl#id" :
                // id
            case "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000181" :
                // has associated instance resource [input_A]
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000036" :
                // MDB entry composition of
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000088" :
                // has visible label 1 ---> redundant
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000010" :
                // execution step: save/delete triple statement(s)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000038" :
                // triggers MDB workflow action
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000040" :
                // object
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000041" :
                // property
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000042" :
                // subject
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077" :
                // load from/save to/update in named graph
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000079" :
                // execution step
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000097" :
                // then:
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000098" :
                // else:
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000095" :
                // execution step: if-then-else statement
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000104" :
                // update with resource/value
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000105" :
                // execution step: update triple statement(s)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000118" :
                // input restricted to individuals of [input_A]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000167" :
                // requirement for triggering a MDB workflow action
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000175" :
                // execution step: decision dialogue
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000184" :
                // trigger workflow action
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000192" :
                // subsequent input through GUI module
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000199" :
                // position
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000219" :
                // execution step: trigger MDB workflow action
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000230" :
                // subject (this entry's specific individual of)
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000236" :
                // wait for execution [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000261" :
                // switch to page
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000266" :
                // label status 'true'
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000267" :
                // label status 'false'
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000269" :
                // execution step: send email
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000270" :
                // email to mbox
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000271" :
                // email text 2
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000272" :
                // email text 1
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000273" :
                // email subject
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000275" :
                // execution step triggered
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000276" :
                // requires GUI input value/resource
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000277" :
                // requirement for triggering the execution step
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000281" :
                // GUI module input type
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000282" :
                // GUI module input belongs to MDB entry component
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000283" :
                // has IF input value
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000284" :
                // has IF operation
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000330" :
                // execution step: close module
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000331" :
                // close module [BOOLEAN]
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000381" :
                // 'user input' keyword defined by input
            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000434" :
                // required input if not hidden [BOOLEAN]
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" :
            case "http://www.w3.org/2000/01/rdf-schema#label" :
            case "http://www.w3.org/2000/01/rdf-schema#subClassOf" :
            case "http://www.w3.org/2002/07/owl#annotatedProperty" :
            case "http://www.w3.org/2002/07/owl#annotatedSource" :
            case "http://www.w3.org/2002/07/owl#annotatedTarget" :


                return false;

            default:

                return true;
        }


    }

}