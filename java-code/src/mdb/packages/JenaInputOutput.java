package mdb.packages;

/*
 * Created by Roman Baum on 16.01.15.
 * Last modified by Roman Baum on 23.03.15.
 */
import mdb.vocabulary.FOAFAdvanced;
import mdb.vocabulary.MDBAgent;
import mdb.vocabulary.MDBCore;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 *      The class "JenaInputOutput" provides one default constructor and two methods. One method to create a default
 *      model from an RDF-file and another method to write a Model back to an RDF-file.
 */


public class JenaInputOutput {

    // default - Constructor
    public JenaInputOutput() {

    }


    Model createModelFromRDFFile(String inputFileName) {

        /**
         *      create a default model for a given RDF-input file
         */

        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // use the class loader to find the input-database
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }

        // read the RDF/XML file
        model.read(new InputStreamReader(in), "");

        return model;

    }

    void saveModelToRDFFile(Model model, String outputFileName) throws IOException {

        /**
         *      add a new individual(Kermit_The_Frog) to the model and save the model to a given RDF-input file
         */

        // get the datatype from a URI
        RDFDatatype dtype = TypeMapper.getInstance().getTypeByName(RDFS.Literal.toString());



        // create the resource
        model	.createResource(MDBCore.getURI()+"Kermit_The_Frog")
                // add the properties
                .addProperty(MDBAgent.postalAddress, "Hollywood", dtype)
                .addProperty(MDBAgent.departmentName, "Frog and More", dtype)
                .addProperty(FOAF.firstName, "Kermit", dtype)
                .addProperty(FOAFAdvanced.lastName, "the frog", dtype)
                .addProperty(MDBAgent.mdbPassword, "MissPiggy", dtype)
                .addProperty(MDBAgent.mdbLoginName, "TheFrog", dtype)
                .addProperty(RDF.type, model.createResource( MDBAgent.mdbUser.toString() ))
                .addProperty(RDF.type, model.createResource( OWL2.NamedIndividual.toString() ));

        // get the xml:base from the base namespace of the ontology
        String BaseNamespace = model.getNsPrefixURI("owl");
        model.setNsPrefix("", BaseNamespace);
        BaseNamespace = BaseNamespace.substring(0,BaseNamespace.length()-1);

        // create the outputStream
        FileWriter out = new FileWriter( outputFileName );

        // create a RDFWriter and set some property for the output-database
        RDFWriter w = model.getWriter("RDF/XML");
        w.setProperty("attributeQuoteChar","\"");
        w.setProperty("xmlbase",BaseNamespace);
        w.setProperty("showDoctypeDeclaration",true);
        w.setProperty("tab",8);

        try {
            // write the ontology to the output-database
            w.write(model,out,BaseNamespace);
        }
        finally {
            try {
                out.close();
                // write a message if the export was successfully
                System.out.println("Erfolgreich exportiert!");
            }
            catch (IOException closeException) {
                // ignore
            }
        }

    }


    public static void main(String[] args) throws IOException {

        // Hide the first 3 rows
        LogCtl.setCmdLogging();

        // name of the input-database or input-file
        String inputFileName = "MDBCore0v1_RDF.owl";

        // name of the output-database or output-file
        String outputFileName = "output.owl";

        // create a new class instance
        JenaInputOutput jio = new JenaInputOutput();

        // create a new model with a method
        Model model = jio.createModelFromRDFFile(inputFileName);

        // write the model to an RDF-output file
        jio.saveModelToRDFFile(model, outputFileName);


    }

}

