/** Attention: underscore.js is needed to operate this script!
 */
ocArchive = new (function() {

  var EPISODE_LIST_URL = '../episode/episode.json';   // URL of workflow instances statistics endpoint

  var SORT_FIELDS = {
    'Title' : 'TITLE',
    'Presenter' : 'CREATOR',
    'Series' : 'SERIES_TITLE',
    'Date' : 'DATE_CREATED'
  }

  var SEARCH_FILTER_FIELDS = [ {
    q: "Full text"
  }];

  this.totalEpisodes = 0;
  this.currentShownEpisodes = 0;
  this.numSelectedEpisodes = 0;

  // components
  this.searchbox = null;
  this.pager = null;

  this.data = null;     // currently displayed recording data
  this.statistics = null;

  var refreshing = false;      // indicates if JSONP requesting recording data is in progress
  this.refreshingStats = false; // indicates if JSONP requesting statistics data is in progress
  this.refreshInterval = null;
  this.statsInterval = null;

  /** Executed directly when script is loaded: parses url parameters and
   *  returns the configuration object.
   */
  this.conf = new (function() {

    // default configuartion
    this.state = 'all';
    this.pageSize = 10;
    this.page = 0;
    this.refresh = 5;
    this.doRefresh = 'true';
    this.sortField = 'Date';
    this.sortOrder = 'ASC';
    this.filterField = null;
    this.filterText = '';

    this.lastState = 'all'
    this.lastPageSize = 10;
    this.lastPage = 0;

    // parse url parameters
    try {
      var p = document.location.href.split('?', 2)[1] || false;
      if (p !== false) {
        p = p.split('&');
        for (i in p) {
          var param = p[i].split('=');
          if (this[param[0]] !== undefined) {
            this[param[0]] = unescape(param[1]);
          }
        }
      }
    }
    catch (e) {
      alert('Unable to parse url parameters:\n' + e.toString());
    }

    return this;
  })();

  /** Initiate new JSONP call to workflow instances list endpoint
   */
  function refresh() {
    if (!refreshing) {
      refreshing = true;
      var params = [];

      // generic function to create the json url
      function mkUrl(baseUrl, startPageParam, countParam, params) {
        var p = $.merge([], params);
        p.push(countParam + "=" + ocArchive.conf.pageSize);
        p.push(startPageParam + "=" + ocArchive.conf.page);
        return baseUrl + "?" + p.join("&");
      }

      // a URL handler is an object containing the two functions
      // createUrl(params) and extractData(json).
      var urlHandler = {
        createUrl: function(params) {
          return mkUrl(EPISODE_LIST_URL, "offset", "limit", params);
        },
        extractData: function(json) {
          return {
            raw: json,
            totalCount: parseInt(json["search-results"].total),
            count: parseInt(json["search-results"].limit),
            mkRenderData: function() {
              return makeRenderDataEpisodes(json);
            }
          }
        }
      };

      // sorting if specified
      if (ocArchive.conf.sortField != null) {
        var sort = SORT_FIELDS[ocArchive.conf.sortField];
        if (ocArchive.conf.sortOrder == 'DESC') {
          sort += "_DESC";
        }
        params.push('sort=' + sort);
      }
      // filtering if specified
      if (ocArchive.conf.filterText != '') {
        params.push(ocArchive.conf.filterField + '=' + encodeURI(ocArchive.conf.filterText));
      }

      // issue the ajax request
      $.ajax({
        url: urlHandler.createUrl(params),
        dataType: 'jsonp',
        jsonp: 'jsonp',
        success: function(data) {
          ocArchive.render(urlHandler.extractData(data));
        }
      });
    }
  }

  /** Create an object representing an episode from json.
   *  @param json -- json data from the episode service
   */
  function Episode(json) {
    this.id = json.id;
    this.title = json.dcTitle;
    this.creators = _.map(json.mediapackage.creators, function(v, k) { return v; }).join(", ");
    this.date = ocUtils.fromUTCDateStringToFormattedTime(json.mediapackage.start);
  }

  /** Prepare json data delivered by episode service endpoint for template rendering.
   *  @param json -- the json data as it is returned by the server
   */
  function makeRenderDataEpisodes(json) {
    return {
      episodes: $.map(ocUtils.ensureArray(json["search-results"].result), function(elem, i) {
        return new Episode(elem);
      })
    }
  }

  /** JSONP callback for calls to the workflow instances list endpoint.
   *  @param pdata -- "parsed data", the data structure as it is created by the url handler
   */
  this.render = function(pdata) {
    // select template
    $("#controlsFoot").show();
    refreshing = false;

    ocArchive.data = pdata.raw;
    ocArchive.totalEpisodes = pdata.totalCount;
    if (ocArchive.totalEpisodes >= pdata.count) {
      ocArchive.currentShownEpisodes = pdata.count;
    } else {
      ocArchive.currentShownEpisodes = ocArchive.totalEpisodes;
    }
    var result = TrimPath.processDOMTemplate("archiveTemplate", pdata.mkRenderData());
    $('#tableContainer').empty().append(result);

    // display number of matches if filtered
    if (ocArchive.conf.filterText) {
      if (pdata.totalCount == '0') {
        $('#filterRecordingCount').css('color', 'red');
      } else {
        $('#filterRecordingCount').css('color', 'black');
      }
      $('#filterRecordingCount').text(pdata.totalCount + ' found').show();
    } else {
      $('#filterRecordingCount').hide();
    }

    var page = ocArchive.conf.page + 1;
    var pageCount = Math.ceil(pdata.totalCount / ocArchive.conf.pageSize);
    pageCount = pageCount == 0 ? 1 : pageCount;
    $('#pageList').text(page + " of " + pageCount);
    if (page == 1) {
      $('#prevButtons').hide();
      $('#prevText').show();
    } else {
      $('#prevButtons').show();
      $('#prevText').hide();
    }
    if (page == pageCount) {
      $('#nextButtons').hide();
      $('#nextText').show();
    } else {
      $('#nextButtons').show();
      $('#nextText').hide();
    }

    // When table is ready, attach event handlers
    $('.sortable')
        .click(function() {
          var sortDesc = $(this).find('.sort-icon').hasClass('ui-icon-circle-triangle-s');
          var sortField = ($(this).attr('id')).substr(4);
          $('#ocRecordingsTable th .sort-icon')
              .removeClass('ui-icon-circle-triangle-s')
              .removeClass('ui-icon-circle-triangle-n')
              .addClass('ui-icon-triangle-2-n-s');
          if (sortDesc) {
            ocArchive.conf.sortField = sortField;
            ocArchive.conf.sortOrder = 'ASC';
            ocArchive.conf.page = 0;
            ocArchive.reload();
          } else {
            ocArchive.conf.sortField = sortField;
            ocArchive.conf.sortOrder = 'DESC';
            ocArchive.conf.page = 0;
            ocArchive.reload();
          }
        });
    // if results are sorted, display icon indicating sort order in respective table header cell
    if (ocArchive.conf.sortField != null) {
      var th = $('#sort' + ocArchive.conf.sortField);
      $(th).find('.sort-icon').removeClass('ui-icon-triangle-2-n-s');
      if (ocArchive.conf.sortOrder == 'ASC') {
        $(th).find('.sort-icon').addClass('ui-icon-circle-triangle-n');
      } else if (ocArchive.conf.sortOrder == 'DESC') {
        $(th).find('.sort-icon').addClass('ui-icon-circle-triangle-s');
      }
    }
  };

  this.buildURLparams = function() {
    var pa = [];
    for (p in this.conf) {
      if (this.conf[p] != null) {
        pa.push(p + '=' + escape(this.conf[p]));
      }
    }
    return pa.join('&');
  }

  /** Make the page reload with the currently set configuration
   */
  this.reload = function() {
    var url = document.location.href.split('?', 2)[0];
    url += '?' + ocArchive.buildURLparams();
    document.location.href = url;
  }

  /** Start the retract workflow for a certain media package.
   *  @param mediaPackageId -- the id of the package to retract
   */
  this.retract = function(mediaPackageId) {
    $.ajax({
      type: "POST",
      url: "../episode/applyworkflow",
      data: {
        id: mediaPackageId,
        definitionId: "retract"
      },
      complete: function(xhr, status) {
        if (xhr.status == 204) {
          // 204: NO_CONTENT -> ok, expected response
          alert("Started retraction of media package " + mediaPackageId);
          ocArchive.reload();
        } else {
          alert("Unexpected response " + xhr.status);
        }
      }
    });
  };

  this.init = function() {
    // search box
    $('#searchBox').css('width', $('#addButtonsContainer').outerWidth(false) - 10);   // make searchbox beeing aligned with upload/schedule buttons (MH-6519)
    this.searchbox = $("#searchBox").searchbox({
      search: function(text, field) {
        if ($.trim(text) != '') {
          ocArchive.conf.filterField = field;
          ocArchive.conf.filterText = text;
          ocArchive.conf.page = 0;
        }
        refresh();
      },
      clear: function() {
        ocArchive.conf.filterField = '';
        ocArchive.conf.filterText = '';
        ocArchive.conf.page = 0;
        refresh();
      },
      searchText: ocArchive.conf.filterText,
      options: SEARCH_FILTER_FIELDS,
      selectedOption: ocArchive.conf.filterField
    });

    // pager
    $('#pageSize').val(ocArchive.conf.pageSize);

    $('#pageSize').change(function() {
      ocArchive.conf.pageSize = $(this).val();
      ocArchive.conf.page = 0;
      ocArchive.reload();
    });

    $('#page').val(parseInt(ocArchive.conf.page) + 1);

    $('#page').blur(function() {
      ocArchive.gotoPage($(this).val() - 1);
    });

    $('#page').keypress(function(event) {
      if (event.keyCode == '13') {
        event.preventDefault();
        ocArchive.gotoPage($(this).val() - 1);
      }
    });

    //
    refresh();
  };

  //TEMPORARY (quick'n'dirty) PAGING
  this.nextPage = function() {
    numPages = Math.floor(this.totalEpisodes / ocArchive.conf.pageSize);
    if (ocArchive.conf.page < numPages) {
      ocArchive.conf.page++;
    }
    ocArchive.reload();
  }

  this.previousPage = function() {
    if (ocArchive.conf.page > 0) {
      ocArchive.conf.page--;
    }
    ocArchive.reload();
  }

  this.lastPage = function() {
    ocArchive.conf.page = Math.floor(this.totalEpisodes / ocArchive.conf.pageSize);
    ocArchive.reload();
  }

  this.gotoPage = function(page) {
    if (page > (ocArchive.totalEpisodes / ocArchive.conf.pageSize)) {
      ocArchive.lastPage();
    } else {
      if (page < 0) {
        page = 0;
      }
      ocArchive.conf.page = page;
      ocArchive.reload();
    }
  }

  // Initialize after load
  $(this.init);

  return this;
})();
