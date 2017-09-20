package mdb.testng.sparql;

import mdb.packages.querybuilder.SPARQLFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class SPARQLFilter_Test {



    @Test
    public void getRegexSTRFilter_CreateFilter_ReturnDataAsString() throws Exception {

        // create an instance of the class which should be tested
        SPARQLFilter testSPARQLFilter = new SPARQLFilter();

        // create a test input array list
        ArrayList<String> testCollection= new ArrayList<>();
        testCollection.add("test");

        // call the method to test
        ArrayList<String> testFilter = testSPARQLFilter.getRegexSTRFilter("?s", testCollection);

        ArrayList<String> expectedFilter;
        expectedFilter = new ArrayList<>();

        // check if the two object have the same type
        Assert.assertEquals(
                testFilter.getClass(), expectedFilter.getClass(), "Incorrect type from the method: getRegexSTRFilter");

    }

    @Test
    public void getINFilter_CreateFilter_ReturnDataAsString() throws Exception {


        // generate some data some the test input
        ArrayList<String> varAList = new ArrayList<>();

        varAList.add("?p");
        varAList.add("<http://www.example.com/some/uri>");



        ArrayList<String> varAList2 = new ArrayList<>();

        varAList2.add("?o");
        varAList2.add("<example^^Literal>");



        // create a test input array list
        ArrayList<ArrayList<String>> testFilterItems = new ArrayList<>();

        // fill the testdata
        testFilterItems.add(varAList);
        testFilterItems.add(varAList2);

        // create an instance of the class which should be tested
        SPARQLFilter testSPARQLFilter = new SPARQLFilter();

        // call the method to test
        ArrayList<String> testFilter = testSPARQLFilter.getINFilter(testFilterItems);

        ArrayList<ArrayList<String>> expectedFilter;
        expectedFilter = new ArrayList<>();

        // check if the two object have the same type
        Assert.assertEquals(
                testFilter.getClass(), expectedFilter.getClass(), "Incorrect type from the method: getINFilter");

    }
}