/**
 * Copyright 2009-2011 The Regents of the University of California Licensed
 * under the Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

var editor = editor || {};

var SMIL_RESTSERVICE = "/smil/";
var SMIL_FLAVOR = "smil/smil";
var WAVEFORM_FLAVOR = "image/waveform";

var ME_JSON = "/info/me.json";

var PREVIOUS_FRAME = "trim.previous_frame";
var NEXT_FRAME = "trim.next_frame";
var SPLIT_AT_CURRENT_TIME = "trim.split_at_current_time";
var PLAY_CURRENT_SEGMENT = "trim.play_current_segment";
var PLAY_CURRENT_PRE_POST = "trim.play_current_pre_post";
var SET_CURRENT_TIME_AS_INPOINT = "trim.set_current_time_as_inpoint";
var SET_CURRENT_TIME_AS_OUTPOINT = "trim.set_current_time_as_outpoint";
var PLAY_PAUSE = "trim.play_pause";
var SELECT_ITEM_AT_CURRENT_TIME = "trim.select_item_at_current_time";
var DELETE_SELECTED_ITEM = "trim.delete_selected_segment";
var NEXT_MARKER = "trim.next_marker";
var PREVIOUS_MARKER = "trim.previous_marker";
var PLAY_ENDING_OF_CURRENT_SEGMENT = "trim.play_ending_of_current_segment";

var default_config = {};
default_config[PREVIOUS_FRAME] = "left";
default_config[NEXT_FRAME] = "right";
default_config[SPLIT_AT_CURRENT_TIME] = "v";
default_config[PLAY_CURRENT_SEGMENT] = "c";
default_config[PLAY_CURRENT_PRE_POST] = "Shift+c";
default_config[SET_CURRENT_TIME_AS_INPOINT] = "i";
default_config[SET_CURRENT_TIME_AS_OUTPOINT] = "o";
default_config[PLAY_PAUSE] = "space";
default_config[SELECT_ITEM_AT_CURRENT_TIME] = "y";
default_config[DELETE_SELECTED_ITEM] = "Delete";
default_config[PREVIOUS_MARKER] = "Up";
default_config[NEXT_MARKER] = "Down";
default_config[PLAY_ENDING_OF_CURRENT_SEGMENT] = "n";

var timeout = null;
var endTime = 0;

editor.splitData = {};
editor.splitData.splits = [];
editor.selectedSplit = null;
editor.player = null;
editor.smil = null;
editor.canvas = null;
editor.ready = false;

/**
 * update split list in UI
 */
editor.updateSplitList = function() {
  cancelButtonClick();
  $('#leftBox').html($('#splitElements').jqote(editor.splitData));
  $('#splitSegments').html($('#splitSegmentTemplate').jqote(editor.splitData));
  $('.splitItemDiv').click(splitItemClick);
  $('.splitSegmentItem').click(splitItemClick);
  // $('.splitRemover').click(splitRemoverClick);

  $('.splitRemoverLink').button({
    text : false,
    icons : {
      primary : "ui-icon-trash"
    }
  });

  $('.splitAdderLink').button({
    text : false,
    icons : {
      primary : "ui-icon-arrowreturnthick-1-w"
    }
  });

  $('.splitRemoverLink').click(splitRemoverClick);
  $('.splitAdderLink').click(splitRemoverClick);

  $('.splitSegmentItem').dblclick(jumpToSegment);
  $('.splitItemDiv').dblclick(jumpToSegment);

  $('.splitSegmentItem').hover(splitHoverIn, splitHoverOut);
  $('.splitItemDiv').hover(splitHoverIn, splitHoverOut);

}

/**
 * create a new smil for the current workflow should already be handled by
 * workflowoperationhandler
 */
editor.createSMIL = function() {
  $.ajax({
    url : SMIL_RESTSERVICE + "new",
    data : {
      workflowId : workflowInstance.id
    }
  });
}

/**
 * clear the content of the smil
 */
editor.clearSMIL = function() {
  $.ajax({
    url : SMIL_RESTSERVICE + "clear/" + workflowInstance.id,
    async : false
  });
}

/**
 * save the content to smil file
 */
