/*
 * Created by Roman Baum on 12.10.15.
 * Last modified by Roman Baum on 12.10.15.
 */

package mdb.basic;


public class MDBUser {

    private String mdbUser;
    private String mdbUserResource;

    /**
     * initialize a new MDB User
     * @param firstName the first name of the MDB User
     * @param lastName the last name of the MDB User
     */
    public MDBUser(String firstName, String lastName) {

        createMDBUser(firstName, lastName);
    }

    /**
     * initialize a new MDB User
     * @param mdbUser a MDB User input string
     */
    public MDBUser(String mdbUser) {

        this.mdbUser = mdbUser;
        this.mdbUserResource = "http://www.morphdbase.de/Ontologies/MDB/USER#" + this.mdbUser;

    }


    /**
     * Creates a new MDB User with a first and a last name
     * @param firstName the first name of the MDB User
     * @param lastName the last name of the MDB User
     */
    public void createMDBUser (String firstName, String lastName) {

        // create the mdbUser
        this.mdbUser = (firstName.substring(0, 1)).toUpperCase() + "_" + lastName.toUpperCase();
        this.mdbUserResource = "http://www.morphdbase.de/Ontologies/MDB/USER#" + this.mdbUser;

    }

    /**
     *
     * @return the MDB User
     */
    public String getMDBUser() {
        return mdbUser;
    }

    /**
     *
     * @return the MDB User resource
     */
    public String getMDBUserResource() {
        return mdbUserResource;
    }



}
