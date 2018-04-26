/*
 * Created by Roman Baum on 19.02.16.
 * Last modified by Roman Baum on 06.03.18.
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
import org.apache.jena.sparql.core.Var;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private MongoDBConnection mongoDBConnection;

    public InputInterpreter(JSONObject jsonInputObject, MongoDBConnection mongoDBConnection) {

        this.jsonInputObject = jsonInputObject;
        this.mongoDBConnection = mongoDBConnection;

    }

    public InputInterpreter(String individualURI, JSONObject jsonInputObject, MongoDBConnection mongoDBConnection) {

        this.individualURI = individualURI;

        this.jsonInputObject = jsonInputObject;

        this.mongoDBConnection = mongoDBConnection;

    }

    public InputInterpreter(String individualURI, JSONObject jsonInputObject, Model overlayModel, MongoDBConnection mongoDBConnection) {

        this.individualURI = individualURI;

        this.jsonInputObject = jsonInputObject;

        this.overlayModel = overlayModel;

        this.mongoDBConnection = mongoDBConnection;

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

        checkUseKeywordsFromComposition(constructResult);

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
     * This method generate the output data for input check of an entry list query
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSONObject with data of an entry component
     */
    public JSONObject checkInputForListEntry(JenaIOTDBFactory connectionToTDB) {

        Model unionModel = ModelFactory.createDefaultModel();

        FilterBuilder filterBuilder = new FilterBuilder();

        PrefixesBuilder draftListPB = new PrefixesBuilder();

        ConstructBuilder draftListCB = new ConstructBuilder();

        SelectBuilder draftListSWB = new SelectBuilder();

        Var sVar = draftListCB.makeVar("?s"), pVar = draftListCB.makeVar("?publishedPVar"), oVar = draftListCB.makeVar("?publishedOVar");

        draftListCB.addConstruct(sVar, pVar, oVar);

        draftListSWB = draftListPB.addPrefixes(draftListSWB);

        draftListSWB.addWhere(sVar, "<http://purl.org/spar/pso/withStatus>", "?oDummy");

        draftListSWB.addWhere(sVar, pVar, oVar);

        draftListCB = draftListPB.addPrefixes(draftListCB);

        if (this.jsonInputObject.getString("value").equals("all")) {

            draftListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000748");
            // NAMED_GRAPH: NAMED_GRAPH: MDB draft media versions list
            draftListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000749");
            // NAMED_GRAPH: MDB draft morphological description versions list
            draftListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000750");
            // NAMED_GRAPH: MDB draft specimen versions list

        } else if (this.jsonInputObject.getString("value").equals("md")) {

            draftListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000749");
            // NAMED_GRAPH: MDB draft morphological description versions list

        } else if (this.jsonInputObject.getString("value").equals("s")) {

            draftListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000750");
            // NAMED_GRAPH: MDB draft specimen versions list

        }

        draftListCB.addGraph("?g", draftListSWB);

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

        filterItems = filterBuilder.addItems(filterItems, pVar.toString(), "<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000379>");
        // has header

        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

        draftListCB = filterBuilder.addFilter(draftListCB, filter);

        filterItems.clear();

        String sparqlQueryString = draftListCB.buildString();

        TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

        Model draftModel = connectionToTDB.pullDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494"), sparqlQueryString);
        // MDB_WORKSPACE_DIRECTORY: MDB core workspace directory

        unionModel.add(draftModel);

        filterBuilder = new FilterBuilder();

        PrefixesBuilder publishedListPB = new PrefixesBuilder();

        ConstructBuilder publishedListCB = new ConstructBuilder();

        SelectBuilder publishedListSWB = new SelectBuilder();

        Var publishedSVar = publishedListCB.makeVar("?publishedSVar"), publishedPVar = publishedListCB.makeVar("?publishedPVar"), publishedOVar = publishedListCB.makeVar("?publishedOVar");

        publishedListCB.addConstruct(publishedSVar, publishedPVar, publishedOVar);

        publishedListSWB = publishedListPB.addPrefixes(publishedListSWB);

        publishedListSWB.addWhere(publishedSVar, "<http://purl.org/spar/pso/withStatus>", "?oDummy");

        publishedListSWB.addWhere(publishedSVar, publishedPVar, publishedOVar);

        publishedListCB = publishedListPB.addPrefixes(publishedListCB);

        if (this.jsonInputObject.getString("value").equals("all")) {

            publishedListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000751");
            // NAMED_GRAPH: NAMED_GRAPH: MDB published media versions list
            publishedListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000752");
            // NAMED_GRAPH: MDB published morphological description versions list
            publishedListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000753");
            // NAMED_GRAPH: MDB published specimen versions list

        } else if (this.jsonInputObject.getString("value").equals("md")) {

            publishedListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000752");
            // NAMED_GRAPH: MDB published morphological description versions list

        } else if (this.jsonInputObject.getString("value").equals("s")) {

            publishedListCB.fromNamed("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000753");
            // NAMED_GRAPH: MDB published specimen versions list

        }

        publishedListCB.addGraph("?g", publishedListSWB);

        SPARQLFilter publishedSPARQLFilter = new SPARQLFilter();

        ArrayList<ArrayList<String>> publishedFilterItems = new ArrayList<>();

        publishedFilterItems = filterBuilder.addItems(publishedFilterItems, publishedPVar.toString(), "<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000379>");
        // has header

        ArrayList<String> publishedFilter = publishedSPARQLFilter.getINFilter(publishedFilterItems);

        publishedListCB = filterBuilder.addFilter(publishedListCB, publishedFilter);

        publishedFilterItems.clear();

        String publishedSPARQLQueryString = publishedListCB.buildString();

        Model publishedModel = connectionToTDB.pullDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494"), publishedSPARQLQueryString);
        // MDB_WORKSPACE_DIRECTORY: MDB core workspace directory

        unionModel.add(publishedModel);

        Selector selector = new SimpleSelector(null, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000379"), null, "");
        // has header

        Iterator<Statement> stmtIter = unionModel.listStatements(selector);

        JSONArray outputDataJSON = new JSONArray();

        JSONObject knownUser = new JSONObject();

        while (stmtIter.hasNext()) {

            Statement currStmt = stmtIter.next();

            Resource mdbEntryID = currStmt.getSubject().asResource();

            Resource headerNG = currStmt.getObject().asResource();

            Model headerModel;

            if (headerNG.toString().contains("-d_")
                    && headerNG.toString().contains(jsonInputObject.getString("mdbueid"))) {

                headerModel = connectionToTDB.pullNamedModelFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"), headerNG.toString());
                // MDB_WORKSPACE_DIRECTORY: MDB draft workspace directory

            } else {

                headerModel = connectionToTDB.pullNamedModelFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000502"), headerNG.toString());
                // MDB_WORKSPACE_DIRECTORY: MDB published workspace directory

            }

            JSONObject entryComponents = new JSONObject();

            entryComponents.put("uri", mdbEntryID.toString());

            if (headerModel.contains(mdbEntryID, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000602"))) {

                Statement stmt = headerModel.getProperty(mdbEntryID, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000602"));

                entryComponents.put(stmt.getPredicate().getLocalName(), stmt.getObject().asResource().getLocalName());

            }

            if (headerModel.contains(mdbEntryID, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000088"))) {

                Statement stmt = headerModel.getProperty(mdbEntryID, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000088"));

                entryComponents.put("entryLabel", stmt.getObject().asLiteral().getLexicalForm());

            }

            if (headerModel.contains(mdbEntryID, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000287"))) {

                Statement stmt = headerModel.getProperty(mdbEntryID, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000287"));

                entryComponents.put("entryLabel2", stmt.getObject().asLiteral().getLexicalForm());

            }

            if (headerModel.contains(mdbEntryID, ResourceFactory.createProperty("http://purl.org/spar/pso/withStatus"))) {

                Statement stmt = headerModel.getProperty(mdbEntryID, ResourceFactory.createProperty("http://purl.org/spar/pso/withStatus"));

                entryComponents.put(stmt.getPredicate().getLocalName(), stmt.getObject().asResource().getLocalName());

            }

            if (headerModel.contains(mdbEntryID, ResourceFactory.createProperty("http://purl.org/pav/createdBy"))) {

                Statement stmt = headerModel.getProperty(mdbEntryID, ResourceFactory.createProperty("http://purl.org/pav/createdBy"));

                String ueidString = stmt.getObject().asResource().getNameSpace();

                Resource ueid = ResourceFactory.createResource(ueidString.substring(0, (ueidString.length()-1)));

                if (!(knownUser.has(ueid.toString()))) {

                    PrefixesBuilder userListPB = new PrefixesBuilder();

                    SelectBuilder userListCB = new SelectBuilder();

                    SelectBuilder userListSWB = new SelectBuilder();

                    Var userVar = userListCB.makeVar("?o");

                    userListCB.addVar(userVar);

                    userListSWB = userListPB.addPrefixes(userListSWB);

                    userListSWB.addWhere(ueid , "<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000765>", userVar);
                    // has MDB user entry ID named graph

                    userListCB = userListPB.addPrefixes(userListCB);

                    userListCB.addGraph("<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000730>", userListSWB);
                    // NAMED_GRAPH: MDB user entry list

                    String userListSPARQLQueryString = userListCB.buildString();

                    String userEntryIDNG = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"),userListSPARQLQueryString, userVar.toString());

                    Model userEntryModel = connectionToTDB.pullNamedModelFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), userEntryIDNG);

                    if (userEntryModel.contains(ueid, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000088"))) {

                        Statement userStmt = userEntryModel.getProperty(ueid, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000088"));

                        entryComponents.put(userStmt.getPredicate().getLocalName(), userStmt.getObject().asLiteral().getLexicalForm());

                        if (!(knownUser.has(ueid.toString()))) {

                            JSONObject currKnownUser = new JSONObject();

                            currKnownUser.put(userStmt.getPredicate().getLocalName(), userStmt.getObject().asLiteral().getLexicalForm());

                            knownUser.put(ueid.toString(), currKnownUser);

                        } else {

                            (knownUser.getJSONObject(ueid.toString()))
                                    .put(userStmt.getPredicate().getLocalName(), userStmt.getObject().asLiteral().getLexicalForm());

                        }

                    }

                    if (userEntryModel.contains(ueid, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000288"))) {

                        Statement userStmt = userEntryModel.getProperty(ueid, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000288"));

                        entryComponents.put(userStmt.getPredicate().getLocalName(), userStmt.getObject().asLiteral().getLexicalForm());

                        if (!(knownUser.has(ueid.toString()))) {

                            JSONObject currKnownUser = new JSONObject();

                            currKnownUser.put(userStmt.getPredicate().getLocalName(), userStmt.getObject().asLiteral().getLexicalForm());

                            knownUser.put(ueid.toString(), currKnownUser);

                        } else {

                            (knownUser.getJSONObject(ueid.toString()))
                                    .put(userStmt.getPredicate().getLocalName(), userStmt.getObject().asLiteral().getLexicalForm());

                        }

                    }

                } else {

                    Iterator<String> keysIter = knownUser.getJSONObject(ueid.toString()).keys();

                    while (keysIter.hasNext()) {

                        String currKey = keysIter.next();

                        entryComponents.put(currKey, knownUser.getJSONObject(ueid.toString()).getString(currKey));

                    }

                }

            }

            int numberOfKeys = 0;

            Iterator<String> keys = entryComponents.keys();

            while (keys.hasNext()) {

                keys.next();

                numberOfKeys++;

            }

            if (numberOfKeys > 1) {

                outputDataJSON.put(entryComponents);

            }

        }

        JSONObject outputObject = new JSONObject();

        outputObject.put("data", outputDataJSON);

        return outputObject;

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

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000077" :
                // GUI_COMPONENT_INPUT_TYPE: float

                if (stringChecker.checkIfStringIsAFloat(this.jsonInputObject.getString("value"))) {

                    if (Float.parseFloat(this.jsonInputObject.getString("value")) > 0) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

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

                MDBURLEncoder mdbLEncoderSomeValue = new MDBURLEncoder();

                UrlValidator urlValidatorSomeValue = new UrlValidator();

                if (urlValidatorSomeValue.isValid(mdbLEncoderSomeValue.encodeUrl(this.jsonInputObject.getString("value"), "UTF-8"))) {

                    this.currComponentObject.put("valid", "true");

                    this.currInputIsValid = true;

                    this.inputIsValid = true;

                    return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                }

                break;


            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000111" :
                // GUI_COMPONENT_INPUT_TYPE: date time stamp

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                dateFormat.setLenient(false);

                try {

                    dateFormat.parse((this.jsonInputObject.getString("value")).trim());

                } catch (ParseException pe) {

                    break;

                }

                this.currComponentObject.put("valid", "true");

                this.currInputIsValid = true;

                this.inputIsValid = true;

                return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

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

                if (this.jsonInputObject.getString("value").equals("true")
                        || this.jsonInputObject.getString("value").equals("false")) {

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

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000558" :
                // GUI_COMPONENT_INPUT_TYPE: integer percentage (0-100%)

                if (stringChecker.checkIfStringIsAnInteger(this.jsonInputObject.getString("value"))) {

                    if (Integer.parseInt(this.jsonInputObject.getString("value")) > 0
                            && Integer.parseInt(this.jsonInputObject.getString("value")) <= 100) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000559" :
                // GUI_COMPONENT_INPUT_TYPE: float percentage (0-100%)

                if (stringChecker.checkIfStringIsAFloat(this.jsonInputObject.getString("value"))) {

                    if (Float.parseFloat(this.jsonInputObject.getString("value")) > 0
                            && Float.parseFloat(this.jsonInputObject.getString("value")) <= 100) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000560" :
                // GUI_COMPONENT_INPUT_TYPE: float pH

                if (stringChecker.checkIfStringIsAFloat(this.jsonInputObject.getString("value"))) {

                    if (Float.parseFloat(this.jsonInputObject.getString("value")) > 0
                            && Float.parseFloat(this.jsonInputObject.getString("value")) <= 14) {

                        this.currComponentObject.put("valid", "true");

                        this.currInputIsValid = true;

                        this.inputIsValid = true;

                        return entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

                    }

                }

                break;

        }

        if ((this.jsonInputObject.getString("value")).trim().isEmpty()) {
            // check if String only conatins whitespaces

            this.jsonInputObject.put("value", "");

            this.currComponentObject.put("valid", "true");

            this.currInputIsValid = true;

            this.inputIsValid = true;

        } else {

            SelectBuilder selectBuilder = new SelectBuilder();

            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

            SelectBuilder innerSelect = new SelectBuilder();

            Property property = ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000388");
            // MDB error-message

            innerSelect.addWhere("<" + typeToCheck + ">", property, "?o");

            selectBuilder.addVar(selectBuilder.makeVar("?o"));

            selectBuilder.addGraph("?g", innerSelect);

            String sparqlQueryString = selectBuilder.buildString();

            String errorMessage = connectionToTDB.pullSingleDataFromTDB(OntologiesPath.pathToOntology, sparqlQueryString, "?o");

            if (!errorMessage.isEmpty()) {

                this.currComponentObject.put(property.getLocalName(), errorMessage);

            }

            this.currComponentObject.put("valid", "false");

            this.currInputIsValid = false;

        }



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
     * This method checks if the model contains the 'use keywords from composition  [BOOLEAN]' or not.
     * @param constructResult is a model to check for input
     */
    private void checkUseKeywordsFromComposition(Model constructResult) {

        Property useKeywordsFromComposition = ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000529");
        // use keywords from composition  [BOOLEAN]

        if (constructResult.contains(null, useKeywordsFromComposition) && this.inputIsValid) {

            Statement useKeywordsFromCompositionStmt = constructResult.getProperty(null, useKeywordsFromComposition);

            this.jsonInputObject.put("useKeywordsFromComposition", useKeywordsFromCompositionStmt.getObject().asLiteral().getLexicalForm());

            constructResult.remove(useKeywordsFromCompositionStmt);

        }

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


        if (this.jsonInputObject.has("MDB_UIAP_0000000413") ||
                // autocomplete for ontology
                this.jsonInputObject.has("MDB_UIAP_0000000578")) {
                // autocomplete for

            JSONArray externalOntologyURIJSON = new JSONArray();

            if (this.jsonInputObject.has("MDB_UIAP_0000000413")) {

                if (this.jsonInputObject.get("MDB_UIAP_0000000413") instanceof JSONArray) {

                    externalOntologyURIJSON = this.jsonInputObject.getJSONArray("MDB_UIAP_0000000413");

                } else if (this.jsonInputObject.get("MDB_UIAP_0000000413") instanceof String) {

                    externalOntologyURIJSON.put(this.jsonInputObject.getString("MDB_UIAP_0000000413"));

                }

            }

            if (this.jsonInputObject.has("MDB_UIAP_0000000578")) {

                if (this.jsonInputObject.get("MDB_UIAP_0000000578") instanceof JSONArray) {

                    externalOntologyURIJSON = this.jsonInputObject.getJSONArray("MDB_UIAP_0000000578");

                } else if (this.jsonInputObject.get("MDB_UIAP_0000000578") instanceof String) {

                    externalOntologyURIJSON.put(this.jsonInputObject.getString("MDB_UIAP_0000000578"));

                }

            }

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

            selectBuilder.addGraph("?g", subSelectBuilder);

            for (int i = 0; i < externalOntologyURIJSON.length(); i++) {

                selectBuilder.fromNamed("http://www.morphdbase.de/Ontologies/MDB/" + ResourceFactory.createResource(externalOntologyURIJSON.getString(i)).getLocalName());

            }

            selectBuilder.setDistinct(true);

            QueryBuilderConverter queryBuilderConverter = new QueryBuilderConverter();

            String sparqlQueryString = queryBuilderConverter.toString(selectBuilder);

            String basicPathToLucene = "/home/YOUR_HOME_DIR/tdb-lucene/external-ontologies/";

            JSONArray autoCompleteResults = connectionToTDB.pullAutoCompleteFromTDBLucene(OntologiesPath.mainDirectory + "external-ontologies/", basicPathToLucene, sparqlQueryString);

            this.currComponentObject.put("autoCompleteData", autoCompleteResults);

            JSONArray outputDataJSON = new JSONArray();

            JSONObject entryComponents = new JSONObject();

            entryComponents.put(this.jsonInputObject.getString("localID"), this.currComponentObject);

            outputDataJSON.put(entryComponents);

            outputObject.put("data", outputDataJSON);

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

                        mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), localIdentifiedResources, this.overlayModel, this.mongoDBConnection);

                    } else {

                        mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), this.overlayModel, this.mongoDBConnection);

                    }

                } else {

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder innerSelect = new SelectBuilder();

                    innerSelect.addWhere("<" + this.individualURI + ">", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<" + resourceSubject + ">");
                    innerSelect.addWhere("<" + this.individualURI + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000587>", "?isGeneralItem");
                    // is general application item  [BOOLEAN]

                    selectBuilder.addVar(selectBuilder.makeVar("?isGeneralItem"));

                    selectBuilder.addGraph("?g", innerSelect);

                    String sparqlQueryString = selectBuilder.buildString();

                    String isGeneralItem = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?isGeneralItem");

                    String resourceFromHTMLForm;

                    if (isGeneralItem.equals("true")) {

                        resourceFromHTMLForm = "http://www.morphdbase.de/" + this.jsonInputObject.getString("html_form");

                    } else {

                        resourceFromHTMLForm = "http://www.morphdbase.de/resource/" + this.jsonInputObject.getString("html_form");

                    }

                    System.out.println("resourceFromHTMLForm = " + resourceFromHTMLForm);

                    mdbidFinder = new MDBIDFinder(resourceFromHTMLForm, connectionToTDB);

                    if (mdbidFinder.hasMDBCoreID() &&
                            mdbidFinder.hasMDBEntryID() &&
                            mdbidFinder.hasMDBUEID()) {

                        this.jsonInputObject.put("mdbentryid", mdbidFinder.getMDBEntryID());
                        this.jsonInputObject.put("mdbcoreid", mdbidFinder.getMDBCoreID());

                        if (keywordsToTransfer) {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), localIdentifiedResources, this.overlayModel, this.mongoDBConnection);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbidFinder.getMDBCoreID(), mdbidFinder.getMDBEntryID(), mdbidFinder.getMDBUEID(), this.overlayModel, this.mongoDBConnection);

                        }

                    } else if(this.jsonInputObject.has("mdbueid_uri")) {

                        if (this.individualURI.startsWith("http://www.morphdbase.de/resource/dummy-overlay#")) {
                            // special case for GUI_COMPONENT__BASIC_WIDGET: specify required information

                            this.mongoDBConnection.putJSONInputObjectInMongoDB(this.jsonInputObject);

                            String mdbCoreID = ResourceFactory.createResource(this.individualURI).getNameSpace().substring(0, ResourceFactory.createResource(this.individualURI).getNameSpace().length() - 1);
                            String mdbEntryID = ResourceFactory.createResource(this.individualURI).getNameSpace().substring(0, ResourceFactory.createResource(this.individualURI).getNameSpace().length() - 1);
                            String mdbUEID = this.jsonInputObject.getString("mdbueid_uri");

                            this.jsonInputObject.put("mdbentryid", mdbEntryID);
                            this.jsonInputObject.put("mdbcoreid", mdbCoreID);

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(mdbCoreID, mdbEntryID, mdbUEID, localIdentifiedResources, this.overlayModel, this.mongoDBConnection);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.jsonInputObject.getString("mdbueid_uri"), this.overlayModel, this.mongoDBConnection);

                        }

                    } else {

                        mdbjsonObjectFactory = new MDBJSONObjectFactory(this.mongoDBConnection);

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
