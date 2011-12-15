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
 * @namespace the global Opencast namespace segments_ui_Plugin
 */
Opencast.clipshow_editor_ui_Plugin = (function ()
{

    var minClipSize = 1;

    var templateComponent = '{for c in clips}' +
                                '<div '+
                                      'class="clipshow-editor-component segment-holder ui-widget ui-widget-content" ' +
                                      'id="${c.id}" ' +
                                      'style="position: absolute; left: ${c.start * pps}px; width: ${(c.stop - c.start) * pps}px;">' +
                                      '<p>&nbsp;</p>' +
                                 '</div>' +
                             '{/for}';
    var templateOrdering = '{for o in order}' +
                                '<div '+
                                      'class="clipshow-editor-component clipshow-order-component segment-holder ui-widget ui-widget-content" ' +
                                      'id="${o.id}" ' +
                                      'style="width: ${width}px; margin-right: 2px;">' +
                                      '<p>${o.clip}</p>' +
                                 '</div>' +
                             '{/for}';

    // The number of clips created so far
    var counter = 0;

    // The current positions (in seconds) of the start and stop points for each segment
    var currentPositions = {};

    // The Elements to put the div into
    var elementClipshowEditor;
    
    /**
     * @memberOf Opencast.clipshow_editor_ui_Plugin
     * @description Add As Plug-in
     * @param elemClipshowEditor The element to display the clipshow editor in
     */
    function addAsPlugin(elemClipshowEditor) {
        elementClipshowEditor = elemClipshowEditor;
        if (currentPositions.clips == undefined) {
          currentPositions.clips = {};
        }
    }
  
    function updatePosition(event, ui) {
      var start = ui.helper.position().left;
      var stop = ui.helper.width() + start;
      var pps = Opencast.Player.getPixelsPerSecond();
      currentPositions["clips"][ui.helper.context.id]["start"] = Math.round(start / pps);
      currentPositions["clips"][ui.helper.context.id]["stop"] = Math.round(stop / pps);
    }

    /**
     * @memberOf Opencast.clipshow_ui_Plugin
     * @description Processes the Data and puts it into the Element
     */
    function createClipshowEditorElement() {
        var id = "edit-clip" + counter;
        currentPositions["clips"][id] = { "id": id, "stop": Math.round(Opencast.Player.getCurrentPosition()) };
        if (Math.round(currentPositions["clips"][id]["stop"] - 60 < 0))
        {
          currentPositions["clips"][id]["start"] = 0;
        }
        else
        {
          currentPositions["clips"][id]["start"] = currentPositions["clips"][id]["stop"] - 60;
        }
        counter++;
        redraw();
    }

    function redraw() 
    {
        var pps = Opencast.Player.getPixelsPerSecond();
        currentPositions["pps"] = pps;
        var processedClipshowData = templateComponent.process(currentPositions);
        elementClipshowEditor.html(processedClipshowData);
        var clipSize = minClipSize * pps;
        for (var i = 0; i < counter; i++)
        {
          $("#edit-clip" + i).resizable({handles: 'e,w', containment: elementClipshowEditor, grid: clipSize, minwidth: clipSize, stop: Opencast.clipshow_editor_ui_Plugin.updatePosition});
          $("#edit-clip" + i).draggable({containment: elementClipshowEditor, grid: [ clipSize, clipSize ], axis: 'x', stop: Opencast.clipshow_editor_ui_Plugin.updatePosition});
          $("#edit-clip" + i).sortable({revert: true});
        }
    }

    function saveDialog() {
      if (counter > 0) {
        var playbackPositions = [];
        $("#oc_clipshow-dialog").dialog("open");
				currentPositions["pps"] = $('#oc-clipshow-ordering-source').width() / Opencast.Player.getDuration();
			  var processedData = templateComponent.process(currentPositions);
			  $("#oc-clipshow-ordering-source").html(processedData);

		    $("#oc-clipshow-ordering-dest").sortable({
		      stop: function(event, ui) {
		        //New items
		        if (!ui.item.data('tag')) {
		          ui.item.data('tag', true);
		          ui.item.children()[0].innerHTML = ui.item.attr("id");
		        } else { //Removing old items
		          if (ui.position.left + ui.item.width() < $(this).position().left || 
	              ui.position.left > $(this).position().left + $(this).width() ||
	              ui.position.top + ui.item.height() < $(this).position().top ||
	              ui.position.top > $(this).position().top + $(this).height()) {
	              ui.item.remove();
	            }
	          }
		      },
		      receive: function(event, ui) {
		        var children = $("#oc-clipshow-ordering-dest > div.clipshow-editor-component");
		        var width = Math.min(1/children.length * 0.90 * $("#oc-clipshow-ordering-dest").width(), 120);
		        $.each(children, function() {
		          $(this).attr("style", "width: " + width + "px; margin-right: 2px;");
		        });
		      }
		    });

			  $("#oc-clipshow-ordering-source > div.clipshow-editor-component").draggable({
          revert: "true",
          containment: "#oc-clipshow-ordering-container",
          connectToSortable: "#oc-clipshow-ordering-dest",
          helper: "clone",
          cursor: "move"
        });

  	    $("#oc-clipshow-ordering-source").disableSelection();
  	    $("#oc-clipshow-ordering-dest").disableSelection();

        $('#oc-input-series-name-select').autocomplete({
          source: function(request, response) {
            $.ajax({
              url: '../../clipshow/series/list',
              dataType: 'json',
              type: 'GET',
              success: function(data) {
                var series_list = [];
                data = data.wrapper;
                $.each(data, function(){
                  series_list.push({
                    value: this.title,
                    id: this.id
                  });
                });
                response(series_list);
              }, 
              error: function() {
                ocUtils.log('could not retrieve clipshow series data');
              }
            });
          },
          select: function(event, ui){
            $('#oc-input-series-name').val(ui.item.id);
          },
          search: function(){
            $('#oc-input-series-name').val('');
          }
        });
      }
    }

    function saveClipshow(title, series, tags, allowedUsers) {
      var clips = [];
      $.each($("#oc-clipshow-ordering-dest").sortable('toArray'), function(index, value) { 
        clips.push(currentPositions.clips[value]);
      });

      $.ajax(
      {
          type: "POST",
          url: "../../clipshow/create",
          data: JSON.stringify({title: title, mediapackageId: Opencast.Player.getMediaPackageId(), clips: clips, series: series, tags: tags, allowedUsers: allowedUsers}),
          contentType: 'application/json; charset=utf-8',
          dataType: 'json',
          success: function (json)
          {
              Opencast.clipshow_ui.refreshClipshowList();
              Opencast.clipshow_ui.updateClipshowDropdown();
          },
          error: function (a, b, c)
          {
              // Some error while adding the clipshow
          }
      });
    }

    function createSeries(title) {
        $.ajax(
        {
            type: "POST",
            url: "../../clipshow/series/create",
            data: JSON.stringify({seriesName: title}),
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success: function (json)
            {
                // Do nothing, the event has been saved
            },
            error: function (a, b, c)
            {
                // Some error while adding the series
            }
        });
    }

    function addClipshowToSeries(clipshowId, seriesId) {
        $.ajax(
        {
            type: "POST",
            url: "../../clipshow/series/add",
            data: JSON.stringify({clipshowId: clipshowId, seriesId: seriesId}),
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success: function (json)
            {
                // Do nothing, the event has been saved
            },
            error: function (a, b, c)
            {
                // Some error while adding the series
            }
        });
    }

    return {
        addAsPlugin: addAsPlugin,
        updatePosition: updatePosition,
        redraw: redraw,
        createClipshowEditorElement: createClipshowEditorElement,
        saveDialog: saveDialog,
        saveClipshow: saveClipshow
    };
}());
