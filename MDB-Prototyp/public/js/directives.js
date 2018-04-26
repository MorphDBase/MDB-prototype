'use strict';
/* Directives */

var MDBDirectives = angular.module('mdbApp.directives', []);

MDBDirectives.directive('appVersion', ['version', function (version) {
    return function (scope, elm, attrs) {
        elm.text(version);
    };
}]);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// MDB Authentication ////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// directive for MDB authentication forms
// usage:
/** <mdb-authentication-form formname='json.load_page_localID' formlabel='json.html_form_label' formitems='json'></mdb-authentication-form> */
MDBDirectives.directive('mdbAuthenticationForm', function (RecursionHelper, $rootScope, $location, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        //priority: 100000,
        scope: {
            formname: '=',
            formlabel: '=',
            formitems: '='
        },
        template:
        '<form name="forms.mdbForm" id={{formname}} autocomplete="off" novalidate>' +
        '   <mdb-authentication-form-items items="formitems"></mdb-authentication-form-items>' +
        '</form>',
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope) {
            $scope.mdbForm = {};
            $scope.overview = [];
            $scope.forms = {};
            $scope.formname = '';

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////
            $scope.submit = MDBDocumentService.submitAuthData;

            /***
             socket_message: message,
             validation_ref: message.localID,
             validation_status: message.data[0]["MDB_DATASCHEME_0000000684"].valid,
             validation_message: "Sign up successful!",
             validation_error:  message.data[0]["MDB_DATASCHEME_0000000684"].MDB_UIAP_0000000388,
             mdbueid: message.mdbueid,
             connectSID: message.connectSID
             */
            $rootScope.$on('WSSignupFeedbackMessage', function (event, args) {
                if (args.validation_status == "true") {
                    $scope.forms.mdbForm.$setPristine();
                    $scope.mdbForm = {};
                    WsService.sendGenerateDocToWebsocket('GUI_COMPONENT_0000000175');
                }
            });

            /***
             socket_message: message,
             validation_ref: message.localID,
             validation_status: message.data[0]["MDB_DATASCHEME_0000000780"].valid,
             validation_message: "Login successful!",
             mdbueid: message.mdbueid
             */
            $rootScope.$on('WSLoginFeedbackMessage', function (event, args) {
                if (args.validation_status == "true") {
                    $scope.mdbForm = {};
                    $scope.forms.mdbForm.$setPristine();
                }
            });

            /***
             socket_message: message,
             validation_ref: message.localID,
             validation_status: message.data[0]["MDB_DATASCHEME_0000000947"].valid,
             validation_message: "Logout successful!",
             mdbueid: message.mdbueid
             connectSID: message.connectSID,
             load_page: message.load_page
             */
            $rootScope.$on('WSLogoutFeedbackMessage', function (event, args) {
                if (args.validation_status == "true") {
                    $scope.mdbForm = {};
                    $scope.forms.mdbForm.$setPristine();
                    $location.path('/index');
                    WsService.sendGenerateDocToWebsocket('GUI_COMPONENT_0000000175');
                }
            });

            $rootScope.$on('WSFormValidationMessage', function (event, args) {
                $scope.validation_message = args.validation_message;
                $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
                $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;

                if (args.validation_form == $scope.formname) {

                    if ($scope.validation_status == 'false') {
                        if($scope.forms.mdbForm[args.validation_field]) {
                            $scope.forms.mdbForm[args.validation_field].$setValidity("mdbvalid", false);
                        }
                        if ($scope.validation_notification) {
                            NotificationService.showError($scope.validation_notification);
                        }
                    }
                    else {
                        if($scope.forms.mdbForm[args.validation_field]){
                            $scope.forms.mdbForm[args.validation_field].$setValidity("mdbvalid", true);
                        }
                        $scope.validation_notification = "";
                    }
                }
            });
        }
    }
});

// directive for MDB authentication form items
// called from within mdbAuthenticationForm
// usage:
/** <mdb-authentication-form-items items="=''></mdb-authentication-form-items> */
MDBDirectives.directive('mdbAuthenticationFormItems', function () {
    return {
        restrict: 'E',
        require: '^mdbAuthenticationForm',
        replace: true,
        //priority: 100000,
        scope: {
            items: '='
        },
        template:
        '<div ng-repeat="item in items.MDB_GUI_0000000040">' +

            /*'<!-- "GUI_COMPONENT__BACKGROUND: .css-bg-white" -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000058\') > -1\" id={{item.localID}} class=\"css-bg-white form-group\">' +
        '       <mdb-authentication-form-items items="formitems"></mdb-authentication-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__INPUT_TYPE: .css-data-input-narrow -------------------------------------->' +*/
        '   <div class="form-group has-feedback">' +
        '       <input ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000113\'\"' +
        '               class=\"form-control css-data-input-narrow\" ' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}} ' +
        '               ng-model=\"mdbForm[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
            //'               ng-class=\"{{item.MDB_UIAP_0000000386}}==\"true\" ? \'ng-invalid\'\"' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(formname, forms.mdbForm, item.localID, mdbForm[item.localID])\">' +
        '       <span ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000113\' && forms.mdbForm[item.localID].$invalid\" ' +
        '               ng-hide="mdbForm[item.localID]" class="form-control-feedback required-info" ' +
        '               data-toggle="tooltip" title="required information"></span>' +

        '   </div>' +

            /*'<!-- GUI_COMPONENT__TEXT_FIELD_INPUT: vertically scalable with information text -->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000120\'\"' +
        '               class=\"css-text-area\" ' +
        '               name={{item.localID}} ' +
        '               id={{item.localID}}' +
        '               ng-model=\"mdbForm[item.localID]\"' +
        '               placeholder={{item.MDB_UIAP_0000000220}} ' +
        '               title={{item.MDB_UIAP_0000000018}} ' +
        '               type={{item.html_input_type}} ' +
        '               ng-required={{item.MDB_UIAP_0000000386}} ' +
        '               ng-blur=\"submit(formname, forms.mdbForm, item.localID, mdbForm[item.localID])\">' +

            /*'<!-- GUI_COMPONENT__BUTTON: .css-search-literature-btn --------------------------------->' +*/
        '   <button ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000141\'\"' +
        '       type=\"submit\"' +
        '       id={{item.localID}}' +
        '       title={{item.MDB_UIAP_0000000018}} ' +
        '       ng-click=\"submit(formname, forms.mdbForm, item.localID, mdbForm[item.localID])\"' +
        '       class="btn css-labeled-btn"> ' +
        '       {{item.MDB_GUI_0000000088}} ' +
        '       {{item.MDB_UIAP_0000000220}} ' + // TODO: wieder raus nehmen, wenn Ontologie aktualisiert wurde
        '   </button>' +


            /*'<!-- GUI_COMPONENT__FONT: .css-headline2 -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000193\') > -1\" class=\"css-headline2\">' +
        '       {{item.MDB_GUI_0000000088}}' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__DIV: div -------------------------->' +*/
        '   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000230\') > -1\" id={{item.localID}} class=\"css-div form-group\">' +
        '       <mdb-authentication-form-items items=\"item\"></mdb-authentication-form-items>' +
        '   </div>' +

            /*'<!-- GUI_COMPONENT__FONT: .css-label8 ------------------------------->' +*/
        '   <span ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000300\') > -1\" id={{item.localID}} class=\"css-label8\">' +
        '       {{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}' +
        '   </span>' +

            /*'<!-- check-box ------------------------------->' +*/
        '   <input ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000045\') > -1\" ' +
        '       type=\"checkbox\"' +
        '       id={{item.localID}} ' +
        '       name={{item.localID}} ' +
        '       ng-model=\"mdbForm[item.localID]\"' +
        '       title={{item.MDB_UIAP_0000000018}}' +
        '       ng-click=\"submit(formname, forms.mdbForm, item.localID, mdbForm[item.localID])\" ' +
        '   </input>' +

            /*'<!-- GUI_COMPONENT__BUTTON: Login Button --------------------------------->' +*/
            //'   <button ng-if=\"item.MDB_UIAP_0000000193 == \'GUI_COMPONENT_0000000141\' && item.localID == \'MDB_DATASCHEME_0000000780\'\"' +
            //'       type=\"submit\"' +
            //'       id={{item.localID}}' +
            //'       title={{item.MDB_UIAP_0000000018}} ' +
            //'       ng-click=\"submit(formname, forms.mdbForm, item.localID, mdbForm[item.localID])\"' +
            //'       class="btn css-labeled-btn"> ' +
            //'       {{item.MDB_GUI_0000000088}} ' +
            //'       {{item.MDB_UIAP_0000000220}} ' + // TODO: wieder raus nehmen, wenn Ontologie aktualisiert wurde
            //'   </button>' +



        '</div>'
    };
});


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// MDB Documents /////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// directive for MDB pages, html page with one column
// usage:
/**<mdb-page></mdb-page>*/
MDBDirectives.directive('mdbPage', function (RecursionHelper, $rootScope, $location, $compile, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        template:
        '<div id="mdbpage" class=\"mdb_document container-fluid\">' +

        '   <ng-form name=\"forms.mdbDoc\" id={{mdbpage.load_page_localID}} class=\"formular\" autocomplete="off" novalidate style=\"height: 100%;\">                      ' +

        '       <div id=\"spinningLoader\" class=\"loader no-animate\" ng-show=\"loading\"></div>                                                        ' +

        '       <mdb-document-form-items items=\"mdbpage.data[0]\"></mdb-document-form-items>                                        ' +

        '   <div style=\"height: 300px;\" ng-show=\"developer\">                                                 ' +
        '       <div class="col-sm-6" style="padding-left: 0; padding-right: 5px;">                                                 ' +
        '           <pre >WsService.sent[0] = {{WsService.sent[0] | json}}</pre>                                                      ' +
        '           <pre >mdbDoc = {{mdbDoc | json}}</pre>                                                                            ' +
        '       </div>                                                                                                                ' +
        '       <div class="col-sm-6" style="padding-left: 5px; padding-right: 0;">                                                 ' +
        '           <pre >WsService.response[0] = {{WsService.response[0] | json}}</pre>                                              ' +
        '       </div>                                                                                                                ' +
        '   </div>                                                                                                                ' +

        '   </ng-form>                                                                                                                          ' +

        //'   <div id="mdboverlayid" class="modal fade" data-backdrop="static" data-keyboard="false">' +
        //'       <mdb-overlay mdboverlay=\"mdboverlay\"></mdb-overlay>' +
        //'   </div>' +

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
            $scope.mdbpartid = null;
            $scope.mdbactivetab = null;
            $scope.mdbpage = MDBDocumentService.getMDBPage();
            $scope.autocomplete = {};
            $scope.loading = true;
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
                            console.log("mdbPage on MDBPageComposition_updated - getMDBPage()\nmdbpage - " + mdbpage)
                        }
                        $scope.mdbpage = mdbpage;
                        if ($scope.mdbpage.data) {
                            $scope.mdbformid = $scope.mdbpage.load_page_localID;
                            MDBDocumentService.setInput($scope.mdbpage.data)
                                .then(function(items) {
                                    $scope.mdbDoc = items;
                                    console.info("$scope.mdbDoc: " + JSON.stringify($scope.mdbDoc));
                                });
                        }
                    })
                    .then(function(re) {
                        MDBDocumentService.resetDocument("page");
                    });
                $scope.loading = false;
                $scope.nopage = false;
            });

            //// stop loading button on incoming overlay composition
            //$scope.$on('MDBOverlayComposition_updated', function (event, args) {
            //    if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
            //        console.log("mdbPage on MDBOverlayComposition_updated\nevent - " + event + "\nargs - " + JSON.stringify(args))
            //    }
            //    $scope.loading = false;
            //});

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
            $rootScope.$on('WSFormValidationMessage', function (event, args) {
                //alert("args " + JSON.stringify(args));
                $scope.validation_message = args.validation_message;
                $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
                $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;

                function markValid(name, valid) {
                    if($scope.forms.mdbDoc) {
                        $scope.forms.mdbDoc.$setValidity("mdbvalid", valid);
                    }

                    $(`[name=${name}]`).removeClass(`ng-${valid ? "invalid" : "valid"}`);
                    $(`[name=${name}]`).addClass(`ng-${valid ? "valid" : "invalid"}`)
                }

                markValid(args.validation_field, $scope.validation_status === "true");

                if ($scope.validation_notification) {
                    NotificationService.showError($scope.validation_notification);
                }

                /*if (args.validation_form == $scope.mdbformid) {
                 if ($scope.validation_status === 'false') {
                 test.$setValidity("mdbvalid", false);
                 if ($scope.validation_notification) {
                 NotificationService.showError($scope.validation_notification);
                 }
                 }
                 else {
                 test.$setValidity("mdbvalid", true);
                 $scope.validation_notification = "";
                 }
                 }*/
            });

            // TODO: remove deleted input values from mdbDoc - does not work yet
            $rootScope.$on('WSFormUpdateMessage', function (event, args) {
                //socket_message: message,
                //socket_message_type: 'mdb_form_update',
                //update_id: message.localID,
                //update_message: message.data,
                //update_uris: message.data[0][message.localID].update_uri,
                //remove_uris: message.data[0][message.localID].delete_uri,
                //update_data: message.data[0][message.localID]

                //console.warn("Page - WSFormUpdateMessage\n" + JSON.stringify(args));
                $scope.remove_uris = args.remove_uris;
                $scope.update_uris = args.update_uris;
                $scope.update_data = args.update_data;

                console.error("MDBPage - on WSFormUpdateMessage\n" +
                    "mdbDoc  - " + JSON.stringify($scope.mdbDoc) + "\n" +
                    "mdbForm - " + $scope.forms);

                // TODO: remove deleted input values from mdbDoc
                /*if ($scope.update_data.hasOwnProperty('delete_uri')){
                 //"delete_uri":["MDB_DATASCHEME_0000000986_1", "MDB_DATASCHEME_0000000973_1"]
                 //console.warn("Overlay - WSFormUpdateMessage - 'delete_uri'\n" + JSON.stringify($scope.remove_uris));

                 angular.forEach($scope.remove_uris, function(value, key){

                 $scope.whut = $scope.mdbDoc.hasOwnProperty(value);

                 $scope.remove_element = angular.element("#" + value);
                 console.warn("remove_element\n" + JSON.stringify($scope.remove_element));

                 $scope.what = document.getElementById(value);
                 console.warn("remove_what\n" + JSON.stringify($scope.what));

                 //$scope.remove_element.find();

                 $scope.children = $scope.remove_element.children();
                 console.warn("$scope.children\n" + JSON.stringify($scope.children));
                 angular.forEach($scope.children, function(va, ke){
                 console.warn("$scope.remove_element.children()\n" + JSON.stringify(va));
                 });

                 $scope.par = $scope.remove_element.parent();

                 if ($scope.whut) {
                 $scope.abc = $scope.mdbDoc[value];
                 $scope.mdbDoc[value] = "";

                 };

                 });
                 }*/
            });

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////

            $scope.submit = MDBDocumentService.submit;

            $scope.submitAutocompleteSelect = MDBDocumentService.submitAutocompleteSelect;

            $scope.submitAutocompleteChange = MDBDocumentService.submitAutocompleteChange;

        }
    }
});

// directive for MDB overlays
// usage:
/** <mdb-overlay mdboverlay="mdboverlay"></mdb-overlay> */
MDBDirectives.directive('mdbOverlay', function (RecursionHelper, $rootScope, $location, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        //priority: 100000,
        scope: {
            mdboverlay: '=mdboverlay'
        },
        template:
        '   <form name="forms.mdbDoc" id={{mdboverlay.load_overlay_localID}} form-autofill-fix class="formular" autocomplete="off" novalidate> ' +

        '       <div class="modal-dialog modal-lg">' +
        '           <div class="modal-content">' +

        '               <div class="modal-header"> ' +
        '                   <button type="button" class="close" ng-click="cancel()">&times;</button>' +
        '               </div>' +

        '               <div class="modal-body">' +
        '                   <mdb-document-form-items items=\"mdboverlay.data[0]\" mdbformid=\"mdboverlay.load_overlay_localID\"></mdb-document-form-items>' +
        '               </div>' +

        '               <div class="modal-footer">' +
        '                   <button id="{{mdboverlay.data[1].localID}}" class="btn btn-sm btn-default" ng-disabled="!isValid()" ' + // ng-disabled="forms.mdbDoc.$invalid"
        '                       ng-click="submitCreate(mdbformid, forms.mdbDoc, mdboverlay.data[1].localID)" title="{{mdboverlay.data[1].MDB_UIAP_0000000018}}"     ' +
        '                       data-loading-text=\"<i class=\'fa fa-circle-o-notch fa-spin\'></i>  {{mdboverlay.data[1].MDB_GUI_0000000088}}\">                   ' +
        '                       {{mdboverlay.data[1].MDB_GUI_0000000088}}                                                                                          ' +
        '                   </button>' +
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
            $scope.mdbpartid = null;
            $scope.mdbactivetab = null;
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

            $scope.submitAutocompleteSelect = MDBDocumentService.submitAutocompleteSelect;

            $scope.submitAutocompleteChange = MDBDocumentService.submitAutocompleteChange;

            $scope.isValid = function() {
                let inputs = $("form[name='forms.mdbDoc'] input[required], form[name='forms.mdbDoc'] select[required]");
                return inputs.toArray().every(input => $(input).hasClass("ng-valid"))
            };

            // websocket events
            ///////////////////////////////////////////////////////////////////////////

            $scope.$on('MDBOverlayComposition_updated', function (event, args) {
                MDBDocumentService.getMDBOverlay()
                    .then(function(mdboverlay) {
                        if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                            console.log("mdbOverlay on MDBOverlayComposition_updated - getMDBOverlay()\nmdboverlay - " + JSON.stringify(mdboverlay))
                        }
                        $scope.mdboverlay = mdboverlay;
                        if ($scope.mdboverlay.data) {
                            $scope.mdbformid = $scope.mdboverlay.load_overlay_localID;
                            $scope.mdboverlayid = $scope.mdboverlay.widget;
                            MDBDocumentService.setInput($scope.mdboverlay.data)
                                .then(function(items) {
                                    $scope.mdbDoc = items;
                                });
                        }
                    })
                    .then(function(re) {
                        MDBDocumentService.resetDocument("overlay");
                    });

                //$scope.mdboverlay = args.mdbOverlay;
                //if ($scope.mdboverlay.data) {
                //    $scope.mdbformid = $scope.mdboverlay.load_overlay_localID;
                //    $scope.mdboverlayid = $scope.mdboverlay.widget;
                //    MDBDocumentService.setInput($scope.mdboverlay.data)
                //        .then(function(items) {
                //            $scope.mdbDoc = items;
                //        });
                //}
                $scope.showOverlay("#mdboverlayid");
                $scope.loading = false;
                //TODO: Check code: Shouldn't ths be '$scope.$apply()' ?
                $scope.$apply;
            });

            $rootScope.$on('WSFormValidationMessage', function (event, args) {
                //alert("args " + JSON.stringify(args));
                //socket_message: message,
                //socket_message_type: 'mdb_form_validation',
                //validation_form: message.html_form,
                //validation_message: message.data,
                $scope.validation_message = args.validation_message;
                $scope.validation_field = $scope.validation_message[0][args.validation_field];
                $scope.validation_status = $scope.validation_field.valid;
                $scope.validation_notification_error = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388; // error
                $scope.validation_notification_info = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000387; // info

                if (args.validation_form == $scope.mdbformid) {

                    $scope.mdbcreateBtnID = $scope.mdboverlay.data[1].localID;

                    if ($scope.validation_notification_error) {
                        NotificationService.showError($scope.validation_notification_error);
                        $scope.validation_notification_error = "";
                    }
                    else if ($scope.validation_notification_info) {
                        NotificationService.showSuccess($scope.validation_notification_info);
                        $scope.validation_notification_info = "";
                    }
                    if(args.validation_field == $scope.mdbcreateBtnID){
                        angular.element(document.getElementById($scope.mdbcreateBtnID)).button('reset');
                        $scope.cancel();
                    }
                    else {
                        if ($scope.validation_status == 'false') {
                            //angular.element("#" + args.validation_field).removeClass("ng-valid");
                            //angular.element("#" + args.validation_field).addClass("ng-invalid");
                            $scope.forms.mdbDoc[args.validation_field].$setValidity("mdbvalid", false);
                        }
                        else {
                            //angular.element("#" + args.validation_field).removeClass("ng-invalid");
                            //angular.element("#" + args.validation_field).addClass("ng-valid");
                            $scope.forms.mdbDoc[args.validation_field].$setValidity("mdbvalid", true);
                        }
                    }
                }
            });

            $rootScope.$on('WSFormAutocompleteMessage', function (event, args) {
                $scope.autocomplete[args.autocomplete_field] = args.autocomplete_data[0][args.autocomplete_field].autoCompleteData;
            });

            $rootScope.$on('WSFormUpdateMessage', function (event, args) {
                //socket_message: message,
                //socket_message_type: 'mdb_form_update',
                //update_id: message.localID,
                //update_message: message.data,
                //update_uris: message.data[0][message.localID].update_uri,
                //remove_uris: message.data[0][message.localID].delete_uri,
                //update_data: message.data[0][message.localID]

                //console.warn("Page - WSFormUpdateMessage\n" + JSON.stringify(args));
                $scope.remove_uris = args.remove_uris;
                $scope.update_uris = args.update_uris;
                $scope.update_data = args.update_data;

                console.warn("MDBOverlay - WSFormUpdateMessage" + "\n'delete_uri' " + JSON.stringify($scope.remove_uris) + "\n'update_uri' " + JSON.stringify($scope.update_uris));

                if ($scope.update_data.hasOwnProperty('delete_uri')){
                    //"delete_uri":["MDB_DATASCHEME_0000000986_1", "MDB_DATASCHEME_0000000973_1"]
                    //console.warn("Overlay - WSFormUpdateMessage - 'delete_uri'\n" + JSON.stringify($scope.remove_uris));

                    angular.forEach($scope.remove_uris, function(value, key){
                        $scope.remove_dom_id = "#" + value;
                        $scope.remove_element = angular.element($scope.remove_dom_id);
                        if ($scope.remove_element){
                            // TODO: remove values of deleted inputs from mdbDoc
                            //if ($scope.mdbDoc[value]) {$scope.mdbDoc[value] = "";};
                            $scope.remove_element_mdbcomponent = $scope.remove_element.parent(); // mdb-component
                            $scope.remove_element_mdbcomponent_repeatdiv = $scope.remove_element_mdbcomponent.parent(); // repeat div
                            $scope.remove_element_mdbcomponent_repeatdiv.remove();
                        }
                    });
                }

                if ($scope.update_data.hasOwnProperty('update_uri')){
                    //console.warn("Overlay - WSFormUpdateMessage - 'update_uri'\n" + JSON.stringify($scope.update_uris));

                    angular.forEach($scope.update_uris, function(value, key){
                        $scope.update_dom_id = "#" + value;
                        $scope.update_element = angular.element($scope.update_dom_id);

                        if ($scope.update_element) {
                            let targetScope = angular.element($scope.update_element.selector).parent().parent().scope();

                            targetScope.item = $scope.update_data[value];
                            targetScope.$apply();
                        }
                    });
                }
            });

            // OVERLAY FUNCTIONALITY /////////////////////////////////////////

            $scope.submitButton = {
                label: 'Ok',
                isDisabled: true
            };

            // TODO: Debug function
            $scope.getOverlayJSON = function () {
                alert(JSON.stringify($scope.mdboverlay));
            };
            // TODO: Debug function
            $scope.clearOverlayJSON = function () {
                $scope.mdboverlay = {};
            };

            $scope.submitCreate = function (mdbformid, form, button_localID) {
                //id="annotationLoadingButton" => id="{{mdboverlay.data[1].localID}}"
                //angular.element(document.getElementById('annotationLoadingButton')).button('loading');
                //angular.element(document.getElementById('annotationLoadingButton')).button('reset');
                // submit(mdbformid, forms.mdbDoc, mdboverlay.data[1].localID)"
                angular.element(document.getElementById(button_localID)).button('loading');
                $scope.submit(mdbformid, form, button_localID);
            };

            $scope.cancel = function () {
                angular.element("#mdboverlayid").modal('hide');
                $scope.mdbDoc = {};
                $scope.mdboverlay = {};
                close(null, 500);
            };

            $scope.showOverlay = function (element) {
                $(".overlay-spinner").remove();
                angular.element(element).modal('show');
            };
        }
    }
});


