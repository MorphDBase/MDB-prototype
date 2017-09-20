package mdb.testng.packages;

import mdb.basic.DataFactory;
import mdb.packages.MDBJSONObjectFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class MDBJSONObjectFactory_Test {

    private ArrayList<ArrayList<String>> inputArrayListData = new ArrayList<>();

    private JSONObject inputJSONObject = new JSONObject();

    private JSONArray subjectJSONArray = new JSONArray();
    private JSONArray propertyJSONArray = new JSONArray();
    private JSONArray objectDataJSONArray = new JSONArray();
    private JSONArray objectTypeJSONArray = new JSONArray();
    private JSONArray operationJSONArray = new JSONArray();
    private JSONArray ngJSONArray = new JSONArray();
    private JSONArray directoryJSONArray = new JSONArray();

    @BeforeMethod
    public void setUp() throws Exception {

        subjectJSONArray.put("http://www.example.com/subject1");
        subjectJSONArray.put("http://www.example.com/subject2");
        propertyJSONArray.put("http://www.example.com/property1");
        propertyJSONArray.put("http://www.example.com/property2");
        objectDataJSONArray.put("http://www.example.com/object1");
        objectDataJSONArray.put("object2");
        objectTypeJSONArray.put("r");
        objectTypeJSONArray.put("l");
        operationJSONArray.put("s");
        operationJSONArray.put("d");
        ngJSONArray.put("http://www.example.com/ng1");
        ngJSONArray.put("http://www.example.com/ng2");
        directoryJSONArray.put("example/path/to/directory1");
        directoryJSONArray.put("example/path/to/directory2");


        // fill this JSON input object with data
        inputJSONObject.put("subject", subjectJSONArray);
        inputJSONObject.put("property", propertyJSONArray);
        inputJSONObject.put("object_data", objectDataJSONArray);
        inputJSONObject.put("object_type", objectTypeJSONArray);
        inputJSONObject.put("operation", operationJSONArray);
        inputJSONObject.put("ng", ngJSONArray);
        inputJSONObject.put("directory", directoryJSONArray);

        DataFactory dataFactory = new DataFactory();

        inputArrayListData = dataFactory.generateCoreIDNGData(inputJSONObject);

    }


    @Test
    public void convertKBToJSONObject_CalculateFromKB_ReturnDataAsJSONObject() throws Exception {
        //todo this test
    }


    @Test
    public void convertArrayListToJSONObject_ConvertMultipleArrayListToJSONObject_ReturnDataAsJSONObject()
            throws Exception {

        MDBJSONObjectFactory mdbjsonObjectFactory = new MDBJSONObjectFactory();

        // call the method to test;
        JSONObject testJSONObject = mdbjsonObjectFactory.convertArrayListToJSONObject(inputArrayListData);

        JSONObject expectedJSONObject = new JSONObject();

        // check if the two object have the same type
        Assert.assertEquals(
                testJSONObject.getClass(),
                expectedJSONObject.getClass(), "Incorrect return type from the method: convertArrayListToJSONObject");


    }

    @Test
    public void convertArrayListToJSONObject_ConvertMultipleArrayListToJSONObject_CheckContentOfJSONObjectIsEmpty()
            throws Exception {

        MDBJSONObjectFactory mdbjsonObjectFactory = new MDBJSONObjectFactory();


        // call the method to test;
        JSONObject testJSONObject = mdbjsonObjectFactory.convertArrayListToJSONObject(inputArrayListData);


        Assert.assertFalse(testJSONObject.isNull("datasets"),
                "The datasets value from the method convertArrayListToJSONObject is empty");

        Assert.assertFalse(testJSONObject.getJSONArray("datasets").getJSONObject(0).isNull("dataset"),
                "The dataset value from the method convertArrayListToJSONObject is empty");

        Assert.assertFalse(testJSONObject.getJSONArray("datasets").getJSONObject(0).isNull("ngs"),
                "The ngs value from the method convertArrayListToJSONObject is empty");

        Assert.assertFalse(
                testJSONObject
                        .getJSONArray("datasets").getJSONObject(0).getJSONArray("ngs").getJSONObject(0).isNull("ng"),
                "The ng value from the method convertArrayListToJSONObject is empty");

        Assert.assertFalse(
                testJSONObject
                        .getJSONArray("datasets").getJSONObject(0).getJSONArray("ngs").getJSONObject(0).isNull("triples"),
                "The triples value from the method convertArrayListToJSONObject is empty");

    }

}