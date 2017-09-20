/*
 * Created by Roman Baum on 10.04.15.
 * Last modified by Roman Baum on 19.09.17.
 */

package mdb.packages;

import mdb.basic.*;
import mdb.mongodb.MongoDBConnection;
import mdb.packages.operation.OperationManager;
import mdb.packages.operation.OutputGenerator;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.SPARQLFilter;
import mdb.vocabulary.FOAFAdvanced;
import mdb.vocabulary.IndividualURI;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


public class MDBJSONObjectFactory {

    private String pathToOntologies = OntologiesPath.pathToOntology;

    // create a model to get all annotation properties
    private AnnotationPropertySet annotationPropertySet = new AnnotationPropertySet(this.pathToOntologies);
    private JSONObject annotationPropertiesInJSON = this.annotationPropertySet.createJSONObject();

    // create a model to get all data properties
    private DataPropertySet dataPropertySet = new DataPropertySet(this.pathToOntologies);
    private JSONObject dataPropertiesInJSON = this.dataPropertySet.createJSONObject();

    // create a model to get all object properties
    private ObjectPropertySet objectPropertySet = new ObjectPropertySet(this.pathToOntologies);
    private JSONObject objectPropertiesInJSON = this.objectPropertySet.createJSONObject();

    private MDBDate mdbDate = new MDBDate();

    private JSONObject bNodeIdentifier = new JSONObject();

    // create a model to get all classes
    private ClassSet classSet = new ClassSet(this.pathToOntologies);
    private Model classModel = this.classSet.createModel();

    private String mdbCoreID = "", mdbEntryID = "", mdbUEID = "", currentFocus = "", parentRoot = "", createOverlayNG;

    private boolean mdbCoreIDNotEmpty = false, mdbEntryIDNotEmpty = false, mdbUEIDNotEmpty = false,
            focusHasNewNS = false, parentRootExist = false, hasCreateOverlayInput = false, updateComposition = false;

    private int parentRootPosition;

    private JSONObject generatedResources = new JSONObject(), identifiedResources = new JSONObject(),
            infoInput = new JSONObject(), numberOfClassInstances = new JSONObject(),
            classOverlayMapping = new JSONObject(), numberOfClassInstancesOverlay = new JSONObject(),
            entrySpecificAndDefaultResourcesMap = new JSONObject(), rootResourcesOfCompositions = new JSONObject(),
            parentComponents = new JSONObject(), compositionUpdateJSON = new JSONObject();

    private Model overlayModel;

    private MongoDBConnection mongoDBConnection = new MongoDBConnection("localhost", 27017);
    public MDBJSONObjectFactory() {

    }

    public MDBJSONObjectFactory(JSONObject identifiedResources, Model overlayModel) {

        this.identifiedResources = identifiedResources;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(JSONObject identifiedResources, JSONObject infoInput, Model overlayModel) {

        this.identifiedResources = identifiedResources;
        this.infoInput = infoInput;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(String mdbUEID, Model overlayModel) {

        this.mdbUEID = mdbUEID;
        this.currentFocus = mdbUEID;
        this.mdbUEIDNotEmpty = true;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(String mdbUEID, JSONObject identifiedResources, Model overlayModel) {

        this.mdbUEID = mdbUEID;
        this.currentFocus = mdbUEID;
        this.identifiedResources = identifiedResources;
        this.mdbUEIDNotEmpty = true;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(String mdbUEID, JSONObject identifiedResources, JSONObject infoInput, Model overlayModel) {

        this.mdbUEID = mdbUEID;
        this.currentFocus = mdbUEID;
        this.identifiedResources = identifiedResources;
        this.infoInput = infoInput;
        this.mdbUEIDNotEmpty = true;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(String mdbCoreID, String mdbEntryID, String mdbUEID, Model overlayModel) {

        this.mdbCoreID = mdbCoreID;
        this.mdbEntryID = mdbEntryID;
        this.currentFocus = mdbEntryID;
        this.mdbUEID = mdbUEID;
        this.mdbCoreIDNotEmpty = true;
        this.mdbEntryIDNotEmpty = true;
        this.mdbUEIDNotEmpty = true;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(String mdbCoreID, String mdbEntryID, String mdbUEID, JSONObject identifiedResources, Model overlayModel) {

        this.mdbCoreID = mdbCoreID;
        this.mdbEntryID = mdbEntryID;
        this.currentFocus = mdbEntryID;
        this.mdbUEID = mdbUEID;
        this.identifiedResources = identifiedResources;
        this.mdbCoreIDNotEmpty = true;
        this.mdbEntryIDNotEmpty = true;
        this.mdbUEIDNotEmpty = true;
        this.overlayModel = overlayModel;

    }

    public MDBJSONObjectFactory(String mdbCoreID, String mdbEntryID, String mdbUEID, JSONObject identifiedResources, JSONObject infoInput, Model overlayModel) {

        this.mdbCoreID = mdbCoreID;
        this.mdbEntryID = mdbEntryID;
        this.currentFocus = mdbEntryID;
        this.mdbUEID = mdbUEID;
        this.identifiedResources = identifiedResources;
        this.infoInput = infoInput;
        this.mdbCoreIDNotEmpty = true;
        this.mdbEntryIDNotEmpty = true;
        this.mdbUEIDNotEmpty = true;
        this.overlayModel = overlayModel;

    }




    /**
     * This method calculates a default entry composition of an individual
     * @param individualToCheck contains the root individual, which should be use for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an unnamed model with a default entry composition
     */
    private Model calculateDefaultEntryComposition(JSONArray individualToCheck, JenaIOTDBFactory connectionToTDB) {

        String root = individualToCheck.getString(0);

        Model defaultComposition = ModelFactory.createDefaultModel();

        JSONArray allClassesFromTDB = new JSONArray();

        while (!individualToCheck.isNull(0)) {

            FilterBuilder filterBuilder = new FilterBuilder();

            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

            ConstructBuilder constructBuilder = new ConstructBuilder();

            constructBuilder = prefixesBuilder.addPrefixes(constructBuilder);

            constructBuilder.addConstruct("?s", "?p", "?o");

            SelectBuilder tripleSPOConstruct = new SelectBuilder();

            tripleSPOConstruct.addWhere("?s", "?p", "?o");

            SPARQLFilter sparqlFilter = new SPARQLFilter();

            ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

            filterItems = filterBuilder.addItems(filterItems, "?s", "<" + individualToCheck.getString(0) + ">");

            ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

            constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

            filterItems.clear();

            constructBuilder.addGraph("?g", tripleSPOConstruct);

            String sparqlQueryString = constructBuilder.buildString();

            Model currIndividualModel = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

            defaultComposition.add(currIndividualModel);

            StmtIterator resultIterator = currIndividualModel.listStatements();

            if (currIndividualModel.contains(ResourceFactory.createResource(individualToCheck.getString(0)), ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000653")) &&
                    // is root entry component of composition contained in named graph
                    (!individualToCheck.getString(0).equals(root))) {

                System.out.println("The individual " + individualToCheck.getString(0) + " is another root element");


            } else {

                while (resultIterator.hasNext()) {

                    Statement currStmt = resultIterator.nextStatement();

                    String currProperty = currStmt.getPredicate().toString();

                    if (currProperty.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000040")) {
                        // has MDB entry component

                        individualToCheck.put(currStmt.getObject().toString());

                    } else if (currProperty.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                        // rdf:type

                        if (!((currStmt.getObject().toString()).equals("http://www.w3.org/2002/07/owl#NamedIndividual"))) {

                            allClassesFromTDB.put(currStmt.getObject().toString());

                        }

                    }

                }
            }

            // find axiom for this individual
            PrefixesBuilder prefixesAxiomBuilder = new PrefixesBuilder();

            ConstructBuilder constructAxiomBuilder = new ConstructBuilder();

            constructAxiomBuilder = prefixesAxiomBuilder.addPrefixes(constructAxiomBuilder);

            constructAxiomBuilder.addConstruct("?s", "?p", "?o");

            SelectBuilder tripleAxiomSPOConstruct = new SelectBuilder();

            tripleAxiomSPOConstruct.addWhere("?s", "?p", "?o");
            tripleAxiomSPOConstruct.addWhere("?s", "<http://www.w3.org/2002/07/owl#annotatedSource>", "<" + individualToCheck.getString(0) + ">");

            constructAxiomBuilder.addGraph("?g", tripleAxiomSPOConstruct);

            sparqlQueryString = constructAxiomBuilder.buildString();

            Model currIndividualAxiomModel = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

            // add axioms individual statements
            defaultComposition.add(currIndividualAxiomModel);

            // remove the old key
            individualToCheck.remove(0);

        }

        for (int i = 0; i < allClassesFromTDB.length(); i++) {

            // find the statements for the corresponding class
            FilterBuilder filterBuilder = new FilterBuilder();

            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

            ConstructBuilder constructBuilder = new ConstructBuilder();

            constructBuilder = prefixesBuilder.addPrefixes(constructBuilder);

            constructBuilder.addConstruct("?s", "?p", "?o");

            SelectBuilder tripleSPOConstruct = new SelectBuilder();

            tripleSPOConstruct.addWhere("?s", "?p", "?o");

            constructBuilder.addGraph("?g", tripleSPOConstruct);

            SPARQLFilter sparqlFilter = new SPARQLFilter();

            ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

            filterItems = filterBuilder.addItems(filterItems, "?s", "<" + allClassesFromTDB.get(i) + ">");

            ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

            constructBuilder = filterBuilder.addFilter(constructBuilder, filter);

            filterItems.clear();

            String sparqlQueryString = constructBuilder.buildString();

            Model currClassModel = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

            defaultComposition.add(currClassModel);


            // find axiom for the corresponding class
            FilterBuilder filterAxiomBuilder = new FilterBuilder();

            PrefixesBuilder prefixesAxiomBuilder = new PrefixesBuilder();

            ConstructBuilder constructAxiomBuilder = new ConstructBuilder();

            constructAxiomBuilder = prefixesAxiomBuilder.addPrefixes(constructAxiomBuilder);

            constructAxiomBuilder.addConstruct("?s", "?p", "?o");

            SelectBuilder tripleAxiomSPOConstruct = new SelectBuilder();

            tripleAxiomSPOConstruct.addWhere("?s", "?p", "?o");
            tripleAxiomSPOConstruct.addWhere("?s", "?p1", "?o1");

            constructAxiomBuilder.addGraph("?g", tripleAxiomSPOConstruct);

            SPARQLFilter sparqlAxiomFilter = new SPARQLFilter();

            ArrayList<ArrayList<String>> filterAxiomItems = new ArrayList<>();

            filterAxiomItems = filterAxiomBuilder.addItems(filterAxiomItems, "?p1", "<http://www.w3.org/2002/07/owl#annotatedSource>");

            filterAxiomItems = filterAxiomBuilder.addItems(filterAxiomItems, "?o1", "<" + allClassesFromTDB.get(i) + ">");

            ArrayList<String> axiomFilter = sparqlAxiomFilter.getINFilter(filterAxiomItems);

            constructAxiomBuilder = filterAxiomBuilder.addFilter(constructAxiomBuilder, axiomFilter);

            filterAxiomItems.clear();

            sparqlQueryString = constructAxiomBuilder.buildString();

            currClassModel = connectionToTDB.pullDataFromTDB(this.pathToOntologies, sparqlQueryString);

            defaultComposition.add(currClassModel);

        }

        return defaultComposition;


    }


    /**
     * This method calculate the named graph for a statement
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the uri of a named graph
     */
    private String calculateNG(JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        // handle special case create overlay
        if (this.hasCreateOverlayInput) {

           return this.createOverlayNG;

        }

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077")) {
                // load from/save to/update in named graph

                String localNamePropertyInObject = currExecStep.getJSONObject(i).getString("object");

                if (localNamePropertyInObject.contains("__MDB_UIAP_")) {

                    localNamePropertyInObject = localNamePropertyInObject.substring(localNamePropertyInObject.indexOf("__") + 2);

                    Iterator<String> keyIterator = this.generatedResources.keys();

                    while (keyIterator.hasNext()) {

                        String currKey = keyIterator.next();

                        // get local name of a key
                        String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                        if (localNameOfKey.equals(localNamePropertyInObject)) {
                            // get ng from generated resources

                            if (this.mdbCoreIDNotEmpty
                                    && this.mdbUEIDNotEmpty
                                    && !this.mdbEntryIDNotEmpty) {


                            } else if (this.mdbEntryIDNotEmpty
                                    && this.mdbUEIDNotEmpty) {

                                return this.generatedResources.getString(currKey);


                            } else if (this.mdbUEIDNotEmpty) {

                                return this.generatedResources.getString(currKey);

                            }

                        }

                    }

                } else {
                    // return object from input

                    return currExecStep.getJSONObject(i).getString("object");

                }

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000392")) {
                // load from/save to/update in named graph (individual of)


                if (this.mdbCoreIDNotEmpty
                        && this.mdbUEIDNotEmpty
                        && !this.mdbEntryIDNotEmpty) {


                } else if (this.mdbEntryIDNotEmpty
                        && this.mdbUEIDNotEmpty) {

                    for (int j = 0; j < currExecStep.length(); j++) {

                        if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000134")) {
                            // named graph belongs to MDB ID

                            IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                            String workspace = calculateWorkspaceDirectory(currExecStep);

                            return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                        }

                    }

                    IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                    String workspace = calculateWorkspaceDirectory(currExecStep);

                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                } else if (this.mdbUEIDNotEmpty) {

                }



            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                // load from/save to/update in named graph (this entry's specific individual of)


                if (this.mdbCoreIDNotEmpty
                        && this.mdbUEIDNotEmpty
                        && !this.mdbEntryIDNotEmpty) {


                } else if (this.mdbEntryIDNotEmpty
                        && this.mdbUEIDNotEmpty) {

                    IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                    String workspace = calculateWorkspaceDirectory(currExecStep);

                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);


                } else if (this.mdbUEIDNotEmpty) {

                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                    String workspace = calculateWorkspaceDirectory(currExecStep);

                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                }

            }

        }

        return null;


    }


    /**
     * This method calculate the property for a statement
     * @param currExecStep contains all information from the ontology for the current execution step
     * @return the specific uri of a property
     */
    private String calculateProperty(JSONArray currExecStep) {

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000041")) {
                // property

                return currExecStep.getJSONObject(i).getString("object");

            }

        }

        return "Error: Can't calculate property.";

    }


    /**
     * This method calculate the object for a property
     * @param dataToFindObjectInTDB contains information to find a potential object in a jena tdb
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param objectType contains "a", "l" or "r"
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the specific value of the object resource
     */
    private String calculateObject(JSONObject dataToFindObjectInTDB, JSONArray currExecStep, JSONObject currComponentObject, JSONObject jsonInputObject, String objectType, JenaIOTDBFactory connectionToTDB) {

        switch (objectType) {

            case "a" :

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000040")) {
                        // object

                        String object = currExecStep.getJSONObject(i).getString("object");

                        if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000116")) {
                            // KEYWORD: this MDB user ID

                            // the MDB user ID is the combination of the mdbUEID and the local identifier MDB_AGENT_0000000009_1
                            return this.mdbUEID + "#MDB_AGENT_0000000009_1";

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000161")) {
                            // KEYWORD: this MDB core ID

                            return this.mdbCoreID;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000220")) {
                            // KEYWORD: ?

                            SelectBuilder selectBuilder = new SelectBuilder();

                            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                            SelectBuilder tripleSPO = new SelectBuilder();

                            String subject = "?s";

                            if (dataToFindObjectInTDB.has("subject")) {

                                subject = "<" + dataToFindObjectInTDB.getString("subject") + ">";

                            }

                            String property = "?p";

                            if (dataToFindObjectInTDB.has("property")) {

                                property = "<" + dataToFindObjectInTDB.getString("property") + ">";

                            }

                            tripleSPO.addWhere(subject, property, "?o");

                            selectBuilder.addVar(selectBuilder.makeVar("?o"));

                            String ng = "?g";

                            if (dataToFindObjectInTDB.has("ng")) {

                                ng = "<" + dataToFindObjectInTDB.getString("ng") + ">";

                            }

                            selectBuilder.addGraph(ng, tripleSPO);

                            String sparqlQueryString = selectBuilder.buildString();

                            return connectionToTDB.pullSingleDataFromTDB(dataToFindObjectInTDB.getString("directory"), sparqlQueryString, "?o");

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                            // KEYWORD: this MDB user entry ID

                            return this.mdbUEID;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000410")) {
                            // KEYWORD: MDB entry currently in focus

                            return this.currentFocus;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411")) {
                            // KEYWORD: known resource A

                            boolean hasConstraint = false;
                            String constraint = "";

                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                                    // has constraint

                                    constraint = currExecStep.getJSONObject(j).getString("object");
                                    hasConstraint = true;

                                }

                            }

                            if (hasConstraint) {

                                if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                                    // KEYWORD: this 'cookie'

                                    return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411");

                                }

                            }

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412")) {
                            // KEYWORD: known resource B

                            boolean hasConstraint = false;
                            String constraint = "";

                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                                    // has constraint