// directive for MDB pages, html page with one column
// usage:
/**<mdb-partonomy-page></mdb-partonomy-page>*/
MDBDirectives.directive('mdbPartonomyPage', function (RecursionHelper, $rootScope, $location, $compile, $http, $timeout, $q, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        template:
        `<div id="mdbpartonomypage" class="mdb_document" style="padding-bottom: 0;">                                                                                                                                                            
           <ng-form name="forms.mdbDoc" id={{mdbpartonomypage.load_page_localID}} class="formular" autocomplete="off" novalidate style="height: 100%;"> 
            <div class="description_widget">                                                                                                                                                  
                                                                                                                                                                                                
                <!-- Left column -->                                                                                                                                                            
                <div class="description_left_main_panel">                                                                                                                                     
                    <div class="description_breadcrumb_panel">                                                                                                                                
                        <!--<span class="mdb-icon icon-partonomy-icon"></span>-->                                                                                                             
                        <img class="css-icon-partonomy-icon" src="/img/icon_description_partonomy.svg">                                                                                     
                        <input class="mdb-description-breadcrumb-input" style="width: 80%; float: right;"/>                                                                                 
                    </div>                                                                                                                                                                      
                    <div class="description-bg-white" style="overflow-y: auto;">                                                                                                                                        
                        <div ui-tree id="tree-root">                                                                                                                                          
                            <ol ui-tree-nodes="" ng-model="mdbPartonomy" data-nodrop-enabled="true">                                                                                      
                                <li ng-repeat="node in mdbPartonomy" ui-tree-node ng-include="'nodes_renderer.html'" ng-show="visible(node)"></li>
                            </ol>                                                                                                                                                               
                        </div>                                                                                                                                                                  
                    </div>                                                                                                                                                                      
                    <div class="description_property_panel">                                                                                                                                  
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_group.jpg" title="group of scattered anatomical entities"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_MDB_GUI_0000000496.jpg" title="anatomical structure"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_MDB_GUI_0000000497.jpg" title="anatomical surface"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_MDB_GUI_0000000493.jpg" title="anatomical line"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_MDB_GUI_0000000494.jpg" title="anatomical point"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_MDB_GUI_0000000495.jpg" title="anatomical space"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_MDB_GUI_0000000498.jpg" title="portion of organism substance"></span>          
                        <span class="description_property_icon_frame"><img class="description_property_icon" src="/img/icon_description_property_not.jpg" title="absence of"></span>                         
                    </div>                                                                                                                                                                      
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
                <!-- Main column -->                                                                                                                                                            
                <div class="description_right_main_panel">                                                                                                                                    
                                                                                                                                                                                                
                    <div class="description-bg-white" ng-hide="selectedItem">                                                                                                               
                        <br><br>                                                                                                                                                                
                                                                                                                                                                                                
                        <div class="description-bg-white" ng-hide="selectedItem">                                                                                                           
                            <span class="h4">Please click on an item for details or modification.</span>                                                                                      
                        </div>                                                                                                                                                                  
                    </div>                                                                                                                                                                      
                                                                                                                                                                                                
                    <div class="description_header" ng-if="selectedItem">                                                                                                                   
                                                                                                                                                                                                
                        <!-- Top row -->                                                                                                                                                        
                        <div class="description_title_panel">                                                                                                                                 
                            <img ng-style="{'visibility': selectedItem.hasOwnProperty("MDB_DATASCHEME_0000002429") ? 'visible' : 'hidden'}"
                               src="/img/icon_description_property_{{selectedItem.MDB_DATASCHEME_0000002429}}.jpg">                                
                               <!-- TODO: workaround for anatomical structure images until relative paths are implemented to application ontology
                               src="{{selectedItem.MDB_DATASCHEME_0000002429}}"> --> 
                            <span class="css-headline1">{{selectedItem.MDB_UIAP_0000000527}}</span>&nbsp;&nbsp;                                                                                             
                            <span class="css-paragraph1">{{selectedItem.label_class}}</span>                                                                
                        </div>                                                                                                                                                                  
                                                                                                                                                                                                
                        <div class="description_header_nav">                                                                                                                                  
                            <uib-tabset> <!-- active="activeJustified" justified="true">-->                                                                                                 

                              <uib-tab index="0" active="mdbTabs.formaldescription.active" ng-click="onTabChange(index, mdbTabs, scope)">                                                                                             
                                  <uib-tab-heading>                                                                                             
                                      <div class="description_header_nav_tab_heading">                                                        
                                          <i class="partonomy-icon css-icon-formal-description-icon" title="Formal description"></i>&nbsp;  
                                          <span class="css-paragraph1 hidden-xs hidden-sm hidden-md">Formal&nbsp;description</span>           
                                      </div>                                                                                                    
                                  </uib-tab-heading>                                                                                            
                                  <div ng-include=""description_form.html""></div>                                                          
                              </uib-tab>                                                                                                        
                              <uib-tab index="1" active="mdbTabs.metadata.active" ng-click="onTabChange(index, mdbTabs, scope)">                                                                                             
                                  <uib-tab-heading>                                                                                             
                                      <div class="description_header_nav_tab_heading">                                                        
                                          <i class="css-icon-metadata-icon partonomy-icon" title="Metadata"></i>                            
                                          <span class="css-paragraph1 hidden-xs hidden-sm hidden-md">Metadata</span>                          
                                      </div>                                                                                                    
                                  </uib-tab-heading>                                                                                            
                                  <div ng-include=""description_metadata.html""></div>                                                      
                              </uib-tab>                                                                                                        

                              <uib-tab index="2" active="mdbTabs.text.active" ng-click="onTabChange(index, mdbTabs, scope)">                                                                                             
                                  <uib-tab-heading>                                                                                             
                                      <div class="description_header_nav_tab_heading">                                                        
                                          <i class="partonomy-icon css-icon-text-icon" title="Text"></i>&nbsp;                              
                                          <span class="css-paragraph1 hidden-xs hidden-sm hidden-md">Text</span>                              
                                      </div>                                                                                                    
                                  </uib-tab-heading>                                                                                            
                                  <div ng-include=""description_text.html""></div>                                                          
                              </uib-tab>                                                                                                        
                              <uib-tab index="3" active="mdbTabs.image.active" onclick="window._reloadTool()">                                                            
                                  <uib-tab-heading>                                                                                             
                                      <div class="description_header_nav_tab_heading">                                                        
                                          <i class="partonomy-icon css-icon-image-icon" title="Image"></i>&nbsp;                            
                                          <span class="css-paragraph1 hidden-xs hidden-sm hidden-md">Image</span>                             
                                      </div>                                                                                                    
                                  </uib-tab-heading>                                                                                            
                                  <div ng-include=""description_image.html""></div>                                                         
                              </uib-tab>                                                                                                        

                              <uib-tab index="4" active="mdbTabs.graph.active" ng-click="onTabChange(index, mdbTabs, scope)">                                                                                             
                                  <uib-tab-heading>                                                                                             
                                      <div class="description_header_nav_tab_heading">                                                        
                                          <i class="partonomy-icon css-icon-triple-icon" title="Graph view"></i>&nbsp;                      
                                          <span class="css-paragraph1 hidden-xs hidden-sm hidden-md">Graph</span>                             
                                      </div>                                                                                                    
                                  </uib-tab-heading>                                                                                            
                              <div ng-include=""description_graph.html""></div>                                                             
                              </uib-tab>                                                                                                        

                            </uib-tabset>                                                                                                                                                       
                        </div>                                                                                                                                                                  
                                                                                                                                                                                                
                    </div>                                                                                                                                                                      
                                                                                                                                                                                                
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
            </div>                                                                                                                                                                              
           </ng-form>
                                                                                                                                                                                                
        <!-- Nested node template -->
        <script type="text/ng-template" id="nodes_renderer.html">                                                                                                                          
            <div style="display: flex; width: inherit;">                                                                                                                                      
                <div class="angular-ui-tree-node-content-collapseexpandbtn" ng-if="node.BFO_0000051 && node.BFO_0000051.length > 0"                                                                     
                     ng-click="toggle(this)">                                                                                                                                                 
                    <span class="glyphicon" ng-class="{"glyphicon-plus": collapsed, "glyphicon-minus": !collapsed}"></span>                                                             
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
                <div class="angular-ui-tree-node-content-collapseexpandbtn" ng-hide="node.BFO_0000051 && node.BFO_0000051.length > 0">                                                                  
                    <span class="glyphicon glyphicon-minus"></span>                                                                                                                           
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
                <div class="angular-ui-tree-node-content" 
                       ng-click="submit(mdbformid, forms.mdbDoc, node.localID, "show_localID", mdbpartid, mdbactivetab)" // $scope.setSelectedItem(node, this);
                       ng-class="{active: node.localID === selectedItemID}">                                                                                                                    
                                                                                                                                                                                                
                    <!--<span title="drag to move item" class="glyphicon glyphicon-menu-hamburger" ui-tree-handle></span>-->                                                                
                                                                                                                                                                                                
                    <div class="tree-node-icon" ui-tree-handle title="drag to move item" ng-style="{"visibility": node.hasOwnProperty("MDB_DATASCHEME_0000002429") ? "visible" : "hidden"}">                                                                                                   
                        <img ng-style="{"visibility": node.hasOwnProperty("MDB_DATASCHEME_0000002429") ? "visible" : "hidden"}"
                               src="/img/icon_description_property_{{node.MDB_DATASCHEME_0000002429}}.jpg">
                                <!-- TODO: workaround for anatomical structure images until relative paths are implemented to application ontology
                               src="{{node.MDB_DATASCHEME_0000002429}}"> -->
                    </div>                                                                                                                                                                      
                    <div class="tree-node-annotations">                                                                                                                                       
                        <span class="css-icon-text-icon tree-node-annotation-icon"></span>   
                        <span class="css-icon-image-icon tree-node-annotation-icon"></span>   
                    </div>                                                                                                                                                                      
                    <div class="tree-node-title"><span>{{node.MDB_UIAP_0000000527}}</span></div>                                                                                                            
                                                                                                                                                                                                
                </div>                                                                                                                                                                          
            </div>                                                                                                                                                                              
                                                                                                                                                                                                
            <ol ui-tree-nodes="" ng-model="node.BFO_0000051" ng-class="{hidden: collapsed}">                                                                                                    
                <li ng-repeat="node in node.BFO_0000051" ui-tree-node ng-include=""nodes_renderer.html"">                                                                                       
                </li>                                                                                                                                                                           
            </ol>                                                                                                                                                                               
        </script>                                                                                                                                                                               


        <!-- template -->                                                                                                                                                                       
        <script type="text/ng-template" id="description_form.html">                                                                                                                         
                                                                                                                                                                                                
            <div ng-if="selectedItem" style="display: -webkit-flex; display: flex; -webkit-flex-direction: column; flex-direction: column;">                                                                                                                                          
            /*mdbPartonomyHeader*/

                <div>                                                                                                                                                       
                    <div style="display:block">                                                                                                                                               
                        <div class="description_active_part_panel">                                                                                                                           
                               <mdb-document-form-items items="mdbPartonomyHeader"></mdb-document-form-items>                                        
                        </div>                                                                                                                                                                  
                        <br><br>                                                                                                                                                                
                    </div>                                                                                                                                                                      
                </div>                                                                                                                                                                          
                                                                                                                                                                                               
               <div class="description-accordion">                                                                                                                                                       
                   <uib-accordion close-others="oneAtATime" class="description-formal-description-panels">                                                                                 
                                                                                                                                                                                                
                        <div uib-accordion-group is-open="panelstatus.isOpen.formal_des_haspart">                                                                                             
                                                                                                                                                                                                
                            <uib-accordion-heading>                                                                                                                                             
                                <i class="btn"                                                                                                                                 
                                   ng-class="{"css-expand-collapse-btn": panelstatus.isOpen.formal_des_haspart, "css-expand-collapse-btn css-expand-collapse-btn-closed": !panelstatus.isOpen.formal_des_haspart}">&nbsp;</i>        
                                <div class="css-headline1" style="font-size: 18px;margin-top: 0;">has Part</div>                                                                                                                                         
                            </uib-accordion-heading>                                                                                                                                            
                                        
                            <div style="display: grid">                                                                                                                                    
                               <div ng-show="selectedItem.BFO_0000051.length > 0" class="col-sm-10" style="display:block">                                                                         
                                   <div ng-repeat="node in selectedItem.BFO_0000051" class="description-formal-description-haspart">                                                                 
                                       <img ng-style="{"visibility": node.hasOwnProperty("MDB_DATASCHEME_0000002429") ? "visible" : "hidden"}"                                                                                                               
                                           src="/img/icon_description_property_{{node.MDB_DATASCHEME_0000002429}}.jpg">                               
                                           <!-- TODO: workaround for anatomical structure images until relative paths are implemented to application ontology
                                           src="{{node.MDB_DATASCHEME_0000002429}}"> -->
                                       <span class="css-label1 hasTitle"> {{node.MDB_UIAP_0000000527}} </span>                                                                                                 
                                       <span class="css-label1 typeOf"> {{node.label_class}}</span>                                                                    
                                       <button ng-if="mdbformid.indexOf("-p_") == -1" ng-click="selectedItem.BFO_0000051.splice($index, 1)" class="btn-small removePart" >                                                                  
                                           <span class="glyphicon glyphicon-remove"></span>                                                                                                      
                                       </button>                                                                                                                                                   
                                   </div><br><br>                                                                                                                                                        
                               </div>    
                                                                                                                                                                  
                               <mdb-document-form-items items="mdbTabs.formaldescription.addline"></mdb-document-form-items>                                        
                            </div>    
                                                                                                                                                                                                
                        </div>                                                                                                                                                                  
                                                                                                                                                                                                
                    </uib-accordion>                                                                                                                                                            
                    <mdb-document-form-items items="mdbTabs.formaldescription.accordion"></mdb-document-form-items>                                        
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
            </div>                                                                                                                                                                              
        </script>                                                                                                                                                                               
                                                                                                                                                                                                
        <!-- template -->                                                                                                                                                                       
        <script type="text/ng-template" id="description_metadata.html">                                                                                                                     
                                                                                                                                                                                                
            <br><br>                                                                                                                                                                            
                                                                                                                                                                                                
            <div class="description-bg-white">                                                                                                                                                
                                                                                                                                                                                                
                <div class="sample">                                                                                                                                                          
                    <img src="/img/sample_images/sample_metadata.png" title="sample" class="img-responsive">                                                                              
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
                                                                                                                                                                                                
            </div>                                                                                                                                                                              
                                                                                                                                                                                                
        </script>   

        <!-- template -->                                                                                                                                                                       
        <script type="text/ng-template" id="description_text.html">                                                                                                                         
            <form name="forms.annotateText" class="description-text_container-fluid">                                                                                                       
                                                                                                                                                                                                
                <div class="description-text_top">                                                                                                                                            
                    <input class="checkbox css-check" type="checkbox" ng-model="annotationForm.longest_only">                                                                             
                    <div class="css-paragraph1">Match Longest Only</div>                                                                                                                      
                    <input class="checkbox css-check" type="checkbox" ng-model="annotationForm.mappings_all">                                                                             
                    <div class="css-paragraph1">Include Mappings</div>                                                                                                                        
                    <input class="checkbox css-check" type="checkbox" ng-model="annotationForm.exclude_numbers">                                                                          
                    <div class="css-paragraph1">Exclude Numbers</div>                                                                                                                         
                    <input class="checkbox css-check" type="checkbox" ng-model="annotationForm.exclude_synonyms">                                                                         
                    <div class="css-paragraph1">Exclude Synonyms</div>                                                                                                                        
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
                <div class="description-text_main">                                                                                                                                           
                                                                                                                                                                                                
                    <div class="description-text_left">                                                                                                                                       
                                                                                                                                                                                                
                            <div class="description-text_annotation_box_top description-text_annotation_box">                                                                                 
                                <div class="description-text_annotation_box_header">                                                                                                          
                                    <div class="description-text_annotation_textarea_header">                                                                                                 
                                        <button class="btn" ng-click="setTestValues()" title="Load example text from wikipedia and select some NCBO ontologies for demonstration">Example text</button> &nbsp;&nbsp;                                                                  
                                        <button class="btn" ng-click="setDefault()">Clear</button> &nbsp;&nbsp;                                                                             
                                        <button class="btn"                                                                                                                                   
                                                id="annotationLoadingButton"                                                                                                                  
                                                ng-click="checkAnnotations()"
                                                title="Start automatic annotation of the text using the selected ontologies"                                                                                      
                                                data-loading-text="<i class="fa fa-circle-o-notch fa-spin"></i> Loading...">Annotate!</button></div>                                        
                                </div>                                                                                                                                                          
                                                                                                                                                                                                                  
                                <div contenteditable="true" id="divtextarea" class="description-text_annotation_box_main description-text_annotation_textarea"></div>                     
                                <!--<textarea id="divtextarea" class="description-annotation_textarea" ng-model="selectedItem.text_annotation.text"></textarea>-->                        
                            </div>                                                                                                                                                              
                                                                                                                                                                                                
                            <div class="description-text_annotation_box_bottom description-text_annotation_box">                                                                              
                                <div class="description-text_annotation_box_header" style="color: #808080">Ontologies in use</div>                                                          
                                <div class="description-text_annotation_box_main">                                                                                                            
                                    <tags-input ng-model="ontologytags"                                                                                                                       
                                                display-property="name"                                                                                                                       
                                                placeholder="Type to get suggestions for ontologies or leave blank to use all  "                                                              
                                                replace-spaces-with-dashes="false">                                                                                                           
                                        <auto-complete source="loadOntologyTags($query)"                                                                                                      
                                                       min-length="0"                                                                                                                         
                                                       load-on-focus="true"                                                                                                                   
                                                       load-on-empty="true"                                                                                                                   
                                                       max-results-to-show="1000"></auto-complete>                                                                                            
                                    </tags-input>                                                                                                                                               
                                </div>                                                                                                                                                          
                            </div>                                                                                                                                                              
                                                                                                                                                                                                
                    </div>                                                                                                                                                                      
                                                                                                                                                                                                
                    <div class="description-text_right">                                                                                                                                      
                                                                                                                                                                                                
                        <div ui-grid="gridOptions" ui-grid-selection ui-grid-auto-resize ui-grid-resize-columns ui-grid-move-columns class="grid"></div>                                    
                                                                                                                                                                                                
                    </div>                                                                                                                                                                      
                </div>                                                                                                                                                                          
            </form>                                                                                                                                                                             
        </script>                                                                                                                                                                               
                                                                                                                                                                                                
        <!-- template -->                                                                                                                                                                       
        <script type="text/ng-template" id="description_image.html">                                                                                                                        
                                                                                                                                                                                                
                <!-- The Image Annotation Tool -->                                                                                                                                              
                <div layout="row">                                                                                                                                                            
                    <div class="col-md-4 description-accordion"> 
                                                                                                                                                                                                
                        <!--<p>Todo: add am menue to choose different images</p>-->                                                                                                             
                        <!--<h3>Tools</h3>-->                                                                                                                                                   
                                                                                                                                                                                                
                                                                                                                                                                                                
                        <uib-accordion close-others="oneAtATime" class="description-formal-description-panels">                                                                             
                                                                                                                                                                                                
                        <div uib-accordion-group is-open="panelstatus.isOpen.image_tools">                                                                                                    
                                                                                                                                                                                                
                            <uib-accordion-heading>                                                                                                                                             
                                <i class="pull-left mdb-icon"                                                                                                                                 
                                   ng-class="{"icon-collapse-icon": panelstatus.isOpen.image_tools, "icon-expand-icon": !panelstatus.isOpen.image_tools}">                                
                                    &nbsp;</i>                                                                                                                                                  
                                Tools                                                                                                                                                           
                            </uib-accordion-heading>                                                                                                                                            
                            <div>                                                                                                                                                               
                                <form class="form-inline">                                                                                                                                    
                                    <div id="layers">                                                                                                                                         
                                        <label class="col-md-4">Choose Shape &nbsp;</label>                                                                                                   
                                        <fieldset>                                                                                                                                              
                                            <fieldset class="drawradio">                                                                                                                      
                                                <!--<option value="Fhl">Freehand Line</option>                                                                                                
                                                <option value="Fha">Freehand Area</option>-->                                                                                                 
                                                <input class="drawrb" type="radio" name="drawtype" id="dpoint" value="Point"/>                                                                                                                        
                                                <label class="drawlabel" for="dpoint"><img                                                                                                  
                                                        src="/exlibs/img/drawpoints.png" width="40px" height="40px" alt="Points"/></label>                                                                                                                
                                                <input class="drawrb" type="radio" name="drawtype" id="dline" value="LineString"/>                                                                                                                   
                                                <label class="drawlabel" for="dline"><img                                                                                                   
                                                        src="/exlibs/img/drawline.png" width="40px" height="40px" alt="LineString"/></label>                                                                                                            
                                            </fieldset>                                                                                                                                         
                                            <fieldset class="drawradio">                                                                                                                      
                                                <input class="drawrb" type="radio" name="drawtype" id="dpolygon" value="Polygon" checked="checked"/>                                                                                                  
                                                <label class="drawlabel" for="dpolygon"><img                                                                                                
                                                        src="/exlibs/img/drawpolygon.png" width="40px" height="40px" alt="Polygon"/></label>                                                                                                               
                                                <input class="drawrb" type="radio" name="drawtype" id="dcircle" value="Circle"/>                                                                                                                       
                                                <label class="drawlabel" for="dcircle"><img                                                                                                 
                                                        src="/exlibs/img/drawcircle.png" width="40px" height="40px" alt="Circle"/></label>                                                                                                                
                                            </fieldset>                                                                                                                                         
                                            <fieldset class="drawradio">                                                                                                                      
                                                <input class="drawrb" type="radio" name="drawtype" id="dsquare" value="Square"/>                                                                                                                       
                                                <label class="drawlabel" for="dsquare"><img                                                                                                 
                                                        src="/exlibs/img/drawsquare.png" width="40px" height="40px" alt="Square"/></label>                                                                                                                
                                                <input class="drawrb" type="radio" name="drawtype" id="dbox" value="Box"/>                                                                                                                          
                                                <label class="drawlabel" for="dbox"><img                                                                                                    
                                                        src="/exlibs/img/drawbox.png" width="40px" height="40px" alt="Box"/></label>                                                                                                                   
                                            </fieldset>                                                                                                                                         
                                            <fieldset class="drawradio">                                                                                                                      
                                                <input class="drawrb" type="radio" name="drawtype" id="dedit" value="Edit"/>                                                                                                                         
                                                <label class="drawlabel" for="dedit"><img 
                                                       src="/exlibs/img/drawedit.png" width="40px" height="40px" alt="Edit"/></label>                                                                                                                  
                                            </fieldset>                                                                                                                                         
                                        </fieldset>                                                                                                                                             
                                                                                                                                                                                                
                                    </div>                                                                                                                                                      
                                    <div>                                                                                                                                                       
                                        <label class="col-md-4">Choose object</label>                                                                                                         
                                        <select id="layerlist">                                                                                                                               
                                            <option value="0">Object 0</option>                                                                                                               
                                        </select>                                                                                                                                               
                                        &nbsp;<span id="colorindicator">&nbsp;&nbsp;&nbsp;</span>                                                                                             
                                    </div>                                                                                                                                                      
                                                                                                                                                                                                
                                                                                                                                                                                                
                                    <div>                                                                                                                                                       
                                        <label class="col-md-4">Add a new object</label>                                                                                                      
                                        <button id="addlayer" type="button">new object                                                                                                      
                                        </button>                                                                                                                                               
                                    </div>                                                                                                                                                      
                                                                                                                                                                                                
                                    <!--<div>Todo: for some reason new layers can only be added if window was not reloaded. <br>                                                                
                                        Try &lt;strg&gt; + &lt;F5&gt;</div>-->                                                                                                                  
                                </form>                                                                                                                                                         
                            </div>                                                                                                                                                              
                        </div>                                                                                                                                                                  
                                                                                                                                                                                                
                        <div uib-accordion-group is-open="panelstatus.isOpen.image_help">                                                                                                     
                                                                                                                                                                                                
                            <uib-accordion-heading>                                                                                                                                             
                                <i class="pull-left mdb-icon"                                                                                                                                 
                                   ng-class="{"icon-collapse-icon": panelstatus.isOpen.image_help, "icon-expand-icon": !panelstatus.isOpen.image_help}">                                  
                                    &nbsp;</i>                                                                                                                                                  
                                Help                                                                                                                                                            
                            </uib-accordion-heading>                                                                                                                                            
                            <div>                                                                                                                                                               
                                <h4>Image navigation</h4>                                                                                                                                       
                                <ul>                                                                                                                                                            
                                    <li>Pan image: press and hold left mouse button</li>                                                                                                        
                                    <li>Zoom image: roll mouse wheel to zoom in and out</li>                                                                                                    
                                </ul>                                                                                                                                                           
                                <h4>Keyboard</h4>                                                                                                                                               
                                <ul>                                                                                                                                                            
                                    <li>Del-key: delete selected shape (active object only)</li>                                                                                                
                                    <li>Esc-key: cancel current drawing, change to "Edit" mode</li>                                                                                           
                                </ul>                                                                                                                                                           
                                <h4>Draw freehand:</h4>                                                                                                                                         
                                <ol>                                                                                                                                                            
                                    <li>Choose "Line String" or "Polygon" tool</li>                                                                                                         
                                    <li>Hold "Shift"-key while drawing</li>                                                                                                                   
                                </ol>                                                                                                                                                           
                                <h4>Edit shapes:</h4>                                                                                                                                           
                                <ol>                                                                                                                                                            
                                    <li>Select "Edit" mode (Esc-key)</li>                                                                                                                     
                                    <li>Mark shape by clicking into it</li>                                                                                                                     
                                    <li>Add nodes by click and drag on line between existing nodes                                                                                              
                                    </li>                                                                                                                                                       
                                    <li>Delete node by clicing on it</li>                                                                                                                       
                                    <li>Move node by cklick and hold mous button while dragging the                                                                                             
                                        node                                                                                                                                                    
                                    </li>                                                                                                                                                       
                                </ol>                                                                                                                                                           
                            </div>                                                                                                                                                              
                        </div>                                                                                                                                                                  
                        </uib-accordion>                                                                                                                                                        
                    </div>                                                                                                                                                                      
                </div>                                                                                                                                                                          
                                                                                                                                                                                                
                                                                                                                                                                                                
                <div class="col-md-8">                                                                                                                                                        
                    <div id="image-annotator">                                                                                                                                                
                                                                                                                                                                                                
                        <div id="spinningLoaderDiv" style="padding: 100px;">                                                                                                                
                            <div id="spinningLoader" class="loader" style="margin: 0 auto;"></div>                                                                                        
                        </div>                                                                                                                                                                  
                                                                                                                                                                                                
                        <div id="map" class="map"></div>                                                                                                                                    
                                                                                                                                                                                                
                    </div>                                                                                                                                                                      
                    <div style="clear: both"></div>                                                                                                                                           
                </div>                                                                                                                                                                          
        </script>                                                                                                                                                                               

        <!-- template -->
        <script type="text/ng-template" id="description_graph.html">
                                                                                                                                                                                                
           <div class="description-bg-white">
               <div class="sample">
                   <img src="/img/sample_images/sample_graph_display.jpg" title="sample" class="img-responsive">
               </div>
           </div>
                                                                                                                                                                                                
        </script> 

        </div>`                                                                                                                                                                                  
        ,
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope, $rootScope) {
            $scope.forms = {};
            $scope.mdbDoc = {};
            $scope.mdbformid = '';
            $scope.mdbpartid = null;
            $scope.mdbactivetab = null;

            $scope.mdbpartonomypage = MDBDocumentService.getMDBPartonomyPage();
            $scope.mdbPartonomy = [];
            $scope.mdbPartonomyHeader = {};

            $scope.mdbTabs = {};
            $scope.mdbTabs.formaldescription = {};
            $scope.mdbTabs.formaldescription.active = true;
            $scope.mdbTabs.formaldescription.form = {};
            $scope.mdbTabs.formaldescription.addline = {};
            $scope.mdbTabs.formaldescription.accordion = {};
            $scope.mdbTabs.metadata = {};
            $scope.mdbTabs.metadata.active = false;
            $scope.mdbTabs.metadata.form = {};
            $scope.mdbTabs.text = {};
            $scope.mdbTabs.text.active = false;
            $scope.mdbTabs.text.form = {};
            $scope.mdbTabs.image = {};
            $scope.mdbTabs.image.active = false;
            $scope.mdbTabs.image.form = {};
            $scope.mdbTabs.graph = {};
            $scope.mdbTabs.graph.active = false;
            $scope.mdbTabs.graph.form = {};

            $scope.setAllTabsFalse = function () {
                $scope.mdbTabs.formaldescription.active = false;
                $scope.mdbTabs.metadata.active = false;
                $scope.mdbTabs.text.active = false;
                $scope.mdbTabs.image.active = false;
                $scope.mdbTabs.graph.active = false;
            };

            $scope.autocomplete = [];
            $scope.selectedItemID = '';
            $scope.selectedItem = {};
            $scope.selectedNode = {};
            $scope.loading = true;
            $scope.nopage = true;
            $scope.WsService = WsService;

            $scope.currentUser = AuthService.getUsername();
            $scope.developer = false;
            if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){$scope.developer = true;}

            if ($scope.mdbpartonomypage.data) {
                MDBDocumentService.setInput($scope.mdbpartonomypage.data)
                    .then(function(items) {
                        $scope.mdbDoc = items;
                        console.info("$scope.mdbDoc: " + JSON.stringify($scope.mdbDoc));
                    });
            }

            // websocket events
            ///////////////////////////////////////////////////////////////////////////

            // update $scope.mdbpartonomypage
            $scope.$on('MDBPartonomyPageComposition_updated', function (event, args) {
                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("MDBPartonomyPageComposition_updated\nmdbpartonomypage - " + JSON.stringify(args))
                }

                $scope.mdbpartonomypage = args.socket_message;
                if ($scope.mdbpartonomypage.data) {

                    $scope.mdbformid = $scope.mdbpartonomypage.load_page_localID;
                    $scope.mdbpartid = $scope.mdbpartonomypage.partID;
                    $scope.mdbactivetab = $scope.mdbpartonomypage.active_tab;

                    $scope.mdbPartonomy[0] = $scope.mdbpartonomypage.data[0];

                    //if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    //    console.log("$scope.mdbPartonomy\n - " + JSON.stringify($scope.mdbPartonomy))
                    //}

                    if ($scope.mdbactivetab == "form"){
                        $scope.mdbTabs.formaldescription.form = $scope.mdbpartonomypage.data[1];
                        $scope.mdbTabs.formaldescription.addline = $scope.mdbpartonomypage.data[1];
                        $scope.mdbTabs.formaldescription.accordion = {};
                        $scope.setAllTabsFalse();
                        $scope.mdbTabs.formaldescription.active = true;
                    }
                    else if ($scope.mdbactivetab == "metadata"){
                        $scope.mdbTabs.metadata.form = $scope.mdbpartonomypage.data[1];
                        $scope.setAllTabsFalse();
                        $scope.mdbTabs.metadata.active = true;
                    }
                    else if ($scope.mdbactivetab == "text"){
                        $scope.mdbTabs.text.form = $scope.mdbpartonomypage.data[1];
                        $scope.setAllTabsFalse();
                        $scope.mdbTabs.text.active = true;
                    }
                    else if ($scope.mdbactivetab == "image"){
                        $scope.mdbTabs.image.form = $scope.mdbpartonomypage.data[1];
                        $scope.setAllTabsFalse();
                        $scope.mdbTabs.image.active = true;
                    }
                    else if ($scope.mdbactivetab == "graph"){
                        $scope.mdbTabs.graph.form = $scope.mdbpartonomypage.data[1];
                        $scope.setAllTabsFalse();
                        $scope.mdbTabs.graph.active = true;
                    }

                    $scope.setSelectedItem($scope.mdbPartonomy[0]);

                    MDBDocumentService.setInput($scope.mdbpartonomypage.data)
                        .then(function(items) {
                            $scope.mdbDoc = items;
                            if ($scope.mdbpartonomypage.data[0].hasOwnProperty("MDB_CORE_0000000727")){
                                console.info("$scope.mdbpartonomypage.data[0].MDB_UIAP_0000000527: " + JSON.stringify($scope.mdbpartonomypage.data[0].MDB_UIAP_0000000527));
                                $scope.mdbDoc[$scope.mdbpartonomypage.data[0].MDB_DATASCHEME_0000002740] = $scope.mdbpartonomypage.data[0].MDB_UIAP_0000000527;
                            }
                        });
                }

                /*
                MDBDocumentService.getMDBPartonomyPage()
                    .then(function(mdbpartonomypage) {

                    if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                        console.log("mdbpartonomypage on MDBPageComposition_updated - getMDBPage()\nmdbpartonomypage - " + mdbpartonomypage)
                    }

                    //$scope.mdbpartonomypage = mdbpartonomypage;
                    $scope.mdbpartonomypage = args.socket_message;

                    if ($scope.mdbpartonomypage.data) {
                        $scope.mdbPartonomy.push($scope.mdbpartonomypage.data[0]);
                    if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                        console.log("$scope.mdbPartonomy\n - " + JSON.stringify($scope.mdbPartonomy))
                    }
                    $scope.mdbFormular.push($scope.mdbpartonomypage.data[1]);
                    if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                        console.log("$scope.mdbFormular\n - " + JSON.stringify($scope.mdbFormular))
                    }

                    $scope.selectedItemID = $scope.mdbPartonomy[0].localId;
                    $scope.selectedItem = $scope.mdbPartonomy[0];
                    $scope.selectedNode = $scope.mdbPartonomy[0];
                    $scope.mdbformid = $scope.mdbpartonomypage.load_page_localID;

                    MDBDocumentService.setInput($scope.mdbpartonomypage.data)
                        .then(function(items) {
                            $scope.mdbDoc = items;
                        });
                    }
                })
                    .then(function(re) {
                        MDBDocumentService.resetDocument("partonomy_page");
                    });
                */
                $scope.loading = false;
                $scope.nopage = false;
            });

            // update $scope.mdbpartonomypage positions
            $scope.$on('MDBPartonomyPagePosition_updated', function (event, args) {

                //$rootScope.$on('WSPartonomyPagePositionUpdateMessage', function (event, args) {
                //    let partoPosition = args.socket_message;
                //    $rootScope.$broadcast('MDBPartonomyPagePosition_updated', {
                //        socket_message: partoPosition,
                //        socket_message_type: 'mdb_partonomy_page_position',
                //        update_id: partoPosition.localID,
                //        update_message: partoPosition.data,
                //        update_position: partoPosition.data[0][partoPosition.localID].update_position,
                //        update_data: partoPosition.data[0][partoPosition.localID]
                //    });
                //});

                /*if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("MDBPartonomyPagePosition_updated\nmdbpartonomyposition_update - " + JSON.stringify(args))
                }*/

                $scope.mdbpartonomyposition_update = args.socket_message; // whole message
                $scope.mdbpartonomyposition_update_message = args.update_message; // data array
                $scope.mdbpartonomyposition_update_data = args.update_data; // list of update data
                $scope.mdbpartonomyposition_update_position = args.update_position; // array of positions to be updated

                $scope.mdbpartid = $scope.mdbpartonomyposition_update.partID; // selected part
                $scope.mdbactivetab = $scope.mdbpartonomyposition_update.active_tab; // selected tab

                // list of all promises
                var promises = [];

                if ($scope.mdbpartonomyposition_update_position.length) {

                    angular.forEach($scope.mdbpartonomyposition_update_position, function(value, key) {

                        // $q deferred promise
                        var deferred = $q.defer();

                        // "1" is Partonomy
                        if(value == "1"){

                            $scope.mdbPartonomy[0] = $scope.mdbpartonomyposition_update_data[value];

                            // promise successfully resolved
                            deferred.resolve("1");
                        }
                        // "2" is Tab & Form
                        else if (value == "2"){
                            if ($scope.mdbactivetab == "form"){
                                $scope.mdbTabs.formaldescription.form = $scope.mdbpartonomyposition_update_data[value];
                                $scope.mdbTabs.formaldescription.addline = $scope.mdbTabs.formaldescription.form.MDB_GUI_0000000040[0];
                                $scope.mdbTabs.formaldescription.accordion = $scope.mdbTabs.formaldescription.form.MDB_GUI_0000000040[1];
                                $scope.setAllTabsFalse();
                                $scope.mdbTabs.formaldescription.active = true;
                            }
                            else if ($scope.mdbactivetab == "metadata"){
                                $scope.mdbTabs.metadata.form = $scope.mdbpartonomyposition_update_data[value];
                                $scope.setAllTabsFalse();
                                $scope.mdbTabs.metadata.active = true;
                            }
                            else if ($scope.mdbactivetab == "text"){
                                $scope.mdbTabs.text.form = $scope.mdbpartonomyposition_update_data[value];
                                $scope.setAllTabsFalse();
                                $scope.mdbTabs.text.active = true;
                            }
                            else if ($scope.mdbactivetab == "image"){
                                $scope.mdbTabs.image.form = $scope.mdbpartonomyposition_update_data[value];
                                $scope.setAllTabsFalse();
                                $scope.mdbTabs.image.active = true;
                            }
                            else if ($scope.mdbactivetab == "graph"){
                                $scope.mdbTabs.graph.form = $scope.mdbpartonomyposition_update_data[value];
                                $scope.setAllTabsFalse();
                                $scope.mdbTabs.graph.active = true;
                            }

                            let inputarray = [];
                            inputarray.push($scope.mdbpartonomyposition_update_data[value]);
                            MDBDocumentService.setInput(inputarray)
                                .then(function(items) {
                                    $scope.mdbDoc = {};
                                    $scope.mdbDoc = items;
                                    console.info("setInput: $scope.mdbDoc: " + JSON.stringify($scope.mdbDoc));
                                });

                            // promise successfully resolved
                            deferred.resolve("2");
                        }

                        // add to the list of promises
                        promises.push(deferred.promise);
                    });

                    // execute all the promises and do something with the results
                    $q.all(promises).then(
                        // success
                        // results: an array of data objects from each deferred.resolve(data) call
                        function(results) {
                            $scope.getandsetSelectedItem($scope.mdbPartonomy, $scope.mdbpartid);
                        },
                        // error
                        function(response) {
                            console.error("getandsetSelectedItem did not work - " + response);
                        }
                    );

                    //$scope.setSelectedItemHeader = function (selectedItem) {
                    //    if (selectedItem.hasOwnProperty("MDB_CORE_0000000727")){
                    //        $scope.mdbPartonomyHeader = {};
                    //        $scope.mdbPartonomyHeader = selectedItem.MDB_CORE_0000000727[0];
                    //        $scope.mdbDoc[selectedItem.MDB_DATASCHEME_0000002740] = selectedItem.MDB_UIAP_0000000527;
                    //        console.info("mdbDoc - " + $scope.mdbDoc);
                    //    }
                    //};
                }

               /* MDBDocumentService.setInput($scope.mdbpartonomypage.data)
                    .then(function(items) {
                        $scope.mdbDoc = items;
                        if ($scope.mdbpartonomypage.data[0].hasOwnProperty("MDB_CORE_0000000727")){
                            console.info("$scope.mdbpartonomypage.data[0].MDB_UIAP_0000000527: " + JSON.stringify($scope.mdbpartonomypage.data[0].MDB_UIAP_0000000527));
                            $scope.mdbDoc[$scope.mdbpartonomypage.data[0].MDB_DATASCHEME_0000002740] = $scope.mdbpartonomypage.data[0].MDB_UIAP_0000000527;
                        }
                        console.info("$scope.mdbDoc: " + JSON.stringify($scope.mdbDoc));
                    });*/
            });


            // stop loading button on incoming overlay composition
            $scope.$on('MDBOverlayComposition_updated', function (event, args) {
                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("mdbPartonomyPage on MDBOverlayComposition_updated\nevent - " + event + "\nargs - " + JSON.stringify(args))
                }
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
                console.log("WSFormAutocompleteMessage:\n" + JSON.stringify(args));
                $scope.autocomplete[args.autocomplete_field] = args.autocomplete_data[0][args.autocomplete_field].autoCompleteData;
            });

            // input validation message
            $rootScope.$on('WSFormValidationMessage', function (event, args) {
                //alert("args " + JSON.stringify(args));
                $scope.validation_message = args.validation_message;
                $scope.validation_status = $scope.validation_message[0][args.validation_field].valid;
                $scope.validation_notification = $scope.validation_message[0][args.validation_field].MDB_UIAP_0000000388;

                function markValid(name, valid) {
                    if($scope.forms.mdbDoc) {
                        $scope.forms.mdbDoc.$setValidity("mdbvalid", valid);
                    }

                    $(`[name=${name}]`).removeClass(`ng-${valid ? "invalid" : "valid"}`);
                    $(`[name=${name}]`).addClass(`ng-${valid ? "valid" : "invalid"}`)
                }

                markValid(args.validation_field, $scope.validation_status === "true");

                if ($scope.validation_notification) {
                    NotificationService.showError($scope.validation_notification);
                }
            });


            // tabs
            ///////////////////////////////////////////////////////////////////////////

            $scope.onTabChange = function (index, mdbTabs, scope) {
                console.warn("onTabChange, to: " + index + "\nmdbTabs: " + JSON.stringify(mdbTabs) + "\nscope: " + scope);
            };

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////

            $scope.submit = MDBDocumentService.submit;

            $scope.submitAutocompleteSelect = MDBDocumentService.submitAutocompleteSelect;

            $scope.submitAutocompleteChange = MDBDocumentService.submitAutocompleteChange;


            // TODO: current workaround & functions
            ///////////////////////////////////////////////////////////////////////////

            $scope.remove = function (scope) {
                scope.remove();
            };

            $scope.toggle = function (scope) {
                scope.toggle();
            };

            $scope.moveLastToTheBeginning = function () {
                var a = $scope.data.pop();
                $scope.data.splice(0, 0, a);
            };


            $scope.collapseAll = function () {
                $scope.$broadcast('angular-ui-tree:collapse-all');
            };

            $scope.expandAll = function () {
                $scope.$broadcast('angular-ui-tree:expand-all');
            };

            $scope.visible = function (item) {
                return !($scope.query && $scope.query.length > 0
                && item.title.indexOf($scope.query) == -1);

            };

            $scope.setSelectedItem = function (selectedItem, scope) {

                 // update text for text annotation of former selectedItem
                $scope.updateTextAnnotation(selectedItem, $scope.selectedNode);

                $scope.selectedItem = selectedItem;
                $scope.selectedNode = selectedItem;
                $scope.selectedItemID = selectedItem.localID;
                $scope.setSelectedItemHeader(selectedItem);

                // destroy and reload olsketchtool
                window._reloadTool();

                return !($scope.selectedItem);
            };

            $scope.setSelectedItemHeader = function (selectedItem) {
                if (selectedItem.hasOwnProperty("MDB_CORE_0000000727")){
                    $scope.mdbPartonomyHeader = {};
                    $scope.mdbPartonomyHeader = selectedItem.MDB_CORE_0000000727[0];
                    $scope.mdbDoc[selectedItem.MDB_DATASCHEME_0000002740] = selectedItem.MDB_UIAP_0000000527;
                    console.info("mdbDoc - " + JSON.stringify($scope.mdbDoc));
                }
            };

            $scope.getandsetSelectedItem = function (items, selectedItemId) {

                angular.forEach(items, function(value, key) {

                    if (selectedItemId == value.localID){
                        $scope.setSelectedItem(value);
                    }
                    else if (value.hasOwnProperty("BFO_0000051")) {
                        $scope.getandsetSelectedItem(value.BFO_0000051, selectedItemId);
                    }
                });
            };

            $scope.newSubItem = function (scope) {
                var nodeData = scope.$modelValue;
                nodeData.nodes.push({
                    id: nodeData.localID * 10 + nodeData.nodes.length,
                    title: nodeData.title + ' part ' + (nodeData.nodes.length + 1),
                    instance_of: nodeData.instance_of,
                    definition: '',
                    obo_id: nodeData.obo_id,
                    URI: nodeData.URI,
                    ontology: nodeData.ontology,
                    obo_namespace: nodeData.obo_namespace,
                    nodes: []
                });
            };

            $scope.update = function (item) {
                var tree = this.tree;
                var name = item.name;
                var dataItem = tree.dataItem(tree.select());
                dataItem.title = undefined; // force refresh of dataItem
                dataItem.set("title", name);
            };

            $scope.updateInstance = function (item) {
                var tree = this.tree;
                var instance = item.instance_of;
                var dataItem = tree.dataItem(tree.select());
                dataItem.instance_of = undefined; // force refresh of dataItem
                dataItem.set("instance_of", instance);
            };

            $scope.updateNamespace = function (item) {
                var tree = this.tree;
                var obonamespace = item.obo_namespace;
                var dataItem = tree.dataItem(tree.select());
                dataItem.obo_namespace = undefined; // force refresh of dataItem
                dataItem.set("obo_namespace", obonamespace);
            };

            $scope.updateTextAnnotation = function (newnode, formernode) {

                if (formernode){

                    if (!formernode.text_annotation){

                        formernode.text_annotation = {};
                        formernode.text_annotation.text = "";
                        formernode.text_annotation.html = "";
                        formernode.text_annotation.autoannotations = [];
                        formernode.text_annotation.ontologytags = [];
                    }
                    if ($scope.selectedItem.text_annotation){
                        formernode.text_annotation.html = angular.element(document.getElementById('divtextarea')).html();
                        formernode.text_annotation.autoannotations = $scope.selectedItem.text_annotation.autoannotations;
                        formernode.text_annotation.ontologytags = $scope.ontologytags;
                    }
                }

                // clear former preferences
                $scope.setDefault();
                angular.element(document.getElementById('divtextarea')).empty();

                if (newnode){

                    if (!newnode.text_annotation){

                        newnode.text_annotation = {};
                        newnode.text_annotation.text = "";
                        newnode.text_annotation.html = "";
                        newnode.text_annotation.autoannotations = [];
                        newnode.text_annotation.ontologytags = [];
                    }
                        var html = newnode.text_annotation.html;
                        var text = newnode.text_annotation.text;

                        if(html.length > 0){
                            angular.element(document.getElementById('divtextarea')).html(html);
                        }
                        else if (text.length > 0){
                            angular.element(document.getElementById('divtextarea')).text(text);
                        }
                        // fill annotation data to grid data
                        $scope.gridOptions.data = newnode.text_annotation.autoannotations;
                        $scope.ontologytags = newnode.text_annotation.ontologytags;
                }


            };

            // formal description panels
            $scope.panelstatus = {
                isOpen: {
                    formal_des_haspart: true,
                    formal_des_measurement: true,
                    formal_des_shapecolor: true,
                    image_tools: true,
                    image_help: false
                },
                isFirstOpen: true,
                isFirstDisabled: false
            };

            $scope.updateText = function (node) {
                console.warn("update Text: " + node + "\n" + JSON.stringify(node));

                /*"annotationFormText": "",*/
            };

            // text annotation
            $scope.annotationForm = {};
            $scope.annotationForm.ontologies = "";
            $scope.annotationForm.include_views = true;
            $scope.annotationForm.display_context = false;
            $scope.annotationForm.include = "prefLabel,synonym,definition,notation,cui,semanticType,properties";
            $scope.annotationForm.longest_only = false;
            $scope.annotationForm.exclude_numbers = false;
            $scope.annotationForm.whole_word_only = true;
            $scope.annotationForm.exclude_synonyms = false;
            $scope.annotationForm.expand_class_hierarchy = false;
            $scope.annotationForm.class_hierarchy_max_level = "1";
            $scope.annotationForm.format = "json";

            $scope.divtextarea = angular.element(document.getElementById('divtextarea'));
            $scope.loadingButton = angular.element(document.getElementById('annotationLoadingButton'));

            ///////////////////////////////////////////////////////////////////////////
            // ONTOLOGY TAGS
            ///////////////////////////////////////////////////////////////////////////

            $scope.ontologytags = [];

            $scope.loadOntologyTags = function ($query) {
                return $http.get('../data/ontologies.json', {cache: true}).then(function (response) {
                    var ontology_tags = response.data;
                    return ontology_tags.filter(function (ontologytag) {
                        return ontologytag.value.toLowerCase().indexOf($query.toLowerCase()) != -1;
                    });
                });
            };

            $scope.setTestValues = function () {
                $scope.ontologytags = [
                    {
                        "value": "MESH",
                        "name": "Medical Subject Headings (MESH)"
                    },{
                        "value": "CARO",
                        "name": "Common Anatomy Reference Ontology (CARO)"
                    },{
                        "value": "MP",
                        "name": "Mammalian Phenotype Ontology (MP)"
                    }, {
                        "value": "CL",
                        "name": "Cell Ontology (CL)"
                    }, {
                        "value": "BOF",
                        "name": "Biodiversity Ontology (BOF)"
                    }, {
                        "value": "TAXRANK",
                        "name": "Taxonomic Rank Vocabulary (TAXRANK)"
                    },{
                        "value": "VTO",
                        "name": "Vertebrate Taxonomy Ontology (VTO)"
                    }];
                angular.element(document.getElementById('divtextarea')).text("The head in most insects is enclosed in a hard, heavily sclerotized, exoskeletal head capsule, or epicranium. The main exception is in those species whose larvae are not fully sclerotised, mainly some holometabola; but even most unsclerotised or weakly sclerotised larvae tend to have well sclerotised epicrania, for example the larvae of Coleoptera and Hymenoptera. The larvae of Cyclorrhapha however, tend to have hardly any head capsule at all.(Source: https://en.wikipedia.org/wiki/Insect_morphology)");
            };

            $scope.setDefault = function () {
                $scope.clearValues();
                $scope.annotationForm.whole_word_only = true;
                $scope.annotationForm.include = "prefLabel,synonym,definition,notation,cui,semanticType,properties";
            };

            $scope.clearValues = function () {
                $scope.ontologytags = [];
                angular.element(document.getElementById('divtextarea')).text("");
                $scope.annotationForm.ontologies = "";
                $scope.annotationForm.include_views = true;
                $scope.annotationForm.display_context = false;
                $scope.annotationForm.include = "";
                $scope.annotationForm.longest_only = false;
                $scope.annotationForm.exclude_numbers = false;
                $scope.annotationForm.whole_word_only = false;
                $scope.annotationForm.exclude_synonyms = false;
                $scope.annotationForm.expand_class_hierarchy = false;
                $scope.annotationForm.class_hierarchy_max_level = "1";
                $scope.annotationForm.format = "json";
            };

            $scope.setTestText = function () {
                $scope.ontologytags = [
                    {
                        "value": "MESH",
                        "name": "Medical Subject Headings (MESH)"
                    },{
                        "value": "CARO",
                        "name": "Common Anatomy Reference Ontology (CARO)"
                    },{
                        "value": "MP",
                        "name": "Mammalian Phenotype Ontology (MP)"
                    }, {
                        "value": "CL",
                        "name": "Cell Ontology (CL)"
                    }, {
                        "value": "BOF",
                        "name": "Biodiversity Ontology (BOF)"
                    }, {
                        "value": "TAXRANK",
                        "name": "Taxonomic Rank Vocabulary (TAXRANK)"
                    },{
                        "value": "VTO",
                        "name": "Vertebrate Taxonomy Ontology (VTO)"
                    }];
                angular.element(document.getElementById('divtextarea')).html('Cell division is a cellular process.');
            };

            $scope.setTestHTML = function () {
                angular.element(document.getElementById('divtextarea')).html('Cell division is a <span id="ID20TO35" class="annotatedText"><span id="ID20TO27" class="annotatedText">cellular</span> <span id="ID29TO35" class="annotatedText">process</span></span>');
            };


            ///////////////////////////////////////////////////////////////////////////
            // CHECK FOR ANNOTATIONS
            ///////////////////////////////////////////////////////////////////////////

            $scope.checkAnnotations = function () {
                var url = "https://data.bioontology.org/annotator?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb";
                var parameters = $scope.annotationForm;
                parameters.text = angular.element(document.getElementById('divtextarea')).text();
                parameters.text = parameters.text.replace(/<[^>]*>?/g, '');
                angular.element(document.getElementById('divtextarea')).text(parameters.text);
                $scope.selectedItem.text_annotation.text = parameters.text;
                $scope.config = {
                    params: parameters
                };
                angular.forEach($scope.ontologytags, function (value, key) {
                    if ($scope.annotationForm.ontologies == "") {
                        $scope.annotationForm.ontologies = value.value;
                    }
                    else {
                        $scope.annotationForm.ontologies = $scope.annotationForm.ontologies + "," + value.value;
                    }
                });
                // empty grid data
                $scope.gridOptions.data = [];
                angular.element(document.getElementById('annotationLoadingButton')).button('loading');

                $scope.content = "";
                $scope.word = 'str';

                $http.get(url, $scope.config)
                    .then(
                    function (response) {
                        // success callback

                        if (response.data.length == 0){
                            alert("Sorry, no matches found.");
                            // stop loading button
                            angular.element(document.getElementById('annotationLoadingButton')).button('reset');
                        }
                        else {

                            $scope.autoannotations = [];
                            $scope.distinctannotationlist = [];
                            $scope.distinctannotationrangeslist = [];
                            $scope.overlappingrangeslist = [];

                            // index
                            var i = 0;
                            // for each annotation
                            angular.forEach(response.data, function (value, key) {
                                var prefLabel = value.annotatedClass.prefLabel;
                                var annoDetails = value;

                                // for each range per annotation
                                angular.forEach(value.annotations, function (value, key) {

                                    /////----- extract annotation data and send to grid -----/////
                                    var rangeID = "ID" + value.from + "TO" + value.to;
                                    var details = "";
                                    if (!(annoDetails.annotatedClass.definition == null)) {
                                        details = annoDetails.annotatedClass.definition[0];
                                    }
                                    var ont = annoDetails.annotatedClass.links.ontology;
                                    var ontsplit = ont.split("/");
                                    var ontabbr = ontsplit[ontsplit.length - 1];
                                    $scope.autoannotations.push({
                                        "temp_id": i,
                                        "from": value.from,
                                        "to": value.to,
                                        "range_id": rangeID,
                                        "matchType": value.matchType,
                                        "text": value.text,
                                        "quote": value.text,
                                        "prefLabel": prefLabel,
                                        "class_id": annoDetails.annotatedClass["@id"],
                                        "ontology": annoDetails.annotatedClass.links.ontology,
                                        "ontologyname": ontabbr,
                                        "type": annoDetails.annotatedClass["@type"],
                                        "definition": details,
                                        "details": annoDetails
                                    });

                                    i = i + 1;

                                    ///// extract distinct ranges //////////////
                                    if (!(_.includes($scope.distinctannotationrangeslist, rangeID))) {
                                        $scope.distinctannotationrangeslist.push(rangeID);
                                        $scope.distinctannotationlist.push({
                                            "from": value.from,
                                            "to": value.to,
                                            "html_id": rangeID,
                                            "quote": value.text
                                        });
                                    }

                                });

                            });

                            // sort annotation data according to occurrence a-z
                            $scope.autoannotations = _.sortBy($scope.autoannotations, "from");
                            // fill annotation data to grid data
                            $scope.gridOptions.data = $scope.autoannotations;
                            // stop loading button
                            angular.element(document.getElementById('annotationLoadingButton')).button('reset');

                            // sort Annotation list "to" a-z, "from" z-a
                            $scope.distinctannotationlist.sort(function (x, y) { return x.to - y.to || y.from - x.from; });
                            // reverse list
                            $scope.distinctannotationlist = $scope.distinctannotationlist.reverse();

                            angular.forEach($scope.distinctannotationlist, function (value, key) {

                                if ($scope.overlappingrangeslist.indexOf(value.html_id) <= -1){
                                    $scope.content = angular.element(document.getElementById('divtextarea')).html();
                                    $scope.content = $scope.content.replace(/&nbsp;/gi," ");

                                    $scope.sub1 = $scope.content.substring(0, value.from - 1);
                                    $scope.sub2 = $scope.content.substring(value.from - 1, value.to);
                                    $scope.sub3 = $scope.content.substring(value.to);

                                    //alert("$scope.sub " + value.from + " " + value.to + " " + value.html_id + " " + value.quote +  "\n" + $scope.sub1 + "\n text - " + $scope.sub2 + "\n" + $scope.sub3);

                                    if ($scope.sub2.indexOf(" ") > -1) {
                                        $scope.wordlist = $scope.sub2.split(" ");
                                        $scope.wordstart = value.from;
                                        $scope.subsubArray = [];
                                        $scope.subsub2 = "";

                                        angular.forEach($scope.wordlist, function (word, key) {
                                            var from = $scope.wordstart;
                                            var to = parseInt($scope.wordstart) + parseInt(word.length) -1;
                                            var tempid = "ID" + from + "TO" + to;
                                            //alert("word: " + word  + "\nword.length: " + word.length + "\nword.from: " + from + "\nword.to: " + to);
                                            $scope.subsubArray.push("<span id='" + tempid + "' class='annotatedText'>" + word + "</span>");
                                            $scope.wordstart = to + 2;
                                            $scope.overlappingrangeslist.push(tempid);
                                        });

                                        angular.forEach($scope.subsubArray, function (span, key) {

                                            if ($scope.subsub2.length == 0){
                                                $scope.subsub2 =  span;
                                            }
                                            else{
                                                $scope.subsub2 =  $scope.subsub2 + " " + span;
                                            }
                                        });

                                        $scope.sub2 = $scope.subsub2;
                                    }

                                    $scope.word = !!$scope.sub2 ? $scope.sub2 : $scope.word;
                                    $scope.sub2 = "<span id='" + value.html_id + "' class='annotatedText'>" + $scope.sub2 + "</span>";
                                    $scope.overlappingrangeslist.push(value.html_id);

                                    $scope.subsubsub = $scope.sub1 + $scope.sub2 + $scope.sub3;

                                    angular.element(document.getElementById('divtextarea')).html($scope.subsubsub);
                                }
                                else {
                                    //alert("$scope.overlappingrangeslist " + JSON.stringify($scope.overlappingrangeslist));
                                }
                            });

                            $scope.selectedItem.text_annotation.autoannotations.html =
                                $scope.selectedItem.text_annotation.autoannotations = $scope.autoannotations;

                        }
                    },
                    function (response) {
                        // failure call back
                        alert("Sorry, something went wrong. Please reload page and try again."); // : " + response.data.errors[0]);

                        // stop loading button
                        angular.element(document.getElementById('annotationLoadingButton')).button('reset');
                    }
                )
                    .then(function () {
                        //necessary for grid to draw correctly
                        $timeout(function () {
                            $scope.gridApi.core.handleWindowResize();
                        }, 250);
                    });

            };

            ///////////////////////////////////////////////////////////////////////////
            // UI GRID
            ///////////////////////////////////////////////////////////////////////////

            $scope.lastSelectedRow = {};
            $scope.info = {};

            $scope.gridOptions = {
                enableRowSelection: true,
                enableFullRowSelection: true,
                enableSelectAll: false,
                enableGridMenu: true,
                selectionRowHeaderWidth: 0,
                rowHeight: 30,
                showGridFooter: false,
                multiSelect: false,
                showGroupPanel: true,
                enableCellSelection: true,
                enablePaging: true,
                columnDefs: [
                    {
                        name: 'text',
                        displayName: 'Annotated Text',
                        //width: '200',
                        enableColumnResizing: true
                    }
                    , {
                        name: 'class_id',
                        displayName: 'Annotated Class',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><a href="{{COL_FIELD}}" target="_blank">{{row.entity.prefLabel}}</a></div>'
                    }, {
                        name: 'ontologyname',
                        displayName: 'Ontology',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><a href="{{row.entity.ontology}}" target="_blank">{{COL_FIELD}}</a></div>'
                    }
                ]
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    var msg = 'row selected ' + row.isSelected;
                    console.log(msg);

                    if (row.isSelected) {
                        //"temp_id": i,
                        //"from": value.from,
                        //"to": value.to,
                        //"range_id": rangeID,
                        //"matchType": value.matchType,
                        //"text": value.text,
                        //"quote": value.text,
                        //"prefLabel": prefLabel,
                        //"class_id": annoDetails.annotatedClass["@id"],
                        //"ontology": annoDetails.annotatedClass.links.ontology,
                        //"type": annoDetails.annotatedClass["@type"],
                        //"definition": annoDetails.annotatedClass.definition[0],
                        //"details": annoDetails
                        if ($scope.lastSelectedRow) {
                            angular.element(document.getElementById($scope.lastSelectedRow.range_id)).removeClass('highlightedText');
                        }
                        angular.element(document.getElementById(row.entity.range_id)).addClass('highlightedText');
                        $scope.lastSelectedRow = row.entity;
                        //if (!row.entity.definition)
                        //    $scope.lastSelectedRow.definition = "";
                    }
                    else {
                        angular.element(document.getElementById(row.entity.range_id)).removeClass('highlightedText');
                    }

                });
            };

        }
    }
});


