/*
 * Created by Roman Baum on 29.05.15.
 * Last modified by Roman Baum on 17.08.15.
 */
package mdb.basic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * The public method of this Class generate a two-dimensional ArrayList. This ArrayList contains the following kind of
 * data: subject, property, object_data, object_type, operation, named graphs and directories.
 */
public class DataFactory {

    /**
     * generate an ArrayList and sort the unordered input data
     * @param currInputDataJSONObject contains the unordered data
     * @return the sorted data in an ArrayList
     */
    private ArrayList<String> generateCurrTriple (JSONObject currInputDataJSONObject) {

        ArrayList<String> currGeneratedCoreIDData = new ArrayList<>();

        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("subject")); // 0
        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("property")); // 1
        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("object_data")); // 2
        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("object_type")); // 3
        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("operation")); // 4
        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("ng"));// 5
        currGeneratedCoreIDData.add(currInputDataJSONObject.getString("directory"));// 6


        return currGeneratedCoreIDData;

    }

    /**
     * collect the sorted data ArrayLists in a second ArrayList
     * @param currInputDataJSONObject contains the unordered data
     * @param generatedCoreIDData contains the sorted data
     * @return the sorted data in an two-dimensional ArrayList
     */
    private ArrayList<ArrayList<String>> generateCurrCoreIDNGData(JSONObject currInputDataJSONObject,
                                                                  ArrayList<ArrayList<String>> generatedCoreIDData) {


        ArrayList<String> currGeneratedCoreIDData = generateCurrTriple(currInputDataJSONObject);

        generatedCoreIDData.add(currGeneratedCoreIDData);

        return generatedCoreIDData;
    }


    /**
     * generate a two-dimensional sorted ArrayList. This ArrayList contains the following kind of data: subject,
     * property, object_data, object_type, operation, named graphs and directories.
     * @param inputDataJSONObjects contains the complete unordered input data
     * @return the sorted data in an two-dimensional ArrayList
     */
    public ArrayList<ArrayList<String>> generateCoreIDNGData(JSONObject inputDataJSONObjects) {


        ArrayList<ArrayList<String>> generatedCoreIDData = new ArrayList<>();

        JSONArray inputSubjectJSONArray = inputDataJSONObjects.getJSONArray("subject");
        JSONArray inputPropertyJSONArray = inputDataJSONObjects.getJSONArray("property");
        JSONArray inputObjectDataJSONArray = inputDataJSONObjects.getJSONArray("object_data");
        JSONArray inputObjectTypeJSONArray = inputDataJSONObjects.getJSONArray("object_type");
        JSONArray inputOperationJSONArray = inputDataJSONObjects.getJSONArray("operation");
        JSONArray inputNGJSONArray = inputDataJSONObjects.getJSONArray("ng");
        JSONArray inputDirectoryJSONArray = inputDataJSONObjects.getJSONArray("directory");

        for (int i = 0; i < inputSubjectJSONArray.length(); i++) {


            JSONObject currInputDataJSONObject = new JSONObject();

            currInputDataJSONObject.put("subject", inputSubjectJSONArray.get(i));
            currInputDataJSONObject.put("property", inputPropertyJSONArray.get(i));
            currInputDataJSONObject.put("object_data", inputObjectDataJSONArray.get(i));
            currInputDataJSONObject.put("object_type", inputObjectTypeJSONArray.get(i));
            currInputDataJSONObject.put("operation", inputOperationJSONArray.get(i));
            currInputDataJSONObject.put("ng", inputNGJSONArray.get(i));
            currInputDataJSONObject.put("directory", inputDirectoryJSONArray.get(i));

            generatedCoreIDData = generateCurrCoreIDNGData(currInputDataJSONObject, generatedCoreIDData);

        }




        return generatedCoreIDData;

    }

}
