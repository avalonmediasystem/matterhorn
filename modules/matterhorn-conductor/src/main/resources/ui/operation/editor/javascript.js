var PLAYER_URL = '/admin/embed.html';
var DEFAULT_SERIES_CATALOG_ID = 'seriesCatalog';
var WORKFLOW_RESTSERVICE = '/workflow/instance/';
var DUBLIN_CORE_NS_URI = 'http://purl.org/dc/terms/';

var postData = {
  'id' : parent.document.getElementById("holdWorkflowId").value
};

var catalogUrl = '';
var mediapackage = null;
var DCmetadata = null;
var metadataChanged = false;
var seriesChanged = false;
var seriesServiceURL = false;

var inpoint = 0;
var outpoint = 0;

// Variables for the "In point"- and "Out point"- increase-/decrease-Buttons
var secondsForward = 1;
var secondsBackward = 1;

var intervalTimer = 0;
// Timeout of the Intervall Timer
var timerTimeout = 1500;
var timerSet = false; // -> temporary solution. clearInterval(timer) does not
// work properly

function initCategories() {
  var options = '';
  for ( var key in iTunesCategories) {
    options += '<option value="' + key + '">' + iTunesCategories[key]['name']
  }
  $('#categorySelector').html(options);
}

function changedCategory() {
  var categoryId = $('#categorySelector').val();
  var options = '';

  var category = iTunesCategories[categoryId];
  options += '<option value="' + categoryId + '">-- Choose a Subcategory --</option>';
  for ( var i = 0; i < category['subCategories'].length; i++) {
    var sub = category['subCategories'][i];
    options += '<option value="' + sub.value + '">' + sub.name + '</option>';
  }
  $("#category").html(options);
  changedSubCategory();
}

function changedSubCategory() {
  var subject = $('#category option:selected').index() == 0 ? $('#categorySelector option:selected').text() : $(
      '#category option:selected').text();
  $("#meta-subject").val(subject);
}

function addSelectValues(selectId, from, to) {
  for ( var i = from; i <= to; i++) {
    var option = $('<option/>');
    option.attr({
      'value' : i
    });
    option.html(zeroFill(i, 2));
    $('#' + selectId).append(option);
  }
}

function zeroFill(number, width) {
  width -= number.toString().length;
  if (width > 0) {
    return new Array(width + (/\./.test(number) ? 2 : 1)).join('0') + number;
  }
  return number + ""; // always return a string
}

$(document).ready(function() {
  addSelectValues('startTimeHour', 0, 23);
  addSelectValues('startTimeMin', 0, 59);
})

