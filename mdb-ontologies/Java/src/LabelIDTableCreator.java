/**
 * Created by Roman Baum on 30.11.16.
 * Last modified by Roman Baum on 01.03.18.
 */

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.tdb.TDBFactory;

/**
 * This Class executes a SPARQL-Query for an semantic input file and list the corresponding "id, label"-pairs in
 * ascending order.
 */
public class LabelIDTableCreator {

    public static void main(String[] args)  {

        // enter the file you want to search
        String pathToOntologyFile = "MDBCore0v1.owl";

        // hide the not important log information
        LogCtl.setCmdLogging();

        //create the query with the jena querybuilder
        SelectBuilder selectBuilder = new SelectBuilder();

        Var id = selectBuilder.makeVar("?id");
        Var label = selectBuilder.makeVar("?label");
        Var uri = selectBuilder.makeVar("?uri");
        Var o = selectBuilder.makeVar("?o");

        selectBuilder.addVar(id);
        selectBuilder.addVar(label);

        selectBuilder.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        selectBuilder.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        selectBuilder.addPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");

        selectBuilder.addWhere(uri, "oboInOwl:id", id);
        selectBuilder.addWhere(uri, "rdfs:label", label);
        selectBuilder.addWhere(uri, "rdf:type", o);


        try {

            selectBuilder
                    .addFilter(
                            "regex(str(" + o + "), \"http://www.w3.org/2002/07/owl#Class\" ) || " +
                                    "regex(str(" + o + "), \"http://www.w3.org/2002/07/owl#ObjectProperty\" ) || " +
                                    "regex(str(" + o + "), \"http://www.w3.org/2002/07/owl#DatatypeProperty\" ) || " +
                                    "regex(str(" + o + "), \"http://www.w3.org/2002/07/owl#AnnotationProperty\" ) || " +
                                    "regex(str(" + o + "), \"http://www.w3.org/2002/07/owl#NamedIndividual\" ) ");

        } catch (ParseException e) {

            e.printStackTrace();

        }

        selectBuilder.addOrderBy("ASC " + id);

        String queryString = selectBuilder.toString();

        // print the query in the output
        System.out.println("\nQuerystring \n------------\n" + queryString + "\n\n");

        Dataset dataset = TDBFactory.createDataset();

        dataset.begin(ReadWrite.READ);

        try {

            Query query = QueryFactory.create(queryString);

            Model m = ModelFactory.createDefaultModel();

            // read input file
            RDFDataMgr.read(m, pathToOntologyFile);

            System.out.println("ontology file = " + pathToOntologyFile + "\n\n\n");

            QueryExecution qExec = QueryExecutionFactory.create(query , m);

            ResultSet resultSet = qExec.execSelect();

            String resultsString = ResultSetFormatter.asText(resultSet);

            // print the result table for the query
            System.out.println(resultsString);

        } finally {

            // close the dataset
            dataset.end();
            dataset.close();

        }

    }

}
