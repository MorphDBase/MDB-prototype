package mdb.testng.basic;

import mdb.basic.OrderDataset;
import mdb.vocabulary.OntologiesPath;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class OrderDataset_Test {

    @Test
    public void sortGenerateResources_GenerateJSON_ReturnSortetJSON() throws Exception {

//        public ArrayList<String> sortGenerateResources (JSONArray unorderedGenerateRes, String pathToOntologies) {
//
//            ArrayList<Integer> indices = new ArrayList<>();
//
//            JSONObject genericResourcesJSONObject = new JSONObject();

/*        JSONObject unorderedObject1 = new JSONObject();
        unorderedObject1.put("position", "1");
        unorderedObject1.put("username", "kfrog");
        JSONObject unorderedObject2 = new JSONObject();
        unorderedObject2.put("position", "3");
        unorderedObject2.put("username", "tuser");
        JSONObject unorderedObject3 = new JSONObject();
        unorderedObject3.put("position", "2");
        unorderedObject3.put("username","adent");

        JSONArray unorderedArray = new JSONArray();
        unorderedArray.put(unorderedObject1);
        unorderedArray.put(unorderedObject2);
        unorderedArray.put(unorderedObject3);

        OrderDataset orderDataset = new OrderDataset();

        // sort resources with numbers in string
        ArrayList<String> sortedResources = orderDataset.sortGenerateResources(unorderedArray, OntologiesPath.pathToOntology);

        System.out.println("sortedResources: " + sortedResources);*/
    }
}