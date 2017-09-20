package mdb.testng.packages;
/**
 * Created by christian on 16.04.15.
 * Last modified by Roman Baum on 21.04.15.
 */

import mdb.packages.JenaIOTDBFactory;
import mdb.packages.MDBDataset;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.block.FileMode;
import org.apache.jena.tdb.sys.SystemTDB;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;

public class JenaIOTDBFactory_Test {

    // path to the tdb store
    private String pathToTDB = "out/test_output/test_sample_store";

    // subject
    private Resource mdbSubject = ResourceFactory.createResource("http://www.example.com/s1");

    // property
    private Property mdbProperty = ResourceFactory.createProperty("http://www.example.com/p1");

    // object
    private Literal mdbObject = ResourceFactory.createPlainLiteral("o1");

    // subject
    private Resource mdbSubject2 = ResourceFactory.createResource("http://www.example.com/s2");

    // property
    private Property mdbProperty2 = ResourceFactory.createProperty("http://www.example.com/p2");

    // object
    private Literal mdbObject2 = ResourceFactory.createPlainLiteral("o2");

    // named graph
    private String namedModelString = "http://www.example.com/NG1";

    // SPARQL query
    private String sparqlQueryString = "";

    private String inputFileName = "testInput.owl";
    private String outputPath = "out/test_output";

    private Model getMDBTestModel() {

        // create a default model
        Model createJenaModel = ModelFactory.createDefaultModel();

        // create a statement
        Statement currStatement = ResourceFactory.createStatement(this.mdbSubject, this.mdbProperty, this.mdbObject);

        // add a statement to the model
        createJenaModel.add(currStatement);

        return createJenaModel;
    }

    private Model getMultipleMDBTestModel() {

        // create a default model
        Model createJenaModel = ModelFactory.createDefaultModel();

        // create a statement
        Statement currStatement = ResourceFactory.createStatement(this.mdbSubject, this.mdbProperty, this.mdbObject);

        // add a statement to the model
        createJenaModel.add(currStatement);

        currStatement = ResourceFactory.createStatement(this.mdbSubject2, this.mdbProperty2, this.mdbObject2);

        // add a statement to the model
        createJenaModel.add(currStatement);

        return createJenaModel;
    }

    @BeforeClass
    public void pullDataFromTDB_SetUp() {

        ConstructBuilder constructBuilder = new ConstructBuilder();

        constructBuilder.addConstruct("?s", "?p", "?o");

        constructBuilder.fromNamed("http://www.example.com/NG1");

        SelectBuilder selectBuilder = new SelectBuilder();

        selectBuilder.addWhere("?s", "?p", "?o");

        constructBuilder.addGraph("?g", selectBuilder);

        this.sparqlQueryString = constructBuilder.buildString();

        File dir = new File(this.outputPath);
        dir.mkdir();
    }

    @AfterClass
    public void JenaIOTDBFactory_Test_tearDown() {

        MDBDataset mdbDataset = new MDBDataset();

        // delete the dataset directory
        mdbDataset.deleteDatasetDirectory(this.pathToTDB);

        // delete the output directory
        mdbDataset.deleteDatasetDirectory(this.outputPath);

    }


    @BeforeGroups(groups = {"addModelDataInTDB", "pullDataFromTDB", "removeModelDataInTDB", "removeNamedModelFromTDB"})
    public void JenaIOTDBFactory_Test_setUp() {

        // reduce the size of the TDB
        TDB.getContext().set(SystemTDB.symFileMode, FileMode.direct);

        // get a working model
        Model workingModel = getMDBTestModel();

        // create sample triple for the tdb storage
        JenaIOTDBFactory mdbTDBFactory = new JenaIOTDBFactory();

        // save the model to the tdb
        mdbTDBFactory.addModelDataInTDB(this.pathToTDB, this.namedModelString, workingModel);
    }

    @BeforeGroups(groups = {"removeModelsFromTDB"})
    public void removeModelsFromTDB_setUp() {

        // reduce the size of the TDB
        TDB.getContext().set(SystemTDB.symFileMode, FileMode.direct);

        // get a working model
        Model workingModel = getMultipleMDBTestModel();

        // create sample triple for the tdb storage
        JenaIOTDBFactory mdbTDBFactory = new JenaIOTDBFactory();

        // create and fill the expected array list for the names of the named graphs
        ArrayList<String> namedModelAL = new ArrayList<>();
        namedModelAL.add(this.namedModelString);

        // create and fill the expected array list for the named graphs
        ArrayList<Model> workingModelAL = new ArrayList<>();
        workingModelAL.add(workingModel);

        // save the model to the tdb
        mdbTDBFactory.addModelsInTDB(pathToTDB, namedModelAL, workingModelAL);

    }

