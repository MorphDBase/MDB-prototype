'use strict';
/* Test Directives */

var MDBTestDirectives = angular.module('mdbApp.testdirectives', []);

MDBTestDirectives.directive('appVersion', ['version', function (version) {
    return function (scope, elm, attrs) {
        elm.text(version);
    };
}]);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// MDB Test Directives //// SANDRAS BEREICH, NICHT BEARBEITEN ODER LÖSCHEN! ///////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// directive for MDB pages
// usage:
/**<mdb-test-page></mdb-test-page>*/
MDBTestDirectives.directive('mdbTestPage', function (RecursionHelper, $rootScope, $location, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        template:
        '<div id="mdbpage" class=\"mdb_document container-fluid\">' +

        '   <form name=\"forms.mdbDoc\" id={{mdbpage.load_page_localID}} class=\"formular\" autocomplete="off" novalidate style=\"height: 100%;\">                      ' +

        '       <div id=\"spinningLoader\" class=\"loader no-animate\" ng-show=\"loading\"></div>                                                        ' +

        /*'       <mdb-form-items items=\"mdbpage.data[0]\"></mdb-form-items>                                        ' +*/
        '       <mdb-document-form-items items=\"mdbpage.data[0]\"></mdb-document-form-items>                                        ' +

            //'       <div class=\"col-sm-12\" style=\"text-align: center;\" ng-show=\"nopage\"> <br>  ' +
            //'           <button class=\"btn btn-primary\" ng-click=\"getPage(1)\" style=\"width: 15%\">Specimen Entry</button> &nbsp;&nbsp;' +
            //'           <button class=\"btn btn-primary\" ng-click=\"getPage(2)\" style=\"width: 15%\">User Entry</button> &nbsp;&nbsp;' +
            //'           <button class=\"btn btn-primary\" ng-click=\"getPage(3)\" style=\"width: 15%\">Description</button> &nbsp;&nbsp; ' +
            //'           <button class=\"btn btn-primary\" ng-click=\"getPage(4)\" style=\"width: 15%\">Admin</button> <br><br><br>' +
            //'       </div>' +

        '       <div class=\"col-sm-12\" style=\"text-align: center;\" ng-show=\"nopage\"> <br>  ' +
        '           <input ng-model="pageParams.html_form" style=\"width: 20%\"placeholder="html form name"> &nbsp;&nbsp;' +
        '           <input ng-model="pageParams.localID" style=\"width: 20%\"placeholder="local ID">' +
        '           <button class=\"btn btn-primary\" ng-click=\"getPage(5)\" style=\"width: 15%\">Custom</button><br><br> ' +
        '       </div>' +

        '       <div class=\"col-sm-12\" style=\"text-align: center;\" ng-show=\"nopage\"> <br>  ' +
        '           <button class=\"btn btn-primary\" ng-click=\"getPage(6)\" style=\"width: 15%\">Test Specimen Entry</button> &nbsp;&nbsp;' +
        '           <button class=\"btn btn-primary\" ng-click=\"getPage(7)\" style=\"width: 15%\">Test User Entry</button> &nbsp;&nbsp;' +
        '           <button class=\"btn btn-primary\" ng-click=\"getPage(8)\" style=\"width: 15%\">Test Description</button> &nbsp;&nbsp; ' +
        '           <button class=\"btn btn-primary\" ng-click=\"getPage(9)\" style=\"width: 15%\">Test Modal</button> &nbsp;&nbsp; ' +
        '           <button class=\"btn btn-primary\" ng-click=\"getPage(10)\" style=\"width: 15%\">Test Partonomy</button> <br><br><br>' +
        '       </div>' +

        '       <div style=\"height: 300px;\">                                                 ' +
        '           <div class="col-sm-3 col-md-3 col-lg-3" style="padding-left: 0px; padding-right: 5px;">                                                 ' +
        '               <pre >mdbDoc = {{mdbDoc | json}}</pre>' +
        '               <pre >WsService.sent[0] = {{WsService.sent[0] | json}}</pre>     ' +
        '           </div>    ' +
        '           <div class="col-sm-12 col-md-12 col-lg-4" style="padding-left: 0px; padding-right: 5px;">                                                 ' +
        '           </div>                                                                                                             ' +
        '           <div class="col-sm-4 col-md-4 col-lg-4" style="padding-left: 5px; padding-right: 0px;">                                                 ' +
        '               <pre >WsService.response[0] = {{WsService.response[0] | json}}</pre>                                              ' +
        '           </div>                                                                                                                ' +
        '       </div>                                                                                                                ' +

        '   </form>                                                                                                                          ' +

        '   <div id="mdboverlayid" class="modal fade" data-backdrop="static" data-keyboard="false">' +
        '       <mdb-test-overlay mdboverlay=\"mdboverlay\"></mdb-test-overlay>' +
        '   </div>' +

        '</div>'
        ,
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope, $rootScope) {
            $scope.forms = {};
            $scope.mdbDoc = {};
            $scope.mdbformid = '';
            $scope.mdbpage = MDBDocumentService.getMDBPage();
            $scope.autocomplete = {};
            $scope.loading = false;
            $scope.nopage = true;
            $scope.WsService = WsService;

            $scope.currentUser = AuthService.getUsername();
            $scope.developer = false;
            if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){$scope.developer = true;}


            if ($scope.mdbpage.data) {
                MDBDocumentService.setInput($scope.mdbpage.data)
                    .then(function(items) {
                        $scope.mdbDoc = items;
                    });
            }

            // websocket events
            ///////////////////////////////////////////////////////////////////////////

            // update $scope.mdbpage
            $scope.$on('MDBPageComposition_updated', function (event, args) {
                MDBDocumentService.getMDBPage()
                    .then(function(mdbpage) {
                        if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                            console.log("mdbTestPage on MDBPageComposition_updated\nmdbpage - " + mdbpage)
                        }
                        $scope.mdbpage = mdbpage;
                        if ($scope.mdbpage.data) {
                            $scope.mdbformid = $scope.mdbpage.load_page_localID;
                            MDBDocumentService.setInput($scope.mdbpage.data)
                                .then(function(items) {
                                    $scope.mdbDoc = items;
                                });
                        }
                    })
                    .then(function(re) {
                        MDBDocumentService.resetDocument("page");
                    });
                $scope.loading = false;
                $scope.nopage = false;
            });

            // stop loading button on incoming overlay composition
            $scope.$on('MDBOverlayComposition_updated', function (event, args) {
                $scope.loading = false;
            });

            // load autocomplete list for current input box
            $rootScope.$on('WSFormAutocompleteMessage', function (event, args) {
                /*$rootScope.$broadcast('WSFormAutocompleteMessage', {
                 socket_message: message,
                 socket_message_type: 'mdb_form_autocomplete',
                 autocomplete_form: message.html_form,
                 autocomplete_data: message.data,
                 autocomplete_field: message.localID
                 });*/
                $scope.autocomplete[args.autocomplete_field] = args.autocomplete_data[0][args.autocomplete_field].autoCompleteData;
            });

            // input validation message
            //$rootScope.$on('WSFormValidationMessage', function (event, args) {
            //    //alert("args " + JSON.stringify(args));
            //    $scope.validation_message = args.validation_message;
            //    $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
            //    $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;
            //
            //    function markValid(name, valid) {
            //        // TODO: remove this later
            //        if($scope.developer) console.log(`[name=${name}] is ${valid ? "valid" : "invalid"}`)
            //
            //        $scope.forms.mdbDoc.$setValidity("mdbvalid", valid)
            //
            //        $(`[name=${name}]`).removeClass(`ng-${valid ? "invalid" : "valid"}`)
            //        $(`[name=${name}]`).addClass(`ng-${valid ? "valid" : "invalid"}`)
            //    }
            //
            //    markValid(args.validation_field, $scope.validation_status === "true")
            //
            //    if ($scope.validation_notification) {
            //        NotificationService.showError($scope.validation_notification);
            //    }
            //
            //    /*if (args.validation_form == $scope.mdbformid) {
            //        if ($scope.validation_status == 'false') {
            //            test.$setValidity("mdbvalid", false);
            //            if ($scope.validation_notification) {
            //                NotificationService.showError($scope.validation_notification);
            //            }
            //        }
            //        else {
            //            test.$setValidity("mdbvalid", true);
            //            $scope.validation_notification = "";
            //        }
            //    }*/
            //});

            $rootScope.$on('WSFormValidationMessage', function (event, args) {
                //alert("args " + JSON.stringify(args));
                $scope.validation_message = args.validation_message;
                $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
                $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;

                if (args.validation_form == $scope.mdbformid) {
                    if ($scope.validation_status == 'false') {
                        $scope.forms.mdbDoc[args.validation_field].$setValidity("mdbvalid", false);
                        if ($scope.validation_notification) {
                            NotificationService.showError($scope.validation_notification);
                        }
                    }
                    else {
                        $scope.forms.mdbDoc[args.validation_field].$setValidity("mdbvalid", true);
                        $scope.validation_notification = "";
                    }
                }
            });


            // submit on blur
            ///////////////////////////////////////////////////////////////////////////

            $scope.submit = MDBDocumentService.submit;

            $scope.submitAutocompleteSelect = MDBDocumentService.submitAutocompleteSelect;

            $scope.submitAutocompleteChange = MDBDocumentService.submitAutocompleteChange;


            ///////////////////////////////////////////////////////////////////////////
            // TODO: workaround for testing, if ws is missing
            ///////////////////////////////////////////////////////////////////////////
            $scope.pageParams = {};
            $scope.pageParams.html_form = "";
            $scope.pageParams.localID = "";
            $scope.getPage = function (button) {
                $scope.loading = true;
                if(button == 1) {
                    // Specimen Entry
                    //WsService.sendCheckInputToWebsocket("6cc9ca2f-20170106-s-3-d_1_1#MDB_CORE_0000000412_1", "MDB_DATASCHEME_0000002090", "", [], $scope.currentUEID);
                }
                else if(button == 2) {
                    // User Entry
                    //WsService.sendCheckInputToWebsocket("MY_DUMMY_MDB_0000000001", "MDB_DATASCHEME_0000000949", "", [], $scope.currentUEID);
                }
                else if(button == 3) {
                    //Description
                    //WsService.sendCheckInputToWebsocket("MY_DUMMY_DESCRIPTION_0000000001", "MDB_DATASCHEME_0000002104", "", [], $scope.currentUEID);
                }
                else if(button == 4) {
                    //Admin
                    //WsService.sendCheckInputToWebsocket("mydummyadmin0000000001", "MDB_DATASCHEME_0000002330", "", [], $scope.currentUEID);
                }
                else if(button == 5) {
                    //Custom
                    //WsService.sendCheckInputToWebsocket($scope.pageParams.html_form, $scope.pageParams.localID, "", [], $scope.currentUEID);
                }
                else if(button == 6) {
                    $rootScope.$broadcast('sendTestSpecEntry');
                }
                else if(button == 7) {
                    $rootScope.$broadcast('sendTestUserEntry');
                }
                else if(button == 8) {
                    $rootScope.$broadcast('sendTestDescEntry');
                }
                else if(button == 9) {
                    $rootScope.$broadcast('sendTestNewEntryOverlay');
                }
            };
            ///////////////////////////////////////////////////////////////////////////

        }
    }
});

