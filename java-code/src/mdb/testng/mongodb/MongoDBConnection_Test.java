package mdb.testng.mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import mdb.mongodb.MongoDBConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.testng.Assert;

public class MongoDBConnection_Test {

    MongoDBConnection mongoDBConnection = new MongoDBConnection("localhost", 27017);

    @BeforeMethod
    public void setUp() throws Exception {
        if (!mongoDBConnection.collectionExist("mdb-prototyp", "testng")) {
            mongoDBConnection.createCollection("mdb-prototyp", "testng");
            System.out.println("Collection testng does now exist.");

        }

        Assert.assertTrue(
                mongoDBConnection.collectionExist("mdb-prototyp", "testng")
        );
    }

    @AfterClass
    public void close() {

        this.mongoDBConnection.closeConnection();

    }

    @Test
    public void createCollection_CheckIfCollectionExists_ReturnIfCollectionExists() throws Exception {
        // create mongoDBCollection
        mongoDBConnection.createCollection("mdb-prototyp", "createcollectiontest");

        // check if the collection exists
        Assert.assertTrue(
                mongoDBConnection.collectionExist("mdb-prototyp", "createcollectiontest")
        );

        // drop mongoDBCollection
        mongoDBConnection.dropCollection("mdb-prototyp", "createcollectiontest");
    }

    @Test
    public void collectionExist_CreateCollection_ReturnIfCollectionExists() throws Exception {
        // check if the collection already exists
        Assert.assertFalse(
                mongoDBConnection.collectionExist("mdb-prototyp", "testngtemp")
        );

        if (mongoDBConnection.collectionExist("mdb-prototyp", "testngtemp")) {
            System.out.println("Collection testngtemp already exist.");
            mongoDBConnection.dropCollection("mdb-prototyp", "testngtemp");
        }
        else{
            System.out.println("Collection testngtemp doesn't yet exist.");

            mongoDBConnection.createCollection("mdb-prototyp", "testngtemp");

            if (mongoDBConnection.collectionExist("mdb-prototyp", "testngtemp")) {
                System.out.println("Collection testngtemp does now exist.");

                mongoDBConnection.dropCollection("mdb-prototyp", "testngtemp");

                System.out.println("Collection testngtemp does not longer exist.");

            }
        }
    }

    @Test
    public void insertDataToMongoDB_CreateDocuments_ReturnIfDocumentsExist() throws Exception {

        String returnString;

        JSONObject jo = new JSONObject();
        jo.put("firstName", "John");
        jo.put("lastName", "Snow");
        jo.put("username", "jsnow");

        JSONArray ja = new JSONArray();
        ja.put(jo);

        JSONArray keyarray = new JSONArray();
        keyarray.put("key_one");
        keyarray.put("key_two");
        keyarray.put("key_three");

        // public String insertDataToMongoDB (String db, String collection, String key, String value)
        returnString = mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "testng", "testkey", "testvalue");
        System.out.println(returnString);
        Assert.assertTrue(mongoDBConnection.documentExist("mdb-prototyp", "testng", "testkey", "testvalue"));

        //public String findObjectID (String db, String collection, String key, String value) {}
        String returnObjID = mongoDBConnection.findObjectID ("mdb-prototyp", "testng", "testkey", "testvalue");
        System.out.println(returnObjID);

        // public String insertDataToMongoDB (String db, String collection, String id, String key, String value)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", returnObjID, "testkey", "anothertestvalue");
        System.out.println(returnString);
        Assert.assertTrue(mongoDBConnection.documentExist("mdb-prototyp", "testng", "testkey", "anothertestvalue"));

        // public String insertDataToMongoDB (String db, String collection, String key, JSONArray valuesInJSON)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", "testchild", ja);
        System.out.println(returnString);

