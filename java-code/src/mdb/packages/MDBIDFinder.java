/**
 * Created by Roman Baum on 28.10.16.
 * Last modified by Roman Baum on 10.02.17.
 */

package mdb.packages;

import mdb.basic.MDBDate;
import mdb.basic.StringChecker;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.SPARQLFilter;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.ArrayList;


public class MDBIDFinder {

    private String mdbUEID, mdbCoreID, mdbEntryID;

    private boolean mdbUEIDNotEmpty = false, mdbEntryIDNotEmpty = false, mdbCoreIDNotEmpty = false;


    public MDBIDFinder(String uri, JenaIOTDBFactory connectionToTDB) {

        extractMDBID(uri, connectionToTDB);

    }


    /**
     * This method calculates the MDBCoreID for a known corresponding MDBEntryID.
     */
    private void calculateMDBCoreIDFromMDBEntryID () {

        this.mdbCoreID = this.mdbEntryID.substring(0, this.mdbEntryID.lastIndexOf("-"));

        this.mdbCoreIDNotEmpty = true;

        System.out.println(this.mdbCoreID + " is a MDBCoreID!");

        calculateMDBUEIDFromDBCoreID();

    }


    /**
     * This method calculates the MDBUEID for a known corresponding MDBCoreID.
     */
    private void calculateMDBUEIDFromDBCoreID() {

        this.mdbUEID = this.mdbCoreID.substring(0, this.mdbCoreID.indexOf("-"));

        this.mdbUEIDNotEmpty = true;

        System.out.println(this.mdbUEID + " is a MDBUEID!");

    }


    /**
     * This method checks if a String contains a valid integer value.
     * @param stringToCheck contains a String with a potential integer value
     * @return "true" if the String is a valid integer value, else "false"
     */
    private boolean checkIfStringIsAnInteger(String stringToCheck) {

        StringChecker stringChecker = new StringChecker();

        return stringChecker.checkIfStringIsAnInteger(stringToCheck);

    }


    /**
     * This method checks if a String contains a valid hex value.
     * @param stringToCheck contains a String with a potential hex value
     * @return "true" if the String is a valid hex value, else "false"
     */
    private boolean checkIfStringIsAHex(String stringToCheck) {

        StringChecker stringChecker = new StringChecker();

        return stringChecker.checkIfStringIsAHex(stringToCheck);

    }


