/**
 *  Copyright 2009-2011 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
 
var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace Description_Plugin
 */
Opencast.User_Plugin = (function ()
{
    // The Template to process
    var template =  '<div style="float: left; width: 100%; height: 10%;">' + 
                        '<form id="oc-form-clipshow-search">' +
                        '<p>Search: <input id="oc-form-clipshow-search-tag" type="text" name="search" value="${searchTerm}" style="width: 150px;"/> and sort by ' + 
                          '<select id="oc-form-clipshow-search-sort">' +
                            '<option value="GOOD">good</option>' +
                            '<option value="FUNNY">funny</option>' +
                          '</select>' +
                        ' ratings </p>' +
                    '</div>' +
                    '<div id="oc_usertab-column-one" style="float: left; width: 100%; height: 90%">' +
                      '<ol style="float:left; width: 30%;">' +
                        '{for c in clipshows.firstColumn}' +
                          '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${c.id})">${c.title} by ${c.author}</button></li>' +
                        '{/for}' +
                      '</ol>' +
                      '<ol id="oc_usertab-column-two" style="float:left; width: 30%;" start="${columnLength + 1}">' +
                        '{for c in clipshows.secondColumn}' +
                          '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${c.id})">${c.title} by ${c.author}</button></li>' +
                        '{/for}' +
                      '</ol>' +
                      '<ol id="oc_usertab-column-three" style="float:left; width: 30%;" start="${(2 * columnLength) + 1}">' +
                        '{for c in clipshows.thirdColumn}' +
                          '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${c.id})">${c.title} by ${c.author}</button></li>' +
                        '{/for}' +
                      '</ol>' +
                    '</div>' +
                    '<div style="clear: both">' + 
                    '</div>';
                    
    // The Element to put the div into
    var element;
    // Data to process
    var user_data;
    // Precessed Data
    var processedTemplateData = false;
    
    /**
     * @memberOf Opencast.User_Plugin
     * @description Add As Plug-in
     * @param elem Element to fill with the Data (e.g. a div)
     * @param data The data to fill the tab with
    */
    function addAsPlugin(elem)
    {
        element = elem;

        user_data = {searchTerm: ""};

        createTab("GOOD");
    }

    function refreshSearchData() {
      user_data.searchTerm = $('#oc-form-clipshow-search-tag').val();
      $.ajax(
      {
          type: "GET",
          url: "../../clipshow/tags/search/mediapackage",
          data: {mediapackageId: Opencast.Player.getMediaPackageId() ,tag: $('#oc-form-clipshow-search-tag').val()},
          dataType: 'json',
          success: function (json)
          {
            var results = json.wrapper.data;
            if ('undefined' != typeof results) {
              if ($.isArray(results)) {
                user_data.searches = results;
              } else {
                user_data.searches = [results];
              }
            } else {
              user_data.searches = [];
            }
            createTab($("#oc-form-clipshow-search-sort option:selected").val());
          },
          error: function (a, b, c)
          {
            user_data.searches = [];
            createTab($("#oc-form-clipshow-search-sort option:selected").val());
          }
      });
    }

    function changeUsername() {
      user_data.displayName = $('#oc-form-username').val();
      $.ajax(
      {
          type: "POST",
          url: "../../clipshow/user/newName",
          data: {newName: $('#oc-form-username').val()},
          dataType: 'json',
          success: function (json)
          {
              Opencast.clipshow_ui.updateUserCredentials();
              Opencast.clipshow_ui.refreshClipshowList();
              createTab($("#oc-select-sort-type option:selected").val());
          },
          error: function (a, b, c)
          {
              $('#oc-form-username').val() = "Error";
          }
      });
    }

    /**
     * @memberOf Opencast.User_Plugin
     * @description Processes the Data and puts it into the Element
     * @param selectType
     * @return true if successfully processed, false else
     */
    function createTab(selectType)
    {
        if ('undefined' != typeof element)
        {
          var clist = [];
          if ('undefined' != typeof user_data.searches && user_data.searches != []) {
            clist = user_data.searches;
          } else {
            clist = Opencast.clipshow_ui.getClipshowList();
          }            

          //Sort by dislike first so those end up at the top
          clist.sort(function(a,b) { return parseInt(b.dislike) - parseInt(a.dislike) } );
          if (selectType == "GOOD") {
            clist.reverse();
            clist.sort(function(a,b) { return parseInt(b.funny) - parseInt(a.funny) } );
            clist.sort(function(a,b) { return parseInt(b.good) - parseInt(a.good) } );
          } else if (selectType == "FUNNY") {
            clist.reverse();
            clist.sort(function(a,b) { return parseInt(b.good) - parseInt(a.good) } );
            clist.sort(function(a,b) { return parseInt(b.funny) - parseInt(a.funny) } );
          } else if (selectType == "DISLIKE") {
            //Sorted above, so pass?
          }
          //Sort into three columns, divide clipshows as evenly as possible
          var columnSize = Math.max(Math.round(clist.length / 3), 1);
          user_data.columnLength = columnSize;
          var clipshows = { firstColumn: [], secondColumn: [], thirdColumn: []};
          for (var i = 0; i < columnSize; i++) {
            clipshows.firstColumn[i] = clist[i];
          }
          for (var j = 0; j < columnSize; j++) {
            if ('undefined' == typeof clist[j + columnSize]) {
              break;
            }
            clipshows.secondColumn[j] = clist[j + columnSize];
          }
          for (var k = 0; k < columnSize; k++) {
            if ('undefined' == typeof clist[k + (2 * columnSize)]) {
              break;
            }
            clipshows.thirdColumn[k] = clist[k + (2 * columnSize)];
          }
          user_data.clipshows = clipshows;

          $.log("User Plugin: Data available, processing template");
          processedTemplateData = template.process(user_data);
          element.html(processedTemplateData);
          $('#oc-clipshow-user').show();
          $('#clipshow-user-loading').hide();
          $(".clipshow-link-button").button();

          $('#oc-form-clipshow-search-sort').val(selectType);
          $("#oc-form-clipshow-search-sort").change(function() {
            var type = $("#oc-form-clipshow-search-sort option:selected").val();
            createTab(type);
          });

          //$('#oc-input-tagged-button').button();
          $('#oc-form-clipshow-search').submit(function ()
          {
            refreshSearchData();
            return false;
          });
          return true;
        }
        else
        {
            $.log("User Plugin: No data available");
            return false;
        }
    }

            /*$.ajax(
            {
                type: "GET",
                url: "../../clipshow/user/rankings",
                dataType: 'json',
                success: function (json)
                {
                  user_data.my_votes = json["clipshow-ranking-blob"].my_score;
                  if ($.isArray(json["clipshow-ranking-blob"].top_users)) {
                    user_data.users = json["clipshow-ranking-blob"].top_users;
                  } else {
                    user_data.users = [json["clipshow-ranking-blob"].top_users];
                  }*/

    function randomInt(from, to){
       return Math.floor(Math.random() * (to - from + 1) + from);
    }

    
    return {
        addAsPlugin: addAsPlugin,
    };
}());
