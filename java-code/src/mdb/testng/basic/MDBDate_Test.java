package mdb.testng.basic;

import mdb.basic.MDBDate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MDBDate_Test {

    private MDBDate testMDBDate = new MDBDate();

    @Test
    public void getDate_CreateDateString_ReturnDataAsString() throws Exception {

        // create an instance of the class which should be tested

        // call the method to test
        String testMDBDateStringType = this.testMDBDate.getDate();

        String expectedMDBDateStringType = "";

        // check if the two object have the same type
        Assert.assertEquals(
                testMDBDateStringType.getClass(),
                expectedMDBDateStringType.getClass(), "Incorrect String from the method: getDate");

    }
}