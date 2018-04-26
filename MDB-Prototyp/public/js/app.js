'use strict';

// define a global application module which depends on filters, and services
var mdbApp = angular.module('mdbApp', [
    'ngRoute',
    'ui.tree',
    'ui.router',
    'ui.bootstrap',
    'ui.bootstrap.datetimepicker',
    'ngCookies',
    //'uiGmapgoogle-maps',
    'angular-websocket',
    'mwAnnotator',
    'ngSanitize',
    'ui.layout',
    'ngTouch',
    'ui.grid',
    'ui.grid.selection',
    'ui.grid.resizeColumns',
    'ui.grid.moveColumns',
    'ui.grid.pagination',
    'ngTagsInput',
    'ngResource',
    'ngAnimate',
    'mgcrea.ngStrap',
    'mdbApp.controllers',
    'mdbApp.filters',
    'mdbApp.services',
    'mdbApp.testdirectives',
    'mdbApp.directives']);

mdbApp.value('version', '0.2');

// create app router for url management and redirect
// templateUrl: html file to show
// controller: name of the controller which is defined in controller.js
// access: {restricted: true} - restricted, if not authenticated
mdbApp.config(function ($routeProvider, $locationProvider) {
    $routeProvider.
        when('/', {
            templateUrl: 'partials/index',
            controller: 'IndexCtrl',
            access: {restricted: false}
        }).
        when('/impressum', {
            templateUrl: 'partials/impressum',
            controller: 'ImpressumCtrl',
            access: {restricted: false}
        })
        .when('/help', {
            templateUrl: 'partials/help',
            controller: 'HelpCtrl',
            access: {restricted: false}
        }).
        when('/user', {
            templateUrl: 'partials/user',
            controller: 'UserCtrl',
            access: {restricted: false}
        }).
        when('/entries', {
            templateUrl: 'partials/entries',
            controller: 'EntriesCtrl',
            access: {restricted: true}
        }).
        when('/messages', {
            templateUrl: 'partials/messages',
            controller: 'MessagesCtrl',
            access: {restricted: false}
        }).
        when('/colleagues', {
            templateUrl: 'partials/colleagues',
            controller: 'ColleaguesCtrl',
            access: {restricted: true}
        }).
        when('/usergroups', {
            templateUrl: 'partials/usergroups',
            controller: 'UsergroupsCtrl',
            access: {restricted: true}
        }).
        when('/websocket', {
            templateUrl: 'partials/websocket',
            controller: 'WebsocketCtrl',
            access: {restricted: false}
        }).
        when('/formtest', {
            templateUrl: 'partials/formtest',
            controller: 'FormtestCtrl',
            access: {restricted: false}
        }).
        when('/ui_layout', {
            templateUrl: 'partials/ui_layout',
            controller: 'UiCtrl',
            access: {restricted: false}
        }).
        when('/morphdescription', {
            templateUrl: 'partials/morphdescription',
            controller: 'MorphDescriptionCtrl',
            access: {restricted: false}
        }).
        when('/annotation_text', {
            templateUrl: 'partials/annotation_text',
            controller: 'TextAnnotationCtrl',
            access: {restricted: false}
        }).
        when('/annotation_image', {
            templateUrl: 'partials/annotation_image',
            controller: 'ImageAnnotationCtrl',
            access: {restricted: false}
        }).
        when('/annotation', {
            templateUrl: 'partials/annotation',
            controller: 'AnnotationCtrl',
            access: {restricted: false}
        }).
        when('/annotation_partonomy', {
            templateUrl: 'partials/annotation_partonomy',
            controller: 'PartonomyAnnotationCtrl',
            access: {restricted: false}
        }).
        when('/annotation_parto', {
            templateUrl: 'partials/annotation_parto',
            controller: 'PartoAnnotationCtrl',
            access: {restricted: false}
        }).
        when('/annotation_part', {
            templateUrl: 'partials/annotation_part',
            controller: 'PartAnnotationCtrl',
            access: {restricted: false}
        }).
        when('/entry_specimen', {
            templateUrl: 'partials/entry_specimen',
            controller: 'EntrySpecCtrl',
            access: {restricted: true}
        }).
        when('/entry_description', {
            templateUrl: 'partials/entry_description',
            controller: 'EntryDescCtrl',
            access: {restricted: true}
        }).
        when('/johnsmith', {
            templateUrl: 'partials/johnsmith',
            controller: 'JohnSmithCtrl',
            access: {restricted: false}
        }).
        when('/mdb_entry', {
            templateUrl: 'partials/mdb_entry',
            controller: 'MDBEntryCtrl',
            access: {restricted: true}
        }).
        // https://www.morphdbase.de/resource/de46dd64-20171018-md-1-d_1_1
        when('/resource/:entryId', {
            templateUrl: 'partials/mdb_entry',
            controller: 'MDBEntryCtrl',
            access: {restricted: true}
        }).
        when('/mdb_entry_list/:entryType', {
            templateUrl: 'partials/mdb_entry_list/',
            controller: 'MDBEntryListCtrl',
            access: {restricted: true}
        }).
        when('/mdb_partonomy', {
            templateUrl: 'partials/mdb_partonomy',
            controller: 'MDBPartonomyCtrl',
            access: {restricted: false}
        }).
        when('/mdb_page', {
            templateUrl: 'partials/mdb_page',
            controller: 'MDBPageCtrl',
            access: {restricted: true}
        }).
        when('/entry', {
            templateUrl: 'partials/entry',
            controller: 'EntryCtrl',
            access: {restricted: false}
        }).
        when('/admin', {
            templateUrl: 'partials/admin',
            controller: 'AdminCtrl',
            access: {restricted: true}
        }).
        otherwise({
            redirectTo: '/',
            controller: 'IndexCtrl',
            access: {restricted: false}
        });
    $locationProvider.html5Mode(true);
    $locationProvider.hashPrefix('!');
});

