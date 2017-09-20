/*
 * Created by Roman Baum on 07.10.15.
 * Last modified by Roman Baum on 20.09.17.
 */

package mdb.packages;

import mdb.basic.TDBPath;
import mdb.vocabulary.OntologiesPath;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.block.FileMode;
import org.apache.jena.tdb.sys.SystemTDB;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;


public class MDBAdminNGGenerator {

    private String mdbUser;
    private String mdbUserResource;
    private String pathToTDB;


    public MDBAdminNGGenerator(String mdbUser) {
        this.mdbUser = mdbUser;
        this.mdbUserResource = "http://www.morphdbase.de/Ontologies/MDB/USER#" + mdbUser;

        TDBPath tdbPath = new TDBPath(OntologiesPath.mainDirectory);

        this.pathToTDB = tdbPath.getPathToTDB("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354");
    }

    public String generateNGinAdminDirectory() {
        // hide the first 3 rows
        LogCtl.setCmdLogging();

        //reduce the size of the TDB
        TDB.getContext().set(SystemTDB.symFileMode, FileMode.direct);


        ArrayList<String> coreIDs = new ArrayList<>();

        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000405");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000406");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000407");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000408");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000421");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000422");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000423");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000424");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000427");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000428");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000438");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000439");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000547");
        coreIDs.add("http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000548");

        ArrayList<String> modelNames = new ArrayList<>();
        ArrayList<Model> models = new ArrayList<>();



        for (String coreID : coreIDs) {

            Resource object = ResourceFactory.createResource(coreID);

            Resource subject = ResourceFactory.createResource(mdbUserResource + "-" + object.getLocalName());

            modelNames.add(String.valueOf(subject));

            Model inputModel = ModelFactory.createDefaultModel();

            inputModel.add( subject, RDF.type, object);

            models.add(inputModel);


        }

        JenaIOTDBFactory mdbFactory = new JenaIOTDBFactory();

        return mdbFactory.addModelsInTDB(pathToTDB, modelNames, models);

    }

    public String getDirectoryByClass (String classURI) {

        if (ngForUserAlreadyExist(classURI)) {
            return pathToTDB;
        } else {
            return null;
        }



    }

    public String getMdbUser() {
        return mdbUser;
    }




    public boolean ngForUserAlreadyExist (String classURI) {

        Dataset dataset = TDBFactory.createDataset(pathToTDB);

        // Start a Read transaction
        dataset.begin( ReadWrite.READ );

        try {

            return dataset
                    .containsNamedModel(mdbUserResource + "-" + ResourceFactory.createResource(classURI).getLocalName());


        } finally {

            // close the dataset
            dataset.end();

        }


    }

    public void setMdbUser(String mdbUser) {
        this.mdbUser = mdbUser;
    }


}