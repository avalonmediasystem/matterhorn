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

    var minClipSize = 5;

    var templateComponent = '{for c in clips}' +
                                '<div '+
                                      'class="clipshow-editor-component segment-holder ui-widget ui-widget-content" ' +
                                      'id="${c.id}" ' +
                                      'style="position: absolute; top: 0px; left: ${c.start * pps}px; width: ${(c.stop - c.start) * pps}px; height: 22px;">' +
                                      '<p>&nbsp;</p>' +
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
        

/*        //This is the relevant html code
              <input type="text" class="oc-ui-form-field ui-autocomplete-input" name="seriesSelect" id="seriesSelect" maxlength="255" />
              <input type="hidden" id="series" />
        //This is the series selection autocomplete for the scheduler.  This should be adapted to the series selection for the clipshow
        $('#seriesSelect').autocomplete({
          source: function(request, response) {
            $.ajax({
              url: SERIES_URL + '/series.json',
              data: {
                q: request.term,
                sort: 'TITLE'
              },
              dataType: 'json',
              type: 'GET',
              success: function(data) {
                var series_list = [];
                data = data.catalogs;
                $.each(data, function(){
                  series_list.push({
                    value: this[DUBLIN_CORE_NS_URI]['title'][0].value,
                    id: this[DUBLIN_CORE_NS_URI]['identifier'][0].value
                  });
                });
                response(series_list);
              }, 
              error: function() {
                ocUtils.log('could not retrieve series_data');
              }
            });
          },
          select: function(event, ui){
            $('#series').val(ui.item.id);
          },
          search: function(){
            $('#series').val('');
          }
        });
*/
      
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
        $("#oc_clipshow-dialog").dialog("open");
      }
    }

    function saveClipshow(title) {
        $.ajax(
        {
            type: "POST",
            url: "../../clipshow/create",
            data: JSON.stringify({title: title, mediapackageId: Opencast.Player.getMediaPackageId(), clips: currentPositions["clips"]}),
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success: function (xml)
            {
                Opencast.clipshow_ui.refreshClipshowList();
                Opencast.clipshow_ui.updateClipshowDropdown();
                // Do nothing, the event has been saved
            },
            error: function (a, b, c)
            {
                // Some error while adding the event
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
