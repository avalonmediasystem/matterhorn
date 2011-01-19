var Opencast = Opencast || {};

Opencast.WorkflowInspect = (function() {

  this.WORKFLOW_INSTANCE_URL = '../workflow/rest/instance/';
  this.SCHEDULER_URL = '../scheduler/rest/';

  var $container;       // id of the target container
  var templateId;
  var instanceView;     // view of the workflow instance data
  var targetView;       // indicates if technical details or info page should be rendered ('details' | 'info')

  this.renderInfo = function(id, container, template) {
    targetView = 'info';
    templateId = template;
    $container = $('#' + container);
    requestWorkflow(id);
  }

  this.renderDetails = function(id, container, template) {
    targetView = 'details';
    templateId = template;
    $container = $('#' + container);
    requestWorkflow(id);
  }

  function requestWorkflow(id) {
    $.ajax({
      url : this.WORKFLOW_INSTANCE_URL + id + ".json?jsonp=?",
      dataType: 'jsonp',
      jsonp: 'jsonp',
      success: Opencast.WorkflowInspect.rx
    });
  }

  /** JSONP recieve function
   *
   */
  this.rx = function(data) {
    instanceView = buildInstanceView(data.workflow);
    if (targetView == 'details') {
      renderDetailsView(instanceView, $container);
    } else if (targetView == 'info') {
      renderInfoView(instanceView, $container);
    }
  }

  /** Build view of workflow instance data
   *
   */
  function buildInstanceView(workflow) {
    var out = Opencast.RenderUtils.extractScalars(workflow);
    out.config = buildConfigObject(workflow.configurations.configuration);

    // Operations
    var ops = Opencast.RenderUtils.ensureArray(workflow.operations.operation);
    $.each(ops, function(index, op) {
      if (op.configurations !== undefined && op.configurations.configuration !== undefined) {
        op.configurations = buildConfigObject(op.configurations.configuration);
      } else {
        op.configurations = [];
      }
    });
    out.operations = ops;

    if (workflow.mediapackage) {
      var mp = workflow.mediapackage;

      // prepare info object for View Info (top most box)
      out.info = {};
      out.info.title = mp.title;
      out.info.episodeDC = false;
      out.info.seriesDC = false;
      if (mp.creators) {
        out.info.creators = Opencast.RenderUtils.ensureArray(mp.creators.creator).join(', ');
      } else {
        out.info.creators = [];
      }
      out.info.department = '';

      // Attachments
      mp.attachments = Opencast.RenderUtils.ensureArray(mp.attachments.attachment);

      // Tracks
      if (!mp.media) {
        mp.media = {};
      }
      mp.media.track = Opencast.RenderUtils.ensureArray(mp.media.track);

      // Metadata catalogs
      if (mp.metadata) {
        mp.metadata.catalog = Opencast.RenderUtils.ensureArray(mp.metadata.catalog);
        $.each(mp.metadata.catalog, function(index, catalog) {
          if (catalog.type == 'dublincore/episode') {
            out.info.episodeDC = catalog.url;
          } else if (catalog.type == 'dublincore/series') {
            out.info.seriesDC = catalog.url;
          }
        });
      } else {
        mp.metadata = {};
        mp.metadata.catalog = [];
      }

      // 'flatten' encoder and scantype properties
      try {
        $.each(mp.media.track, function(index, track) {
          if (track.audio && track.audio.encoder) {
            track.audio.encoder = track.audio.encoder.type;
          }
          if (track.video) {
            if (track.video.encoder) track.video.encoder = track.video.encoder.type;
            if (track.video.scantype) track.video.scantype = track.video.scantype.type;
          }
        });
      } catch (e) {
        ocUtils.log('Could not flatten encoder/scantype properties of tracks');
      }
      out.mediapackage = mp;
    } else {
      out.info = {};
      out.mediapackage = false;
    }

    // in case of an 'upcoming event' episode dublin core catalog is obtained from scheduler service
    if (workflow.template == 'scheduling') {
      out.info.episodeDC = this.SCHEDULER_URL + workflow.id + '/dublincore';
    }

    return {
      workflow : out
    };
  }

  /** Render workflow view to specified container
   *
   */
  function renderDetailsView(workflow, $target) {
    var result = TrimPath.processDOMTemplate(templateId, workflow);
    $target.append(result);
    $target.tabs();
    $('.unfoldable-tr').click(function() {
      var $content = $(this).find('.unfoldable-content');
      var unfolded = $content.is(':visible');
      $('.unfoldable-content').hide('fast');
      if (!unfolded) {
        $content.show('fast');
      }
    });
    renderWorkflowPerformance(workflow);
  }

  /** Render workflow info page (View Info) to specified container
   *
   */
  function renderInfoView(workflow, $target) {
    var result = TrimPath.processDOMTemplate(templateId, workflow);
    $target.append(result);

    // Render Episode DC if present
    if (workflow.workflow.info.episodeDC) {
      $.ajax({
        url : workflow.workflow.info.episodeDC,
        type : 'GET',
        dataType : 'xml',
        error : function() {
          $('#episodeContainer').text('Error: Could not retrieve Episode Dublin Core Catalog');
        },
        success : function(data) {
          var $table = $('<table>');
          $(data).find('dublincore').children().each(function() {
            var tagname = $(this).context.tagName.split(':')[1];
            if (tagname == 'contributor') {                   // Sponsoring Department (contributor) is not part workflow info so we have to get it from here
              $('#departmentField').text($(this).text());
            } else if (tagname != 'title' && tagname != 'creator') {    
              var $row = $('<tr></tr>');
              $('<td></td>').addClass('td-key').text(tagname + ':').appendTo($row);
              $('<td></td>').addClass('td-value').text($(this).text()).appendTo($row);
              $row.appendTo($table);
            }
          });
          $table.appendTo('#episodeContainer');
        }
      });
    }

    // Render Series DC if present
    if (workflow.workflow.info.seriesDC) {
      $.ajax({
        url : workflow.workflow.info.seriesDC,
        type : 'GET',
        dataType : 'xml',
        error : function() {
          $('#episodeContainer').text('Error: Could not retrieve Episode Dublin Core Catalog');
        },
        success : function(data) {
          var $table = $('<table>');
          $(data).find('dublincore').children().each(function() {
            var $row = $('<tr></tr>');
            var tagname = $(this).context.tagName.split(':')[1] + ':';
            $('<td></td>').addClass('td-key').text(tagname).appendTo($row);
            $('<td></td>').addClass('td-value').text($(this).text()).appendTo($row);
            $row.appendTo($table);
          });
          $table.appendTo('#seriesContainer');
        }
      });
    }
  }

  /** render workflow performance chart
   */
  function renderWorkflowPerformance(data) {
    // Make a graph object with canvas id and width
    var g = new Bluff.SideStackedBar('graph', '600x300');

    // Set theme and options
    //g.theme_greyscale();
    g.title = 'Processing times for ' + data.workflow.mediapackage.title;
    g.x_axis_label = 'Seconds';

    // Add data and labels
    var queue = [];
    var run = [];
    var labels = {};
    jQuery.each(data.workflow.operations, function(index, operationInstance) {
      var op = data.workflow.operations[index];
      if(op.state == 'SUCCEEDED') {
        var runtime = (op.completed - op.started) / 1000;
        if(runtime < 1) {
          return;
        }
        run.push(runtime);
        queue.push(op['time-in-queue'] / 1000);
        labels['' + run.length-1] = op.id;
      }
    });

    g.data('Queue', queue);
    g.data('Run', run);
    g.labels = labels;

    // Render the graph
    g.draw();
  }

  /** Build an object that can be rendered easily from the Configuration objects
   *  of Workflow, Operation etc. If the same key is encountered twice ore more
   *  the field is converted to an array.
   */
  function buildConfigObject(data) {
    var out = {};
    data = Opencast.RenderUtils.ensureArray(data);
    $.each(data, function(index, member) {
      if ($.isArray(out[member.key])) {
        out[member.key].push(member.value);
      } else if (out[member.key] !== undefined) {
        out[member.key] = [out[member.key], member.value];
      } else {
        out[member.key] = member['$'];
      }
    });
    return out;
  }

  return this;
}());

Opencast.RenderUtils = (function() {

  /** Returns
   *    [obj] if an object is passed
   *    obj if obj is already an Array
   *    [] if obj === undefined or something eles goes wrong
   */
  this.ensureArray = function(obj) {
    try {
      if (obj === undefined) {
        return [];
      } else if ($.isArray(obj)) {
        return obj;
      } else {
        return [obj];
      }
    } catch (e) {
      return [];
    }
  }

  /** Returns either the value of obj if obj is a scalar or a ',' separated list
   *  of the scalar values of obj if obj is an Array
   */
  this.ensureString = function(obj) {
    if ($.isArray(obj)) {
      return obj.join(', ');
    } else {
      return '' + obj;
    }
  }

  /** Returns an object containing all scalar members of obj.
   *
   */
  this.extractScalars = function(obj) {
    var out = {};
    for (var key in obj) {
      var value = obj[key];
      if (typeof value == 'string' || typeof value == 'number') {
        out[key] = obj[key];
      }
    }
    return out;
  }

  return this;
}());