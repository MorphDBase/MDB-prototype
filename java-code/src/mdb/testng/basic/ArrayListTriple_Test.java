package mdb.testng.basic;

import mdb.basic.ArrayListTriple;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class ArrayListTriple_Test {

    @Test
    public void getTripleArrayList_CreateArrayList_ReturnDataAsArrayList() throws Exception {

        // create some input strings
        String subject = "http://www.example.com/subject";
        String property = "http://www.example.com/property";
        String object = "http://www.example.com/object";

        // create an instance of the class which should be tested
        ArrayListTriple arrayListTriple = new ArrayListTriple(subject, property, object);

        // call the method to test
        ArrayList<String> testArrayListTripleType = arrayListTriple.getTripleArrayList();

        // create an instance of the expected type
        ArrayList expectedArrayListTripleType;
        expectedArrayListTripleType = new ArrayList();

        // create an instance of the unexpected type
        String unexpectedArrayListTripleType = new String();

        System.out.println(testArrayListTripleType.getClass());
        System.out.println(expectedArrayListTripleType.getClass());
        System.out.println(unexpectedArrayListTripleType.getClass());

        // check if the two object have the same type
        Assert.assertEquals(
                testArrayListTripleType.getClass(),
                expectedArrayListTripleType.getClass(), "Incorrect return type from the method: getTripleArrayList");

        // check if the two object have the different type
        Assert.assertFalse(
                testArrayListTripleType.getClass().equals(unexpectedArrayListTripleType.getClass())
        );

    }
}