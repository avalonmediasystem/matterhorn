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

editor.splitData = {};
editor.splitData.splits = [];
editor.player = null;
editor.smil = null;
editor.canvas = null;

editor.updateSplitList = function() {
  $('#leftBox').html($('#splitElements').jqote(editor.splitData));
  $('#splitSegments').html($('#splitSegmentTemplate').jqote(editor.splitData));
  $('.splitItem').click(splitItemClick);
  $('.splitRemover').click(splitRemoverClick);
}

editor.createSMIL = function() {
  $.ajax({
    url : SMIL_RESTSERVICE + "new",
    data : {
      workflowId : workflowInstance.id
    }
  });
}

editor.clearSMIL = function() {
  $.ajax({
    url : SMIL_RESTSERVICE + "clear/" + workflowInstance.id,
    async : false
  });
}

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

editor.addMediaElement = function(parallelId, data) {
  $.ajax({
    url : SMIL_RESTSERVICE + "addMediaElement/" + workflowInstance.id + "/" + parallelId,
    data : data,
    async : false,
    success : function(data) {
    }
  })
}

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

$(document).ready(function() {
  waitForPlayerReady();
  $('.clipItem').timefield();
  $('#splitButton').click(splitButtonClick);
  $('#okButton').click(okButtonClick);
  $('#cancelButton').click(cancelButtonClick);
  enabledRightBox(false);
  initPlayButtons();
})

function initPlayButtons() {
  $('#playFrom').click(function() {
    startTime = $('#clipBegin').timefield('option', 'value').toString().replace("s", "");
    editor.player[0].currentTime = startTime;
    editor.player[0].play();
  });

  $('#playTo').click(function() {
    endTime = parseInt($('#clipEnd').timefield('option', 'value').toString().replace("s", ""));
    editor.player[0].currentTime = Math.max(endTime, endTime - 5);
    editor.player[0].play();
  })
}

function okButtonClick() {
  id = $('#splitUUID').val();
  if (id != "") {
    splitItem = editor.splitData.splits[id];
    splitItem.description = $('#splitDescription').val();
    splitItem.clipBegin = $('#clipBegin').timefield('option', 'value');
    splitItem.clipEnd = $('#clipEnd').timefield('option', 'value');
    cancelButtonClick();
    editor.updateSplitList();
  }
}

function cancelButtonClick() {
  $('#splitDescription').html('');
  $('#splitUUID').val('');
  $('#splitDescription').val("");
  $('#clipBegin').timefield('option', 'value', 0);
  $('#clipEnd').timefield('option', 'value', 0);
  $('#splitIndex').html('#');
  $('.splitItem').removeClass('splitItemSelected');
  enabledRightBox(false);
}

function enabledRightBox(enabled) {
  if (enabled) {
    $('#rightBox :input').removeProp('disabled');
  } else {
    $('#rightBox :input').prop('disabled', 'disabled');
  }
}

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

function waitForPlayerReady() {
  if ($('#videoPlayer').prop("readyState") >= $('#videoPlayer').prop("HAVE_FUTURE_DATA")) {
    ocUtils.log("player ready");
    playerReady();
  } else {
    setTimeout(waitForPlayerReady, 200);
  }
}

function playerReady() {
  editor.player = $('#videoPlayer');
  
  $('#videoHolder').append('<div id="splitSegments"></div>')
  $('#videoHolder').append('<div id="waveform"></div>');
  $('#waveform').append('<div id="imageDiv"><img id="waveformImage" alt="waveform"/></div>');
//  $('#waveform').append('<div id="canvasDiv"><canvas id="waveformCanvas" /></div>');
  
//  editor.canvas = $('#waveformCanvas')[0];
  
  editor.player.bind('timeupdate', updateCurrentTime);
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
        $('<div/>').html("Found existing SMIL. Do you want to take over the cutting list?").dialog({
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
              editor.clearSMIL();
            }
          },
          title : "Apply existent SMIL"
        });
      }
    }
  });
  if (smil == null) {
    editor.createSMIL();
  }
  editor.updateSplitList();
  $('#editor').removeClass('disabled');
  
  // load waveform image
  $.each(ocUtils.ensureArray(workflowInstance.mediapackage.attachments.attachment), function(key, value) {
    if(value.type == WAVEFORM_FLAVOR) {
      $('#waveformImage').prop("src", value.url);
      $('#waveformImage').addimagezoom();
    }
  });
  
  var height = parseInt($('.holdStateUI').css('height').replace("px", ""));
  var heightVideo = parseInt($('#videoHolder').css('height').replace("px", ""));
  
  $('.holdStateUI').css('height', (height + heightVideo) + "px");
  parent.ocRecordings.adjustHoldActionPanelHeight();
}

function updateCurrentTime() {
  $('#current_time').html(formatTime(editor.player.prop("currentTime")));
}

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

function splitRemoverClick() {
  item = $(this).parent();
  id = item.prop('id');
  id = id.replace("splitItem-", "");
  if (!item.hasClass('disabled')) {
    $('#deleteDialog').dialog({
      buttons : {
        "Yes" : function() {
          item.addClass('disabled');
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
    item.removeClass('disabled');
    setEnabled(id, true);
  }
}

function setEnabled(uuid, enabled) {
  editor.splitData.splits[uuid].enabled = enabled;
}

function splitItemClick() {
  if (!$(this).hasClass('disabled')) {
    $('.splitItem').removeClass('splitItemSelected');
    $(this).addClass('splitItemSelected');
    id = $(this).attr('id');
    id = id.replace('splitItem-', '');
    splitItem = editor.splitData.splits[id];
    $('#splitDescription').val(splitItem.description);
    $('#splitUUID').val(id);
    $('#clipBegin').timefield('option', 'value', splitItem.clipBegin);
    $('#clipEnd').timefield('option', 'value', splitItem.clipEnd);
    $('#splitIndex').html(parseInt(id) + 1);
    enabledRightBox(true);
  }
}