//// config for angularUI Google Maps
//mdbApp.config(function (uiGmapGoogleMapApiProvider) {
//    uiGmapGoogleMapApiProvider.configure({
//        //Google API key passed as post parameter in index.html
//        //key: 'YOUR KEY',
//        v: '3.',
//        libraries: 'geometry,visualization' // 'weather,geometry,visualization,places'
//    });
//});

/*
 mdbApp.config(function($provide) {
 $provide.decorator('ngModelDirective', function($delegate) {
 var ngModel = $delegate[0], controller = ngModel.controller;
 ngModel.controller = ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
 var $interpolate = $injector.get('$interpolate');
 attrs.$set('name', $interpolate(attrs.name || '')(scope));
 $injector.invoke(controller, this, {
 '$scope': scope,
 '$element': element,
 '$attrs': attrs
 });
 }];
 return $delegate;
 });
 $provide.decorator('formDirective', function($delegate) {
 var form = $delegate[0], controller = form.controller;
 form.controller = ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
 var $interpolate = $injector.get('$interpolate');
 attrs.$set('name', $interpolate(attrs.name || attrs.ngForm || '')(scope));
 $injector.invoke(controller, this, {
 '$scope': scope,
 '$element': element,
 '$attrs': attrs
 });
 }];
 return $delegate;
 });
 });
 */

