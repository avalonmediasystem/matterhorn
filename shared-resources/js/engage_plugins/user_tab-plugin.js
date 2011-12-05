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
//                        '<option value="DISLIKE">disliked</option>' +
    // The Template to process
    var template =  '<div style="float: left; width: 25%">' + 
                        '<form id="oc-form-name"><div style="float: left;">' +
                        '<span>Your Name:&nbsp;<input id="oc-form-username" type="text" name="newName" value="${displayName}" style="width: 150px;"/></span>' +
                        '<input id="oc-input-username" type="submit" value="Change Name" />' +
                        '</div></form><br />' +
                        '<p>Your total votes: ${my_votes}</p>' +
                        '<p>Top Users</p>' +
                        '<ol>' +
                          '{for u in users}' +
                            '<li>${u}</li>' +
                          '{/for}' +
                        '</ol>' +
                    '</div>' +
                    '<div style="float: left; width: 25%">' +
                        '<p>Top five <select id="oc-select-sort-type">' +
                        '<option value="GOOD">useful</option>' + 
                        '<option value="FUNNY">funny</option>' +

                        '</select>clipshows in for this episode:</p>' +
                        '<div id="oc_clipshow-sorted-list">' +
                          '<ol>' +
                            '{for c in sorted}' +
                              '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${c.id})">${c.title} by ${c.author}</button></li>' +
                            '{/for}' +
                          '</ol>' +
                        '</div>' +
                    '</div>' +
                    '<div style="float: left; width: 25%">' +
                        'Other clipshows in for this episode:<br />' +
                        '<div id="oc_clipshow-random-list">' +
                          '<ol>' +
                            '{for r in randoms}' +
                              '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${r.id})">${r.title} by ${r.author}</button></li>' +
                            '{/for}' +
                          '</ol>' +
                        '</div>' +
                    '</div>' +
                    '<div style="float: left; width: 25%">' +
                        '<form id="oc-form-tag"><div style="float: left;">' +
                          'Clipshows tagged with <input type="text" id="oc-form-tagged-with" value="${searchTerm}" style="width: 150px"/>' +
                          '<input id="oc-input-tagged-button" type="submit" value="Search" />' + 
                        '</div></form>' +
                        '<div id="oc_clipshow-search-list">' +
                          '<ol>' +
                            '{for s in searches}' +
                              '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${s.id})">${s.title} by ${s.author}</button></li>' +
                            '{/for}' +
                          '</ol>' +
                        '</div>' +
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

        user_data = {displayName: Opencast.clipshow_ui.getUsername(), searchTerm: "", searches: []};

        createTab("GOOD");
    }

    function refreshSearchData() {
      user_data.searchTerm = $('#oc-form-tagged-with').val();
      $.ajax(
      {
          type: "GET",
          url: "../../clipshow/tags/search",
          data: {tag: $('#oc-form-tagged-with').val()},
          dataType: 'json',
          success: function (json)
          {
            var results = json.wrapper.data;
            if (results != undefined) {
              if ($.isArray(results)) {
                Opencast.User_Plugin.setSearchData(results);
              } else {
                Opencast.User_Plugin.setSearchData([results]);
              }
            } else {
              Opencast.User_Plugin.setSearchData([]);
            }
            createTab($("#oc-select-sort-type option:selected").val());
          },
          error: function (a, b, c)
          {
            Opencast.User_Plugin.setSearchData([]);
            createTab($("#oc-select-sort-type option:selected").val());
          }
      });
    }

    function setSearchData(searchData) {
      user_data.searches = searchData;
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
              Opencast.User_Plugin.createTab($("#oc-select-sort-type option:selected").val());
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
        if ((element !== undefined))
        {
            var clist = Opencast.clipshow_ui.getClipshowList();
            if ($.isArray(clist)) {
              //Sort by dislike first so those end up at the top
              clist.sort(function(a,b) { return parseInt(b.dislike) - parseInt(a.dislike) } );
              if (selectType == "GOOD") {
                clist.reverse();
                clist.sort(function(a,b) { return parseInt(b.good) - parseInt(a.good) } );
              } else if (selectType == "FUNNY") {
                clist.reverse();
                clist.sort(function(a,b) { return parseInt(b.funny) - parseInt(a.funny) } );
              } else if (selectType == "DISLIKE") {
                //Sorted above, so pass?
              }
              sorted = []
              randomShows = []
              //Fill the sorted list
              for (var i = 0; i < 5; i++) {
                if (clist[i] == undefined) {
                  break;
                }
                sorted[i] = clist[i];
              }
              user_data.sorted = sorted;

              //Pick up to five other options randomly
              var numberOfRandomClipshows = Math.min(clist.length - 5, 5);
              if (numberOfRandomClipshows > 0) {
                for (var i = 0; i < numberOfRandomClipshows; i++) {
                  randomShows[i] = clist[randomInt(5, clist.length-1)];
                }
              }
              user_data.randoms = randomShows;
            } else {
              user_data.sorted = [clist];
              user_data.randoms = [];
            }

            $.ajax(
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
                  }

                  $.log("User Plugin: Data available, processing template");
                  processedTemplateData = template.process(user_data);
                  element.html(processedTemplateData);
                  $('#oc-clipshow-user').show();
                  $('#clipshow-user-loading').hide();
                  $(".clipshow-link-button").button();

                  $('#oc-select-sort-type').val(selectType);
                  $("#oc-select-sort-type").change(function() {
                    var type = $("#oc-select-sort-type option:selected").val();
                    Opencast.User_Plugin.createTab(type);
                  });

                  $('#oc-input-username').button();
                  $('#oc-form-name').submit(function ()
                  {
                    Opencast.User_Plugin.changeUsername();
                    return false;
                  });

                  $('#oc-input-tagged-button').button();
                  $('#oc-form-tag').submit(function ()
                  {
                    Opencast.User_Plugin.refreshSearchData();
                    return false;
                  });
                },
                error: function (a, b, c)
                {
                    $('#oc-form-username').val() = "Error";
                }
            });
            return true;
        }
        else
        {
            $.log("User Plugin: No data available");
            return false;
        }
    }

    function randomInt(from, to){
       return Math.floor(Math.random() * (to - from + 1) + from);
    }

    
    return {
        addAsPlugin: addAsPlugin,
        setSearchData: setSearchData,
        refreshSearchData: refreshSearchData,
        changeUsername: changeUsername,
        createTab: createTab
    };
}());
