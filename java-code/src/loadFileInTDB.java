/*
 * Created by Roman Baum on 07.07.15.
 * Last modified by Roman Baum on 17.11.16.
 */

import mdb.packages.JenaIOTDBFactory;
import mdb.vocabulary.OntologiesPath;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.tdb.base.block.FileMode;
import org.apache.jena.tdb.sys.SystemTDB;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;


public class loadFileInTDB {


    public static void main(String[] args) throws IOException, ParseException {

        // hide the first 3 rows
        LogCtl.setCmdLogging();

        //reduce the size of the TDB
        TDB.getContext().set(SystemTDB.symFileMode, FileMode.direct);

        String pathToOntologies  = OntologiesPath.pathToOntology;

        String pathToInputFolder = "../lars-mdb-ontologies/";

        // get input folder by path
        File inputFolder = new File(pathToInputFolder);

        // save all files of the folder in a list
        File[] listOfFiles = inputFolder.listFiles();


        // set date format to: dd.MM.yyyy hh:mm:ss
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

        // create a buffer reader to process the TDBLastUpdatedOn file
        BufferedReader brLastUpdatedOn = new BufferedReader(new FileReader(pathToInputFolder + "TDBLastUpdatedOn"));

        // read the first line of the file (get the last updated on date)
        String firstLineUpdatedOn = brLastUpdatedOn.readLine();

        // transform the last updated on date from String to Date
        Date lastUpdatedOn = df.parse(firstLineUpdatedOn);

        // create a date instance for the potential new update date
        Date newUpdateDate = lastUpdatedOn;

        ArrayList<String> modelNameArList = new ArrayList<>();

        ArrayList<Model> addedModelArList = new ArrayList<>();

        // iterate the file list
        assert listOfFiles != null;

        for (File listOfFile : listOfFiles) {

            if (listOfFile.isFile() & (FilenameUtils.getExtension(listOfFile.getName()).equals("owl"))) {

                Date currDate = df.parse(df.format(listOfFile.lastModified()));

                if (currDate.compareTo(lastUpdatedOn) > 0) {

                    if (currDate.compareTo(newUpdateDate) > 0) {

                        newUpdateDate = currDate;
                    }

                    String basicNG = "http://www.morphdbase.de/Ontologies/MDB/";

                    if (listOfFile.getName().contains("0v1")) {

                        String currNG = FilenameUtils.getBaseName(listOfFile.getName());

                        basicNG += currNG.replace("0v1", "");

                    } else {

                        basicNG += FilenameUtils.getBaseName(listOfFile.getName());

                    }


                    modelNameArList.add(basicNG);


                    Model currModel = ModelFactory.createDefaultModel();

                    // load model to the Jena TDB

                    //System.out.println("path to file " + i + pathToInputFolder + listOfFiles[i].getName());

                    TDBLoader.loadModel(currModel, pathToInputFolder + listOfFile.getName());

                    addedModelArList.add(currModel);


                    System.out.println("basicNG " + basicNG);

                    //System.out.println("File after " + listOfFiles[i].getName());

                }

            }
        }

        System.out.println("last updated on " + df.format(lastUpdatedOn));

        System.out.println("modelNameArList " + modelNameArList.size());
        System.out.println("addedModelArList " + addedModelArList.size());

        System.out.println("pathToOntologies " + pathToOntologies);

        if (newUpdateDate.compareTo(lastUpdatedOn) > 0) {

            JenaIOTDBFactory mdbFactory = new JenaIOTDBFactory();

            mdbFactory.removeNamedModelsFromTDB(pathToOntologies, modelNameArList);

            System.out.println("addedmodels " + mdbFactory.addModelsInTDB(pathToOntologies, modelNameArList, addedModelArList));

            PrintWriter writer = new PrintWriter(pathToInputFolder + "TDBLastUpdatedOn", "UTF-8");
            writer.println(df.format(newUpdateDate));
            writer.close();



        }

        String pathToExternalOntologiesInputFolder = "../lars-mdb-ontologies/external-ontologies/";

        // get input folder by path
        File inputFolderExternalOntologies = new File(pathToExternalOntologiesInputFolder);

        // save all files of the folder in a list
        File[] listOfFilesExternalOntologies = inputFolderExternalOntologies.listFiles();

        // create a buffer reader to process the TDBLastUpdatedOn file
        BufferedReader brExternalOntologiesLastUpdatedOn = new BufferedReader
                (new FileReader(pathToExternalOntologiesInputFolder + "TDBLastUpdatedOn"));

        // read the first line of the file (get the last updated on date)
        String firstLineExternalOntologiesUpdatedOn = brExternalOntologiesLastUpdatedOn.readLine();

        // transform the last updated on date from String to Date
        Date externalOntologiesLastUpdatedOn = df.parse(firstLineExternalOntologiesUpdatedOn);

        // create a date instance for the potential new update date
        newUpdateDate = externalOntologiesLastUpdatedOn;

        ArrayList<String> modelNameExternalOntologiesArList = new ArrayList<>();

        ArrayList<Model> addedModelExternalOntologiesArList = new ArrayList<>();

        // iterate the file list
        assert listOfFilesExternalOntologies != null;

        for (File listOfFile : listOfFilesExternalOntologies) {

            if (listOfFile.isFile() & (FilenameUtils.getExtension(listOfFile.getName()).equals("owl"))) {

                Date currDate = df.parse(df.format(listOfFile.lastModified()));

                if (currDate.compareTo(externalOntologiesLastUpdatedOn) > 0) {

                    if (currDate.compareTo(newUpdateDate) > 0) {

                        newUpdateDate = currDate;
                    }

                    String basicNG = "http://www.morphdbase.de/Ontologies/MDB/";

                    if (listOfFile.getName().contains("0v1")) {

                        String currNG = FilenameUtils.getBaseName(listOfFile.getName());

                        basicNG += currNG.replace("0v1", "");

                    } else {

                        basicNG += FilenameUtils.getBaseName(listOfFile.getName());

                    }


                    modelNameExternalOntologiesArList.add(basicNG);

                    Model currModel = ModelFactory.createDefaultModel();

                    // load model to the Jena TDB

                    //System.out.println("path to file " + i + pathToInputFolder + listOfFiles[i].getName());


                    //TDBLoader.loadModel(currModel, "data1.ttl");

                    TDBLoader.loadModel(currModel, pathToExternalOntologiesInputFolder + listOfFile.getName());

                    addedModelExternalOntologiesArList.add(currModel);

                    System.out.println("basicNG " + basicNG);

                    //System.out.println("File after " + listOfFiles[i].getName());

                }

            }
        }

        System.out.println("external ontologies last updated on " + df.format(externalOntologiesLastUpdatedOn));

        System.out.println("modelNameExternalOntologiesArList " + modelNameExternalOntologiesArList.size());
        System.out.println("addedModelExternalOntologiesArList " + addedModelExternalOntologiesArList.size());

        System.out.println("pathToOntologies " + pathToOntologies);

        if (newUpdateDate.compareTo(externalOntologiesLastUpdatedOn) > 0) {

            JenaIOTDBFactory mdbFactory = new JenaIOTDBFactory();

            // todo write a method which deletes the data from the jena tdb and the corresponding lucene index
            //mdbFactory.removeNamedModelsFromTDB(pathToOntologies, modelNameArList);

            System.out.println("addedmodels " + mdbFactory.addModelsInTDBLucene(pathToOntologies, modelNameExternalOntologiesArList, addedModelExternalOntologiesArList));

            PrintWriter writer = new PrintWriter(pathToExternalOntologiesInputFolder + "TDBLastUpdatedOn", "UTF-8");
            writer.println(df.format(newUpdateDate));
            writer.close();

        }

        JenaIOTDBFactory jenaIOTDBFactory = new JenaIOTDBFactory();

        // create sub query structure

        SelectBuilder selectWhereBuilder = new SelectBuilder();

        selectWhereBuilder.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        selectWhereBuilder.addPrefix("owl", "http://www.w3.org/2002/07/owl#");

        selectWhereBuilder.addWhere("<http://purl.org/pav/createdOn>", "rdf:type", "owl:DatatypeProperty");

        // create main query structure

        AskBuilder askBuilder = new AskBuilder();

        askBuilder.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        askBuilder.addPrefix("owl", "http://www.w3.org/2002/07/owl#");

        askBuilder.addGraph("?g", selectWhereBuilder);

        //System.out.println("askbuilder: \n" + askBuilder);

        // create a Query
        Query sparqlQuery = QueryFactory.create(askBuilder.buildString());

        System.out.println(jenaIOTDBFactory.pullStringDataFromTDB(pathToOntologies, sparqlQuery, "RDF/XML-ABBREV"));

        System.out.println("newUpdateDate " + df.format(newUpdateDate));

    }

}
