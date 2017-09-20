package mdb.testng.packages;

import org.apache.jena.rdf.model.Model;
import mdb.packages.MDBInputModelFactory;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class MDBInputModelFactory_Test {

    private JSONArray inputTripleJSONArray;

    @BeforeMethod
    public void MDBInputModelFactory_Test_SetUp() throws Exception {

        String tripleJSONInput = "[{" +
                "'subject':'http://test.com/s1','property':'http://test.com/p1', 'object':{'object_data':'o_d1', 'object_type':'l'}, 'operation':'s'" +
                "}]";

        String operationJSONInput = "[{'operation':'s'}]";

        this.inputTripleJSONArray = new JSONArray(tripleJSONInput);
    }

    @Test
    public void createMDBInputModel_CreateJenaModel_CheckType() throws Exception {

        MDBInputModelFactory mdbInputModelFactory = new MDBInputModelFactory();

        // get the test model for the input data
        ArrayList<Model> testArrayList = mdbInputModelFactory
                                        .createMDBInputModel(this.inputTripleJSONArray);

        // create the expected model type
        ArrayList<Model> expectedArrayList = new ArrayList<>();

        // check if the two object have the same type
        Assert.assertEquals(
                testArrayList.getClass(), expectedArrayList.getClass(), "Test model doesn't have the right class type.");

    }
}