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
Opencast.clipshow_ui_Plugin = (function ()
{
    // The Templates to process
    var templateClipshow = '{for c in clips}' +
                                '<div '+
                                      'class="segment-holder ui-widget ui-widget-content" ' +
                                      'id="clip${c.id}" ' +
                                      'style="width: ${(c.stop - c.start) * pps}px; height: 22px;"' +
																			'onclick="Opencast.Player.doSeekToClip(${c.id});">' +
                                      '<p>&nbsp;</p>' +
                                 '</div>' +
                             '{/for}';

    // The Elements to put the div into
    var elementClipshow;
    
    // The data to display
    var clipshow_ui_data;
        
    // Precessed Data
    var processedClipshowdata;

    //The clipshow id
    var clipshowId;

    function getCurrentClipshowId() {
      return clipshowId;
    }

		function setClipshowData(clipshow) {
			clipshow_ui_data = clipshow;
		}

    function getClipshow(id) {
      if (id == 'x') {
        return;
      }

      clipshowId = id;

      $.ajax({
            type: "GET",
            url: "../../clipshow/get?clipshowId=" + clipshowId  + "&mediapackageId=" + Opencast.Player.getMediaPackageId(),
            dataType: 'json',
            success: function(json) {
              
              var clipshowSource = json.clipshow;
              var clips = json.clipshow.clips;

              if (clips.length == 0) {
                return;
              }

							Opencast.Player.doPause();

              $("#oc_btn-vote-funny").removeAttr("checked");
              $("#oc_btn-vote-good").removeAttr("checked");
              $("#oc_btn-vote-dislike").removeAttr("checked");
              $("#oc_votes").buttonset("refresh");

              if (clipshowSource.author.id != Opencast.clipshow_ui.getUserId()) {
                $("#oc_votes").show();
                $.ajax({
                  type: "GET",
                  url: "../../clipshow/vote/mine",
                  data: 'clipshowId=' + clipshowId,
                  dataType: 'json',
                  success: function(json) {
                    //{"properties-response":{"properties":{"item":[{"key":"GOOD","value":0},{"key":"DISLIKE","value":0},{"key":"FUNNY","value":1}]}}}
                    var types = json["properties-response"].properties.item;
                    for (var i = 0; i < types.length; i++) {
                      if (types[i].key == "GOOD" && types[i].value == 1) {
                        $("#oc_btn-vote-good").attr("checked", true);
                      } else if (types[i].key == "FUNNY" && types[i].value == 1) {
                        $("#oc_btn-vote-funny").attr("checked", true);
                      } else if (types[i].key == "DISLIKE" && types[i].value == 1) {
                        $("#oc_btn-vote-dislike").attr("checked", true);
                        $("#oc_btn-vote-funny").removeAttr("checked");
                        $("#oc_btn-vote-good").removeAttr("checked");
                      }
                    }
                    $("#oc_votes").buttonset("refresh");
                  }
                });
              } else {
                $("#oc_votes").hide();
              }

              //Ensure the clipshow is an array, even if there's only one clip
              if (!$.isArray(clips)) {
                clips = [clips];
              }

							Opencast.Player.setClips(clips);
              setClipshowData({clips: clips});
              Opencast.clipshow_ui_Plugin.draw();
            }
          });
    }

    /**
     * @memberOf Opencast.clipshow_ui_Plugin
     * @description Add As Plug-in
     * @param elemClipshow The element to display the clipshow in
     * @param withClipshow boolean True to display the clipshow, false otherwise
     */
    function addAsPlugin(elemClipshow) {
        elementClipshow = elemClipshow;

        draw();
    }

    /**
     * @memberOf Opencast.clipshow_ui_Plugin
     * @description Processes the Data and puts it into the Element
     * @param withClipshow true if process with Clips, false if without Clips
     */
    function draw() {
        // Process Element Segments 1
        if ((elementClipshow !== undefined) && (clipshow_ui_data !== undefined) && (clipshow_ui_data.clips.length > 0)) {
            clipshow_ui_data.pps = Opencast.Player.getPixelsPerSecond();
            processedClipshowData = templateClipshow.process(clipshow_ui_data);
            elementClipshow.html(processedClipshowData);
						for (var i = 0; i < clipshow_ui_data.clips.length; i++) {
							var clip = clipshow_ui_data.clips[i];
							var clipId = "#clip" + clip.id;
							$(clipId).position({
								of: $("#clipshowtable"),
								my: "left top",
								at: "left top",
								offset: (clip.start * clipshow_ui_data.pps) + " 0",
								collision: "none none"
							});
						}
            $.log("Clipshow UI Plugin: Clipshow initialized");
        }
    }

    return {
        addAsPlugin: addAsPlugin,
        getClipshow: getClipshow,
        getCurrentClipshowId: getCurrentClipshowId,
        draw: draw
    };
}());
