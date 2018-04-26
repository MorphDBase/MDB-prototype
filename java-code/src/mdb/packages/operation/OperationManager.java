/*
 * Created by Roman Baum on 15.02.16.
 * Last modified by Roman Baum on 02.01.18.
 */

package mdb.packages.operation;


import mdb.basic.MDBIDChecker;
import mdb.basic.ShowEntryButton;
import mdb.mongodb.MongoDBConnection;
import mdb.packages.JenaIOTDBFactory;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.SPARQLFilter;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class OperationManager {

    private String pathToOntologies = OntologiesPath.pathToOntology;

    private String mdbCoreID = "", mdbEntryID = "", mdbUEID = "";

    private Model overlayModel = ModelFactory.createDefaultModel();

    private MongoDBConnection mongoDBConnection;

    private JSONObject keywordsFromInputForSubsequentlyWA = new JSONObject();

    /**
     * Default constructor
     */
    public OperationManager(MongoDBConnection mongoDBConnection) {

        this.mongoDBConnection = mongoDBConnection;

    }


    /**
     * A constructor which provide a specific MDBUserEntryID for further calculations
     * @param mdbUEID contains the uri of the MDBUserEntryID
     */
    public OperationManager(String mdbUEID, MongoDBConnection mongoDBConnection) {

        this.mdbUEID = mdbUEID;
        this.mongoDBConnection = mongoDBConnection;

    }


    /**
     * A constructor which provide a specific MDBCoreID, MDBEntryID and MDBUserEntryID for further calculations
     * @param mdbCoreID contains the uri of the MDBCoreID
     * @param mdbEntryID contains the uri of the MDBEntryID
     * @param mdbUEID contains the uri of the MDBUserEntryID
     */
    public OperationManager(String mdbCoreID, String mdbEntryID, String mdbUEID, MongoDBConnection mongoDBConnection) {

        this.mdbCoreID = mdbCoreID;

        this.mdbEntryID = mdbEntryID;

        this.mdbUEID = mdbUEID;

        this.mongoDBConnection = mongoDBConnection;

    }

    public JSONObject checkAutocomplete(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        String individualURI = getIndividualURIForLocalIDFromMongoDB(jsonInputObject);

        InputInterpreter inputinterpreter = new InputInterpreter(individualURI, jsonInputObject, this.mongoDBConnection);

        return inputinterpreter.checkAutocomplete(connectionToTDB);

    }

    /**
     * This method reads and coordinates the input data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject checkInput(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        String classURI, individualURI;

        if (jsonInputObject.getString("value").equals("show_localID")) {
            // case: change selected part in partonomy

            classURI = "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000003026";
            // change part button item

            individualURI = "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000003027";
            // BASIC_MDB_COMPONENT: change part button

        } else {

            classURI = getClassURIForLocalIDFromMongoDB(jsonInputObject);

            individualURI = getIndividualURIForLocalIDFromMongoDB(jsonInputObject);

            jsonInputObject = getKeywordsForLocalIDFromMongoDB(jsonInputObject);

        }

        InputInterpreter inputinterpreter = new InputInterpreter(individualURI, jsonInputObject, this.overlayModel, this.mongoDBConnection);

        JSONObject outputJSON = inputinterpreter.checkInput(classURI, connectionToTDB);

        this.overlayModel = inputinterpreter.getOverlayModel();

        return outputJSON;

    }

    /**
     * This method reads and coordinates the input data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject checkURI(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        MDBIDChecker mdbIDChecker = new MDBIDChecker();

        if (mdbIDChecker.isMDBID(jsonInputObject.getString("value"), connectionToTDB)) {

            String mdbResourcePart = jsonInputObject.getString("value");

            mdbResourcePart = mdbResourcePart.substring(mdbResourcePart.lastIndexOf("/") + 1);

            if (mdbIDChecker.isMDBEntryID()) {

                mdbResourcePart = mdbResourcePart + "#MDB_CORE_0000000412_1";
                // entry-composition named graph

                jsonInputObject.put("html_form", mdbResourcePart);

                jsonInputObject.put("localID", ShowEntryButton.localIndividualID);

                String individualURI = ShowEntryButton.individualID;

                String classURI = ShowEntryButton.classID;

                InputInterpreter inputinterpreter = new InputInterpreter(individualURI, jsonInputObject, this.overlayModel, this.mongoDBConnection);

                JSONObject outputJSON = inputinterpreter.checkInput(classURI, connectionToTDB);

                this.overlayModel = inputinterpreter.getOverlayModel();

                return outputJSON;

            }

        }

        return null;
    }



    /**
     * This method reads and coordinates the input data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject checkInputForListEntry(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        InputInterpreter inputinterpreter = new InputInterpreter(jsonInputObject, this.mongoDBConnection);

        JSONObject outputJSON = inputinterpreter.checkInputForListEntry(connectionToTDB);

        return outputJSON;

    }

    /**
     * This method reads and coordinates the input data for an overlay queue
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return 'true' if all steps are proceed, else 'false'
     */
    public boolean checkOverlayQueueInput(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        String newNS = jsonInputObject.getString("mdbentryid");

        String mongoDBKey = jsonInputObject.getString("html_form");

        String session = jsonInputObject.getString("connectSID");

        System.out.println("jsonFromMongoDBkeyjsonnewNS = " + newNS);

        System.out.println("jsonFromMongoDBkeyjsonmongoDBkey = " + mongoDBKey);

        if (this.mongoDBConnection.documentExist("mdb-prototyp", session, mongoDBKey)) {

            JSONArray overlayJSON = this.mongoDBConnection.getJSONArrayForKey("mdb-prototyp", session, mongoDBKey);

            for (int i = 0; i < overlayJSON.length(); i++) {

                String oldIndividualID = overlayJSON.getJSONObject(i).getString("individualID");

                if (oldIndividualID.contains("dummy-overlay")) {

                    String newIndividualID = newNS + "#" + ResourceFactory.createResource(oldIndividualID).getLocalName();

                    overlayJSON.getJSONObject(i).put("individualID", newIndividualID);

                }

            }

            this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", session, mongoDBKey, overlayJSON);

            JSONObject jsonFromMongoDB = this.mongoDBConnection.pullJSONObjectFromMongoDB(jsonInputObject);

            Iterator<String> keys = jsonFromMongoDB.keys();

            JSONArray jsonInputQueue = new JSONArray();

            while (keys.hasNext()) {

                String key = keys.next();

                if (StringUtils.isNumeric(key)) {

                    if (jsonInputObject.has("subsequently_workflow_action")) {

                        if (jsonInputObject.getString("subsequently_workflow_action").equals("true")) {

                            JSONObject currInputJSON = jsonFromMongoDB.getJSONArray(key).getJSONObject(0);

                            if (jsonInputObject.has("subsequently_root")) {

                                currInputJSON.put("subsequently_root", jsonInputObject.getString("subsequently_root"));

                            }

                            if (jsonInputObject.has("keywords_to_transfer")) {

                                currInputJSON.put("keywords_to_transfer", jsonInputObject.getJSONObject("keywords_to_transfer"));

                            }

                            jsonInputQueue.put(Integer.parseInt(key), currInputJSON);

                        }

                    } else {

                        jsonInputQueue.put(Integer.parseInt(key), jsonFromMongoDB.getJSONArray(key).getJSONObject(0));

                    }

                    System.out.println("jsonFromMongoDBkey = " + key);

                }

            }

            for (int i = 0; i < jsonInputQueue.length(); i++) {

                OperationManager queueOperationManager = new OperationManager(this.mongoDBConnection);

                // calculate the input from the mongoDB
                JSONObject outputJSON = queueOperationManager.checkInput(jsonInputQueue.getJSONObject(i), connectionToTDB);

                if (outputJSON.has("use_in_known_subsequent_WA")) {

                    JSONObject currKeywordFromInputForSubsequentlyWA = outputJSON.getJSONObject("use_in_known_subsequent_WA");

                    Iterator<String> currKeywordFromInputForSubsequentlyWAIter = currKeywordFromInputForSubsequentlyWA.keys();

                    while (currKeywordFromInputForSubsequentlyWAIter.hasNext()) {

                        String currKey = currKeywordFromInputForSubsequentlyWAIter.next();

                        this.keywordsFromInputForSubsequentlyWA.put(currKey, currKeywordFromInputForSubsequentlyWA.getString(currKey));

                    }

                }

            }

            return true;

        }

        return false;

    }

    /**
     * This method coordinates the input data for a subsequently workflow
     * @param jsonInputObject contains the information for the calculation
     * @param classURI contains the URI of an ontology class
     * @param keywordsToTransferJSON contains keywords from a preceding transition
     * @param connectionToTDB contains a JenaIOTDBFactory object
     */
    public void checkSubsequentlyWorkflow(JSONObject jsonInputObject, String classURI,
                                          JSONObject keywordsToTransferJSON, JenaIOTDBFactory connectionToTDB) {

        String individualURI = getIndividualURIForClassURIFromJena(classURI, connectionToTDB);

        jsonInputObject = updateJSONInputObject(individualURI, keywordsToTransferJSON, jsonInputObject);

        InputInterpreter inputinterpreter = new InputInterpreter(individualURI, jsonInputObject, this.overlayModel, this.mongoDBConnection);

        inputinterpreter.checkInput(classURI, connectionToTDB);

        this.overlayModel = inputinterpreter.getOverlayModel();

    }

    /**
     * This method modifies the JSON input object.
     * @param individualURI contains the URI of an ontology individual
     * @param keywordsToTransferJSON contains keywords from a preceding transition
     * @param jsonInputObject contains the information for the calculation
     * @return
     */
    private JSONObject updateJSONInputObject(String individualURI, JSONObject keywordsToTransferJSON,
                                             JSONObject jsonInputObject) {

        jsonInputObject.put("localID", ResourceFactory.createResource(individualURI).getLocalName());

        if (this.keywordsFromInputForSubsequentlyWA.keys().hasNext()) {

            Iterator<String> keywordsFromInputForSubsequentlyWAIter = this.keywordsFromInputForSubsequentlyWA.keys();

            while (keywordsFromInputForSubsequentlyWAIter.hasNext()) {

                String currKey = keywordsFromInputForSubsequentlyWAIter.next();

                if (keywordsToTransferJSON.has(currKey)) {

                    keywordsToTransferJSON.put(currKey, this.keywordsFromInputForSubsequentlyWA.getString(currKey));

                }

                this.keywordsFromInputForSubsequentlyWA.remove(currKey);

            }

        }

        jsonInputObject.put("precedingKeywords", keywordsToTransferJSON);

        jsonInputObject.put("localIDs", new JSONArray());

        return jsonInputObject;

    }


    /**
     * This method find the corresponding individual URI for a class URI.
     * @param classURI contains the URI of an ontology class
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an URI of an individual
     */
    private String getIndividualURIForClassURIFromJena(String classURI, JenaIOTDBFactory connectionToTDB) {

        SelectBuilder selectBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO.addWhere("?s", RDF.type, "<" + classURI + ">");

        selectBuilder.addVar(selectBuilder.makeVar("?s"));

        selectBuilder.addGraph("?g", tripleSPO);

        String sparqlQueryString = selectBuilder.buildString();

        return connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?s");


    }


    /**
     * This method find the corresponding local identifier for an individual
     * @param jsonInputObject contains the information for the calculation
     * @return a modified JSONObject for further calculation
     */
    public JSONObject getKeywordsForLocalIDFromMongoDB (JSONObject jsonInputObject) {

        JSONArray jsonFromMongoDB = this.mongoDBConnection.pullListFromMongoDB(jsonInputObject);

        if (jsonInputObject.has("localIDs")) {

            JSONArray localIDs = jsonInputObject.getJSONArray("localIDs");

            if (localIDs != null) {

                for (int i = 0; i < localIDs.length(); i++) {

                    for (int j = 0; j < jsonFromMongoDB.length(); j++) {

                        if ((localIDs.getJSONObject(i).getString("localID")).equals(jsonFromMongoDB.getJSONObject(j).getString("localID")) &&
                                (jsonFromMongoDB.getJSONObject(j).has("keyword"))) {

                            // add the corresponding keyword information to the input
                            jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("keyword", jsonFromMongoDB.getJSONObject(j).getString("keyword"));

                        }

                        if ((localIDs.getJSONObject(i).getString("localID")).equals(jsonFromMongoDB.getJSONObject(j).getString("localID")) &&
                                (jsonFromMongoDB.getJSONObject(j).has("keywordLabel"))) {

                            // add the corresponding keyword label information to the input
                            jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("keywordLabel", jsonFromMongoDB.getJSONObject(j).getString("keywordLabel"));

                            if (jsonInputObject.getJSONArray("localIDs").getJSONObject(i).get("value") instanceof JSONObject) {

                                JSONObject valueObject = jsonInputObject.getJSONArray("localIDs").getJSONObject(i).getJSONObject("value");

                                jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("valueLabel", valueObject.getString("label"));

                                jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("value", valueObject.getString("resource"));

                            }

                        }

                        if ((localIDs.getJSONObject(i).getString("localID")).equals(jsonFromMongoDB.getJSONObject(j).getString("localID")) &&
                                (jsonFromMongoDB.getJSONObject(j).has("keywordDefinition"))) {

                            // add the corresponding keyword label information to the input
                            jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("keywordDefinition", jsonFromMongoDB.getJSONObject(j).getString("keywordDefinition"));

                            if (jsonInputObject.getJSONArray("localIDs").getJSONObject(i).get("value") instanceof JSONObject) {

                                JSONObject valueObject = jsonInputObject.getJSONArray("localIDs").getJSONObject(i).getJSONObject("value");

                                jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("valueDefinition", valueObject.getString("definition"));

                                jsonInputObject.getJSONArray("localIDs").getJSONObject(i).put("value", valueObject.getString("resource"));

                            }

                        }

                    }

                }

            }

        }

        return jsonInputObject;

    }


    /**
     * This method gets the path of current the work directory
     * @return the path to the current ontology workspace
     */
    public String getPathToOntologies() {
        return this.pathToOntologies;
    }



    /**
     * This method is a getter for the overlay named graph.
     * @return a jena model for a MDB overlay
     */
    public Model getOverlayModel() {

        return this.overlayModel;

    }

    /**
     * This method reads and coordinates the output data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject getOutput(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        String resourceURI = getURIForLocalID(jsonInputObject.getString("localID"), connectionToTDB);


        OutputGenerator outputGenerator;

        if (!this.mdbCoreID.isEmpty() && !this.mdbEntryID.isEmpty() && !this.mdbUEID.isEmpty()) {

            outputGenerator = new OutputGenerator(this.mdbCoreID, this.mdbEntryID, this.mdbUEID, this.mongoDBConnection);

        } else if(!this.mdbCoreID.isEmpty()) {

            outputGenerator = new OutputGenerator(this.mdbUEID, this.mongoDBConnection);

        } else {

            outputGenerator = new OutputGenerator(this.mongoDBConnection);

        }

        JSONObject jsonOutputObject = outputGenerator.getOutputJSONObjectOld(jsonInputObject, new JSONObject(), resourceURI, connectionToTDB);

        jsonOutputObject.put("load_page", resourceURI);

        try {

            URL url = new URL(resourceURI);

            String loadPageLocalID = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

            jsonOutputObject.put("load_page_localID", loadPageLocalID);

        } catch (MalformedURLException e) {

            e.printStackTrace();

        }

        return jsonOutputObject;

    }

    /**
     * This method reads and coordinates the output data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param outputObject an already existing output object
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject getOutput(JSONObject jsonInputObject, JSONObject outputObject, JenaIOTDBFactory connectionToTDB) {

        String resourceURI = getURIForLocalID(jsonInputObject.getString("localID"), connectionToTDB);

        OutputGenerator outputGenerator;

        if (!this.mdbCoreID.isEmpty() && !this.mdbEntryID.isEmpty() && !this.mdbUEID.isEmpty()) {

            outputGenerator = new OutputGenerator(this.mdbCoreID, this.mdbEntryID, this.mdbUEID, this.mongoDBConnection);

        } else if(!this.mdbCoreID.isEmpty()) {

            outputGenerator = new OutputGenerator(this.mdbUEID, this.mongoDBConnection);

        } else {

            outputGenerator = new OutputGenerator(this.mongoDBConnection);

        }

        return outputGenerator.getOutputJSONObjectOld(jsonInputObject, outputObject, resourceURI, connectionToTDB);

    }


    /**
     * This method reads and coordinates the output data for a panel
     * @param jsonInputObject contains the information for the calculation
     * @param outputObject an already existing output object
     * @param resourceURI contains the root resource for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an output JSONObject with data
     */
    public JSONObject getOutput(JSONObject jsonInputObject, JSONObject outputObject, String resourceURI, JenaIOTDBFactory connectionToTDB) {

        OutputGenerator outputGenerator;

        if (!this.mdbCoreID.isEmpty() && !this.mdbEntryID.isEmpty() && !this.mdbUEID.isEmpty()) {

            outputGenerator = new OutputGenerator(this.mdbCoreID, this.mdbEntryID, this.mdbUEID, this.mongoDBConnection);

        } else if(!this.mdbUEID.isEmpty()) {

            outputGenerator = new OutputGenerator(this.mdbUEID, this.mongoDBConnection);

        } else {

            outputGenerator = new OutputGenerator(this.mongoDBConnection);

        }

        outputGenerator.setPathToOntologies(this.pathToOntologies);

        return outputGenerator.getOutputJSONObjectOld(jsonInputObject, outputObject, resourceURI, connectionToTDB);

    }


    /**
     * This method find an URI of a resource with his local identifier
     * @param localID the local identifier of the URI
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an uri of a resource
     */
    public String getURIForLocalID (String localID, JenaIOTDBFactory connectionToTDB) {

        FilterBuilder filterBuilder = new FilterBuilder();

        SelectBuilder selectBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO.addWhere("?s", "?p", "?o");

        selectBuilder.addVar(selectBuilder.makeVar("?s"));

        selectBuilder.addGraph("?g", tripleSPO);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, "?p", "<http://www.geneontology.org/formats/oboInOwl#id>");

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

        filterItems.clear();

        ArrayList<String> oneDimensionalFilterItems = new ArrayList<>();

        oneDimensionalFilterItems.add(localID);

        filter = sparqlFilter.getRegexSTRFilter("?o", oneDimensionalFilterItems);

        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

        String sparqlQueryString = selectBuilder.buildString();

        return connectionToTDB.pullSingleDataFromTDB(pathToOntologies, sparqlQueryString, "?s");


    }


    /**
     * This method find the class URI of a resource with the local identifier of an input field
     * @param jsonInputObject contains the information for the calculation
     * @return a class URI of a resource
     */
    public String getClassURIForLocalIDFromMongoDB(JSONObject jsonInputObject) {

        JSONObject jsonFromMongoDB = this.mongoDBConnection.pullDataFromMongoDBWithLocalID(jsonInputObject);

        return jsonFromMongoDB.getString("classID");

    }


    /**
     * This method find the individual URI of a resource with the local identifier of an input field
     * @param jsonInputObject contains the information for the calculation
     * @return a individual URI of a resource
     */
    public String getIndividualURIForLocalIDFromMongoDB(JSONObject jsonInputObject) {

        JSONObject jsonFromMongoDB = this.mongoDBConnection.pullDataFromMongoDBWithLocalID(jsonInputObject);

        return jsonFromMongoDB.getString("individualID");

    }


    /**
     * This method sets the path of the current work directory
     * @param pathToOntologies contains the path to the ontology workspace
     */
    public void setPathToOntologies(String pathToOntologies) {
        this.pathToOntologies = pathToOntologies;
    }



}