// directive for the MDB search overlay
// usage:
/** <mdb-search-overlay></mdb-search-overlay> */
MDBDirectives.directive('mdbSearchOverlay', function (RecursionHelper, $rootScope, $compile, $location,  $timeout, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        template:
        `<div id="mdbentrylist" class="mdb_document container-fluid">"
           <form name="forms.mdbDoc" id={{mdbentrylist.load_page_localID}} class="formular" autocomplete="off" novalidate style="height: 100%;">
               <div class="mdb-entry-list-panel">
                 <button class="btn btn-default" ng-click="showMDBEntryList()" title="">Show All Entries</button> &nbsp;&nbsp;
                 <button class="btn btn-default" ng-click="showMDBSEntryList()" title="">Show Specimen Entries</button> &nbsp;&nbsp;
                 <button class="btn btn-default" ng-click="showMDBMDEntryList()" title="">Show Morphological Description Entries</button> &nbsp;&nbsp;
                 <br><br>
                 <div class="grid mdb-entry-list-grid" ui-grid="gridOptions"
                       ui-grid-selection ui-grid-auto-resize ui-grid-resize-columns ui-grid-move-columns>
                 </div>
                 <div style="height: 300px;" ng-show="developer">
                    <div class="col-sm-6" style="padding-left: 0; padding-right: 5px;">
                       <pre >WsService.sent[0] = {{WsService.sent[0] | json}}</pre>
                    </div>
                    <div class="col-sm-6" style="padding-left: 5px; padding-right: 0;">
                       <pre >WsService.response[0] = {{WsService.response[0] | json}}</pre>
                    </div>
                 </div>
               </div>
           </form>
        </div>`
        ,
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope, $rootScope) {
            $scope.forms = {};
            $scope.mdbformid = '';
            $scope.mdbentrylist = {};
            $scope.mdbentrylist_items = {};
            $scope.loading = true;
            $scope.WsService = WsService;
            $scope.currentUser = AuthService.getUsername();

            // grid
            $scope.lastSelectedRow = {};
            $scope.info = {};

            $scope.gridOptions = {
                multiSelect: false,
                enableSelectAll: false,
                showGridFooter: true,
                enableGridMenu: true,
                selectionRowHeaderWidth: 0,
                rowHeight: 40,
                minimumColumnSize: 70,
                showGroupPanel: true,
                enablePaging: true,
                columnDefs: [
                    {
                        name: 'MDB_CORE_0000000602',
                        displayName: 'Type',
                        width: '70',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents ui-grid-cell-contents-centered"><img class=\"hasType\" src=\"/img/tmp/{{row.entity.MDB_CORE_0000000602}}.svg\"></div>'
                    },{
                        name: 'withStatus',
                        displayName: 'Status',
                        width: '80',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents ui-grid-cell-contents-centered {{row.entity.bgcolor}}"><img class=\"hasStatus\" src=\"/img/tmp/{{row.entity.withStatus}}.svg\"></div>'
                    },{
                        name: 'entryLabel',
                        displayName: 'Label',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div>{{row.entity.entryLabel}} {{row.entity.entryLabel2}}</div></div>'
                    },{
                        name: 'MDB_GUI_0000000288',
                        displayName: 'Creator',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div ng-show="row.entity.MDB_GUI_0000000088.length">{{row.entity.MDB_GUI_0000000088}}, {{row.entity.MDB_GUI_0000000288}}</div></div>'
                    },{
                        name: 'uri',
                        displayName: '',
                        //width: '500',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><a href="{{row.entity.uri}}"><i class="css-icon-crosslink-icon"></i>  show entry</a></div>'
                    }
                ],
                data: []
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                /*gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                 var msg = 'row selected ' + row.isSelected;
                 console.log(msg);

                 if (row.isSelected) {
                 //"temp_id": i,
                 //"from": value.from,
                 //"to": value.to,
                 //"range_id": rangeID,
                 //"matchType": value.matchType,
                 //"text": value.text,
                 //"quote": value.text,
                 //"prefLabel": prefLabel,
                 //"class_id": annoDetails.annotatedClass["@id"],
                 //"ontology": annoDetails.annotatedClass.links.ontology,
                 //"type": annoDetails.annotatedClass["@type"],
                 //"definition": annoDetails.annotatedClass.definition[0],
                 //"details": annoDetails
                 if ($scope.lastSelectedRow) {
                 angular.element(document.getElementById($scope.lastSelectedRow.range_id)).removeClass('highlightedText');
                 }
                 angular.element(document.getElementById(row.entity.range_id)).addClass('highlightedText');
                 $scope.lastSelectedRow = row.entity;
                 //if (!row.entity.definition)
                 //    $scope.lastSelectedRow.definition = "";
                 }
                 else {
                 angular.element(document.getElementById(row.entity.range_id)).removeClass('highlightedText');
                 }

                 });*/
            };

            $scope.setGridData = function(griddata){

                // TODO: change back if possible: https://www.morphdbase.de/ <=> https://proto.morphdbase.de/
                angular.forEach($scope.mdbentrylist_items, function (val, key) {
                    let value = val.uri;
                    $scope.mdbentrylist_items[key].uri = value.replace('www', 'proto');

                    // default color
                    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-user-entry";

                    // current draft
                    if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000115"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-current-draft";
                    }
                    // current published
                    else if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000116"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-current-published";
                    }
                    //// trash icon
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-deleted-draft";
                    //}
                    // previously published
                    else if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000484"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-previously-published";
                    }
                    //// recycle bin icon
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-bin-draft";
                    //}
                    //// saved draft
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-saved-draft";
                    //}


                });


                // fill data to grid
                $scope.gridOptions.data = griddata;
                //necessary for grid to draw correctly
                /*$timeout(function () {
                 $scope.gridApi.core.handleWindowResize();
                 }, 250);*/

            };

            // TODO: workaround for websocket pause
            //$scope.mdbentrylist = [
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "First Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Klaus",
            //        "MDB_GUI_0000000088": "Meyer",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Second Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Liselotte",
            //        "MDB_GUI_0000000088": "Müller",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Third Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Manfred",
            //        "MDB_GUI_0000000088": "Mustermann",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Auch ein Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Thursday",
            //        "MDB_GUI_0000000088": "Next",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Entry 6",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Otto",
            //        "MDB_GUI_0000000088": "Müller",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Klaus Maiers new Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Klaus",
            //        "MDB_GUI_0000000088": "Maier",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Title changed",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171018-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "SpecimenEntryTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "3",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171027-s-3-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Der Sinn des Lebens ist",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "42",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-4-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Specimen Description",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "this is a new title",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171019-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "fdgd",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-3-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "wqeqe",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171020-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "test",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "1701",
            //        "uri": "http://www.morphdbase.de/resource/272b7f79-20171023-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "2CreateOn",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "27102017",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-3-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Label was changed afterwards",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Klaus",
            //        "MDB_GUI_0000000088": "Maier",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "test",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "1",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171025-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "A short and meaningful title",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/272b7f79-20171023-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "wq",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "1teston",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "27102017",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "SpecimenEntryTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "3",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171027-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "specimenEntryTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "2",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171026-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Sample Specimen",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "2310",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "sad",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171020-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Lycoris aurea entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/272b7f79-20171018-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "specimenTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171027-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Multicellular Organism",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/421701d8-20171020-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Klaus Maiers new Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Klaus",
            //        "MDB_GUI_0000000088": "Maier",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "3CreateOn",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "27102017",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-4-d_1_1"
            //    }
            //];
            //$scope.mdbentrylist_items = [
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Title changed",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171018-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "SpecimenEntryTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "3",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171027-s-3-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Der Sinn des Lebens ist",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "42",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-4-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Specimen Description",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "this is a new title",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171019-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "fdgd",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-3-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "wqeqe",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171020-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "test",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "1701",
            //        "uri": "http://www.morphdbase.de/resource/272b7f79-20171023-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "2CreateOn",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "27102017",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-3-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Label was changed afterwards",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Klaus",
            //        "MDB_GUI_0000000088": "Maier",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "test",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "1",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171025-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "A short and meaningful title",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/272b7f79-20171023-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "wq",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "1teston",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "27102017",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "SpecimenEntryTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "3",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171027-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "specimenEntryTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "2",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171026-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Sample Specimen",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "2310",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171023-s-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "sad",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "12",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171020-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Lycoris aurea entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/272b7f79-20171018-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "specimenTest",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "uri": "http://www.morphdbase.de/resource/a3d611c8-20171027-s-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Multicellular Organism",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "uri": "http://www.morphdbase.de/resource/421701d8-20171020-md-1-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "Klaus Maiers new Entry",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000611",
            //        "MDB_GUI_0000000288": "Klaus",
            //        "MDB_GUI_0000000088": "Maier",
            //        "uri": "http://www.morphdbase.de/resource/caab6b18-20171019-md-2-d_1_1"
            //    },
            //    {
            //        "withStatus": "MDB_CORE_0000000115",
            //        "entryLabel": "3CreateOn",
            //        "MDB_CORE_0000000602": "MDB_CORE_0000000613",
            //        "entryLabel2": "27102017",
            //        "uri": "http://www.morphdbase.de/resource/de46dd64-20171027-s-4-d_1_1"
            //    }
            //];
            //$scope.setGridData($scope.mdbentrylist_items);

            // websocket events
            ///////////////////////////////////////////////////////////////////////////

            //$rootScope.$emit('WSMDBEntryListMessage', {
            //    socket_message: message,
            //    socket_message_type: 'mdb_entry_list',
            //    composition_id: message.load_page_localID,
            //    composition_items: message.data,
            //    mdbueid_uri: message.mdbueid_uri
            //});

            // update $scope.mdbentrylist
            $rootScope.$on('WSMDBEntryListMessage', function (event, args) {
                console.info("directive: on WSMDBEntryListMessage");

                $scope.mdbentrylist = args.socket_message;
                $scope.mdbentrylist_items = args.composition_items;
                $scope.mdbformid = args.composition_id;

                $scope.setGridData($scope.mdbentrylist_items);

                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("MDBEntryList on WSMDBEntryListMessage\nmdbentrylist_items - " + JSON.stringify($scope.mdbentrylist_items))
                }

                $scope.loading = false;
                $scope.nopage = false;
            });

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////
            $scope.submit = MDBDocumentService.submit;

            $scope.showMDBEntryList = function(){
                $location.path('mdb_entry_list/all');
                //WsService.sendShowEntryListToWebsocket("all");
            };

            $scope.showMDBSEntryList = function(){
                $location.path('mdb_entry_list/s');
                //WsService.sendShowEntryListToWebsocket("s");
            };

            $scope.showMDBMDEntryList = function(){
                $location.path('mdb_entry_list/md');
                //WsService.sendShowEntryListToWebsocket("md");
            };
        }
    }
});