editor.saveSplitList = function() {
  editor.clearSMIL();
  $.each(editor.splitData.splits, function(key, value) {
    if (value.enabled) {
      parallelId = editor.addParallel();
      $.each(workflowInstance.mediapackage.media.track, function(key, track) {
        if (track.type.indexOf("work") != -1) {
          value.src = track.url;
          value.mhElement = track.id;
          editor.addMediaElement(parallelId, value);
        }
      });
    }
  });
}

/**
 * add a media element to the smil
 * 
 * @param parallelId
 *          the id of the parallelElement this MediaElement should be added to
 * @param data
 *          the data of this MediaElement
 */
editor.addMediaElement = function(parallelId, data) {
  $.ajax({
    url : SMIL_RESTSERVICE + "addMediaElement/" + workflowInstance.id + "/" + parallelId,
    data : data,
    async : false,
    success : function(data) {
    }
  })
}

/**
 * add a ParallelElement to the SMIL
 */
editor.addParallel = function() {
  var parallelId = "";
  $.ajax({
    url : SMIL_RESTSERVICE + "addParallel/" + workflowInstance.id,
    success : function(data) {
      parallelId = data;
    },
    async : false
  });
  return parallelId;
}

/**
 * download the SMIL if there is already one e.g. from silence detection
 */
editor.downloadSMIL = function() {
  var smil = null;
  $.ajax({
    url : SMIL_RESTSERVICE + "get/" + workflowInstance.id,
    data : {
      format : "json"
    },
    dataType : "json",
    async : false,
    success : function(data) {
      smil = data;
    }
  });
  return smil;
}

/**
 * add all shortcuts
 */
function addShortcuts() {
  $.ajax({
    url : ME_JSON,
    dataType : "json",
    async : false,
    success : function(data) {
      $.each(data.org.properties, function(key, value) {
        default_config[key] = value;
        $('#' + key.replace(".", "_")).html(value);
      });
    }
  });
  
  $.each(default_config, function(key, value) {
    $('#' + key.replace(".", "_")).html(value);
  });

  // add shortcuts for easier editing
  shortcut.add(default_config[SPLIT_AT_CURRENT_TIME], splitButtonClick, {
    disable_in_input : true,
  });
  shortcut.add(default_config[PREVIOUS_FRAME], function() {
    pauseVideo();
    $('.video-prev-frame').click();
  }, {
    disable_in_input : true,
  });
  shortcut.add(default_config[NEXT_FRAME], function() {
    pauseVideo();
    $('.video-next-frame').click();
  }, {
    disable_in_input : true,
  });
  shortcut.add(default_config[PLAY_PAUSE], function() {
    if (editor.player.prop("paused")) {
      editor.player[0].play();
    } else {
      editor.player[0].pause();
    }
  }, {
    disable_in_input : true,
  });
  shortcut.add(default_config[PLAY_CURRENT_SEGMENT], playCurrentSplitItem, {
    disable_in_input : true,
  });
  shortcut.add(default_config[DELETE_SELECTED_ITEM], splitRemoverClick, {
    disable_in_input : true,
  });
  shortcut.add(default_config[SELECT_ITEM_AT_CURRENT_TIME], selectCurrentSplitItem, {
    disable_in_input : true,
  });
  shortcut.add(default_config[SET_CURRENT_TIME_AS_INPOINT], setCurrentTimeAsNewInpoint, {
    disable_in_input : true,
  });
  shortcut.add(default_config[SET_CURRENT_TIME_AS_OUTPOINT], setCurrentTimeAsNewOutpoint, {
    disable_in_input : true,
  });
  shortcut.add(default_config[NEXT_MARKER], nextSegment, {
    disable_in_input : true,
  });
  shortcut.add(default_config[PREVIOUS_MARKER], previousSegment, {
    disable_in_input : true,
  });
  shortcut.add(default_config[PLAY_ENDING_OF_CURRENT_SEGMENT], playEnding, {
    disable_in_input : true,
  });
  shortcut.add(default_config[PLAY_CURRENT_PRE_POST], playWithoutDeleted, {
    disable_in_input : true
  });
}

/**
 * play last 2 seconds of the current segment
 */
