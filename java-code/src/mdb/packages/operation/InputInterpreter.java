/*
 * Created by Roman Baum on 19.02.16.
 * Last modified by Roman Baum on 21.08.17.
 */
package mdb.packages.operation;


import mdb.basic.MDBURLEncoder;
import mdb.basic.StringChecker;
import mdb.basic.TDBPath;
import mdb.mongodb.MongoDBConnection;
import mdb.packages.JenaIOTDBFactory;
import mdb.packages.KBOrder;
import mdb.packages.MDBIDFinder;
import mdb.packages.MDBJSONObjectFactory;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.QueryBuilderConverter;
import mdb.packages.querybuilder.SPARQLFilter;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class InputInterpreter {

    private String pathToOntologies = OntologiesPath.pathToOntology;

    private boolean currInputIsValid = true;

    private boolean inputIsValid;

    private JSONObject currComponentObject = new JSONObject();

    private String individualURI;

    private JSONObject jsonInputObject;

    private Model overlayModel = ModelFactory.createDefaultModel();

    public InputInterpreter(String individualURI, JSONObject jsonInputObject) {

        this.individualURI = individualURI;

        this.jsonInputObject = jsonInputObject;

    }

    public InputInterpreter(String individualURI, JSONObject jsonInputObject, Model overlayModel) {

        this.individualURI = individualURI;

        this.jsonInputObject = jsonInputObject;

        this.overlayModel = overlayModel;

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

        String sparqlQueryString = constructBuilder.buildString();

        Model constructResult = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

        constructResult = checkInputTypeInModel(constructResult, entryComponents, connectionToTDB);

        Selector selector = new SimpleSelector(null, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000490"), null, "");
        // triggers 'click' for MDB entry component

        StmtIterator typeStmts = constructResult.listStatements(selector);

        ArrayList<Statement> stmtList = new ArrayList<>();

        while (typeStmts.hasNext() && this.currInputIsValid) {

            Statement currStatement = typeStmts.next();

            entryComponents = manageProperty(resourceSubject, currStatement, entryComponents, connectionToTDB);

            stmtList.add(currStatement);

        }

        constructResult.remove(stmtList);

        StmtIterator resultIterator = constructResult.listStatements();

        while (resultIterator.hasNext() && this.currInputIsValid) {

            Statement currStatement = resultIterator.next();

            entryComponents = manageProperty(resourceSubject, currStatement, entryComponents, connectionToTDB);

        }

        return entryComponents;

    }

    /**
     * This method extract(s) statement(s) from the model.
     * @param constructResult contains the gui input type in one statement
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the input model without the extracted statement(s)
     */
    private Model checkInputTypeInModel(Model constructResult, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

        Property hasGUIInputType = ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000019");
        // has GUI input type

        while (constructResult.contains(null, hasGUIInputType) && !this.inputIsValid) {
            // check all gui input types - multiple statements are possible

            Statement hasGUIInputTypeStmt = constructResult.getProperty(null, hasGUIInputType);

            //System.out.println("hasGUIInputTypeStmt = " + hasGUIInputTypeStmt);

            checkInputTypeInStmt(hasGUIInputTypeStmt, entryComponents, connectionToTDB);

            constructResult.remove(hasGUIInputTypeStmt);

        }

        return constructResult;

    }

    /**
     * This method check if the input has the correct mdb data type.
     * @param stmtToCheck contains the gui input type in the object
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    private JSONObject checkInputTypeInStmt(Statement stmtToCheck, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

        String typeToCheck = stmtToCheck.getObject().toString();

        //System.out.println("typeToCheck = " + typeToCheck);

        UrlValidator annotationValidator = new UrlValidator();

        // get a MDB url Encoder to encode the uri with utf-8
        MDBURLEncoder mdburlEncoder = new MDBURLEncoder();

        StringChecker stringChecker = new StringChecker();

        switch (typeToCheck) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000070" :
                // GUI_COMPONENT_INPUT_TYPE: ontology class

                if (annotationValidator
                        .isValid(mdburlEncoder.encodeUrl(this.jsonInputObject.getString("value"), "UTF-8"))) {

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    SelectBuilder selectWhereBuilder = new SelectBuilder();

                    selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                    selectWhereBuilder.addWhere("<" + this.jsonInputObject.getString("value") + ">", "rdf:type", "<http://www.w3.org/2002/07/owl#Class>");

                    AskBuilder askBuilder = new AskBuilder();

                    askBuilder = prefixesBuilder.addPrefixes(askBuilder);

                    askBuilder.addGraph("?g", selectWhereBuilder);

                    // create a Query
                    String sparqlQueryString = askBuilder.buildString();

                    boolean validInput = connectionToTDB.statementExistInTDB(OntologiesPath.pathToOntology, sparqlQueryString);

                    if (validInput) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000071" :
                // GUI_COMPONENT_INPUT_TYPE: ontology instance (individual)

                if (annotationValidator
                        .isValid(mdburlEncoder.encodeUrl(this.jsonInputObject.getString("value"), "UTF-8"))) {

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    SelectBuilder selectWhereBuilder = new SelectBuilder();

                    selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                    selectWhereBuilder.addWhere("<" + this.jsonInputObject.getString("value") + ">", "rdf:type", "<http://www.w3.org/2002/07/owl#NamedIndividual>");

                    AskBuilder askBuilder = new AskBuilder();

                    askBuilder = prefixesBuilder.addPrefixes(askBuilder);

                    askBuilder.addGraph("?g", selectWhereBuilder);

                    // create a Query
                    String sparqlQueryString = askBuilder.buildString();

                    boolean validInput = connectionToTDB.statementExistInTDB(OntologiesPath.pathToOntology, sparqlQueryString);

                    if (validInput) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000072" :
                // GUI_COMPONENT_INPUT_TYPE: literal
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000354" :
                // GUI_COMPONENT_INPUT_TYPE: MDB password

                if (((this.jsonInputObject.getString("value")).getClass().equals("".getClass()))) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000079" :
                // GUI_COMPONENT_INPUT_TYPE: positive integer

                if (stringChecker.checkIfStringIsAnInteger(this.jsonInputObject.getString("value"))) {

                    if (Integer.parseInt(this.jsonInputObject.getString("value")) > 0) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000080" :
                // GUI_COMPONENT_INPUT_TYPE: email address

                if (EmailValidator.getInstance().isValid(this.jsonInputObject.getString("value"))) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000081" :
                // GUI_COMPONENT_INPUT_TYPE: web address

                MDBURLEncoder mdburlEncoderSomeValue = new MDBURLEncoder();

                UrlValidator urlValidatorSomeValue = new UrlValidator();

                if (urlValidatorSomeValue.isValid(mdburlEncoderSomeValue.encodeUrl(this.jsonInputObject.getString("value"), "UTF-8"))) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000112" :
                // GUI_COMPONENT_INPUT_TYPE: MDB user entry ID

                if (annotationValidator
                        .isValid(mdburlEncoder.encodeUrl(this.jsonInputObject.getString("value"), "UTF-8"))) {

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    SelectBuilder selectWhereBuilder = new SelectBuilder();

                    selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                    selectWhereBuilder
                            .addWhere(
                                    "<" + this.jsonInputObject.getString("value") + ">",
                                    "rdf:type",
                                    "<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575>"
                            );
                    //MDB user entry ID

                    AskBuilder askBuilder = new AskBuilder();

                    askBuilder = prefixesBuilder.addPrefixes(askBuilder);

                    askBuilder
                            .addGraph(
                                    "<" + this.jsonInputObject.getString("value") + "#MDB_CORE_0000000587_1>",
                                    selectWhereBuilder
                            );
                    // MDB user entry ID individuals named graph

                    // create a Query
                    String sparqlQueryString = askBuilder.buildString();

                    TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                    boolean validInput = connectionToTDB.statementExistInTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), sparqlQueryString);

                    if (validInput) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000172" :
                // GUI_COMPONENT_INPUT_TYPE: boolean

                if (Boolean.parseBoolean(this.jsonInputObject.getString("value"))) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000341" :
                // GUI_COMPONENT_INPUT_TYPE: click

                this.currInputIsValid = true;

                this.inputIsValid = true;

                return entryComponents;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000353" :
                // GUI_COMPONENT_INPUT_TYPE: phone number

                String pattern = "^\\+?\\s?\\d{0,}\\s?\\(?\\s?\\+?\\s?\\d{0,}\\s?\\(?\\s?\\)?\\s?\\d{0,}\\s?\\-?\\s?\\d{1,}\\s?\\d{1,}\\s?\\d{1,}$";

                if ((this.jsonInputObject.getString("value")).matches(pattern)) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000422" :
                // GUI_COMPONENT_INPUT_TYPE: ontology resource through literal and autocomplete

                if (annotationValidator
                        .isValid(mdburlEncoder.encodeUrl(this.jsonInputObject.getString("value"), "UTF-8"))) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;

        }

        this.currComponentObject.put("valid", "false");

        this.currInputIsValid = false;

        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

    }

    /**
     * This method get the corresponding properties for a subject class resource from the jena tdb and save the
     * corresponding statements in an JSONObject.
     * @param classSubject contains the uri of an resource
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkClassProperties (String classSubject, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

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

        String sparqlQueryString = constructBuilder.buildString();

        Model constructResult = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

        System.out.println("constructResult" + constructResult);

        StmtIterator resultIterator = constructResult.listStatements();

        while (resultIterator.hasNext() && this.currInputIsValid) {

            Statement currStatement = resultIterator.next();

            entryComponents = manageProperty(classSubject, currStatement, entryComponents, connectionToTDB);

        }

        return entryComponents;

    }

    /**
     * This method finds a value for a key, if the key exist in the entry components
     * @param entryComponents contains the data of an entry resource
     * @param key contains a key for investigation
     * @param value contains an empty string or the value of the key
     * @return the value for a key
     */
    private String findAndRemoveKeyInEntryComponents(JSONObject entryComponents, String key, String value) {

        Iterator<String> keyIter = entryComponents.keys();

        if (entryComponents.has(key)) {

            value = entryComponents.getString(key);

            entryComponents.remove(key);

            return value;

        } else {

            while (keyIter.hasNext()) {

                String currKey = keyIter.next();

                if (entryComponents.get(currKey) instanceof JSONObject) {

                    value = findAndRemoveKeyInEntryComponents(entryComponents.getJSONObject(currKey), key, value);

                }

            }

        }

        return value;

    }

    /**
     * This method reorders some <key, value> pairs, if necessary
     * @param outputObject contains the data for the output
     * @param entryComponents contains the data of an entry resource
     * @return the (reordered) JSON output object
     */
    private JSONObject reorderOutputObject (JSONObject outputObject, JSONObject entryComponents) {

        String keywordKnownResourceA = "";

        keywordKnownResourceA = findAndRemoveKeyInEntryComponents(entryComponents, "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411", keywordKnownResourceA);

        if (!keywordKnownResourceA.equals("")) {

            outputObject.put("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411", keywordKnownResourceA);

        }

        String keywordKnownResourceB = "";

        keywordKnownResourceB = findAndRemoveKeyInEntryComponents(entryComponents, "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412", keywordKnownResourceB);

        if (!keywordKnownResourceB.equals("")) {

            outputObject.put("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412", keywordKnownResourceB);

        }

        String loadPage = "";

        loadPage = findAndRemoveKeyInEntryComponents(entryComponents, "load_page", loadPage);

        if (!loadPage.equals("")) {

            outputObject.put("load_page", loadPage);

        }

        String loadPageLocalID = "";

        loadPageLocalID = findAndRemoveKeyInEntryComponents(entryComponents, "load_page_localID", loadPageLocalID);

        if ((!loadPageLocalID.equals("")) && (!loadPageLocalID.contains("."))) {

            outputObject.put("load_page_localID", loadPageLocalID);

        }

        return outputObject;

    }

    /**
     * This method generate the output data for autocomplete check
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkAutocomplete(JenaIOTDBFactory connectionToTDB) {

        JSONObject outputObject = new JSONObject();

        // autocomplete for ontology
        if (this.jsonInputObject.has("MDB_UIAP_0000000413")) {

            String externalOntologyURI = this.jsonInputObject.getString("MDB_UIAP_0000000413");

            if (externalOntologyURI.contains("http://www.morphdbase.de/Ontologies/MDB/MDBCore")) {

                PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                SelectBuilder selectBuilder = new SelectBuilder();

                selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                SelectBuilder subSelectBuilder = new SelectBuilder();

                subSelectBuilder = prefixesBuilder.addPrefixes(subSelectBuilder);

                // the angle brackets in the object are necessary to build a jena text query
                subSelectBuilder.addWhere
                        ("?s", "<http://jena.apache.org/text#query>",
                                "<(rdfs:label '" + this.jsonInputObject.getString("value") + "*' 10)>");
                subSelectBuilder.addWhere
                        ("?s", "<http://www.w3.org/2000/01/rdf-schema#label>", "?label");

                selectBuilder.addGraph("<http://www.morphdbase.de/Ontologies/MDB/" + ResourceFactory.createResource(externalOntologyURI).getLocalName() + ">", subSelectBuilder);

                QueryBuilderConverter queryBuilderConverter = new QueryBuilderConverter();

                String sparqlQueryString = queryBuilderConverter.toString(selectBuilder);

                String basicPathToLucene = "/home/path/to/tdb-lucene/external-ontologies/";

                JSONArray autoCompleteResults = connectionToTDB.pullAutoCompleteFromTDBLucene(OntologiesPath.mainDirectory + "external-ontologies/", basicPathToLucene, sparqlQueryString);

                this.currComponentObject.put("autoCompleteData", autoCompleteResults);

                JSONArray outputDataJSON = new JSONArray();

                JSONObject entryComponents = new JSONObject();

                entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                outputDataJSON.put(entryComponents);

                outputObject.put("data", outputDataJSON);

            }

        }

        outputObject.put("localID", jsonInputObject.getString("localID"));

        return outputObject;

    }

    /**
     * This method generate the output data for input check
     * @param classURI contains the URI of a subject class
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkInput(String classURI, JenaIOTDBFactory connectionToTDB) {

        JSONArray outputDataJSON = new JSONArray();

        JSONObject entryComponents = new JSONObject();

        entryComponents = checkClassProperties(classURI, entryComponents, connectionToTDB);

        //System.out.println("entryComponents = " + entryComponents);

        outputDataJSON.put(entryComponents);

        //System.out.println("outputDataJSON = " + outputDataJSON);

        JSONObject outputObject = new JSONObject();

        outputObject.put("data", outputDataJSON);

        outputObject = reorderOutputObject(outputObject, entryComponents);

        if (outputObject.getJSONArray("data").getJSONObject(0).getJSONObject(this.jsonInputObject.getString("localID")).has("data")) {

            Iterator<String> keys = outputObject.getJSONArray("data").getJSONObject(0).getJSONObject(this.jsonInputObject.getString("localID")).keys();

            JSONObject dummyObject = new JSONObject();

            while (keys.hasNext()) {

                String currKey = keys.next();

                dummyObject.put(currKey, outputObject.getJSONArray("data").getJSONObject(0).getJSONObject(this.jsonInputObject.getString("localID")).get(currKey));

            }

            keys = dummyObject.keys();

            while (keys.hasNext()) {

                String currKey = keys.next();

                outputObject.put(currKey, dummyObject.get(currKey));

            }

        }

        if (this.jsonInputObject.has("create_new_entry")) {
            // move information from input to output

            outputObject.put("create_new_entry", this.jsonInputObject.getString("create_new_entry"));

            this.jsonInputObject.remove("create_new_entry");

        }

        if (this.jsonInputObject.has("subsequently_workflow_action")) {
            // move information from input to output

            outputObject.put("subsequently_workflow_action", this.jsonInputObject.getString("subsequently_workflow_action"));

            this.jsonInputObject.remove("subsequently_workflow_action");

            if (this.jsonInputObject.has("subsequently_root")) {
                // move information from input to output

                outputObject.put("subsequently_root", this.jsonInputObject.getString("subsequently_root"));

                this.jsonInputObject.remove("subsequently_root");

            }

            if (this.jsonInputObject.has("keywords_to_transfer")) {
                // move information from input to output

                outputObject.put("keywords_to_transfer", this.jsonInputObject.getJSONObject("keywords_to_transfer"));

                this.jsonInputObject.remove("keywords_to_transfer");

            }

        }

        if (this.jsonInputObject.has("use_in_known_subsequent_WA")) {
            // move information from input to output

            outputObject.put("use_in_known_subsequent_WA", this.jsonInputObject.getJSONObject("use_in_known_subsequent_WA"));

            this.jsonInputObject.remove("use_in_known_subsequent_WA");

        }

        outputObject.put("localID", this.jsonInputObject.getString("localID"));

        return outputObject;
    }


    /**
     * This method is a getter for the overlay named graph.
     * @return a jena model for a MDB overlay
     */
    public Model getOverlayModel() {

        return this.overlayModel;

    }


    /**
     * This method fills the JSONObject with data of an entry component corresponding to a specific property.
     * @param resourceSubject is the URI of a individual resource
     * @param currStatement contains a subject(class or individual), a property and an object for calculation
     * @param entryComponents contains the data of an entry resource
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject manageProperty(String resourceSubject, Statement currStatement, JSONObject entryComponents, JenaIOTDBFactory connectionToTDB) {

        String propertyToCheck = currStatement.getPredicate().toString();

        switch (propertyToCheck) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000274" :
                // execution step trigger


                return checkAnnotationAnnotationProperties(resourceSubject, currStatement, entryComponents, connectionToTDB);


            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000386" :
                // required input (BOOLEAN)

                if (currStatement.getObject().isLiteral()) {

                    if (currStatement.getObject().asLiteral().getBoolean()) {

                        if (!(this.jsonInputObject.getString("value").isEmpty())) {

                            this.currComponentObject.put("valid", "true");

                            return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                        }

                    }

                }

                this.currComponentObject.put("valid", "false");

                this.currInputIsValid = false;

                return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000275" :
                // execution step triggered

                boolean keywordsToTransfer = false;

                JSONObject localIdentifiedResources = new JSONObject();

                if (this.jsonInputObject.has("keywords_to_transfer")) {

                    localIdentifiedResources = new JSONObject(this.jsonInputObject.getJSONObject("keywords_to_transfer").toString());

                    keywordsToTransfer = true;

                }

                KBOrder kbOrder = new KBOrder(connectionToTDB, this.pathToOntologies, resourceSubject);

                // get the sorted input knowledge base
                JSONArray sortedKBJSONArray = kbOrder.getSortedKBJSONArray();

                System.out.println("sortedKBJSONArray" + sortedKBJSONArray);

                // get the sorted indices of the knowledge base
                JSONArray sortedKBIndicesJSONArray = kbOrder.getSortedKBIndicesJSONArray();

                System.out.println("sortedKBIndicesJSONArray" + sortedKBIndicesJSONArray);

                MDBJSONObjectFactory mdbjsonObjectFactory;

                MDBIDFinder mdbidFinder = new MDBIDFinder(this.individualURI, connectionToTDB);

                if (mdbidFinder.hasMDBCoreID() &&
                        mdbidFinder.hasMDBEntryID() &&
                        mdbidFinder.hasMDBUEID()) {

                    this.jsonInputObject.put("mdbentryid", mdbidFinder.getMDBEntryID());
                    this.jsonInputObject.put("mdbcoreid", mdbidFinder.getMDBCoreID());

                    if (keywordsToTransfer) {

                        mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), localIdentifiedResources, this.overlayModel);

                    } else {

                        mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), this.overlayModel);

                    }

                } else {

                    String resourceFromHTMLForm =  "http://www.morphdbase.de/resource/" + this.jsonInputObject.getString("html_form");

                    System.out.println("resourceFromHTMLForm = " + resourceFromHTMLForm);

                    mdbidFinder = new MDBIDFinder(resourceFromHTMLForm, connectionToTDB);

                    if (mdbidFinder.hasMDBCoreID() &&
                            mdbidFinder.hasMDBEntryID() &&
                            mdbidFinder.hasMDBUEID()) {

                        this.jsonInputObject.put("mdbentryid", mdbidFinder.getMDBEntryID());
                        this.jsonInputObject.put("mdbcoreid", mdbidFinder.getMDBCoreID());

                        if (keywordsToTransfer) {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), localIdentifiedResources, this.overlayModel);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), this.overlayModel);

                        }

                    } else if(this.jsonInputObject.has("mdbueid_uri")) {

                        if (this.individualURI.startsWith("http://www.morphdbase.de/resource/dummy-overlay#")) {
                            // special case for GUI_COMPONENT__BASIC_WIDGET: specify required information

                            MongoDBConnection mongoDBConnection = new MongoDBConnection("localhost", 27017);

                            mongoDBConnection.putJSONInputObjectInMongoDB(this.jsonInputObject);

                            String mdbCoreID = ResourceFactory.createResource(this.individualURI).getNameSpace().substring(0, ResourceFactory.createResource(this.individualURI).getNameSpace().length() - 1);
                            String mdbEntryID = ResourceFactory.createResource(this.individualURI).getNameSpace().substring(0, ResourceFactory.createResource(this.individualURI).getNameSpace().length() - 1);
                            String mdbUEID = this.jsonInputObject.getString("mdbueid_uri");

                            this.jsonInputObject.put("mdbentryid", mdbEntryID);
                            this.jsonInputObject.put("mdbcoreid", mdbCoreID);

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbCoreID, mdbEntryID, mdbUEID, localIdentifiedResources, this.overlayModel);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.jsonInputObject.getString("mdbueid_uri"), this.overlayModel);

                        }

                    } else {

                        mdbjsonObjectFactory = new MDBJSONObjectFactory();

                    }

                }

                JSONObject outputObject = mdbjsonObjectFactory.convertKBToJSONObject(sortedKBJSONArray, sortedKBIndicesJSONArray, this.currComponentObject, this.jsonInputObject, connectionToTDB);

                this.overlayModel = mdbjsonObjectFactory.getOverlayModel();

                if (outputObject.has("valid")) {
                    // not in every case exist a valid information in the JSON structure

                    if (!Boolean.valueOf(outputObject.getString("valid"))) {

                        this.currInputIsValid = false;

                    }

                }

                if (outputObject.has("subsequently_workflow_action")) {

                    this.jsonInputObject.put("subsequently_workflow_action", outputObject.getString("subsequently_workflow_action"));

                    if (outputObject.has("subsequently_root")) {

                        this.jsonInputObject.put("subsequently_root", outputObject.getString("subsequently_root"));

                        outputObject.remove("subsequently_root");

                    }

                    if (outputObject.has("keywords_to_transfer")) {

                        this.jsonInputObject.put("keywords_to_transfer", outputObject.getJSONObject("keywords_to_transfer"));

                        outputObject.remove("keywords_to_transfer");

                    }

                    outputObject.remove("subsequently_workflow_action");

                }

                if (outputObject.has("use_in_known_subsequent_WA")) {

                    this.jsonInputObject.put("use_in_known_subsequent_WA", outputObject.getJSONObject("use_in_known_subsequent_WA"));

                    outputObject.remove("use_in_known_subsequent_WA");

                }

                return entryComponents.put(this.jsonInputObject.getString("localID"), outputObject);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000490" :
                // triggers 'click' for MDB entry component

                RDFNode currObject = currStatement.getObject();

                SelectBuilder selectBuilder = new SelectBuilder();

                PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                FilterBuilder filterBuilder = new FilterBuilder();

                selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                SelectBuilder innerSelect = new SelectBuilder();

                innerSelect.addWhere(currObject, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "?o");

                selectBuilder.addVar(selectBuilder.makeVar("?o"));

                SPARQLFilter sparqlFilter = new SPARQLFilter();

                ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

                filterItems = filterBuilder.addItems(filterItems, "?o", "<http://www.w3.org/2002/07/owl#NamedIndividual>");

                ArrayList<String> filter = sparqlFilter.getNotINFilter(filterItems);

                selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                selectBuilder.addGraph("?g", innerSelect);

                String sparqlQueryString = selectBuilder.buildString();

                String classID = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?o");

                JSONObject dummyJSONObject = checkInput(classID, connectionToTDB);

                if (dummyJSONObject.getJSONArray("data").getJSONObject(0).getJSONObject(this.jsonInputObject.getString("localID")).has("valid")) {
                    // not in every case exist a valid information in the JSON structure

                    if (!Boolean.valueOf(dummyJSONObject.getJSONArray("data").getJSONObject(0).getJSONObject(this.jsonInputObject.getString("localID")).getString("valid"))) {

                        this.currInputIsValid = false;

                    }

                }

                // entryComponents was already created in methods call "checkInput" above
                return dummyJSONObject.getJSONArray("data").getJSONObject(0);

            case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000522" :
                // triggers status transition 'create new MDB entry' from overlay  [BOOLEAN]

                if (currStatement.getObject().isLiteral()) {

                    if (currStatement.getObject().asLiteral().getBoolean()) {
                        // true case

                        this.jsonInputObject.put("create_new_entry", "true");

                        break;

                    }

                }

                this.jsonInputObject.put("create_new_entry", "false");

                break;

        }

        return entryComponents;

    }


}