// directive for MDB entry lists
// usage:
/**<mdb-entry-list></mdb-entry-list>*/
MDBDirectives.directive('mdbEntryList', function (RecursionHelper, $rootScope, $location, $compile, $timeout, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        template:
        '<div id="mdbentrylist" class=\"mdb_document container-fluid\">' +

        '   <form name=\"forms.mdbDoc\" id={{mdbentrylist.load_page_localID}} class=\"formular\" autocomplete="off" novalidate style=\"height: 100%;\">                      ' +

        '       <div class=\"mdb-entry-list-panel\">' +

        '           <div class=\"grid mdb-entry-list-grid\" ui-grid=\"gridOptions\"                                             ' +
        '               ui-grid-selection ui-grid-pagination ui-grid-auto-resize ui-grid-resize-columns ui-grid-move-columns>  ' +
        '           </div>                                                                                  ' +

        '           <div style=\"height: 300px;\" ng-show=\"developer\">                                               ' +
        '               <div class="col-sm-6" style="padding-left: 0; padding-right: 5px;">                                                  ' +
        '                   <pre >WsService.sent[0] = {{WsService.sent[0] | json}}</pre>                    ' +
        '               </div>                                                                              ' +
        '               <div class="col-sm-6" style="padding-left: 5px; padding-right: 0;">                                                  ' +
        '                   <pre >WsService.response[0] = {{WsService.response[0] | json}}</pre>            ' +
        '               </div>                                                                              ' +
        '           </div>                                                                                  ' +


        '       </div>    ' +

        '   </form>    ' +

        '</div>'
        ,
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope, $rootScope) {
            $scope.forms = {};
            $scope.mdbformid = '';
            $scope.mdbentrylist = {};
            $scope.mdbentrylist_items = {};
            $scope.loading = true;
            $scope.WsService = WsService;
            $scope.currentUser = AuthService.getUsername();

            // grid
            $scope.lastSelectedRow = {};
            $scope.info = {};

            /*$scope.gridOptions = {
                multiSelect: false,
                enableSelectAll: false,
                showGridFooter: true,
                enableGridMenu: true,
                selectionRowHeaderWidth: 0,
                rowHeight: 40,
                minimumColumnSize: 70,
                showGroupPanel: true,
                enablePaging: true,
                columnDefs: [
                    {
                        name: 'MDB_CORE_0000000602',
                        displayName: 'Type',
                        width: '70',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents ui-grid-cell-contents-centered"><img class=\"hasType\" src=\"/img/tmp/{{row.entity.MDB_CORE_0000000602}}.svg\"></div>'
                    },{
                        name: 'withStatus',
                        displayName: 'Status',
                        width: '80',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents ui-grid-cell-contents-centered {{row.entity.bgcolor}}"><img class=\"hasStatus\" src=\"/img/tmp/{{row.entity.withStatus}}.svg\"></div>'
                    },{
                        name: 'entryLabel',
                        displayName: 'Label',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div>{{row.entity.entryLabel}} {{row.entity.entryLabel2}}</div></div>'
                    },{
                        name: 'MDB_GUI_0000000288',
                        displayName: 'Creator',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div ng-show="row.entity.MDB_GUI_0000000088.length">{{row.entity.MDB_GUI_0000000088}}, {{row.entity.MDB_GUI_0000000288}}</div></div>'
                    },{
                        name: 'uri',
                        displayName: '',
                        //width: '500',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><a href="{{row.entity.uri}}"><i class="css-icon-crosslink-icon"></i>  show entry</a></div>'
                    }
                ],
                data: []
            };*/
            $scope.gridOptions = {
                //multiSelect: false,
                //enableSelectAll: false,
                //showGridFooter: true,
                enableGridMenu: true,
                selectionRowHeaderWidth: 0,
                rowHeight: 40,
                minimumColumnSize: 70,
                showGroupPanel: true,
                columnDefs: [
                    {
                        name: 'MDB_CORE_0000000602',
                        displayName: 'Type',
                        width: '70',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents ui-grid-cell-contents-centered"><img class=\"hasType\" src=\"/img/tmp/{{row.entity.MDB_CORE_0000000602}}.svg\"></div>'
                    },{
                        name: 'withStatus',
                        displayName: 'Status',
                        width: '80',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents ui-grid-cell-contents-centered {{row.entity.bgcolor}}"><img class=\"hasStatus\" src=\"/img/tmp/{{row.entity.withStatus}}.svg\" title=\"TODO: add tooltip with status info\"></div>'
                    },{
                        name: 'entryLabel',
                        displayName: 'Label',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div>{{row.entity.entryLabel}} {{row.entity.entryLabel2}}</div></div>'
                    },{
                        name: 'MDB_GUI_0000000288',
                        displayName: 'Creator',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div ng-show="row.entity.MDB_GUI_0000000088.length">{{row.entity.MDB_GUI_0000000088}}, {{row.entity.MDB_GUI_0000000288}}</div></div>'
                    },{
                        name: 'createdOn',
                        displayName: 'Created/Published on',
                        //width: '200',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><div ng-show="row.entity.MDB_GUI_0000000088.length">{{row.entity.createdON}}</div></div>'
                    },{
                        name: 'uri',
                        displayName: '',
                        //width: '500',
                        enableColumnResizing: true,
                        cellTemplate: '<div class="ui-grid-cell-contents"><a href="{{row.entity.uri}}"><i class="css-icon-crosslink-icon"></i>  show entry</a></div>'
                    }
                ],
                enablePagination: true,
                paginationPageSizes: [20, 50, 100, 200, 500, 1000],
                paginationPageSize: 20,
                paginationCurrentPage: 1,
                data: []
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                /*gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                 var msg = 'row selected ' + row.isSelected;
                 console.log(msg);

                 if (row.isSelected) {
                 //"temp_id": i,
                 //"from": value.from,
                 //"to": value.to,
                 //"range_id": rangeID,
                 //"matchType": value.matchType,
                 //"text": value.text,
                 //"quote": value.text,
                 //"prefLabel": prefLabel,
                 //"class_id": annoDetails.annotatedClass["@id"],
                 //"ontology": annoDetails.annotatedClass.links.ontology,
                 //"type": annoDetails.annotatedClass["@type"],
                 //"definition": annoDetails.annotatedClass.definition[0],
                 //"details": annoDetails
                 if ($scope.lastSelectedRow) {
                 angular.element(document.getElementById($scope.lastSelectedRow.range_id)).removeClass('highlightedText');
                 }
                 angular.element(document.getElementById(row.entity.range_id)).addClass('highlightedText');
                 $scope.lastSelectedRow = row.entity;
                 //if (!row.entity.definition)
                 //    $scope.lastSelectedRow.definition = "";
                 }
                 else {
                 angular.element(document.getElementById(row.entity.range_id)).removeClass('highlightedText');
                 }

                 });*/
            };

            $scope.setGridData = function(griddata){

                // TODO: change back if possible: https://www.morphdbase.de/ <=> https://proto.morphdbase.de/
                angular.forEach($scope.mdbentrylist_items, function (val, key) {
                    let value = val.uri;
                    $scope.mdbentrylist_items[key].uri = value.replace('www', 'proto');

                    // default color
                    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-user-entry";

                    // current draft
                    if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000115"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-current-draft";
                    }
                    // current published
                    else if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000116"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-current-published";
                    }
                    //// trash icon
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-deleted-draft";
                    //}
                    // previously published
                    else if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000484"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-previously-published";
                    }
                    //// recycle bin icon
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-bin-draft";
                    //}
                    //// saved draft
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-saved-draft";
                    //}


                });


                // fill data to grid
                $scope.gridOptions.data = griddata;
                //necessary for grid to draw correctly
                /*$timeout(function () {
                 $scope.gridApi.core.handleWindowResize();
                 }, 250);*/

            };


            // websocket events
            ///////////////////////////////////////////////////////////////////////////

            //$rootScope.$emit('WSMDBEntryListMessage', {
            //    socket_message: message,
            //    socket_message_type: 'mdb_entry_list',
            //    composition_id: message.load_page_localID,
            //    composition_items: message.data,
            //    mdbueid_uri: message.mdbueid_uri
            //});

            // update $scope.mdbentrylist
            $rootScope.$on('WSMDBEntryListMessage', function (event, args) {
                console.info("directive: on WSMDBEntryListMessage");

                $scope.mdbentrylist = args.socket_message;
                $scope.mdbentrylist_items = args.composition_items;
                $scope.mdbformid = args.composition_id;

                $scope.setGridData($scope.mdbentrylist_items);

                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("MDBEntryList on WSMDBEntryListMessage\nmdbentrylist_items - " + JSON.stringify($scope.mdbentrylist_items))
                }

                $scope.loading = false;
                $scope.nopage = false;
            });

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////
            $scope.submit = MDBDocumentService.submit;

            $scope.showMDBEntryList = function(){
                $location.path('mdb_entry_list/all');
                //WsService.sendShowEntryListToWebsocket("all");
            };

            $scope.showMDBSEntryList = function(){
                $location.path('mdb_entry_list/s');
                //WsService.sendShowEntryListToWebsocket("s");
            };

            $scope.showMDBMDEntryList = function(){
                $location.path('mdb_entry_list/md');
                //WsService.sendShowEntryListToWebsocket("md");
            };
        }
    }
});

