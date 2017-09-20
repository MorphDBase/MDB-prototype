package mdb.testng.basic;

import mdb.basic.DataFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class DataFactory_Test {

    private JSONObject testJSONObject = new JSONObject();

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
        testJSONObject.put("subject", subjectJSONArray);
        testJSONObject.put("property", propertyJSONArray);
        testJSONObject.put("object_data", objectDataJSONArray);
        testJSONObject.put("object_type", objectTypeJSONArray);
        testJSONObject.put("operation", operationJSONArray);
        testJSONObject.put("ng", ngJSONArray);
        testJSONObject.put("directory", directoryJSONArray);

    }




    @Test
    public void generateCoreIDNGData_ConvertJSONObjectToMultipleArrayList_ReturnDataAsMultipleArrayList()
                                                                                                throws Exception {

        System.out.println(testJSONObject);



        DataFactory testFactory = new DataFactory();



        // call the method to test
        ArrayList<ArrayList<String>> testArrayListTriplesType = testFactory.generateCoreIDNGData(testJSONObject);

        // create an instance of the expected type
        ArrayList expectedArrayListTripleType;
        expectedArrayListTripleType = new ArrayList();

        ArrayList<ArrayList> expectedArrayListTriplesType = new ArrayList<>();

        expectedArrayListTriplesType.add(expectedArrayListTripleType);


        // check if the two object have the same type
        Assert.assertEquals(
                testArrayListTriplesType.getClass(),
                expectedArrayListTriplesType.getClass(), "Incorrect return type from the method: generateCoreIDNGData");

    }

    @Test
    public void generateCoreIDNGData_ConvertJSONObjectToMultipleArrayList_CheckContentOfArrayListIsEmpty()
            throws Exception {

        DataFactory testFactory = new DataFactory();



        // call the method to test
        ArrayList<ArrayList<String>> testArrayListData = testFactory.generateCoreIDNGData(testJSONObject);

        for (ArrayList<String> currTestArrayListData : testArrayListData) {

            Assert.assertFalse(currTestArrayListData.get(0).isEmpty(),
                    "The subject from the method generateCoreIDNGData is empty");

            Assert.assertFalse(currTestArrayListData.get(1).isEmpty(),
                    "The property from the method generateCoreIDNGData is empty");

            Assert.assertFalse(currTestArrayListData.get(2).isEmpty(),
                    "The object data from the method generateCoreIDNGData is empty");

            Assert.assertFalse(currTestArrayListData.get(3).isEmpty(),
                    "The object type from the method generateCoreIDNGData is empty");

            Assert.assertFalse(currTestArrayListData.get(4).isEmpty(),
                    "The operation from the method generateCoreIDNGData is empty");

            Assert.assertFalse(currTestArrayListData.get(5).isEmpty(),
                    "The ng from the method generateCoreIDNGData is empty");

            Assert.assertFalse(currTestArrayListData.get(6).isEmpty(),
                    "The directory from the method generateCoreIDNGData is empty");

        }

    }


}