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
 * @namespace the global Opencast namespace clipshow_ui
 */
Opencast.clipshow_ui = (function ()
{
    var dateIn;
    var clipshowDisplayed = false,
        clipshowEditorDisplayed = false,
        clipshow = [ ],
        resizeEndTimeoutRunning = false,
        waitForMove = 150;
    var clipshowList;
    var clipshowDropdownRefreshInterval;
    // User name and id
    var userCredentials;


    /**
     * @memberOf Opencast.segments_ui
     * @description Initializes the segments ui view
     */
    function initialize()
    {
        if (Opencast.Player.getMediaPackageId() == undefined) {
          setTimeout("Opencast.clipshow_ui.initialize()", 250);
          return;
        }
        $("#oc_button-clipshowEditorAdd").button();
        $("#oc_button-clipshowEditorAdd").click(function ()
        {
            Opencast.clipshow_editor_ui_Plugin.createClipshowEditorElement();
        });

        $("#oc_button-clipshowSave").button();
        $("#oc_button-clipshowSave").click(function ()
        {
          Opencast.clipshow_editor_ui_Plugin.saveDialog();
        });
        $("#oc_select-clipshow").change(function() {
          var id = $("#oc_select-clipshow option:selected").val();
          Opencast.clipshow_ui_Plugin.getClipshow(id);
        });
        $("#oc_votes").buttonset();
        $(".clipshow_votes").click(function() {
          if (($(this).attr("id") == 'oc_btn-vote-funny' || $(this).attr("id") == 'oc_btn-vote-good') && $(this).attr("checked")) {
            $("#oc_btn-vote-dislike").removeAttr("checked");
          } else if (($(this).attr("id") == 'oc_btn-vote-dislike') && $(this).attr("checked")) {
            $("#oc_btn-vote-funny").removeAttr("checked");
            $("#oc_btn-vote-good").removeAttr("checked");
          }

          if ($("#oc_btn-vote-dislike").attr("checked")) {
            postVotes("DISLIKE");
          } else if ($("#oc_btn-vote-funny").attr("checked") && $("#oc_btn-vote-good").attr("checked")) {
            postVotes("FUNNY,GOOD");
          } else if ($("#oc_btn-vote-funny").attr("checked")) {
            postVotes("FUNNY");
          } else if ($("#oc_btn-vote-good").attr("checked")) {
            postVotes("GOOD");
          } else {
            postVotes("NEUTRAL");
          }
          $("#oc_votes").buttonset("refresh");
        });

        updateUserCredentials();
       
        refreshClipshowList();
        window.setInterval(function event() {
            Opencast.clipshow_ui.refreshClipshowList();
        }, 5 * 60 * 1000); //5 minutes
        initResizeEnd();
        hideClipshow();
        hideClipshowEditor();
    }

    function updateUserCredentials() {
        $.ajax(
        {
            type: "GET",
            url: "../../clipshow/user/getName",
            dataType: 'json',
            success: function (json)
            {
              Opencast.clipshow_ui.setUserCredentials(json);
            },
            error: function (a, b, c)
            {

            }
        });    
    }

    function setUserCredentials(json) {
      userCredentials = json;
    }

    function getUserId() {
      return userCredentials.id;
    }

    function getUsername() {
      return userCredentials.displayName;
    }

    function refreshClipshowList() {
      $.ajax({
        type: "GET",
        url: "../../clipshow/list?mediapackageId=" + Opencast.Player.getMediaPackageId(),
        dataType: 'json',
        success: function(json) {
          Opencast.clipshow_ui.setClipshowList(json.wrapper.data);
        }
      });
    }

    function setClipshowList(json) {
      clipshowList = json;
    }

    function getClipshowList() {
      return clipshowList;
    }

    function updateClipshowDropdown() {
      if (clipshowList == undefined) {
        return;
      }
      var selector = $("#oc_select-clipshow");
      selector.empty().append($("<option />").val("x").text("Select Clipshow"));
      //TODO:  Get curent entry and re-select it
      //TODO:  Same issue as above, re: multiple entries are in an array, single one are not
      if ($.isArray(clipshowList)) {
        for (var i = 0; i < clipshowList.length; i++) {
          selector.append($("<option />").val(clipshowList[i].id).text(clipshowList[i].title + " by " + clipshowList[i].author));
        }
      } else {
        selector.append($("<option />").val(clipshowList.id).text(clipshowList.title + " by " + clipshowList.author));
      }
    }

    function postVotes(votes) {
      $.ajax({
        type: "POST",
        url: "../../clipshow/vote/add",
        data: 'clipshowId=' + Opencast.clipshow_ui_Plugin.getCurrentClipshowId() + '&types=' + votes,
        dataType: 'json',
        failure: function(json) {
          //TODO:  undo votes?
        }
      });
    }

    function switchToClipshow(clipshowId)
    {
      if (!clipshowDisplayed) {
        doToggleClipshow();
        $("#oc_checkbox-clipshow").attr("checked", true);
        $("#oc_select-clipshow").val(clipshowId);
      }
      Opencast.clipshow_ui_Plugin.getClipshow(clipshowId);
    }

    function doToggleClipshow()
    {
      if (clipshowDisplayed)
      {
        hideClipshow();
        clipshowDisplayed = false;
      } 
      else
      {
        showClipshow();
        clipshowDisplayed = true;
      }
    }

    function doToggleClipshowEditor()
    {
      if (clipshowEditorDisplayed)
      {
        hideClipshowEditor();
        clipshowEditorDisplayed = false;
      } 
      else
      {
        showClipshowEditor();
        clipshowEditorDisplayed = true;
      }
    }


    function showClipshowEditor() 
    {
      Opencast.clipshow_editor_ui_Plugin.addAsPlugin($('#clipshoweditor'));
      $("#oc_label-save").show();
      $("#oc_label-add").show();
      $("#oc_button-clipshowSave").show();
      $("#oc_button-clipshowEditorAdd").show();
      $('#clipshoweditor').show();
      if (clipshowDisplayed)
      {
        $('#analytics-and-annotations').height("82px");
      }
      else
      {
        $('#analytics-and-annotations').height("55px");
      }
      $("#oc_video-view").height("35px");
    }

    function hideClipshowEditor() 
    {
      $("#oc_label-add").hide();
      $("#oc_label-save").hide();
      $("#oc_button-clipshowSave").hide();
      $("#oc_button-clipshowEditorAdd").hide();
      $('#clipshoweditor').hide();
      if (clipshowDisplayed)
      {
        $('#analytics-and-annotations').height("82px");
      }
      else
      {
        $('#analytics-and-annotations').css({'height': ''});
      }
      $("#oc_video-view").css({'height': ''});
    }

    function showClipshow()
     {
      $('#clipshowtable').show();
      $("#oc_label-select").show();
      $("#oc_select-clipshow").show();

      Opencast.clipshow_ui_Plugin.addAsPlugin($('#clipshowtable'));
      if (clipshowEditorDisplayed)
      {
        $('#analytics-and-annotations').height("82px");
      }
      else
      {
        $('#analytics-and-annotations').height("55px");
      }
      updateClipshowDropdown();
      clipshowDropdownRefreshInterval = window.setInterval(function event() {
            Opencast.clipshow_ui.updateClipshowDropdown();
        }, 5 * 60 * 1000); //5 minutes
    }

    function hideClipshow() 
    {
      $('#clipshowtable').hide();
      $("#oc_label-select").hide();
      $("#oc_select-clipshow").hide();
      $("#oc_votes").hide();
      if (clipshowEditorDisplayed)
      {
        $('#analytics-and-annotations').height("55px");
      }
      else
      {
        $('#analytics-and-annotations').css({'height': ''});
      }
      clearInterval(clipshowDropdownRefreshInterval);
    }
        
    /**
     * @memberOf Opencast.Analytics
     * @description Binds the Window-Resize-Event
     */
    function initResizeEnd()
    {
        if(!resizeEndTimeoutRunning)
        {
          $(window).resize(function ()
          {
              dateIn = new Date();
              if (clipshowEditorDisplayed)
              {
                  if (resizeEndTimeoutRunning === false)
                  {
                      resizeEndTimeoutRunning = setTimeout(editorResizeEnd, waitForMove);
                  }
              }
          });
        }
        // If window has been resized
        if(resizeEndTimeoutRunning == true)
        {
            resizeEndTimeoutRunning = false;
            Opencast.clipshow_ui_Plugin.draw();
            Opencast.clipshow_editor_ui_Plugin.redraw();
        }
    }

    function editorResizeEnd()
    {
        var dateOut = new Date();
        // if the Resize-Event is not over yet: set new timeout
        if ((dateOut - dateIn) < waitForMove)
        {
            setTimeout(editorResizeEnd, waitForMove);
        }
        else
        {
            resizeEndTimeoutRunning = true;
            initResizeEnd();
        }
    }
    
    return {
        initialize: initialize,
        doToggleClipshow: doToggleClipshow,
        doToggleClipshowEditor: doToggleClipshowEditor,
        switchToClipshow: switchToClipshow,
        setClipshowList: setClipshowList,
        getClipshowList: getClipshowList,
        refreshClipshowList: refreshClipshowList,
        updateUserCredentials: updateUserCredentials,
        setUserCredentials: setUserCredentials,
        getUserId: getUserId,
        getUsername: getUsername,
        updateClipshowDropdown: updateClipshowDropdown
    };
}());