        //public String insertDataToMongoDB (String db, String collection, JSONArray keys, String value)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", keyarray, "testvalue");
        System.out.println(returnString);

    }

    @Test
    public void findUserByUsername_InsertDocument_ReturnUser() throws Exception {

        String returnString;

        // public String insertDataToMongoDB (String db, String collection, String key, JSONArray valuesInJSON)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", "username", "kfrog");
        System.out.println(returnString);

        //public String findObjectID (String db, String collection, String key, String value) {}
        String returnObjID = mongoDBConnection.findObjectID ("mdb-prototyp", "testng", "username", "kfrog");
        System.out.println(returnObjID);

        // public String insertDataToMongoDB (String db, String collection, String id, String key, String value)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", returnObjID, "firstname", "Kermit");
        System.out.println(returnString);
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", returnObjID, "lastname", "TheFrog");
        System.out.println(returnString);

        // public boolean findUserByUsername(String db, String collection, String userToFind)
        Assert.assertTrue(mongoDBConnection.findUserByUsername("mdb-prototyp", "testng", "kfrog"));

    }

    @Test
    public void documentExist_CreateDocuments_ReturnIfDocumentsExist() throws Exception {

        if (!mongoDBConnection.documentExist("mdb-prototyp", "testng", "testkey", "testvaluetoo")) {

            mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "testng", "testkey", "testvaluetoo");
        }

        Assert.assertTrue(mongoDBConnection.documentExist("mdb-prototyp", "testng", "testkey", "testvaluetoo"));

        JSONObject jo = new JSONObject();
        jo.put("firstName", "Arthur");
        jo.put("lastName", "Dent");
        jo.put("username", "adent");

        JSONArray ja = new JSONArray();
        ja.put(jo);

        if (!mongoDBConnection.documentWithDataExist("mdb-prototyp", "testng", "testchild", ja)) {

            mongoDBConnection.insertDataToMongoDB("mdb-prototyp", "testng", "testchild", ja);

        }

        Assert.assertTrue(mongoDBConnection.documentWithDataExist("mdb-prototyp", "testng", "testchild", ja));

        // create a mongoDB object
        BasicDBObject bo = new BasicDBObject();
        bo.append("firstName", "Arthur");
        bo.append("lastName", "Dent");
        bo.append("username", "adent");

        // create a mongoDB list
        BasicDBList bl = new BasicDBList();
        bl.add(bo);

        Assert.assertTrue(mongoDBConnection.documentExist("mdb-prototyp", "testng", "testchild", bl));


    }

    @Test
    public void findObjectID_InsertData_ReturnObjID() throws Exception {

        String testString = "57cd8ce23db5914bf7e66b81";
        String returnString;

        // public String insertDataToMongoDB (String db, String collection, String key, JSONArray valuesInJSON)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", "username", "jlpicard");
        System.out.println(returnString);

        //public String findObjectID (String db, String collection, String key, String value) {}
        String returnObjID = mongoDBConnection.findObjectID ("mdb-prototyp", "testng", "username", "jlpicard");
        System.out.println(returnObjID);

        // check if the two object have the same type
        Assert.assertEquals(
                returnObjID.getClass(),
                testString.getClass(), "Incorrect return type from the method: findObjectID");
    }

    @Test
    public void setId_InsertDataAndSetId_ReturnId() throws Exception {

        String returnString;
        // this.dbIdentifier = new BasicDBObject("_id", new ObjectId(id));


        // public String insertDataToMongoDB (String db, String collection, String key, JSONArray valuesInJSON)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", "username", "astark");
        System.out.println(returnString);

        //public String findObjectID (String db, String collection, String key, String value) {}
        String returnObjID = mongoDBConnection.findObjectID ("mdb-prototyp", "testng", "username", "astark");
        System.out.println(returnObjID);

        mongoDBConnection.setId(returnObjID);
        BasicDBObject newIdDBObj = mongoDBConnection.getId();
        String newId = newIdDBObj.get("_id").toString();

                // check if the two object have the same type
        Assert.assertEquals(
                returnObjID,
                newId, "Incorrect return type from the method: getId");

    }

    @Test
    public void pullDataFromMongoDB_findObjectID_CompareResults() throws Exception {

        String returnString, returnStringOne, returnStringTwo, returnStringThree;

        // public String insertDataToMongoDB (String db, String collection, String key, JSONArray valuesInJSON)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", "username", "adent");
        System.out.println(returnString);

        // public boolean findUserByUsername(String db, String collection, String userToFind)
        Assert.assertTrue(mongoDBConnection.findUserByUsername("mdb-prototyp", "testng", "adent"));

        String returnObjID = mongoDBConnection.findObjectID ("mdb-prototyp", "testng", "username", "adent");
        System.out.println("pullDataFromMongoDBWithLocalID - ObjID of \"adent\": " + returnObjID);
        mongoDBConnection.setId(returnObjID);

        // public String insertDataToMongoDB (String db, String collection, String id, String key, String value)
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", returnObjID, "firstname", "Arthur");
        System.out.println(returnString);
        returnString = mongoDBConnection.insertDataToMongoDB ("mdb-prototyp", "testng", returnObjID, "lastname", "Dent");
        System.out.println(returnString);

        // public String pullDataFromMongoDBWithLocalID(String db, String collection)
        returnStringOne = mongoDBConnection.pullDataFromMongoDB("mdb-prototyp", "testng");
        System.out.println("pullDataFromMongoDBWithLocalID " + returnStringOne);

        // public String pullDataFromMongoDBWithLocalID(String db, String collection, String id)
        returnStringTwo = mongoDBConnection.pullDataFromMongoDB("mdb-prototyp", "testng", returnObjID);
        System.out.println("pullDataFromMongoDBWithLocalID " + returnStringTwo);

        // check if the two object have the same type
        Assert.assertEquals(
                returnStringOne,
                returnStringTwo, "Incorrect return type from the method: pullDataFromMongoDBWithLocalID");

        // public String pullDataFromMongoDBWithLocalID(String db, String collection, String id)
        mongoDBConnection.putDataToMongoDB("mdb-prototyp", "testng", "comment", "\'I like the cover,\' he said. \'Don't Panic. It's the first helpful or intelligible thing anybody\'s said to me all day.\'");
        returnStringThree = mongoDBConnection.pullDataFromMongoDB("mdb-prototyp", "testng");
        System.out.println("pullDataFromMongoDBWithLocalID " + returnStringThree);

        // check if the two object have the same type
        /*Assert.assertEquals(
                returnStringOne,
                returnStringThree, "Incorrect return type from the method: pullDataFromMongoDBWithLocalID");*/


        String connectSID, htmlForm, localID, classID, individualID, keyword, newChild;
        connectSID = "s:42-HALLOHIERALARMEINS1ELF-TTESTT.SANDRASGANZPERSOENLICHEMDBPROTOTYPSESSIONID";
        htmlForm = "GUI_COMPONENT_4711";
        localID = "MDB_DATASCHEME_1701";
        classID = "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_1701";
        individualID = "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_1701";
        keyword = "MDB_GUI_007";

        newChild = "guicomponent4711";

        if (!mongoDBConnection.collectionExist("mdb-prototyp", connectSID)) {
            mongoDBConnection.createCollection("mdb-prototyp", connectSID);
        }

        JSONObject inputJO = new JSONObject();
        inputJO.put("connectSID", connectSID);
        inputJO.put("html_form", htmlForm);
        inputJO.put("localID", localID);
        inputJO.put("classID", classID);
        inputJO.put("individualID", individualID);
        inputJO.put("keyword", keyword);


        JSONArray inputJA = new JSONArray();
        inputJA.put(inputJO);

        if (!mongoDBConnection.documentWithDataExist("mdb-prototyp", connectSID, newChild, inputJA)) {
            mongoDBConnection.insertDataToMongoDB("mdb-prototyp", connectSID, newChild, inputJA);
        }

        JSONObject searchJO = new JSONObject();
        searchJO.put("connectSID", connectSID);
        searchJO.put("html_form", htmlForm);
        searchJO.put("localID", localID);

        // public JSONObject pullDataFromMongoDBWithLocalID(JSONObject jsonToFindData)
        JSONArray returnJA = mongoDBConnection.pullListFromMongoDB(searchJO);

        // check if the object have the correct type
        Assert.assertEquals(
                inputJA.getClass(),
                returnJA.getClass(), "Incorrect return type from the method: pullDataFromMongoDBWithLocalID");


        // public JSONObject pullDataFromMongoDBWithLocalID(JSONObject jsonToFindData)
        JSONObject returnJO = mongoDBConnection.pullDataFromMongoDBWithLocalID(searchJO);

        // check if the objects are equal
        Assert.assertEquals(
                inputJO.toString(),
                returnJO.toString(), "Incorrect return from the method: pullDataFromMongoDBWithLocalID");

        // check if the object have the correct type
        Assert.assertEquals(
                inputJO.getClass(),
                returnJO.getClass(), "Incorrect return type from the method: pullDataFromMongoDBWithLocalID");

        mongoDBConnection.dropCollection("mdb-prototyp", connectSID);
    }

    @AfterMethod
    public void tearDown_() throws Exception {
        mongoDBConnection.dropCollection("mdb-prototyp", "testng");
        System.out.println("Collection testng does not longer exist.");

        Assert.assertFalse(
                mongoDBConnection.collectionExist("mdb-prototyp", "testng")
        );
    }
}