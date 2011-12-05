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
    var templateClipshow = '<tr>' +
                            '{for c in clips}' +
                                '{if (parseInt(c.duration) > 0)}' +
                                    '<td role="button" class="segment-holder ui-widget ui-widget-content" ' +
                                        'id="clip${c.index}" ' +
                                        '{if (c.spacer)}' +
                                        ' style="width: ${parseInt(c.duration) / parseInt(c.completeDuration) * 100}%;" ' +
                                        '{else}' +
                                        ' style="width: ${parseInt(c.duration) / parseInt(c.completeDuration) * 100}%; background: blue;" ' +
                                        'alt="Clip ${parseInt(c.clip)} of ${parseInt(c.count)}" ' +
                                        'onclick="Opencast.Player.doSeek(${parseInt(c.start)}); Opencast.Player.currentClip = c.clip;" ' +
                                        '{/if}' +
                                     '>' +
                                        '{if (false && !c.spacer)}' +
                                        '<p>Clip ${parseInt(c.clip)}</p>' +
                                        '{/if}' +
                                        '<span class="segments-time" style="display: none">${parseInt(c.start)}</span>' +
                                     '</td>' +
                                 '{/if}' +
                             '{forelse}' +
                                 '<td style="width: 100%;" id="segment-holder-empty" class="segment-holder" />' +
                             '{/for}' +
                             '</tr>';

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

              //TODO:  Fix playback
              Opencast.Player.times = clips;

              var duration = Math.round(Opencast.Player.getDuration());

              //Build the clipshow display data
              var last = 0;
              var counter = 1;
              var clipshowLength = 0;
              var clipshow = [];
              if ($.isArray(clips)) {
                clipshowLength = clips.length;
                clips.sort(function(a,b) {
                  return parseInt(a.stop) - parseInt(b.start);

                  if (b.start > a.stop) {
                    return -1;
                  } else if (b.start < a.stop) {  //Later clip overlaps earlier clip
                    
                  } else if (b.stop > a.start) {  //Earlier clips overlas later clip
                    
                  } else if (a.start <= b.start && a.stop >= b.stop) { //Complete overlap
                    
                  }
                  return parseInt(b.start) - parseInt(a.stop) 
                });
                for (var i = 0; i < clipshowLength; i++) {
                	if (clips[i]["start"] != last) {
                		clipshow.push({"start": last, "stop": clips[i]["start"], "duration": (clips[i]["start"] - last), "completeDuration": duration, "spacer": true, "index": counter});
                		counter++;
              		}
              		clips[i]["index"] = counter;
              		clips[i]["clip"] = i;
              		clips[i]["count"] = clipshowLength;
              		clips[i]["completeDuration"] = duration;
              		clips[i]["duration"] = clips[i]["stop"] - clips[i]["start"];
              		counter++;
              		clipshow.push(clips[i]);
              		last = clips[i]["stop"];
                }

                //Add a final entry to finish off the full length
                //clipshow[clipshow.length-1]["stop"]
                if (last != duration) {
                		clipshow.push({"start": last, "stop": duration, "duration": (duration - last), "completeDuration": duration, "spacer": true, "index": ++counter});
                }
                Opencast.Player.doPause();
                Opencast.Player.doSeek(clips[0]["start"]);
                Opencast.Player.currentClip = 0;
              } else {
                //TODO:  This branch can go away if I can figure out how to get the clipshow to always be an array (re: single element idiocy)
                clipshowLength = 1;
                if (clips["start"] != 0) {
                  clipshow.push({"start": last, "stop": clips["start"], "duration": (clips["start"] - last), "completeDuration": duration, "spacer": true, "index": counter});
                  counter++;
                }
            		clips["index"] = counter;
            		clips["clip"] = i;
            		clips["count"] = clipshowLength;
            		clips["completeDuration"] = duration;
            		clips["duration"] = clips["stop"] - clips["start"];
            		counter++;
            		clipshow.push(clips);
            		last = clips["stop"];
            		
                //Add a final entry to finish off the full length
                if (last != duration) {
                		clipshow.push({"start": last, "stop": duration, "duration": (duration - last), "completeDuration": duration, "spacer": true, "index": ++counter});
                }
                Opencast.Player.doPause();
                Opencast.Player.doSeek(clips["start"]);
                Opencast.Player.currentClip = 0;
              }

              clipshow_ui_data = {clips: clipshow};
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
            processedClipshowData = templateClipshow.process(clipshow_ui_data);
            elementClipshow.html(processedClipshowData);
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
