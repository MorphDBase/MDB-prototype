/*
 * Created by Roman Baum on 28.05.15.
 * Last modified by Roman Baum on 27.01.17.
 */

package mdb.vocabulary;

import mdb.packages.JenaIOTDBFactory;
import mdb.packages.querybuilder.FilterBuilder;
import mdb.packages.querybuilder.PrefixesBuilder;
import mdb.packages.querybuilder.SPARQLFilter;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;

import java.util.ArrayList;

/**
 * A local named graph mdb.vocabulary class which provide a method to calculate an individual namespace
 */
public class IndividualURI {

    String uri;

    public IndividualURI(String uri) {
        this.uri = uri;
    }


    /**
     * This method creates a common individual URI for future calculation of the specific local identifier from the
     * input resource
     * @param currClass contains an arbitrary input class
     * @param pathToTDB contains the path to the jena tdb
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an individual URI
     */
    public String createURIForAnIndividual(String currClass, String pathToTDB, JenaIOTDBFactory connectionToTDB) {

        return createURIForAnIndividual(currClass, "?g", pathToTDB, connectionToTDB);

    }


    /**
     * This method creates a common individual URI for future calculation of the specific local identifier from the
     * input resource
     * @param currClass contains an arbitrary input class
     * @param ng contains an URI for a known named graph resource or "?g" for an unknown named graph resource
     * @param pathToTDB contains the path to the jena tdb
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an individual URI
     */
    public String createURIForAnIndividual(String currClass, String ng, String pathToTDB, JenaIOTDBFactory connectionToTDB) {

        if (UrlValidator.getInstance().isValid(currClass)) {

            String individualURIWithoutNumber = this.uri + "#" + ResourceFactory.createResource(currClass).getLocalName();

            SelectBuilder selectWhereBuilder = new SelectBuilder();

            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

            selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

            selectWhereBuilder.addWhere("?s", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>","<" + currClass + ">");

            FilterBuilder filterBuilder = new FilterBuilder();

            SPARQLFilter sparqlFilter = new SPARQLFilter();

            // create an array list to collect the filter parts
            ArrayList<String> filterCollection= new ArrayList<>();

            // add a part to the collection
            filterCollection.add(individualURIWithoutNumber);

            // generate a filter string
            ArrayList<String> filter = sparqlFilter.getSTRStartsSTRFilter("?s", filterCollection);

            selectWhereBuilder = filterBuilder.addFilter(selectWhereBuilder, filter);

            // create main query structure

            SelectBuilder selectBuilder = new SelectBuilder();

            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

            ExprVar exprVar = new ExprVar("s");

            Aggregator aggregator = AggregatorFactory.createCountExpr(true, exprVar.getExpr());

            ExprAggregator exprAggregator = new ExprAggregator(exprVar.asVar(), aggregator);

            selectBuilder.addVar(exprAggregator.getExpr(), "?count");

            if (UrlValidator.getInstance().isValid(ng)) {

                selectBuilder.addGraph("<" + ng + ">", selectWhereBuilder);

            } else {

                selectBuilder.addGraph("?g", selectWhereBuilder);

            }

            String sparqlQueryString = selectBuilder.buildString();

            //System.out.println("sparqlQueryString = " + sparqlQueryString);

            String index = connectionToTDB
                    .pullSingleDataFromTDB(pathToTDB, sparqlQueryString, "?count");

            index = String.valueOf(Integer.parseInt(index) + 1);

            // concatenate the individual uri namespace and the local identifier of the class
            return individualURIWithoutNumber + "_" + index;

        }

        return "-1";

    }

    /**
     * This method creates a common individual URI considering an unknown namespace in the tdb.
     * @param currClass contains an arbitrary input class
     * @return an individual URI
     */
    public String createURIForAnIndividualForANewNamespace(String currClass) {

        return this.uri + "#" + ResourceFactory.createResource(currClass).getLocalName() + "_1";

    }


    /**
     * This method get a specific individual URI for future calculation from the jena tdb
     * @param currClass contains an arbitrary input class
     * @param pathToTDB contains the path to the jena tdb
     * @param connectionToTDB contains a JenaIOTDBFactory object
     * @return an individual URI
     */
    public String getThisURIForAnIndividual(String currClass, String pathToTDB, JenaIOTDBFactory connectionToTDB) {

        if (UrlValidator.getInstance().isValid(currClass)) {

            String individualURIWithoutNumber = this.uri + "#" + ResourceFactory.createResource(currClass).getLocalName();

            SelectBuilder selectWhereBuilder = new SelectBuilder();

            PrefixesBuilder prefixesBuilder = new PrefixesBuilder();

            selectWhereBuilder = prefixesBuilder.addPrefixes(selectWhereBuilder);

            selectWhereBuilder.addWhere("?s", "?p","?o");

            FilterBuilder filterBuilder = new FilterBuilder();

            ArrayList<ArrayList<String>> filterItems = new ArrayList<>();

            filterItems = filterBuilder.addItems(filterItems, "?p", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
            filterItems = filterBuilder.addItems(filterItems, "?o", "<" + currClass + ">");

            SPARQLFilter sparqlFilter = new SPARQLFilter();

            ArrayList<String> filter = sparqlFilter.getINFilter(filterItems);

            selectWhereBuilder = filterBuilder.addFilter(selectWhereBuilder, filter);

            filterItems.clear();

            // create an array list to collect the filter parts
            ArrayList<String> filterCollection= new ArrayList<>();

            // add a part to the collection
            filterCollection.add(individualURIWithoutNumber);

            // generate a filter string
            filter = sparqlFilter.getRegexSTRFilter("?s", filterCollection);

            selectWhereBuilder = filterBuilder.addFilter(selectWhereBuilder, filter);

            // create main query structure

            SelectBuilder selectBuilder = new SelectBuilder();

            selectBuilder = prefixesBuilder.addPrefixes(selectBuilder);

            selectBuilder.addVar("?s");

            selectBuilder.addGraph("?g", selectWhereBuilder);

            String sparqlQueryString = selectBuilder.buildString();

            String subject = connectionToTDB.pullSingleDataFromTDB(pathToTDB, sparqlQueryString, "?s");

            if (subject.isEmpty()) {
                // create a new resource

                return createURIForAnIndividual(currClass, pathToTDB, connectionToTDB);

            } else {
                // return known resource from jena tdb

                return subject;

            }



        }

        return "-1";

    }


}