function playEnding() {
  var split = getCurrentSplitItem();
  if (split != null) {
    clipEnd = split.clipEnd;
    editor.player.prop("currentTime", clipEnd - 2);
    editor.player.on("play", {
      duration : 2000,
      endTime : clipEnd
    }, onPlay);
    editor.player[0].play();
  }
}

/**
 * play from current playhead -2s exclude the removed segments
 */
function playWithoutDeleted() {
  editor.player[0].pause();
  currentTime = editor.player.prop("currentTime");
  currentSplit = getCurrentSplitItem();
  if(!currentSplit.enabled) {
    currentSplit = editor.splitData.splits[currentSplit.id - 1];
  }
  startTime = currentTime - 2;
  nextSplit = editor.splitData.splits[currentSplit.id + 1]
  nextStart = parseFloat(nextSplit.clipBegin.replace("s", ""));

  // set current time -2s for pre roll
  editor.player.prop("currentTime", startTime);

  // if next split is disabled jump over it
  if (!nextSplit.enabled) {
    ocUtils.log("starting jump over");
    editor.player.on("play", {
      duration : (nextStart - startTime) * 1000,
      endTime : nextStart
    }, onPlay);
    editor.player.on("pause", function(evt) {
      editor.player.off("pause");
      editor.player.prop("currentTime", nextSplit.clipEnd.replace("s", ""));
      editor.player.on("play", {
        duration : 2000,
        endTime : parseFloat(nextSplit.clipEnd.replace("s", "")) + 2
      }, onPlay);
    });
    editor.player[0].play();
  } else {
    editor.player.on("play", {
      duration : 4000,
      endTime : currentTime + 2
    }, onPlay);
    editor.player[0].play();
  }
}

/**
 * jump to beginning of current split item
 */
function jumpToSegment() {
  id = $(this).prop('id');
  id = id.replace('splitItem-', '');
  id = id.replace('splitItemDiv-', '');
  id = id.replace('splitSegmentItem-', '');

  editor.player.prop("currentTime", editor.splitData.splits[id].clipBegin.replace("s", ""));
}

/**
 * jump to next segment
 */
function nextSegment() {
  split = getCurrentSplitItem();
  if (split != null) {
    new_id = split.id + 1;
    if (new_id <= editor.splitData.splits.length - 1) {
      editor.player.prop("currentTime", editor.splitData.splits[new_id].clipBegin.replace("s", ""));
    }
  }
}

/**
 * jump to previous segment
 */
function previousSegment() {
  split = getCurrentSplitItem();
  new_id = split.id - 1;
  if (new_id >= 0) {
    editor.player.prop("currentTime", editor.splitData.splits[new_id].clipBegin.replace("s", ""));
  }
}

/**
 * handler for hover in events on split segements and -list
 * 
 * @param evt
 *          the corresponding event
 */
function splitHoverIn(evt) {
  id = $(this).prop('id');
  id = id.replace('splitItem-', '');
  id = id.replace('splitItemDiv-', '');
  id = id.replace('splitSegmentItem-', '');

  $('#splitItem-' + id).addClass('hover');
  if (!$('#splitSegmentItem-' + id).hasClass("splitSegmentItemSelected")) {
    $('#splitSegmentItem-' + id).addClass('hover hoverOpacity');
  }
}

/**
 * handler for hover out events on split segements and -list
 * 
 * @param evt
 *          the corresponding event
 */
function splitHoverOut(evt) {
  id = $(this).prop('id');
  id = id.replace('splitItem-', '');
  id = id.replace('splitItemDiv-', '');
  id = id.replace('splitSegmentItem-', '');

  $('#splitItem-' + id).removeClass('hover');
  $('#splitSegmentItem-' + id).removeClass('hover hoverOpacity');
}

/**
 * init the playbuttons in the editing box
 */
