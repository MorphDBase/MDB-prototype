/*
 * Created by Roman Baum on 24.03.15.
 * Last modified by Roman Baum on 19.09.17.
 */

import mdb.mongodb.MongoDBConnection;
import mdb.packages.JSONInputInterpreter;
import mdb.packages.JenaIOTDBFactory;
import mdb.packages.MDBOverlayHandler;
import mdb.packages.operation.OperationManager;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.base.block.FileMode;
import org.apache.jena.tdb.sys.SystemTDB;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The class "TestSocket" provides one default constructor and three methods. The method "connect" create a new session
 * and is the connection between Java and Javascript. The method "close" close the connection between Java and
 * Javascript. The method "onMessage" processed the javascript input and create a new Jena model. This model will be
 * converted to a string and finally sent back to Javascript.
 */


@ServerEndpoint("/echo")
public class TestSocket {

    // default - Constructor
    public TestSocket() {

    }

    private static Set<Object> clients = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void connect(Session session) {
        /**
         *      create a new session and is the connection between Java and Javascript.
         */

        // set timeout to 180000ms
        session.getContainer().setAsyncSendTimeout(180000);

        // add this session to the connected sessions set
        clients.add(session);

        System.out.println("session = " + session);

    }

    @OnClose
    public void close(Session session) {

        /**
         *      close the connection between Java and Javascript.
         */

        // remove this session from the connected sessions set
        clients.remove(session);
    }