    @BeforeGroups(groups = {"pushDataInADefaultGraph", "pushDataInANamedGraph"})
    public void pushDataInTDB_SetUp() {

        // create a TDB-dataset
        TDBFactory.createDataset(pathToTDB);
    }

    @Test(groups = "addModelDataInTDB")
    public void addModelDataInTDB_ReadFromTDB_CheckContentInModelIsEmpty() throws Exception{

        // get a test model
        Model testModel = getMDBTestModel();

        // create an instance of the class which should be tested
        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        String testModelString = jenaIOTDBFactory.addModelDataInTDB(this.pathToTDB, this.namedModelString, testModel);

        // check if the two object instances contains the same data
        Assert.assertEquals(
                testModelString,
                "The triple was successfully saved in the named graph called: " + this.namedModelString,
                "Can't find triple in Jena tdb. Maybe the triple was not save.");

    }

    @Test(groups = "pullDataFromTDB")
    public void pullDataFromTDB_ReadFromTDB_ReturnDataAsModel() throws Exception {

        // create an instance of the class which should be tested
        JenaIOTDBFactory mdbTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        Model testModel = mdbTDBFactory.pullDataFromTDB(this.pathToTDB, this.sparqlQueryString);

        Model expectedJenaModel = ModelFactory.createDefaultModel();

        // check if the two object have the same type
        Assert.assertEquals(
                testModel.getClass(), expectedJenaModel.getClass(), "Incorrect model object from Jena tdb.");
    }

    @Test(groups = "pullDataFromTDB")
    public void pullDataFromTDB_ReadFromTDB_CheckContentInModel() throws Exception {

        // create an instance of the class which should be tested
        JenaIOTDBFactory mdbTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        Model testModel = mdbTDBFactory.pullDataFromTDB(this.pathToTDB, this.sparqlQueryString);

        // get the expected model
        Model expectedModel = getMDBTestModel();

        // check if the two object instances contains the same data
        Assert.assertEquals(
                testModel.toString(), expectedModel.toString(), "Incorrect model object from Jena tdb.");
    }

    @Test(groups = "pullDataFromTDB")
    public void pullDataFromTDB_ReadFromTDB_CheckIfFileWasGenerated() throws Exception {

        // create an instance of the class which should be tested
        JenaIOTDBFactory mdbTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        mdbTDBFactory.pullDataFromTDB(this.pathToTDB, this.outputPath + "/test-output", this.sparqlQueryString);

        boolean testDirectoryIsEmpty = false;

        File file = new File("out/test_output");

        if (file.list().length > 0) {
            testDirectoryIsEmpty = true;
        }

        // check if the file was generated
        Assert.assertTrue(testDirectoryIsEmpty, "Output directory is empty.");
    }

    @Test(groups = "pullDataFromTDB")
    public void pullStringDataFromTDB_ReadFromTDB_ReturnDataAsString() throws Exception{

        // create an instance of the class which should be tested
        JenaIOTDBFactory mdbTDBFactory = new JenaIOTDBFactory();

        // create a Query
        Query sparqlQuery = QueryFactory.create(sparqlQueryString);

        // call the method to test
        String testStringType = mdbTDBFactory.pullStringDataFromTDB(pathToTDB, sparqlQuery, "RDF/XML");

        String expectedStringType = "";

        // check if the two object have the same type
        Assert.assertEquals(
                testStringType.getClass(), expectedStringType.getClass(), "Incorrect string from Jena tdb.");

    }

    @Test(groups = "pushDataInADefaultGraph")
    public void pushDataInTDB_WriteDefaultGraphToTDB_CheckContentIsNotEmpty() throws Exception{

        // create an instance of the class which should be tested
        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // push the test file to the tdb
        jenaIOTDBFactory.pushDataInTDB(this.pathToTDB, this.inputFileName);

        // get the test data set to find the model in the next steps
        Dataset testDataset = TDBFactory.createDataset(this.pathToTDB);

        // check if the test model in the tdb was generated
        testDataset.begin( ReadWrite.READ );
        try {

            Model testModel = testDataset.getDefaultModel();

            // check if the default model exist in the tdb
            Assert.assertFalse(testModel.isEmpty(), "Model in TDB doesn't exist.");

        } finally {
            // close the dataset
            testDataset.end();
        }

    }

