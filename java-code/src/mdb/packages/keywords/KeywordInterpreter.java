/*
 * Created by Roman Baum on 09.10.15.
 * Last modified by Roman Baum on 13.10.15.
 */
package mdb.packages.keywords;

import mdb.basic.MDBDate;
import mdb.packages.MDBAdminNGGenerator;

//TODO write tests

/**
 * The class "KeywordInterpreter" provide a method which combine the different input keywords with the corresponding
 * class URI of the MDB Ontologies.
 */
public class KeywordInterpreter {

    private String mdbUser;


    public KeywordInterpreter() {

    }


    public KeywordInterpreter(String mdbUser) {
        this.mdbUser = mdbUser;
    }





    /**
     * calculate the corresponding ontology entity of an input URI
     * @param keywordURI a specific keyword URI
     * @return the corresponding class of the specific keyword URI
     */
    public String getEntityByKeyword(String keywordURI) {

        String entity;

        // TODO provide more cases for other keywords
        switch (keywordURI) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000111" :

                MDBDate mdbDate = new MDBDate();

                entity = mdbDate.getDate();

                break;

            // interpret the keyword URI and substitute this uri with the corresponding class uri
            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000271":

                entity = "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000408";

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000310" :

                // TODO: create interface to get the current User Name
                entity = "User Name";

                break;

            default:

                entity = keywordURI;

        }

        return entity;

    }

    /**
     * calculate the path to the specific tdb with a input keyword URI
     * @param keywordURI a specific keyword URI
     * @return the path to the tdb
     */
    public String getDirectoryByKeyword(String keywordURI) {


        String classURI = getEntityByKeyword(keywordURI);


        MDBAdminNGGenerator mdbAdminNGGenerator = new MDBAdminNGGenerator(mdbUser);

        return mdbAdminNGGenerator.getDirectoryByClass(classURI);



    }

    /**
     * returns the MDB User of the class
     * @return the MDB User of the class
     */
    public String getMdbUser() {
        return mdbUser;
    }


    /**
     * set the input as the internal mdbUser
     * @param mdbUser name of the mdb user
     */
    public void setMdbUser(String mdbUser) {
        this.mdbUser = mdbUser;
    }


}
