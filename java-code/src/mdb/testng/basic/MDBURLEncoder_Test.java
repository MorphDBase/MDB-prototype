package mdb.testng.basic;

import mdb.basic.MDBURLEncoder;
import org.apache.commons.validator.routines.UrlValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MDBURLEncoder_Test {

    @Test
    public void encodeUrl_TransformInputURI_ReturnDataAsString() throws Exception {

        // create an instance of the class which should be tested
        MDBURLEncoder testMDBURLEncoder = new MDBURLEncoder();

        // call the method to test
        String testString = testMDBURLEncoder.encodeUrl("http://example.com/test", "UTF-8");

        // check if the two object have the same type
        String expectedString = "";

        // check if the two object have the same type
        Assert.assertEquals(
                testString.getClass(),
                expectedString.getClass(), "Incorrect return type from the method: encodeUrl");

    }


    @Test
    public void encodeUrl_TransformInputURI_CheckContentOfString() throws Exception {

        // create an instance of the class which should be tested
        MDBURLEncoder testMDBURLEncoder = new MDBURLEncoder();

        // define an input URI
        String testURI = "http://example.com/special/char/ä/ö/ü/ß";

        // get an url validator to check the input
        UrlValidator testValidator = new UrlValidator();

        Assert.assertFalse(testValidator.isValid(testURI),
                "The return String is a valid URI");

        // call the method to test
        Assert.assertTrue(testValidator.isValid(testMDBURLEncoder.encodeUrl(testURI, "UTF-8")),
                "The return String is not a valid URI");

    }

    @Test
    public void encodeUrl_InputIsNoURI_CheckContentOfString() throws Exception {

        // create an instance of the class which should be tested
        MDBURLEncoder testMDBURLEncoder = new MDBURLEncoder();

        // define an input URI
        String testURI = "MDB_UIAP_0000000115";

        // get an url validator to check the input
        UrlValidator testValidator = new UrlValidator();

        Assert.assertFalse(testValidator.isValid(testURI),
                "The return String is a valid URI");

        // call the method to test
        Assert.assertEquals(testMDBURLEncoder.encodeUrl(testURI, "UTF-8"), "The input is no URI!",
                "The input String is no URI!");

    }

}