// has probably some influence on route change actions
mdbApp.run(function ($rootScope, $location, $route, AuthService, $uibModalStack) {
    $rootScope.$on('$routeChangeStart', function (event, next, current) {
        if (next.access.restricted && AuthService.isLoggedIn() === false) {
            $location.path('/');
            $route.reload();
        }
        else {
            $rootScope.isLoggedIn = AuthService.isLoggedIn();
            $rootScope.CurrentUser = AuthService.getUsername();
        }
    });

    $rootScope.$on('$stateChangeStart', function () {
        var top = $uibModalStack.getTop();
        if (top) {
            $uibModalStack.dismiss(top.key);
        }
    });

    $rootScope.$on('$locationChangeStart', function (event) {
        var top = $uibModalStack.getTop();
        if (top) {
            $uibModalStack.dismiss(top.key);
            event.preventDefault();
        }
    });


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// root scope variables, sparql & queries ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    $rootScope.developers = [
        "september@mdb.de",
        "august@mdb.de",
        "mai@mdb.de",
        "d@w.de",
        "doc@delorean.com",
        "hui@ui.de",
        "hi@ha.de",
        "buddy@test.de",
        "sandra@mdb.de",
        "sandra@proto.com",
        "christian@mdb.de",
        "lars.m.vogt@gmail.com",
        "lars.m.vogt@googlemail.com"
    ];

    $rootScope.moreinfo = [
        "local@test.de",
        "lala@lila.de",
        "test@test.com",
        "sandra@proto.de",
        "tdwg@mdb.com",
        "tdwg@proto.com",
        "tdwg@mdb.de"
    ];



    // keywordKnownResources
    $rootScope.keywordKnownResourceA = "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411";
    $rootScope.keywordKnownResourceB = "http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412";

    // prefixes
    $rootScope.defaultPrefixes = 'PREFIX rdf: <' + 'http:' + '//www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
    'PREFIX mdboldentry: <' + 'http:' + '//www.morphdbase.de/Ontologies/MDB/MDBEntry0v1#>' + '\n' +
    'PREFIX mdbentry: <' + 'http:' + '//www.morphdbase.de/Ontologies/MDB/MDBEntry#>';

    // defaultQueries
    $rootScope.defaultUserQuery = $rootScope.defaultPrefixes + '\n\n' +
    'SELECT ?s' + '\n\n' +
    'WHERE {' + '\n' +
    '    GRAPH ?g {?s ?p ?o}.' + '\n' +
    "    FILTER ( regex(STR(?s), 'USER'))" + '\n' +
    '}' + '\n' +
    'GROUP BY ?s';

    $rootScope.defaultConstructQuery = $rootScope.defaultPrefixes + '\n\n' +
    'CONSTRUCT {?s rdf:type ?o}' + '\n\n' +
    'WHERE {' + '\n' +
    '    GRAPH ?g {?s rdf:type ?o}' + '\n' +
    '}';

    $rootScope.defaultPConstructQuery =
    'CONSTRUCT {?s ?p ?o}' + '\n\n' +
    'WHERE {' + '\n' +
    '    GRAPH ?g {?s ?p ?o}' + '\n' +
    '}';

    $rootScope.defaultSelectCountSpecimenQuery = $rootScope.defaultPrefixes + '\n\n' +
    'SELECT (COUNT(DISTINCT ?s) as ?count)' + '\n\n' +
    'WHERE {' + '\n' +
    '    GRAPH ?g {?s rdf:type  mdbentry:MDB_ENTRY_0000000029 } .' + '\n' +
    'FILTER ( regex(STR(?s), ' + "'http:" + "//www.morphdbase.de/resource')&& regex(STR(?s), '-S_' )) }";


    $rootScope.getCoreIDsQuery = $rootScope.defaultPrefixes + '\n\n' +
    'CONSTRUCT {?s rdf:type ?o}' + '\n\n' +
    'WHERE {' + '\n' +
    '    GRAPH ?g {?s rdf:type ?o}' + '\n' +
    '}';

    $rootScope.registerDocumentQuery = '{' + '\n' +
    '  \"type\": \"check_input\",' + '\n' +
    '  \"localID\": \"MDB_DATASCHEME_0000000784\",' + '\n' +
    '  \"value\": \"\",' + '\n' +
    '  \"localIDs\":' + '\n' +
    '    [' + '\n' +
    '       { \"localID\": \"MDB_DATASCHEME_0000000781\", \"value\": \"\"},' + '\n' +
    '       { \"localID\": \"MDB_DATASCHEME_0000000783\", \"value\": \"\"}' + '\n' +
    '    ],' + '\n' +
    '  \"mdbueid\": \"\",' + '\n' +
    '  \"html_form\": \"GUI_COMPONENT_0000000175\",' + '\n' +
    '  \"connectSID\": \"s:42-HALLOHIERALARMEINS1ELF-TTESTT.SANDRASGANZPERSOENLICHEMDBPROTOTYPSESSIONID\"' + '\n' +
    '}';

    $rootScope.loginDocumentQuery = '{' + '\n' +
    '  \"type\" : \"check_input\",' + '\n' +
    '  \"localID\" : \"MDB_DATASCHEME_0000000942\",' + '\n' +
    '  \"localIDs\" :' + '\n' +
    '    [' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000692\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000691\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000690\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000689\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000688\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000687\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000686\", \"value\" : \"\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000787\", \"value\" : \"\"}' + '\n' +
    '    ],' + '\n' +
    '  \"mdbueid\": \"\",' + '\n' +
    '  \"html_form\": \"GUI_COMPONENT_0000000143\",' + '\n' +
    '  \"connectSID\": \"s:42-HALLOHIERALARMEINS1ELF-TTESTT.SANDRASGANZPERSOENLICHEMDBPROTOTYPSESSIONID\"' + '\n' +
    '}';

    $rootScope.signUpUserQuery = '{' + '\n' +
    '  \"type\" : \"check_input\",' + '\n' +
    '  \"localID\" : \"MDB_DATASCHEME_0000000684\",' + '\n' +
    '  \"localIDs\" :' + '\n' +
    '    [' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000692\", \"value\" : \"Vorname\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000691\", \"value\" : \"Nachname\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000690\", \"value\" : \"vname\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000689\", \"value\" : \"ZFMK\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000688\", \"value\" : \"vname@zfmk.de\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000687\", \"value\" : \"vname@zfmk.de\"},' + '\n' +
    '       {\"localID\" : \"MDB_DATASCHEME_0000000787\", \"value\" : \"ABCD1234\"}' + '\n' +
    '    ],' + '\n' +
    '  \"mdbueid\": \"ABCD1234\",' + '\n' +
    '  \"html_form\": \"GUI_COMPONENT_0000000143\",' + '\n' +
    '  \"connectSID\": \"s:42-HALLOHIERALARMEINS1ELF-TTESTT.SANDRASGANZPERSOENLICHEMDBPROTOTYPSESSIONID\"' + '\n' +
    '}';

    $rootScope.fixLogout = '{' + '\n' +
    '  \"type\": \"check_input\",' + '\n' +
    '  \"localID\": \"MDB_DATASCHEME_0000000947\",' + '\n' +
    '  \"mdbueid\": \"[XXX--HIERMDBUEIDEINTRAGEN--XXX]\",' + '\n' +
    '  \"html_form\": \"MY_DUMMY_LOGOUT_0000000001\",' + '\n' +
    '  \"connectSID\": \"s:42-HALLOHIERALARMEINS1ELF-TTESTT.SANDRASGANZPERSOENLICHEMDBPROTOTYPSESSIONID\",' + '\n' +
    '  \"http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000411\": \"http://www.morphdbase.de/resource/[XXX--HIERMDBUEIDEINTRAGEN--XXX]#TimeInterval_[XXX--HIERTimeIntervalEINTRAGEN--XXX]\",' + '\n' +
    '  \"http://www.morphdbase.de/Ontologies/MDB/MDB_GUI#MDB_GUI_0000000412\": \"http://www.morphdbase.de/resource/[XXX--HIERMDBUEIDEINTRAGEN--XXX]#MDB_CORE_0000000568_[XXX--HIERTimeIntervalEINTRAGEN--XXX]\"' + '\n' +
    '}';
});