// directive for an MDB entry list page
// usage:
/**<mdb-entry-list-page></mdb-entry-list-page>*/
MDBDirectives.directive('mdbEntryListPage', function (RecursionHelper, $rootScope, $location, $compile, $timeout, WsService, AuthService, NotificationService, MDBDocumentService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        template:
        `<div id="mdbentrylist" class="mdb_document container-fluid">
           <form name="forms.mdbDoc" id={{mdbentrylist.load_page_localID}} class="formular" autocomplete="off" novalidate style="height: 100%;">
              <div class="mdb-entry-list-page-panel">
                <button class="btn btn-default" ng-click="showMDBEntryList()" title="">Show All Entries</button> &nbsp;&nbsp;
                <button class="btn btn-default" ng-click="showMDBSEntryList()" title="">Show Specimen Entries</button> &nbsp;&nbsp;
                <button class="btn btn-default" ng-click="showMDBMDEntryList()" title="">Show Morphological Description Entries</button> &nbsp;&nbsp;
                <br><br>
                   <div class="grid mdb-entry-list-grid" ui-grid="gridOptions"
                        ui-grid-selection ui-grid-auto-resize ui-grid-resize-columns ui-grid-move-columns>
                   </div>

                   <div style="height: 300px;" ng-show="developer">
                       <div class="col-sm-6" style="padding-left: 0; padding-right: 5px;">
                           <pre >WsService.sent[0] = {{WsService.sent[0] | json}}</pre>
                       </div>
                       <div class="col-sm-6" style="padding-left: 5px; padding-right: 0;">
                           <pre >WsService.response[0] = {{WsService.response[0] | json}}</pre>
                       </div>
                   </div>
              </div>
           </form>
        </div>`
        ,
        compile: function (element) {
            return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
            });
        },
        controller: function ($scope, $rootScope) {
            $scope.forms = {};
            $scope.mdbformid = '';
            $scope.mdbentrylist = {};
            $scope.mdbentrylist_items = {};
            $scope.loading = true;
            $scope.WsService = WsService;
            $scope.currentUser = AuthService.getUsername();

            // grid
            $scope.lastSelectedRow = {};
            $scope.info = {};

            $scope.gridOptions = {
                multiSelect: false,
                enableSelectAll: false,
                showGridFooter: true,
                enableGridMenu: true,
                selectionRowHeaderWidth: 0,
                rowHeight: 40,
                minimumColumnSize: 70,
                showGroupPanel: true,
                enablePaging: true,
                columnDefs: [
                    {
                        name: 'MDB_CORE_0000000602',
                        displayName: 'Type',
                        width: '70',
                        enableColumnResizing: true,
                        cellTemplate: `<div class="ui-grid-cell-contents ui-grid-cell-contents-centered">
                                            <img class="hasType" src="/img/tmp/{{row.entity.MDB_CORE_0000000602}}.svg"
                                                 title="{{row.entity.entryType}}">

                                       </div>`
                    },{
                        name: 'withStatus',
                        displayName: 'Status',
                        width: '80',
                        enableColumnResizing: true,
                        cellTemplate: `<div class="ui-grid-cell-contents ui-grid-cell-contents-centered {{row.entity.bgcolor}}">
                                            <img class="hasStatus\" src="/img/tmp/{{row.entity.withStatus}}.svg"
                                                 title="{{row.entity.status}}">
                                       </div>`
                    },{
                        name: `entryLabel`,
                        displayName: `Label`,
                        //width: `200`,
                        enableColumnResizing: true,
                        cellTemplate: `<div class="ui-grid-cell-contents">
                                           <div>{{row.entity.entryLabel}} {{row.entity.entryLabel2}}</div>
                                       </div>`
                    },{
                        name: `MDB_GUI_0000000288`,
                        displayName: `Creator`,
                        //width: `200`,
                        enableColumnResizing: true,
                        cellTemplate: `<div class="ui-grid-cell-contents">
                                            <div ng-show="row.entity.MDB_GUI_0000000088.length">{{row.entity.MDB_GUI_0000000088}}, {{row.entity.MDB_GUI_0000000288}}</div>
                                       </div>`
                    },{
                        name: `createdOn`,
                        displayName: `Created/Published on`,
                        //width: `200`,
                        enableColumnResizing: true,
                        cellTemplate: `<div class="ui-grid-cell-contents">
                                            <div ng-show="row.entity.MDB_GUI_0000000088.length">{{row.entity.createdOn}}</div>
                                       </div>`
                    },{
                        name: `uri`,
                        displayName: ``,
                        //width: `500`,
                        enableColumnResizing: true,
                        cellTemplate: `<div class="ui-grid-cell-contents">
                                            <a href="{{row.entity.uri}}">
                                                <i class="css-icon-crosslink-icon"></i>  show entry
                                            </a>
                                        </div>`
                    }
                ],
                data: []
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                /*gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                 var msg = 'row selected ' + row.isSelected;
                 console.log(msg);

                 if (row.isSelected) {
                 //"temp_id": i,
                 //"from": value.from,
                 //"to": value.to,
                 //"range_id": rangeID,
                 //"matchType": value.matchType,
                 //"text": value.text,
                 //"quote": value.text,
                 //"prefLabel": prefLabel,
                 //"class_id": annoDetails.annotatedClass["@id"],
                 //"ontology": annoDetails.annotatedClass.links.ontology,
                 //"type": annoDetails.annotatedClass["@type"],
                 //"definition": annoDetails.annotatedClass.definition[0],
                 //"details": annoDetails
                 if ($scope.lastSelectedRow) {
                 angular.element(document.getElementById($scope.lastSelectedRow.range_id)).removeClass('highlightedText');
                 }
                 angular.element(document.getElementById(row.entity.range_id)).addClass('highlightedText');
                 $scope.lastSelectedRow = row.entity;
                 //if (!row.entity.definition)
                 //    $scope.lastSelectedRow.definition = "";
                 }
                 else {
                 angular.element(document.getElementById(row.entity.range_id)).removeClass('highlightedText');
                 }

                 });*/
            };

            $scope.setGridData = function(griddata){

                // TODO: change back if possible: https://www.morphdbase.de/ <=> https://proto.morphdbase.de/
                angular.forEach($scope.mdbentrylist_items, function (val, key) {
                    let value = val.uri;
                    $scope.mdbentrylist_items[key].uri = value.replace('www', 'proto');

                    // default color
                    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-user-entry";

                    // current draft
                    if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000115"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-current-draft";
                        $scope.mdbentrylist_items[key].status = "current draft version";
                    }
                    // current published
                    else if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000116"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-current-published";
                        $scope.mdbentrylist_items[key].status = "current published version";
                    }
                    //// trash icon
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-deleted-draft";
                    //}
                    // previously published
                    else if ($scope.mdbentrylist_items[key].withStatus == "MDB_CORE_0000000484"){
                        $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-previously-published";
                        $scope.mdbentrylist_items[key].status = "previously published version";
                    }
                    //// recycle bin icon
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-bin-draft";
                    //}
                    //// saved draft
                    //else if ($scope.mdbentrylist_items[key].withStatus == ""){
                    //    $scope.mdbentrylist_items[key].bgcolor = "css-bg-color-saved-draft";
                    //}

                    // Tooltip for entry type icons.
                    // Todo: Info should come from ontology:

                    if ($scope.mdbentrylist_items[key].MDB_CORE_0000000602 == 'MDB_CORE_0000000611') {
                        $scope.mdbentrylist_items[key].entryType = 'Morphological Description Entry';
                    }
                    else if ($scope.mdbentrylist_items[key].MDB_CORE_0000000602 == 'MDB_CORE_0000000613') {
                        $scope.mdbentrylist_items[key].entryType = 'Specimen Entry';
                    }
                });


                // fill data to grid
                $scope.gridOptions.data = griddata;
                //necessary for grid to draw correctly
                /*$timeout(function () {
                 $scope.gridApi.core.handleWindowResize();
                 }, 250);*/

            };


            // update $scope.mdbentrylist
            $rootScope.$on('WSMDBEntryListMessage', function (event, args) {
                console.info("directive: on WSMDBEntryListMessage");

                $scope.mdbentrylist = args.socket_message;
                $scope.mdbentrylist_items = args.composition_items;
                $scope.mdbformid = args.composition_id;

                $scope.setGridData($scope.mdbentrylist_items);

                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){
                    console.log("MDBEntryList on WSMDBEntryListMessage\nmdbentrylist_items - " + JSON.stringify($scope.mdbentrylist_items))
                }

                $scope.loading = false;
                $scope.nopage = false;
            });

            // submit on blur
            ///////////////////////////////////////////////////////////////////////////
            $scope.submit = MDBDocumentService.submit;

            $scope.showMDBEntryList = function(){
                $location.path('mdb_entry_list/all');
                //WsService.sendShowEntryListToWebsocket("all");
            };

            $scope.showMDBSEntryList = function(){
                $location.path('mdb_entry_list/s');
                //WsService.sendShowEntryListToWebsocket("s");
            };

            $scope.showMDBMDEntryList = function(){
                $location.path('mdb_entry_list/md');
                //WsService.sendShowEntryListToWebsocket("md");
            };
        }
    }
});






