package mdb.testng.basic;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import mdb.basic.MDBUser;

public class MDBUser_Test {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @Test
    public void createMDBUser_CreateUser_ReturnUser() throws Exception {

        // create test input strings
        String mdbUser = "adent";

        MDBUser mdbTestUser = new MDBUser(mdbUser);

        String mdbUser_Test = mdbTestUser.getMDBUser();
        System.out.println(mdbUser_Test);

        // check if the two strings are the same
        Assert.assertEquals(
                mdbUser,
                mdbUser_Test, "Incorrect return from the method: getMDBUser");
    }

    @Test
    public void createMDBUser_CreateUser_ReturnUserResource() throws Exception {

        // create test input strings
        String mdbUser = "adent";
        String mdbUserResource = "http://www.morphdbase.de/Ontologies/MDB/USER#" + mdbUser;

        MDBUser mdbTestUser = new MDBUser(mdbUser);

        String mdbUserResource_Test = mdbTestUser.getMDBUserResource();
        System.out.println(mdbUserResource_Test);

        // check if the two strings are the same
        Assert.assertEquals(
                mdbUserResource,
                mdbUserResource_Test, "Incorrect return from the method: getMDBUserResource");
    }
}