    @OnMessage
    public void onMessage(String JSONQuery, Session session) {

        /**
         *      process the javascript input and sent the output string back to Javascript.
         */

        // reduce the size of the TDB
        TDB.getContext().set(SystemTDB.symFileMode, FileMode.direct);

        // create a new JSON object with the input
        JSONObject jsonInputObject = new JSONObject(JSONQuery);

        System.out.println();
        System.out.println("jsonInputObject = " + jsonInputObject);
        System.out.println();

        // get the SPARQL-Query from the JSON object
        String inputTypeString = jsonInputObject.getString("type");

        OperationManager operationManager = new OperationManager();

        // create new connectionToTDB
        JenaIOTDBFactory connectionToTDB = new JenaIOTDBFactory();

        MDBOverlayHandler mdbOverlayHandler = new MDBOverlayHandler();

        switch (inputTypeString) {

            case "query":

                // get the SPARQL-Query from the JSON object
                String sparqlQueryString = jsonInputObject.getString("query");

                // get the outputFormat from the JSON object
                String outputFormat = jsonInputObject.getString("format");

                // position of the TDB
                String tripleStoreDirectory  = jsonInputObject.getString("dataset");

                // calculate the start date
                long executionStart = System.currentTimeMillis();

                // create a Query
                Query sparqlQuery = QueryFactory.create(sparqlQueryString);

                // get result string from the data set
                String resultString = connectionToTDB.pullStringDataFromTDB(tripleStoreDirectory, sparqlQuery,
                                                                    outputFormat);

                // calculate the query time
                long queryTime = System.currentTimeMillis() - executionStart;

                // create a new JSON object
                JSONObject jsonOutputObject = new JSONObject();

                // add a pair (key, value) to the JSON object
                jsonOutputObject.put("output_message", resultString);

                // add a pair (key, value) to the JSON object
                jsonOutputObject.put("query_time", queryTime);

                // convert the JSON object to a string
                String jsonOutputString = jsonOutputObject.toString();

                // send output to javascript
                session.getAsyncRemote().sendText(jsonOutputString);

                break;

            case "push_triples":

                String mdbTestStatusTransitionString = jsonInputObject.getString("mdb_status_transition");

                switch (mdbTestStatusTransitionString) {

                    case "test_status_transition":

                        JSONObject inputDataObject = jsonInputObject.getJSONObject("input_data");

                        JSONInputInterpreter jsonInputInterpreter = new JSONInputInterpreter();

                        // calculate the start date
                        executionStart = System.currentTimeMillis();

                        // get an array list with a core id and an output message
                        ArrayList<String> outputArrayList = jsonInputInterpreter.interpretObject(inputDataObject, connectionToTDB);

                        // calculate the query time
                        queryTime = System.currentTimeMillis() - executionStart;

                        System.out.println("query time= " + queryTime);

                        // create a new JSON object
                        jsonOutputObject = new JSONObject();

                        // add a pair (key, value) to the JSON object
                        jsonOutputObject.put("output_message", outputArrayList.get(0));

                        // add a pair (key, value) to the JSON object
                        jsonOutputObject.put("query_time", queryTime);

                        // convert the JSON object to a string
                        jsonOutputString = jsonOutputObject.toString();

                        // send output to javascript
                        session.getAsyncRemote().sendText(jsonOutputString);

                        break;

                }

                break;

            case "check_autocomplete" :

                // calculate the start date
                executionStart = System.currentTimeMillis();

                jsonOutputObject = operationManager.checkAutocomplete(jsonInputObject, connectionToTDB);

                // calculate the query time
                queryTime = System.currentTimeMillis() - executionStart;

                System.out.println("query time= " + queryTime);

                // create a new JSON object

                // fill output data
                jsonOutputObject.put("html_form", jsonInputObject.getString("html_form"));

                if (jsonInputObject.has("mdbueid")) {

                    jsonOutputObject.put("mdbueid", jsonInputObject.getString("mdbueid"));

                }

                if (jsonInputObject.has("mdbueid_uri")) {

                    jsonOutputObject.put("mdbueid_uri", jsonInputObject.getString("mdbueid_uri"));

                }

                if (jsonInputObject.has("partID")) {

                    jsonOutputObject.put("partID", jsonInputObject.getString("partID"));

                }

                if (jsonInputObject.has("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411")) {
                    // KEYWORD: known resource A

                    jsonOutputObject.put("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411", jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411"));

                }

                if (jsonInputObject.has("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412")) {
                    // KEYWORD: known resource B

                    jsonOutputObject.put("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412", jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412"));

                }

                jsonOutputObject.put("connectSID", jsonInputObject.getString("connectSID"));

                System.out.println("jsonOutputObject: " + jsonOutputObject);

                // convert the JSON object to a string
                jsonOutputString = jsonOutputObject.toString();

                // send output to javascript
                session.getAsyncRemote().sendText(jsonOutputString);

                break;

            case "check_input" :

                // calculate the start date
                executionStart = System.currentTimeMillis();

                JSONObject originalJSONInputObject = new JSONObject(jsonInputObject.toString());

                jsonOutputObject = operationManager.checkInput(jsonInputObject, connectionToTDB);

                // calculate the query time
                queryTime = System.currentTimeMillis() - executionStart;

                System.out.println("query time= " + queryTime);

                // create a new JSON object

                // fill output data
                jsonOutputObject.put("html_form", jsonInputObject.getString("html_form"));

                if (jsonInputObject.has("mdbueid")) {

                    jsonOutputObject.put("mdbueid", jsonInputObject.getString("mdbueid"));

                }

                if (jsonInputObject.has("mdbueid_uri")) {

                    jsonOutputObject.put("mdbueid_uri", jsonInputObject.getString("mdbueid_uri"));

                }

                if (jsonInputObject.has("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411")) {
                    // KEYWORD: known resource A

                    jsonOutputObject.put("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411", jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411"));

                }

                if (jsonInputObject.has("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412")) {
                    // KEYWORD: known resource B

                    jsonOutputObject.put("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412", jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412"));

                }

                if (jsonInputObject.has("partID")) {

                    jsonOutputObject.put("partID", jsonInputObject.getString("partID"));

                }

                jsonOutputObject.put("connectSID", jsonInputObject.getString("connectSID"));

                System.out.println("jsonOutputObject: " + jsonOutputObject);

                if (jsonOutputObject.getString("html_form").equals("Ontologies/GUIComponent#GUI_COMPONENT_0000000175")) {

                    initializeMongoDB(jsonOutputObject);

                }

                boolean createNewEntry = false;

                if (jsonOutputObject.has("create_new_entry")) {

                    if (jsonOutputObject.getString("create_new_entry").equals("true")) {

                        createNewEntry = true;

                    }

                    jsonOutputObject.remove("create_new_entry");

                }

                boolean subsequentlyWorkflowAction = false;

                String subsequentlyRoot = "";

                JSONObject keywordsToTransferJSON = new JSONObject();

                if (jsonOutputObject.has("subsequently_workflow_action")) {

                    if (jsonOutputObject.getString("subsequently_workflow_action").equals("true")) {

                        subsequentlyWorkflowAction = true;

                        if (jsonOutputObject.has("subsequently_root")) {

                            subsequentlyRoot = jsonOutputObject.getString("subsequently_root");

                            jsonInputObject.put("subsequently_root", jsonOutputObject.getString("subsequently_root"));

                            jsonOutputObject.remove("subsequently_root");

                        }

                        if (jsonOutputObject.has("keywords_to_transfer")) {

                            keywordsToTransferJSON = jsonOutputObject.getJSONObject("keywords_to_transfer");

                            jsonInputObject.put("keywords_to_transfer", jsonOutputObject.getJSONObject("keywords_to_transfer"));

                            jsonOutputObject.remove("keywords_to_transfer");

                        }

                    }

                    jsonInputObject.put("subsequently_workflow_action", jsonOutputObject.getString("subsequently_workflow_action"));

                    jsonOutputObject.remove("subsequently_workflow_action");

                }

                // convert the JSON object to a string
                jsonOutputString = jsonOutputObject.toString();

                // send output to javascript
                session.getAsyncRemote().sendText(jsonOutputString);

                if (jsonOutputObject.has("load_overlay")) {

                    if (jsonOutputObject.getString("load_overlay").contains("http://www.morphdbase.de/resource/dummy-overlay")) {

                        //System.out.println("overlayModel = " + overlayModel);

                        Model overlayModel = operationManager.getOverlayModel();

                        String overlayNGURI = jsonOutputObject.getString("load_overlay");

                        mdbOverlayHandler.create(overlayNGURI, overlayModel, connectionToTDB);

                    }

                }

                if (createNewEntry) {

                    boolean queueIsProcessed = operationManager.checkOverlayQueueInput(jsonInputObject, connectionToTDB);

                    if (queueIsProcessed) {

                        mdbOverlayHandler.removeOverlay(jsonInputObject, connectionToTDB);

                    }

                } else if (jsonOutputObject.has("html_form")) {

                    if (jsonOutputObject.getString("html_form").contains("resource/dummy-overlay")) {

                        mdbOverlayHandler.updateTimeStamp(jsonInputObject, connectionToTDB);

                    }

                }

                if (subsequentlyWorkflowAction) {

                    operationManager.checkSubsequentlyWorkflow(originalJSONInputObject, subsequentlyRoot, keywordsToTransferJSON, connectionToTDB);

                }

                break;

            case "generate_doc" :

                // calculate the start date
                executionStart = System.currentTimeMillis();

                jsonOutputObject = operationManager.getOutput(jsonInputObject, connectionToTDB);

                // calculate the query time
                queryTime = System.currentTimeMillis() - executionStart;

                System.out.println("query time= " + queryTime);

                // fill output data
                jsonOutputObject.put("connectSID", jsonInputObject.getString("connectSID"));

                System.out.println("jsonOutputObject: " + jsonOutputObject);

                // convert the JSON object to a string
                jsonOutputString = jsonOutputObject.toString();

                // send output to javascript
                session.getAsyncRemote().sendText(jsonOutputString);

                break;

            case "overlay" :

                // calculate the start date
                executionStart = System.currentTimeMillis();

                mdbOverlayHandler.removeDeprecatedOverlays(jsonInputObject, connectionToTDB);

                // calculate the query time
                queryTime = System.currentTimeMillis() - executionStart;

                System.out.println("query time= " + queryTime);

                break;

        }




    }

    private void initializeMongoDB (JSONObject jsonOutputObject) {

        String mongoDBKey = "MY_DUMMY_ADMIN_0000000001";

        JSONObject morphologicalDescriptionJSON = new JSONObject();

        morphologicalDescriptionJSON.put("classID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000002329");
        morphologicalDescriptionJSON.put("individualID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000002330");
        morphologicalDescriptionJSON.put("localID", "MDB_DATASCHEME_0000002330");

        JSONArray identifiedResources = new JSONArray();

        identifiedResources.put(morphologicalDescriptionJSON);

        generateDummiesForPrototyp(mongoDBKey,  identifiedResources, jsonOutputObject);

        mongoDBKey = "MY_DUMMY_DESCRIPTION_0000000001";

        morphologicalDescriptionJSON = new JSONObject();

        morphologicalDescriptionJSON.put("classID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000002103");
        morphologicalDescriptionJSON.put("individualID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000002104");
        morphologicalDescriptionJSON.put("localID", "MDB_DATASCHEME_0000002104");

        identifiedResources = new JSONArray();

        identifiedResources.put(morphologicalDescriptionJSON);

        generateDummiesForPrototyp(mongoDBKey,  identifiedResources, jsonOutputObject);

        mongoDBKey = "MY_DUMMY_LOGOUT_0000000001";

        morphologicalDescriptionJSON = new JSONObject();

        morphologicalDescriptionJSON.put("classID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000943");
        morphologicalDescriptionJSON.put("individualID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000947");
        morphologicalDescriptionJSON.put("localID", "MDB_DATASCHEME_0000000947");

        identifiedResources = new JSONArray();

        identifiedResources.put(morphologicalDescriptionJSON);

        generateDummiesForPrototyp(mongoDBKey,  identifiedResources, jsonOutputObject);

        mongoDBKey = "MY_DUMMY_MDB_0000000001";

        morphologicalDescriptionJSON = new JSONObject();

        morphologicalDescriptionJSON.put("classID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000948");
        morphologicalDescriptionJSON.put("individualID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000949");
        morphologicalDescriptionJSON.put("localID", "MDB_DATASCHEME_0000000949");

        identifiedResources = new JSONArray();

        identifiedResources.put(morphologicalDescriptionJSON);

        generateDummiesForPrototyp(mongoDBKey,  identifiedResources, jsonOutputObject);

        mongoDBKey = "MY_DUMMY_SPECIMEN_0000000001";

        morphologicalDescriptionJSON = new JSONObject();

        morphologicalDescriptionJSON.put("classID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000694");
        morphologicalDescriptionJSON.put("individualID", "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000695");
        morphologicalDescriptionJSON.put("localID", "MDB_DATASCHEME_0000000695");

        identifiedResources = new JSONArray();

        identifiedResources.put(morphologicalDescriptionJSON);

        generateDummiesForPrototyp(mongoDBKey,  identifiedResources, jsonOutputObject);

    }

    private void generateDummiesForPrototyp (String mongoDBKey, JSONArray identifiedResources, JSONObject jsonOutputObject) {

        MongoDBConnection mongoDBConnection = new MongoDBConnection("localhost", 27017);

        if (mongoDBConnection.collectionExist("mdb-prototyp", "sessions")) {

            System.out.println("Collection already exist");

            if (!mongoDBConnection.documentExist("mdb-prototyp", "sessions", "session", jsonOutputObject.getString("connectSID"))) {

                mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", "session", jsonOutputObject.getString("connectSID"));

                mongoDBConnection.createCollection("mdb-prototyp", jsonOutputObject.getString("connectSID"));

                mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonOutputObject.getString("connectSID"), mongoDBKey, identifiedResources);

            } else {

                if (mongoDBConnection.documentExistNew("mdb-prototyp", jsonOutputObject.getString("connectSID"), mongoDBKey)) {

                    System.out.println("There exist a document for this key!");

                    if (!mongoDBConnection.documentWithDataExist("mdb-prototyp", jsonOutputObject.getString("connectSID"), mongoDBKey, identifiedResources)) {

                        mongoDBConnection.putDataToMongoDB("mdb-prototyp", jsonOutputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                    } else {

                        System.out.println("The document already exist in the collection");
                    }


                } else {

                    mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonOutputObject.getString("connectSID"), mongoDBKey, identifiedResources);

                }

            }

        } else {

            mongoDBConnection.createCollection("mdb-prototyp", "sessions");

            mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", "session", jsonOutputObject.getString("connectSID"));

            mongoDBConnection.createCollection("mdb-prototyp", jsonOutputObject.getString("connectSID"));

            mongoDBConnection.insertDataToMongoDB("mdb-prototyp", jsonOutputObject.getString("connectSID"), mongoDBKey, identifiedResources);

        }

    }

}