function initPlayButtons() {
  $('#clipBeginSet, #clipEndSet').button({
    text : false,
    icons : {
      primary : "ui-icon-arrowthickstop-1-s"
    }
  });

  $('#clipBeginSet').click(setCurrentTimeAsNewInpoint);
  $('#clipEndSet').click(setCurrentTimeAsNewOutpoint);

  $('#shortcuts').button();

  $('#shortcuts').click(function() {
    $('#shortcutsDialog').dialog({
      title : "shortcuts for video editing",
      resizable : false,
      buttons : {
        Close : function() {
          $(this).dialog("close");
        }
      }
    })
  });

  $('#clearList').button();

  $('#clearList').click(function() {
    editor.splitData.splits = [];
    // create standard split point
    editor.splitData.splits.push({
      clipBegin : '0s',
      clipEnd : workflowInstance.mediapackage.duration / 1000 + "s",
      enabled : true,
      description : ""
    });
    editor.updateSplitList();
  });

}

/**
 * click handler for saving data in editing box
 */
function okButtonClick() {
  id = $('#splitUUID').val();
  if (id != "") {
    id = parseInt(id);
    if (parseFloat($('#clipBegin').timefield('option', 'value').replace("s", "")) > parseFloat($('#clipEnd').timefield(
        'option', 'value').replace("s", ""))) {
      $('<div />').html("The inpoint is bigger than the outpoint. Please check.").dialog({
        title : "Check in and outpoint",
        resizable : false,
        buttons : {
          OK : function() {
            $(this).dialog("close");
          }
        }
      });
      return;
    }
    splitItem = editor.splitData.splits[id];
    splitItem.description = $('#splitDescription').val();
    splitItem.clipBegin = $('#clipBegin').timefield('option', 'value');
    splitItem.clipEnd = $('#clipEnd').timefield('option', 'value');

    checkPrevAndNext(id);

    cancelButtonClick();
    editor.updateSplitList();
  }
}

function checkPrevAndNext(id) {
  // it's the first check whether we need a new first item
  if (id == 0) {
    if (editor.splitData.splits.length > 1) {
      var next = editor.splitData.splits[id + 1];
      next.clipBegin = splitItem.clipEnd;
    }
    if ($('#clipBegin').timefield('option', 'seconds') != 0) {
      var newSplitItem = {
        description : "",
        clipBegin : "0s",
        enabled : true,
        clipEnd : splitItem.clipBegin
      };

      // add new item to front
      editor.splitData.splits.splice(0, 0, newSplitItem);
    }
  } else if (id == editor.splitData.splits.length - 1) {
    if ($('#clipEnd').timefield('option', 'seconds') != editor.player.prop("duration")) {
      var newLastItem = {
        description : "",
        enabled : true,
        clipBegin : splitItem.clipEnd,
        clipEnd : editor.player.prop("duration") + "s"
      };

      // add the new item to the end
      editor.splitData.splits.push(newLastItem);
    }
    var prev = editor.splitData.splits[id - 1];
    prev.clipEnd = splitItem.clipBegin
  } else {
    var next = editor.splitData.splits[id + 1];
    var prev = editor.splitData.splits[id - 1];

    prev.clipEnd = splitItem.clipBegin;
    next.clipBegin = splitItem.clipEnd;
  }
}

/**
 * click handler for canceling editing
 */
function cancelButtonClick() {
  $('#splitDescription').html('');
  $('#splitUUID').val('');
  $('#splitDescription').val("");
  $('#clipBegin').timefield('option', 'value', 0);
  $('#clipEnd').timefield('option', 'value', 0);
  $('#splitIndex').html('#');
  $('.splitItem').removeClass('splitItemSelected');
  $('.splitSegmentItem').removeClass('splitSegmentItemSelected');
  editor.selectedSplit = null;
  enabledRightBox(false);
}

/**
 * enable/disable the right editing box
 * 
 * @param enabled
 *          whether enabled or not
 */
function enabledRightBox(enabled) {
  if (enabled) {
    $('#rightBox :input').removeProp('disabled');
    $('.frameButton').button("enable");
    $('#descriptionCurrentTimeDiv').show();
  } else {
    $('#rightBox :input').prop('disabled', 'disabled');
    $('.frameButton').button("disable");
    $('#descriptionCurrentTimeDiv').hide();
  }
}

/**
 * select the split at the current time
 */
function selectCurrentSplitItem() {
  var splitItem = getCurrentSplitItem();
  $('#splitSegmentItem-' + splitItem.id).click();
}