// directive for MDB overlays
// usage:
/** <mdb-test-overlay mdboverlay="mdboverlay"></mdb-test-overlay> */
MDBTestDirectives.directive('mdbTestOverlay', function (RecursionHelper, $rootScope, $location, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        //priority: 100000,
        scope: {
            mdboverlay: '=mdboverlay'
        },
        template:
        '   <form name=\"forms.mdbDoc\" id={{mdboverlay.load_overlay_localID}} form-autofill-fix class=\"formular\" autocomplete="off" novalidate> ' +

        '       <div class="modal-dialog modal-lg">' +
        '           <div class="modal-content">' +

        '               <div class="modal-header"> ' +
        '                   <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
        '               </div>' +

        '               <div class="modal-body">' +
        '                   <mdb-form-items items=\"mdboverlay.data[0]\"></mdb-form-items>' +
        '               </div>' +

        '               <div class="modal-footer">' +
        '                   <span ng-show="showNote">{{notification}}</span>' +
        '                   <span ng-if="forms.mdbDoc.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '                   <span ng-if="forms.mdbDoc.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '                   <button ng-disabled="forms.mdbDoc.$invalid"' +
        '                       type=\"submit\" class="btn btn-sm btn-default id=\"mdboverlay.data[1].localID\"' +
        '                       ng-click=\"submit(mdbformid, forms.mdbDoc, mdboverlay.data[1].localID)\"> ' +
        '                       {{mdboverlay.data[1].MDB_GUI_0000000088}} ' +
        '                   </button>' +
        //'                   <button type="submit" class="btn btn-sm btn-default"  ng-disabled="forms.mdbDoc.$invalid" ng-click="ok()">{{submitButton.label}}</button>' +
        '                   <button type="button" class="btn btn-sm btn-default" ng-click="cancel()">Cancel</button>' +
        '               </div>' +
        '           </div>' +
        '       </div>' +

        '   </form>',
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope, $rootScope) {
            $scope.forms = {};
            $scope.mdbDoc = {};
            $scope.mdbformid = "";
            $scope.mdboverlay = MDBDocumentService.getMDBOverlay();
            $scope.autocomplete = {};
            $scope.loading = false;
            $scope.WsService = WsService;

            $scope.currentUser = AuthService.getUsername();
            $scope.developer = false;
            if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){$scope.developer = true;}

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////

            $scope.submit = MDBDocumentService.submit;

            $scope.submitSelect = MDBDocumentService.submitSelect;

            $scope.submitAutocomplete = MDBDocumentService.submitAutocomplete;

            // websocket events
            ///////////////////////////////////////////////////////////////////////////

            $scope.$on('MDBOverlayComposition_updated', function (event, args) {
                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("mdbTestOverlay on MDBOverlayComposition_updated\nevent - " + event + "\nargs - " + JSON.stringify(args))
                }
                $scope.mdboverlay = args.mdbOverlay;
                MDBDocumentService.resetDocument("overlay");
                if ($scope.mdboverlay.data) {
                    $scope.mdbformid = $scope.mdboverlay.load_overlay_localID;
                    $scope.mdboverlayid = $scope.mdboverlay.widget;
                    MDBDocumentService.setInput($scope.mdboverlay.data)
                        .then(function(items) {
                            $scope.mdbDoc = items;
                        });
                }
                $scope.showOverlay("#mdboverlayid");
                $scope.loading = false;
            });

            //$rootScope.$on('WSFormValidationMessage', function (event, args) {
            //    //alert("args " + JSON.stringify(args));
            //    $scope.validation_message = args.validation_message;
            //    $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
            //    $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;
            //
            //    function markValid(name, valid) {
            //        // TODO: remove this later
            //        if($scope.developer) console.log(`[name=${name}] is ${valid ? "valid" : "invalid"}`)
            //
            //        $scope.forms.mdbDoc.$setValidity("mdbvalid", valid)
            //
            //        $(`[name=${name}]`).removeClass(`ng-${valid ? "invalid" : "valid"}`)
            //        $(`[name=${name}]`).addClass(`ng-${valid ? "valid" : "invalid"}`)
            //    }
            //
            //    markValid(args.validation_field, $scope.validation_status === "true")
            //
            //    if ($scope.validation_notification) {
            //        NotificationService.showError($scope.validation_notification);
            //    }
            //
            //    if (args.validation_form == $scope.mdbformid) {
            //        if ($scope.validation_status == 'false') {
            //            test.$setValidity("mdbvalid", false);
            //            if ($scope.validation_notification) {
            //                NotificationService.showError($scope.validation_notification);
            //            }
            //        }
            //        else {
            //            test.$setValidity("mdbvalid", true);
            //            $scope.validation_notification = "";
            //        }
            //    }
            //});

            $rootScope.$on('WSFormValidationMessage', function (event, args) {
                //alert("args " + JSON.stringify(args));
                $scope.validation_message = args.validation_message;
                $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
                $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;

                if (args.validation_form == $scope.mdbformid) {
                    if ($scope.validation_status == 'false') {
                        $scope.forms.mdbDoc[args.validation_field].$setValidity("mdbvalid", false);
                        if ($scope.validation_notification) {
                            NotificationService.showError($scope.validation_notification);
                        }
                    }
                    else {
                        $scope.forms.mdbDoc[args.validation_field].$setValidity("mdbvalid", true);
                        $scope.validation_notification = "";
                    }
                }
            });

            $rootScope.$on('WSFormAutocompleteMessage', function (event, args) {
                $scope.autocomplete[args.autocomplete_field] = args.autocomplete_data[0][args.autocomplete_field].autoCompleteData;
            });

            // OVERLAY FUNCTIONALITY /////////////////////////////////////////

            $scope.submitButton = {
                label: 'Ok',
                isDisabled: true
            };

            $scope.cancel = function () {
                angular.element("#mdboverlayid").modal('hide');
                $scope.mdbDoc = {};
                close(null, 500);
            };

            $scope.showOverlay = function (element) {
                angular.element(element).modal('show');
            };
        }
    }
});