    @Test(groups = "pushDataInANamedGraph")
    public void pushDataInTDB_WriteNamedGraphToTDB_CheckContentIsNotEmpty() throws Exception{

        // create an instance of the class which should be tested
        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // push the test file to the tdb
        jenaIOTDBFactory.pushDataInTDB(this.pathToTDB, this.namedModelString , this.inputFileName);

        // get the test data set to find the model in the next steps
        Dataset testDataset = TDBFactory.createDataset(this.pathToTDB);

        // check if the test model in the tdb was generated
        testDataset.begin( ReadWrite.READ );
        try {

            Model testModel = testDataset.getNamedModel(this.namedModelString);

            // check if the named model exist in the tdb
            Assert.assertFalse(testModel.isEmpty(), "Model in TDB doesn't exist.");

        } finally {
            // close the dataset
            testDataset.end();
        }

    }

    @Test(groups = "removeModelDataInTDB")
    public void removeModelDataInTDB_ReadFromTDB_CheckContentInModelIsEmpty() throws Exception{

        // get a test model
        Model testModel = getMDBTestModel();

        // create an instance of the class which should be tested
        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        String testModelString = jenaIOTDBFactory.removeModelDataInTDB(this.pathToTDB, this.namedModelString, testModel);

        // check if the two object instances contains the same data
        Assert.assertEquals(
                testModelString,
                "The triple was successfully removed from the named graph called: " + this.namedModelString,
                "Triple content from Jena tdb is not empty.");

    }

    @Test(groups = {"removeModelsFromTDB"})
    public void removeModelsFromTDB_WriteInTDB_CheckDataIsNoLongerInTDB() throws Exception {

        // put the name of the model to test to the expected input array list
        ArrayList<String> namedModelAL = new ArrayList<>();
        namedModelAL.add(this.namedModelString);

        // get a test model
        Model testModel = getMDBTestModel();

        // put the test model to the expected input array list
        ArrayList<Model> testModelAL = new ArrayList<>();
        testModelAL.add(testModel);

        // create an instance of the class which should be tested
        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        jenaIOTDBFactory.removeModelsFromTDB(this.pathToTDB, namedModelAL, testModelAL);

        // build a sub query for the where clause
        SelectBuilder selectBuilder = new SelectBuilder();

        // fill this query with content
        selectBuilder.addWhere(this.mdbSubject, this.mdbProperty, this.mdbObject);

        // build the query to test if the statement was deleted or not
        AskBuilder askBuilder = new AskBuilder();

        // create a resource for the named graph
        Resource namedModelRes = ResourceFactory.createResource(this.namedModelString);

        // add the graph to the query build
        askBuilder.addGraph(namedModelRes, selectBuilder);

        // convert the build query to a String
        String askSPARQLQueryString = askBuilder.buildString();

        // create a Query
        Query askSPARQLQuery = QueryFactory.create(askSPARQLQueryString);

        // get the result of the query
        String booleanTestString = jenaIOTDBFactory.pullStringDataFromTDB(this.pathToTDB, askSPARQLQuery,"RDF/XML");

        // check if the statement exist in the tdb or not
        Assert.assertFalse(Boolean.valueOf(booleanTestString), "The Statement exist in TDB.");


    }

    @Test(groups = "removeNamedModelFromTDB")
    public void removeNamedModelFromTDB_ReadFromTDB_CheckNamedGraphIsEmpty() throws Exception{

        // create an instance of the class which should be tested
        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // call the method to test
        jenaIOTDBFactory.removeNamedModelFromTDB(this.pathToTDB, this.namedModelString);

        // get the test data set to find the model in the next steps
        Dataset testDataset = TDBFactory.createDataset(this.pathToTDB);

        // check if the test model in the tdb was deleted
        testDataset.begin( ReadWrite.READ );
        try {

            Model testModel = testDataset.getNamedModel(this.namedModelString);

            // check if the named model exist in the tdb
            Assert.assertTrue(testModel.isEmpty(), "Model in TDB exist.");

        } finally {
            // close the dataset
            testDataset.end();
        }

    }
}