/**
 * set current time as the new inpoint of selected item
 */
function setCurrentTimeAsNewInpoint() {
  if (editor.selectedSplit != null) {
    $('#clipBegin').timefield('option', 'value', editor.player.prop("currentTime") + "s");
  }
}

/**
 * set current time as the new outpoint of selected item
 */
function setCurrentTimeAsNewOutpoint() {
  if (editor.selectedSplit != null) {
    $('#clipEnd').timefield('option', 'value', editor.player.prop("currentTime") + "s");
  }
}

/**
 * plays the current split item from it's beginning
 */
function playCurrentSplitItem() {
  var splitItem = getCurrentSplitItem();
  if (splitItem != null) {
    var clipBegin = parseFloat(splitItem.clipBegin.replace("s", ""));
    var clipEnd = parseFloat(splitItem.clipEnd.replace("s", ""));
    var duration = (clipEnd - clipBegin) * 1000;
    editor.player.prop("currentTime", clipBegin);

    editor.player.on("play", {
      duration : duration,
      endTime : clipEnd
    }, onPlay);
    editor.player[0].play();
  }
}

/**
 * function executed when play event was thrown
 * 
 * @param evt
 *          the event
 */
function onPlay(evt) {
  timeout = window.setTimeout(onTimeout, evt.data.duration);
  editor.player.off("play");
  endTime = evt.data.endTime;
}

/**
 * the timeout function pausing the video again
 */
function onTimeout() {
  editor.player[0].pause();
  var check = function() {
    if (endTime > editor.player.prop("currentTime")) {
      editor.player[0].play();
      window.setTimeout(check, 10);
    } else {
      editor.player[0].pause();
    }
  }
  check();
}

/**
 * retrieves the current split item by time
 * 
 * @returns the current split item
 */
function getCurrentSplitItem() {
  var currentTime = editor.player.prop("currentTime");
  for ( var i = 0; i < editor.splitData.splits.length; i++) {
    var splitItem = editor.splitData.splits[i];
    var clipBegin = parseFloat(splitItem.clipBegin.replace("s", ""));
    var clipEnd = parseFloat(splitItem.clipEnd.replace("s", ""));
    if (clipBegin <= currentTime && currentTime < clipEnd) {
      splitItem.id = i;
      return splitItem;
    }
  }
  return null;
}

/**
 * click/shortcut handler for adding a split item at current time
 */
function splitButtonClick() {
  var currentTime = editor.player.prop('currentTime');
  for ( var i = 0; i < editor.splitData.splits.length; i++) {
    var splitItem = editor.splitData.splits[i];
    var clipBegin = parseFloat(splitItem.clipBegin.replace("s", ""));
    var clipEnd = parseFloat(splitItem.clipEnd.replace("s", ""));
    if (clipBegin < currentTime && currentTime < clipEnd) {
      newEnd = "0s";
      if (editor.splitData.splits.length == i + 1) {
        newEnd = splitItem.clipEnd;
      } else {
        newEnd = editor.splitData.splits[i + 1].clipBegin;
      }
      var newItem = {
        clipBegin : currentTime + 's',
        clipEnd : newEnd,
        enabled : true,
        description : ""
      }
      splitItem.clipEnd = currentTime + 's';
      editor.splitData.splits.splice(i + 1, 0, newItem);
      editor.updateSplitList();
      return;
    }
  }
}

/**
 * when player is ready set all neccassary stuff
 */
