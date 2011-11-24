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
    var template =  '<div style="float: left; width: 20%">' + 
                        '<form id="oc-form-name"><div style="float: left;">' +
                        '<span>Your Name:&nbsp;<input id="oc-form-username" type="text" name="newName" value="${displayName}" style="width: 150px;"/></span>' +
                        '<input id="oc-input-username" type="submit" value="Change Name" />' +
                        '</div></form>' +
                    '</div>' +
                    '<div style="float: left; width: 40%">' +
                        '<p>Top five <select id="oc-select-sort-type">' +
                        '<option value="GOOD">good</option>' + 
                        '<option value="FUNNY">funny</option>' +
                        '<option value="DISLIKE">disliked</option>' +
                        '</select>clipshows in for this episode:</p>' +
                        '<div id="oc_clipshow-sorted-list">' +
                          '<ol>' +
                            '{for c in sorted}' +
                              '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${c.id})">${c.title} by ${c.author}</button></li>' +
                            '{/for}' +
                          '</ol>' +
                        '</div>' +
                    '</div>' +
                    '<div style="float: left; width: 40%">' +
                        'Other clipshows in for this episode:<br />' +
                        '<div id="oc_clipshow-random-list">' +
                          '<ol>' +
                            '{for r in randoms}' +
                              '<li><button class="clipshow-link-button" onclick="Opencast.clipshow_ui.switchToClipshow(${r.id})">${r.title} by ${r.author}</button></li>' +
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
    // Username
    var username;
    
    /**
     * @memberOf Opencast.User_Plugin
     * @description Add As Plug-in
     * @param elem Element to fill with the Data (e.g. a div)
     * @param data The data to fill the tab with
    */
    function addAsPlugin(elem)
    {
        element = elem;

        $.ajax(
        {
            type: "GET",
            url: "../../clipshow/user/getName",
            dataType: 'text',
            success: function (text)
            {
                Opencast.User_Plugin.createTab(text, "GOOD");
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
     * @param uname The current user's display name
     * @return true if successfully processed, false else
     */
    function createTab(uname, selectType)
    {
        username = uname;
        user_data = {displayName: username};
        if ((element !== undefined) && (user_data !== undefined))
        {
            var clist = Opencast.clipshow_ui.getClipshowList();
            if ($.isArray(clist)) {
              //TODO:  Better sort metrics, natural ordering is too strong (dislike being high, etc)
              if (selectType == "GOOD") {
                clist.sort(function(a,b) { return parseInt(b.good) - parseInt(a.good) } );
              } else if (selectType == "FUNNY") {
                clist.sort(function(a,b) { return parseInt(b.funny) - parseInt(a.funny) } );
              } else if (selectType == "DISLIKE") {
                clist.sort(function(a,b) { return parseInt(b.dislike) - parseInt(a.dislike) } );
              }
              sorted = []
              randomShows = []
              //Fill the sorted list
              for (var i = 0; i < 5; i++) {
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
              //TODO: Handle things when there is no array
            }
            $.log("User Plugin: Data available, processing template");
            processedTemplateData = template.process(user_data);
            element.html(processedTemplateData);
            $(".clipshow-link-button").button();

            $('#oc-select-sort-type').val(selectType);
            $("#oc-select-sort-type").change(function() {
              var type = $("#oc-select-sort-type option:selected").val();
              Opencast.User_Plugin.createTab(username, type);
            });

            $('#oc-input-username').button();
            $('#oc-form-name').submit(function ()
            {
              $.ajax(
              {
                  type: "POST",
                  url: "../../clipshow/user/newName",
                  data: {newName: $('#oc-form-username').val()},
                  dataType: 'json',
                  success: function (json)
                  {
                      // Do nothing, the name has been saved
                  },
                  error: function (a, b, c)
                  {
                      $('#oc-form-username').val() = "Error";
                  }
              });
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

    function randomInt(from, to){
       return Math.floor(Math.random() * (to - from + 1) + from);
    }

    
    return {
        addAsPlugin: addAsPlugin,
        createTab: createTab
    };
}());