////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// MDB Components /////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
 // directive for MDB document form items
 // called from within mdbPage, mdbPartonomyPage and mdbOverlay and soon as well by mdbTestPage and mdbTestOverlay
 // usage:
 */
/** <mdb-document-form-items items='json.data[0]'></mdb-document-form-items> */
MDBDirectives.directive('mdbDocumentFormItems', function () {
    return {
        restrict: 'E',
        scope: {
            items: '=items',
            mdbformid: '=mdbformid'
        },
        require: '^mdbPage, ^mdbOverlay, ^mdbTestPage, ^mdbTestOverlay',
        template: (
            `<div ng-repeat=\"item in items.MDB_GUI_0000000040\" ng-class=\"{\'new_row\' : item.MDB_UIAP_0000000399}\">
                <mdb-component item="item" mdbformid="mdbformid" x-item-id={{item.localID}} x-item-type={{item.MDB_UIAP_0000000193}} />
            </div>`
        ),
        controller: function($scope, $attrs) {

            if($scope.loading) {
                // TODO: remove this later
                if ($scope.$root.developers.indexOf($scope.$root.CurrentUser) > -1) console.log("watching", $scope, "for items...");
                $scope.$watch("loading", function() {
                    if(!$scope.loading) {

                        // TODO: this has to be called some time after loading to reselect the
                        // correct values, might move this to some other place in the future
                        // for instance a potential "done loading, ui is built"-event
                        setTimeout(function() {

                            // HACK: to make right_main_panel scrollable
                            // TODO: this has to run "fairly" late to work, it's probably better to run this code from somewhere else
                            let elem = $(".right_main_panel");

                            while(elem.length > 0) {
                                if(elem.attr("id") === "mdbpage") {
                                    break
                                }

                                if(elem.attr("id") === "MDB_DATASCHEME_0000000207_1") {
                                    elem.css("height", "92%")
                                } else {
                                    elem.css("height", "100%")
                                }

                                elem = elem.parent()
                            }
                        }, 500); // TODO: temporary fix while stuff is loading slowly
                    }
                })
            } else {
                // HACK: to make right_main_panel scrollable
                // TODO: this has to run "fairly" late to work, it's probably better to run this code from somewhere else
                let elem = $(".right_main_panel");

                while(elem.length > 0) {
                    if(elem.attr("id") === "mdbpage") {
                        break
                    }

                    if(elem.attr("id") === "MDB_DATASCHEME_0000000207_1") {
                        elem.css("height", "92%")
                    } else {
                        elem.css("height", "100%")
                    }

                    elem = elem.parent()
                }
            }
        },
        replace: true
    };
});