function playerReady() {
  if (!editor.ready) {
    editor.ready = true;
    // create additional data output
    $('#videoHolder').append('<div id="segmentsWaveform"></div>');
    $('#segmentsWaveform').append('<div id="splitSegments"></div>')
    $('#segmentsWaveform').append('<div id="imageDiv"><img id="waveformImage" alt="waveform"/></div>');
    $('#segmentsWaveform').append('<div id="currentTimeDiv"></div>');

    // paint a green line for current time
    editor.player.bind("timeupdate", function() {
      var duration = workflowInstance.mediapackage.duration / 1000;
      var perc = editor.player.prop("currentTime") / duration * 100;
      $('#currentTimeDiv').css("left", perc + "%");
    });

    // create standard split point
    editor.splitData.splits.push({
      clipBegin : '0s',
      clipEnd : workflowInstance.mediapackage.duration / 1000 + "s",
      enabled : true,
      description : ""
    });

    // load smil if there is already one
    smil = null;
    $.each(workflowInstance.mediapackage.metadata.catalog, function(key, value) {
      if (value.type == SMIL_FLAVOR) {
        // download smil
        smil = editor.downloadSMIL();
        // check whether SMIL has already cutting points
        if (smil.smil.body.seq.par) {
          $('<div/>').html(
              "Found existing SMIL from silence detection. Do you want to transfer the data into the list?").dialog({
            buttons : {
              Yes : function() {
                editor.splitData.splits = [];
                smil.smil.body.seq.par = ocUtils.ensureArray(smil.smil.body.seq.par);
                $.each(smil.smil.body.seq.par, function(key, value) {
                  value.ref = ocUtils.ensureArray(value.ref);
                  editor.splitData.splits.push({
                    clipBegin : value.ref[0].clipBegin,
                    clipEnd : value.ref[0].clipEnd,
                    enabled : true,
                    description : value.ref[0].description ? value.ref[0].description : "",
                  });
                });
                $(this).dialog('close');
                editor.updateSplitList();
              },
              No : function() {
                $(this).dialog('close');
              }
            },
            title : "Apply existent SMIL"
          });
        }
      }
    });
    // create smil if it doesn't exist
    if (smil == null) {
      editor.createSMIL();
    }
    // update split list and enable the editor
    editor.updateSplitList();
    $('#editor').removeClass('disabled');

    // load waveform image
    $.each(ocUtils.ensureArray(workflowInstance.mediapackage.attachments.attachment), function(key, value) {
      if (value.type == WAVEFORM_FLAVOR) {
        $('#waveformImage').prop("src", value.url);
        $('#waveformImage').load(function() {
          $('#segmentsWaveform').height($('#waveformImage').height());
        });
        $(window).resize(function(evt) {
          $('#segmentsWaveform').height($('#waveformImage').height());
          $('.holdStateUI').height($('#segmentsWaveform').height() + $('#videoPlayer').height() + 70);
        });
        // $('#waveformImage').addimagezoom();
        // $('#splitSegments').mouseover(function(evt) {
        // $('#waveformImage').trigger("mouseover");
        // }).mouseout(function(evt) {
        // $('#waveformImage').trigger("mouseout");
        // }).mousemove(function(evt) {
        // $('#waveformImage').trigger("mousemove");
        // })
      }
    });

    // adjust size of the holdState UI
    var height = parseInt($('.holdStateUI').css('height').replace("px", ""));
    var heightVideo = parseInt($('#videoHolder').css('height').replace("px", ""));
    $('.holdStateUI').css('height', (height + heightVideo) + "px");
    parent.ocRecordings.adjustHoldActionPanelHeight();

    // grab the focus in the Iframe so one can use
    // the keyboard shortcuts
    $(window).focus();

    // add click handler for btns in control bar
    $('.video-previous-marker').click(previousSegment);
    $('.video-next-marker').click(nextSegment);
    $('.video-play-pre-post').click(playWithoutDeleted);

    // add timelistener for current time in description div
    editor.player.on("timeupdate", function() {
      $('#descriptionCurrentTime').html(formatTime(editor.player.prop("currentTime")));
    });

    // add evtl handler for enter in editing fields
    $('#clipBegin input').keyup(function(evt) {
      if (evt.keyCode == 13) {
        editor.selectedSplit.clipBegin = $('#clipBegin').timefield('option', 'value');
        checkPrevAndNext(editor.selectedSplit.id);
        editor.updateSplitList();
      }
    });
    $('#clipEnd input').keyup(function(evt) {
      if (evt.keyCode == 13) {
        editor.selectedSplit.clipEnd = $('#clipEnd').timefield('option', 'value');
        checkPrevAndNext(editor.selectedSplit.id);
        editor.updateSplitList();
      }
    });
  }
}

/**
 * clearing events
 */
