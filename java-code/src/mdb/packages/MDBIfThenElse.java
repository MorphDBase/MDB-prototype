/*
 * Created by Roman Baum on 18.11.15.
 * Last modified by Roman Baum on 12.02.18.
 */

package mdb.packages;

import mdb.basic.MDBURLEncoder;
import mdb.basic.TDBPath;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.SPARQLFilter;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.arq.querybuilder.SelectBuilder;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * This class contains different methods to evaluate the if statement of the input knowledge bases.
 */
//TODO: write tests
public class MDBIfThenElse {

    /**
     * Check if all input values are empty or not
     * @param inputValues an array list which contains different input to check
     * @return true (if all inputValues are empty) or false
     */
    private boolean allEmpty (ArrayList<String> inputValues) {


        for (Object inputValue : inputValues) {

            String currValue = inputValue.toString();

            if (notEmpty(currValue)) {

                return false;

            }

        }

        return true;

    }

    /**
     * Check if all input values are equal or not
     * @param inputValues an array list which contains different input to check
     * @return true (if all inputValues are equal) or false
     */
    private boolean allEqual (ArrayList<String> inputValues) {

        boolean allInputAreTheSame = true;

        for (int i = 0; i < (inputValues.size() - 1); i++) {
            if (!(inputValues.get(i)).equals(inputValues.get(i + 1))) {
                allInputAreTheSame = false;
            }

        }

        return allInputAreTheSame;


    }

