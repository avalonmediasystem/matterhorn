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

editor.splitData = {};
editor.splitData.splits = [];
editor.player = {};

editor.updateSplitList = function() {
  $('#leftBox').html($('#splitElements').jqote(editor.splitData));
  $('.splitItem').click(splitItemClick);
  $('.splitRemover').click(splitRemoverClick);
}

$(document).ready(function() {
  waitForPlayerReady();
  $('.clipItem').timefield();
  $('#splitButton').click(splitButtonClick);
  $('#okButton').click(okButtonClick);
  $('#cancelButton').click(cancelButtonClick);
  enabledRightBox(false);
})

function okButtonClick() {
  id = $('#splitUUID').val();
  if (id != "") {
    splitItem = editor.splitData.splits[id];
    splitItem.description = $('#splitDescription').html();
    splitItem.clipBegin = $('#clipBegin').timefield('option', 'value');
    splitItem.clipEnd = $('#clipEnd').timefield('option', 'value');
    cancelButtonClick();
    editor.updateSplitList();
  }
}

function cancelButtonClick() {
  $('#splitDescription').html('');
  $('#splitUUID').val('');
  $('#clipBegin').timefield('option', 'value', 0);
  $('#clipEnd').timefield('option', 'value', 0);
  $('#splitIndex').html('#');
  $('.splitItem').removeClass('splitItemSelected');
  enabledRightBox(false);
}

function enabledRightBox(enabled) {
  if(enabled) {
    $('#rightBox :input').removeProp('disabled');
  } else {
    $('#rightBox :input').prop('disabled', 'disabled');
  }
}


function splitButtonClick() {
  var currentTime = editor.player.getCurrentPosition();
  for ( var i = 0; i < editor.splitData.splits.length; i++) {
    var splitItem = editor.splitData.splits[i];
    var clipBegin = parseFloat(splitItem.clipBegin.replace("s", ""));
    var clipEnd = parseFloat(splitItem.clipEnd.replace("s", ""));
    if (clipBegin < currentTime && currentTime < clipEnd) {
      ocUtils.log("foundItem");
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
  if ($('#player-container')[0].contentWindow.Opencast
      && $('#player-container')[0].contentWindow.Opencast.Player
      && $('#player-container')[0].contentWindow.Opencast.Player.getDuration() != 0
      && $('#player-container')[0].contentWindow.Opencast.Player.getDuration() != -1) {
    ocUtils.log("player ready");
    playerReady();
  } else {
    setTimeout(waitForPlayerReady, 200);
  }
}

function playerReady() {
  editor.player = $('#player-container')[0].contentWindow.Opencast.Player;
  setInterval(updateCurrentTime, 500);
  editor.splitData.splits.push({
    clipBegin : '0s',
    clipEnd : editor.player.getDuration() + "s",
    enabled : true,
    description : ""
  });
  editor.updateSplitList();
}

function updateCurrentTime() {
  $('#current_time').html(formatTime(editor.player.getCurrentPosition()));
}

function formatTime(time) {
  if (typeof time == "string") {
    time = time.replace("s", "");
    time = parseFloat(time);
  }
  seconds = parseInt(time);
  millis = parseInt((time - seconds) * 1000);
  formatted = ocUtils.formatSeconds(time) + "." + millis;
  return formatted;
}

function splitRemoverClick() {
  item = $(this).parent();
  id = item.attr('id');
  if (!item.hasClass('splitItemDisabled')) {
    $('#deleteDialog').dialog({
      buttons : {
        "Yes" : function() {
          item.addClass('splitItemDisabled');
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
    item.removeClass('splitItemDisabled');
    setEnabled(id, true);
  }
}

function setEnabled(uuid, enabled) {
  editor.splitData.splits[uuid].enabled = enabled;
}

function splitItemClick() {
  if (!$(this).hasClass('splitItemDisabled')) {
    $('.splitItem').removeClass('splitItemSelected');
    $(this).addClass('splitItemSelected');
    id = $(this).attr('id');
    id = id.replace('splitItem-', '');
    splitItem = editor.splitData.splits[id];
    $('#splitDescription').html(splitItem.description);
    $('#splitUUID').val(id);
    $('#clipBegin').timefield('option', 'value', splitItem.clipBegin);
    $('#clipEnd').timefield('option', 'value', splitItem.clipEnd);
    $('#splitIndex').html(parseInt(id) + 1);
    enabledRightBox(true);
  }
}