MDBDirectives.directive("mdbComponent", function($compile, $rootScope, AuthService){

    function parseItem(item, mdbformid) {
        const componentIs = (name) => item.MDB_UIAP_0000000193 ? item.MDB_UIAP_0000000193.indexOf(name) > -1 : false;


        // divs & backgrounds

        if(componentIs("GUI_COMPONENT_0000000230")) {
            if (item.MDB_UIAP_0000000579) {
                
                // todo remove/update this bridge at a later point or update the replacement in a local installation
                item.MDB_UIAP_0000000579 = item.MDB_UIAP_0000000579.replace('www', 'proto');

                return (
                    `<a href={{item.MDB_UIAP_0000000579}} title={{item.MDB_UIAP_0000000579}}>
				        <div id={{item.localID}} class=\"css-div form-group\" title={{item.MDB_UIAP_0000000018}}>
				            <mdb-document-form-items items=\"item\"></mdb-document-form-items>
				        </div>
			        </a>`
                )
            }
            else {
                return (
                    `<div id={{item.localID}} class=\"css-div form-group\" title={{item.MDB_UIAP_0000000018}}>
		                <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                    </div>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000310")) {
            return (
                `<div id={{item.localID}} class=\"left_main_panel form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000311")) {
            return (
                `<div id={{item.localID}} class=\"right_main_panel form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000058")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-white form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000168")) {

            //MDB_DATASCHEME_0000000791_1

            // TODO: workaround for user entry: hide admin comment
            if (item.localID == "MDB_DATASCHEME_0000000791_1"){
                return (
                    `<div id={{item.localID}} class=\"css-bg-white-bordered form-group\"
                        title={{item.MDB_UIAP_0000000018}}
                        style=\"background:transparent;width:100%;margin:0;padding:0;\">
                    </div>`
                )
            }
            // Special element for version information with scrolbar
            else if (item.localID == "MDB_DATASCHEME_0000000559_1" || item.localID == "MDB_DATASCHEME_0000000236_1") {
                return (
                    `<div id={{item.localID}} class=\"css-bg-white-bordered form-group\" title={{item.MDB_UIAP_0000000018}}>
                      <div>
                        <mdb-document-form-items items=\"item\" ></mdb-document-form-items>
                      </div>
                    </div>`
                )
            }
            else {
                return (
                    `<div id={{item.localID}} class=\"css-bg-white-bordered form-group\" title={{item.MDB_UIAP_0000000018}}>
                       <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                    </div>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000229")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-lighter-grey form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000006")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-lighter-grey-bordered form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        // GUI_COMPONENT__DIV: css-template-div
        else if(componentIs("GUI_COMPONENT_0000000348")) {
            return (
                `<div id={{item.localID}} class=\"css-template-div form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <button type=\"submit\" title={{item.MDB_UIAP_0000000018}} class="btn css-template-btn-top"
                        ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                        <span class="css-label4">template</span>     <span class="glyphicon glyphicon-cog"></span>
                    </button>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                    <button type=\"submit\" title={{item.MDB_UIAP_0000000018}} class="btn css-template-btn-bottom"
                        ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                        <span class="css-label4">template</span>     <span class="glyphicon glyphicon-cog"></span>
                    </button>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000304")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-green form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000305")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-red form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000301")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-turquoise form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000302")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-blue form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000307")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-red form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000306")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-orange form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000277")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-dark-grey form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000303")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-light-grey form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000145")) {
            return (
                `<div id={{item.localID}} class=\"panel_with_image_with_button\" title={{item.MDB_UIAP_0000000018}}>
                    <img src=\"/img/user.png\">
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000152")) {
            return (
                `<div id={{item.localID}} class=\"css-sort-div form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000026")) {
            return (
                `<div id={{item.localID}} class=\"css-dragable-div form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
                // title="Hold down left mouse button to drag&drop this object."
            )
        }

        // GUI_COMPONENT__BACKGROUND: .css-bg-white-transparent-bordered-round
        else if(componentIs("GUI_COMPONENT_0000000334")) {
            return (
                `<div id={{item.localID}} class=\"css-bg-white-transparent-bordered-round form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        //GUI_COMPONENT__BASIC_WIDGET: geo coordinates and map
        else if(componentIs("GUI_COMPONENT_0000000308")) {
            console.log("GUI_COMPONENT__BASIC_WIDGET: geo coordinates and map " + JSON.stringify(item));
            //TODO: dummy map
            return (
                `<div id={{item.localID}} class=\"css-geo-div form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/BlankMap-World6-Equirectangular.svg/500px-BlankMap-World6-Equirectangular.svg.png">
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        // entry header

        else if(componentIs("GUI_COMPONENT_0000000317")) {

            //console.info("css-entry-header-shadow-area: ", item.MDB_GUI_0000000040[0]);

            // default color
            item.bgcolor = "css-bg-color-user-entry";

            // TODO: IMPORTANT! JSON has to fulfill current arrangement to secure functionality of this code workaround:
            // entry type icon has to be first in array
            // check, which entry type ico the panel uses to set background color of child nodes
            let versionicon = item.MDB_GUI_0000000040[0].MDB_GUI_0000000040[0].MDB_UIAP_0000000193;

            // current draft
            if (versionicon == "GUI_COMPONENT_0000000072"){
                item.bgcolor = "css-bg-color-current-draft";
            }
            // current published
            else if (versionicon == "GUI_COMPONENT_0000000073"){
                item.bgcolor = "css-bg-color-current-published";
            }
            // trash icon
            else if (versionicon == "GUI_COMPONENT_0000000074"){
                item.bgcolor = "css-bg-color-deleted-draft";
            }
            // previously published
            else if (versionicon == "GUI_COMPONENT_0000000075"){
                item.bgcolor = "css-bg-color-previously-published";
            }
            // recycle bin icon
            else if (versionicon == "GUI_COMPONENT_0000000076"){
                item.bgcolor = "css-bg-color-bin-draft";
            }
            // saved draft
            else if (versionicon == "GUI_COMPONENT_0000000077"){
                item.bgcolor = "css-bg-color-saved-draft";
            }

            return (
                `<div id={{item.localID}} class=\"css-entry-header-bg-shadow css-div {{item.bgcolor}} form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000267")) {

            //console.info("css-entry-header-upper-area: ", item);
            // item.MDB_GUI_0000000040[0].MDB_UIAP_0000000193 = "GUI_COMPONENT_0000000072"

            item.bgcolor = "css-bg-color-user-entry";

            // TODO: IMPORTANT! JSON has to fulfill current arrangement to secure functionality of this code workaround:
            // entry type icon has to be first in array

            // check, which icon the panel uses to set background color of parentNode (3 level up)
            let versionicon = item.MDB_GUI_0000000040[0].MDB_UIAP_0000000193;

            // current draft
            if (versionicon == "GUI_COMPONENT_0000000072"){
                item.bgcolor = "css-bg-color-current-draft";
            }
            // current published
            else if (versionicon == "GUI_COMPONENT_0000000073"){
                item.bgcolor = "css-bg-color-current-published";
            }
            // trash icon
            else if (versionicon == "GUI_COMPONENT_0000000074"){
                item.bgcolor = "css-bg-color-deleted-draft";
            }
            // previously published
            else if (versionicon == "GUI_COMPONENT_0000000075"){
                item.bgcolor = "css-bg-color-previously-published";
            }
            // recycle bin icon
            else if (versionicon == "GUI_COMPONENT_0000000076"){
                item.bgcolor = "css-bg-color-bin-draft";
            }
            // saved draft
            else if (versionicon == "GUI_COMPONENT_0000000077"){
                item.bgcolor = "css-bg-color-saved-draft";
            }

            return (
                `<div id={{item.localID}} class=\"css-entry-header-upper-area form-group\" mdb-panel-color="{{item.bgcolor}}" mdb-panel-color-depth=3 title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )

        }

        else if(componentIs("GUI_COMPONENT_0000000278")) {
            return (
                `<div id={{item.localID}} class=\"css-entry-header-middle-area form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000266")) {

            return (
                `<div id={{item.localID}} class=\"css-entry-header-lower-area form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000235")) {
            return (
                `<div id={{item.localID}} class=\"panel_with_image_with_button css-user-frame3\" title={{item.MDB_UIAP_0000000018}}>
                    <img src=\"/img/user.png\">
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
                //'   <div ng-if=\"item.MDB_UIAP_0000000193.indexOf(\'GUI_COMPONENT_0000000145\') > -1\" class=\"class="clearfix ng-scope\">Weil ich es nicht besser kann: <br>Raum für Notizen und ein paar Sterne<br> <i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i><i class="glyphicon x-large glyphicon-star"></i>\</div>
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000140")) {
            return (
                `<span class=\"glowing-label\" title={{item.MDB_UIAP_0000000018}}>
                    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} ==online\"
                    title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-green\"></div>
                    <div ng-show=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}} "=online\"
                    title=\"{{item.MDB_UIAP_0000000266}}{{item.MDB_UIAP_0000000267}}\" class=\"led led-red\"></div>
                </span>`
            )
        }

        // headlines, paragraphs & labels

        else if(componentIs("GUI_COMPONENT_0000000068")) {
            return `<div class=\"css-headline1\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}}</div>`
        }

        else if(componentIs("GUI_COMPONENT_0000000193")) {
            return `<div class=\"css-headline2\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}}</div>`
        }

        else if(componentIs("GUI_COMPONENT_0000000226")) {
            return `<div class=\"css-headline3\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}}</div>`
        }

        else if(componentIs("GUI_COMPONENT_0000000227")) {
            return `<div class=\"css-headline4\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}}</div>`
        }

        else if(componentIs("GUI_COMPONENT_0000000280")) {
            return `<div class=\"css-paragraph1\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}}</div>`
        }

        else if(componentIs("GUI_COMPONENT_0000000289")) {
            return `<div class=\"css-paragraph2\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}}</div>`
        }

        else if(componentIs("GUI_COMPONENT_0000000008")) {

            // TODO: workaround to convert label1 to hyperlink (exception)
            if (item.MDB_UIAP_0000000579) {
                return `<a class=\"css-label1-link\" href={{item.MDB_UIAP_0000000579}} target="_blank" title={{item.MDB_UIAP_0000000579}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</a>`
            }
            else {
            return `<span class=\"css-label1\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000228")) {
            return `<span class=\"css-label2\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000242")) {
            return `<span class=\"css-label3\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000287")) {
            return `<span class=\"css-label4\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000288")) {
            return `<span class=\"css-label5\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000286")) {
            return `<span class=\"css-label6\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000291")) {
            return `<span class=\"css-label7\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000300")) {
            return `<span class=\"css-label8\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000337")) {
            return `<span class=\"css-label9\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000349")) {
            return `<span class=\"css-resource-label\" title={{item.MDB_UIAP_0000000018}}>{{item.MDB_GUI_0000000088}} {{item.MDB_GUI_0000000287}}</span>`
        }

        // buttons

        else if(componentIs("GUI_COMPONENT_0000000141")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                type=\"submit\"
                id={{item.localID}}
                title={{item.MDB_UIAP_0000000018}}
                ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                class="btn css-labeled-btn">
                {{item.MDB_GUI_0000000088}}
            </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000324")) {

            // TODO: workaround for testing early stage of partonomy view
            if (mdbformid ==="resource/de46dd64-20170707-md-2-d_1_1#GUI_COMPONENT_0000000316"){
                return (
                    `<a class="btn css-to-description-btn" href="/annotation_part"
                        id={{item.localID}}
                        title={{item.MDB_UIAP_0000000018}}>
                        <div class="path1"><span class="css-icon-partonomy-icon" style="font-size: x-large;"></span></span></div>
                        <div class="path2">{{item.MDB_GUI_0000000088}}</div>
                    </a>`
                )
            }
            else {
                return (
                    `<button
                        type=\"submit\"
                        id={{item.localID}}
                        title={{item.MDB_UIAP_0000000018}}
                        ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        class="btn css-to-description-btn">
                        <div class="path1"><span class="css-icon-partonomy-icon" style="font-size: x-large;"></span></span></div>
                        <div class="path2">{{item.MDB_GUI_0000000088}}</div>
                    </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000013")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return ( // TODO: remove onclick later again, only for testing
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    onclick="window.soverlay()"
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-icon-search-literature-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000015")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-icon-search-media-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000017")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                    return (
                        `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn btn-block css-icon-search-specimen-btn">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </button>`
                    )
                }
        }

        else if(componentIs("GUI_COMPONENT_0000000069")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<label class="css-toggle-btn">
                    <input
                        type="checkbox"
                        id={{item.localID}}
                        name={{item.localID}}
                        ng-model="mdbDoc[item.localID]"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-click="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)"
                    />
                    <i class="css-toggle-btn-switch"></i>
                    <span class="css-toggle-btn-background"></span>
                </label>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000110")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-icon-add-external-link-btn">
                        {{item.MDB_GUI_0000000088}}
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000196")) {
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class=\"btn css-expand-collapse-btn\" mdb-collapse-button>{{item.MDB_GUI_0000000088}}</button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000217")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                        ``
                )
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-icon-search-user-btn">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000218")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-icon-search-usergroup-btn">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000239")) {
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-labeled-btn-small">
                        {{item.MDB_GUI_0000000088}}
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000270")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="btn css-icon-search-btn">
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000037")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-backup">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="mdb-iconlabel-backup"></span>
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000038")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class=\"btn css-icon-transition-btn-revise\"
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="mdb-iconlabel-revise"></span>
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000039")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="btn css-icon-transition-btn-delete">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="mdb-iconlabel-delete"></span>
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000040")) {
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
                        <span class="mdb-iconlabel-tobin"></span>
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000041")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class=\"btn css-icon-transition-btn-publish\"
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                        <span class="mdb-iconlabel-publish"</span>
                </button>`
            )
        }

        // depricated?

        else if(componentIs("GUI_COMPONENT_0000000045")) { // .check-box
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            // TODO: still in use?
            return (
                `<input
                    type=\"checkbox\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                </input>`
            )
        }

        // icons

        else if(componentIs("GUI_COMPONENT_0000000072")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<i
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="css-icon-current-draft-icon">
                    <span class="path1"></span>
                    <span class="path2"></span>
                </i>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000073")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<i
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="css-icon-current-published-icon">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </i>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000075")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            return (
                `<i
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    class="css-icon-previously-published-icon">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                </i>`
            )
        }

        // .css-long-lat-selector-btn
        else if(componentIs("GUI_COMPONENT_0000000273")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                    return (

                        `<button type=\"button\" class=\"btn css-icon-long-lat-selector-btn\">
                    <div class="path1"></div>
                    <div class="path2"></div>
                    <div class="path3"></div>
                </button>`

                        //`<p class=\"input-group\">
                        //    <span class=\"css-data-input-left-narrow2-icon-date-selector-btn-group\">
                        //        <input type=\"text\" class=\"css-data-input-left-narrow2\" uib-datepicker-popup=\"dd.MM.yyyy\"
                        //            name={{item.localID}} id=\"{{item.localID}}\" ng-model=\"mdbDoc[item.localID]\"
                        //            is-open=\"datepickerOpened[item.localID]\"
                        //            datepicker-options=\"dateOptions\" close-text=\"close\"
                        //            title={{item.MDB_UIAP_0000000018}}
                        //            ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"/>
                        //        <button type=\"button\" class=\"btn css-icon-date-selector-btn\" ng-click=\"openDatePicker($event,item.localID)\"></button>
                        //    </span>
                        //</p>`
                    )
                }
        }

        else if(componentIs("GUI_COMPONENT_0000000275")) {
            return (
                `<p class=\"input-group\">
                    <span class=\"css-data-input-left-narrow2-icon-date-selector-btn-group\">
                        <input type=\"text\" class=\"css-data-input-left-narrow2\" uib-datepicker-popup=\"dd.MM.yyyy\"
                            name={{item.localID}} id=\"{{item.localID}}\" ng-model=\"mdbDoc[item.localID]\"
                            is-open=\"datepickerOpened[item.localID]\"
                            datepicker-options=\"dateOptions\" close-text=\"close\"
                            title={{item.MDB_UIAP_0000000018}}
                            ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"/>
                        <button type=\"button\" class=\"btn css-icon-date-selector-btn\" ng-click=\"openDatePicker($event,item.localID)\"></button>
                    </span>
                </p>`
                // TODO change date format for websocket in dd.MM.yyyy and websocket message has no key named "value"
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000114")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<div class="form-group has-feedback">
                    <input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"css-data-input\"
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
                        disabled=\"disabled\">
                    <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                </div>`
                )
            }
            else {
                return (
                    `<div class="form-group has-feedback">
                    <input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"css-data-input\"
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
                        ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                    <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                </div>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000113")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<div class="form-group has-feedback">
                        <input
                            name={{item.localID}}
                            id={{item.localID}}
                            class=\"css-data-input-narrow \"
                            ng-model=\"mdbDoc[item.localID]\"
                            placeholder={{item.MDB_UIAP_0000000220}}
                            title={{item.MDB_UIAP_0000000018}}
                            type={{item.html_input_type}}
                            disabled=\"disabled\">
                        <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                    </div>`
                )
            }
            else {
                return (
                    `<div class="form-group has-feedback">
                        <input
                            name={{item.localID}}
                            id={{item.localID}}
                            class=\"css-data-input-narrow\"
                            ng-model=\"mdbDoc[item.localID]\"
                            placeholder={{item.MDB_UIAP_0000000220}}
                            title={{item.MDB_UIAP_0000000018}}
                            type={{item.html_input_type}}
                            ng-blur="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)">
                        <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                    </div>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000237")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    disabled=\"disabled\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
            else {
                return (
                    `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000243")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-middle-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000244")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-right-narrow\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    disabled=\"disabled\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
            else {
                return (
                    `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-middle\"
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000245")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    // ng-required={{item.MDB_UIAP_0000000386}}
                    `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow\"
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    disabled=\"disabled\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
            else {
                return (
                    // ng-required={{item.MDB_UIAP_0000000386}}
                    `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow\"
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000247")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow2\"
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000246")) {
            return (
                `<input
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-data-input-left-narrow3\"
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                    ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000241")) {

            if (item.MDB_UIAP_0000000578){
                console.log("directive - css-search-input item:\n" + JSON.stringify(item));
                if (mdbformid.indexOf("-p_") !== -1) {
                    return (
                        `<div class="form-group has-feedback">
                    <input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"css-search-input\"
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
			            disabled=\"disabled\">
                    <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                </div>`
                    )
                }
                else {
                    return (
                        // TODO: workaround to get along with asynchronous websocket messages
                        `<div class="form-group has-feedback">
                    <input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"css-search-input\"
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
                        typeahead-wait-ms=150
                        uib-typeahead="i as i.label for i in autocomplete[item.localID] | filter:{label:$viewValue}"
                        typeahead-append-to-body="true"
                        typeahead-on-select=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413, item.MDB_UIAP_0000000578)\">
                    <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                </div>`
                    )
                }
            }
            else {
                if (mdbformid.indexOf("-p_") !== -1) {
                    return (
                        `<div class="form-group has-feedback">
                    <input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"css-search-input\"
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
			            disabled=\"disabled\">
                    <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                </div>`
                    )
                }
                else {
                    return (
                        // TODO: workaround to get along with asynchronous websocket messages
                        `<div class="form-group has-feedback">
                    <input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"css-search-input\"
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
                        typeahead-wait-ms=150
                        uib-typeahead="i as i.label for i in autocomplete[item.localID] | filter:{label:$viewValue}"
                        typeahead-append-to-body="true"
                        typeahead-on-select=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">
                    <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                </div>`
                    )
                }
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000120")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<textarea
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-textarea\"
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    disabled=\"disabled\">
                </textarea>`
                )
            }
            else {
                return (
                    `<textarea
                    name={{item.localID}}
                    id={{item.localID}}
                    class=\"css-textarea\"
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                </textarea>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000149")) {
            if(item.MDB_UIAP_0000000578){
                return (
                    // TODO: workaround to get along with asynchronous websocket messages
                    `<input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"\"
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
                        typeahead-wait-ms=150
                        uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"
                        typeahead-on-select=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        ng-blur=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000578)\">
                    <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                        ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
            else {
                return (
                    // TODO: workaround to get along with asynchronous websocket messages
                    `<input
                        name={{item.localID}}
                        id={{item.localID}}
                        class=\"\"
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model=\"mdbDoc[item.localID]\"
                        placeholder={{item.MDB_UIAP_0000000220}}
                        title={{item.MDB_UIAP_0000000018}}
                        type={{item.html_input_type}}
                        typeahead-wait-ms=150
                        uib-typeahead="dingsi as dingsi.label for dingsi in autocomplete[item.localID] | filter:$viewValue.label"
                        typeahead-on-select=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        ng-blur=\"submitAutocompleteSelect(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                        ng-change=\"submitAutocompleteChange(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], item.MDB_UIAP_0000000413)\">
                    <p ng-if=\"forms.mdbDoc[item.localID].errorMessage\"
                        ng-show=\"forms.mdbDoc.{{item.localID}}.$invalid\" class=\"help-block\">{{forms.mdbDoc[item.localID].errorMessage}}</p>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000119")) {
            return (
                `<textarea
                    name={{item.localID}}
                    ng-required={{item.MDB_UIAP_0000000386}}
                    ng-model=\"mdbDoc[item.localID]\"
                    placeholder={{item.MDB_UIAP_0000000220}}
                    title={{item.MDB_UIAP_0000000018}}
                    type={{item.html_input_type}}
                    ng-blur=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                </textarea>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000066")) {

            // TODO: select options can be either in MDB_UIAP_0000000023 or MDB_UIAP_0000000118
            if(item.MDB_UIAP_0000000023) {
                if (mdbformid.indexOf("-p_") !== -1) {
                    //item.selectOptions = item.MDB_UIAP_0000000118;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000118"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        disabled=\"disabled\">
                    <option ng-repeat="i in item.MDB_UIAP_0000000023" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
                else {
                    //item.selectOptions = item.MDB_UIAP_0000000023;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        ng-change="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000023" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
            }
            else if(item.MDB_UIAP_0000000118) {
                if (mdbformid.indexOf("-p_") !== -1) {
                    //item.selectOptions = item.MDB_UIAP_0000000118;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000118"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        disabled=\"disabled\">
                    <option ng-repeat="i in item.MDB_UIAP_0000000118" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
                else {
                    //item.selectOptions = item.MDB_UIAP_0000000118;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000118"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        ng-change="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000118" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000249")) {

            // TODO: select options can be either in MDB_UIAP_0000000023 or MDB_UIAP_0000000118
            if(item.MDB_UIAP_0000000023) {
                if (mdbformid.indexOf("-p_") !== -1) {
                    //item.selectOptions = item.MDB_UIAP_0000000023;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown-right"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        disabled=\"disabled\">
                    <option ng-repeat="i in item.MDB_UIAP_0000000023" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
                else {
                    //item.selectOptions = item.MDB_UIAP_0000000023;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown-right"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        ng-change="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)"
                        ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000023" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>

                </select-wrapper>`
                    )
                }
            }
            else if(item.MDB_UIAP_0000000118) {
                if (mdbformid.indexOf("-p_") !== -1) {
                    //item.selectOptions = item.MDB_UIAP_0000000118;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000118"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown-right"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        disabled=\"disabled\">
                    <option ng-repeat="i in item.MDB_UIAP_0000000118" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
                else {
                    //item.selectOptions = item.MDB_UIAP_0000000118;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000118"

                    return (
                        `<select-wrapper for={{item.localID}} class="form-group has-feedback" ng-class="{requirement_fullfilled: mdbDoc[item.localID]}">
                    <select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="Fpicker css-dropdown-right"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-required={{item.MDB_UIAP_0000000386}}
                        ng-model="mdbDoc[item.localID]"
                        ng-change="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000118" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>
                    <span ng-if="item.MDB_UIAP_0000000386"
                        ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                        class="form-control-feedback required-info"
                        data-toggle="tooltip" title="required information"></span>
                </select-wrapper>`
                    )
                }
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000248")) {

            if(item.MDB_UIAP_0000000023) {
                if (mdbformid.indexOf("-p_") !== -1) {
                    //item.selectOptions = item.MDB_UIAP_0000000023;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023"

                    return (
                        `<select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="css-creative-commons-dropdown"
                        style="width: 200px"
                        placeholder={{item.MDB_UIAP_0000000457}}
                        title={{item.MDB_UIAP_0000000018}}
                        ng-model="mdbDoc[item.localID]"
                        disabled=\"disabled\">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000023" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>`
                    )
                }
                else {
                    //item.selectOptions = item.MDB_UIAP_0000000023;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023"

                    return (
                        `<select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="css-creative-commons-dropdown"
                        style="width: 200px"
                        placeholder={{item.MDB_UIAP_0000000457}}
                        title={{item.MDB_UIAP_0000000018}}
                        ng-model="mdbDoc[item.localID]"
                        ng-blur="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000023" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>`
                    )
                }
            }
            else if(item.MDB_UIAP_0000000118) {
                if (mdbformid.indexOf("-p_") !== -1) {
                    //item.selectOptions = item.MDB_UIAP_0000000023;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000023"

                    return (
                        `<select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="css-creative-commons-dropdown"
                        style="width: 200px"
                        placeholder={{item.MDB_UIAP_0000000457}}
                        title={{item.MDB_UIAP_0000000018}}
                        ng-model="mdbDoc[item.localID]"
                        disabled=\"disabled\">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000118" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>`
                    )
                }
                else {
                    //item.selectOptions = item.MDB_UIAP_0000000118;

                    // Note: changed from ng-options to ng-repeat: ng-options does not provide titles for dropdown-list-entries
                    // ng-options="i.selValue as i.selLabel for i in item.MDB_UIAP_0000000118"

                    return (
                        `<select
                        name={{item.localID}}
                        id={{item.localID}}
                        class="css-creative-commons-dropdown"
                        placeholder={{item.MDB_UIAP_0000000457}}
                        title={{item.MDB_UIAP_0000000018}}
                        ng-model="mdbDoc[item.localID]"
                        ng-blur="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)">
                    <option value="" disabled>{{item.MDB_UIAP_0000000457}}</option> <!-- default option / nothing selected -->
                    <option ng-repeat="i in item.MDB_UIAP_0000000118" value={{i.selValue}} title="{{i.MDB_UIAP_0000000018}}" alt="{{i.MDB_UIAP_0000000018}}">{{i.selLabel}}</option>
                    </select>`
                    )
                }
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000139")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return `<img src=\"/img/user.png\">`
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000102")) {
            return `<span href=\" \">{{item.MDB_GUI_0000000088}}</span>`
        }

        else if(componentIs("GUI_COMPONENT_0000000186")) {
            return (
                `<a
                    ng-href="" target="_blank" title="Browse recource." data-toggle="tooltip">
                    <!--ng-if="forms.mdbDoc[MDB_GUI_0000000037]$invalid"-->
                    <i class="glyphicon glyphicon-search input-group-addon"></i>
                </a>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000284")) {
            return (
                `<div id={{item.localID}} class=\"css-table-of-contents form-group\">
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000255")) {
            // todo add this part after holidays  ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
            return (
                `<div id={{item.localID}}
                      class=\"css-tab1 {{item.MDB_UIAP_0000000667}}\"
                      title={{item.MDB_UIAP_0000000018}}>
                   {{item.MDB_GUI_0000000088}}
                </div>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000264")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"

            //console.info("css-version: ", item.MDB_GUI_0000000040[1].MDB_GUI_0000000088);
            //console.info("css-version: ", item);

            //css-bg-color-previously-published
            //css-bg-color-current-published
            //css-bg-color-saved-draft
            //css-bg-color-current-draft
            //css-bg-color-deleted-draft
            //css-bg-color-bin-draft
            //css-bg-color-user-entry

            // TODO: IMPORTANT! JSON has to fit current arrangement to secure functionality of this code workaround:
            // versionicon has to be first item in array
            var versionicon = item.MDB_GUI_0000000040[0].MDB_UIAP_0000000193;

            if (versionicon == "GUI_COMPONENT_0000000072"){
                item.bgcolor = "css-bg-color-current-draft";
            }
            else if (versionicon == "GUI_COMPONENT_0000000073"){
                item.bgcolor = "css-bg-color-current-published";
            }
            // trash icon
            else if (versionicon == "GUI_COMPONENT_0000000074"){
                item.bgcolor = "css-bg-color-deleted-draft";
            }
            else if (versionicon == "GUI_COMPONENT_0000000075"){
                item.bgcolor = "css-bg-color-previously-published";
            }
            // recycle bin icon
            else if (versionicon == "GUI_COMPONENT_0000000076"){
                item.bgcolor = "css-bg-color-bin-draft";
            }
            else if (versionicon == "GUI_COMPONENT_0000000077"){
                item.bgcolor = "css-bg-color-saved-draft";
            }

            return (
                `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{ item.MDB_UIAP_0000000018}}
                    class="btn css-version {{item.bgcolor}}">
                    <div class="path1"></div>
                    <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                </button>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000297")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    ng-model=\"mdbDoc[item.localID]\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="css-editing-status-selector-left-toggle-btn"
                    ng-init="mdbDoc[item.localID]=false" ng-click="mdbDoc[item.localID] = !mdbDoc[item.localID]"
                    ng-class="mdbDoc[item.localID] ? 'css-editing-status-selector-left-toggle-btn-on' : 'css-editing-status-selector-left-toggle-btn-off'">
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000298")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    ng-model=\"mdbDoc[item.localID]\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="css-editing-status-selector-middle-toggle-btn"
                    ng-init="mdbDoc[item.localID]=false" ng-click="mdbDoc[item.localID] = !mdbDoc[item.localID]"
                    ng-class="mdbDoc[item.localID] ? 'css-editing-status-selector-middle-toggle-btn-on' : 'css-editing-status-selector-middle-toggle-btn-off'">
                </button>`
                )
            }
        }

        else if(componentIs("GUI_COMPONENT_0000000299")) {
            //'       ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID])\"
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    ng-model=\"mdbDoc[item.localID]\"
                    title={{item.MDB_UIAP_0000000018}}
                    class="css-editing-status-selector-right-toggle-btn"
                    ng-init="mdbDoc[item.localID]=false" ng-click="mdbDoc[item.localID] = !mdbDoc[item.localID]"
                    ng-class="mdbDoc[item.localID] ? 'css-editing-status-selector-right-toggle-btn-on' : 'css-editing-status-selector-right-toggle-btn-off'">
                </button>`
                )
            }
        }

        //    "classID": "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000991"
        //    "individualID": "http://www.morphdbase.de/Ontologies/MDB/MDBDataScheme#MDB_DATASCHEME_0000000993"
        else if(componentIs("GUI_COMPONENT_0000000195")) {
            return (
                `<span id={{item.localID}} class="css-icon-crosslink-icon"></span>`
            )
        }

        else if(componentIs("GUI_COMPONENT_0000000309")) {
            return (
                `<p class=\"input-group\">
                    <span class=\"css-data-input-left-narrow2-icon-time-selector-btn-group\">
                        <input type=\"text\" class=\"css-data-input-left-narrow2\"
                            name={{item.localID}} id=\"{{item.localID}}\" ng-model=\"mdbDoc[item.localID]\"
                            title={{item.MDB_UIAP_0000000018}}
                            ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"/>
                        <button type=\"button\" class=\"btn css-icon-time-selector-btn\" \"></button>
                    </span>
                </p>`
                // TODO: no functionality
            )
        }

        //GUI_COMPONENT_0000000023
        else if(componentIs("GUI_COMPONENT_0000000023")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return ``
            }
            else {
                return (
                    `<button
                    type=\"submit\"
                    id={{item.localID}}
                    title={{item.MDB_UIAP_0000000018}}
                    ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                    class="css-delete-btn">
                    X
                </button>`
                )
            }
        }

        //GUI_COMPONENT_0000000052
        else if(componentIs("GUI_COMPONENT_0000000052")) {
            return (
                `<span id={{item.localID}} title={{item.MDB_UIAP_0000000018}} class="css-icon-specimen-icon"></span>`
            )
        }

        // GUI_COMPONENT_0000000181
        else if(componentIs("GUI_COMPONENT_0000000181")) {
            return (
                `<span id={{item.localID}} title={{item.MDB_UIAP_0000000018}} class="css-icon-morphological-description-icon"></span>`
            )
        }

        // GUI_COMPONENT_0000000314
        else if(componentIs("GUI_COMPONENT_0000000314")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<label class="css-not-toggle-icon">
                    <input
                        type="checkbox"
                        id={{item.localID}}
                        name={{item.localID}}
                        ng-model="mdbDoc[item.localID]"
                        ng-init="mdbDoc[item.localID]=false"
                        title={{item.MDB_UIAP_0000000018}}
                        disabled="disabled"/>
                    <i ng-hide="mdbDoc[item.localID]" class="css-icon-not-negated-icon"></i>
                    <i ng-show="mdbDoc[item.localID]" class="css-icon-negated-icon"></i>
                </label>`
                )
            }
            else {
                return (
                    `<label class="css-not-toggle-icon">
                    <input
                        type="checkbox"
                        id={{item.localID}}
                        name={{item.localID}}
                        ng-model="mdbDoc[item.localID]"
                        ng-init="mdbDoc[item.localID]=false"
                        title={{item.MDB_UIAP_0000000018}}
                        ng-click="submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)"/>
                    <i ng-hide="mdbDoc[item.localID]" class="css-icon-not-negated-icon"></i>
                    <i ng-show="mdbDoc[item.localID]" class="css-icon-negated-icon"></i>
                </label>`
                )
            }
        }

        // GUI_COMPONENT_0000000318
        // TODO: frame for thumbnails only needed, if cute animal pictures are banned from entry header
        /*else if(componentIs("GUI_COMPONENT_0000000318")) {
         return (
         `<div id={{item.localID}} title={{item.MDB_UIAP_0000000018}} class="css-picture-frame"></div>`
         )
         }*/

        // GUI_COMPONENT_0000000326 GUI_COMPONENT__ICON: .css-negated-icon
        else if(componentIs("GUI_COMPONENT_0000000326")) {
            return (
                `<i class="css-icon-negated-icon"></i>`
            )
        }

        // GUI_COMPONENT_0000000327 GUI_COMPONENT__ICON: .css-not-negated-icon
        else if(componentIs("GUI_COMPONENT_0000000327")) {
            return (
                `<i class="css-icon-not-negated-icon"></i>`
            )
        }

        // GUI_COMPONENT_0000000335 GUI_COMPONENT__INPUT_TYPE: .css-date-time-input
        else if(componentIs("GUI_COMPONENT_0000000335")) {
            // todo workaround for workshop
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `<div class="form-group has-feedback">
                        <input
                            name={{item.localID}}
                            id={{item.localID}}
                            class=\"css-data-input-narrow \"
                            ng-model=\"mdbDoc[item.localID]\"
                            placeholder={{item.MDB_UIAP_0000000220}}
                            title={{item.MDB_UIAP_0000000018}}
                            type={{item.html_input_type}}
                            disabled=\"disabled\">
                        <span ng-if="item.MDB_UIAP_0000000386"
                            ng-class="{requirement_fullfilled: mdbDoc[item.localID]}"
                            class="form-control-feedback required-info"
                            data-toggle="tooltip" title="required information"></span>
                    </div>`
                )
            }
            else {
                return (
                    `<p class=\"input-group\">
                        <span class=\"css-data-input-left-narrow2-icon-date-selector-btn-group\">
                            <input
                                type=\"text\"
                                class=\"css-data-input-left-narrow2\"
                                datetime-picker=\"dd.MM.yyyy HH:mm:ss \"
                                name={{item.localID}} id=\"{{item.localID}}\"
                                ng-model=\"mdbDoc[item.localID]\"
                                is-open=\"datepickerOpened[item.localID]\"
                                close-on-date-selection=\"false\"
                                close-text=\"close\"
                                timepicker-options=\"
                                    {
                                        showMeridian: false,
                                        showSeconds: true
                                    }\"
                                title={{item.MDB_UIAP_0000000018}}
                                ng-change=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\"
                            />
                            <button
                                type=\"button\"
                                class=\"btn css-icon-date-selector-btn\"
                                ng-click=\"openDatePicker($event,item.localID)\">
                            </button>
                        </span>
                    </p>`
                )
            }
        }

        // .css-map-div
        else if(componentIs("GUI_COMPONENT_0000000336")) {
            //TODO: dummy map
            return (
                `<div id={{item.localID}} class=\"css-map-div form-group\" title={{item.MDB_UIAP_0000000018}}>
                    <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/BlankMap-World6-Equirectangular.svg/500px-BlankMap-World6-Equirectangular.svg.png">
                </div>`
            )
        }

        // partonomy icons

        // GUI_COMPONENT_0000000328
        else if(componentIs("GUI_COMPONENT_0000000328")) {
            // TODO: adjust to proper component
            return (
                `<div id={{item.localID}}>
                    <img src="/img/icon_description_property_3D.jpg">
                </div>`
            )
        }

        // GUI_COMPONENT_0000000188
        else if(componentIs("GUI_COMPONENT_0000000188")) {
            if (mdbformid.indexOf("-p_") !== -1) {
                return (
                    `
                    <input
                        type=\"radio\"
                        class=\"css-radio-button\"
                        id={{item.localID}}
                        title={{item.MDB_UIAP_0000000018}}
                        name={{item.MDB_GUI_0000000555}}
                        ng-model=\"mdbDoc[item.localID]\"
                        ng-value=true
                        disabled=\"disabled\">
                `
                )
            }
            else {
                return (
                    `
                    <input
                        type=\"radio\"
                        class=\"css-radio-button\"
                        id={{item.localID}}
                        title={{item.MDB_UIAP_0000000018}}
                        name={{item.MDB_GUI_0000000555}}
                        ng-model=\"mdbDoc[item.localID]\"
                        ng-value=true
                        ng-click=\"submit(mdbformid, forms.mdbDoc, item.localID, mdbDoc[item.localID], mdbpartid, mdbactivetab)\">
                `
                )
            }
        }

        /// ELSE!
        else {
            if(item.MDB_UIAP_0000000193){
                if(item.MDB_UIAP_0000000193 == "GUI_COMPONENT_0000000318"){
                    console.warn("no class component yet, frame for thumbnails only needed, if cute animal pictures are banned from entry header: ", item.MDB_UIAP_0000000193);
                }
                else {
                    console.error("component without class component: ", item.MDB_UIAP_0000000193);
                }
            }
            else {
                if ($rootScope.developers.indexOf(AuthService.getUsername()) > -1){console.error("item with no component type (MDB_UIAP_0000000193): ", item);}
                return (
                    `<div id={{item.localID}}
                            class=\"css-div css-bg-turquoise form-group\"
                            style=\"border: solid 1px grey; padding: 5px;\">
                            {{item.localID}} - no component type!
                            <mdb-document-form-items items=\"item\"></mdb-document-form-items>
                        </div>`
                )

            }

        }
    }
    return {
        restrict: "E",
        replace: true,
        priority: 100000,
        scope: false,
        link: function(scope, element) {
            scope.datepickerOpened = {};

            scope.openDatePicker = function(ev, localID) {
                scope.datepickerOpened[localID] = true;
            };

            angular.element(element).injector().invoke(["$compile", function($compile) {

                let parsed = parseItem(scope.item, scope.mdbformid);

                // TODO: show Christian
                // Do Not compile the element NOT being part of the DOM
                //let compiled = $compile(parsed)(scope);
                //element.append(compiled);

                // Firstly include in the DOM, then compile
                let result = $(parsed).appendTo(element);
                $compile(result)(scope);
            }])
        }
    }
});

MDBDirectives.directive("mdbPanelColor", function($timeout) {
    return {
        restrict: "A",
        scope: false,
        link: function(scope, element, attrs) {
            $timeout(function() {
                let depth = Number(attrs["mdbPanelColorDepth"]) || 0;

                let current = element;

                for(let i = 0; i < depth; i++) {
                    current = current.parent()
                }

                current.addClass(attrs["mdbPanelColor"])
            })
        }
    }
});

MDBDirectives.directive("mdbCollapseButton", function() {
    return {
        restrict: "A",
        scope: false,
        link: function(scope, element, attrs) {
            // TODO: a collapse button and the correct field are (at the moment) always 6 components apart
            // this works but is fairly hacky, need to fix this later. This is due to the nested structure of the
            // page elements not complying to jQuery accordion - https://jqueryui.com/accordion/
            const componentOffset = 6;

            // TODO: need a better point in time to hook this code but the divs aren't yet created so
            // waiting 200 ms is sadly needed
            setTimeout(function() {
                const componentObject = $(element).parent();
                const components = $("mdb-component");
                const componentIndex = components.index(componentObject);

                let targetScope = angular.element(components[componentIndex + componentOffset]).scope();

                if(targetScope.item && targetScope.item.MDB_UIAP_0000000447 !== "true") {
                    $(components[componentIndex + componentOffset]).slideUp(0);
                    scope.collapsed = true;
                    $(element).addClass("css-expand-collapse-btn-closed")
                }
            }, 200);

            element.on("click", function() {
                const componentObject = $(element).parent();
                const components = $("mdb-component");
                const componentIndex = components.index(componentObject);

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

MDBDirectives.directive("external", function() {
    return {
        restrict: "A",
        scope: false,
        link: function(scope, element, attrs) {
            element.on("click", function(ev) {
                if(attrs.target === "_blank") {
                    window.open(attrs.href);
                    return
                }

                location.href = attrs.href
            })
        }
    }
});