    /**
     * Check if all input values already exist in the mongodb
     * @param inputValues an array list which contains different input to check
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return true (if all entries already exist) or false
     */
    private boolean allInputAlreadyExistInDB(ArrayList<String> inputValues, JenaIOTDBFactory connectionToTDB) {

        boolean allInputAlreadyExist = false;

        ListIterator inputValuesLI = inputValues.listIterator();

        while ((inputValuesLI.hasNext()) & (!allInputAlreadyExist)) {

            String mailToCheck = (inputValuesLI.next()).toString();

            if (EmailValidator.getInstance().isValid(mailToCheck)) {
                // check if the mail address already exist in the jena tdb

                FilterBuilder filterBuilder = new FilterBuilder();

                SelectBuilder selectBuilder = new SelectBuilder();

                PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

                selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

                SelectBuilder tripleSPO = new SelectBuilder();

                tripleSPO.addWhere("?s", "?p", "?o");

                selectBuilder.addVar(selectBuilder.makeVar("?s"));

                selectBuilder.addGraph("?g", tripleSPO);

                SPARQLFilter sparqlFilter = new SPARQLFilter();

                ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

                filterItems = filterBuilder.addItems(filterItems, "?p", "<http://xmlns.com/foaf/0.1/mbox>");
                filterItems = filterBuilder.addItems(filterItems, "?o", "<mailto:" + mailToCheck + ">");

                ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

                selectBuilder = filterBuilder.addFilter(selectBuilder, filter);

                String sparqlQueryString = selectBuilder.buildString();

                TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

                String mailURI = connectionToTDB.pullSingleDataFromTDB(tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354"), sparqlQueryString, "?s");

                if (!mailURI.equals("")) {
                    // mail address already exist

                    allInputAlreadyExist = true;

                }



            }

            // todo check if user already exist in jena tdb and remove check from mongodb because the user must exist in the mongodb
            // todo check if this still useful
            /*
            MongoDBConnection mongoDBConnection = new MongoDBConnection("localhost", 27017);

            if (mongoDBConnection.findUserByUsername("mdb-prototyp", "users", mailToCheck)) {

                allInputAlreadyExist = true;

            }*/

        }


        return allInputAlreadyExist;

    }

    /**
     * This method check if an input value is a value or not.
     * @param inputValues an array list which contains different input to check
     * @return true (if all entries are values) or false
     */
    private boolean allInputIsValue(ArrayList<String> inputValues) {

        MDBURLEncoder mdburlEncoder = new MDBURLEncoder();

        UrlValidator urlValidator = new UrlValidator();

        boolean allInputAreValues = true;

        ListIterator inputValuesLI = inputValues.listIterator();

        while ((inputValuesLI.hasNext()) && (allInputAreValues)) {

            String currInputValue = inputValuesLI.next().toString();

            if (urlValidator.isValid(mdburlEncoder.encodeUrl(currInputValue, "UTF-8"))) {

                    allInputAreValues = false;

            }

        }

        return allInputAreValues;

    }

    /**
     * Check if an input value has a correct gui input syntax
     * @param currInputValue a String which contains input to check
     * @param currTargetValue a String which contains a target to check
     * @return true (if input has a correct gui input type syntax) or false
     */
    private boolean isGUIInputType (String currInputValue, String currTargetValue) {

        switch (currTargetValue) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000080" :
                // GUI_COMPONENT_INPUT_TYPE: email address

                if (EmailValidator.getInstance().isValid(currInputValue)) {

                    return true;

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000104" :
                // GUI_COMPONENT_INPUT_TYPE: IRI / URI

                MDBURLEncoder mdburlEncoder = new MDBURLEncoder();

                UrlValidator urlValidator = new UrlValidator();

                if (urlValidator.isValid(mdburlEncoder.encodeUrl(currInputValue, "UTF-8"))) {

                    return true;

                }

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000406" :
                // KEYWORD: some value

                MDBURLEncoder mdburlEncoderSomeValue = new MDBURLEncoder();

                UrlValidator urlValidatorSomeValue = new UrlValidator();

                if (!(EmailValidator.getInstance().isValid(currInputValue)) &&
                        !(urlValidatorSomeValue.isValid(mdburlEncoderSomeValue.encodeUrl(currInputValue, "UTF-8")))) {

                    return true;

                }

                break;

        }

        return false;

    }

    /**
     * This method checks if all input values are equal with a target value
     * @param inputValues an array list which contains different input to check
     * @param targetValues an array list which contains one target to check
     * @return true (if all inputValues are equal to the target value) or false
     */
    private boolean allInputIsOfTargetType (ArrayList<String> inputValues, ArrayList<String> targetValues) {

        boolean allInputIsOfTargetType = true;

        ListIterator inputValuesLI = inputValues.listIterator();

        if (!inputValuesLI.hasNext()) {

            return false;

        }

        while ((inputValuesLI.hasNext()) && (allInputIsOfTargetType)) {

            String currInputValue = inputValuesLI.next().toString();

            ListIterator targetValuesLI = targetValues.listIterator();

            while ((targetValuesLI.hasNext()) && (allInputIsOfTargetType)) {

                String currTargetValue = targetValuesLI.next().toString();

                ArrayList<String> currInputAndTargetValue = new ArrayList<>();

                currInputAndTargetValue.add(currInputValue);
                currInputAndTargetValue.add(currTargetValue);

                if (!allEqual(currInputAndTargetValue)) {

                    allInputIsOfTargetType = false;

                }

            }

        }

        return allInputIsOfTargetType;

    }

    /**
     * This method checks if all input values are resources
     * @param inputValues an array list which contains different input to check
     * @return true (if all inputValues are resources) or false
     */
    private boolean allInputIsSomeResource(ArrayList<String> inputValues) {

        boolean allInputIsSomeResource = true;

        ListIterator inputValuesLI = inputValues.listIterator();

        if (!inputValuesLI.hasNext()) {

            return false;

        }

        while ((inputValuesLI.hasNext()) && (allInputIsSomeResource)) {

            String currInputValue = inputValuesLI.next().toString();

            MDBURLEncoder mdbLEncoderSomeValue = new MDBURLEncoder();

            UrlValidator urlValidatorSomeValue = new UrlValidator();

            if (!urlValidatorSomeValue.isValid(mdbLEncoderSomeValue.encodeUrl(currInputValue, "UTF-8"))
                    && !(EmailValidator.getInstance().isValid(currInputValue))) {

                allInputIsSomeResource = false;

            }

        }

        return allInputIsSomeResource;



    }


    /**
     * Check if the input value is empty or not
     * @param inputValue is a string which contains input to check
     * @return true (if the inputValue is empty) or false
     */
    private boolean isEmpty (String inputValue) {

        if (inputValue.isEmpty()
                || inputValue.equals("http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000423")) {
            // KEYWORD: empty

            return true;

        } else {

            return !inputValue.matches(".*\\w.*");

        }

    }


    /**
     * Check if the input value is empty or not
     * @param inputValue is a string which contains input to check
     * @return true (if the inputValue is not empty) or false
     */
    private boolean notEmpty (String inputValue) {

        return !isEmpty(inputValue);
    }


    /**
     * Check if some input value is empty or not
     * @param inputValues an array list which contains different input to check
     * @return true (if some inputValues are empty) or false
     */
    private boolean someEmpty (ArrayList<String> inputValues) {

        if (inputValues.isEmpty()) {

            System.out.println("ERROR: There exist no input value!");

            return true;

        }

        ListIterator inputValuesLI = inputValues.listIterator();

        boolean someInputValueIsEmpty = false;

        while ((inputValuesLI.hasNext()) & (!someInputValueIsEmpty)) {

            String currValue = (inputValuesLI.next()).toString();

            someInputValueIsEmpty = isEmpty(currValue);

        }

        return someInputValueIsEmpty;
    }


    /**
     * This method checks if some input value is larger then a target value
     * @param inputValues an array list which contains different input to check
     * @param targetValues an array list which contains different target to check
     * @return true (if some inputValues are larger then a target value) or false
     */
    private boolean someInputLargerThanTarget (ArrayList<String> inputValues, ArrayList<String> targetValues) {

        boolean someInputLargerThanTarget = false;

        ListIterator inputValuesLI = inputValues.listIterator();

        while ((inputValuesLI.hasNext()) && (!someInputLargerThanTarget)) {

            int currInputValue = Integer.parseInt(inputValuesLI.next().toString());

            ListIterator targetValuesLI = targetValues.listIterator();

            while ((targetValuesLI.hasNext()) && (!someInputLargerThanTarget)) {

                String currTargetValueString = targetValuesLI.next().toString();

                if (currTargetValueString.contains("MDB_GUI_0000000423")) {
                    // KEYWORD: empty

                    someInputLargerThanTarget = false;

                } else {

                    int currTargetValue = Integer.parseInt(currTargetValueString);

                    if (currInputValue > currTargetValue) {

                        someInputLargerThanTarget = true;

                    }

                }

            }

        }

        return someInputLargerThanTarget;

    }


    /**
     * This method checks if some input value has a value in the targets list or not.
     * @param inputValues an array list which contains different input to check
     * @param targetValues an array list which contains different target to check
     * @return true (if an inputValues has no value in the targetValues) or false
     */
    private boolean someNull(ArrayList<String> inputValues, ArrayList<String> targetValues) {

        if (inputValues.isEmpty()) {

            System.out.println("ERROR: There exist no input value!");

        }

        ListIterator inputValuesLI = inputValues.listIterator();

        while (inputValuesLI.hasNext()) {

            String currInputValue = inputValuesLI.next().toString();

            ListIterator targetValuesLI = targetValues.listIterator();

            while ((targetValuesLI.hasNext())) {

                String currTargetValue = targetValuesLI.next().toString();

                if (currInputValue.equals(currTargetValue)) {

                    return false;

                }

            }

        }

        return true;

    }


    /**
     * Differ the input operations and allocate the operation with another method which calculate the result of the
     * input operation
     * @param operation to differ the different operations in the knowledge base
     * @param inputValues an array list which contains different input to check
     * @param targetValues an array list which contains different target to check
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return a boolean result for one specific operation
     */
    public boolean checkCondition(String operation, ArrayList<String> inputValues, ArrayList<String> targetValues,
                                  JenaIOTDBFactory connectionToTDB) {

        boolean ifDecision;

        switch (operation) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000342" :
                // GUI__IF_OPERATION: ALL empty

                ifDecision = allEmpty(inputValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000344" :
                // GUI__IF_OPERATION: ALL equal

                ifDecision = allEqual(inputValues);

                break;


            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000345" :
                // GUI__IF_OPERATION: ALL input is of target type

                ifDecision = allInputIsOfTargetType(inputValues, targetValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000346" :
                // GUI__IF_OPERATION: ALL input already exists in DB

                ifDecision = allInputAlreadyExistInDB(inputValues, connectionToTDB);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000349" :
                // GUI__IF_OPERATION: SOME empty

                ifDecision = someEmpty(inputValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000409" :
                // GUI__IF_OPERATION: log in check

                // This is a temporary fixed value for a login check. To use a method for the calculation instead we
                // need a correct and save transfer of a password through the web socket.

                ifDecision = true;

                //ifDecision = logInCheck(inputValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000414" :
                // GUI__IF_OPERATION: SOME input larger than target

                ifDecision = someInputLargerThanTarget(inputValues, targetValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000508" :
                // GUI__IF_OPERATION: ALL input is value

                ifDecision = allInputIsValue(inputValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000509" :
                // GUI__IF_OPERATION: ALL input is some resource

                ifDecision = allInputIsSomeResource(inputValues);

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000517" :
                // GUI__IF_OPERATION: SOME null

                ifDecision = someNull(inputValues, targetValues);

                break;

            default:

                ifDecision = Boolean.parseBoolean(null);

                break;

        }

        return ifDecision;
    }


}