function clearEvents(evt) {
  window.clearTimeout(timeout);
  editor.player.off("play");
  editor.player.off("seeking");
  editor.player.off("pause");
  console.log(evt);
}

/**
 * pause the video
 */
function pauseVideo() {
  if (!editor.player.prop("paused")) {
    editor.player[0].pause();
  }
}

/**
 * updates the currentTime div
 */
function updateCurrentTime() {
  $('#current_time').html(formatTime(editor.player.prop("currentTime")));
}

/**
 * formating a time String to hh:MM:ss.mm
 * 
 * @param time
 *          the timeString
 * @returns the formated time string
 */
function formatTime(time) {
  if (typeof time == "string") {
    time = time.replace("s", "");
    time = parseFloat(time);
  }
  seconds = parseInt(time);
  millis = parseInt((time - seconds) * 100);
  formatted = ocUtils.formatSeconds(time) + "." + millis;
  return formatted;
}

/**
 * click/shortcut handler for removing current split item
 */
function splitRemoverClick() {
  item = $(this);
  id = item.prop('id');
  if (id != undefined) {
    id = id.replace("splitItem-", "");
    id = id.replace("splitRemover-", "");
    id = id.replace("splitAdder-", "");
  } else {
    id = "";
  }
  if (id == "" || id == "deleteButton") {
    id = $('#splitUUID').val();
  }
  if (editor.splitData.splits[id].enabled) {
    $('#deleteDialog').dialog({
      buttons : {
        "Yes" : function() {
          $('#splitItemDiv-' + id).addClass('disabled');
          $('#splitRemover-' + id).hide();
          $('#splitAdder-' + id).show();
          $('.splitItem').removeClass('splitItemSelected');
          $(this).dialog('close');
          setEnabled(id, false);
        },
        "No" : function() {
          $(this).dialog('close')
        }
      },
      title : "Remove Item?"
    });
  } else {
    $('#splitItemDiv-' + id).removeClass('disabled');
    $('#splitRemover-' + id).show();
    $('#splitAdder-' + id).hide();
    setEnabled(id, true);
  }
  cancelButtonClick();
}

/**
 * enable/disable a split item
 * 
 * @param uuid
 *          the id of the splitItem
 * @param enabled
 *          whether enabled or not
 */
function setEnabled(uuid, enabled) {
  editor.splitData.splits[uuid].enabled = enabled;
  editor.updateSplitList();
}

/**
 * click handler for selecting a split item in segment bar or list
 */
function splitItemClick() {
  // if it's not already disabled
  if (!$(this).hasClass('disabled')) {

    // remove all selected classes
    $('.splitSegmentItem').removeClass('splitSegmentItemSelected');
    $('.splitItem').removeClass('splitItemSelected');

    // get the id of the split item
    id = $(this).prop('id');
    id = id.replace('splitItem-', '');
    id = id.replace('splitItemDiv-', '');
    id = id.replace('splitSegmentItem-', '');

    // add the selected class to the corresponding items
    $('#splitItem-' + id).addClass('splitItemSelected');
    $('#splitSegmentItem-' + id).addClass('splitSegmentItemSelected');

    $('#splitSegmentItem-' + id).removeClass('hover hoverOpacity');

    // load data into right box
    splitItem = editor.splitData.splits[id];
    editor.selectedSplit = splitItem;
    editor.selectedSplit.id = parseInt(id);
    $('#splitDescription').val(splitItem.description);
    $('#splitUUID').val(id);
    $('#clipBegin').timefield('option', 'value', splitItem.clipBegin);
    $('#clipEnd').timefield('option', 'value', splitItem.clipEnd);
    $('#splitIndex').html(parseInt(id) + 1);
    enabledRightBox(true);
  }
}

$(document).ready(function() {
  // waitForPlayerReady();
  editor.player = $('#videoPlayer');
  editor.player.on("canplay", playerReady);
  $('.clipItem').timefield();
  $('.video-split-button').click(splitButtonClick);
  $('#okButton').click(okButtonClick);
  $('#cancelButton').click(cancelButtonClick);
  $('#deleteButton').click(splitRemoverClick);

  enabledRightBox(false);
  initPlayButtons();
  addShortcuts();
})