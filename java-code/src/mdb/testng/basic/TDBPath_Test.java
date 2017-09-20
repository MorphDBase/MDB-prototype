package mdb.testng.basic;

import mdb.basic.TDBPath;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TDBPath_Test {

    @Test
    public void getPathToTDB_FindThePathToTDB_ReturnDataAsString() throws Exception {

        // create an instance of the class which should be tested
        TDBPath testTDBPath = new TDBPath("path/to/root/");

        // call the method to test
        String testString = testTDBPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000229");

        // check if the two object have the same type
        String expectedString = "";

        // check if the two object have the same type
        Assert.assertEquals(
                testString.getClass(),
                expectedString.getClass(), "Incorrect return type from the method: getPathToTDB");

    }

    @Test
    public void getPathToTDB_FindThePathToTDB_CheckContentOfString() throws Exception {

        // create an instance of the class which should be tested
        TDBPath testTDBPath = new TDBPath("path/to/root/");

        // call the method to test
        String testString = testTDBPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000229");

        String expectedString = "path/to/root/mdb_draft_workspace/";

        // check if the content is equal
        Assert.assertTrue(testString.equals(expectedString),
                "The return String of the method getPathToTDB is unequal to the expected String");

        // call the method to test
        testString = testTDBPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000226");

        expectedString = "path/to/root/mdb_published_workspace/";

        // check if the content is equal
        Assert.assertTrue(testString.equals(expectedString),
                "The return String of the method getPathToTDB is unequal to the expected String");

        // call the method to test
        testString = testTDBPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000065");

        expectedString = "path/to/root/mdb_core_workspace/";

        // check if the content is equal
        Assert.assertTrue(testString.equals(expectedString),
                "The return String of the method getPathToTDB is unequal to the expected String");

        // call the method to test
        testString = testTDBPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000082");

        expectedString = "path/to/root/mdb_admin_workspace/";

        // check if the content is equal
        Assert.assertTrue(testString.equals(expectedString),
                "The return String of the method getPathToTDB is unequal to the expected String");


    }
}