// directive for MDB form items
// called from within mdbTestPage, mdbTestOverlay
// usage:
/** <mdb-form-items items='json.data[0]'></mdb-form-items> */
MDBTestDirectives.directive('mdbFormItems', function () {
    return {
        restrict: 'E',
        require: '^mdbTestPage ^mdbTestOverlay,',
        replace: true,
        //priority: 100000,
        scope: {
            items: '='
        },
        template:
        '<div ng-repeat=\"item in items.MDB_GUI_0000000040\"  id={{items.localID}} ng-class=\"{\'new_row\' : item.MDB_UIAP_0000000399}\">' +
        '<!-- ********************************* -->' +
        '<!-- *** ontology generated panels *** -->' +
        '<!-- ********************************* -->' +

            /*'<!################### PANELS #############################################################################################################>' +*/

            /*'<!-- formerly "simple panel"  || now "GUI_COMPONENT__BACKGROUND: .css-bg-white" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000058\') > -1\" id={{item.localID}} class=\"css-bg-white form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- new "GUI_COMPONENT__BACKGROUND: .css-bg-white-bordered" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000168\') > -1\" id={{item.localID}} class=\"css-bg-white-bordered form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- new "GUI_COMPONENT__BACKGROUND: .css-bg-lighter-grey" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000229\') > -1\" id={{item.localID}} class=\"css-bg-lighter-grey form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- formerly "bordered panel" || now "GUI_COMPONENT__BACKGROUND: .css-bg-lighter-grey-bordered" ---------------------------------------------->' +*/
        '   <!-- bordered panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000006\') > -1\" id={{item.localID}} class=\"css-bg-lighter-grey-bordered form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- new "GUI_COMPONENT__BACKGROUND: .css-bg-light-green" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000304\') > -1\" id={{item.localID}} class=\"css-bg-light-green form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- new "GUI_COMPONENT__BACKGROUND: .css-bg-light-red" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000305\') > -1\" id={{item.localID}} class=\"css-bg-light-red form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__BACKGROUND: .css-bg-turquoise" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000301\') > -1\" id={{item.localID}} class=\"css-bg-turquoise form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__BACKGROUND: .css-bg-light-blue" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000302\') > -1\" id={{item.localID}} class=\"css-bg-light-blue form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__BACKGROUND: .css-bg-red" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000307\') > -1\" id={{item.localID}} class=\"css-bg-red form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__BACKGROUND: .css-bg-orange" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000306\') > -1\" id={{item.localID}} class=\"css-bg-orange form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__BACKGROUND: .css-bg-dark-grey" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000277\') > -1\" id={{item.localID}} class=\"css-bg-dark-grey form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__BACKGROUND: .css-bg-light-grey" -------------------------->' +*/
        '   <!-- simple panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000303\') > -1\" id={{item.localID}} class=\"css-bg-light-grey form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-------------------------------------- document structure ----------------------------------------------->' +*/

            /*'<!-- left  -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000310\') > -1\" id={{item.localID}} class=\"left_main_panel form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- right  -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000311\') > -1\" id={{item.localID}} class=\"right_main_panel form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-------------------------------------- /document structure ----------------------------------------------->' +*/

            /*'<!-- formerly "user image panel" -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" id={{item.localID}} class=\"panel_with_image_with_button\">' +
        '       <img src=\"/img/user.png\">' +
        '       <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +
            //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>' +

            /*'<!-------------------------------------- new ----------------------------------------------->' +*/

            /*'<!-- new "GUI_COMPONENT__DIV: div" -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000230\') > -1\" id={{item.localID}} class=\"css-div form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- new "GUI_COMPONENT__DIV: .css-sort-div" -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000152\') > -1\" id={{item.localID}} class=\"css-sort-div form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__SHAPE: .css-entry-header-bg-shadow -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000317\') > -1\" id={{item.localID}} class=\"css-entry-header-bg-shadow form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- formerly "bordered panel" || now "GUI_COMPONENT__SHAPE: .css-entry-header-upper-area" ---------------------------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000267\') > -1\" id={{item.localID}} class=\"css-entry-header-upper-area form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- formerly "bordered panel" || now "GUI_COMPONENT__SHAPE: .css-entry-header-middle-area" ---------------------------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000278\') > -1\" id={{item.localID}} class=\"css-entry-header-middle-area form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- formerly "bordered panel" || now "GUI_COMPONENT__SHAPE: .css-entry-header-lower-area" ---------------------------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000266\') > -1\" id={{item.localID}} class=\"css-entry-header-lower-area form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- formerly "user image panel" || now "GUI_COMPONENT__SHAPE: .css-user-frame3" -------------------------->' +*/
        '   <!-- user image panel -->' +
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000235\') > -1\" id={{item.localID}} class=\"panel_with_image_with_button css-user-frame3\">' +
        '       <img src=\"/img/user.png\">' +
        '       <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +
            //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>' +



            /*'<!################### TEXT #############################################################################################################>' +*/

            /*'<!-- formerly "online/offline text" || now GUI_COMPONENT__FONT: glowing-label ------------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000140\') > -1\" class=\"glowing-label\">' +
        '    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} ==online\" ' +
        '         title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-green\"></div>' +
        '    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} "=online\" ' +
        '         title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-red\"></div>' +
        '   </span>' +

            /*'<!-------------------------------------- new ----------------------------------------------->' +*/

            /*'<!-- new GUI_COMPONENT__FONT: .css-headline1 (Accordion section) -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000068\') > -1\" class=\"css-headline1\">{{item.MDB_GUI_0000000088}}</div>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-headline2 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000193\') > -1\" class=\"css-headline2\">{{item.MDB_GUI_0000000088}}</div>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-headline3 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000226\') > -1\" class=\"css-headline3\">{{item.MDB_GUI_0000000088}}</div>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-headline4 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000227\') > -1\" class=\"css-headline4\">{{item.MDB_GUI_0000000088}}</div>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-paragraph1 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000280\') > -1\"  class=\"css-paragraph1\">{{item.MDB_GUI_0000000088}}</div>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-paragraph2 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000289\') > -1\"  class=\"css-paragraph2\">{{item.MDB_GUI_0000000088}}</div>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label1 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000008\') > -1\" class=\"css-label1\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label2 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000228\') > -1\" class=\"css-label2\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label3 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000242\') > -1\" class=\"css-label3\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label4 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000287\') > -1\" class=\"css-label4\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label5 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000288\') > -1\" class=\"css-label5\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label6 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000286\') > -1\" class=\"css-label6\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label7 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000291\') > -1\" class=\"css-label7\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!-- new GUI_COMPONENT__FONT: .css-label8 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000300\') > -1\" class=\"css-label8\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>' +

            /*'<!################### BUTTONS #############################################################################################################>' +*/

            /*'<!-- formerly "button" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000141\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn btn-default"> ' +
        '       {{item.MDB_GUI_0000000088}} ' +
        '   </button>' +


            /*'<!-------------------------------------- new ----------------------------------------------->' +*/

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-search-literature-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000013\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-search-literature-btn"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-search-media-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000015\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-search-media-btn"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-search-specimen-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000017\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn btn-block css-icon-search-specimen-btn"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-toggle-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000069\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn btn-default css-toggle-btn"> ' +
        '       {{item.MDB_GUI_0000000088}} ' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-add-external-link-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000110\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-add-external-link-btn"> ' +
        '       {{item.MDB_GUI_0000000088}} ' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-expand-collapse-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000196\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-expand-collapse-btn"> ' +
        '       {{item.MDB_GUI_0000000088}} ' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-search-user-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000217\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-search-user-btn"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-search-usergroup-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000218\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-search-usergroup-btn"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-labeled-btn-small" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000239\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-labeled-btn-small"> ' +
        '       {{item.MDB_GUI_0000000088}} ' +
        '   </button>' +

            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-icon-search-btn" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000270\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-search-btn"> ' +
        '   </button>' +

            /*'<!-- "GUI_COMPONENT__BUTTON: .css-icon-transition-btn-revise" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000038\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-transition-btn-revise"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '   </button>' +

            /*'<!-- "GUI_COMPONENT__BUTTON: .css-icon-transition-btn-delete" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000039\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-transition-btn-delete"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '   </button>' +

            /*'<!-- "GUI_COMPONENT__BUTTON: .css-icon-transition-btn-tobin" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000040\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-transition-btn-tobin"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '           <span class="path4"></span>' +
        '   </button>' +

            /*'<!-- "GUI_COMPONENT__BUTTON: .css-icon-transition-btn-publish" --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000041\') > -1\"' +
        '       type=\"submit\"' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '       class="btn css-icon-transition-btn-publish"> ' +
        '           <span class="path1"></span>' +
        '           <span class="path2"></span>' +
        '           <span class="path3"></span>' +
        '   </button>' +


            /*'<!-- new "GUI_COMPONENT__BUTTON: .css-date-selector-btn" --------------------------------->' +*/
        '   <p ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000275\') > -1\" class=\"input-group\">' +
        '       <span class=\"css-data-input-left-narrow2-icon-date-selector-btn-group\">' +
        '       <input type=\"text\" class=\"css-data-input-left-narrow2\" uib-datepicker-popup=\"dd.MM.yyyy\" ' +
        '           name={{item.localID}} id=\"{{item.localID}}\" ng-model=\"mdbDoc[item.localID]\" ' +
        '           is-open=\"datepickerOpened[item.localID]\" ' +
        '           datepicker-options=\"dateOptions\" ng-required={{item.MDB_UIAP_0000000386}} close-text=\"close\" ' +
        '           title={{item.MDB_UIAP_0000000018}} ' +
        '           ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"/>' +
        '           <button type=\"button\" class=\"btn css-icon-date-selector-btn\" ng-click=\"openDatePicker($event,item.localID)\"></button>' +
        '       </span>' +
            // TODO change date format for websocket in dd.MM.yyyy and websocket message has no key named "value"
        '   </p>' +

            /*'<!################### INPUTS #############################################################################################################>' +*/

            /*'<!-- formerly "GUI_COMPONENT__TEXT_FIELD_INPUT: simple with information text" || now "GUI_COMPONENT__INPUT_TYPE: .css-data-input" ---------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000114\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} '  +
        '               class=\"css-data-input\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000114\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- formerly "GUI_COMPONENT__TEXT_FIELD_INPUT: simple" || now "GUI_COMPONENT__INPUT_TYPE: .css-data-input-narrow" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000113\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-narrow\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000113\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- new "GUI_COMPONENT__INPUT_TYPE: .css-data-input-middle" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000244\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-middle\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000237\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- new "GUI_COMPONENT__INPUT_TYPE: .css-data-input-middle-narrow" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000243\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-middle-narrow\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000237\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- new "GUI_COMPONENT__INPUT_TYPE: .css-data-input-left" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000237\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-left\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000237\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- new "GUI_COMPONENT__INPUT_TYPE: .css-data-input-left-narrow" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000245\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-left-narrow\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000237\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- new "GUI_COMPONENT__INPUT_TYPE: .css-data-input-left-narrow2" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000247\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-left-narrow2\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000237\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- new "GUI_COMPONENT__INPUT_TYPE: .css-data-input-left-narrow3" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000246\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-data-input-left-narrow3\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000237\' && forms.mdbDoc[item.localID].errorMessage\" ' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +

            /*'<!-- formerly "GUI_COMPONENT__RESOURCE_SELECTOR: autocomplete text field input" || now "GUI_COMPONENT__INPUT_TYPE: .css-search-input" -------------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000241\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"css-search-input\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               typeahead-wait-ms=150' + // TODO: workaround to get along with asynchronous websocket messages
        '               uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"' +
        '               typeahead-on-select=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '               ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000241\' && forms.mdbDoc[item.localID].errorMessage\"' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +
            //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{mdbDoc[item.localID] | json}}</pre>' +
            //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{autocomplete[item.localID] | json}}</pre>' +

            /*'<!-- formerly "GUI_COMPONENT__TEXT_FIELD_INPUT: vertically scalable with information text" || now "GUI_COMPONENT__INPUT_TYPE: .css-textarea" -->' +*/
        '   <textarea ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000120\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} '  +
        '               class=\"css-textarea\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"></textarea>' +


            /*'<!################### WHATEVER #############################################################################################################>' +*/

            /*'<!-- formerly "GUI_COMPONENT__RESOURCE_SELECTOR: autocomplete text field input" -------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"\"' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               typeahead-wait-ms=150' + // TODO: workaround to get along with asynchronous websocket messages
        '               uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"' +
        '               typeahead-on-select=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '               ng-blur=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"' +
        '               ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">' +
        '   <p ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000114\' && forms.mdbDoc[item.localID].errorMessage\"' +
        '               ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>' +
            //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{mdbDoc[item.localID] | json}}</pre>' +
            //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{autocomplete[item.localID] | json}}</pre>' +

            /*'<!-- formerly "GUI_COMPONENT__TEXT_FIELD_INPUT: vertically scalable" ------------------------>' +*/
        '   <textarea ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000119\') > -1\"' +
        '               name={{item.localID}} ' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"></textarea>' +

            /*'<!-- formerly "select" || now "GUI_COMPONENT__DROPDOWN: css.-dropdown" -------------------------->' +*/
        '   <select ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000066\') > -1\"' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               class=\"Fpicker css-dropdown"' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-model=\"mdbDoc[item.localID]\"' +
        '               ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">' +
        '               <option value=\"\">{{item.MDB_UIAP_0000000457}}</option> <!-- not selected / blank option -->' +
        '               <option ng-if= \"item.MDB_UIAP_0000000023 && selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" ' +
        '                       title={{selData.MDB_UIAP_0000000018}} ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000023" ' +
        '                       value="{{selData.selValue}}" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000023 && !selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000023" ' +
        '                       value="{{selData.selValue}}" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000023 && selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" ' +
        '                       title={{selData.MDB_UIAP_0000000018}} ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000023" ' +
        '                       value="{{selData.selValue}}" ' +
        '                       selected = "selected" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000023 && !selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\"' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000023" ' +
        '                       value="{{selData.selValue}}" ' +
        '                       selected = "selected" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000118 && selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" ' +
        '                       title={{selData.MDB_UIAP_0000000018}} ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000118" ' +
        '                       value="{{selData.selValue}}" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000118 && !selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000118" ' +
        '                       value="{{selData.selValue}}" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000118 && selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" ' +
        '                       title={{selData.MDB_UIAP_0000000018}} ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000118" ' +
        '                       value="{{selData.selValue}}" ' +
        '                       selected = "selected" >{{selData.selLabel}}</option>' +
        '               <option ng-if= \"item.MDB_UIAP_0000000118 && !selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" ' +
        '                       ng-repeat="selData in item.MDB_UIAP_0000000118" ' +
        '                       value="{{selData.selValue}}" ' +
        '                       selected = "selected" >{{selData.selLabel}}</option>' +
            // TODO: add an option with selected at a later point
        '   </select>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$invalid" class="glyphicon glyphicon-check" title="All required fields contain valid data"></span>' +
        '   <span ng-if="forms.mdbDoc.{{item.localID}}.$valid" class="glyphicon glyphicon-check glyphicon-valid" title="All required fields contain valid data"></span>' +


            /*'<!-- formerly "GUI_COMPONENT__ICON: MDB user entry icon" -------------------------->' +*/
        '   <img ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000139\') > -1\" src=\"/img/user.png\">' +

            /*'<!-- formerly "link" ------------------------------------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000102\') > -1\" href=\" \">{{item.MDB_GUI_0000000088}}</span>' +

            /*'<!-- formerly "GUI_COMPONENT__RESOURCE_SELECTOR: browse and select from given ontology button" ------------------->' +*/
        '   <a ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000186\') > -1\" ' +
        '       ng-href="" target="_blank" title="Browse recource." data-toggle="tooltip">' +
        '       <!--ng-if="forms.mdbDoc[MDB_GUI_0000000037]$invalid"-->' +
        '       <i class="glyphicon glyphicon-search input-group-addon""></i>' +
        '   </a>' +

            /*'<!-- new "GUI_COMPONENT__CLASS: .css-table-of-contents" -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000284\') > -1\" id={{item.localID}} class=\"css-table-of-contents form-group\">' +
        '     <mdb-form-items items=\"item\"></mdb-form-items>' +
        '   </div>' +

            /*'<!-- placeholder for GUI_COMPONENT_0000000255 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000255\') > -1\" id={{item.localID}} class=\"\">' +
        '        {{item.MDB_GUI_0000000088}}                                                                                                                 '+
        '   </div>' +

        '</div>'
    };
});



/*
 // test directive for MDB document form items
 */

MDBTestDirectives.directive('mdbTestFormItems', function () {
    return {
        restrict: 'E',
        template: (
            `<div ng-repeat=\"item in items.MDB_GUI_0000000040\" id={{items.localID}} ng-class=\"{\'new_row\' : item.MDB_UIAP_0000000399}\">
                <div x-wrapper-for={{item.localID}}">
                    <mdb-test-component item="item" x-item-id={{item.localID}} x-item-type={{item.MDB_UIAP_0000000193}} />
                </div>
            </div>`
        ),
        require: '^mdbPage, ^mdbOverlay, ^mdbTestPage, ^mdbTestOverlay',
        //priority: 100000,
        replace: true,
        scope: {
            items: "="
        }
    };
});

MDBTestDirectives.directive("mdbTestComponent", function() {
    function parseItem(item) {
        const componentIs = (name) => item.MDB_UIAP_0000000193 ? item.MDB_UIAP_0000000193.indexOf(name) > -1 : false;

        if(!item.MDB_UIAP_0000000193){
            console.warn("component without \"component id\" (MDB_UIAP_0000000193)?", item)
        }

        if(componentIs("GUI_COMPONENT_0000000058")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-white form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000168")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-white-bordered form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000229")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-lighter-grey form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000006")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-lighter-grey-bordered form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000304")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-green form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000305")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-red form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000301")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-turquoise form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000302")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-blue form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000307")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-red form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000306")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-orange form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000277")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-dark-grey form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000303")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-grey form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000310")) {
            return (
                `<div id={{item.localID}} class=\"left_main_panel form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000311")) {
            return (
                `<div id={{item.localID}} class=\"right_main_panel form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000145")) {
            return (
                `<div id={{item.localID}} class=\"panel_with_image_with_button\">
                    <img src=\"/img/user.png\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
                //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000230")) {
            return (
                `<div id={{item.localID}} class=\"css-div form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000152")) {
            return (
                `<div id={{item.localID}} class=\"css-sort-div form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000026")) {
            return (
                `<div id={{item.localID}} class=\"css-dragable-div form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000317")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-bg-shadow css-div form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000267")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-upper-area form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000278")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-middle-area form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000266")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-lower-area form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000235")) {
            return (
                `<div id={{item.localID}} class=\"panel_with_image_with_button css-user-frame3\">
                    <img src=\"/img/user.png\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
                //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000140")) {
            return (
                `<span class=\"glowing-label\">
                    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} ==online\"
                    title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-green\"></div>
                    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} "=online\"
                    title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-red\"></div>
                </span>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000068")) {
            return `<div class=\"css-headline1\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000193")) {
            return `<div class=\"css-headline2\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000226")) {
            return `<div class=\"css-headline3\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000227")) {
            return `<div class=\"css-headline4\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000280")) {
            return `<div class=\"css-paragraph1\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000289")) {
            return `<div class=\"css-paragraph2\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000008")) {
            return `<span class=\"css-label1\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000228")) {
            return `<span class=\"css-label2\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000242")) {
            return `<span class=\"css-label3\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000287")) {
            return `<span class=\"css-label4\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000288")) {
            return `<span class=\"css-label5\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000286")) {
            return `<span class=\"css-label6\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000291")) {
            return `<span class=\"css-label7\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000300")) {
            return `<span class=\"css-label8\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000141")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn btn-default">
                    {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000013")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-literature-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000015")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-media-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000017")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn btn-block css-icon-search-specimen-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000069")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn btn-default css-toggle-btn">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000110")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-add-external-link-btn">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000196")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class=\"btn css-expand-collapse-btn\" mdb-collapse-button>{{item.MDB_GUI_0000000088}}</button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000217")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-user-btn">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000218")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-usergroup-btn">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000239")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-labeled-btn-small">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000270")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-btn">
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000038")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-revise">
                        <span class="path1"></span>
                        <span class="path2"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000039")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-delete">
                        <span class="path1"></span>
                        <span class="path2"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000040")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-tobin">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                        <span class="path4"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000041")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-publish">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000275")) {
            return (
                `<p class=\"input-group\">
                    <span class=\"css-data-input-left-narrow2-icon-date-selector-btn-group\">
                        <input type=\"text\" class=\"css-data-input-left-narrow2\" uib-datepicker-popup=\"dd.MM.yyyy\"
                            name={{item.localID}} id=\"{{item.localID}}\" ng-model=\"mdbDoc[item.localID]\"
                            is-open=\"datepickerOpened[item.localID]\"
                            datepicker-options=\"dateOptions\" ng-required={{item.MDB_UIAP_0000000386}} close-text=\"close\"
                            title={{item.MDB_UIAP_0000000018}}
                            ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"/>
                        <button type=\"button\" class=\"btn css-icon-date-selector-btn\" ng-click=\"openDatePicker($event,item.localID)\"></button>
                    </span>
                </p>`
                // TODO change date format for websocket in dd.MM.yyyy and websocket message has no key named "value"
            )
        }

        if(componentIs("GUI_COMPONENT_0000000114")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000113")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000244")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-middle\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000243")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-middle-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000237")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000245")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000247")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow2\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000246")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow3\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000241")) {
            return (
                // TODO: workaround to get along with asynchronous websocket messages
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-search-input\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    typeahead-wait-ms=150
                    uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"
                    typeahead-on-select=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{mdbDoc[item.localID] | json}}</pre>
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{autocomplete[item.localID] | json}}</pre>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000120")) {
            return (
                `<textarea
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-textarea\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                </textarea>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000149")) {
            return (
                // TODO: workaround to get along with asynchronous websocket messages
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    typeahead-wait-ms=150
                    uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"
                    typeahead-on-select=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    ng-blur=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{mdbDoc[item.localID] | json}}</pre>
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{autocomplete[item.localID] | json}}</pre>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000119")) {
            return (
                `<textarea
                    name={{item.localID}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                </textarea>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000066")) {
            return (
                `<select
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"Fpicker css-dropdown"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-model=\"mdbDoc[item.localID]\"
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                        <option value=\"\">{{item.MDB_UIAP_0000000457}}</option> <!-- not selected / blank option -->
                        <option ng-if= \"item.MDB_UIAP_0000000023 && selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\"
                                title={{selData.MDB_UIAP_0000000018}}
                                ng-repeat="selData in item.MDB_UIAP_0000000023"
                                value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000023 && !selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\"
                                ng-repeat="selData in item.MDB_UIAP_0000000023"
                                value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000023 && selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\"
                                title={{selData.MDB_UIAP_0000000018}}
                                ng-repeat="selData in item.MDB_UIAP_0000000023"
                                value="{{selData.selValue}}"
                                selected = "selected" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000023 && !selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\"
                                ng-repeat="selData in item.MDB_UIAP_0000000023"
                                value="{{selData.selValue}}"
                                selected = "selected" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\"
                                title={{selData.MDB_UIAP_0000000018}} ng-repeat="selData in item.MDB_UIAP_0000000118"
                                value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && !selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\"
                                ng-repeat="selData in item.MDB_UIAP_0000000118"
                                value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\"
                                title={{selData.MDB_UIAP_0000000018}}
                                ng-repeat="selData in item.MDB_UIAP_0000000118" value="{{selData.selValue}}"
                                selected = "selected" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && !selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\"
                                ng-repeat="selData in item.MDB_UIAP_0000000118"
                                value="{{selData.selValue}}"
                                selected = "selected" >{{selData.selLabel}}</option>
                </select>`
                // TODO: add an option with selected at a later point
            )
        }

        if(componentIs("GUI_COMPONENT_0000000139")) {
            return `<img src=\"/img/user.png\">`
        }

        if(componentIs("GUI_COMPONENT_0000000102")) {
            return `<span href=\" \">{{item.MDB_GUI_0000000088}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000186")) {
            return (
                `<a
                    ng-href="" target="_blank" title="Browse recource." data-toggle="tooltip">
                    <!--ng-if="forms.mdbDoc[MDB_GUI_0000000037]$invalid"-->
                    <i class="glyphicon glyphicon-search input-group-addon"></i>
                </a>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000284")) {
            return (
                `<div id={{item.localID}} class=\"css-table-of-contents form-group\">
                    <mdb-test-form-items items=\"item\"></mdb-test-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000255")) {
            return (
                `<div id={{item.localID}} class=\"\">
                    {{item.MDB_GUI_0000000088}}
                </div>`
            )
        }
    }

    // temporary solution to grab certain angular attributes
    function findAttributes(scope, attributes) {
        let scopeAttrs = Object.keys(scope);

        let currentScope = scope;

        while(currentScope) {
            let scopeAttrs = Object.keys(currentScope);

            // every requested element is found in current scope?
            if(attributes.every(attr => scopeAttrs.indexOf(attr) > -1)) {
                let obj = {};

                attributes.forEach(attr => obj[attr] = currentScope[attr]);

                return obj
            }

            currentScope = currentScope.$parent
        }

        return null
    }

    return {
        restrict: "E",
        replace: true,
        scope: {
            item: "="
        },
        link: function(scope, element) {
            scope.datepickerOpened = {};

            scope.openDatePicker = function(ev, localID) {
                scope.datepickerOpened[localID] = true
            };

            angular.element(element).injector().invoke(["$compile", function($compile) {
                let parsed = parseItem(scope.item);

                let compiled = $compile(parsed)(scope);

                element.append(compiled);

                if(!window._cached_attrs) {
                    const attrs = findAttributes(scope, ["mdbformid", "forms", "mdbDoc", "submit"]);

                    if(attrs && attrs.forms && attrs.forms.mdbDoc) {
                        window._cached_attrs = attrs;
                        Object.assign(scope, window._cached_attrs)
                    } else {
                        if(!window._scopes_without_attrs) {
                            window._scopes_without_attrs = []
                        }

                        window._scopes_without_attrs.push(scope);

                        if(!window._scope_timeout) {
                            window._scope_timeout = true;
                            setTimeout(function() {
                                window._scopes_without_attrs.forEach(scope => Object.assign(scope, window._cached_attrs))
                            }, 200)
                        }
                    }
                } else {
                    Object.assign(scope, window._cached_attrs)
                }
            }])
        }
    }
});

MDBTestDirectives.directive("mdbTestCollapseButton", function() {
    return {
        restrict: "A",
        scope: true,
        link: function(scope, element, attrs) {
            element.on("click", function() {
                const componentObject = $(element).parent();
                const components = $("mdb-test-component");
                const componentIndex = components.index(componentObject);

                // TODO: a collapse button and the correct field are (at the moment) always 6 components apart
                // this works but is fairly hacky, need to fix this later
                const componentOffset = 6;

                if(scope.collapsed) {
                    $(components[componentIndex + componentOffset]).slideDown();
                    scope.collapsed = false;

                    $(element).removeClass("css-expand-collapse-btn-closed")
                } else {
                    $(components[componentIndex + componentOffset]).slideUp();
                    scope.collapsed = true;

                    $(element).addClass("css-expand-collapse-btn-closed")
                }
            })
        }
    }
});


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// MDB Components /////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
 // directive for MDB document form items
 // called from within mdbPage, mdbOverlay and soon as well by mdbTestPage and mdbTestOverlay
 // usage:
 */
/** <mdb-document-form-items items='json.data[0]'></mdb-document-form-items> */

MDBTestDirectives.directive('mdbTestDocumentFormItems', function () {
    return {
        restrict: 'E',
        require: '^mdbPage, ^mdbOverlay, ^mdbTestPage, ^mdbTestOverlay',
        replace: true,
        template: (
            `<div ng-repeat=\"item in items.MDB_GUI_0000000040\" ng-class=\"{\'new_row\' : item.MDB_UIAP_0000000399}\">
                <mdb-test-document-component item="item" x-item-id={{item.localID}} x-item-type={{item.MDB_UIAP_0000000193}} />
            </div>`
        ),
        controller: function($scope, $attrs) {
            let elements = $attrs.items.split(".");

            if($scope.loading) {
                $scope.$watch("loading", function() {
                    if(!$scope.loading) {
                        if($scope[elements[0]] && $scope[elements[0]].data) {
                            $scope.items = $scope[elements[0]].data[0]
                        } else {
                            $scope.items = items
                        }
                    }
                })
            } else {
                let hasData = false;

                if(elements.length > 1) {
                    if(!$scope[elements[0]].data) {
                        // overlays for some reasaons don't get their data instantly
                        // TODO: would be nicer to hook this into "loading" as well
                        let unwatch = $scope.$watch(elements[0], function() {
                            if($scope[elements[0]].data) {
                                $scope.items = $scope[elements[0]].data[0];
                                unwatch()
                            }
                        })
                    } else {
                        $scope.items = $scope[elements[0]].data[0]
                    }
                } else {
                    $scope.items = $scope[elements[0]]
                }
            }
        }
    };
});

MDBTestDirectives.directive("mdbTestDocumentComponent", function() {
    function parseItem(item) {
        const componentIs = (name) => item.MDB_UIAP_0000000193 ? item.MDB_UIAP_0000000193.indexOf(name) > -1 : false;

        if(!item.MDB_UIAP_0000000193){
            console.warn("component without \"component id\" (MDB_UIAP_0000000193)?", item)
        }

        if(componentIs("GUI_COMPONENT_0000000058")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-white form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000168")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-white-bordered form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000229")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-lighter-grey form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000006")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-lighter-grey-bordered form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000304")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-green form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000305")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-red form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000301")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-turquoise form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000302")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-blue form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000307")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-red form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000306")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-orange form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000277")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-dark-grey form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000303")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-grey form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000310")) {
            return (
                `<div id={{item.localID}} class=\"left_main_panel form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000311")) {
            // HACK: to make right_main_panel scrollable
            // TODO: this has to run "fairly" late to work, it's probably better to run this code from somewhere else
            setTimeout(function() {
                let elem = $(".right_main_panel");

                while(elem.length > 0) {
                    if(elem.attr("id") === "MDB_DATASCHEME_0000000207_1") {
                        elem.css("height", "92%");
                        break;
                    } else {
                        elem.css("height", "100%");
                        elem = elem.parent()
                    }
                }
            }, 100);

            return (
                `<div id={{item.localID}} class=\"right_main_panel form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000145")) {
            return (
                `<div id={{item.localID}} class=\"panel_with_image_with_button\">
                    <img src=\"/img/user.png\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
                //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000230")) {
            return (
                `<div id={{item.localID}} class=\"css-div form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000152")) {
            return (
                `<div id={{item.localID}} class=\"css-sort-div form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000317")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-bg-shadow form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000267")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-upper-area form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000278")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-middle-area form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000266")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-lower-area form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000235")) {
            return (
                `<div id={{item.localID}} class=\"panel_with_image_with_button css-user-frame3\">
                    <img src=\"/img/user.png\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
                //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000140")) {
            return (
                `<span class=\"glowing-label\">
                    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} ==online\"
                    title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-green\"></div>
                    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} "=online\"
                    title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-red\"></div>
                </span>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000068")) {
            return `<div class=\"css-headline1\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000193")) {
            return `<div class=\"css-headline2\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000226")) {
            return `<div class=\"css-headline3\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000227")) {
            return `<div class=\"css-headline4\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000280")) {
            return `<div class=\"css-paragraph1\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000289")) {
            return `<div class=\"css-paragraph2\">{{item.MDB_GUI_0000000088}}</div>`
        }

        if(componentIs("GUI_COMPONENT_0000000008")) {
            return `<span class=\"css-label1\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000228")) {
            return `<span class=\"css-label2\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000242")) {
            return `<span class=\"css-label3\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000287")) {
            return `<span class=\"css-label4\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000288")) {
            return `<span class=\"css-label5\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000286")) {
            return `<span class=\"css-label6\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000291")) {
            return `<span class=\"css-label7\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000300")) {
            return `<span class=\"css-label8\">{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000141")) {
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn btn-default">
                    {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000013")) {
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-literature-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000015")) {
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-media-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000017")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn btn-block css-icon-search-specimen-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000069")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn btn-default css-toggle-btn">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000110")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-add-external-link-btn">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000196")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class=\"btn css-expand-collapse-btn\" mdb-test-document-collapse-button>
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000217")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-user-btn">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000218")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-usergroup-btn">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000239")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-labeled-btn-small">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000270")) {
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    class="btn css-icon-search-btn">
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000038")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-revise">
                        <span class="path1"></span>
                        <span class="path2"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000039")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-delete">
                        <span class="path1"></span>
                        <span class="path2"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000040")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-tobin">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                        <span class="path4"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000041")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-publish">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000275")) {
            return (
                `<p class=\"input-group\">
                    <span class=\"css-data-input-left-narrow2-icon-date-selector-btn-group\">
                        <input type=\"text\" class=\"css-data-input-left-narrow2\" uib-datepicker-popup=\"dd.MM.yyyy\"
                            name={{item.localID}} id=\"{{item.localID}}\" ng-model=\"mdbDoc[item.localID]\"
                            is-open=\"datepickerOpened[item.localID]\"
                            datepicker-options=\"dateOptions\" ng-required={{item.MDB_UIAP_0000000386}} close-text=\"close\"
                            title={{item.MDB_UIAP_0000000018}}
                            ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"/>
                        <button type=\"button\" class=\"btn css-icon-date-selector-btn\" ng-click=\"openDatePicker($event,item.localID)\"></button>
                    </span>
                </p>`
                // TODO change date format for websocket in dd.MM.yyyy and websocket message has no key named "value"
            )
        }

        if(componentIs("GUI_COMPONENT_0000000114")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000113")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000244")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-middle\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000243")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-middle-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000237")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000245")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000247")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow2\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000246")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow3\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000241")) {
            return (
                // TODO: workaround to get along with asynchronous websocket messages
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-search-input\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    typeahead-wait-ms=150
                    uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"
                    typeahead-on-select=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{mdbDoc[item.localID] | json}}</pre>
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{autocomplete[item.localID] | json}}</pre>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000120")) {
            return (
                `<textarea
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-textarea\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                </textarea>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000149")) {
            return (
                // TODO: workaround to get along with asynchronous websocket messages
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    typeahead-wait-ms=150
                    uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"
                    typeahead-on-select=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    ng-blur=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
                    ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{mdbDoc[item.localID] | json}}</pre>
                //'   <pre ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000149\') > -1\">{{autocomplete[item.localID] | json}}</pre>
            )
        }

        if(componentIs("GUI_COMPONENT_0000000119")) {
            return (
                `<textarea
                    name={{item.localID}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                </textarea>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000066")) {
            return (
                `<select
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"Fpicker css-dropdown"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-model=\"mdbDoc[item.localID]\"
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\">
                        <option value=\"\">{{item.MDB_UIAP_0000000457}}</option> <!-- not selected / blank option -->
                        <option ng-if= \"item.MDB_UIAP_0000000023 && selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" title={{selData.MDB_UIAP_0000000018}} ng-repeat="selData in item.MDB_UIAP_0000000023" value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000023 && !selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" ng-repeat="selData in item.MDB_UIAP_0000000023" value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000023 && selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" title={{selData.MDB_UIAP_0000000018}} ng-repeat="selData in item.MDB_UIAP_0000000023" value="{{selData.selValue}}" selected = "selected" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000023 && !selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" ng-repeat="selData in item.MDB_UIAP_0000000023" value="{{selData.selValue}}" selected = "selected" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" title={{selData.MDB_UIAP_0000000018}} ng-repeat="selData in item.MDB_UIAP_0000000118" value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && !selData.MDB_UIAP_0000000018 && (!(item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1) || !item.MDB_GUI_0000000445)\" ng-repeat="selData in item.MDB_UIAP_0000000118" value="{{selData.selValue}}" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" title={{selData.MDB_UIAP_0000000018}} ng-repeat="selData in item.MDB_UIAP_0000000118" value="{{selData.selValue}}" selected = "selected" >{{selData.selLabel}}</option>
                        <option ng-if= \"item.MDB_UIAP_0000000118 && !selData.MDB_UIAP_0000000018 && (item.MDB_GUI_0000000445.indexOf(selData.selValue) > -1)\" ng-repeat="selData in item.MDB_UIAP_0000000118" value="{{selData.selValue}}" selected = "selected" >{{selData.selLabel}}</option>
                </select>`
                // TODO: add an option with selected at a later point
            )
        }

        if(componentIs("GUI_COMPONENT_0000000139")) {
            return `<img src=\"/img/user.png\">`
        }

        if(componentIs("GUI_COMPONENT_0000000102")) {
            return `<span href=\" \">{{item.MDB_GUI_0000000088}}</span>`
        }

        if(componentIs("GUI_COMPONENT_0000000186")) {
            return (
                `<a
                    ng-href="" target="_blank" title="Browse recource." data-toggle="tooltip">
                    <!--ng-if="forms.mdbDoc[MDB_GUI_0000000037]$invalid"-->
                    <i class="glyphicon glyphicon-search input-group-addon"></i>
                </a>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000284")) {
            return (
                `<div id={{item.localID}} class=\"css-table-of-contents form-group\">
                    <mdb-test-document-form-items items=\"item\"></mdb-test-document-form-items>
                </div>`
            )
        }

        if(componentIs("GUI_COMPONENT_0000000255")) {
            return (
                `<div id={{item.localID}} class=\"\">
                    {{item.MDB_GUI_0000000088}}
                </div>`
            )
        }
    }
    return {
        restrict: "E",
        replace: true,
        link: function(scope, element) {
            scope.datepickerOpened = {};

            scope.openDatePicker = function(ev, localID) {
                scope.datepickerOpened[localID] = true
            };

            angular.element(element).injector().invoke(["$compile", function($compile) {
                let parsed = parseItem(scope.item);

                let compiled = $compile(parsed)(scope);

                element.append(compiled)
            }])
        }
    }
});

MDBTestDirectives.directive("mdbTestDocumentCollapseButton", function() {
    return {
        restrict: "A",
        scope: true,
        link: function(scope, element, attrs) {
            element.on("click", function() {
                const componentObject = $(element).parent();
                const components = $("mdb-test-document-component");
                const componentIndex = components.index(componentObject);

                // TODO: a collapse button and the correct field are (at the moment) always 6 components apart
                // this works but is fairly hacky, need to fix this later
                const componentOffset = 6;

                if(scope.collapsed) {
                    $(components[componentIndex + componentOffset]).slideDown();
                    scope.collapsed = false;

                    $(element).removeClass("css-expand-collapse-btn-closed")
                } else {
                    $(components[componentIndex + componentOffset]).slideUp();
                    scope.collapsed = true;

                    $(element).addClass("css-expand-collapse-btn-closed")
                }
            })
        }
    }
});