                                    constraint = currExecStep.getJSONObject(j).getString("object");
                                    hasConstraint = true;

                                }

                            }

                            if (hasConstraint) {

                                if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                                    // KEYWORD: this 'cookie'

                                    return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412");

                                }

                            }

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000306")) {
                            // KEYWORD: entry type icon corresponding to 'input of type [info-input_1]'
                            // todo recalculate this part when the icon has an uri

                            return object;

                        } else {

                            // check identified resources
                            Iterator<String> identifiedResIterator = this.identifiedResources.keys();

                            while (identifiedResIterator.hasNext()) {

                                String currKey = identifiedResIterator.next();

                                if (currKey.equals(object)) {
                                    // get already identified resource from cache

                                    return this.identifiedResources.getString(currKey);

                                }

                            }

                            // check info input
                            Iterator<String> infoInputKeys = this.infoInput.keys();

                            while (infoInputKeys.hasNext()) {

                                String currKey = infoInputKeys.next();

                                if (currKey.equals(object)) {

                                    return this.infoInput.getString(currKey);

                                }

                            }

                            if (object.contains("__MDB_UIAP_")) {

                                String localNamePropertyInObject = object.substring(object.indexOf("__") + 2);

                                Iterator<String> genResIterator = this.generatedResources.keys();

                                while (genResIterator.hasNext()) {

                                    String currKey = genResIterator.next();

                                    // get local name of a key
                                    String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                                    if (localNameOfKey.equals(localNamePropertyInObject)) {
                                        // get already generated resource from cache

                                        return this.generatedResources.getString(currKey);

                                    }

                                }

                            }

                            if (jsonInputObject.has("localIDs")) {

                                JSONArray currJSONArray = jsonInputObject.getJSONArray("localIDs");

                                for (int j = 0; j < currJSONArray.length(); j++) {

                                    JSONObject currJSONObject = currJSONArray.getJSONObject(j);

                                    if (currJSONObject.has("keyword")) {

                                        if (ResourceFactory.createResource(object).getLocalName().equals(currJSONObject.getString("keyword")) &&
                                                jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                            if (EmailValidator.getInstance().isValid(currJSONObject.getString("value"))) {

                                                return "mailto:" + currJSONObject.getString("value");

                                            } else {

                                                return currJSONObject.getString("value");

                                            }

                                        }  else if (currJSONObject.has("keywordLabel")) {

                                            if (ResourceFactory.createResource(object).getLocalName().equals(currJSONObject.getString("keywordLabel")) &&
                                                    jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                                if (EmailValidator.getInstance().isValid(currJSONObject.getString("valueLabel"))) {

                                                    return "mailto:" + currJSONObject.getString("valueLabel");

                                                } else {

                                                    return currJSONObject.getString("valueLabel");

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                            return object;

                        }

                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000228")) {
                        // object (copied individual of)

                        if (this.mdbCoreIDNotEmpty
                                && this.mdbUEIDNotEmpty
                                && !this.mdbEntryIDNotEmpty) {


                        } else if (this.mdbEntryIDNotEmpty
                                && this.mdbUEIDNotEmpty) {

                            // check if object already exist in another workspace
                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000408")) {
                                    // object belongs to MDB ID

                                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                    String workspace = calculateWorkspaceDirectory(currExecStep);

                                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                                }

                            }

                            JSONArray objectsInJSONArray = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");
                            // todo maybe advance this calculation for multiple instances of one class

                            // check if object already was generated in execution step 'copy and save triple statement(s)'
                            for (int j = 0; j < objectsInJSONArray.length(); j++) {

                                if (objectsInJSONArray.getString(j).equals(currExecStep.getJSONObject(i).getString("object"))) {

                                    if (currComponentObject.getJSONObject("input_data").getJSONArray("property").getString(j).equals(RDF.type.toString())) {

                                        return currComponentObject.getJSONObject("input_data").getJSONArray("subject").getString(j);

                                    }

                                }

                            }

                        } else if (this.mdbUEIDNotEmpty) {

                            JSONArray objectsInJSONArray = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");

                            for (int j = 0; j < objectsInJSONArray.length(); j++) {

                                if (objectsInJSONArray.getString(j).equals(currExecStep.getJSONObject(i).getString("object"))) {

                                    if (currComponentObject.getJSONObject("input_data").getJSONArray("property").getString(j).equals(RDF.type.toString())) {

                                        return currComponentObject.getJSONObject("input_data").getJSONArray("subject").getString(j);

                                    }

                                }

                            }

                        }


                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000231")) {
                        // object (this entry's specific individual of)

                        for (int j = 0; j < currExecStep.length(); j++) {

                            if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000407")) {
                                // set new focus on MDB entry ID for this execution step

                                if (currExecStep.getJSONObject(j).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                                    // KEYWORD: this MDB user entry ID

                                    // input contains for example this.mdbUEID
                                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                    String workspace = calculateWorkspaceDirectory(currExecStep);

                                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                                }

                            }

                        }

                        IndividualURI individualURI = new IndividualURI(this.currentFocus);

                        String workspace = calculateWorkspaceDirectory(currExecStep);

                        return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);


                    }

                }

                break;

            case "l" :

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000040")) {
                        // object

                        String object = currExecStep.getJSONObject(i).getString("object");

                        if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000220")) {
                            // KEYWORD: ?

                            SelectBuilder selectBuilder = new SelectBuilder();

                            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                            SelectBuilder tripleSPO = new SelectBuilder();

                            String subject = "?s";

                            if (dataToFindObjectInTDB.has("subject")) {

                                subject = "<" + dataToFindObjectInTDB.getString("subject") + ">";

                            }

                            String property = "?p";

                            if (dataToFindObjectInTDB.has("property")) {

                                property = "<" + dataToFindObjectInTDB.getString("property") + ">";

                            }

                            tripleSPO.addWhere(subject, property, "?o");

                            selectBuilder.addVar(selectBuilder.makeVar("?o"));

                            String ng = "?g";

                            if (dataToFindObjectInTDB.has("ng")) {

                                ng = "<" + dataToFindObjectInTDB.getString("ng") + ">";

                            }

                            selectBuilder.addGraph(ng, tripleSPO);

                            String sparqlQueryString = selectBuilder.buildString();

                            return connectionToTDB.pullSingleDataFromTDB(dataToFindObjectInTDB.getString("directory"), sparqlQueryString, "?o");

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000116")) {
                            // KEYWORD: this MDB user ID

                            // the MDB user ID is the combination of the mdbUEID and the local identifier MDB_AGENT_0000000009_1
                            return this.mdbUEID + "#MDB_AGENT_0000000009_1";

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000161")) {
                            // KEYWORD: this MDB core ID

                            return this.mdbCoreID;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                            // KEYWORD: this MDB user entry ID

                            return this.mdbUEID;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000111")) {
                            // GUI_COMPONENT_INPUT_TYPE: date time stamp

                            return this.mdbDate.getDate();

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000310")) {
                            // KEYWORD: name of this MDB user ID

                            TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                            String firstName = getLiteralFromStore(this.mdbUEID, (FOAF.firstName).toString(), tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), connectionToTDB);

                            String lastName = getLiteralFromStore(this.mdbUEID, (FOAFAdvanced.lastName).toString(), tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), connectionToTDB);

                            return firstName + " " + lastName;

                        } else if ( object.equals("true") ||
                                object.equals("false")) {

                            return object;

                        } else {
                            // check identified resources

                            //System.out.println("object = " + currExecStep.getJSONObject(i).getString("object"));

                            Iterator<String> identifiedResIterator = this.identifiedResources.keys();

                            while (identifiedResIterator.hasNext()) {

                                String currKey = identifiedResIterator.next();

                                if (currKey.equals(object)) {
                                    // get already identified resource from cache

                                    return this.identifiedResources.getString(currKey);

                                }

                            }

                            // check info input
                            Iterator<String> infoInputKeys = this.infoInput.keys();

                            while (infoInputKeys.hasNext()) {

                                String currKey = infoInputKeys.next();

                                if (currKey.equals(object)) {

                                    return this.infoInput.getString(currKey);

                                }

                            }

                            if (object.contains("__MDB_UIAP_")) {

                                String localNamePropertyInObject = object.substring(object.indexOf("__") + 2);

                                Iterator<String> genResIterator = this.generatedResources.keys();

                                while (genResIterator.hasNext()) {

                                    String currKey = genResIterator.next();

                                    // get local name of a key
                                    String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                                    if (localNameOfKey.equals(localNamePropertyInObject)) {
                                        // get already generated resource from cache

                                        return this.generatedResources.getString(currKey);

                                    }

                                }

                                if (jsonInputObject.has("localIDs")) {

                                    JSONArray currJSONArray = jsonInputObject.getJSONArray("localIDs");

                                    for (int j = 0; j < currJSONArray.length(); j++) {

                                        JSONObject currJSONObject = currJSONArray.getJSONObject(j);

                                        if (currJSONObject.has("keyword")) {

                                            if (ResourceFactory.createResource(object).getLocalName().equals(currJSONObject.getString("keyword")) &&
                                                    jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                                return currJSONObject.getString("value");

                                            } else if (currJSONObject.has("keywordLabel")) {

                                                if (ResourceFactory.createResource(object).getLocalName().equals(currJSONObject.getString("keywordLabel")) &&
                                                        jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                                    return currJSONObject.getString("valueLabel");

                                                }

                                            }

                                        }

                                    }

                                }

                            } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000410")) {
                                // KEYWORD: MDB entry currently in focus

                                return this.currentFocus;

                            } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411")) {
                                // KEYWORD: known resource A

                                boolean hasConstraint = false;
                                String constraint = "";

                                for (int j = 0; j < currExecStep.length(); j++) {

                                    if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                                        // has constraint

                                        constraint = currExecStep.getJSONObject(j).getString("object");
                                        hasConstraint = true;

                                    }

                                }

                                if (hasConstraint) {

                                    if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                                        // KEYWORD: this 'cookie'

                                        return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411");

                                    }

                                }

                            } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412")) {
                                // KEYWORD: known resource B

                                boolean hasConstraint = false;
                                String constraint = "";

                                for (int j = 0; j < currExecStep.length(); j++) {

                                    if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                                        // has constraint

                                        constraint = currExecStep.getJSONObject(j).getString("object");
                                        hasConstraint = true;

                                    }

                                }

                                if (hasConstraint) {

                                    if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                                        // KEYWORD: this 'cookie'

                                        return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412");

                                    }

                                }

                            }

                            return object;

                        }

                    }

                }

                break;

            case "r" :

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property")
                            .equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000040")) {
                        // object

                        String object = currExecStep.getJSONObject(i).getString("object");

                        if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000220")) {
                            // KEYWORD: ?

                            SelectBuilder selectBuilder = new SelectBuilder();

                            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                            SelectBuilder tripleSPO = new SelectBuilder();

                            String subject = "?s";

                            if (dataToFindObjectInTDB.has("subject")) {

                                subject = "<" + dataToFindObjectInTDB.getString("subject") + ">";

                            }

                            String property = "?p";

                            if (dataToFindObjectInTDB.has("property")) {

                                property = "<" + dataToFindObjectInTDB.getString("property") + ">";

                            }

                            tripleSPO.addWhere(subject, property, "?o");

                            selectBuilder.addVar(selectBuilder.makeVar("?o"));

                            String ng = "?g";

                            if (dataToFindObjectInTDB.has("ng")) {

                                ng = "<" + dataToFindObjectInTDB.getString("ng") + ">";

                            }

                            selectBuilder.addGraph(ng, tripleSPO);

                            String sparqlQueryString = selectBuilder.buildString();

                            String queryResult = connectionToTDB.pullSingleDataFromTDB(dataToFindObjectInTDB.getString("directory"), sparqlQueryString, "?o");

                            // return 'KEYWORD: empty' - URI (if result is empty) or the URI from the jena tdb
                            return queryResult.equals("") ?  "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423" : queryResult;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000116")) {
                            // KEYWORD: this MDB user ID

                            // the MDB user ID is the combination of the mdbUEID and the local identifier MDB_AGENT_0000000009_1
                            return this.mdbUEID + "#MDB_AGENT_0000000009_1";

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000161")) {
                            // KEYWORD: this MDB core ID

                            return this.mdbCoreID;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                            // KEYWORD: this MDB user entry ID

                            return this.mdbUEID;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000155")) {
                            // KEYWORD: this MDB entry component

                            if (jsonInputObject.has("precedingKeywords")) {

                                return jsonInputObject.getString("mdbentryid") + "#" + jsonInputObject.getString("localID");

                            } else {

                                JSONObject jsonFromMongoDB = this.mongoDBConnection.pullDataFromMongoDB(jsonInputObject);

                                return jsonFromMongoDB.getString("individualID");

                            }

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000410")) {
                            // KEYWORD: MDB entry currently in focus

                            return this.currentFocus;

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411")) {
                            // KEYWORD: known resource A

                            boolean hasConstraint = false;
                            String constraint = "";

                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                                    // has constraint

                                    constraint = currExecStep.getJSONObject(j).getString("object");
                                    hasConstraint = true;

                                }

                            }

                            if (hasConstraint) {

                                if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                                    // KEYWORD: this 'cookie'

                                    return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411");

                                }

                            }

                        } else if (object.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412")) {
                            // KEYWORD: known resource B

                            boolean hasConstraint = false;
                            String constraint = "";

                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                                    // has constraint

                                    constraint = currExecStep.getJSONObject(j).getString("object");
                                    hasConstraint = true;

                                }

                            }

                            if (hasConstraint) {

                                if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                                    // KEYWORD: this 'cookie'

                                    return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412");

                                }

                            }

                        } else if (object.contains("__MDB_UIAP_")) {

                            String localNamePropertyInObject = object.substring(object.indexOf("__") + 2);

                            Iterator<String> genResIterator = this.generatedResources.keys();

                            while (genResIterator.hasNext()) {

                                String currKey = genResIterator.next();

                                // get local name of a key
                                String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                                if (localNameOfKey.equals(localNamePropertyInObject)) {
                                    // get already generated resource from cache

                                    return this.generatedResources.getString(currKey);

                                }

                            }

                            if (jsonInputObject.has("localIDs")) {

                                JSONArray currJSONArray = jsonInputObject.getJSONArray("localIDs");

                                for (int j = 0; j < currJSONArray.length(); j++) {

                                    JSONObject currJSONObject = currJSONArray.getJSONObject(j);

                                    if (currJSONObject.has("keyword")) {

                                        if (ResourceFactory.createResource(object).getLocalName().equals(currJSONObject.getString("keyword")) &&
                                                jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                            if (EmailValidator.getInstance().isValid(currJSONObject.getString("value"))) {

                                                return "mailto:" + currJSONObject.getString("value");

                                            } else {

                                                return currJSONObject.getString("value");

                                            }

                                        } else if (currJSONObject.has("keywordLabel")) {

                                            if (ResourceFactory.createResource(object).getLocalName().equals(currJSONObject.getString("keywordLabel")) &&
                                                    jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                                if (EmailValidator.getInstance().isValid(currJSONObject.getString("valueLabel"))) {

                                                    return "mailto:" + currJSONObject.getString("valueLabel");

                                                } else {

                                                    return currJSONObject.getString("valueLabel");

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                        }

                        // check identified resources
                        Iterator<String> identifiedResIterator = this.identifiedResources.keys();

                        while (identifiedResIterator.hasNext()) {

                            String currKey = identifiedResIterator.next();

                            if (currKey.equals(object)) {
                                // get already identified resource from cache

                                if (EmailValidator.getInstance().isValid(this.identifiedResources.getString(currKey))) {
                                    // convert mail to a complete uri

                                    return "mailto:" + this.identifiedResources.getString(currKey);

                                }

                                return this.identifiedResources.getString(currKey);

                            }

                        }

                        // check info input
                        Iterator<String> infoInputKeys = this.infoInput.keys();

                        while (infoInputKeys.hasNext()) {

                            String currKey = infoInputKeys.next();

                            if (currKey.equals(object)) {

                                return this.infoInput.getString(currKey);

                            }

                        }

                        return object;

                    } else if (currExecStep.getJSONObject(i).getString("property")
                            .equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000228")) {
                        // object (copied individual of)

                        String focusForNewIndividualOfClass = "";

                        if (this.mdbCoreIDNotEmpty
                                && this.mdbUEIDNotEmpty
                                && !this.mdbEntryIDNotEmpty) {


                        } else if (this.mdbEntryIDNotEmpty
                                && this.mdbUEIDNotEmpty) {

                            // check if object already exist in another workspace
                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000408")) {
                                    // object belongs to MDB ID

                                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                    String workspace = calculateWorkspaceDirectory(currExecStep);

                                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                                }

                            }

                            JSONArray objectsInJSONArray = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");

                            // check if object already was generated in execution step 'copy and save triple statement(s)'
                            for (int j = 0; j < objectsInJSONArray.length(); j++) {

                                if (objectsInJSONArray.getString(j).equals(currExecStep.getJSONObject(i).getString("object"))) {

                                    if (currComponentObject.getJSONObject("input_data").getJSONArray("property").getString(j).equals(RDF.type.toString())) {

                                        return currComponentObject.getJSONObject("input_data").getJSONArray("subject").getString(j);

                                    }

                                }

                            }

                            focusForNewIndividualOfClass = this.mdbEntryID;

                        } else if (this.mdbUEIDNotEmpty) {

                            JSONArray objectsInJSONArray = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");

                            for (int j = 0; j < objectsInJSONArray.length(); j++) {

                                if (objectsInJSONArray.getString(j).equals(currExecStep.getJSONObject(i).getString("object"))) {

                                    if (currComponentObject.getJSONObject("input_data").getJSONArray("property").getString(j).equals(RDF.type.toString())) {

                                        return currComponentObject.getJSONObject("input_data").getJSONArray("subject").getString(j);

                                    }

                                }

                            }

                            focusForNewIndividualOfClass = this.mdbUEID;

                        }

                        IndividualURI individualURI = new IndividualURI(focusForNewIndividualOfClass);

                        String workspace = calculateWorkspaceDirectory(currExecStep);

                        String potentialObject = individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                    } else if (currExecStep.getJSONObject(i).getString("property")
                            .equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000231")) {
                        // object (this entry's specific individual of)

                        for (int j = 0; j < currExecStep.length(); j++) {

                            if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000407")) {
                                // set new focus on MDB entry ID for this execution step

                                if (currExecStep.getJSONObject(j).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                                    // KEYWORD: this MDB user entry ID

                                    // input contains for example this.mdbUEID
                                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                    String workspace = calculateWorkspaceDirectory(currExecStep);

                                    return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                                }

                            }

                        }

                        IndividualURI individualURI = new IndividualURI(this.currentFocus);

                        String workspace = calculateWorkspaceDirectory(currExecStep);

                        return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                    }

                }

                break;

        }

        System.out.println();
        System.out.println();
        System.out.println("Error: jsonInputObject = " + jsonInputObject);
        System.out.println();
        System.out.println();
        System.out.println("Error: currExecStep = " + currExecStep);
        return "Error: Can't calculate object.";

    }


    /**
     * This method calculate the object type for a property
     * @param property contains the input for the calculation of the type
     * @return "a", "l" or "r" according to the input property
     */
    private String calculateObjectType(String property) {

        boolean objectPropertyCheck = this.objectPropertySet.objectPropertyExist(this.objectPropertiesInJSON, property);

        if (objectPropertyCheck ||
                property.equals((RDF.type).toString()) ||
                property.equals((RDFS.subClassOf).toString())) {
            // object is a literal

            return "r";

        } else {

            boolean dataPropertyCheck = this.dataPropertySet.dataPropertyExist(this.dataPropertiesInJSON, property);

            if (dataPropertyCheck) {

                return "l";

            } else {

                boolean annotationPropertyCheck = this.annotationPropertySet.annotationPropertyExist(this.annotationPropertiesInJSON, property);

                if (annotationPropertyCheck ||
                        property.equals((RDFS.label).toString()) ||
                        property.equals((OWL2.annotatedProperty).toString()) ||
                        property.equals((OWL2.annotatedSource).toString()) ||
                        property.equals((OWL2.annotatedTarget).toString()) ||
                        property.equals((OWL2.equivalentClass).toString())) {

                    return "a";

                }
            }
        }

        return "Error: Can't calculate property type.";

    }


    /**
     * This method calculate the object type for an annotation property
     * @param object contains an object value
     * @param objectType contains "a", "l" or "r"
     * @return "l" or "r" according to the input object
     */
    private String calculateObjectTypeForAnnotationProperty(String object, String objectType) {

        if (objectType.equals("a") && UrlValidator.getInstance().isValid(object)) {

            return  "r";

        } else if (objectType.equals("a")) {

            return  "l";

        } else if (!objectType.equals("a")) {

            return objectType;

        }

        return "Error: Can't calculate object type from annotation Property";

    }


    /**
     * This method calculate the operation for a statement
     * @param currExecStep contains all information from the ontology for the current execution step
     * @return "s" for save or "d" for delete
     */
    private String calculateOperation(JSONArray currExecStep) {

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000135")) {
                // delete triple statement (BOOLEAN)

                if (currExecStep.getJSONObject(i).getString("object").equals("true")) {

                    return "d";

                } else {

                    return "s";

                }

            }

        }

        return "s";

    }


    /**
     * This method calculate the subject for a statement
     * @param dataToFindObjectInTDB contains information to find a potential object in a jena tdb
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the specific uri of a subject
     */
    private String calculateSubject(JSONObject dataToFindObjectInTDB, JSONArray currExecStep, JSONObject currComponentObject, JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000042")) {
                // subject

                String subject = currExecStep.getJSONObject(i).getString("object");

                if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000116")) {
                    // KEYWORD: this MDB user ID

                    // the MDB user ID is the combination of the mdbUEID and the local identifier MDB_AGENT_0000000009_1
                    return this.mdbUEID + "#MDB_AGENT_0000000009_1";

                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000161")) {
                    // KEYWORD: this MDB core ID

                    return this.mdbCoreID;

                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000220")) {
                    // KEYWORD: ?

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder tripleSPO = new SelectBuilder();

                    String property = "?p";

                    if (dataToFindObjectInTDB.has("property")) {

                        property = "<" + dataToFindObjectInTDB.getString("property") + ">";

                    }

                    String object = "?o";

                    if (dataToFindObjectInTDB.has("object")) {

                        object = "<" + dataToFindObjectInTDB.getString("object") + ">";

                    }

                    tripleSPO.addWhere("?s", property, object);

                    selectBuilder.addVar(selectBuilder.makeVar("?s"));

                    String ng = "?g";

                    if (dataToFindObjectInTDB.has("ng")) {

                        ng = "<" + dataToFindObjectInTDB.getString("ng") + ">";

                    }

                    selectBuilder.addGraph(ng, tripleSPO);

                    String sparqlQueryString = selectBuilder.buildString();

                    String queryResult = connectionToTDB.pullSingleDataFromTDB(dataToFindObjectInTDB.getString("directory"), sparqlQueryString, "?s");

                    // return 'KEYWORD: empty' - URI (if result is empty) or the URI from the jena tdb
                    return queryResult.equals("") ? "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423" : queryResult;

                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000155")) {
                    // KEYWORD: this MDB entry component

                    JSONObject jsonFromMongoDB = this.mongoDBConnection.pullDataFromMongoDB(jsonInputObject);

                    return jsonFromMongoDB.getString("individualID");


                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                    // KEYWORD: this MDB user entry ID

                    return this.mdbUEID;

                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000410")) {
                    // KEYWORD: MDB entry currently in focus

                    return this.currentFocus;

                } else if (subject.contains("__MDB_UIAP_")) {

                    String localNamePropertyInObject = subject.substring(subject.indexOf("__") + 2);

                    Iterator<String> keyIterator = this.generatedResources.keys();

                    while (keyIterator.hasNext()) {

                        String currKey = keyIterator.next();

                        // get local name of a key
                        String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                        if (localNameOfKey.equals(localNamePropertyInObject)) {
                            // get already generated resource from cache

                            return this.generatedResources.getString(currKey);

                        }

                    }

                    if (jsonInputObject.has("localIDs")) {

                        JSONArray currJSONArray = jsonInputObject.getJSONArray("localIDs");

                        for (int j = 0; j < currJSONArray.length(); j++) {

                            JSONObject currJSONObject = currJSONArray.getJSONObject(j);

                            if (currJSONObject.has("keyword")) {

                                if (ResourceFactory.createResource(subject).getLocalName().equals(currJSONObject.getString("keyword")) &&
                                        jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                    if (EmailValidator.getInstance().isValid(currJSONObject.getString("value"))) {

                                        return "mailto:" + currJSONObject.getString("value");

                                    } else {

                                        return currJSONObject.getString("value");

                                    }

                                }

                            }

                        }

                    }

                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411")) {
                    // KEYWORD: known resource A

                    boolean hasConstraint = false;
                    String constraint = "";

                    for (int j = 0; j < currExecStep.length(); j++) {

                        if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                            // has constraint

                            constraint = currExecStep.getJSONObject(j).getString("object");
                            hasConstraint = true;

                        }

                    }

                    if (hasConstraint) {

                        if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                            // KEYWORD: this 'cookie'

                            return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411");

                        }

                    }

                } else if (subject.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412")) {
                    // KEYWORD: known resource B

                    boolean hasConstraint = false;
                    String constraint = "";

                    for (int j = 0; j < currExecStep.length(); j++) {

                        if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000397")) {
                            // has constraint

                            constraint = currExecStep.getJSONObject(j).getString("object");
                            hasConstraint = true;

                        }

                    }

                    if (hasConstraint) {

                        if (constraint.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000416")) {
                            // KEYWORD: this 'cookie'

                            return jsonInputObject.getString("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412");

                        }

                    }

                } else {

                    // check identified resources
                    Iterator<String> identifiedResIterator = this.identifiedResources.keys();

                    while (identifiedResIterator.hasNext()) {

                        String currKey = identifiedResIterator.next();

                        if (currKey.equals(subject)) {
                            // get already identified resource from cache

                            return this.identifiedResources.getString(currKey);

                        }

                    }

                    // check info input
                    Iterator<String> infoInputKeys = this.infoInput.keys();

                    while (infoInputKeys.hasNext()) {

                        String currKey = infoInputKeys.next();

                        if (currKey.equals(subject)) {

                            return this.infoInput.getString(currKey);

                        }

                    }

                    return subject;

                }

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000229")) {
                // subject (copied individual of)

                if (this.mdbCoreIDNotEmpty
                        && this.mdbUEIDNotEmpty
                        && !this.mdbEntryIDNotEmpty) {


                } else if (this.mdbEntryIDNotEmpty
                        && this.mdbUEIDNotEmpty) {

                    // check if subject already exist in another workspace
                    for (int j = 0; j < currExecStep.length(); j++) {

                        if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000409")) {
                            // subject belongs to MDB ID

                            IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                            String workspace = calculateWorkspaceDirectory(currExecStep);

                            return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                        }

                    }

                    JSONArray objectsInJSONArray = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");

                    // check if subject already was generated in execution step 'copy and save triple statement(s)'
                    for (int j = 0; j < objectsInJSONArray.length(); j++) {

                        if (objectsInJSONArray.getString(j).equals(currExecStep.getJSONObject(i).getString("object"))) {

                            if (currComponentObject.getJSONObject("input_data").getJSONArray("property").getString(j).equals(RDF.type.toString())) {

                                return currComponentObject.getJSONObject("input_data").getJSONArray("subject").getString(j);

                            }

                        }

                    }

                } else if (this.mdbUEIDNotEmpty) {

                    JSONArray objectsInJSONArray = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");

                    for (int j = 0; j < objectsInJSONArray.length(); j++) {

                        if (objectsInJSONArray.getString(j).equals(currExecStep.getJSONObject(i).getString("object"))) {

                            if (currComponentObject.getJSONObject("input_data").getJSONArray("property").getString(j).equals(RDF.type.toString())) {

                                return currComponentObject.getJSONObject("input_data").getJSONArray("subject").getString(j);

                            }

                        }

                    }

                }

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000230")) {
                // subject (this entry's specific individual of)

                for (int j = 0; j < currExecStep.length(); j++) {

                    if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000407")) {
                        // set new focus on MDB entry ID for this execution step

                        if (currExecStep.getJSONObject(j).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                            // KEYWORD: this MDB user entry ID

                            // input contains for example this.mdbUEID
                            IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                            String workspace = calculateWorkspaceDirectory(currExecStep);

                            return individualURI.getThisURIForAnIndividual(currExecStep.getJSONObject(i).getString("object"), workspace, connectionToTDB);

                        }

                    }

                }

                if (this.mdbCoreIDNotEmpty
                        && this.mdbUEIDNotEmpty
                        && !(this.mdbEntryIDNotEmpty)) {


                } else if (this.mdbEntryIDNotEmpty
                        && this.mdbUEIDNotEmpty) {

                    FilterBuilder filterBuilder = new FilterBuilder();

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder tripleSPO = new SelectBuilder();

                    tripleSPO.addWhere("?s", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<" + currExecStep.getJSONObject(i).getString("object") + ">");

                    selectBuilder.addVar(selectBuilder.makeVar("?s"));

                    selectBuilder.addGraph("?g", tripleSPO);

                    SPARQLFilter sparqlFilter = new SPARQLFilter();

                    ArrayList<String> filterItems = new ArrayList<>();

                    filterItems.add(this.mdbEntryID);

                    ArrayList<String> filter = sparqlFilter.getRegexSTRFilter("?s", filterItems);

                    selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                    String sparqlQueryString = selectBuilder.buildString();

                    TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                    String potentialSubject = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"), sparqlQueryString, "?s");
                    // MDB_WORKSPACE_DIRECTORY: MDB draft workspace directory

                    if (potentialSubject.isEmpty()) {

                        return connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494"), sparqlQueryString, "?s");
                        // MDB_WORKSPACE_DIRECTORY: MDB core workspace directory

                    } else {

                        return potentialSubject;

                    }

                } else if (this.mdbUEIDNotEmpty) {

                    FilterBuilder filterBuilder = new FilterBuilder();

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder tripleSPO = new SelectBuilder();

                    tripleSPO.addWhere("?s", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<" + currExecStep.getJSONObject(i).getString("object") + ">");

                    selectBuilder.addVar(selectBuilder.makeVar("?s"));

                    selectBuilder.addGraph("?g", tripleSPO);

                    SPARQLFilter sparqlFilter = new SPARQLFilter();

                    ArrayList<String> filterItems = new ArrayList<>();

                    filterItems.add(jsonInputObject.getString("mdbueid"));

                    ArrayList<String> filter = sparqlFilter.getRegexSTRFilter("?s", filterItems);

                    selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                    String sparqlQueryString = selectBuilder.buildString();

                    TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                    return connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), sparqlQueryString, "?s");
                    // MDB_WORKSPACE_DIRECTORY: MDB admin workspace directory

                }

            }

        }

        return "Error: Can't calculate subject.";

    }


    /**
     * This method calculate the corresponding workspace directory for an execution step
     * @param currExecStep contains all information from the ontology for the current execution step
     * @return the path to the workspace directory
     */
    private String calculateWorkspaceDirectory(JSONArray currExecStep) {

        for (int i = 0; i < currExecStep.length();i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000078")) {
                // named graph belongs to workspace

                TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                return tdbPath.getPathToTDB(currExecStep.getJSONObject(i).getString("object"));

            }

        }

        System.out.println("can't calculate directory = " + currExecStep);

        return "Error: Can't calculate directory.";

    }


    /**
     * This method gets the corresponding workspace directory for a default composition
     * @param currExecStep contains all information from the ontology for the current execution step
     * @return the path to the workspace directory
     */
    private String calculateWorkspaceDirectoryForDefaultComposition(JSONArray currExecStep) {

        for (int i = 0; i < currExecStep.length();i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000107")) {
                // copy from workspace

                TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                return tdbPath.getPathToTDB(currExecStep.getJSONObject(i).getString("object"));

            }

        }

        return "Error: Can't find directory.";

    }


    /**
     * This method convert the input multiple ArrayList to a JSONObject, which can use as input for the
     * MDBJSONObjectFactory
     * @param generatedCoreIDData contains multiple data
     * @return a JSON Object, which can use as Input for the MDBJSONObjectFactory
     */
    public JSONObject convertArrayListToJSONObject (ArrayList<ArrayList<String>> generatedCoreIDData) {

        ListIterator<ArrayList<String>> generatedCoreIDDataLI = generatedCoreIDData.listIterator();

        JSONObject datasetsJSONObject = new JSONObject();

        JSONObject ngsJSONObject = new JSONObject();

        ArrayList<String> datasetsArrayList = new ArrayList<>();

        ArrayList<String> ngsArrayList = new ArrayList<>();

        // transform the generated data to json format
        while (generatedCoreIDDataLI.hasNext()) {

            JSONObject datasetJSONObject = new JSONObject();
            JSONObject ngJSONObject = new JSONObject();
            JSONObject triplesAndOperationJSONObject = new JSONObject();
            JSONObject objectJSONObject = new JSONObject();

            ArrayList<String> currGeneratedCoreIDData = generatedCoreIDDataLI.next();

            String currDataset = currGeneratedCoreIDData.get(6);
            String currNG = currGeneratedCoreIDData.get(5);

            if (!datasetsArrayList.contains(currDataset)) {

                datasetsArrayList.add(currDataset);

                datasetJSONObject.put("dataset", currDataset);


                if (!ngsArrayList.contains(currNG)) {

                    ngsArrayList.add(currNG);

                    triplesAndOperationJSONObject.put("subject", currGeneratedCoreIDData.get(0));
                    triplesAndOperationJSONObject.put("property", currGeneratedCoreIDData.get(1));

                    objectJSONObject.put("object_data", currGeneratedCoreIDData.get(2));
                    objectJSONObject.put("object_type", currGeneratedCoreIDData.get(3));

                    triplesAndOperationJSONObject.put("object", objectJSONObject);
                    triplesAndOperationJSONObject.put("operation", currGeneratedCoreIDData.get(4));

                    ngJSONObject.put("ng", currNG);

                    ngJSONObject.append("triples", triplesAndOperationJSONObject);

                    ngsJSONObject.append("ngs", ngJSONObject);

                }

                datasetJSONObject.append("ngs", ngJSONObject);

                datasetsJSONObject.append("datasets", datasetJSONObject);


            } else if (datasetsArrayList.contains(currDataset)) {

                JSONArray datasetsJSONArray= datasetsJSONObject.getJSONArray("datasets");

                for (int i = 0; i < datasetsJSONArray.length(); i++) {

                    if (datasetsJSONArray.getJSONObject(i).get("dataset").equals(currDataset)) {

                        if (!ngsArrayList.contains(currNG)) {

                            ngsArrayList.add(currNG);

                            ngJSONObject.put("ng", currNG);

                            triplesAndOperationJSONObject.put("subject", currGeneratedCoreIDData.get(0));
                            triplesAndOperationJSONObject.put("property", currGeneratedCoreIDData.get(1));

                            objectJSONObject.put("object_data", currGeneratedCoreIDData.get(2));
                            objectJSONObject.put("object_type", currGeneratedCoreIDData.get(3));

                            triplesAndOperationJSONObject.put("object", objectJSONObject);
                            triplesAndOperationJSONObject.put("operation", currGeneratedCoreIDData.get(4));

                            ngJSONObject.append("triples", triplesAndOperationJSONObject);

                            datasetsJSONObject.getJSONArray("datasets").getJSONObject(i).append("ngs", ngJSONObject);

                        } else {

                            JSONArray ngsJSONArray= datasetsJSONObject.getJSONArray("datasets").getJSONObject(i).getJSONArray("ngs");

                            for (int j = 0; j < ngsJSONArray.length(); j++) {

                                if (ngsJSONArray.getJSONObject(j).get("ng").equals(currNG)) {

                                    triplesAndOperationJSONObject.put("subject", currGeneratedCoreIDData.get(0));
                                    triplesAndOperationJSONObject.put("property", currGeneratedCoreIDData.get(1));

                                    objectJSONObject.put("object_data", currGeneratedCoreIDData.get(2));
                                    objectJSONObject.put("object_type", currGeneratedCoreIDData.get(3));

                                    triplesAndOperationJSONObject.put("object", objectJSONObject);
                                    triplesAndOperationJSONObject.put("operation", currGeneratedCoreIDData.get(4));

                                    datasetsJSONObject.getJSONArray("datasets").getJSONObject(i).getJSONArray("ngs").getJSONObject(j).append("triples", triplesAndOperationJSONObject);

                                }

                            }

                        }


                    }

                }
            }

        }

        return datasetsJSONObject;

    }


    /**
     * This method provides code to differ action for different execution step properties.
     * @param sortedKBJSONArray contains the sorted knowledge base order
     * @param sortedKBIndicesJSONArray contains the sorted knowledge base order indices
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the expanded current component information
     */
    public JSONObject convertKBToJSONObject(JSONArray sortedKBJSONArray, JSONArray sortedKBIndicesJSONArray,
                                            JSONObject currComponentObject, JSONObject jsonInputObject,
                                            JenaIOTDBFactory connectionToTDB) {


        for (int i = 0;  i < sortedKBJSONArray.length(); i++) {

            JSONArray currExecStep = sortedKBJSONArray.getJSONArray(i);

            String currExecStepIndex = sortedKBIndicesJSONArray.getString(i);

            String annotatedProperty = "";

            for (int j = 0; j < currExecStep.length(); j++) {

                if (currExecStep.getJSONObject(j).has("annotatedProperty")) {

                    annotatedProperty = currExecStep.getJSONObject(j).getString("annotatedProperty");

                    currExecStep.remove(j);

                }

            }

            // calculate the start date
            long executionStart = System.currentTimeMillis();

            // calculate the query time
            long queryTime;

            switch (annotatedProperty) {

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000010":
                    // execution step: save/delete triple statement(s)

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    currComponentObject = executionStepSaveDeleteTripleStatements
                            (currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    /*if (currComponentObject.has("input_data")) {

                        System.out.println("length subject: " + currComponentObject.getJSONObject("input_data").getJSONArray("subject").length());
                        System.out.println("length property: " + currComponentObject.getJSONObject("input_data").getJSONArray("property").length());
                        System.out.println("length object_data: " + currComponentObject.getJSONObject("input_data").getJSONArray("object_data").length());
                        System.out.println("length object_type: " + currComponentObject.getJSONObject("input_data").getJSONArray("object_type").length());
                        System.out.println("length ng: " + currComponentObject.getJSONObject("input_data").getJSONArray("ng").length());
                        System.out.println("length directory: " + currComponentObject.getJSONObject("input_data").getJSONArray("directory").length());
                        System.out.println("length operation: " + currComponentObject.getJSONObject("input_data").getJSONArray("operation").length());

                    }*/

                    System.out.println("query time1= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000095" :
                    // execution step: if-then-else statement

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    String nextStep = executionStepIfThenElseStatement(jsonInputObject, currExecStep, connectionToTDB);

                    if  (UrlValidator.getInstance().isValid(nextStep)) {
                        // case nextStep is a resource

                        if (nextStep.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000347")) {
                            // GUI_OPERATION: end action (case true)

                            System.out.println("Finished execution step iteration.");

                            return currComponentObject.put("valid", "true");

                        } else if (nextStep.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000407")) {
                            // GUI_OPERATION: ERROR end action (case false)

                            return currComponentObject.put("valid", "false");

                        }

                    } else {
                        // case nextStep is a literal

                        for (int j = 0; j < sortedKBIndicesJSONArray.length(); j++) {

                            if (sortedKBIndicesJSONArray.getString(j).equals(nextStep)) {
                                // get next execution step

                                i = j - 1;

                            }

                        }
                    }

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time2= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000100" :
                    // execution step: copy and save triple statement(s)

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println(currExecStep);

                    currComponentObject = executionStepCopyAndSaveTripleStatements
                            (currExecStep, currComponentObject, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time3= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000105" :
                    // execution step: update triple statement(s)

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println(currExecStep);

                    currComponentObject = executionStepUpdateTripleStatements
                            (currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                    break;


                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000175" :
                    // execution step: decision dialogue

                    for (int j = 0; j < currExecStep.length(); j++) {

                        if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000176")) {
                            // MDB dialogue-message

                            String outputKey = ResourceFactory.createResource(currExecStep.getJSONObject(j).getString("property")).getLocalName();

                            currComponentObject.put(outputKey, currExecStep.getJSONObject(j).getString("object"));

                        } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000388")) {
                            // MDB error-message

                            String outputKey = ResourceFactory.createResource(currExecStep.getJSONObject(j).getString("property")).getLocalName();

                            currComponentObject.put(outputKey, currExecStep.getJSONObject(j).getString("object"));

                        }

                    }

                    currComponentObject.put("valid", "false");

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time4= " + queryTime);

                    return currComponentObject;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000219" :
                    // execution step: trigger MDB workflow action

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();

                    currComponentObject = executionStepTriggerMDBWorkflowAction
                            (currComponentObject, jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time5= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000232":
                    // execution step: generate resources

                    System.out.println("currExecStepIndex: " + currExecStepIndex);

                    currComponentObject = executionStepGenerateResources
                            (currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time6= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000235":
                    // execution step: execute now

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    currComponentObject = executionStepExecuteNow
                            (currComponentObject, jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time7= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000279" :
                    // execution step: MDB hyperlink

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();

                    currComponentObject = executionStepMDBHyperlink(currComponentObject, jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time8= " + queryTime);

                    return currComponentObject;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000330" :
                    // execution step: close module

                    return currComponentObject;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000332" :
                    // execution step: search MDB

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();

                    executionStepSearchMDB(currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000395" :
                    // execution step: MDB operation

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    currComponentObject = executionStepMDBOperation(currComponentObject, jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time9= " + queryTime);


                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000439" :
                    // execution step: define variables

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    currComponentObject = executionStepDefineVariables(currComponentObject,jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time10= " + queryTime);


                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000484" :
                    // execution step: delete all triples of named graph

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    executionStepDeleteAllTriplesOfNamedGraph(currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time11= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000485" :
                    // execution step: extract and save MDB entry composition

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    executionStepExtractAndSaveMDBEntryComposition(currExecStep, currComponentObject, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time12= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000509" :
                    // execution step: specifications and allocations for MDB hyperlink

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    currComponentObject = executionStepCreateCompositionForMDBHyperlink(currComponentObject, jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time13= " + queryTime);

                    break;

                case "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000521" :
                    // execution step: delete multiple triple statements

                    System.out.println("currExecStepIndex: " + currExecStepIndex);
                    System.out.println();
                    //System.out.println("currExecStep: " + currExecStep);
                    //System.out.println();

                    currComponentObject = executionStepDeleteMultipleTripleStatements(currComponentObject, jsonInputObject, currExecStep, connectionToTDB);

                    // calculate the query time
                    queryTime = System.currentTimeMillis() - executionStart;

                    System.out.println("query time14= " + queryTime);

                    break;

                default:
                    //System.out.println("annotatedProperty: " + annotatedProperty + " in step: " + i);

                    //currComponentObject.put("valid", "false");

                    break;

            }

            // check if there exist the property "go to execution step" in the current execution step
            for (int j = 0; j < currExecStep.length(); j++) {

                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000239")) {
                    // go to execution step

                    for (int k = 0; k < sortedKBIndicesJSONArray.length(); k++) {

                        if (sortedKBIndicesJSONArray.getString(k).equals(currExecStep.getJSONObject(j).getString("object"))) {
                            // get next execution step

                            i = k - 1;

                        }

                    }

                } else if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000280")) {
                    // end action operation

                    i = sortedKBJSONArray.length();

                }

            }

        }

        if (!currComponentObject.has("valid")) {

            currComponentObject.put("valid", "true");

        }

        return currComponentObject;

    }
    /**
     * This method add the information for a new hyperlink of a current object
     * @param currComponentObject contains information about the current object
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return additional modified information about the current object
     */
    public JSONObject executionStepCreateCompositionForMDBHyperlink(JSONObject currComponentObject,
                                                                    JSONObject jsonInputObject, JSONArray currExecStep,
                                                                    JenaIOTDBFactory connectionToTDB) {

        System.out.println("in method executionStepCreateCompositionForMDBHyperlink");

        String directory = "", rootComponentOfComposition = "", componentOfOntology = "", compositionFromEntry = "",
                rootComponentOfUnionComposition = "", propertyForResourcesToShowExpanded = "";
        int position = -1;
        JSONArray ngs = new JSONArray(), outputDataJSON = new JSONArray(), resourcesToShowExpanded = new JSONArray();
        boolean useComponentFromComposition = false, useComponentFromOntology = false, useCompositionFromEntry = false,
                useUnionOfCompositionsWithParentRoot = false, showExpanded = false;

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077")
                    || currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                // load from/save to/update in named graph
                // load from/save to/update in named graph (this entry's specific individual of)

                ngs.put(calculateNG(currExecStep, connectionToTDB));

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000078")) {
                // named graph belongs to workspace

                directory = calculateWorkspaceDirectory(currExecStep);

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000491")) {
                // use root element

                rootComponentOfComposition = currExecStep.getJSONObject(i).getString("object");
                useComponentFromComposition = true;

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000492")) {
                // use MDB entry component

                componentOfOntology = currExecStep.getJSONObject(i).getString("object");
                useComponentFromOntology = true;

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000199")) {
                // position

                position = Integer.parseInt(currExecStep.getJSONObject(i).getString("object"));

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000520")) {
                // use composition from entry

                compositionFromEntry = currExecStep.getJSONObject(i).getString("object");
                useCompositionFromEntry = true;

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000488")) {
                // use union of compositions with parent root MDB entry component

                rootComponentOfUnionComposition = currExecStep.getJSONObject(i).getString("object");
                useUnionOfCompositionsWithParentRoot = true;

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000390")) {
                // update store [BOOLEAN]

                //System.out.println("currComponentObject before save store" + currComponentObject);

                saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000447")) {
                // show expanded (this entry's specific individual of)

                resourcesToShowExpanded.put(currExecStep.getJSONObject(i).getString("object"));
                propertyForResourcesToShowExpanded = currExecStep.getJSONObject(i).getString("property");
                showExpanded = true;

            }

        }

        if ((useComponentFromComposition ||
                useUnionOfCompositionsWithParentRoot ||
                useCompositionFromEntry) && this.parentRootExist) {

            System.out.println("rootComponentOfUnionComposition = " + this.parentRoot);

            Model unionNGModel = ModelFactory.createDefaultModel(), entryComponentsModel = ModelFactory.createDefaultModel();

            for (int j = 0; j < ngs.length(); j++) {

                unionNGModel = unionNGModel.union(connectionToTDB.pullNamedModelFromTDB(directory, ngs.getString(j)));

            }

            ResIterator resIter = unionNGModel.listSubjects();

            while (resIter.hasNext()) {

                Resource entryComponentURI = resIter.next();

                if (unionNGModel.contains(entryComponentURI, RDF.type, OWL2.NamedIndividual)) {

                    Selector tripleSelector = new SimpleSelector(entryComponentURI, null, null, "");

                    StmtIterator tripleStmts = unionNGModel.listStatements(tripleSelector);

                    while (tripleStmts.hasNext()) {

                        Statement stmt = tripleStmts.nextStatement();

                        Resource currSubject = stmt.getSubject();

                        Property currProperty = stmt.getPredicate();

                        Resource currObject;

                        if (stmt.getObject().isURIResource()) {

                            currObject = stmt.getObject().asResource();

                            if (currSubject.equals(entryComponentURI)
                                    && currProperty.equals(RDF.type)
                                    && !currObject.equals(OWL2.NamedIndividual)) {

                                Selector classSelector = new SimpleSelector(currObject, null, null, "");

                                StmtIterator classStmts = unionNGModel.listStatements(classSelector);

                                Resource classSubject = null;

                                while (classStmts.hasNext()) {

                                    Statement classStmt = classStmts.nextStatement();

                                    classSubject = classStmt.getSubject();

                                    if ((!classStmt.getObject().equals(OWL2.Class))
                                            && (!classStmt.getPredicate().equals(RDFS.label))
                                            && (!classStmt.getPredicate().equals(RDFS.subClassOf))
                                            && (!classStmt.getPredicate().equals(OWL2.annotatedTarget))
                                            && (!classStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                        entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, classStmt.getPredicate(), classStmt.getObject()));

                                    }

                                }

                                if (unionNGModel.contains(null, OWL2.annotatedSource, classSubject)) {

                                    ResIterator axiomsForClassSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, classSubject);

                                    while (axiomsForClassSubject.hasNext()) {

                                        Resource axiomClassSubject = axiomsForClassSubject.next();

                                        Selector axiomClassSelector = new SimpleSelector(axiomClassSubject, null, null, "");

                                        StmtIterator axiomClassStmts = unionNGModel.listStatements(axiomClassSelector);

                                        while (axiomClassStmts.hasNext()) {

                                            Statement axiomClassStmt = axiomClassStmts.nextStatement();

                                            if ((!axiomClassStmt.getObject().equals(OWL2.Axiom))
                                                    && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedSource))
                                                    && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                    && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                                entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomClassStmt.getPredicate(), axiomClassStmt.getObject()));

                                            }

                                        }

                                    }

                                }

                            }

                        }

                        entryComponentsModel.add(stmt);

                    }

                    if (unionNGModel.contains(null, OWL2.annotatedSource, entryComponentURI)) {

                        ResIterator axiomsForSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, entryComponentURI);

                        while (axiomsForSubject.hasNext()) {

                            Resource axiomSubject = axiomsForSubject.next();

                            Selector axiomSelector = new SimpleSelector(axiomSubject, null, null, "");

                            StmtIterator axiomStmts = unionNGModel.listStatements(axiomSelector);

                            while (axiomStmts.hasNext()) {

                                Statement axiomStmt = axiomStmts.nextStatement();

                                if ((!axiomStmt.getObject().equals(OWL2.Axiom))
                                        && (!axiomStmt.getPredicate().equals(OWL2.annotatedSource))
                                        && (!axiomStmt.getPredicate().equals(OWL2.annotatedTarget))
                                        && (!axiomStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                    entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomStmt.getPredicate(), axiomStmt.getObject()));

                                }

                            }

                        }

                    }

                }

            }

            StmtIterator entryComponentsModelIter = entryComponentsModel.listStatements();

            OutputGenerator outputGenerator = new OutputGenerator();

            JSONObject entryComponents = this.parentComponents;

            while (entryComponentsModelIter.hasNext()) {

                Statement resStmt = entryComponentsModelIter.nextStatement();

                String currSubject = resStmt.getSubject().toString();

                entryComponents = outputGenerator
                        .manageProperty(currSubject, resStmt, entryComponents, jsonInputObject, connectionToTDB);

            }

            entryComponents = outputGenerator.reorderEntryComponentsValues(entryComponents);

            Iterator<String> iter = entryComponents.keys();

            outputDataJSON = new JSONArray();

            while (iter.hasNext()) {

                String currKey = iter.next();

                JSONObject wrapperJSON = new JSONObject();

                wrapperJSON.put(currKey, entryComponents.getJSONObject(currKey));

                outputDataJSON.put(wrapperJSON);

            }

            if (useCompositionFromEntry) {

                if (compositionFromEntry.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                    // KEYWORD: this MDB user entry ID

                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                    String workspace = calculateWorkspaceDirectory(currExecStep);

                    rootComponentOfUnionComposition = individualURI.getThisURIForAnIndividual(this.parentRoot, workspace, connectionToTDB);

                } else if (compositionFromEntry.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000114")) {
                    // KEYWORD: this MDB entry ID

                    IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                    String workspace = calculateWorkspaceDirectory(currExecStep);

                    rootComponentOfUnionComposition = individualURI.getThisURIForAnIndividual(this.parentRoot, workspace, connectionToTDB);

                }

            }

            outputDataJSON = outputGenerator.orderOutputJSON(rootComponentOfUnionComposition, outputDataJSON);

        } else {

            if (useComponentFromComposition) {

                System.out.println("rootComponentOfComposition = " + rootComponentOfComposition);

                Model unionNGModel = ModelFactory.createDefaultModel(), entryComponentsModel = ModelFactory.createDefaultModel();

                for (int j = 0; j < ngs.length(); j++) {

                    unionNGModel = unionNGModel.union(connectionToTDB.pullNamedModelFromTDB(directory, ngs.getString(j)));

                }

                ResIterator subIter = unionNGModel.listSubjects();

                while (subIter.hasNext()) {

                    Resource potentialSubject = subIter.next();

                    if (unionNGModel.contains(potentialSubject, RDF.type, OWL2.NamedIndividual) &&
                            !potentialSubject.toString().contains("http://www.morphdbase.de/resource/")) {

                        Selector tripleSelector = new SimpleSelector(potentialSubject, RDF.type, null, "");

                        StmtIterator tripleStmts = unionNGModel.listStatements(tripleSelector);

                        while (tripleStmts.hasNext()) {

                            Statement stmt = tripleStmts.nextStatement();

                            Resource currSubject = stmt.getSubject();

                            Property currProperty = stmt.getPredicate();

                            Resource currObject;

                            if (stmt.getObject().isURIResource()) {

                                currObject = stmt.getObject().asResource();

                                if (currSubject.equals(potentialSubject)
                                        && currProperty.equals(RDF.type)
                                        && !currObject.equals(OWL2.NamedIndividual)) {

                                    int index;

                                    if (this.numberOfClassInstancesOverlay.has(currObject.toString())) {

                                        index = (this.numberOfClassInstancesOverlay.getInt(currObject.toString()) + 1);

                                        this.numberOfClassInstancesOverlay.put(currObject.toString(), index);

                                    } else {

                                        index = 1;

                                        this.numberOfClassInstancesOverlay.put(currObject.toString(), 1);

                                    }

                                    this.classOverlayMapping = this.classOverlayMapping.put(potentialSubject.toString(), "http://www.morphdbase.de/resource/dummy-overlay#" + currObject.getLocalName() + "_" + index);

                                }

                            }

                        }

                    }

                }

                ModelResourceExchanger modelResourceExchanger = new ModelResourceExchanger();

                unionNGModel = modelResourceExchanger.substituteSubjectIndividualsInModel(unionNGModel, this.classOverlayMapping);

                this.overlayModel.add(unionNGModel);

                if (unionNGModel.isEmpty()) {

                    System.out.println();
                    System.out.println("WARN: The composition for the root " + rootComponentOfComposition + " is empty!");
                    System.out.println("WARN: Maybe update the default composition on the admin page.");
                    System.out.println();

                }

                ResIterator resIter = unionNGModel.listSubjects();

                while (resIter.hasNext()) {

                    Resource entryComponentURI = resIter.next();

                    if (unionNGModel.contains(entryComponentURI, RDF.type, OWL2.NamedIndividual)) {

                        Selector tripleSelector = new SimpleSelector(entryComponentURI, null, null, "");

                        StmtIterator tripleStmts = unionNGModel.listStatements(tripleSelector);

                        while (tripleStmts.hasNext()) {

                            Statement stmt = tripleStmts.nextStatement();

                            Resource currSubject = stmt.getSubject();

                            Property currProperty = stmt.getPredicate();

                            Resource currObject;

                            if (stmt.getObject().isURIResource()) {

                                currObject = stmt.getObject().asResource();

                                if (currSubject.equals(entryComponentURI)
                                        && currProperty.equals(RDF.type)
                                        && !currObject.equals(OWL2.NamedIndividual)) {

                                    Selector classSelector = new SimpleSelector(currObject, null, null, "");

                                    StmtIterator classStmts = unionNGModel.listStatements(classSelector);

                                    Resource classSubject = null;

                                    while (classStmts.hasNext()) {

                                        Statement classStmt = classStmts.nextStatement();

                                        classSubject = classStmt.getSubject();

                                        if ((!classStmt.getObject().equals(OWL2.Class))
                                                && (!classStmt.getPredicate().equals(RDFS.label))
                                                && (!classStmt.getPredicate().equals(RDFS.subClassOf))
                                                && (!classStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                && (!classStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                            entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, classStmt.getPredicate(), classStmt.getObject()));

                                        }

                                        if (showExpanded) {
                                            // add information if a gui component should be expanded

                                            for (int i = 0; i < resourcesToShowExpanded.length(); i++) {

                                                if (ResourceFactory.createResource(resourcesToShowExpanded.getString(i)).equals(classSubject)) {

                                                    entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, ResourceFactory.createProperty(propertyForResourcesToShowExpanded), ResourceFactory.createPlainLiteral("true")));

                                                }

                                            }

                                        }

                                    }

                                    if (unionNGModel.contains(null, OWL2.annotatedSource, classSubject)) {

                                        ResIterator axiomsForClassSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, classSubject);

                                        while (axiomsForClassSubject.hasNext()) {

                                            Resource axiomClassSubject = axiomsForClassSubject.next();

                                            Selector axiomClassSelector = new SimpleSelector(axiomClassSubject, null, null, "");

                                            StmtIterator axiomClassStmts = unionNGModel.listStatements(axiomClassSelector);

                                            while (axiomClassStmts.hasNext()) {

                                                Statement axiomClassStmt = axiomClassStmts.nextStatement();

                                                if ((!axiomClassStmt.getObject().equals(OWL2.Axiom))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedSource))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                                    entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomClassStmt.getPredicate(), axiomClassStmt.getObject()));

                                                }

                                            }

                                        }

                                    }

                                    if (currSubject.equals(entryComponentURI)
                                            && currProperty.equals(RDF.type)
                                            && currObject.equals(ResourceFactory.createResource(rootComponentOfComposition))) {

                                        rootComponentOfComposition = currSubject.toString();

                                    }

                                }

                            }

                            entryComponentsModel.add(stmt);

                        }

                        if (unionNGModel.contains(null, OWL2.annotatedSource, entryComponentURI)) {

                            ResIterator axiomsForSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, entryComponentURI);

                            while (axiomsForSubject.hasNext()) {

                                Resource axiomSubject = axiomsForSubject.next();

                                Selector axiomSelector = new SimpleSelector(axiomSubject, null, null, "");

                                StmtIterator axiomStmts = unionNGModel.listStatements(axiomSelector);

                                while (axiomStmts.hasNext()) {

                                    Statement axiomStmt = axiomStmts.nextStatement();

                                    if ((!axiomStmt.getObject().equals(OWL2.Axiom))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedSource))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedTarget))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                        entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomStmt.getPredicate(), axiomStmt.getObject()));

                                    }

                                }

                            }

                        }

                    }

                }

                StmtIterator entryComponentsModelIter = entryComponentsModel.listStatements();

                OutputGenerator outputGenerator = new OutputGenerator();

                JSONObject entryComponents = new JSONObject();

                while (entryComponentsModelIter.hasNext()) {

                    Statement resStmt = entryComponentsModelIter.nextStatement();

                    String currSubject = resStmt.getSubject().toString();

                    entryComponents = outputGenerator
                            .manageProperty(currSubject, resStmt, entryComponents, jsonInputObject, connectionToTDB);

                }

                entryComponents = outputGenerator.reorderEntryComponentsValues(entryComponents);

                Iterator<String> iter = entryComponents.keys();

                outputDataJSON = new JSONArray();

                while (iter.hasNext()) {

                    String currKey = iter.next();

                    JSONObject wrapperJSON = new JSONObject();

                    wrapperJSON.put(currKey, entryComponents.getJSONObject(currKey));

                    outputDataJSON.put(wrapperJSON);

                }

                if (useCompositionFromEntry) {

                    if (compositionFromEntry.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                        // KEYWORD: this MDB user entry ID

                        IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                        String workspace = calculateWorkspaceDirectory(currExecStep);

                        rootComponentOfComposition = individualURI.getThisURIForAnIndividual(rootComponentOfComposition, workspace, connectionToTDB);

                    }

                }

                if (this.classOverlayMapping.has(rootComponentOfComposition)) {

                    rootComponentOfComposition = this.classOverlayMapping.getString(rootComponentOfComposition);

                }

                System.out.println("outputDataJSON1 = " + outputDataJSON);

                outputDataJSON = outputGenerator.orderOutputJSON(rootComponentOfComposition, outputDataJSON);

                System.out.println("outputDataJSON2 = " + outputDataJSON);

            } else if (useUnionOfCompositionsWithParentRoot) {

                System.out.println("rootComponentOfUnionComposition = " + rootComponentOfUnionComposition);

                Model unionNGModel = ModelFactory.createDefaultModel(), entryComponentsModel = ModelFactory.createDefaultModel();

                for (int j = 0; j < ngs.length(); j++) {

                    unionNGModel = unionNGModel.union(connectionToTDB.pullNamedModelFromTDB(directory, ngs.getString(j)));

                }

                ResIterator subIter = unionNGModel.listSubjects();

                while (subIter.hasNext()) {

                    Resource potentialSubject = subIter.next();

                    if (unionNGModel.contains(potentialSubject, RDF.type, OWL2.NamedIndividual) &&
                            !potentialSubject.toString().contains("http://www.morphdbase.de/resource/")) {

                        Selector tripleSelector = new SimpleSelector(potentialSubject, RDF.type, null, "");

                        StmtIterator tripleStmts = unionNGModel.listStatements(tripleSelector);

                        while (tripleStmts.hasNext()) {

                            Statement stmt = tripleStmts.nextStatement();

                            Resource currSubject = stmt.getSubject();

                            Property currProperty = stmt.getPredicate();

                            Resource currObject;

                            if (stmt.getObject().isURIResource()) {

                                currObject = stmt.getObject().asResource();

                                if (currSubject.equals(potentialSubject)
                                        && currProperty.equals(RDF.type)
                                        && !currObject.equals(OWL2.NamedIndividual)) {

                                    int index;

                                    if (this.numberOfClassInstancesOverlay.has(currObject.toString())) {

                                        index = (this.numberOfClassInstancesOverlay.getInt(currObject.toString()) + 1);

                                        this.numberOfClassInstancesOverlay.put(currObject.toString(), index);

                                    } else {

                                        index = 1;

                                        this.numberOfClassInstancesOverlay.put(currObject.toString(), 1);

                                    }

                                    this.classOverlayMapping = this.classOverlayMapping.put(potentialSubject.toString(), "http://www.morphdbase.de/resource/dummy-overlay#" + currObject.getLocalName() + "_" + index);

                                }

                            }

                        }

                    }

                }

                ModelResourceExchanger modelResourceExchanger = new ModelResourceExchanger();

                unionNGModel = modelResourceExchanger.substituteSubjectIndividualsInModel(unionNGModel, this.classOverlayMapping);

                this.overlayModel.add(unionNGModel);

                ResIterator resIter = unionNGModel.listSubjects();

                while (resIter.hasNext()) {

                    Resource entryComponentURI = resIter.next();

                    if (unionNGModel.contains(entryComponentURI, RDF.type, OWL2.NamedIndividual)) {

                        Selector tripleSelector = new SimpleSelector(entryComponentURI, null, null, "");

                        StmtIterator tripleStmts = unionNGModel.listStatements(tripleSelector);

                        while (tripleStmts.hasNext()) {

                            Statement stmt = tripleStmts.nextStatement();

                            Resource currSubject = stmt.getSubject();

                            Property currProperty = stmt.getPredicate();

                            Resource currObject;

                            if (stmt.getObject().isURIResource()
                                    && stmt.getPredicate().equals(RDF.type)) {

                                currObject = stmt.getObject().asResource();

                                if (currSubject.equals(entryComponentURI)
                                        && currProperty.equals(RDF.type)
                                        && !currObject.equals(OWL2.NamedIndividual)) {

                                    Selector classSelector = new SimpleSelector(currObject, null, null, "");

                                    StmtIterator classStmts = unionNGModel.listStatements(classSelector);

                                    Resource classSubject = null;

                                    while (classStmts.hasNext()) {

                                        Statement classStmt = classStmts.nextStatement();

                                        classSubject = classStmt.getSubject();

                                        if ((!classStmt.getObject().equals(OWL2.Class))
                                                && (!classStmt.getPredicate().equals(RDFS.label))
                                                && (!classStmt.getPredicate().equals(RDFS.subClassOf))
                                                && (!classStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                && (!classStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                            entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, classStmt.getPredicate(), classStmt.getObject()));

                                        }

                                    }

                                    if (unionNGModel.contains(null, OWL2.annotatedSource, classSubject)) {

                                        ResIterator axiomsForClassSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, classSubject);

                                        while (axiomsForClassSubject.hasNext()) {

                                            Resource axiomClassSubject = axiomsForClassSubject.next();

                                            Selector axiomClassSelector = new SimpleSelector(axiomClassSubject, null, null, "");

                                            StmtIterator axiomClassStmts = unionNGModel.listStatements(axiomClassSelector);

                                            while (axiomClassStmts.hasNext()) {

                                                Statement axiomClassStmt = axiomClassStmts.nextStatement();

                                                if ((!axiomClassStmt.getObject().equals(OWL2.Axiom))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedSource))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                                    entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomClassStmt.getPredicate(), axiomClassStmt.getObject()));

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                            entryComponentsModel.add(stmt);

                        }

                        if (unionNGModel.contains(null, OWL2.annotatedSource, entryComponentURI)) {

                            ResIterator axiomsForSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, entryComponentURI);

                            while (axiomsForSubject.hasNext()) {

                                Resource axiomSubject = axiomsForSubject.next();

                                Selector axiomSelector = new SimpleSelector(axiomSubject, null, null, "");

                                StmtIterator axiomStmts = unionNGModel.listStatements(axiomSelector);

                                while (axiomStmts.hasNext()) {

                                    Statement axiomStmt = axiomStmts.nextStatement();

                                    if ((!axiomStmt.getObject().equals(OWL2.Axiom))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedSource))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedTarget))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                        entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomStmt.getPredicate(), axiomStmt.getObject()));

                                    }

                                }

                            }

                        }

                    }

                }

                StmtIterator entryComponentsModelIter = entryComponentsModel.listStatements();

                OutputGenerator outputGenerator = new OutputGenerator();

                JSONObject entryComponents = new JSONObject();

                while (entryComponentsModelIter.hasNext()) {

                    Statement resStmt = entryComponentsModelIter.nextStatement();

                    String currSubject = resStmt.getSubject().toString();

                    entryComponents = outputGenerator
                            .manageProperty(currSubject, resStmt, entryComponents, jsonInputObject, connectionToTDB);

                }

                this.parentRootExist = true;

                this.parentRootPosition = position;

                this.parentRoot = rootComponentOfUnionComposition;

                this.parentComponents = entryComponents;

            } else if (useComponentFromOntology) {

                System.out.println("componentOfOntology = " + componentOfOntology);

                JSONArray resourcesToCheck = new JSONArray();

                Model entryComponentsModel = ModelFactory.createDefaultModel();

                resourcesToCheck.put(componentOfOntology);

                while (!resourcesToCheck.isNull(0)) {

                    Model individualsModel = findTriple(resourcesToCheck.getString(0), directory, connectionToTDB);

                    if (individualsModel.contains(ResourceFactory.createResource(resourcesToCheck.getString(0)), RDF.type, OWL2.NamedIndividual)) {

                        Selector tripleSelector = new SimpleSelector(ResourceFactory.createResource(resourcesToCheck.getString(0)), null, null, "");

                        StmtIterator tripleStmts = individualsModel.listStatements(tripleSelector);

                        while (tripleStmts.hasNext()) {

                            Statement currStatement = tripleStmts.nextStatement();

                            Property currProperty = currStatement.getPredicate();

                            Resource currObject;

                            if (currStatement.getObject().isURIResource()) {

                                currObject = currStatement.getObject().asResource();

                                if (currProperty.equals(RDF.type)
                                        && !currObject.equals(OWL2.NamedIndividual)) {

                                    Model classModel = findTriple(currObject.toString(), directory, connectionToTDB);

                                    StmtIterator classStmts = classModel.listStatements();

                                    while (classStmts.hasNext()) {

                                        Statement classStmt = classStmts.nextStatement();

                                        if ((!classStmt.getObject().equals(OWL2.Class))
                                                && (!classStmt.getPredicate().equals(RDFS.label))
                                                && (!classStmt.getPredicate().equals(RDFS.subClassOf))) {

                                            entryComponentsModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(resourcesToCheck.getString(0)), classStmt.getPredicate(), classStmt.getObject()));

                                        }

                                    }

                                    Model axiomClassModel = findAxiomTriple(currObject.toString(), directory, connectionToTDB);

                                    StmtIterator axiomClassStmts = axiomClassModel.listStatements();

                                    while (axiomClassStmts.hasNext()) {

                                        Statement axiomClassStmt = axiomClassStmts.nextStatement();

                                        if ((!axiomClassStmt.getObject().equals(OWL2.Axiom))
                                                && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedSource))
                                                && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                            entryComponentsModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(resourcesToCheck.getString(0)), axiomClassStmt.getPredicate(), axiomClassStmt.getObject()));

                                        }

                                    }

                                } else if (currProperty.equals(ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000040"))) {
                                    // add childs to loop

                                    resourcesToCheck.put(currObject.toString());

                                }

                            }

                            entryComponentsModel.add(currStatement);

                        }

                    }

                    Model axiomIndividualsModel = findAxiomTriple(resourcesToCheck.getString(0), directory, connectionToTDB);

                    StmtIterator axiomIndividualStmts = axiomIndividualsModel.listStatements();

                    while (axiomIndividualStmts.hasNext()) {

                        Statement axiomIndividualStmt = axiomIndividualStmts.nextStatement();

                        if ((!axiomIndividualStmt.getObject().equals(OWL2.Axiom))
                                && (!axiomIndividualStmt.getPredicate().equals(OWL2.annotatedSource))
                                && (!axiomIndividualStmt.getPredicate().equals(OWL2.annotatedTarget))
                                && (!axiomIndividualStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                            entryComponentsModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(resourcesToCheck.getString(0)), axiomIndividualStmt.getPredicate(), axiomIndividualStmt.getObject()));

                        }

                    }

                    resourcesToCheck.remove(0);

                }

                StmtIterator entryComponentsModelIter = entryComponentsModel.listStatements();

                OutputGenerator outputGenerator = new OutputGenerator();

                JSONObject entryComponents = new JSONObject();

                while (entryComponentsModelIter.hasNext()) {

                    Statement resStmt = entryComponentsModelIter.nextStatement();

                    entryComponents = outputGenerator
                            .manageProperty(resStmt.getSubject().toString(), resStmt, entryComponents,
                                    jsonInputObject, connectionToTDB);

                }

                entryComponents = outputGenerator.reorderEntryComponentsValues(entryComponents);

                Iterator<String> iter = entryComponents.keys();

                while (iter.hasNext()) {

                    String currKey = iter.next();

                    JSONObject wrapperJSON = new JSONObject();

                    wrapperJSON.put(currKey, entryComponents.getJSONObject(currKey));

                    outputDataJSON.put(wrapperJSON);

                }

                outputDataJSON = outputGenerator.orderOutputJSON(componentOfOntology, outputDataJSON);

            }

        }

        if (!outputDataJSON.isNull(0)) {

            if (currComponentObject.has("compositionForMDBHyperlink")) {// todo change key to "data" at a later point

                JSONArray compositionForMDBHyperlink = currComponentObject.getJSONArray("compositionForMDBHyperlink");

                compositionForMDBHyperlink.put(position - 1, outputDataJSON.getJSONObject(0));

                currComponentObject.put("compositionForMDBHyperlink", compositionForMDBHyperlink);

            } else {

                JSONArray compositionForMDBHyperlink = new JSONArray();

                compositionForMDBHyperlink.put(position - 1, outputDataJSON.getJSONObject(0));

                currComponentObject.put("compositionForMDBHyperlink", compositionForMDBHyperlink);

            }

        }

        return currComponentObject;
    }

    /**
     * This method calculates and formats the output for a mdb composition.
     * @param root contains the URI of a root element
     * @param ngs contains the URI of a named graph which contains the root element
     * @param directory contains the path to the directory which contains the root element
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a formatted JSONArray
     */
    public JSONArray getCompositionFromStoreForOutput(String root, JSONArray ngs, String directory, JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        Model unionNGModel = ModelFactory.createDefaultModel(), entryComponentsModel = ModelFactory.createDefaultModel();

        for (int j = 0; j < ngs.length(); j++) {

            unionNGModel = unionNGModel.union(connectionToTDB.pullNamedModelFromTDB(directory, ngs.getString(j)));

        }

        ResIterator resIter = unionNGModel.listSubjects();

        while (resIter.hasNext()) {

            Resource entryComponentURI = resIter.next();

            if (unionNGModel.contains(entryComponentURI, RDF.type, OWL2.NamedIndividual)) {

                Selector tripleSelector = new SimpleSelector(entryComponentURI, null, null, "");

                StmtIterator tripleStmts = unionNGModel.listStatements(tripleSelector);

                while (tripleStmts.hasNext()) {

                    Statement stmt = tripleStmts.nextStatement();

                    Resource currSubject = stmt.getSubject();

                    Property currProperty = stmt.getPredicate();

                    Resource currLabelObject;

                    if (stmt.getObject().isURIResource()) {

                        currLabelObject = stmt.getObject().asResource();

                        if (currSubject.equals(entryComponentURI)
                                && currProperty.equals(RDF.type)
                                && !currLabelObject.equals(OWL2.NamedIndividual)) {

                            Selector classSelector = new SimpleSelector(currLabelObject, null, null, "");

                            StmtIterator classStmts = unionNGModel.listStatements(classSelector);

                            Resource classSubject = null;

                            while (classStmts.hasNext()) {

                                Statement classStmt = classStmts.nextStatement();

                                classSubject = classStmt.getSubject();

                                if ((!classStmt.getObject().equals(OWL2.Class))
                                        && (!classStmt.getPredicate().equals(RDFS.label))
                                        && (!classStmt.getPredicate().equals(RDFS.subClassOf))
                                        && (!classStmt.getPredicate().equals(OWL2.annotatedTarget))
                                        && (!classStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                    entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, classStmt.getPredicate(), classStmt.getObject()));

                                }

                            }

                            if (unionNGModel.contains(null, OWL2.annotatedSource, classSubject)) {

                                ResIterator axiomsForClassSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, classSubject);

                                while (axiomsForClassSubject.hasNext()) {

                                    Resource axiomClassSubject = axiomsForClassSubject.next();

                                    Selector axiomClassSelector = new SimpleSelector(axiomClassSubject, null, null, "");

                                    StmtIterator axiomClassStmts = unionNGModel.listStatements(axiomClassSelector);

                                    while (axiomClassStmts.hasNext()) {

                                        Statement axiomClassStmt = axiomClassStmts.nextStatement();

                                        if ((!axiomClassStmt.getObject().equals(OWL2.Axiom))
                                                && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedSource))
                                                && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                            entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomClassStmt.getPredicate(), axiomClassStmt.getObject()));

                                        }

                                    }

                                }

                            }

                        }

                    }

                    entryComponentsModel.add(stmt);

                }

                if (unionNGModel.contains(null, OWL2.annotatedSource, entryComponentURI)) {

                    ResIterator axiomsForSubject = unionNGModel.listSubjectsWithProperty(OWL2.annotatedSource, entryComponentURI);

                    while (axiomsForSubject.hasNext()) {

                        Resource axiomSubject = axiomsForSubject.next();

                        Selector axiomSelector = new SimpleSelector(axiomSubject, null, null, "");

                        StmtIterator axiomStmts = unionNGModel.listStatements(axiomSelector);

                        while (axiomStmts.hasNext()) {

                            Statement axiomStmt = axiomStmts.nextStatement();

                            if ((!axiomStmt.getObject().equals(OWL2.Axiom))
                                    && (!axiomStmt.getPredicate().equals(OWL2.annotatedSource))
                                    && (!axiomStmt.getPredicate().equals(OWL2.annotatedTarget))
                                    && (!axiomStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                entryComponentsModel.add(ResourceFactory.createStatement(entryComponentURI, axiomStmt.getPredicate(), axiomStmt.getObject()));

                            }

                        }

                    }

                }

            }

        }

        StmtIterator entryComponentsModelIter = entryComponentsModel.listStatements();

        OutputGenerator outputGenerator = new OutputGenerator();

        JSONObject entryComponents = new JSONObject();

        while (entryComponentsModelIter.hasNext()) {

            Statement resStmt = entryComponentsModelIter.nextStatement();

            String currSubject = resStmt.getSubject().toString();

            entryComponents = outputGenerator
                    .manageProperty(currSubject, resStmt, entryComponents, jsonInputObject, connectionToTDB);

        }

        entryComponents = outputGenerator.reorderEntryComponentsValues(entryComponents);

        Iterator<String> iter = entryComponents.keys();

        JSONArray outputDataJSON = new JSONArray();

        while (iter.hasNext()) {

            String currKey = iter.next();

            JSONObject wrapperJSON = new JSONObject();

            wrapperJSON.put(currKey, entryComponents.getJSONObject(currKey));

            outputDataJSON.put(wrapperJSON);

        }

        outputDataJSON = outputGenerator.orderOutputJSON(root, outputDataJSON);

        return outputDataJSON;

    }


    /**
     * This method defines variables for a later use.
     * @param currComponentObject contains information about the current object
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     */
    public JSONObject executionStepDefineVariables(JSONObject currComponentObject, JSONObject jsonInputObject, JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        boolean useInKnownSubsequentWA = false;

        JSONObject keywordsToTransfer = new JSONObject();

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000526")) {
                // use in known subsequent workflow action [BOOLEAN]

                useInKnownSubsequentWA = true;

            }

        }

        for (int i = 0; i < currExecStep.length(); i++) {

            boolean useAsInput = useObjectAsInput(currExecStep.getJSONObject(i).getString("property"), connectionToTDB);

            if (useAsInput) {

                String uriOfIndividual = getKeywordIndividualFromProperty(currExecStep.getJSONObject(i).getString("property"), connectionToTDB);

                if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                    // KEYWORD: empty

                    this.infoInput.put(currExecStep.getJSONObject(i).getString("object"), currExecStep.getJSONObject(i).getString("object"));

                }

                String value = currExecStep.getJSONObject(i).getString("object");

                Iterator<String> keyIterator = this.generatedResources.keys();

                while (keyIterator.hasNext()) {

                    String currKey = keyIterator.next();

                    // get local name of a key
                    String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                    if (value.contains(localNameOfKey)) {
                        // get ng from generated resources

                        value = this.generatedResources.getString(currKey);

                    }

                }

                if (jsonInputObject.has("localIDs")) {

                    JSONArray currJSONArray = jsonInputObject.getJSONArray("localIDs");

                    for (int j = 0; j < currJSONArray.length(); j++) {

                        JSONObject currJSONObject = currJSONArray.getJSONObject(j);

                        if (currJSONObject.has("keyword")) {

                            if (ResourceFactory.createResource(value).getLocalName().equals(currJSONObject.getString("keyword")) &&
                                    jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                if (EmailValidator.getInstance().isValid(currJSONObject.getString("value"))) {

                                    value = "mailto:" + currJSONObject.getString("value");

                                } else {

                                    value = currJSONObject.getString("value");

                                }

                            }

                        }

                    }

                }

                this.infoInput.put(uriOfIndividual, value);
                this.infoInput.put(value, "");

                System.out.println();
                System.out.println("uriOfIndividual = " + uriOfIndividual);
                System.out.println();

                if (useInKnownSubsequentWA) {

                    keywordsToTransfer.put(uriOfIndividual, value);

                }

            }

        }

        currComponentObject.put("use_in_known_subsequent_WA", keywordsToTransfer);

        return currComponentObject;

    }

    /**
     * This method removes statement(s) in a jena tdb
     * @param currComponentObject contains information about the current object
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return additional modified information about the current object
     */
    public JSONObject executionStepDeleteMultipleTripleStatements(JSONObject currComponentObject,
                                                                  JSONObject jsonInputObject, JSONArray currExecStep,
                                                                  JenaIOTDBFactory connectionToTDB) {

        // todo: actualize this method if statements should delete

        boolean deleteTriplesWithSubject = false, deleteTriplesWithObject = false, deleteWithProperty = false;
        String subject = "", property = "", object = "", ng ="", directory = "";

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000229")) {
                // subject (copied individual of)

                JSONObject dataToFindObjectInTDB = new JSONObject();

                subject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                deleteTriplesWithSubject = true;

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000228")) {
                // object (copied individual of)

                JSONObject dataToFindObjectInTDB = new JSONObject();

                object = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, "r", connectionToTDB);

                deleteTriplesWithObject = true;

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                // load from/save to/update in named graph (this entry's specific individual of)

                ng = calculateNG(currExecStep, connectionToTDB);

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000078")) {
                // named graph belongs to workspace

                directory = calculateWorkspaceDirectory(currExecStep);

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000041")) {
                // property

                property = calculateProperty(currExecStep);

                deleteWithProperty = true;

            }

        }

        if (deleteTriplesWithSubject) {

            JSONArray subjectsJSON = currComponentObject.getJSONObject("input_data").getJSONArray("subject");

            if (deleteWithProperty) {

                JSONArray propertyJSON = currComponentObject.getJSONObject("input_data").getJSONArray("property");

                for (int j = (subjectsJSON.length() - 1); j >= 0; j--) {

                    if (subjectsJSON.getString(j).equals(subject)
                            &&  propertyJSON.getString(j).equals(property)) {

                        currComponentObject.getJSONObject("input_data").getJSONArray("subject").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("property").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_data").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_type").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("ng").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("directory").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("operation").remove(j);

                    }

                }


            } else {

                for (int j = (subjectsJSON.length() - 1); j >= 0; j--) {

                    if (subjectsJSON.getString(j).equals(subject)) {

                        currComponentObject.getJSONObject("input_data").getJSONArray("subject").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("property").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_data").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_type").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("ng").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("directory").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("operation").remove(j);

                    }

                }

            }

        } else if (deleteTriplesWithObject) {

            JSONArray objectsJSON = currComponentObject.getJSONObject("input_data").getJSONArray("object_data");

            if (deleteWithProperty) {

                JSONArray propertyJSON = currComponentObject.getJSONObject("input_data").getJSONArray("property");

                for (int j = (objectsJSON.length() - 1); j >= 0; j--) {

                    if (objectsJSON.getString(j).equals(object)
                            &&  propertyJSON.getString(j).equals(property)) {

                        currComponentObject.getJSONObject("input_data").getJSONArray("subject").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("property").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_data").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_type").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("ng").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("directory").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("operation").remove(j);

                    }

                }

            } else {

                for (int j = (objectsJSON.length() - 1); j >= 0; j--) {

                    if (objectsJSON.getString(j).equals(object)) {

                        currComponentObject.getJSONObject("input_data").getJSONArray("subject").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("property").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_data").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("object_type").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("ng").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("directory").remove(j);
                        currComponentObject.getJSONObject("input_data").getJSONArray("operation").remove(j);

                    }

                }

            }

        }

        return currComponentObject;

    }


    /**
     * This method executes an order from the ontology.
     * @param currComponentObject contains information about the current object
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a modified JSONObject
     */
    public JSONObject executionStepExecuteNow (JSONObject currComponentObject, JSONObject jsonInputObject,
                                               JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        boolean saveToStoreExist = false;

        for (int j = 0; j < currExecStep.length(); j++) {

            if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000390")) {
                // update store [BOOLEAN]

                saveToStoreExist = true;

            } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000387")) {
                // MDB info-message

                currComponentObject.put(ResourceFactory.createProperty(currExecStep.getJSONObject(j).getString("property")).getLocalName(), ResourceFactory.createPlainLiteral(currExecStep.getJSONObject(j).getString("object")).asLiteral().getLexicalForm());

            }

        }

        if (saveToStoreExist) {

            //System.out.println("currComponentObject before save store" + currComponentObject);

            saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

        }

        return currComponentObject;

    }


    /**
     * This method search for a resource or value in the jena tdb and save the result in an identified keyword
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     */
    public void executionStepSearchMDB(JSONArray currExecStep, JSONObject currComponentObject,
                                       JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        boolean executeThisStep = true;

        if (jsonInputObject.has("mdbcoreid")) {

            if (jsonInputObject.getString("mdbcoreid").equals("http://www.morphdbase.de/resource/dummy-overlay")) {

                executeThisStep = false;

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077")) {
                        // load from/save to/update in named graph


                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000392")) {
                        // load from/save to/update in named graph (individual of)


                    } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                        // load from/save to/update in named graph (this entry's specific individual of) update store [BOOLEAN]

                        if (jsonInputObject.getString("html_form").contains(ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("object")).getLocalName())) {

                            this.createOverlayNG = this.mdbCoreID.substring(0, this.mdbCoreID.indexOf("resource/dummy-overlay")) + jsonInputObject.getString("html_form");

                            this.hasCreateOverlayInput = true;

                            executeThisStep = true;

                            System.out.println("createOverlayNG = " + this.createOverlayNG);

                        }

                    }

                }

            }

        }

        if (executeThisStep) {

            JSONObject dataToFindObjectInTDB = new JSONObject();

            String currSubject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

            //System.out.println("currSubject = " + currSubject);

            String currProperty = calculateProperty(currExecStep);

            //System.out.println("currProperty = " + currProperty);

            String currNG = calculateNG(currExecStep, connectionToTDB);

            //System.out.println("currNG = " + currNG);

            String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

            //System.out.println("currDirectoryPath = " + currDirectoryPath);

            String currObjectType = calculateObjectType(currProperty);

            //System.out.println("currObjectType = " + currObjectType);

            dataToFindObjectInTDB.put("subject", currSubject);
            dataToFindObjectInTDB.put("property", currProperty);
            dataToFindObjectInTDB.put("ng", currNG);
            dataToFindObjectInTDB.put("directory", currDirectoryPath);

            String currObject = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, currObjectType, connectionToTDB);

            //System.out.println("currObject = " + currObject);

            String searchTarget = null;

            String searchTargetKeyword = null;

            for (int i = 0; i < currExecStep.length();i++) {

                if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000481")) {
                    // search target

                    if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000466")) {
                        // KEYWORD: subject

                        searchTarget = currSubject;

                    } else if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000467")) {
                        // KEYWORD: property

                        searchTarget = currProperty;

                    } else if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000468")) {
                        // KEYWORD: object

                        searchTarget = currObject;

                    } else if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000469")) {
                        // KEYWORD: named graph

                        searchTarget = currNG;

                    }

                } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000340")) {
                    // search target defines keyword

                    searchTargetKeyword = currExecStep.getJSONObject(i).getString("object");

                }

            }

            this.identifiedResources.put(searchTargetKeyword, searchTarget);

        }

    }

    /**
     * This method copies and modifies statements from the default composition to a specific composition
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return input information for a jena tdb
     */
    public JSONObject executionStepCopyAndSaveTripleStatements(JSONArray currExecStep, JSONObject currComponentObject,
                                                               JenaIOTDBFactory connectionToTDB) {

        for (int i = 0; i < currExecStep.length();i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000103")) {
                // copy from named graph (of class)

                Iterator<String> infoInputKeys = this.infoInput.keys();

                boolean continueLoop = true;

                while (infoInputKeys.hasNext() && continueLoop) {

                    String currKey = infoInputKeys.next();

                    if (currKey.equals(currExecStep.getJSONObject(i).getString("object"))) {

                        currExecStep.getJSONObject(i).put("object", this.infoInput.get(currKey));

                        continueLoop = false;

                    }

                }

                Model defaultCompositionModel = findRootIndividual(currExecStep.getJSONObject(i).getString("object"), currExecStep, connectionToTDB);

                String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

                String ng = calculateNG(currExecStep, connectionToTDB);

                boolean modelExistInTDB = connectionToTDB.modelExistInTDB(currDirectoryPath, ng);

                if (!modelExistInTDB) {

                    Selector selector = new SimpleSelector(null, RDF.type, null, "");

                    StmtIterator typeStmts = defaultCompositionModel.listStatements(selector);

                    while (typeStmts.hasNext()) {

                        Statement currStmt = typeStmts.nextStatement();

                        Resource currObject = currStmt.getObject().asResource();
                        Resource currSubject = currStmt.getSubject();

                        String newResource = "";

                        if (!(currObject.equals(OWL2.Axiom))
                                && !(currObject.equals(OWL2.Class))
                                && !(currObject.equals(OWL2.NamedIndividual))) {

                            if (this.mdbCoreIDNotEmpty
                                    && this.mdbUEIDNotEmpty
                                    && !this.mdbEntryIDNotEmpty) {


                            } else if (this.mdbEntryIDNotEmpty
                                    && this.mdbUEIDNotEmpty) {

                                IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                                newResource = individualURI.createURIForAnIndividualForANewNamespace(currObject.toString());

                            } else if (mdbUEIDNotEmpty) {

                                IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                newResource = individualURI.createURIForAnIndividualForANewNamespace(currObject.toString());

                            }

                            if (this.numberOfClassInstances.has(currObject.toString())) {

                                if (defaultCompositionModel.contains(currSubject, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000653"))) {
                                    // is root entry component of composition contained in named graph

                                    newResource = this.rootResourcesOfCompositions.getString(currSubject.toString());

                                } else {

                                    int newNumberOfInstancesOfClass = this.numberOfClassInstances.getInt(currObject.toString()) + 1;

                                    this.numberOfClassInstances.put(currObject.toString(), newNumberOfInstancesOfClass);

                                    newResource = newResource.substring(0, newResource.lastIndexOf("_")) + "_" + newNumberOfInstancesOfClass;

                                }

                            } else {

                                if (defaultCompositionModel.contains(currSubject, ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000653"))) {
                                    // is root entry component of composition contained in named graph

                                    this.rootResourcesOfCompositions.put(currSubject.toString(), newResource);

                                }

                                this.numberOfClassInstances.put(currObject.toString(), 1);

                            }

                            this.entrySpecificAndDefaultResourcesMap.put(currSubject.toString(), newResource);

                        }

                    }

                }

                StmtIterator stmtIterator = defaultCompositionModel.listStatements();

                while (stmtIterator.hasNext()) {

                    Statement currStmt = stmtIterator.nextStatement();
                    String currSubject = currStmt.getSubject().toString();
                    String currObject;

                    boolean generateIndividuals;

                    if (defaultCompositionModel.contains(currStmt.getSubject(), ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000653"))) {
                            // is root entry component of composition contained in named graph

                        if (defaultCompositionModel.contains(currStmt.getSubject(), ResourceFactory.createProperty("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000653"), ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("object")))) {
                            // is root entry component of composition contained in named graph

                            generateIndividuals = true;

                        } else {

                            generateIndividuals = false;

                        }

                    } else {

                        generateIndividuals = true;

                    }

                    if (generateIndividuals) {

                        if (currStmt.getObject().isURIResource()) {

                            currObject = currStmt.getObject().asResource().toString();

                        } else if(currStmt.getObject().isAnon()) {

                            currObject = currStmt.getObject().toString();

                        } else {

                            currObject = currStmt.getObject().asLiteral().getLexicalForm();

                        }

                        if (!(currStmt.getSubject().isAnon()) &&
                                !(this.classSet.classExist(this.classModel, currStmt.getSubject().toString()))) {

                            if (modelExistInTDB) {

                                if (this.mdbCoreIDNotEmpty
                                        && this.mdbUEIDNotEmpty
                                        && !this.mdbEntryIDNotEmpty) {


                                } else if (this.mdbEntryIDNotEmpty
                                        && this.mdbUEIDNotEmpty) {

                                    IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                                    currSubject = individualURI.createURIForAnIndividual(currSubject, ng, currDirectoryPath, connectionToTDB);

                                } else if (mdbUEIDNotEmpty) {

                                    IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                    currSubject = individualURI.createURIForAnIndividual(currSubject, ng, currDirectoryPath, connectionToTDB);

                                }

                            } else {

                                currSubject = this.entrySpecificAndDefaultResourcesMap.getString(currSubject);

                            }

                        }

                        if (    (!(currStmt.getSubject().isAnon()) &&
                                currStmt.getObject().isURIResource() &&
                                !currStmt.getObject().toString().equals("http://www.w3.org/2002/07/owl#Axiom") &&
                                !currStmt.getObject().toString().equals("http://www.w3.org/2002/07/owl#Class") &&
                                !currStmt.getObject().toString().equals("http://www.w3.org/2002/07/owl#NamedIndividual") &&
                                !(this.classSet.classExist(this.classModel, currStmt.getObject().toString())))
                                || (currStmt.getSubject().isAnon() &&
                                ((currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedSource"))
                                        || (currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedTarget"))))) {


                            if (modelExistInTDB) {

                                if (this.mdbCoreIDNotEmpty
                                        && this.mdbUEIDNotEmpty
                                        && !this.mdbEntryIDNotEmpty) {


                                } else if (this.mdbEntryIDNotEmpty
                                        && this.mdbUEIDNotEmpty) {

                                    if ((currStmt.getSubject().isAnon() &&
                                            ((currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedSource"))
                                                    || (currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedTarget"))))) {

                                        if (currStmt.getObject().isResource()) {

                                            if (!this.classSet.classExist(this.classModel, currStmt.getObject().toString())) {

                                                IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                                                currObject = individualURI.createURIForAnIndividual(currObject, ng, currDirectoryPath, connectionToTDB);

                                            }

                                        }

                                    } else if(currStmt.getObject().isAnon()) {


                                    } else {

                                        IndividualURI individualURI = new IndividualURI(this.mdbEntryID);

                                        currObject = individualURI.createURIForAnIndividual(currObject, ng, currDirectoryPath, connectionToTDB);

                                    }

                                } else if (mdbUEIDNotEmpty) {

                                    if ((currStmt.getSubject().isAnon() &&
                                            ((currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedSource"))
                                                    || (currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedTarget"))))) {

                                        if (currStmt.getObject().isResource()) {

                                            if (!this.classSet.classExist(this.classModel, currStmt.getObject().toString())) {

                                                IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                                currObject = individualURI.createURIForAnIndividual(currObject, ng, currDirectoryPath, connectionToTDB);

                                            }

                                        }

                                    } else {

                                        IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                                        currObject = individualURI.createURIForAnIndividual(currObject, ng, currDirectoryPath, connectionToTDB);

                                    }

                                }

                            } else {

                                if ((currStmt.getSubject().isAnon() &&
                                        ((currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedSource"))
                                                || (currStmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#annotatedTarget"))))) {

                                    if (currStmt.getObject().isResource()) {

                                        if (!this.classSet.classExist(this.classModel, currStmt.getObject().toString())
                                                && this.entrySpecificAndDefaultResourcesMap.has(currObject)) {

                                            currObject = this.entrySpecificAndDefaultResourcesMap.getString(currObject);

                                        }

                                    }

                                } else if(this.entrySpecificAndDefaultResourcesMap.has(currObject)) {

                                    currObject = this.entrySpecificAndDefaultResourcesMap.getString(currObject);

                                }

                            }

                        }

                        currComponentObject.getJSONObject("input_data").append("subject", currSubject);

                        String currProperty = currStmt.getPredicate().toString();

                        currComponentObject.getJSONObject("input_data").append("property", currProperty);

                        String currObjectType = calculateObjectType(currProperty);

                        currComponentObject.getJSONObject("input_data").append("object_data", currObject);

                        currObjectType = calculateObjectTypeForAnnotationProperty(currObject, currObjectType);

                        currComponentObject.getJSONObject("input_data").append("object_type", currObjectType);

                        String currNG = calculateNG(currExecStep, connectionToTDB);

                        currComponentObject.getJSONObject("input_data").append("ng", currNG);

                        currComponentObject.getJSONObject("input_data").append("directory", currDirectoryPath);

                        String currOperation = "s";

                        currComponentObject.getJSONObject("input_data").append("operation", currOperation);

                    }


                }

            }

        }

        return currComponentObject;
    }


    /**
     * This method deletes a named graph from a jena tdb.
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     */
    public void executionStepDeleteAllTriplesOfNamedGraph(JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        String workspace = calculateWorkspaceDirectory(currExecStep);

        String namedGraph = calculateNG(currExecStep, connectionToTDB);

        if (connectionToTDB.modelExistInTDB(workspace, namedGraph)) {

            connectionToTDB.removeNamedModelFromTDB(workspace, namedGraph);

        } else {

            System.out.println("There is no ng in the jena tdb.");

        }

    }

    /**
     * This method extracts a MDB Entry Composition in a new generated named graph
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param connectionToTDB contains a JenaIOTDBFactory object
     */
    public void executionStepExtractAndSaveMDBEntryComposition(JSONArray currExecStep, JSONObject currComponentObject,
                                                          JenaIOTDBFactory connectionToTDB){

        JSONArray classToCheck = new JSONArray();

        String directoryPath = calculateWorkspaceDirectory(currExecStep);

        String ng = calculateNG(currExecStep, connectionToTDB);

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000486")) {
                // composition has root element

                classToCheck.put(currExecStep.getJSONObject(i).getString("object"));

            }

        }

        Model defaultCompositionModel = calculateDefaultEntryComposition(classToCheck, connectionToTDB);

        // save named graph in jena tdb
        System.out.println(connectionToTDB.addModelDataInTDB(directoryPath, ng, defaultCompositionModel));

    }

    /**
     * This method generates resources and provide this data in for a transition. Furthermore some input information for
     * the jena tdb will be generated.
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return input information for a jena tdb
     */
    public JSONObject executionStepGenerateResources(JSONArray currExecStep, JSONObject currComponentObject,
                                                     JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        boolean executeThisStep = true;

        // special case overlay
        if (jsonInputObject.has("mdbcoreid")) {

            if (jsonInputObject.getString("mdbcoreid").equals("http://www.morphdbase.de/resource/dummy-overlay")) {

                executeThisStep = false;

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077")) {
                        // load from/save to/update in named graph


                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000392")) {
                        // load from/save to/update in named graph (individual of)


                    } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                        // load from/save to/update in named graph (this entry's specific individual of) update store [BOOLEAN]

                        if (jsonInputObject.getString("html_form").contains(ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("object")).getLocalName())) {

                            this.createOverlayNG = this.mdbCoreID.substring(0, this.mdbCoreID.indexOf("resource/dummy-overlay")) + jsonInputObject.getString("html_form");

                            this.hasCreateOverlayInput = true;

                            executeThisStep = true;

                            System.out.println("createOverlayNG = " + this.createOverlayNG);

                        }

                    }

                }

            }

        }

        if (executeThisStep) {

            String generateResourceFor = "";

            for (int i = 0; i < currExecStep.length(); i++) {

                Iterator<String> infoInputKeys = this.infoInput.keys();

                boolean continueLoop = true;

                while (infoInputKeys.hasNext() && continueLoop) {

                    String currKey = infoInputKeys.next();

                    if (currKey.equals(currExecStep.getJSONObject(i).getString("object"))) {

                        currExecStep.getJSONObject(i).put("object", this.infoInput.get(currKey));

                        continueLoop = false;

                    }

                }

                if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000185")) {
                    // set new focus on MDB entry ID

                    generateResourceFor = setFocusOnIndividual(currExecStep.getJSONObject(i).getString("object"), currExecStep, jsonInputObject, generateResourceFor, connectionToTDB);

                } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000394")){
                    // set new focus on MDB entry ID (individual of)

                    setFocusOnClass(jsonInputObject, connectionToTDB, currExecStep.getJSONObject(i).getString("object"));

                }

            }

            JSONObject currInputDataObject;

            // check if some input data already exists
            if (!currComponentObject.has("input_data")) {

                currInputDataObject = new JSONObject();

            } else {

                currInputDataObject = currComponentObject.getJSONObject("input_data");

            }

            // use the class as key and the number of individuals as value
            JSONObject numberIndividualsOfClass = new JSONObject();


            if (this.mdbCoreIDNotEmpty
                    && this.mdbUEIDNotEmpty
                    && !this.mdbEntryIDNotEmpty) {

                // sort resources with numbers in string
                OrderDataset orderDataset = new OrderDataset(currExecStep, OntologiesPath.pathToOntology, connectionToTDB);

                // get the sorted object resources
                ArrayList<String> sortedResources = orderDataset.getSortedObjects();

                //System.out.println("sortedResources: " + sortedResources);

                ArrayList<String> sortedPropertyResources = orderDataset.getSortedProperties();

                String currSubject, currProperty, currObject;

                int index = 0;

                String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

                for (String currResource : sortedResources) {

                    if (!currResource.equals(generateResourceFor) &&
                            !currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575")) {
                        // MDB user entry ID
                        // don't calculate for already known individual

                        IndividualURI individualURI = new IndividualURI(this.mdbCoreID);

                        currSubject = individualURI.createURIForAnIndividual(currResource, currDirectoryPath, connectionToTDB);

                    } else if (currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575")) {
                        // MDB user entry ID

                        currSubject = this.mdbUEID;

                    } else {

                        currSubject = this.mdbCoreID;

                    }

                    currProperty = String.valueOf(RDF.type);

                    currObject = currResource;

                    String currObjectType = calculateObjectType(currProperty);

                    String currOperation = "s";

                    if (numberIndividualsOfClass.has(currResource)) {

                        currSubject = currSubject.substring(0, currSubject.lastIndexOf("_") + 1) + (numberIndividualsOfClass.getInt(currResource) + 1);

                        numberIndividualsOfClass.put(currResource, (numberIndividualsOfClass.getInt(currResource) + 1));

                    } else {

                        numberIndividualsOfClass.put(currResource, 1);

                    }

                    currInputDataObject.append("subject", currSubject);
                    currInputDataObject.append("property", currProperty);
                    currInputDataObject.append("object_data", currObject);
                    currInputDataObject.append("object_type", currObjectType);
                    currInputDataObject.append("operation", currOperation);
                    currInputDataObject.append("directory", currDirectoryPath);

                    this.generatedResources.put(sortedPropertyResources.get(index), currSubject);

                    index++;

                }

                // the named graph must calculate afterwards
                String currNG = calculateNG(currExecStep, connectionToTDB);

                for (String currResource : sortedResources) {

                    currInputDataObject.append("ng", currNG);

                }

                currComponentObject.put("input_data", currInputDataObject);

                if (this.hasCreateOverlayInput) {

                    this.hasCreateOverlayInput = false;

                }

                return currComponentObject;


            } else if (this.mdbEntryIDNotEmpty
                    && this.mdbUEIDNotEmpty) {

                System.out.println("case entry ID");

                // sort resources with numbers in string
                OrderDataset orderDataset = new OrderDataset(currExecStep, OntologiesPath.pathToOntology, connectionToTDB);

                // get the sorted object resources
                ArrayList<String> sortedObjectResources = orderDataset.getSortedObjects();

                //System.out.println("sortedObjectResources: " + sortedObjectResources);

                ArrayList<String> sortedPropertyResources = orderDataset.getSortedProperties();

                String currSubject, currProperty, currObject;

                int index = 0;

                String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

                for (String currResource : sortedObjectResources) {

                    //System.out.println("numberIndividualsOfClass = " + numberIndividualsOfClass);

                    if (!currResource.equals(generateResourceFor)&&
                            !currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575") &&
                            // MDB user entry ID
                            !currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDBEntry#MDB_ENTRY_0000000029") &&
                            // MDB core ID
                            !currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                        // KEYWORD: empty

                        // don't calculate for already known individual

                        IndividualURI individualURI = new IndividualURI(this.currentFocus);

                        if (this.focusHasNewNS) {

                            currSubject = individualURI.createURIForAnIndividualForANewNamespace(currResource);

                        } else {

                            currSubject = individualURI.createURIForAnIndividual(currResource, currDirectoryPath, connectionToTDB);

                        }

                        if (numberIndividualsOfClass.has(currResource)) {

                            currSubject = currSubject.substring(0, currSubject.lastIndexOf("_") + 1) + (numberIndividualsOfClass.getInt(currResource) + 1);

                            numberIndividualsOfClass.put(currResource, (numberIndividualsOfClass.getInt(currResource) + 1));

                        } else {

                            numberIndividualsOfClass.put(currResource, 1);

                        }

                    } else if (currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDBEntry#MDB_ENTRY_0000000029")) {
                        // MDB core ID

                        currSubject = this.mdbCoreID;

                    } else if (currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575")) {
                        // MDB user entry ID

                        currSubject = this.mdbUEID;

                    } else if (currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                        // KEYWORD: empty

                        currSubject = currResource;

                    } else {

                        currSubject = this.mdbEntryID;

                    }

                    currProperty = String.valueOf(RDF.type);

                    currObject = currResource;

                    String currObjectType = calculateObjectType(currProperty);

                    String currOperation = "s";

                    currInputDataObject.append("subject", currSubject);
                    currInputDataObject.append("property", currProperty);
                    currInputDataObject.append("object_data", currObject);
                    currInputDataObject.append("object_type", currObjectType);
                    currInputDataObject.append("operation", currOperation);
                    currInputDataObject.append("directory", currDirectoryPath);

                    this.generatedResources.put(sortedPropertyResources.get(index), currSubject);

                    index++;

                }

                System.out.println("generatedResources = " + this.generatedResources);

                // the named graph must calculate afterwards
                String currNG = calculateNG(currExecStep, connectionToTDB);

                for (String currResource : sortedObjectResources) {

                    currInputDataObject.append("ng", currNG);

                }

                currComponentObject.put("input_data", currInputDataObject);

                if (this.hasCreateOverlayInput) {

                    this.hasCreateOverlayInput = false;

                }

                return currComponentObject;


            } else if (mdbUEIDNotEmpty) {

                // sort resources with numbers in string
                OrderDataset orderDataset = new OrderDataset(currExecStep, OntologiesPath.pathToOntology, connectionToTDB);

                // get the sorted object resources
                ArrayList<String> sortedResources = orderDataset.getSortedObjects();

                ArrayList<String> sortedPropertyResources = orderDataset.getSortedProperties();

                String currSubject, currProperty, currObject;

                int index = 0;

                String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

                for (String currResource : sortedResources) {

                    if (!currResource.equals(generateResourceFor) &&
                            !currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                        // KEYWORD: empty

                        // don't calculate for already known individual

                        IndividualURI individualURI = new IndividualURI(this.mdbUEID);

                        currSubject = individualURI.createURIForAnIndividual(currResource, currDirectoryPath, connectionToTDB);

                    } else if (currResource.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                        // KEYWORD: empty

                        currSubject = currResource;

                    } else {

                        currSubject = this.mdbUEID;

                    }

                    currProperty = String.valueOf(RDF.type);

                    currObject = currResource;

                    String currObjectType = calculateObjectType(currProperty);

                    String currOperation = "s";

                    if (numberIndividualsOfClass.has(currResource)) {

                        currSubject = currSubject.substring(0, currSubject.lastIndexOf("_") + 1) + (numberIndividualsOfClass.getInt(currResource) + 1);

                        numberIndividualsOfClass.put(currResource, (numberIndividualsOfClass.getInt(currResource) + 1));

                    } else {

                        numberIndividualsOfClass.put(currResource, 1);

                    }

                    currInputDataObject.append("subject", currSubject);
                    currInputDataObject.append("property", currProperty);
                    currInputDataObject.append("object_data", currObject);
                    currInputDataObject.append("object_type", currObjectType);
                    currInputDataObject.append("operation", currOperation);
                    currInputDataObject.append("directory", currDirectoryPath);

                    this.generatedResources.put(sortedPropertyResources.get(index), currSubject);

                    index++;

                }

                //System.out.println("generatedResources = " + generatedResources);

                // the named graph must calculate afterwards
                String currNG = calculateNG(currExecStep, connectionToTDB);

                for (String currResource : sortedResources) {

                    currInputDataObject.append("ng", currNG);

                }

                currComponentObject.put("input_data", currInputDataObject);

                if (this.hasCreateOverlayInput) {

                    this.hasCreateOverlayInput = false;

                }

                return currComponentObject;

            }

        }

        if (this.hasCreateOverlayInput) {

            this.hasCreateOverlayInput = false;

        }

        return currComponentObject;

    }


    /**
     * This method choose the if operation and the values for this operation from the execution step and calculate a
     * result for this operation.
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @return the next execution step or a final resource
     */
    public String executionStepIfThenElseStatement(JSONObject jsonInputObject, JSONArray currExecStep,
                                                   JenaIOTDBFactory connectionToTDB) {

        String ifOperation = "";

        for (int j = 0; j < currExecStep.length(); j++) {

            if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000284")) {
                // get the if operation from the ontology

                ifOperation = currExecStep.getJSONObject(j).getString("object");

            }

        }

        ArrayList<String> inputValues = new ArrayList<>();

        if (!ifOperation.isEmpty()) {

            System.out.println("ifOperation: " + ifOperation);

            for (int j = 0; j < currExecStep.length(); j++) {

                if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000283")) {
                    // has IF input value
                    // get the if input values from the ontology

                    Resource inputValueRes = ResourceFactory.createResource(currExecStep.getJSONObject(j).getString("object"));

                    if (inputValueRes.toString().equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000415")) {
                        // KEYWORD: number of active MDB sessions of this user

                        if (this.mdbUEIDNotEmpty) {

                            // get the number of interval start date from store

                            String timeIntervalURIWithoutNumber = this.mdbUEID + "#TimeInterval";

                            SelectBuilder selectWhereBuilder = new SelectBuilder();

                            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                            selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                            selectWhereBuilder.addWhere("?s", "<http://www.ontologydesignpatterns.org/cp/owl/timeinterval.owl#hasIntervalStartDate>","?o");

                            FilterBuilder filterBuilder = new FilterBuilder();

                            SPARQLFilter sparqlFilter = new SPARQLFilter();

                            // create an array list to collect the filter parts
                            ArrayList<String> filterCollection= new ArrayList<>();

                            // add a part to the collection
                            filterCollection.add(timeIntervalURIWithoutNumber);

                            // generate a filter string
                            ArrayList<String> filter = sparqlFilter.getRegexSTRFilter("?s", filterCollection);

                            selectWhereBuilder = filterBuilder.addFilter(selectWhereBuilder, filter);

                            SelectBuilder selectBuilder = new SelectBuilder();

                            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                            ExprVar exprVar = new ExprVar("s");

                            Aggregator aggregator = AggregatorFactory.createCountExpr(true, exprVar.getExpr());

                            ExprAggregator exprAggregator = new ExprAggregator(exprVar.asVar(), aggregator);

                            selectBuilder.addVar(exprAggregator.getExpr(), "?count");

                            selectBuilder.addGraph("?g", selectWhereBuilder);

                            String sparqlQueryString = selectBuilder.buildString();

                            TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                            String numberOfStartDate = connectionToTDB
                                    .pullSingleDataFromTDB(
                                            tdbPath
                                                    .getPathToTDB(
                                                            "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"),
                                            sparqlQueryString,
                                            "?count");

                            // get the number of interval end date from store

                            selectWhereBuilder = new SelectBuilder();

                            selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                            selectWhereBuilder.addWhere("?s", "<http://www.ontologydesignpatterns.org/cp/owl/timeinterval.owl#hasIntervalEndDate>","?o");

                            filterBuilder = new FilterBuilder();

                            sparqlFilter = new SPARQLFilter();

                            // create an array list to collect the filter parts
                            filterCollection= new ArrayList<>();

                            // add a part to the collection
                            filterCollection.add(timeIntervalURIWithoutNumber);

                            // generate a filter string
                            filter = sparqlFilter.getRegexSTRFilter("?s", filterCollection);

                            selectWhereBuilder = filterBuilder.addFilter(selectWhereBuilder, filter);


                            selectBuilder = new SelectBuilder();

                            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                            selectBuilder.addVar(exprAggregator.getExpr(), "?count");

                            selectBuilder.addGraph("?g", selectWhereBuilder);

                            sparqlQueryString = selectBuilder.buildString();

                            String numberOfEndDate = connectionToTDB
                                    .pullSingleDataFromTDB(
                                            tdbPath
                                                    .getPathToTDB(
                                                            "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"),
                                            sparqlQueryString,
                                            "?count");

                            System.out.println("inputValues: " + (Integer.parseInt(numberOfStartDate) - Integer.parseInt(numberOfEndDate)));

                            // calculate input value(input = #start - #end)
                            inputValues.add(String.valueOf(Integer.parseInt(numberOfStartDate) - Integer.parseInt(numberOfEndDate)));

                        }


                    } else if (inputValueRes.toString().equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000404")) {
                        // KEYWORD: user input

                        inputValues.add(jsonInputObject.getString("value"));
                        System.out.println("inputValues: " + jsonInputObject.getString("value"));

                    } else if (this.infoInput.has(inputValueRes.toString())) {
                        // use info input as value

                        if ((this.infoInput.getString(inputValueRes.toString()).equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423"))) {
                            // KEYWORD: empty

                            return getNextStepFromJSONArray(currExecStep, "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000098");

                        } else {

                            inputValues.add(this.infoInput.getString(inputValueRes.toString()));
                            System.out.println("inputValues: " + this.infoInput.getString(inputValueRes.toString()));

                        }

                    } else if (this.identifiedResources.has(inputValueRes.toString())) {
                        // associated keyword from tdb

                        inputValues.add(this.identifiedResources.getString(inputValueRes.toString()));
                        System.out.println("inputValues: " + this.identifiedResources.getString(inputValueRes.toString()));

                    } else {
                        // use user input as value

                        JSONArray arrayToCheck = jsonInputObject.getJSONArray("localIDs");

                        for (int k = 0; k < arrayToCheck.length(); k++) {

                            if (arrayToCheck.getJSONObject(k).has("keyword")) {

                                // check if current value Resource
                                if ((arrayToCheck.getJSONObject(k).getString("keyword").equals(inputValueRes.getLocalName()) &&
                                        jsonInputObject.getString("localID").equals(arrayToCheck.getJSONObject(k).getString("localID"))) ||
                                        ((arrayToCheck.getJSONObject(k).getString("keyword").equals(inputValueRes.getLocalName()) &&
                                                jsonInputObject.getString("html_form").equals("Ontologies/GUIComponent#GUI_COMPONENT_0000000143")))) {
                                    // add the current value for the if operation

                                    inputValues.add(arrayToCheck.getJSONObject(k).getString("value"));

                                    System.out.println("inputValues: " + arrayToCheck.getJSONObject(k).getString("value"));

                                }

                            }

                        }

                    }

                }

            }
        }

        ArrayList<String> targetValues = new ArrayList<>();

        for (int j = 0; j < currExecStep.length(); j++) {

            if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000285")) {
                // has IF target value

                String potentialTarget = currExecStep.getJSONObject(j).getString("object");

                if (potentialTarget.contains("__MDB_UIAP_")) {

                    String localNamePropertyInObject = potentialTarget.substring(potentialTarget.indexOf("__") + 2);

                    Iterator<String> genResIterator = this.generatedResources.keys();

                    while (genResIterator.hasNext()) {

                        String currKey = genResIterator.next();

                        // get local name of a key
                        String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                        if (localNameOfKey.equals(localNamePropertyInObject)) {
                            // get already generated resource from cache

                            potentialTarget = this.generatedResources.getString(currKey);

                        }

                    }

                    if (jsonInputObject.has("localIDs")) {

                        JSONArray currJSONArray = jsonInputObject.getJSONArray("localIDs");

                        for (int k = 0; k < currJSONArray.length(); k++) {

                            JSONObject currJSONObject = currJSONArray.getJSONObject(k);

                            if (currJSONObject.has("keyword")) {

                                if (ResourceFactory.createResource(potentialTarget).getLocalName().equals(currJSONObject.getString("keyword")) &&
                                        jsonInputObject.getString("localID").equals(currJSONObject.getString("localID"))) {

                                    potentialTarget = currJSONObject.getString("value");

                                }

                            }

                        }

                    }

                    if (this.identifiedResources.has(potentialTarget)) {

                        potentialTarget = this.identifiedResources.getString(potentialTarget);

                    }

                    targetValues.add(potentialTarget);

                } else {

                    targetValues.add(potentialTarget);

                }

                System.out.println("targetValues: " + potentialTarget);

            }

        }

        MDBIfThenElse mdbIfThenElse = new MDBIfThenElse();

        boolean ifDecision = mdbIfThenElse.checkCondition(ifOperation, inputValues, targetValues, connectionToTDB);

        if (ifDecision) {
            // case ifDecision is true

            return getNextStepFromJSONArray(currExecStep, "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000097");

        } else {
            // case ifDecision is false

            return getNextStepFromJSONArray(currExecStep, "http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000098");

        }

    }


    /**
     * This method add the information for a new hyperlink of a current object
     * @param currComponentObject contains information about the current object
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return additional modified information about the current object
     */
    public JSONObject executionStepMDBHyperlink(JSONObject currComponentObject, JSONObject jsonInputObject,
                                                JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        System.out.println("Hyperlink case");

        String ng = "", directory = "", selectedPart = "", switchToPageURI = "", switchToOverlayURI = "";
        Boolean switchToPageExist = false, switchToOverlayExist = false, hasSelectedPartExist = false;

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                // load from/save to/update in named graph (this entry's specific individual of)

                ng = calculateNG(currExecStep, connectionToTDB);

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000078")) {
                // named graph belongs to workspace

                directory = calculateWorkspaceDirectory(currExecStep);

            } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000528")) {
                // has selected part (this entry's specific individual of class)

                hasSelectedPartExist = true;
                selectedPart = currExecStep.getJSONObject(i).getString("object");

            } else if((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000261")) {
                // switch to page

                switchToPageExist = true;
                switchToPageURI = currExecStep.getJSONObject(i).getString("object");

            } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000412")) {
                // switch to overlay

                switchToOverlayExist = true;
                switchToOverlayURI = currExecStep.getJSONObject(i).getString("object");

            } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000387")) {
                // MDB info-message

                currComponentObject.put(ResourceFactory.createProperty(currExecStep.getJSONObject(i).getString("property")).getLocalName(), ResourceFactory.createPlainLiteral(currExecStep.getJSONObject(i).getString("object")).asLiteral().getLexicalForm());

            }

        }

        if (currComponentObject.has("compositionForMDBHyperlink")) { // todo change key to "data" at a later point

            if (switchToPageExist) {
                // switch to page

                currComponentObject.put("data", currComponentObject.getJSONArray("compositionForMDBHyperlink"));

                currComponentObject.remove("compositionForMDBHyperlink");

                String potentialURL;

                if (ResourceFactory.createResource(switchToPageURI).isResource()
                        && !(this.currentFocus.equals(""))) {

                    String localName = ResourceFactory.createResource(switchToPageURI).getLocalName();

                    potentialURL = this.currentFocus + "#" + localName;

                } else {

                    potentialURL = switchToPageURI;

                }

                currComponentObject.put("load_page", potentialURL);

                try {

                    URL url = new URL(potentialURL);

                    String loadPageLocalID = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

                    currComponentObject.put("load_page_localID", loadPageLocalID);

                } catch (MalformedURLException e) {

                    System.out.println("INFO: the variable 'potentialURL' contains no valid URL.");

                }

                if (hasSelectedPartExist) {

                    String resultVarLabel = "?s";

                    PrefixesBuilder prefixesBuilderLabel = new PrefixesBuilder();

                    SelectBuilder selectBuilderLabel = new SelectBuilder();

                    selectBuilderLabel = prefixesBuilderLabel.addPrefixes(selectBuilderLabel);

                    SelectBuilder tripleSPOConstructLabel = new SelectBuilder();

                    tripleSPOConstructLabel.addWhere( "?s", RDF.type, "<" + selectedPart + ">");

                    selectBuilderLabel.addVar(selectBuilderLabel.makeVar(resultVarLabel));

                    selectBuilderLabel.addGraph("<" + ng + ">", tripleSPOConstructLabel);

                    String sparqlQueryStringLabel = selectBuilderLabel.buildString();

                    String partID = connectionToTDB.pullSingleDataFromTDB(directory, sparqlQueryStringLabel, resultVarLabel);

                    currComponentObject.put("partID", ResourceFactory.createResource(partID).getLocalName());

                }

                OutputGenerator outputGenerator = new OutputGenerator();

                outputGenerator.getOutputJSONObject(currComponentObject.getString("load_page_localID"), jsonInputObject, currComponentObject.getJSONArray("data"));

            } else if (switchToOverlayExist) {
                // switch to overlay

                currComponentObject.put("data", currComponentObject.getJSONArray("compositionForMDBHyperlink"));

                currComponentObject.remove("compositionForMDBHyperlink");

                currComponentObject.put("widget", switchToOverlayURI);

                TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                // count value in jena tdb
                SelectBuilder selectWhereBuilder = new SelectBuilder();

                selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                selectWhereBuilder.addWhere("<http://www.morphdbase.de/resource/dummy-overlay#" + ResourceFactory.createResource(switchToOverlayURI).getLocalName() + ">", RDF.value, "?o");

                SelectBuilder selectBuilder = new SelectBuilder();

                selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                selectBuilder.addVar(selectBuilder.makeVar("?o"));

                selectBuilder.addGraph("<http://www.morphdbase.de/resource/dummy-overlay#" + ResourceFactory.createResource(switchToOverlayURI).getLocalName() + ">", selectWhereBuilder);

                String sparqlQueryString = selectBuilder.buildString();

                String result = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"), sparqlQueryString, "?o");

                String overlayNG;

                if (result.isEmpty()) {

                    overlayNG = "http://www.morphdbase.de/resource/dummy-overlay-" + ResourceFactory.createResource(switchToOverlayURI).getLocalName() + "_1#MDB_CORE_0000000412_1";

                } else {

                    int newResourceIndex = Integer.parseInt(result) + 1;

                    overlayNG = "http://www.morphdbase.de/resource/dummy-overlay-" + ResourceFactory.createResource(switchToOverlayURI).getLocalName() + "_" + newResourceIndex + "#MDB_CORE_0000000412_1";

                }

                currComponentObject.put("load_overlay", overlayNG);

                String loadOverlayLocalID = "";

                try {

                    URL url = new URL(overlayNG);

                    loadOverlayLocalID = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

                } catch (MalformedURLException e) {

                    System.out.println("INFO: the variable 'potentialURL' contains no valid URL.");

                }

                currComponentObject.put("load_overlay_localID", loadOverlayLocalID);

                OutputGenerator outputGenerator = new OutputGenerator();

                outputGenerator.getOutputJSONObject(currComponentObject.getString("load_overlay_localID"), jsonInputObject, currComponentObject.getJSONArray("data"));

            }

        } else {

            OperationManager operationManager;

            if (this.mdbCoreIDNotEmpty && this.mdbEntryIDNotEmpty && this.mdbUEIDNotEmpty) {

                operationManager = new OperationManager(this.mdbCoreID, this.mdbEntryID, this.mdbUEID);

            } else if(this.mdbUEIDNotEmpty) {

                operationManager = new OperationManager(this.mdbUEID);

            } else {

                operationManager = new OperationManager();

            }

            boolean saveToStoreExist = false;
            boolean switchToEntryExist = false;
            switchToOverlayExist = false;
            String classID = "";
            String localClassID = "";
            String entryID = "";
            String entryIDRaw = "";

            for (int j = 0; j < currExecStep.length(); j++) {

                if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000261")) {
                    // switch to page

                    classID = currExecStep.getJSONObject(j).getString("object");

                    // add output message to currComponentObject
                    localClassID = ResourceFactory.createResource(classID).getLocalName();

                    for (int k = 0; k < currExecStep.length(); k++) {

                        if ((currExecStep.getJSONObject(k).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000389")) {
                            // switch to entry

                            entryID = currExecStep.getJSONObject(k).getString("object");
                            entryIDRaw = entryID;

                            entryID = entryID.substring(entryID.indexOf("__") + 2);

                            Iterator<String> keyIterator = this.generatedResources.keys();

                            while (keyIterator.hasNext()) {

                                String currKey = keyIterator.next();

                                // get local name of a key
                                String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                                if (localNameOfKey.equals(entryID)) {
                                    // get ng from generated resources

                                    entryID = this.generatedResources.getString(currKey);

                                }

                            }

                            switchToEntryExist = true;

                        }

                    }



                } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000412")) {
                    // switch to overlay

                    if ((!currExecStep.getJSONObject(j).getString("object").contains(mdbCoreID)) &&
                            (!currExecStep.getJSONObject(j).getString("object").contains(mdbEntryID)) &&
                            (!currExecStep.getJSONObject(j).getString("object").contains(mdbUEID))) {

                        currComponentObject.put("widget", ResourceFactory.createResource("http://www.morphdbase.de/Ontologies/GUIComponent#GUI_COMPONENT_0000000185").getLocalName());

                    }

                    classID = currExecStep.getJSONObject(j).getString("object");

                    for (int k = 0; k < currExecStep.length(); k++) {

                        if ((currExecStep.getJSONObject(k).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000389")) {
                            // switch to entry

                            entryID = currExecStep.getJSONObject(k).getString("object");

                            entryIDRaw = entryID;

                            entryID = entryID.substring(entryID.indexOf("__") + 2);

                            Iterator<String> keyIterator = this.generatedResources.keys();

                            while (keyIterator.hasNext()) {

                                String currKey = keyIterator.next();

                                // get local name of a key
                                String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                                if (localNameOfKey.equals(entryID)) {
                                    // get ng from generated resources

                                    entryID = this.generatedResources.getString(currKey);

                                }

                            }

                            switchToEntryExist = true;

                            switchToOverlayExist = true;

                        } else if ((currExecStep.getJSONObject(k).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000448")) {
                            // switch to MDB entry component (this entry's specific individual of)

                            classID = currExecStep.getJSONObject(k).getString("object");

                            // add output message to currComponentObject
                            localClassID = ResourceFactory.createResource(classID).getLocalName();

                        } else if ((currExecStep.getJSONObject(k).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000489")) {
                            // switch to composition with root MDB entry component

                            switchToOverlayExist = true;

                        }

                    }

                } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000331")) {
                    // close module [BOOLEAN]

                    currComponentObject.put("close_old_page", ResourceFactory.createPlainLiteral(currExecStep.getJSONObject(j).getString("object")).asLiteral().getLexicalForm());

                } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000390")) {
                    // update store [BOOLEAN]

                    saveToStoreExist = true;

                } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000387")) {
                    // MDB info-message

                    currComponentObject.put(ResourceFactory.createProperty(currExecStep.getJSONObject(j).getString("property")).getLocalName(), ResourceFactory.createPlainLiteral(currExecStep.getJSONObject(j).getString("object")).asLiteral().getLexicalForm());

                } else if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000424")) {
                    // keyword value transferred to hyperlink

                    String keywordValueToTransfer = currExecStep.getJSONObject(j).getString("object");

                    if (keywordValueToTransfer.contains("__MDB_UIAP_")) {

                        keywordValueToTransfer = keywordValueToTransfer.substring(keywordValueToTransfer.indexOf("__") + 2);

                        Iterator<String> keyIterator = this.generatedResources.keys();

                        while (keyIterator.hasNext()) {

                            String currKey = keyIterator.next();

                            // get local name of a key
                            String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                            if (localNameOfKey.equals(keywordValueToTransfer)) {
                                // get ng from generated resources

                                currComponentObject.append("ngInCache", this.generatedResources.getString(currKey));

                            }

                        }

                    } else {

                        currComponentObject.append("ngInCache", currExecStep.getJSONObject(j).getString("object"));

                    }

                }

            }

            if (saveToStoreExist) {

                //System.out.println("currComponentObject before save store" + currComponentObject);

                saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

            }

            if (switchToEntryExist) {

                System.out.println("switchToEntryExist = " + true);

                if (jsonInputObject.has("mdbueid") && (!this.mdbUEIDNotEmpty)) {
                    // put MDBUEID from jena tdb to cache if not already exist

                    FilterBuilder filterBuilder = new FilterBuilder();

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder innerSelect = new SelectBuilder();

                    innerSelect.addWhere("?s", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<http://www.morphdbase.de/Ontologies/MDB/MDBAgent#MDB_AGENT_0000000009>");
                    // MDB_UIAP_0000000402 >>> value defined through property

                    selectBuilder.addVar(selectBuilder.makeVar("?s"));

                    selectBuilder.addGraph("?g", innerSelect);

                    ArrayList<String> oneDimensionalFilterItems = new ArrayList<>();

                    oneDimensionalFilterItems.add(jsonInputObject.getString("mdbueid"));

                    SPARQLFilter sparqlFilter = new SPARQLFilter();

                    ArrayList<String> filter = sparqlFilter.getRegexSTRFilter("?s", oneDimensionalFilterItems);

                    selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                    String sparqlQueryString = selectBuilder.buildString();

                    TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                    this.mdbUEID = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), sparqlQueryString, "?s");

                    this.mdbUEIDNotEmpty = true;

                }

                if ((entryID.contains(this.mdbEntryID) && this.mdbEntryIDNotEmpty) ||
                        (entryIDRaw.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000114") && this.mdbEntryIDNotEmpty)) {

                    if (switchToOverlayExist) {

                        FilterBuilder filterBuilder = new FilterBuilder();

                        SelectBuilder selectBuilder = new SelectBuilder();

                        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                        SelectBuilder tripleSPO = new SelectBuilder();

                        tripleSPO.addWhere("?s", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<" + classID + ">");

                        selectBuilder.addVar(selectBuilder.makeVar("?s"));

                        selectBuilder.addGraph("?g", tripleSPO);

                        SPARQLFilter sparqlFilter = new SPARQLFilter();

                        ArrayList<String> filterItems = new ArrayList<>();

                        filterItems.add(this.mdbEntryID);

                        ArrayList<String> filter = sparqlFilter.getRegexSTRFilter("?s", filterItems);

                        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                        String sparqlQueryString = selectBuilder.buildString();

                        System.out.println(sparqlQueryString);

                        TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                        System.out.println();

                        String rootURI = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"), sparqlQueryString, "?s");
                        // MDB_WORKSPACE_DIRECTORY: MDB draft workspace directory

                        System.out.println("rootURI = " + rootURI);

                        // set path to draft workspace
                        operationManager.setPathToOntologies(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"));

                        currComponentObject = operationManager.getOutput(jsonInputObject, currComponentObject, rootURI, connectionToTDB);

                        currComponentObject.put("load_overlay", rootURI);

                        currComponentObject.put("load_overlay_localID", ResourceFactory.createResource(rootURI).getLocalName());

                    } else {

                        TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

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

                        filterItems = filterBuilder.addItems(filterItems, "?p", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
                        filterItems = filterBuilder.addItems(filterItems, "?o", "<" + classID + ">");

                        ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

                        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                        filterItems.clear();

                        // create an array list to collect the filter parts
                        ArrayList<String> filterCollection = new ArrayList<>();

                        filterCollection.add(this.mdbEntryID);

                        // generate a filter string
                        filter = sparqlFilter.getRegexSTRFilter("?s", filterCollection);

                        selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                        String sparqlQueryString = selectBuilder.buildString();

                        System.out.println(sparqlQueryString);

                        String rootURI = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503"), sparqlQueryString, "?s");

                        String pathForOperationManager = tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503");

                        if (rootURI.isEmpty()) {

                            rootURI = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494"), sparqlQueryString, "?s");
                            // MDB_WORKSPACE_DIRECTORY: MDB core workspace directory

                            pathForOperationManager = tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494");

                        }

                        System.out.println("rootURI = " + rootURI);

                        // set path to admin workspace
                        operationManager.setPathToOntologies(pathForOperationManager);

                        currComponentObject = operationManager.getOutput(jsonInputObject, currComponentObject, rootURI, connectionToTDB);

                        currComponentObject.put("load_page", rootURI);

                        if (rootURI.contains("http://www.morphdbase.de/resource/")) {

                            try {

                                URL url = new URL(rootURI);

                                String loadPageLocalID = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

                                currComponentObject.put("load_page_localID", loadPageLocalID);

                            } catch (MalformedURLException e) {

                                System.out.println("INFO: the variable 'potentialURL' contains no valid URL.");

                            }

                        } else {

                            currComponentObject.put("load_page_localID", ResourceFactory.createResource(rootURI).getLocalName());

                        }

                    }


                } else if (entryID.contains(this.mdbCoreID) && this.mdbCoreIDNotEmpty) {

                    //System.out.println("mdbCoreID case");

                } else if ((entryID.contains(this.mdbUEID) && this.mdbUEIDNotEmpty) ||
                        entryIDRaw.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000330")) {
                    // KEYWORD: this MDB user entry ID

                /*System.out.println();
                System.out.println("switchToPage = " + classID);
                System.out.println("switchToEntry = " + entryID);
                System.out.println();*/


                    TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

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

                    filterItems = filterBuilder.addItems(filterItems, "?p", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
                    filterItems = filterBuilder.addItems(filterItems, "?o", "<http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000703>");

                    ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

                    selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                    filterItems.clear();

                    // create an array list to collect the filter parts
                    ArrayList<String> filterCollection= new ArrayList<>();

                    filterCollection.add(this.mdbUEID);

                    // generate a filter string
                    filter = sparqlFilter.getRegexSTRFilter("?s", filterCollection);

                    selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                    String sparqlQueryString = selectBuilder.buildString();

                    System.out.println(sparqlQueryString);

                    String rootURI = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), sparqlQueryString, "?s");

                    System.out.println("rootURI = " + rootURI);

                    // set path to admin workspace
                    operationManager.setPathToOntologies(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"));

                    currComponentObject = operationManager.getOutput(jsonInputObject, currComponentObject, rootURI, connectionToTDB);

                    currComponentObject.put("load_page", rootURI);

                    if (rootURI.contains("http://www.morphdbase.de/resource/")) {

                        try {

                            URL url = new URL(rootURI);

                            String loadPageLocalID = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

                            currComponentObject.put("load_page_localID", loadPageLocalID);

                        } catch (MalformedURLException e) {

                            System.out.println("INFO: the variable 'potentialURL' contains no valid URL.");

                        }

                    } else {

                        currComponentObject.put("load_page_localID", ResourceFactory.createResource(rootURI).getLocalName());

                    }

                }

            } else {

                String originalLocalID = jsonInputObject.getString("localID");

                jsonInputObject.put("localID", localClassID);

                System.out.println("jsonToCalculate = " + jsonInputObject);

                operationManager.setPathToOntologies(OntologiesPath.pathToOntology);

                if (!classID.equals("http://www.morphdbase.de")) {

                    currComponentObject = operationManager.getOutput(jsonInputObject, currComponentObject, connectionToTDB);

                }

                jsonInputObject.put("localID", originalLocalID);

                currComponentObject.put("load_page", classID);

                if (classID.contains("http://www.morphdbase.de/resource/")) {

                    try {

                        URL url = new URL(classID);

                        String loadPageLocalID = url.getPath().substring(1, url.getPath().length()) + "#" + url.getRef();

                        currComponentObject.put("load_page_localID", loadPageLocalID);

                    } catch (MalformedURLException e) {

                        System.out.println("INFO: the variable 'potentialURL' contains no valid URL.");

                    }

                } else {

                    currComponentObject.put("load_page_localID", ResourceFactory.createResource(classID).getLocalName());

                }

            }

        }

        return currComponentObject;

    }


    /**
     * This method executes a mdb operation, e.g. "save individual in cookie"
     * @param currComponentObject contains information about the current object
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @return the modified currComponentObject
     */
    public JSONObject executionStepMDBOperation(JSONObject currComponentObject, JSONObject jsonInputObject,
                                                JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        boolean saveToStoreExist = false;

        for (int i = 0; i < currExecStep.length(); i++) {

            String key = "";

            for (int j = 0; j < currExecStep.length(); j++) {

                if ((currExecStep.getJSONObject(j).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000411")) {
                    // MDB operation 'save in cookie as key'

                    key = currExecStep.getJSONObject(j).getString("object");

                }

            }


            if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000396")) {
                // MDB operation 'save individual in cookie'

                String localNamePropertyInObject = currExecStep.getJSONObject(i).getString("object");

                if (localNamePropertyInObject.contains("__MDB_UIAP_")) {

                    localNamePropertyInObject = localNamePropertyInObject.substring(localNamePropertyInObject.indexOf("__") + 2);

                    Iterator<String> keyIterator = this.generatedResources.keys();

                    while (keyIterator.hasNext()) {

                        String currKey = keyIterator.next();

                        // get local name of a key
                        String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                        if (localNameOfKey.equals(localNamePropertyInObject)) {
                            // get already generated resource from cache

                            currComponentObject.put(key, this.generatedResources.getString(currKey));

                        }

                    }

                }

            } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000390")) {
                // update store [BOOLEAN]

                saveToStoreExist = true;

            }

        }

        if (saveToStoreExist) {

            //System.out.println("currComponentObject before save store" + currComponentObject);

            saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

        }


        return currComponentObject;

    }


    /**
     * This method generate new triples. This triples must be save in a jena tdb
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return input information for a jena tdb
     */
    public JSONObject executionStepSaveDeleteTripleStatements(JSONArray currExecStep, JSONObject currComponentObject,
                                                              JSONObject jsonInputObject,
                                                              JenaIOTDBFactory connectionToTDB) {

        boolean executeThisStep = true;

        if (jsonInputObject.has("mdbcoreid")) {

            if (jsonInputObject.getString("mdbcoreid").equals("http://www.morphdbase.de/resource/dummy-overlay")) {

                executeThisStep = false;

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077")) {
                        // load from/save to/update in named graph


                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000392")) {
                        // load from/save to/update in named graph (individual of)


                    } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                        // load from/save to/update in named graph (this entry's specific individual of) update store [BOOLEAN]

                        if (jsonInputObject.getString("html_form").contains(ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("object")).getLocalName())) {

                            this.createOverlayNG = this.mdbCoreID.substring(0, this.mdbCoreID.indexOf("resource/dummy-overlay")) + jsonInputObject.getString("html_form");

                            this.hasCreateOverlayInput = true;

                            executeThisStep = true;

                            System.out.println("createOverlayNG = " + this.createOverlayNG);

                        }

                    }

                }

            }

        }

        if (executeThisStep) {

            for (int i = 0; i < currExecStep.length(); i++) {

                if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000394")) {
                    // set new focus on MDB entry ID (individual of)

                    setFocusOnClass(jsonInputObject, connectionToTDB, currExecStep.getJSONObject(i).getString("object"));

                }

                // check if generated resource is empty
                if (currExecStep.getJSONObject(i).getString("object").contains("__MDB_UIAP_")) {

                    String localNamePropertyInObject = currExecStep.getJSONObject(i).getString("object").substring(currExecStep.getJSONObject(i).getString("object").indexOf("__") + 2);

                    Iterator<String> genResIterator = this.generatedResources.keys();

                    while (genResIterator.hasNext()) {

                        String currKey = genResIterator.next();

                        // get local name of a key
                        String localNameOfKey = ResourceFactory.createResource(currKey).getLocalName();

                        if (localNameOfKey.equals(localNamePropertyInObject)) {
                            // get already generated resource from cache

                            if (this.generatedResources.getString(currKey).equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                                // KEYWORD: empty

                                System.out.println("generated resource " + currKey + " is empty!");

                                if (this.hasCreateOverlayInput) {

                                    this.hasCreateOverlayInput = false;

                                }

                                // if empty skip
                                return currComponentObject;

                            }

                        }

                    }

                }

            }

            JSONObject dataToFindObjectInTDB = new JSONObject();


            if (!currComponentObject.has("input_data")) {
                // no other statement was generated yet

                JSONObject currInputDataObject = new JSONObject();

                String currSubject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                currInputDataObject.append("subject", currSubject);

                String currProperty = calculateProperty(currExecStep);

                currInputDataObject.append("property", currProperty);

                String currNG = calculateNG(currExecStep, connectionToTDB);

                currInputDataObject.append("ng", currNG);

                String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

                currInputDataObject.append("directory", currDirectoryPath);

                String currObjectType = calculateObjectType(currProperty);

                dataToFindObjectInTDB.put("subject", currSubject);
                dataToFindObjectInTDB.put("property", currProperty);
                dataToFindObjectInTDB.put("ng", currNG);
                dataToFindObjectInTDB.put("directory", currDirectoryPath);

                String currObject = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, currObjectType, connectionToTDB);

                currInputDataObject.append("object_data", currObject);

                currObjectType = calculateObjectTypeForAnnotationProperty(currObject, currObjectType);

                currInputDataObject.append("object_type", currObjectType);

                String currOperation = calculateOperation(currExecStep);

                currInputDataObject.append("operation", currOperation);

                currComponentObject.put("input_data", currInputDataObject);

            } else {

                String currSubject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                System.out.println("currSubject = " + currSubject);

                currComponentObject.getJSONObject("input_data").append("subject", currSubject);

                String currProperty = calculateProperty(currExecStep);

                System.out.println("currProperty = " + currProperty);

                currComponentObject.getJSONObject("input_data").append("property", currProperty);

                String currNG = calculateNG(currExecStep, connectionToTDB);

                currComponentObject.getJSONObject("input_data").append("ng", currNG);

                System.out.println("currNG = " + currNG);

                String currDirectoryPath = calculateWorkspaceDirectory(currExecStep);

                currComponentObject.getJSONObject("input_data").append("directory", currDirectoryPath);

                System.out.println("currDirectoryPath = " + currDirectoryPath);

                String currObjectType = calculateObjectType(currProperty);

                System.out.println("currObjectType = " + currObjectType);

                dataToFindObjectInTDB.put("subject", currSubject);
                dataToFindObjectInTDB.put("property", currProperty);
                dataToFindObjectInTDB.put("ng", currNG);
                dataToFindObjectInTDB.put("directory", currDirectoryPath);

                String currObject = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, currObjectType, connectionToTDB);

                System.out.println("currObject = " + currObject);

                currComponentObject.getJSONObject("input_data").append("object_data", currObject);

                currObjectType = calculateObjectTypeForAnnotationProperty(currObject, currObjectType);

                System.out.println("currObjectType = " + currObjectType);

                currComponentObject.getJSONObject("input_data").append("object_type", currObjectType);

                String currOperation = calculateOperation(currExecStep);

                System.out.println("currOperation = " + currOperation);

                currComponentObject.getJSONObject("input_data").append("operation", currOperation);

            }

        }

        if (this.hasCreateOverlayInput) {

            this.hasCreateOverlayInput = false;

        }

        return currComponentObject;

    }


    /**
     * This method provides a query to find all axioms for a resource in a jena tdb.
     * @param resource contains an URI
     * @param directory contains the directory
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a model with all statements for a resource
     */
    private Model findAxiomTriple(String resource, String directory, JenaIOTDBFactory connectionToTDB) {

        PrefixesBuilder prefixesAxiomBuilder = new PrefixesBuilder();

        ConstructBuilder constructAxiomBuilder = new ConstructBuilder();

        constructAxiomBuilder = prefixesAxiomBuilder.addPrefixes(constructAxiomBuilder);

        constructAxiomBuilder.addConstruct("?s", "?p", "?o");

        SelectBuilder tripleAxiomSPOConstruct = new SelectBuilder();

        tripleAxiomSPOConstruct.addWhere("?s", "?p", "?o");
        tripleAxiomSPOConstruct.addWhere("?s", "<http://www.w3.org/2002/07/owl#annotatedSource>", "<" + resource + ">");

        constructAxiomBuilder.addGraph("?g", tripleAxiomSPOConstruct);

        String sparqlQueryAxiomString = constructAxiomBuilder.buildString();

        return connectionToTDB.pullDataFromTDB(directory, sparqlQueryAxiomString);

    }


    /**
     * This method provides a query to find all statements for a subject resource in a jena tdb.
     * @param resource contains an URI
     * @param directory contains the directory
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a model with all statements for a subject resource
     */
    private Model findTriple(String resource, String directory, JenaIOTDBFactory connectionToTDB) {

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        ConstructBuilder constructBuilder = new ConstructBuilder();

        constructBuilder = prefixesBuilder.addPrefixes(constructBuilder);

        constructBuilder.addConstruct("<" + resource + ">", "?p", "?o");

        SelectBuilder tripleSPOConstruct = new SelectBuilder();

        tripleSPOConstruct.addWhere("<" + resource + ">", "?p", "?o");

        constructBuilder.addGraph("?g", tripleSPOConstruct);

        String sparqlQueryString = constructBuilder.buildString();

        return connectionToTDB.pullDataFromTDB(directory, sparqlQueryString);

    }


    /**
     * This method is a getter for the overlay named graph.
     * @return a jena model for a MDB overlay
     */
    public Model getOverlayModel() {

        return this.overlayModel;

    }

    /**
     * This method calculates the URIs for tracking procedures of a parent transition and save them in an JSONArray
     * @param parentTransition contains an URI which has other transitions as an object
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an JSONArray with URI(s) of tracking procedures
     */
    private JSONArray getTrackingProcedures(String parentTransition, JenaIOTDBFactory connectionToTDB) {

        SelectBuilder selectBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        selectBuilder.addVar(selectBuilder.makeVar("?o"));

        SelectBuilder tripleSPO = new SelectBuilder();

        tripleSPO = prefixesBuilder.addPrefixes(tripleSPO);

        UrlValidator urlValidator = new UrlValidator();

        if (urlValidator.isValid(parentTransition)) {

            tripleSPO.addWhere("<" + parentTransition + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000080>", "?o");
            // involves MDB tracking-procedure

        } else {

            tripleSPO.addWhere("<" + parentTransition + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000464>", "?o");
            // involves MDB tracking-procedure

        }

        selectBuilder.addGraph("?g", tripleSPO);

        String sparqlQueryString = selectBuilder.buildString();

        return connectionToTDB.pullMultipleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?o");

    }


    /**
     * This method orders information for the calculation of the transition
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return input information for a jena tdb
     */
    public JSONObject executionStepTriggerMDBWorkflowAction (JSONObject currComponentObject, JSONObject jsonInputObject,
                                                       JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        // calculate the start date
        long executionStart = System.currentTimeMillis();

        boolean startTransition = false;

        String nextTransition = "";

        boolean subsequentlyTriggeredWA = false;

        JSONObject localIdentifiedResources = new JSONObject();

        String subsequentlyRoot = "";

        for (int i = 0; i < currExecStep.length(); i++) {

            if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000253")) {
                // keyword value transferred to triggered action

                String localNameKeyword = ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("object")).getLocalName();

                if (jsonInputObject.has("localIDs")) {

                    JSONArray inputLocalIDs = jsonInputObject.getJSONArray("localIDs");

                    if (inputLocalIDs != null) {

                        for (int j = 0; j < inputLocalIDs.length(); j++) {

                            if ((inputLocalIDs.getJSONObject(j).getString("keyword")).equals(localNameKeyword)) {

                                localIdentifiedResources.put(currExecStep.getJSONObject(i).getString("object"), inputLocalIDs.getJSONObject(j).getString("value"));

                            }

                        }

                    }

                }

                if (jsonInputObject.has("precedingKeywords")) {

                    JSONObject inputPrecedingKeywords = jsonInputObject.getJSONObject("precedingKeywords");

                    if (inputPrecedingKeywords.has(currExecStep.getJSONObject(i).getString("object"))) {

                        localIdentifiedResources.put(currExecStep.getJSONObject(i).getString("object"), inputPrecedingKeywords.getString(currExecStep.getJSONObject(i).getString("object")));

                    }

                }

                if (this.infoInput.keys().hasNext()) {

                    Iterator<String> infoInputKeys = infoInput.keys();

                    while (infoInputKeys.hasNext()) {

                        String currKey = infoInputKeys.next();

                        String localCurrKey = currKey;

                        UrlValidator keyURLValidator = new UrlValidator();

                        // get a MDB url Encoder to encode the uri with utf-8
                        MDBURLEncoder mdburlEncoder = new MDBURLEncoder();

                        if (keyURLValidator.isValid(mdburlEncoder.encodeUrl(localCurrKey, "UTF-8"))) {

                            localCurrKey = ResourceFactory.createResource(localCurrKey).getLocalName();

                        }

                        if (localCurrKey.equals(localNameKeyword)) {

                            localIdentifiedResources.put(currExecStep.getJSONObject(i).getString("object"), this.infoInput.getString(currKey));

                        }

                    }

                }

            } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000038")) {
                // triggers MDB workflow action

                nextTransition = currExecStep.getJSONObject(i).getString("object");

                startTransition = true;

            } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000390")) {
                // update store [BOOLEAN]

                //System.out.println("currComponentObject before save store" + currComponentObject);

                saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

            } else if((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000524")) {
                // subsequently triggered workflow action [BOOLEAN]

                subsequentlyTriggeredWA = true;

            } else if((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000525")) {
                // trigger action of button (of class)

                subsequentlyRoot = currExecStep.getJSONObject(i).getString("object");

            } else {

                boolean useAsInput = useObjectAsInput(currExecStep.getJSONObject(i).getString("property"), connectionToTDB);

                if (useAsInput) {

                    String uriOfIndividual;

                    if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
                        // KEYWORD: empty

                        uriOfIndividual = currExecStep.getJSONObject(i).getString("object");

                    } else {

                        uriOfIndividual = getKeywordIndividualFromProperty(currExecStep.getJSONObject(i).getString("property"), connectionToTDB);

                    }

                    this.infoInput.put(uriOfIndividual, currExecStep.getJSONObject(i).getString("object"));
                    this.infoInput.put(currExecStep.getJSONObject(i).getString("object"), "");

                    System.out.println();
                    System.out.println("uriOfIndividual = " + uriOfIndividual);
                    System.out.println();

                } else {

                    System.out.println("-------------");
                    System.out.println("Else - Branch");
                    System.out.println("-------------");
                    System.out.println();
                    System.out.println("property: " + currExecStep.getJSONObject(i).getString("property"));
                    System.out.println("object: " + currExecStep.getJSONObject(i).getString("object"));
                    System.out.println();

                }

            }

        }

        if (subsequentlyTriggeredWA) {

            currComponentObject.put("subsequently_workflow_action", "true");

            currComponentObject.put("subsequently_root", subsequentlyRoot);

            if (localIdentifiedResources.keys().hasNext()) {

                JSONObject transferKeywordToSubsequentlyActionDummy = new JSONObject();

                Iterator<String> localIdentifiedResourcesKeys = localIdentifiedResources.keys();

                while (localIdentifiedResourcesKeys.hasNext()) {

                    String currIdentifiedResource = localIdentifiedResourcesKeys.next();

                    if (this.infoInput.has(currIdentifiedResource)) {

                        transferKeywordToSubsequentlyActionDummy.put(currIdentifiedResource, this.infoInput.getString(currIdentifiedResource));

                    }

                }

                currComponentObject.put("keywords_to_transfer" ,transferKeywordToSubsequentlyActionDummy);

            }

        } else {

            Iterator<String> localIdentifiedResourcesKeys = localIdentifiedResources.keys();

            while (localIdentifiedResourcesKeys.hasNext()) {

                String currIdentifiedResource = localIdentifiedResourcesKeys.next();

                this.identifiedResources.put(currIdentifiedResource, localIdentifiedResources.getString(currIdentifiedResource));

            }

            if (startTransition) {

                KBOrder kbOrder = new KBOrder(connectionToTDB, this.pathToOntologies, nextTransition);

                if (kbOrder.getSortedKBIndicesJSONArray().length() > 0) {

                    // get the sorted input knowledge base
                    JSONArray sortedKBJSONArray = kbOrder.getSortedKBJSONArray();

                    // get the sorted indices of the knowledge base
                    JSONArray sortedKBIndicesJSONArray = kbOrder.getSortedKBIndicesJSONArray();

                    MDBJSONObjectFactory mdbjsonObjectFactory;

                    if (this.mdbCoreIDNotEmpty && this.mdbEntryIDNotEmpty && this.mdbUEIDNotEmpty) {

                        if (this.infoInput.length() != 0) {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.mdbCoreID, this.mdbEntryID, this.mdbUEID, this.identifiedResources, this.infoInput, this.overlayModel);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.mdbCoreID, this.mdbEntryID, this.mdbUEID, this.identifiedResources, this.overlayModel);

                        }

                    } else if(this.mdbUEIDNotEmpty) {

                        if (this.infoInput.length() != 0) {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.mdbUEID, this.identifiedResources, this.infoInput, this.overlayModel);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.mdbUEID, this.identifiedResources, this.overlayModel);

                        }

                    } else {

                        if (this.infoInput.length() != 0) {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.identifiedResources, this.infoInput, this.overlayModel);

                        } else {

                            mdbjsonObjectFactory = new MDBJSONObjectFactory(this.identifiedResources, this.overlayModel);

                        }

                    }

                    mdbjsonObjectFactory.convertKBToJSONObject(sortedKBJSONArray, sortedKBIndicesJSONArray, currComponentObject, jsonInputObject, connectionToTDB);

                    this.overlayModel = mdbjsonObjectFactory.getOverlayModel();

                }

                boolean updateStoreAfterTrackingProcedure = updateStoreAfterTrackingProcedureExist(nextTransition, connectionToTDB);

                if (updateStoreAfterTrackingProcedure) {

                    System.out.println("updateStoreAfterTrackingProcedure = " + true);

                    JSONArray trackingProcedures = getTrackingProcedures(nextTransition, connectionToTDB);

                    System.out.println();

                    for (int i = 0; i < trackingProcedures.length(); i++) {

                        System.out.println("tracking procedure " + i + " = " + trackingProcedures.getString(i));

                        KBOrder trackingKBOrder = new KBOrder(connectionToTDB, this.pathToOntologies, trackingProcedures.getString(i));

                        // get the sorted input knowledge base
                        JSONArray sortedKBJSONArray = trackingKBOrder.getSortedKBJSONArray();

                        // get the sorted indices of the knowledge base
                        JSONArray sortedKBIndicesJSONArray = trackingKBOrder.getSortedKBIndicesJSONArray();

                        convertKBToJSONObject(sortedKBJSONArray, sortedKBIndicesJSONArray, currComponentObject, jsonInputObject, connectionToTDB);

                    }

                    saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

                }

            }

        }

        return currComponentObject;

    }


    /**
     * This method generate new triples. The triples must be saved or deleted from a jena tdb
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return input information for a jena tdb
     */
    public JSONObject executionStepUpdateTripleStatements (JSONArray currExecStep, JSONObject currComponentObject,
                                                           JSONObject jsonInputObject,
                                                           JenaIOTDBFactory connectionToTDB) {

        boolean executeThisStep = true;

        if (jsonInputObject.has("mdbcoreid")) {

            if (jsonInputObject.getString("mdbcoreid").equals("http://www.morphdbase.de/resource/dummy-overlay")) {

                executeThisStep = false;

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000077")) {
                        // load from/save to/update in named graph


                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000392")) {
                        // load from/save to/update in named graph (individual of)


                    } else if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000393")) {
                        // load from/save to/update in named graph (this entry's specific individual of) update store [BOOLEAN]

                        if (jsonInputObject.getString("html_form").contains(ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("object")).getLocalName())) {

                            this.createOverlayNG = this.mdbCoreID.substring(0, this.mdbCoreID.indexOf("resource/dummy-overlay")) + jsonInputObject.getString("html_form");

                            this.hasCreateOverlayInput = true;

                            executeThisStep = true;

                            System.out.println("createOverlayNG = " + this.createOverlayNG);

                        }

                    }

                }

            }

        }

        if (executeThisStep) {

            boolean saveToStoreExist = false;

            for (int i = 0; i < currExecStep.length(); i++) {

                if ((currExecStep.getJSONObject(i).getString("property")).equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000390")) {
                    // update store [BOOLEAN]

                    saveToStoreExist = true;

                }

            }

            currComponentObject = getStatementToUpdate(currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

            if (saveToStoreExist) {

                saveToStores(currComponentObject, jsonInputObject, connectionToTDB);

            }

        }

        if (this.hasCreateOverlayInput) {

            this.hasCreateOverlayInput = false;

        }

        return currComponentObject;

    }


    /**
     * This method provide a default composition of an entry
     * @param defaultCompositionNGURI contains the uri of the named graph.
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a default model from the directory workspace
     */
    private Model findRootIndividual(String defaultCompositionNGURI, JSONArray currExecStep, JenaIOTDBFactory connectionToTDB) {

        String currDirectoryPath = calculateWorkspaceDirectoryForDefaultComposition(currExecStep);

        Model defaultCompositionModel;

        if (connectionToTDB.modelExistInTDB(currDirectoryPath, defaultCompositionNGURI)) {

            defaultCompositionModel = connectionToTDB.pullNamedModelFromTDB(currDirectoryPath, defaultCompositionNGURI);

        } else {

            System.out.println(defaultCompositionNGURI + " is empty!");

            JSONArray classToCheck = new JSONArray();

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

            filterItems = filterBuilder.addItems(filterItems, "?p", "<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000653>");
            // is root entry component of composition contained in named graph

            filterItems = filterBuilder.addItems(filterItems, "?o", "<" + defaultCompositionNGURI + ">");

            ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

            selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

            String sparqlQueryString = selectBuilder.buildString();

            String subject = connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?s");

            classToCheck.put(subject);

            defaultCompositionModel = calculateDefaultEntryComposition(classToCheck, connectionToTDB);

            // save named graph in jena tdb
            System.out.println(connectionToTDB.addModelDataInTDB(currDirectoryPath, defaultCompositionNGURI, defaultCompositionModel));

        }

        return defaultCompositionModel;

    }


    /**
     * This method finds a literal from a workspace.
     * @param subjectFilter contains a part of the subject to specify the correct individual in the store
     * @param property contains a property to find the corresponding range
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a literal from a workspace
     */
    private String getLiteralFromStore (String subjectFilter, String property, String pathToWorkspace, JenaIOTDBFactory connectionToTDB) {

        FilterBuilder filterBuilder = new FilterBuilder();

        SelectBuilder selectFNBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectFNBuilder = prefixesBuilder.addPrefixes(selectFNBuilder);

        SelectBuilder innerFNSelect = new SelectBuilder();

        innerFNSelect.addWhere("?s", "<" + property + ">", "?o");

        SPARQLFilter sparqlFNFilter = new SPARQLFilter();

        ArrayList<String> filterFNItems = new ArrayList<>();

        filterFNItems.add(subjectFilter);

        ArrayList<String> filterFN = sparqlFNFilter.getRegexSTRFilter("?s", filterFNItems);

        innerFNSelect = filterBuilder.addFilter(innerFNSelect, filterFN);

        selectFNBuilder.addVar(selectFNBuilder.makeVar("?o"));

        selectFNBuilder.addGraph("?g", innerFNSelect);

        String sparqlQueryString = selectFNBuilder.buildString();

        return connectionToTDB.pullSingleDataFromTDB(pathToWorkspace, sparqlQueryString, "?o");

    }


    /**
     * This method get an individual keyword uri related to the property from the jena tdb
     * @param infoInputProperty contains the uri of a property
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the uri of an individual keyword
     */
    private String getKeywordIndividualFromProperty (String infoInputProperty, JenaIOTDBFactory connectionToTDB) {

        SelectBuilder selectBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        SelectBuilder innerSelect = new SelectBuilder();

        innerSelect.addWhere("?s", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000402>", "<" + infoInputProperty + ">");
        // MDB_UIAP_0000000402 >>> value defined through property

        selectBuilder.addVar(selectBuilder.makeVar("?s"));

        selectBuilder.addGraph("?g", innerSelect);

        String sparqlQueryString = selectBuilder.buildString();

        return connectionToTDB.pullSingleDataFromTDB(this.pathToOntologies, sparqlQueryString, "?s");

    }


    /**
     * This method calculates the next step.
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param annotationProperty contains an annotation property with a potential next step
     * @return the next step
     */
    private String getNextStepFromJSONArray(JSONArray currExecStep, String annotationProperty) {

        String nextStep = "";

        for (int j = 0; j < currExecStep.length(); j++) {

            if (currExecStep.getJSONObject(j).getString("property").equals(annotationProperty)) {

                nextStep = currExecStep.getJSONObject(j).getString("object");

                System.out.println("next step: " + nextStep);

            }


        }


        return nextStep;

    }


    /**
     * This method find the statement(s) to updated in the jena tdb and delete the old statement(s) and calculate the new
     * statement(s).
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a JSON Object with statement for the update
     */
    private JSONObject getStatementToUpdate(JSONArray currExecStep, JSONObject currComponentObject, JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        JSONObject updateStatement = new JSONObject();
        JSONObject updateAxiomStatement = new JSONObject();

        boolean axiomStatement = false;
        boolean calculateNewResourceForInput = false;
        boolean calculateNewObjectInput = false;
        boolean calculateNewSubjectInput = false;

        for (int i = 0; i < currExecStep.length(); i++) {

            if (currExecStep.getJSONObject(i).getString("object").equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000221")) {
                // KEYWORD: to be updated

                String resultVar;

                if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000040")) {
                    // object

                    JSONObject dataToFindObjectInTDB = new JSONObject();

                    String subject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                    String property = calculateProperty(currExecStep);

                    String currObjectType = calculateObjectType(property);

                    String ng = calculateNG(currExecStep, connectionToTDB);

                    String directory = calculateWorkspaceDirectory(currExecStep);

                    resultVar = "?o";

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder tripleSPO = new SelectBuilder();

                    tripleSPO.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                    selectBuilder.addVar(selectBuilder.makeVar(resultVar));

                    selectBuilder.addGraph("<" + ng + ">", tripleSPO);

                    String sparqlQueryString = selectBuilder.buildString();

                    String object = connectionToTDB.pullSingleDataFromTDB(directory, sparqlQueryString, resultVar);

                    if (!object.equals("")) {

                        updateStatement.put("subject", subject);

                        updateStatement.put("property", property);

                        updateStatement.put("ng", ng);

                        updateStatement.put("directory", directory);

                        currObjectType = calculateObjectTypeForAnnotationProperty(object, currObjectType);

                        updateStatement.put("object_type", currObjectType);

                        if (currObjectType.equals("l")) {
                            // delete initial object from the jena tdb e.g. "true"^^http://www.w3.org/2001/XMLSchema#boolean

                            String literalDatatypeResultVar = "?o";

                            SelectBuilder literalDatatypeSelectBuilder = new SelectBuilder();

                            PrefixesBuilder literalDatatypePrefixesBuilder = new PrefixesBuilder();

                            literalDatatypeSelectBuilder = literalDatatypePrefixesBuilder.addPrefixes(literalDatatypeSelectBuilder);

                            SelectBuilder literalDatatypeTripleSPO = new SelectBuilder();

                            literalDatatypeTripleSPO.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                            literalDatatypeSelectBuilder.addVar(literalDatatypeSelectBuilder.makeVar(literalDatatypeResultVar));

                            literalDatatypeSelectBuilder.addGraph("<" + ng + ">", literalDatatypeTripleSPO);

                            String literalDatatypeSparqlQueryString = literalDatatypeSelectBuilder.buildString();

                            updateStatement.put("object_data", connectionToTDB.pullSingleLiteralWithDatatypeFromTDB(directory, literalDatatypeSparqlQueryString, literalDatatypeResultVar));

                        } else {

                            updateStatement.put("object_data", object);

                        }

                        updateStatement.put("operation", "d");

                        calculateNewResourceForInput = true;

                        calculateNewObjectInput = true;

                    } else {

                        PrefixesBuilder prefixesAxiomBuilder = new PrefixesBuilder();

                        ConstructBuilder constructAxiomBuilder = new ConstructBuilder();

                        constructAxiomBuilder = prefixesAxiomBuilder.addPrefixes(constructAxiomBuilder);

                        constructAxiomBuilder.addConstruct("?s", "?p", "?o");

                        SelectBuilder tripleAxiomSPOConstruct = new SelectBuilder();

                        tripleAxiomSPOConstruct.addWhere("?s", "?p", "?o");
                        tripleAxiomSPOConstruct.addWhere("?s", "<http://www.w3.org/2002/07/owl#annotatedSource>", "<" + subject + ">");

                        constructAxiomBuilder.addGraph("<" + ng + ">", tripleAxiomSPOConstruct);

                        sparqlQueryString = constructAxiomBuilder.buildString();

                        Model individualAxiomModel = connectionToTDB.pullDataFromTDB(directory, sparqlQueryString);

                        StmtIterator stmtIterator = individualAxiomModel.listStatements();

                        while (stmtIterator.hasNext()) {

                            Statement currStatement = stmtIterator.next();

                            if (currStatement.getSubject().isAnon()) {

                                if (currStatement.getObject().isResource()) {

                                    object =  currStatement.getObject().asResource().toString();

                                    currObjectType = "r";

                                } else if (currStatement.getObject().isLiteral()) {

                                    object = currStatement.getObject().asLiteral().getLexicalForm();

                                    currObjectType = "l";

                                }

                                // old statements
                                updateAxiomStatement.append("subject", currStatement.getSubject().toString());
                                updateAxiomStatement.append("property", currStatement.getPredicate().toString());
                                updateAxiomStatement.append("ng", ng);
                                updateAxiomStatement.append("directory", directory);

                                if (currObjectType.equals("l")) {
                                    // delete initial object from the jena tdb e.g. "true"^^http://www.w3.org/2001/XMLSchema#boolean

                                    String literalDatatypeResultVar = "?o";

                                    SelectBuilder literalDatatypeSelectBuilder = new SelectBuilder();

                                    PrefixesBuilder literalDatatypePrefixesBuilder = new PrefixesBuilder();

                                    literalDatatypeSelectBuilder = literalDatatypePrefixesBuilder.addPrefixes(literalDatatypeSelectBuilder);

                                    SelectBuilder literalDatatypeTripleSPO = new SelectBuilder();

                                    literalDatatypeTripleSPO.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                                    literalDatatypeSelectBuilder.addVar(literalDatatypeSelectBuilder.makeVar(literalDatatypeResultVar));

                                    literalDatatypeSelectBuilder.addGraph("<" + ng + ">", literalDatatypeTripleSPO);

                                    String literalDatatypeSparqlQueryString = literalDatatypeSelectBuilder.buildString();

                                    updateAxiomStatement.append("object_data", connectionToTDB.pullSingleLiteralWithDatatypeFromTDB(directory, literalDatatypeSparqlQueryString, literalDatatypeResultVar));

                                } else {

                                    updateAxiomStatement.append("object_data", object);

                                }

                                updateAxiomStatement.append("object_type", currObjectType);
                                updateAxiomStatement.append("operation", "d");

                                for (int j = 0; j < currExecStep.length(); j++) {

                                    if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000041")) {
                                        // property

                                        if (currStatement.getPredicate().toString().equals(currExecStep.getJSONObject(j).getString("object"))) {

                                            for (int k = 0; k < currExecStep.length(); k++) {

                                                if (currExecStep.getJSONObject(k).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000104")) {
                                                    // update with resource/value

                                                    currObjectType = calculateObjectTypeForAnnotationProperty(currExecStep.getJSONObject(k).getString("object"), currObjectType);

                                                    currExecStep.getJSONObject(i).put("object", currExecStep.getJSONObject(k).getString("object"));

                                                }

                                            }

                                            dataToFindObjectInTDB = new JSONObject();
                                            dataToFindObjectInTDB.put("subject", currStatement.getSubject().toString());
                                            dataToFindObjectInTDB.put("property", currStatement.getPredicate().toString());
                                            dataToFindObjectInTDB.put("ng", ng);
                                            dataToFindObjectInTDB.put("directory", directory);

                                            object = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, currObjectType, connectionToTDB);

                                            currObjectType = calculateObjectTypeForAnnotationProperty(object, currObjectType);

                                        }

                                    }

                                }

                                String newSubjectName;

                                // create new blank node and allocate the corresponding old bNode with the new bNode
                                if (this.bNodeIdentifier.has(currStatement.getSubject().toString())) {

                                    newSubjectName = this.bNodeIdentifier.getString(currStatement.getSubject().toString());

                                } else {

                                    newSubjectName = ResourceFactory.createResource().toString();

                                    this.bNodeIdentifier.put(currStatement.getSubject().toString(), newSubjectName);

                                }

                                // new statements
                                updateAxiomStatement.append("subject", newSubjectName);
                                updateAxiomStatement.append("property", currStatement.getPredicate().toString());
                                updateAxiomStatement.append("ng", ng);
                                updateAxiomStatement.append("directory", directory);
                                updateAxiomStatement.append("object_data", object);
                                updateAxiomStatement.append("object_type", currObjectType);
                                updateAxiomStatement.append("operation", "s");

                                axiomStatement = true;

                            }

                            System.out.println();
                            System.out.println("updateStatement a = " + updateStatement);
                            System.out.println();

                        }

                        if (!axiomStatement) {

                            for (int j = 0; j < currExecStep.length(); j++) {

                                if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000104")) {
                                    // update with resource/value

                                    // count value in jena tdb
                                    SelectBuilder selectWhereBuilder = new SelectBuilder();

                                    selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

                                    selectWhereBuilder.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                                    SelectBuilder countSelectBuilder = new SelectBuilder();

                                    countSelectBuilder = prefixesBuilder.addPrefixes(countSelectBuilder);

                                    ExprVar exprVar = new ExprVar("o");

                                    Aggregator aggregator = AggregatorFactory.createCountExpr(true, exprVar.getExpr());

                                    ExprAggregator exprAggregator = new ExprAggregator(exprVar.asVar(), aggregator);

                                    countSelectBuilder.addVar(exprAggregator.getExpr(), "?count");

                                    countSelectBuilder.addGraph("<" + ng + ">", selectWhereBuilder);

                                    sparqlQueryString = countSelectBuilder.buildString();

                                    int count = Integer.parseInt(connectionToTDB.pullSingleDataFromTDB(directory, sparqlQueryString, "?count"));

                                    if (count <= 0 || !object.equals("")) {
                                        // no data exist in store

                                        currExecStep.getJSONObject(i).put("object", currExecStep.getJSONObject(j).getString("object"));

                                        dataToFindObjectInTDB = new JSONObject();
                                        dataToFindObjectInTDB.put("subject", subject);
                                        dataToFindObjectInTDB.put("property", property);
                                        dataToFindObjectInTDB.put("ng", ng);
                                        dataToFindObjectInTDB.put("directory", directory);

                                        object = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, currObjectType, connectionToTDB);

                                        currObjectType = calculateObjectTypeForAnnotationProperty(object, currObjectType);

                                    }

                                    updateStatement.put("subject", subject);

                                    updateStatement.put("property", property);

                                    updateStatement.put("ng", ng);

                                    updateStatement.put("directory", directory);

                                    updateStatement.put("object_type", currObjectType);

                                    if (currObjectType.equals("l")) {
                                        // delete initial object from the jena tdb e.g. "true"^^http://www.w3.org/2001/XMLSchema#boolean

                                        String literalDatatypeResultVar = "?o";

                                        SelectBuilder literalDatatypeSelectBuilder = new SelectBuilder();

                                        PrefixesBuilder literalDatatypePrefixesBuilder = new PrefixesBuilder();

                                        literalDatatypeSelectBuilder = literalDatatypePrefixesBuilder.addPrefixes(literalDatatypeSelectBuilder);

                                        SelectBuilder literalDatatypeTripleSPO = new SelectBuilder();

                                        literalDatatypeTripleSPO.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                                        literalDatatypeSelectBuilder.addVar(literalDatatypeSelectBuilder.makeVar(literalDatatypeResultVar));

                                        literalDatatypeSelectBuilder.addGraph("<" + ng + ">", literalDatatypeTripleSPO);

                                        String literalDatatypeSparqlQueryString = literalDatatypeSelectBuilder.buildString();

                                        updateStatement.put("object_data", connectionToTDB.pullSingleLiteralWithDatatypeFromTDB(directory, literalDatatypeSparqlQueryString, literalDatatypeResultVar));

                                    } else {

                                        updateStatement.put("object_data", object);

                                    }

                                    updateStatement.put("operation", "d");

                                    calculateNewResourceForInput = true;

                                    calculateNewObjectInput = true;

                                }

                            }

                        }

                    }

                    System.out.println();
                    System.out.println("updateStatement b = " + updateStatement);
                    System.out.println();

                } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000042")) {
                    // subject

                    String property = calculateProperty(currExecStep);

                    String ng = calculateNG(currExecStep, connectionToTDB);

                    String directory = calculateWorkspaceDirectory(currExecStep);

                    String currObjectType = calculateObjectType(property);

                    JSONObject dataToFindObjectInTDB = new JSONObject();
                    dataToFindObjectInTDB.put("subject", "?s");
                    dataToFindObjectInTDB.put("property", property);
                    dataToFindObjectInTDB.put("ng", ng);
                    dataToFindObjectInTDB.put("directory", directory);

                    String object = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, currObjectType, connectionToTDB);

                    resultVar = "?s";

                    FilterBuilder filterBuilder = new FilterBuilder();

                    SelectBuilder selectBuilder = new SelectBuilder();

                    PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                    selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                    SelectBuilder tripleSPO = new SelectBuilder();

                    tripleSPO.addWhere("?s", "?p", "?o");

                    selectBuilder.addVar(selectBuilder.makeVar(resultVar));

                    selectBuilder.addGraph("<" + ng + ">", tripleSPO);

                    SPARQLFilter sparqlFilter = new SPARQLFilter();

                    ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

                    filterItems = filterBuilder.addItems(filterItems, "?p", "<" + property + ">");
                    filterItems = filterBuilder.addItems(filterItems, "?o", "<" + object + ">");

                    ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

                    selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                    String sparqlQueryString = selectBuilder.buildString();

                    String subject = connectionToTDB.pullSingleDataFromTDB(directory, sparqlQueryString, resultVar);

                    if (!subject.equals("")) {

                        updateStatement.put("subject", subject);

                        updateStatement.put("property", property);

                        updateStatement.put("ng", ng);

                        updateStatement.put("directory", directory);

                        currObjectType = calculateObjectTypeForAnnotationProperty(object, currObjectType);

                        updateStatement.put("object_type", currObjectType);

                        if (currObjectType.equals("l")) {
                            // delete initial object from the jena tdb e.g. "true"^^http://www.w3.org/2001/XMLSchema#boolean

                            String literalDatatypeResultVar = "?o";

                            SelectBuilder literalDatatypeSelectBuilder = new SelectBuilder();

                            PrefixesBuilder literalDatatypePrefixesBuilder = new PrefixesBuilder();

                            literalDatatypeSelectBuilder = literalDatatypePrefixesBuilder.addPrefixes(literalDatatypeSelectBuilder);

                            SelectBuilder literalDatatypeTripleSPO = new SelectBuilder();

                            literalDatatypeTripleSPO.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                            literalDatatypeSelectBuilder.addVar(literalDatatypeSelectBuilder.makeVar(literalDatatypeResultVar));

                            literalDatatypeSelectBuilder.addGraph("<" + ng + ">", literalDatatypeTripleSPO);

                            String literalDatatypeSparqlQueryString = literalDatatypeSelectBuilder.buildString();

                            updateStatement.put("object_data", connectionToTDB.pullSingleLiteralWithDatatypeFromTDB(directory, literalDatatypeSparqlQueryString, literalDatatypeResultVar));

                        } else {

                            updateStatement.put("object_data", object);

                        }

                        updateStatement.put("operation", "d");

                        calculateNewResourceForInput = true;

                        calculateNewSubjectInput = true;

                    } else {

                        for (int j = 0; j < currExecStep.length(); j++) {

                            if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000104")) {
                                // update with resource/value

                                currExecStep.getJSONObject(i).put("object", currExecStep.getJSONObject(j).getString("object"));

                                dataToFindObjectInTDB = new JSONObject();
                                dataToFindObjectInTDB.put("object", object);
                                dataToFindObjectInTDB.put("property", property);
                                dataToFindObjectInTDB.put("ng", ng);
                                dataToFindObjectInTDB.put("directory", directory);

                                subject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                                currObjectType = calculateObjectTypeForAnnotationProperty(object, currObjectType);

                                updateStatement.put("subject", subject);

                                updateStatement.put("property", property);

                                updateStatement.put("ng", ng);

                                updateStatement.put("directory", directory);

                                updateStatement.put("object_type", currObjectType);

                                if (currObjectType.equals("l")) {
                                    // delete initial object from the jena tdb e.g. "true"^^http://www.w3.org/2001/XMLSchema#boolean

                                    String literalDatatypeResultVar = "?o";

                                    SelectBuilder literalDatatypeSelectBuilder = new SelectBuilder();

                                    PrefixesBuilder literalDatatypePrefixesBuilder = new PrefixesBuilder();

                                    literalDatatypeSelectBuilder = literalDatatypePrefixesBuilder.addPrefixes(literalDatatypeSelectBuilder);

                                    SelectBuilder literalDatatypeTripleSPO = new SelectBuilder();

                                    literalDatatypeTripleSPO.addWhere("<" + subject + ">", "<" + property + ">", "?o");

                                    literalDatatypeSelectBuilder.addVar(literalDatatypeSelectBuilder.makeVar(literalDatatypeResultVar));

                                    literalDatatypeSelectBuilder.addGraph("<" + ng + ">", literalDatatypeTripleSPO);

                                    String literalDatatypeSparqlQueryString = literalDatatypeSelectBuilder.buildString();

                                    updateStatement.put("object_data", connectionToTDB.pullSingleLiteralWithDatatypeFromTDB(directory, literalDatatypeSparqlQueryString, literalDatatypeResultVar));

                                } else {

                                    updateStatement.put("object_data", object);

                                }

                                updateStatement.put("operation", "d");

                            }

                        }

                        System.out.println();
                        System.out.println("updateStatement c = " + updateStatement);
                        System.out.println();

                    }


                }

            }

        }

        if (axiomStatement) {

            JSONArray jsonArrayForTheAxiomInput = updateAxiomStatement.getJSONArray("subject");

            for (int i = 0; i < jsonArrayForTheAxiomInput.length(); i++) {

                currComponentObject.getJSONObject("input_data").append("subject", updateAxiomStatement.getJSONArray("subject").getString(i));
                currComponentObject.getJSONObject("input_data").append("property", updateAxiomStatement.getJSONArray("property").getString(i));
                currComponentObject.getJSONObject("input_data").append("ng", updateAxiomStatement.getJSONArray("ng").getString(i));
                currComponentObject.getJSONObject("input_data").append("directory", updateAxiomStatement.getJSONArray("directory").getString(i));
                currComponentObject.getJSONObject("input_data").append("object_data", updateAxiomStatement.getJSONArray("object_data").getString(i));
                currComponentObject.getJSONObject("input_data").append("object_type", updateAxiomStatement.getJSONArray("object_type").getString(i));
                currComponentObject.getJSONObject("input_data").append("operation", updateAxiomStatement.getJSONArray("operation").getString(i));

            }

        } else {

            Iterator stmtIter = updateStatement.keys();

            while (stmtIter.hasNext()) {

                String currKey = stmtIter.next().toString();

                if (!currComponentObject.has("input_data")) {

                    currComponentObject.put("input_data", new JSONObject());

                }

                currComponentObject.getJSONObject("input_data").append(currKey, updateStatement.get(currKey));

            }

            if (calculateNewResourceForInput) {

                for (int i = 0; i < currExecStep.length(); i++) {

                    if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000040") &&
                            calculateNewObjectInput) {
                        // object

                        for (int j = 0; j < currExecStep.length(); j++) {

                            if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000104")) {
                                // update with resource/value

                                currExecStep.getJSONObject(i).put("object", currExecStep.getJSONObject(j).getString("object"));

                                JSONObject dataToFindObjectInTDB = new JSONObject();

                                String object = calculateObject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, updateStatement.getString("object_type"), connectionToTDB);

                                updateStatement.put("object_data", object);

                                String objectType = calculateObjectType(updateStatement.getString("property"));

                                objectType = calculateObjectTypeForAnnotationProperty(object, objectType);

                                updateStatement.put("object_type", objectType);

                            }

                        }

                    } else if (currExecStep.getJSONObject(i).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000042") &&
                            calculateNewSubjectInput) {
                        // subject

                        for (int j = 0; j < currExecStep.length(); j++) {

                            if (currExecStep.getJSONObject(j).getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000104")) {
                                // update with resource/value

                                currExecStep.getJSONObject(i).put("object", currExecStep.getJSONObject(j).getString("object"));

                                JSONObject dataToFindObjectInTDB = new JSONObject();

                                String subject = calculateSubject(dataToFindObjectInTDB, currExecStep, currComponentObject, jsonInputObject, connectionToTDB);

                                updateStatement.put("subject", subject);

                            }

                        }

                    }

                }

            }

            updateStatement.put("operation", "s");

            System.out.println();
            System.out.println("updateStatement d = " + updateStatement);
            System.out.println();

            if (updateStatement.getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000204")
                    // hidden store[BOOLEAN]
                    && updateStatement.getString("object_data").equals("false")) {

                this.updateComposition = true;

                if (this.compositionUpdateJSON.has("children")
                        && this.compositionUpdateJSON.has("ngs")
                        && this.compositionUpdateJSON.has("directories") ) {

                    JSONArray childrenJSON = this.compositionUpdateJSON.getJSONArray("children");
                    JSONArray ngsJSON = this.compositionUpdateJSON.getJSONArray("ngs");
                    JSONArray directoriesJSON = this.compositionUpdateJSON.getJSONArray("directories");

                    childrenJSON.put(updateStatement.getString("subject"));
                    ngsJSON.put(updateStatement.getString("ng"));
                    directoriesJSON.put(updateStatement.getString("directory"));

                    this.compositionUpdateJSON.put("children", childrenJSON);
                    this.compositionUpdateJSON.put("ngs", ngsJSON);
                    this.compositionUpdateJSON.put("directories", directoriesJSON);

                } else {

                    JSONArray childrenJSON = new JSONArray();
                    JSONArray ngsJSON = new JSONArray();
                    JSONArray directoriesJSON = new JSONArray();

                    childrenJSON.put(updateStatement.getString("subject"));
                    ngsJSON.put(updateStatement.getString("ng"));
                    directoriesJSON.put(updateStatement.getString("directory"));

                    this.compositionUpdateJSON.put("children", childrenJSON);
                    this.compositionUpdateJSON.put("ngs", ngsJSON);
                    this.compositionUpdateJSON.put("directories", directoriesJSON);

                }

            } else if (updateStatement.getString("property").equals("http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000204")
                    // hidden store[BOOLEAN]
                    && updateStatement.getString("object_data").equals("true")) {

                if (currComponentObject.has("delete_uri")) {

                    currComponentObject.getJSONArray("delete_uri").put(ResourceFactory.createResource(updateStatement.getString("subject")).getLocalName());

                } else {

                    JSONArray updateURIsJSON = new JSONArray();

                    updateURIsJSON.put(ResourceFactory.createResource(updateStatement.getString("subject")).getLocalName());

                    currComponentObject.put("delete_uri", updateURIsJSON);

                }

            }

            stmtIter = updateStatement.keys();

            while (stmtIter.hasNext()) {

                String currKey = stmtIter.next().toString();

                currComponentObject.getJSONObject("input_data").append(currKey, updateStatement.get(currKey));

            }

        }

        return currComponentObject;

    }


    /**
     * This method saves the data to the jena tdb and the mongoDB.
     * @param currComponentObject contains the current component information for the output json
     * @param jsonInputObject contains the information for the calculation
     */
    private void saveToStores(JSONObject currComponentObject, JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB) {

        JSONObject inputData = currComponentObject.getJSONObject("input_data");

        System.out.println();
        System.out.println("saveToStore");
        System.out.println();

        DataFactory dataFactory = new DataFactory();

        ArrayList<ArrayList<String>> generatedCoreIDData = dataFactory.generateCoreIDNGData(inputData);

        inputData = convertArrayListToJSONObject(generatedCoreIDData);

        JSONInputInterpreter jsonInputInterpreter = new JSONInputInterpreter();

        ArrayList<String> dummyArrayList = jsonInputInterpreter.interpretObject(inputData, connectionToTDB);

        for (String aDummyArrayList : dummyArrayList) {

            System.out.println("jsonInputInterpreter: " + aDummyArrayList);

        }

        currComponentObject.remove("input_data");

        if ((jsonInputObject.has("localID")) & (jsonInputObject.has("mdbueid"))) {

            String updateMongoDBKey = jsonInputObject.getString("localID");

            if ((updateMongoDBKey.equals("MDB_DATASCHEME_0000000684"))
                    //SIGN_UP_MODULE_ITEM: sign up button
                    && ((!(jsonInputObject.getString("mdbueid")).equals("")))) {

                System.out.println("Update mongoDB with " + this.mdbUEID + " where mdbueid is = " + jsonInputObject.getString("mdbueid"));

                if (this.mongoDBConnection.documentExist("mdb-prototyp", "users", "mdbueid", jsonInputObject.getString("mdbueid"))) {
                    // insert mdbueiduri in "users" collection in mongoDB

                    String objectID = this.mongoDBConnection.findObjectID("mdb-prototyp", "users", "mdbueid", jsonInputObject.getString("mdbueid"));

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "users", objectID, "mdbueiduri", this.mdbUEID);

                    currComponentObject.put("mdbueid_uri", this.mdbUEID);

                }

                if (this.mongoDBConnection.documentExist("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"))) {
                    // insert mdbueid + mdbueiduri in "sessions" collection in mongoDB

                    String objectID = this.mongoDBConnection.findObjectID("mdb-prototyp", "sessions", "session", jsonInputObject.getString("connectSID"));

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", objectID, "mdbueid", jsonInputObject.getString("mdbueid"));

                    this.mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "sessions", objectID, "mdbueiduri", this.mdbUEID);

                }

            }

        }

        if (this.updateComposition
                && this.compositionUpdateJSON.has("children")
                && this.compositionUpdateJSON.has("directories")
                && this.compositionUpdateJSON.has("ngs")) {

            JSONArray childrenJSON = this.compositionUpdateJSON.getJSONArray("children");

            for (int i = 0; i < childrenJSON.length(); i++) {

                String parent = calculateMDBParent(i, connectionToTDB);

                JSONArray parentToCheck = new JSONArray();

                parentToCheck.put(parent);

                System.out.println();
                System.out.println("child = " + childrenJSON.getString(i));
                System.out.println("parent = " + parent);

                Model subCompositionCopyModel = ModelFactory.createDefaultModel(),
                        subCompositionUpdateModel = ModelFactory.createDefaultModel();

                subCompositionCopyModel =
                        subCompositionCopyModel
                                .union(
                                        connectionToTDB
                                                .pullNamedModelFromTDB(
                                                        this.compositionUpdateJSON
                                                                .getJSONArray("directories").getString(i),
                                                        this.compositionUpdateJSON.getJSONArray("ngs").getString(i)));

                while (!parentToCheck.isNull(0)) {

                    if (subCompositionCopyModel.contains(ResourceFactory.createResource(parentToCheck.getString(0)), RDF.type, OWL2.NamedIndividual)) {

                        Selector parentTripleSelector = new SimpleSelector(ResourceFactory.createResource(parentToCheck.getString(0)), null, null, "");

                        StmtIterator parentStmts = subCompositionCopyModel.listStatements(parentTripleSelector);

                        while (parentStmts.hasNext()) {

                            Statement parentStmt = parentStmts.nextStatement();

                            subCompositionUpdateModel.add(parentStmt);

                            if (parentStmt.getSubject().toString().equals(parentToCheck.getString(0))
                                    && parentStmt.getPredicate().toString().equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000040")) {
                                // has MDB entry component

                                parentToCheck.put(parentStmt.getObject().toString());

                            } else if (parentStmt.getObject().isURIResource()
                                    && parentStmt.getPredicate().equals(RDF.type)) {

                                Resource currSubject = parentStmt.getSubject().asResource();

                                Property currProperty = parentStmt.getPredicate();

                                Resource currObject = parentStmt.getObject().asResource();

                                if (currSubject.equals(ResourceFactory.createResource(parentToCheck.getString(0)))
                                        && currProperty.equals(RDF.type)
                                        && !currObject.equals(OWL2.NamedIndividual)) {

                                    Selector classSelector = new SimpleSelector(currObject, null, null, "");

                                    StmtIterator classStmts = subCompositionCopyModel.listStatements(classSelector);

                                    Resource classSubject = null;

                                    while (classStmts.hasNext()) {

                                        Statement classStmt = classStmts.nextStatement();

                                        classSubject = classStmt.getSubject();

                                        if ((!classStmt.getObject().equals(OWL2.Class))
                                                && (!classStmt.getPredicate().equals(RDFS.label))
                                                && (!classStmt.getPredicate().equals(RDFS.subClassOf))
                                                && (!classStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                && (!classStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                            subCompositionUpdateModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(parentToCheck.getString(0)), classStmt.getPredicate(), classStmt.getObject()));

                                        }

                                    }

                                    if (subCompositionCopyModel.contains(null, OWL2.annotatedSource, classSubject)) {

                                        ResIterator axiomsForClassSubject = subCompositionCopyModel.listSubjectsWithProperty(OWL2.annotatedSource, classSubject);

                                        while (axiomsForClassSubject.hasNext()) {

                                            Resource axiomClassSubject = axiomsForClassSubject.next();

                                            Selector axiomClassSelector = new SimpleSelector(axiomClassSubject, null, null, "");

                                            StmtIterator axiomClassStmts = subCompositionCopyModel.listStatements(axiomClassSelector);

                                            while (axiomClassStmts.hasNext()) {

                                                Statement axiomClassStmt = axiomClassStmts.nextStatement();

                                                if ((!axiomClassStmt.getObject().equals(OWL2.Axiom))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedSource))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedTarget))
                                                        && (!axiomClassStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                                    subCompositionUpdateModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(parentToCheck.getString(0)), axiomClassStmt.getPredicate(), axiomClassStmt.getObject()));

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                        }

                        if (subCompositionCopyModel.contains(null, OWL2.annotatedSource, ResourceFactory.createResource(parentToCheck.getString(0)))) {

                            ResIterator axiomsForSubject = subCompositionCopyModel.listSubjectsWithProperty(OWL2.annotatedSource, ResourceFactory.createResource(parentToCheck.getString(0)));

                            while (axiomsForSubject.hasNext()) {

                                Resource axiomSubject = axiomsForSubject.next();

                                Selector axiomSelector = new SimpleSelector(axiomSubject, null, null, "");

                                StmtIterator axiomStmts = subCompositionCopyModel.listStatements(axiomSelector);

                                while (axiomStmts.hasNext()) {

                                    Statement axiomStmt = axiomStmts.nextStatement();

                                    if ((!axiomStmt.getObject().equals(OWL2.Axiom))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedSource))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedTarget))
                                            && (!axiomStmt.getPredicate().equals(OWL2.annotatedProperty))) {

                                        subCompositionUpdateModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(parentToCheck.getString(0)), axiomStmt.getPredicate(), axiomStmt.getObject()));

                                    }

                                }

                            }

                        }

                    }

                    // remove the old key
                    parentToCheck.remove(0);

                }

                StmtIterator entryComponentsModelIter = subCompositionUpdateModel.listStatements();

                OutputGenerator outputGenerator = new OutputGenerator();

                JSONObject entryComponents = new JSONObject();

                while (entryComponentsModelIter.hasNext()) {

                    Statement resStmt = entryComponentsModelIter.nextStatement();

                    entryComponents = outputGenerator
                            .manageProperty(resStmt.getSubject().toString(), resStmt, entryComponents,
                                    jsonInputObject, connectionToTDB);

                }

                entryComponents = outputGenerator.reorderEntryComponentsValues(entryComponents);

                Iterator<String> iter = entryComponents.keys();

                JSONArray outputDataJSON = new JSONArray();

                while (iter.hasNext()) {

                    String currKey = iter.next();

                    JSONObject wrapperJSON = new JSONObject();

                    wrapperJSON.put(currKey, entryComponents.getJSONObject(currKey));

                    outputDataJSON.put(wrapperJSON);

                }

                outputDataJSON = outputGenerator.orderSubCompositionOutputJSON(parent, outputDataJSON);

                // update mongo composition for html form
                outputGenerator.getOutputJSONObject(jsonInputObject.getString("html_form"), jsonInputObject, outputDataJSON);

                String parentLocalID = ResourceFactory.createResource(parent).getLocalName();

                if (currComponentObject.has("update_uri")) {

                    currComponentObject.getJSONArray("update_uri").put(parentLocalID);

                } else {

                    JSONArray updateURIsJSON = new JSONArray();

                    updateURIsJSON.put(parentLocalID);

                    currComponentObject.put("update_uri", updateURIsJSON);

                }

                currComponentObject.put(parentLocalID, outputDataJSON.getJSONObject(0));

            }

        }

    }

    /**
     * This method calculates a parent for an overlay child.
     * @param arrayPosition contains the position of the child in a JSONArray
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return the URI of the parent
     */
    private String calculateMDBParent(int arrayPosition, JenaIOTDBFactory connectionToTDB) {

        SelectBuilder selectWhereBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectWhereBuilder.addWhere("?s", "<http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000040>","<" + this.compositionUpdateJSON.getJSONArray("children").getString(arrayPosition) + ">");
        // has MDB entry component

        SelectBuilder selectBuilder = new SelectBuilder();

        selectBuilder.addGraph("<" + this.compositionUpdateJSON.getJSONArray("ngs").getString(arrayPosition) + ">", selectWhereBuilder);

        selectBuilder.addVar(selectBuilder.makeVar("?s"));

        selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

        String sparqlQueryString = selectBuilder.buildString();

        return connectionToTDB.pullSingleDataFromTDB(this.compositionUpdateJSON.getJSONArray("directories").getString(arrayPosition), sparqlQueryString, "?s");

    }


    /**
     * This method finds the URI of a resource in the jena tdb and set the focus to this URI.
     * @param jsonInputObject contains the information for the calculation
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @param classToFocus contains the URI of a class to focus on
     */
    private void setFocusOnClass(JSONObject jsonInputObject, JenaIOTDBFactory connectionToTDB, String classToFocus) {

        if (classToFocus.equals("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575")) {
            // MDB user entry ID

            MDBResourceFactory mdbResourceFactory = new MDBResourceFactory();

            String potentialMDBUEID = mdbResourceFactory.createMDBUserEntryID(jsonInputObject.getString("mdbueid"));

            SelectBuilder selectWhereBuilder = new SelectBuilder();

            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

            selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

            selectWhereBuilder.addWhere("<" + potentialMDBUEID + ">", "rdf:type","<http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575>");

            AskBuilder askBuilder = new AskBuilder();

            askBuilder = prefixesBuilder.addPrefixes(askBuilder);

            askBuilder.addGraph("?g", selectWhereBuilder);

            String sparqlQueryString = askBuilder.buildString();

            TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

            boolean mdmUEIDExistInTDB = connectionToTDB.statementExistInTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), sparqlQueryString);

            if (mdmUEIDExistInTDB) {

                this.mdbUEID = potentialMDBUEID;

                this.mdbUEIDNotEmpty = true;

                this.currentFocus = this.mdbUEID;

                System.out.println("focus is on: " + this.currentFocus);

            }

        } else if (classToFocus.equals("http://www.morphdbase.de/Ontologies/MDB/MDBEntry#MDB_ENTRY_0000000030")) {
            // MDB entry ID

            this.mdbEntryID = jsonInputObject.getString("mdbentryid");

            this.mdbEntryIDNotEmpty = true;

            this.currentFocus = this.mdbEntryID;

            System.out.println("focus is on: " + this.currentFocus);


        }

    }


    /**
     * This method generates a resource. This resource is used for the generation of other dependent resources.
     * @param newFocusKey contains a key to find the new focus
     * @param currExecStep contains all information from the ontology for the current execution step
     * @param jsonInputObject contains the information for the calculation
     * @param generateResourceFor is an empty String
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a String with an URI
     */
    private String setFocusOnIndividual(String newFocusKey, JSONArray currExecStep, JSONObject jsonInputObject,
                                        String generateResourceFor, JenaIOTDBFactory connectionToTDB) {

        if (newFocusKey.contains("__MDB_UIAP_")) {

            newFocusKey = newFocusKey.substring(newFocusKey.indexOf("__") + 2);

            for (int i = 0; i < currExecStep.length();i++) {

                String localNameOfProperty = ResourceFactory.createResource(currExecStep.getJSONObject(i).getString("property")).getLocalName();

                if (localNameOfProperty.equals(newFocusKey)) {

                    generateResourceFor = currExecStep.getJSONObject(i).getString("object");

                    if (generateResourceFor.contains("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000575")) {
                        // MDB user entry ID

                        MDBResourceFactory mdbResourceFactory = new MDBResourceFactory();

                        this.mdbUEID = mdbResourceFactory.createMDBUserEntryID(jsonInputObject.getString("mdbueid"));

                        this.mdbUEIDNotEmpty = true;

                        this.currentFocus = this.mdbUEID;

                        System.out.println("mdbUEID = " + mdbUEID);

                    } else if (generateResourceFor.contains("http://www.morphdbase.de/Ontologies/MDB/MDBEntry#MDB_ENTRY_0000000030")) {
                        // MDB entry ID

                        MDBResourceFactory mdbResourceFactory = new MDBResourceFactory();

                        System.out.println();

                        boolean newCoreIDWasGenerated = false;

                        if (!this.mdbCoreIDNotEmpty) {

                            this.mdbCoreID = mdbResourceFactory.createMDBCoreID(currExecStep, this.infoInput, jsonInputObject, this.pathToOntologies, connectionToTDB);

                            this.mdbCoreIDNotEmpty = true;

                            newCoreIDWasGenerated = true;

                            System.out.println("MDBCoreID = " + this.mdbCoreID);

                        }

                        System.out.println();

                        if (newCoreIDWasGenerated) {
                            // if a new MDBCoreID was generated >>> this new MDBEntryID starts with the minimum

                            this.mdbEntryID = this.mdbCoreID + "-d_1_1";

                        } else {

                            this.mdbEntryID = mdbResourceFactory.createMDBEntryID(currExecStep, this.mdbCoreID, 'd', connectionToTDB);

                        }

                        // add info for later calculation
                        jsonInputObject.put("mdbentryid", this.mdbEntryID);

                        this.mdbEntryIDNotEmpty = true;

                        this.currentFocus = this.mdbEntryID;

                        this.focusHasNewNS = true;

                        System.out.println("MDBEntryID = " + this.mdbEntryID);

                        if (jsonInputObject.has("mdbueid_uri")) {

                            if (!(jsonInputObject.getString("mdbueid_uri").equals(""))) {

                                this.mdbUEID = jsonInputObject.getString("mdbueid_uri");

                            } else {

                                this.mdbUEID = mdbResourceFactory.findMDBUserEntryID(jsonInputObject.getString("mdbueid"), connectionToTDB);

                            }

                        } else {

                            this.mdbUEID = mdbResourceFactory.findMDBUserEntryID(jsonInputObject.getString("mdbueid"), connectionToTDB);

                        }

                        this.mdbUEIDNotEmpty = true;

                        System.out.println("mdbUEID = " + this.mdbUEID);

                        System.out.println();

                    }

                }

            }

        } else if (this.identifiedResources.has(newFocusKey)) {

            String potentialIndividualURI = this.identifiedResources.getString(newFocusKey).contains("#")
                    ? this.identifiedResources.getString(newFocusKey)
                    : this.identifiedResources.getString(newFocusKey) + "#Dummy_0000000000";

            MDBIDFinder mdbidFinder = new MDBIDFinder(potentialIndividualURI, connectionToTDB);

            if (mdbidFinder.hasMDBCoreID() &&
                    mdbidFinder.hasMDBEntryID() &&
                    mdbidFinder.hasMDBUEID()) {

                this.mdbUEID = mdbidFinder.getMDBUEID();

                this.mdbUEIDNotEmpty = true;

                this.mdbCoreID = mdbidFinder.getMDBCoreID();

                this.mdbCoreIDNotEmpty = true;

                this.mdbEntryID = mdbidFinder.getMDBEntryID();

                this.mdbEntryIDNotEmpty = true;

                // add info for later calculation
                jsonInputObject.put("mdbentryid", this.mdbEntryID);

                if (mdbidFinder.getMDBEntryID().equals(this.identifiedResources.getString(newFocusKey))) {

                    this.currentFocus = this.mdbEntryID;

                    this.focusHasNewNS = true;

                    generateResourceFor = this.identifiedResources.getString(newFocusKey);

                }

            }

        }

        return generateResourceFor;

    }


    /**
     * This method checks if a transition contains the property "mdbuiap:MDB_UIAP_0000000464" with object value "true".
     * @param transitionToCheck contains the URI of the transition
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return "true" if the statement exist, else "false"
     */
    private boolean updateStoreAfterTrackingProcedureExist(String transitionToCheck, JenaIOTDBFactory connectionToTDB) {

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        AskBuilder askBuilder = new AskBuilder();

        askBuilder = prefixesBuilder.addPrefixes(askBuilder);

        SelectBuilder tripleSPO = new SelectBuilder();

        UrlValidator urlValidator = new UrlValidator();

        if (urlValidator.isValid(transitionToCheck)) {

            tripleSPO.addWhere("<" + transitionToCheck + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000464>", "true");
            // update store after tracking procedure  [BOOLEAN]

        } else {

            tripleSPO.addWhere("<" + transitionToCheck + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000464>", "true");
            // update store after tracking procedure  [BOOLEAN]

        }

        askBuilder.addGraph("?g", tripleSPO);

        String sparqlQueryString = askBuilder.buildString();

        return connectionToTDB.statementExistInTDB(this.pathToOntologies, sparqlQueryString);

    }


    /**
     * This method checks for a property if the object of this property will be used as input for a transition or
     * workflow.
     * @param potentialInputProperty contains the uri of a property
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return "true" if the object of the property must use for later calculation else "false"
     */
    private boolean useObjectAsInput(String potentialInputProperty, JenaIOTDBFactory connectionToTDB) {

        SelectBuilder selectWhereBuilder = new SelectBuilder();

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

        selectWhereBuilder.addWhere("<" + potentialInputProperty + ">", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000400>","true");
        // MDB_UIAP_0000000400 >>> used in transition or workflow  [BOOLEAN]

        AskBuilder askBuilder = new AskBuilder();

        askBuilder = prefixesBuilder.addPrefixes(askBuilder);

        askBuilder.addGraph("?g", selectWhereBuilder);

        String sparqlQueryString = askBuilder.buildString();

        return connectionToTDB.statementExistInTDB(this.pathToOntologies, sparqlQueryString);

    }


}
