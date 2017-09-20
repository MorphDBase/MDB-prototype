/*
 * Created by Roman Baum on 11.06.15.
 * Last modified by Roman Baum on 20.04.16.
 */
package mdb.basic;

/**
 *
 */
public class TDBPath {
    
    String rootDirectory;

    /**
     * The TDBPath is a class with a path to the jena tdb. It has a root directory.
     * @param rootDirectory is the directory which include all other directories
     */
    public TDBPath(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }



    /**
     * differ between the entry status of the input variables (admin, core, draft or published)
     * @param workspace is the current workspace
     * @return the path to the jena tdb for the input variables
     */
    public String getPathToTDB (String workspace) {

        String pathToTDB = this.rootDirectory;

        switch (workspace) {

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000065":
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000494":

                pathToTDB += "mdb_core_workspace/";

                break ;

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000082":
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000354":

                pathToTDB += "mdb_admin_workspace/";

                break ;

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000633":
                // MDB_WORKSPACE_BASIC: MDB ontology workspace

                pathToTDB += "MDB_ontology_workspace/";

                break;

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000226":
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000502":

                pathToTDB += "mdb_published_workspace/";

                break ;

            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000229":
            case "http://www.morphdbase.de/Ontologies/MDB/MDBCore#MDB_CORE_0000000503":

                pathToTDB += "mdb_draft_workspace/";

                break ;

        }
        
        return pathToTDB;
    }


}