    /**
     * This method checks if a String contains a valid MDB Entry Data Type.
     * @param stringToCheck contains a String with a potential MDB Entry Data Type
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return "true" if the String is a valid MDB Entry Data Type, else "false"
     */
    private boolean checkIfStringIsMDBEntryDataType(String stringToCheck, JenaIOTDBFactory connectionToTDB) {

        PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

        SelectBuilder selectWhereBuilder = new SelectBuilder();

        selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

        selectWhereBuilder.addWhere("?s", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000403>", "?o");
        // has abbreviation

        FilterBuilder filterBuilder = new FilterBuilder();

        SPARQLFilter sparqlFilter = new SPARQLFilter();

        ArrayList<String> filterItems = new ArrayList<>();

        filterItems.add(stringToCheck.toUpperCase());

        ArrayList<String> filter = sparqlFilter.getRegexSTRFilter("?o", filterItems);

        selectWhereBuilder = filterBuilder.addFilter(selectWhereBuilder, filter);

        //selectWhereBuilder.addWhere("?s", "<http://www.morphdbase.de/Ontologies/MDB/MDBUserInterfaceAnnotationProperty#MDB_UIAP_0000000403>", "?o");

        // create main query structure

        AskBuilder askBuilder = new AskBuilder();

        askBuilder = prefixesBuilder.addPrefixes(askBuilder);

        askBuilder.addGraph("?g", selectWhereBuilder);

        // create a Query
        Query sparqlQuery = QueryFactory.create(askBuilder.buildString());

        return Boolean.parseBoolean(connectionToTDB.pullStringDataFromTDB(OntologiesPath.pathToOntology, sparqlQuery, "RDF/XML-ABBREV"));

    }


    /**
     * This method checks if a String contains a valid MDB Version ID.
     * @param stringToCheck contains a String with a potential MDB Version ID
     * @param numberOfUnderscoreOccurrences contains the number of underscores in the input String
     * @return "true" if the String is a valid MDB Version ID, else "false"
     */
    private boolean checkIfStringIsMDBVersion(String stringToCheck, int numberOfUnderscoreOccurrences) {

        if (numberOfUnderscoreOccurrences == 1) {
            // published entry

            String[] underscoreSplitParts = stringToCheck.split("_");

            return underscoreSplitParts[0].equals("p") &&
                    checkIfStringIsAnInteger(underscoreSplitParts[1]);


        } else if (numberOfUnderscoreOccurrences == 2) {
            // revision or draft entry

            String[] underscoreSplitParts = stringToCheck.split("_");

            // the combination of 2 different "String" integers must also be an integer
            return (underscoreSplitParts[0].equals("d") || underscoreSplitParts[0].equals("r")) &&
                    checkIfStringIsAnInteger(underscoreSplitParts[1] + underscoreSplitParts[2]);

        } else {

            return false;

        }

    }


    /**
     * This method calculate(s) MDB ID(s) in relation to an input uri.
     * @param uri contains an input uri
     * @param connectionToTDB contains a JenaIOTDBFactory object
     */
    private void extractMDBID(String uri, JenaIOTDBFactory connectionToTDB) {

        String potentialID = ResourceFactory.createResource(uri).getNameSpace();

        if (potentialID.endsWith("#")) {

            potentialID = potentialID.substring(0, potentialID.length() - 1);

            String idPartToAnalyze = potentialID.substring(potentialID.lastIndexOf("/") + 1);

            String[] partsFromPotentialID = idPartToAnalyze.split("-");

            int numberOfHyphenOccurrences = StringUtils.countMatches(idPartToAnalyze, "-");

            boolean idIsCorrect;

            switch (numberOfHyphenOccurrences) {

                case 0 :

                    idIsCorrect = checkIfStringIsAHex(idPartToAnalyze);

                    if (idIsCorrect) {

                        System.out.println(potentialID + " is a MDBUEID!");

                        this.mdbUEID = potentialID;

                        this.mdbUEIDNotEmpty = true;

                    }

                    break;

                case 3 :

                    idIsCorrect = checkIfStringIsAHex(partsFromPotentialID[0]);

                    if (idIsCorrect) {

                        MDBDate mdbDate = new MDBDate();

                        idIsCorrect = mdbDate.isValidURIDateFormat(partsFromPotentialID[1]);

                    }

                    if (idIsCorrect) {

                        idIsCorrect = checkIfStringIsMDBEntryDataType(partsFromPotentialID[2], connectionToTDB);

                    }

                    if (idIsCorrect) {

                        idIsCorrect = checkIfStringIsAnInteger(partsFromPotentialID[3]);

                    }

                    if (idIsCorrect) {

                        System.out.println(potentialID + " is a MDBCoreID!");

                        this.mdbCoreID = potentialID;

                        this.mdbCoreIDNotEmpty = true;

                        calculateMDBUEIDFromDBCoreID();

                    }

                    break;

                case 4 :

                    idIsCorrect = checkIfStringIsAHex(partsFromPotentialID[0]);

                    if (idIsCorrect) {

                        MDBDate mdbDate = new MDBDate();

                        idIsCorrect = mdbDate.isValidURIDateFormat(partsFromPotentialID[1]);

                    }

                    if (idIsCorrect) {

                        idIsCorrect = checkIfStringIsMDBEntryDataType(partsFromPotentialID[2], connectionToTDB);

                    }

                    if (idIsCorrect) {

                        idIsCorrect = checkIfStringIsAnInteger(partsFromPotentialID[3]);

                    }

                    if (idIsCorrect) {

                        int numberOfUnderscoreOccurrences = StringUtils.countMatches(idPartToAnalyze, "_");

                        idIsCorrect = checkIfStringIsMDBVersion(partsFromPotentialID[4], numberOfUnderscoreOccurrences);

                    }

                    if (idIsCorrect) {

                        System.out.println(potentialID + " is a MDBEntryID!");

                        this.mdbEntryID = potentialID;

                        this.mdbEntryIDNotEmpty = true;

                        calculateMDBCoreIDFromMDBEntryID();

                    }

                    break;

                default :

                    idIsCorrect = false;

                    break;

            }

            if (!idIsCorrect) {

                System.out.println("The input URI has no MDB ID.");

            }

        } else {

            System.out.println("The input URI contains no '#'-sign.");

        }

    }


    /**
     * This method is a getter for the class specific MDBCoreID.
     * @return a MDBCoreID
     */
    public String getMDBCoreID() {
        return this.mdbCoreID;
    }


    /**
     * This method is a getter for the class specific MDBEntryID.
     * @return a MDBEntryID
     */
    public String getMDBEntryID() {
        return this.mdbEntryID;
    }


    /**
     * This method is a getter for the class specific MDBUEID.
     * @return a MDBUEID
     */
    public String getMDBUEID() {
        return this.mdbUEID;
    }


    /**
     * This method checks if a MDBCoreID exist for this class.
     * @return "true" if the MDBCoreID exist, else "false"
     */
    public boolean hasMDBCoreID() {
        return this.mdbCoreIDNotEmpty;
    }


    /**
     * This method checks if a MDBEntryID exist for this class.
     * @return "true" if the MDBEntryID exist, else "false"
     */
    public boolean hasMDBEntryID() {
        return this.mdbEntryIDNotEmpty;
    }


    /**
     * This method checks if a MDBUEID exist for this class.
     * @return "true" if the MDBUEID exist, else "false"
     */
    public boolean hasMDBUEID() {
        return this.mdbUEIDNotEmpty;
    }

}
