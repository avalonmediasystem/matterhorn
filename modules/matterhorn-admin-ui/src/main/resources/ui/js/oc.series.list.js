/**
 *  Copyright 2009 The Regents of the University of California
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
ocSeriesList = {} || ocSeriesList;
ocSeriesList.views = {} || ocSeriesList.views;
ocSeriesList.views.seriesView = {} || ocSeriesList.seriesView;

ocSeriesList.Configuration = new (function(){
    //default configuration
    this.count = 10;
    this.total = 10;
    this.startPage = 0;  
    this.lastPage = 0;
    this.sort = 'TITLE';  
});

ocSeriesList.init = function(){
	$("#addSeriesButton").button({
	    icons:{
	      primary:"ui-icon-circle-plus"
	    }
	});

	ocSeriesList.askForSeries();		
 	// pager
	$('#pageSize').val(ocSeriesList.Configuration.count);
		    
	$('#pageSize').change(function(){
		ocSeriesList.Configuration.count = $(this).val();
		ocSeriesList.Configuration.startPage = 0;
                ocSeriesList.Configuration.lastPage = Math.ceil(ocSeriesList.Configuration.total / ocSeriesList.Configuration.count);
		ocSeriesList.askForSeries();		
	});
	$('input#goToPage').change(function(){
		var goPage = parseInt($(this).val());
		if ( (!isNaN(goPage))&&(goPage<=ocSeriesList.Configuration.lastPage)) {		
			ocSeriesList.Configuration.startPage = goPage-1;
			ocSeriesList.askForSeries();
		}
		else {
			$(this).val(ocSeriesList.Configuration.startPage+1);
		}
	});}

ocSeriesList.askForSeries = function(){
  if(ocSeriesList.Configuration.startPage == 0) {
    //$('#prevText').show();
    $('#prevButtons').hide();
    
    //$('#nextText').hide();
    $('#nextButtons').show();
  } else if(ocSeriesList.Configuration.startPage == ocSeriesList.Configuration.lastPage) {
    //$('#nextText').show();
    $('#nextButtons').hide();
  
    //$('#prevText').hide();
    $('#prevButtons').show();
  } else {
    //$('#prevText').hide();
    $('#prevButtons').show();
    
    //$('#nextText').hide();
    $('#nextButtons').show();
  }
	$.ajax({
		type : 'GET',
		dataType : 'json',
		url : "/series/series.json?"+ocSeriesList.buildURLparams(),
		success : ocSeriesList.buildSeriesView,
		error : ocSeriesList.showSeriesTable
	});
}

ocSeriesList.showSeriesTable = function(){
	var result = TrimPath.processDOMTemplate("seriesTemplate", ocSeriesList.views);
	$('#seriesTableContainer').html(result);
	
	//$('#pageList').text((ocSeriesList.Configuration.startPage+1) + " of " + (ocSeriesList.Configuration.lastPage + 1));
	$('#pageCount').text(ocSeriesList.Configuration.lastPage+1);
	$('input#goToPage').val((ocSeriesList.Configuration.startPage+1));

       $('.sortable').click( function() {
      		var sortDesc = $(this).find('.sort-icon').hasClass('ui-icon-circle-triangle-s');
	       var sortField = $(this).attr('id').substr(4);		
		$( '#seriesTable th .sort-icon' )
			.removeClass('ui-icon-circle-triangle-s')
	      		.removeClass('ui-icon-circle-triangle-n')
	      		.addClass('ui-icon-triangle-2-n-s');
      		if (sortDesc) {
			ocSeriesList.Configuration.sort = sortField.toUpperCase();
			ocSeriesList.Configuration.startPage = 0;
			ocSeriesList.askForSeries();		
      		} else {
			ocSeriesList.Configuration.sort = sortField.toUpperCase()+"_DESC";
			ocSeriesList.Configuration.startPage = 0;
			ocSeriesList.askForSeries();		
		}
    });

    if (ocSeriesList.Configuration.sort != null) {
	var sortField = ocSeriesList.Configuration.sort.split("_")[0]; 
	var sortOrder = ocSeriesList.Configuration.sort.split("_")[1];

      var th = $('#sort' + sortField[0]+sortField.toLowerCase().substring(1,sortField.length));
      $(th).find('.sort-icon').removeClass('ui-icon-triangle-2-n-s');
      if (sortOrder == 'DESC') {
        $(th).find('.sort-icon').addClass('ui-icon-circle-triangle-s');
      } else {
        $(th).find('.sort-icon').addClass('ui-icon-circle-triangle-n');
      }
    }
}

ocSeriesList.buildURLparams = function() {
    var pa = [];
    for (p in ocSeriesList.Configuration) {
      if (ocSeriesList.Configuration[p] != null) {	
        pa.push(p + '=' + escape(this.Configuration[p]));
      }
    }
    return pa.join('&');
  }
 

ocSeriesList.buildSeriesView = function(data) {
  ocSeriesList.Configuration.total = data.totalCount;
  ocSeriesList.Configuration.lastPage = Math.ceil(ocSeriesList.Configuration.total / ocSeriesList.Configuration.count) - 1;
//  ocSeriesList.Configuration.totalPages = Math.floor(parseInt(ocSeriesList.Configuration.total) / parseInt(ocSeriesList.Configuration.count));
  catalogs = data.catalogs;
  ocSeriesList.views.seriesView = {};
  ocUtils.log($.isArray(catalogs));
  for(var i = 0; i < catalogs.length; i++) {
    var id = catalogs[i]['http://purl.org/dc/terms/']['identifier'][0].value;
    var s = ocSeriesList.views.seriesView[id] = {};
    s.id = id
    for(var key in catalogs[i]['http://purl.org/dc/terms/']) {
      if(key === 'title'){
        s.title = catalogs[i]['http://purl.org/dc/terms/'][key][0].value
      } else if(key === 'creator') {
        s.creator = catalogs[i]['http://purl.org/dc/terms/'][key][0].value
      } else if(key  === 'contributor') {
        s.contributor = catalogs[i]['http://purl.org/dc/terms/'][key][0].value
      }
    }
  }

	ocSeriesList.showSeriesTable();	
}

ocSeriesList.previousPage = function(){
	if(ocSeriesList.Configuration.startPage > 0) {
	  ocSeriesList.Configuration.startPage--;
	  ocSeriesList.askForSeries();
	}
}

ocSeriesList.nextPage = function(){
    numPages = Math.floor(ocSeriesList.Configuration.total / ocSeriesList.Configuration.count);
    if( ocSeriesList.Configuration.startPage < numPages ) {
      ocSeriesList.Configuration.startPage++;
      ocSeriesList.askForSeries();
    }
}

ocSeriesList.deleteSeries = function(seriesId, title) {
  if(confirm('Are you sure you want to delete the series "' + title + '"?')){
    $.ajax({
      type: 'DELETE',
      url: '/series/' + seriesId,
      error: function(XHR,status,e){
        alert('Could not remove series "' + title + '"');
      },
      success: function(data) {
        location.reload();
      }
    });
  }
}