$(document)
    .ready(
        function() {
          var id = postData.id;
          initCategories();
          $('#categorySelector').change(function() {
            changedCategory();
          });
          $('#category').change(function() {
            changedSubCategory();
          });
          if (id == "") {
            return;
          }
          var recordDate = null;
          $('#recordDate').datepicker({
            showOn : 'both',
            buttonImage : '/admin/img/icons/calendar.gif',
            buttonImageOnly : true,
            dateFormat : 'yy-mm-dd'
          });

          $('.oc-ui-form-field').change(function() {
            metadataChanged = true;
          });

          // Set default Values for In point and Out point
          $('#player-container').load(function() {
            $('#inPoint').val("00:00:00");
            // Set a Timer for the while-Loop
            this.intervalTimer = window.setInterval(function() {
              // Ask if a default Out Point has been set
              if (setOutPointDefaultValue()) {
                // Clear the Intervall
                $('#continueBtn').button('enable');
                window.clearInterval(this.intervalTimer);
              }
            }, timerTimeout);
          });

          // load tracks
          var tracks = {};
          tracks.tracks = [];

          $.ajax({
            url : WORKFLOW_RESTSERVICE + id + ".json",
            async : false,
            success : function(data) {
              data = data.workflow.mediapackage.media.track;
              for (i = 0; i < data.length; i++) {
                if (data[i].type.indexOf("work") != -1) {
                  tracks.tracks.push(data[i]);
                }
              }
            }
          });

          $('#trackForm').append($('#template').jqote(tracks));

          $('input[id^="chk"]').click(function(event) {
            if ($("input:checked").length == 0) {
              $('#trackError').show();
              $(event.currentTarget).prop("checked", true);
            } else {
              $('#trackError').hide();
            }
          });

          // loading tracks ready

          /**
           * Tries to set the default Out point value
           */
          function setOutPointDefaultValue() {
            // If Duration has been set
            if ($('#player-container')[0].contentWindow.Opencast != null
                && ($('#player-container')[0].contentWindow.Opencast.Player.getDuration() != 0) && !timerSet
                && ($('#player-container')[0].contentWindow.Opencast.Player.getDuration() != -1)
                && $('#player-container').contents().find('#oc_duration').text() != 'Initializing') {
              $('#outPoint').val($('#player-container').contents().find('#oc_duration').text());
              $('#newLength').val($('#player-container').contents().find('#oc_duration').text());
              timerSet = true;
              return true;
            }

            return false;
          }

          // create Buttons
          $('.ui-button').button();
          // disable continue
          $('#continueBtn').button('disable');

          // hide some stuff we don't want to see
          window.parent.$('#uploadContainer').hide(0);
          $('#trimming-hint').toggle();

          window.parent.$('#controlsTop').hide(0);
          window.parent.$('#searchBox').hide(0);
          window.parent.$('#tableContainer').hide(0);
          window.parent.ocRecordings.disableRefresh();
          window.parent.ocRecordings.stopStatisticsUpdate();
          window.parent.$('#controlsFoot').hide(0);

          // Event edit link clicked
          $('#edit-link').click(function() {
            // parent.Recordings.retryRecording(id);
            parent.location.href = "/admin/upload.html?retry=" + id;
            return false;
          });

          // Event set inpoint clicked
          $('#set-trimin').click(function() {
            $('#inPoint').val($('#player-container')[0].contentWindow.Opencast.Player.getCurrentTime());
            checkInOutPoint();
          });

          // Event set outpoint clicked
          $('#set-trimout').click(function() {
            $('#outPoint').val($('#player-container')[0].contentWindow.Opencast.Player.getCurrentTime());
            checkInOutPoint();
          });

          // Event forward one second (inpoint)
          $('#step-in-forward').click(function() {
            in_de_creaseObject($('#inPoint'), secondsForward);
            checkInOutPoint();
          });

          // Event backward one second (inpoint)
          $('#step-in-backward').click(function() {
            in_de_creaseObject($('#inPoint'), -secondsForward);
            checkInOutPoint();
          });

          // Event forward one second (outpoint)
          $('#step-out-forward').click(function() {
            in_de_creaseObject($('#outPoint'), secondsBackward);
            checkInOutPoint();
          });

          // Event backward one second (outpoint)
          $('#step-out-backward').click(function() {
            in_de_creaseObject($('#outPoint'), -secondsBackward);
            checkInOutPoint();
          });

          /**
           * Checks if In point is bigger than Out point and prints an Error
           * Message if this is the case
           */
          function checkInOutPoint() {
            // Check if Out point is larger than the Video Duration
            if (getTimeInMilliseconds($('#outPoint').val()) > getTimeInMilliseconds($('#player-container').contents()
                .find('#oc_duration').text())) {
              $('#outPoint').val($('#player-container').contents().find('#oc_duration').text());
            }
            // Check if In point is larger than the Video Duration
            if (getTimeInMilliseconds($('#inPoint').val()) > getTimeInMilliseconds($('#player-container').contents()
                .find('#oc_duration').text())) {
              $('#inPoint').val($('#player-container').contents().find('#oc_duration').text());
            }
            if (getTimeInMilliseconds($('#inPoint').val()) >= getTimeInMilliseconds($('#outPoint').val())) {
              $('div#errorMessage').html("Out point must be later than In point");
              $('#trimming-hint').hide();
              $('div#errorMessage').show();
            } else {
              calculateNewLength();
              $('div#errorMessage').hide();
              $('#trimming-hint').show();
            }
          }

          function setValue(input, value) {
            $('#' + input).val(value);
          }

          // start playing from current in point
          $('#play-from-in').click(function() {
            var videodisplay = $('#player-container')[0].contentWindow.Videodisplay;
            videodisplay.pause();
            var seekTime = getTimeInMilliseconds($('#inPoint').val());
            videodisplay.seek(seekTime / 1000);
            window.setTimeout(function() {
              videodisplay.play();
            }, 800);
          });

          // play last 10 seconds to out
          $('#play-to-out').click(function() {
            var videodisplay = $('#player-container')[0].contentWindow.Videodisplay;
            videodisplay.pause();
            var seekTime = getTimeInMilliseconds($('#outPoint').val()) / 1000;
            var timeOut = 10000;
            // If Out point < 10 Seconds
            if ((seekTime - 10) <= 0) {
              videodisplay.seek(0);
              timeOut = seekTime * 1000;
            } else {
              videodisplay.seek(seekTime - 10);
            }
            window.setTimeout(function() {
              videodisplay.play();
              window.setTimeout(function() {
                videodisplay.pause();
              }, timeOut);

            }, 800);
          });

          // load preview player and metadata
          $
              .ajax({
                url : '/workflow/instance/' + id + '.xml',
                dataType : 'xml', // or XML..
                success : function(data) {

                  // clone mediapackage for editing
                  mediapackage = ocUtils.createDoc('mediapackage', '');
                  var clone = $(data.documentElement).find("mediapackage").clone();
                  $(clone).children().appendTo($(mediapackage.documentElement));
                  $(mediapackage.documentElement).attr('id', $(clone).attr('id'));
                  $(mediapackage.documentElement).attr('start', $(clone).attr('start'));
                  $(mediapackage.documentElement).attr('duration', $(clone).attr('duration'));

                  // populate series field if information
                  // present
                  var seriesid = $(data.documentElement).find("mediapackage > series").text();
                  if (seriesid != '') {
                    $('#ispartof').val(seriesid);
                    $('#series').val($(data.documentElement).find("mediapackage > seriestitle").text());
                    $('#info-series')[0].innerHTML = $(data.documentElement).find("mediapackage > seriestitle").text();
                  }

                  // load metadata from DC xml for editing
                  catalogUrl = $(data.documentElement).find(
                      "mediapackage > metadata > catalog[type='dublincore/episode'] > url").text();
                  $.ajax({
                    url : catalogUrl,
                    dataType : 'xml',
                    error : function(XMLHttpRequest, textStatus, errorThrown) {
                      $('div#errorMessage').html('error: ' + textStatus);
                    },
                    success : function(data) {
                      DCmetadata = data;
                      $(data.documentElement).children().each(function(index, elm) {
                        var tagName = elm.tagName.split(/:/)[1];
                        if ($(elm).text() != '') {
                          $('#meta-' + tagName).val($(elm).text());
                          if ($('#info-' + tagName).length > 0)
                            $('#info-' + tagName)[0].innerHTML = $(elm).text();
                          if (tagName === "category") {
                            value = $(elm).text();
                            $('#categorySelector').val(value.substr(0, 3));
                            changedCategory();
                            if (value.length > 3) {
                              $('#category').val(value);
                              changedSubCategory();
                            }
                          }
                          if (tagName === "created") {
                            $('#recordDate').datepicker('setDate', new Date($(elm).text()));
                            $('#startTimeHour').val((new Date($(elm).text())).getHours());
                            $('#startTimeMin').val((new Date($(elm).text())).getMinutes());
                          }
                        }
                      });

                      // save information that
                      // some metadata changed
                      $('.dcMetaField').change(function() {
                        metadataChanged = true;
                      });
                      $('.ocMetaField').change(function() {
                        metadataChanged = true;
                      });
                      parent.ocRecordings.adjustHoldActionPanelHeight();
                    }
                  });

                  var previewFiles = new Array();

                  $(data.documentElement).find("mediapackage > media > track").each(function(index, elm) {
                    if ($(elm).attr('type').split(/\//)[1] == 'preview') {
                      previewFiles.push($(elm).find('url').text());
                    }
                  });
                  if (previewFiles.length > 0) {
                    var url = PLAYER_URL + '?';
                    for ( var i = 0; i < previewFiles.length; i++) {
                      if (i == 0) {
                        url += 'videoUrl=';
                      } else {
                        url += '&videoUrl' + (i + 1) + '=';
                      }
                      url += previewFiles[i];
                    }
                    $('#player-container').attr('src',
                        url + "&play=false&preview=true&hideControls=false&hideAPLogo=true");
                  } else {
                    $('#player-container').text("No preview media files found for this media package.");
                  }
                  // show links to source media
                  var singleFile = true;
                  $(data.documentElement).find("mediapackage > media > track").each(
                      function(index, elm) {
                        if ($(elm).attr('type').split(/\//)[1] == 'source') {
                          var link = document.createElement('a');
                          var url = $(elm).find('url').text();
                          $(link).attr('href', url);
                          var filename = url.split(/\//);
                          $(link).text(filename[filename.length - 1]).attr('title',
                              'Download ' + filename[filename.length - 1] + ' for editing');
                          if (singleFile) {
                            singleFile = false;
                          } else {
                            $('#files').append($(document.createElement('span')).text(', '));
                          }
                          $('#files').append(link);
                        }
                      });
                }
              });

          // Event: collapsable title clicked, de-/collapse
          // collapsables
          $('.collapse-control2').click(function() {
            $('#ui-icon').toggleClass('ui-icon-triangle-1-e');
            $('#ui-icon').toggleClass('ui-icon-triangle-1-s');
            $(this).next('.collapsable').toggle();
            parent.ocRecordings.adjustHoldActionPanelHeight();
          });

          // try to obtain URL of series service endpoint from service
          // registry
          // on success enable series input field / init autocomplete
          // on it
          // TODO: use JSONP here so we can call services provieded by
          // other hosts in a distributed deployment
          var thisHost = window.location.protocol + '//' + window.location.host;
          $
              .ajax({
                type : 'GET',
                url : '/services/services.json',
                data : {
                  serviceType : 'org.opencastproject.series',
                  host : thisHost
                },
                dataType : 'json',
                success : function(data) { // we are asking for
                  // a series service
                  // on the host this
                  // site comes from
                  if (data.services.service !== undefined) { // so
                    seriesServiceURL = data.services.service.host + data.services.service.path;
                    $('#series').removeAttr('disabled');
                    ocUtils.log('Initializing autocomplete for series field')
                    $('#series')
                        .autocomplete(
                            {
                              source : function(request, response) {
                                $.ajax({
                                  url : seriesServiceURL + '/series.json?q=' + request.term,
                                  dataType : 'json',
                                  type : 'GET',
                                  success : function(data) {
                                    var series_list = [];
                                    $.each(data.catalogs, function() {
                                      series_list.push({
                                        value : this[DUBLIN_CORE_NS_URI]['title'][0].value,
                                        id : this[DUBLIN_CORE_NS_URI]['identifier'][0].value
                                      });
                                    });
                                    response(series_list);
                                  },
                                  error : function() {
                                    ocUtils.log('could not retrieve series_data');
                                  }
                                });
                              },
                              select : function(event, ui) {
                                $('#ispartof').val(ui.item.id);
                              },
                              change : function(event, ui) {
                                if ($('#ispartof').val() === '' && $('#series').val() !== '') {
                                  ocUtils.log("Searching for series in series endpoint");
                                  $
                                      .ajax({
                                        url : seriesServiceURL + '/series.json?seriesTitle=' + $('#series').val(),
                                        type : 'get',
                                        dataType : 'json',
                                        success : function(data) {
                                          var series_input = $('#series').val(), series_list = data["catalogs"], series_title, series_id;

                                          if (series_list.length !== 0) {
                                            series_title = series_list[0][DUBLIN_CORE_NS_URI]["title"] ? series_list[0][DUBLIN_CORE_NS_URI]["title"][0].value
                                                : "";
                                            series_id = series_list[0][DUBLIN_CORE_NS_URI]["identifier"] ? series_list[0][DUBLIN_CORE_NS_URI]["identifier"][0].value
                                                : "";
                                            $('#ispartof').val(series_id);
                                          }
                                        }
                                      });
                                } else if ($('#ispartof').val() === '' && $('#series').val() === '') {
                                  $('#ispartof').val('');
                                }
                              },
                              search : function() {
                                $('#ispartof').val('');
                              }
                            });
                    $('#series').change(function() {
                      seriesChanged = true;
                    });
                  }
                }
              });

          parent.ocRecordings.adjustHoldActionPanelHeight();
        });

/**
 * Returns the Input Time in Milliseconds
 * 
 * @param data
 *          Data in the Format ab:cd:ef
 * @return Time from the Data in Milliseconds
 */
function getTimeInMilliseconds(data) {
  var values = data.split(':');

  // If the Format is correct
  if (values.length == 3) {
    // Try to convert to Numbers
    var val0 = values[0] * 1;
    var val1 = values[1] * 1;
    var val2 = values[2] * 1;
    // Check and parse the Seconds
    if (!isNaN(val0) && !isNaN(val1) && !isNaN(val2)) {
      // Convert Hours, Minutes and Seconds to Milliseconds
      val0 *= 60 * 60 * 1000; // 1 Hour = 60 Minutes = 60 * 60 Seconds =
      // 60 * 60 * 1000 Milliseconds
      val1 *= 60 * 1000; // 1 Minute = 60 Seconds = 60 * 1000
      // Milliseconds
      val2 *= 1000; // 1 Second = 1000 Milliseconds
      // Add the Milliseconds and return it
      return val0 + val1 + val2;
    } else {
      return 0;
    }
  } else {
    return 0;
  }
}

/**
 * Increases or decreases the current In point by val
 * 
 * @param obj
 *          Object with Function .val()
 * @param val
 *          Value in Seconds to increase (val > 0) or decrease (val < 0), val <
 *          20 Seconds
 */
function in_de_creaseObject(obj, val) {
  if ((val != 0) && (Math.abs(val < 20))) {
    // Get current In point data
    var data = obj.val();
    // If data contains something
    if (data != '') {
      var values = data.split(':');
      if (values.length == 3) {
        // Try to convert to Numbers
        var val0 = values[0] * 1;
        var val1 = values[1] * 1;
        var val2 = values[2] * 1;
        // Check and parse the Seconds
        if (!isNaN(val0) && !isNaN(val1) && !isNaN(val2)) {
          // Increase
          if ((val > 0) && ((val0 >= 0) || (val1 >= 0) || (val2 >= 0))) {
            // If >= 59 Seconds
            if ((val2 + val) > 59) {
              // If >= 59 Minutes
              if ((val1 + 1) > 59) {
                // Increase Hours and set Minutes and Seconds to
                // 0
                obj.val(getTimeString(val0 + 1, 0, Math.abs(val - (60 - val2))));
              } else {
                // Increase Minutes and set Seconds to the
                // Difference
                obj.val(getTimeString(val0, val1 + 1, Math.abs(val - (60 - val2))));
              }
            } else {
              // Increase Seconds
              obj.val(getTimeString(val0, val1, val2 + val));
            }
          }
          // Decrease
          else if ((val0 > 0) || (val1 > 0) || (val2 > 0)) {
            // If <= 0 Seconds
            if ((val2 + val) < 0) {
              // If <= 0 Minutes
              if ((val1 - 1) < 0) {
                // Decrease Hours and set Minutes and Seconds to
                // 0
                obj.val(getTimeString(val0 - 1, 59, 60 - Math.abs(60 - Math.abs(val - (60 - val2)))));
              } else {
                // Decrease Minutes and set Seconds to 0
                obj.val(getTimeString(val0, val1 - 1, 60 - Math.abs(60 - Math.abs(val - (60 - val2)))));
              }
            } else {
              // Decrease Seconds
              obj.val(getTimeString(val0, val1, val2 + val));
            }
          } else {
            obj.val("00:00:00");
          }
        } else {
          obj.val("00:00:00");
        }
      } else {
        obj.val("00:00:00");
      }
    }
  }
}

/**
 * calculates the new length of the media and shows the result in the according
 * field
 */
function calculateNewLength() {
  inPoint = getTimeInMilliseconds($('#inPoint').val());
  outPoint = getTimeInMilliseconds($('#outPoint').val());
  newLength = (outPoint - inPoint) / 1000;
  $('#newLength').val(getTimeString(Math.floor(newLength / 3600), Math.floor(newLength / 60), newLength % 60));
}

/**
 * Returns a correct formatted Time String in the Format ab:cd:ef
 * 
 * @param val0
 *          Hours element (0, 99)
 * @param val0
 *          Minutes element (0, 60)
 * @param val0
 *          Seconds element (0, 60)
 * @return a correct formatted Time String in the Format ab:cd:ef
 */
function getTimeString(val0, val1, val2) {
  if ((val0 >= 0) && (val0 < 100) && (val1 >= 0) && (val1 < 60) && (val2 >= 0) && (val2 < 60)) {
    if (val0 <= 9) {
      val0 = "0" + val0.toString();
    }
    if (val1 <= 9) {
      val1 = "0" + val1.toString();
    }
    if (val2 <= 9) {
      val2 = "0" + val2.toString();
    }
    return val0 + ":" + val1 + ":" + val2;
  } else {
    return "00:00:00";
  }
}

/**
 * Continues the Workflow
 */
function continueWorkflow() {
  // Get Video Duration
  var videoDurationData = $('#player-container').contents().find('#oc_duration').text();
  // Format 'Trim From'
  var trimFromData = $('#inPoint').val();
  trimFromData = ((trimFromData == '') || (trimFromData == null)) ? '00:00:00' : trimFromData;
  // Format 'Trim To'
  var trimToData = $('#outPoint').val();
  trimToData = ((trimToData == '') || (trimToData == null)) ? videoDurationData : trimToData;

  // if metadata was changed update DC catalog and mediapackage instance
  if (metadataChanged) {
    if ($('#meta-title').val()) {
      updateDCMetadata();
      updateMediapackageMetadata();
      saveDCMetadata();
    } else {
      alert("Field 'Title' must not be empty!");
      return;
    }
  }

  if (seriesChanged) {
    if ($('#series').val() == '') {
      // remove series from mediapackage
      ocUtils.log('Removing Series');
      $(mediapackage.documentElement).find('series').remove();
      $(mediapackage.documentElement).find('seriestitle').remove();
      var $seriesCatalogRef = $(mediapackage.documentElement).find("metadata > catalog[type='dublincore/series']");
      if ($seriesCatalogRef.length > 0) {
        // delete series DC xml from working file repo
        var seriesCatalogUrl = $seriesCatalogRef.find('url').text();
        $.ajax({
          url : seriesCatalogUrl,
          type : 'delete',
          error : function() {
            ocUtils.log('Failed to removed Series DC XML');
          },
          success : function() {
            ocUtils.log('Removed Series DC XML');
          }
        });
        $seriesCatalogRef.remove();
      }
    } else {
      // update/add series data to mediapackage
      var seriesDcXml = '';
      if ($('#ispartof').val() == '') {
        // create series
        ocUtils.log('Creating Series ' + $('#series').val());
        seriesXml = '<series><additionalMetadata><metadata><key>title</key><value>' + $('#series').val()
            + '</value></metadata></additionalMetadata></series>';
        $.ajax({
          async : false,
          type : 'PUT',
          url : seriesServiceURL,
          data : {
            series : seriesXml
          },
          dataType : 'json',
          success : function(data) {
            $('#ispartof').val(data.series.id);
          }
        });
      }
      // get seriesDcXml
      ocUtils.log('Getting DC catalog for series ' + $('#ispartof').val());
      $.ajax({
        url : seriesServiceURL + '/' + $('#ispartof').val() + '.xml',
        type : 'get',
        async : false,
        dataType : 'xml',
        error : function() {
          ocUtils.log('Could not retrieve series DC catalog for series ' + $('#ispartof').val());
        },
        success : function(data) {
          seriesDcXml = ocUtils.xmlToString(data);
        }
      });

      // find series dc ref in mediapackage
      var seriesDcElm = $(mediapackage.documentElement).find("metadata > catalog[type='dublincore/series']");
      var seriesDcFileId = ''; // MediaPackageElementId of series DC
      // catalog
      if (seriesDcElm.length == 0) {
        var seriesDcElm = $('<catalog></catalog>', mediapackage.documentElement).attr('id', DEFAULT_SERIES_CATALOG_ID)
            .attr('type', 'dublincore/series');
        $('<mimetype></mimetype>', mediapackage.documentElement).text('text/xml').appendTo(seriesDcElm);
        $('<url></url>', mediapackage.documentElement).appendTo(seriesDcElm);
        $(mediapackage.documentElement).find('metadata').append(seriesDcElm);
      }
      // upload series dc xml
      seriesDcFileId = seriesDcElm.attr('id');
      var mediapackageId = $(mediapackage.documentElement).attr('id');
      var url = '/files/mediapackage/' + mediapackageId + '/' + seriesDcFileId + '/dublincore.xml';
      ocUtils.log('Saving Series DC Catalog to ' + url);
      $.ajax({
        url : url,
        type : 'post',
        async : false,
        data : {
          content : seriesDcXml
        },
        error : function() {
          ocUtils.log('Failed to save DC metadata to ' + url);
        },
        success : function(data) {
          ocUtils.log('Save DC metadata to ' + url);
          seriesDcElm.find('url').text(data);
        }
      });
      // finally update series in mediapackage instance
      updateMPElement('seriestitle', $('#series').val());
      updateMPElement('series', $('#ispartof').val());
    }
  }

  // If Input < Output
  if (trimFromData < trimToData) {
    var newduration = getTimeInMilliseconds(trimToData) - getTimeInMilliseconds(trimFromData);
    postData['trimin'] = getTimeInMilliseconds(trimFromData);
    // postData['trimout'] = getTimeInMilliseconds(trimToData);
    postData['newduration'] = newduration;
    var trackChanged = $('input:checkbox:not(:checked)').length != $('input:checkbox').length;
    if (metadataChanged || seriesChanged || trackChanged) {
      var mp = ocUtils.xmlToString(mediapackage);
      mp = mp.replace(/ xmlns="http:\/\/www\.w3\.org\/1999\/xhtml"/g, ''); // no
      // luck
      // with
      // $(element).removeAttr('xmlns');
      $.each($('input:checkbox:not(:checked)'), function(key, value) {
        var trackId = $(value).prop("id");
        trackId = trackId.split('/')[1];
        mp = ocMediapackage.removeTrack(mp, trackId);
      });
      parent.ocRecordings.Hold.changedMediaPackage = mp;
      // alert(mp);
    }
    parent.ocRecordings.continueWorkflow(postData);
  } else {
    $('div#errorMessage').html("The In-Point must not be bigger than the Out-Point");
  }
}

function cancel() {
  window.parent.location.href = "/admin";
  // window.parent.location.href = window.parent.location.href;
}

function updateDCMetadata() {
  ocUtils.log("Updating DC metadata");

  $('.dcMetaField').each(
      function() {
        var $field = $(this);
        var fieldname = $field.attr('name');
        if (fieldname === 'created') {
          recordDate = $('#recordDate').datepicker('getDate').getTime() / 1000; // Get
          // date
          // in
          // milliseconds,
          // convert to seconds.
          recordDate += $('#startTimeHour').val() * 3600; // convert
          // hour to
          // seconds,
          // add to
          // date.
          recordDate += $('#startTimeMin').val() * 60; // convert
          // minutes
          // to
          // seconds,
          // add to
          // date.
          recordDate = recordDate * 1000; // back to milliseconds
          recordDate = ocUtils.toISODate(recordDate); // = (new
          // Date(recordDate)).format("isoUTCdateTime");
        }
        if ($field.val() != '') {
          var $dcelm = false; // $(DCmetadata.documentElement).find('dcterms\\:'
          // + $field.attr('name'));
          $(DCmetadata.documentElement).children().each(function(index, elm) {
            if (elm.tagName == 'dcterms:' + fieldname) {
              $dcelm = $(elm);
            }
          });
          if ($dcelm !== false) {
            ocUtils.log("updating " + $field.attr('name') + " to: " + $field.val());
            $field.attr('name') === "created" ? $dcelm.text(recordDate) : $dcelm.text($field.val());
          } else {
            ocUtils.log("creating " + $field.attr('name') + " with value: " + $field.val());
            $field.attr('name') === "created" ? $('<dcterms:' + $field.attr('name') + '>').text(recordDate).appendTo(
                DCmetadata.documentElement) : $('<dcterms:' + $field.attr('name') + '>').text($field.val()).appendTo(
                DCmetadata.documentElement);
          }
        }
      });

}

// Update the MediaPackage instances metadata fields (see
// org.opencastproject.mediapackage.MediaPackageImpl)
function updateMediapackageMetadata() {
  updateMetadataField('title', 'title');
  updateMetadataField('created', 'start');
  updateMetadataField('language', 'language');
  updateMetadataField('keywords', 'keywords');
  updateMetadataField('rightsholder', 'rightsholder');
  updateMetadataField('license', 'license');
  updateMetadataField('subject', 'subject');
  updateMetadataGroupField('creator', 'creators', 'creator');
  updateMetadataGroupField('contributor', 'contributors', 'contributor');

  // update series
  if ($('#series').val() != '') {
    if ($('#ispartof').val() == '') {
      createSeriesFromText();
    }
    updateMPElement('seriestitle', $('#series').val());
    updateMPElement('series', $('#ispartof').val());
  }
}

function createSeriesFromText() {
  var dcDoc = '<dublincore xmlns="http://www.opencastproject.org/xsd/1.0/dublincore/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oc="http://www.opencastproject.org/matterhorn"><dcterms:title xmlns="">'
      + $('#series').val()
      + '</dcterms:title><dcterms:creator xmlns=""></dcterms:creator><dcterms:contributor xmlns=""></dcterms:contributor><dcterms:subject xmlns=""></dcterms:subject><dcterms:language xmlns=""></dcterms:language><dcterms:license xmlns=""></dcterms:license><dcterms:description xmlns=""></dcterms:description></dublincore>';
  var acl = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns2:acl xmlns:ns2="org.opencastproject.security"></ns2:acl>';
  $.ajax({
    type : 'POST',
    url : '/series/',
    async : false,
    dataType : 'xml',
    data : {
      series : dcDoc,
      acl : acl
    },
    success : function(data) {
      id = data.getElementsByTagName('dcterms:identifier')[0].textContent;
      $('#ispartof').val(id);
    }
  });
}

function updateMPElement(MPname, value) {
  var $mpelm = $(mediapackage.documentElement).find(MPname);
  if ($mpelm.length != 0) {
    ocUtils.log('Updating ' + MPname + ' in MediaPackage to ' + value);
    MPname === "start" ? $mpelm.text(recordDate) : $mpelm.text(value);
  } else {
    ocUtils.log('Creating ' + MPname + ' in MediaPackage with value ' + value);
    $newelm = MPname === "start" ? $('<' + MPname + '/>').text(recordDate) : $('<' + MPname + '/>').text(value);
    $newelm.appendTo(mediapackage.documentElement);
  }
}

function updateMetadataField(DCname, MPname) {
  var $dcelm = $(DCmetadata.documentElement).find('dcterms\\:' + DCname);
  // dcterms:created ->
  if ($dcelm.length != 0) {
    updateMPElement(MPname, $dcelm.text());
  }
}

function updateMetadataGroupField(DCname, MPgroupname, MPname) {
  var $dcelms = $(DCmetadata.documentElement).find('dcterms\\:' + DCname);
  if ($dcelms.length != 0) {
    $parent = $(mediapackage.documentElement).find(MPgroupname);
    if ($parent.length > 0) {
      $parent.empty();
    } else {
      $parent = $('<' + MPgroupname + '/>');
      $(mediapackage.documentElement).append($parent);
    }
    $dcelms.each(function() {
      $('<' + MPname + '/>').text($(this).text()).appendTo($parent);
    });
  }
}

function saveDCMetadata() {
  $.ajax({
    url : catalogUrl,
    async : false,
    type : 'post',
    data : {
      content : ocUtils.xmlToString(DCmetadata)
    },
    error : function() {
      ocUtils.log('Failed to save DC metadata to ' + catalogUrl);
    },
    success : function() {
      ocUtils.log('Save DC metadata to ' + catalogUrl);
    }
  });
}