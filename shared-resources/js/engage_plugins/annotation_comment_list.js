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
 * @namespace the global Opencast namespace Annotation_Comment_List
 */
Opencast.Annotation_Comment_List = (function ()
{
    var COMMENTNOHIDE = "Comments",
        COMMENTSHIDE = "Hide Comments";
    var defaultText = "Type your comment here";
    var defaultNameText = "Your name"
    var comment_list_show = false;
    var mediaPackageId;
    var annotationType = "comment";
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Initializes the segments view
     */
    function initialize()
    {

        $.log("Comment List init");
        
        // //Add Comment Form // //
        $("#oc-comments-list-textbox").val(defaultText);
  		$("#oc-comments-list-namebox").val(defaultNameText);
        $("#oc-comments-list-submit").click(function(){        
            submitComment();         
        });
        
        // Handler keypress CTRL+Enter on textbox
        $("#oc-comments-list-textbox").keyup(function (event)
        {
            if (event.ctrlKey === true)
            {
                if (event.which === 13)
                {
                    submitComment();
                }

            }
        });
        
        // //
        
        //focus and mark text by click on textbox
        $("#oc-comments-list-textbox").click(function(){        
            $("#oc-comments-list-textbox").focus();
            $("#oc-comments-list-textbox").select();          
        });

        $("#oc-comments-list-namebox").click(function(){        
            $("#oc-comments-list-namebox").focus();
            $("#oc-comments-list-namebox").select();          
        });
        
        //
        $.log("init list bindings");
        //$('#scrubber').bind('changePosition', updateTimeText());
        
        //$('#draggable').bind('dragstop', updateTimeText());
    }
    
    function updateTimeText()
    {
    	$.log("updateTimeTExt");
    	var infoTime = "00:00:00";
    	$.log("updateTimeTExt1");
    	if($('#oc_flash-player') !== undefind){
    		$.log("updateTimeTExt2");
    		$.log("updateTimeTExt3"+$('#oc_flash-player'));
	    	var left = e.pageX ;
	    	var playheadPercent = ( left - $('#oc_flash-player').offset().left ) / $('#oc_flash-player').width();
			var commentAtInSeconds = Math.round(playheadPercent * Opencast.Player.getDuration());
			var infoTime = $.formatSeconds(commentAtInSeconds);
    	}
    	$.log("updateTimeTExt4");
		if($("#oc-comments-list-submit-timed") !== undefind){
			$("#oc-comments-list-submit-timed").value("Add comment at "+infoTime);
		}
		
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description submitComment
     */
    function submitComment()
    { 
       var textBoxValue = $("#oc-comments-list-textbox").val();
       var nameBoxValue = $("#oc-comments-list-namebox").val();
       $.log("click submit "+textBoxValue + " "+ nameBoxValue);
       if(textBoxValue !== defaultText && nameBoxValue !== defaultNameText ){             
           addComment(textBoxValue,nameBoxValue,"normal");
           $("#oc-comments-list-textbox").val(defaultText);
           $("#oc-comments-list-namebox").val(defaultNameText);
       }else if(textBoxValue === defaultText){
            $("#oc-comments-list-textbox").focus();
            $("#oc-comments-list-textbox").select(); 
       }else if(nameBoxValue === defaultNameText){
            $("#oc-comments-list-namebox").focus();
            $("#oc-comments-list-namebox").select(); 
       }else{
       	$.log("Opencast.Annotation_Comment_List: illegal input state");
       }      
    }    
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(value,user,type)
    {      
        /* // Get user from system
        var user = "Anonymous";
        if(Opencast.Player.getUserId() !== null){
            user = Opencast.Player.getUserId();
        }
        */
        //comment data [user]:[text]:[type]
        data = user+":"+value+":"+type;        
        
        $.ajax(
        {
            type: 'PUT',
            url: "../../annotation/",
            data: "episode="+mediaPackageId+"&type="+annotationType+"&in="+0+"&value="+data+"&out="+0,
            dataType: 'xml',
            success: function (xml)
            {
                $.log("Add_Comment success");
                showComments();                           
            },
            error: function (jqXHR, textStatus, errorThrown)
            {
                $.log("Add_Comment error: "+textStatus);
            }
        });
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Show the Annotation_Comment_List
     */
    function showComments()
    {
        $.log("show commment list");
        // Hide other Tabs
        Opencast.Description.hideDescription();
        Opencast.segments.hideSegments();
        Opencast.segments_text.hideSegmentsText();
        Opencast.search.hideSearch();
        // Change Tab Caption
        $('#oc_btn-comments-tab').attr(
        {
            title: COMMENTSHIDE
        });
        comment_list_show = true;
        $('#oc_btn-comments-tab').html(COMMENTSHIDE);
        $("#oc_btn-comments-tab").attr('aria-pressed', 'true');
        // Show a loading Image
        $('#oc_comments-list-wrapper').show();
        $('#oc_comments-list-loading').show();
        $('#oc-comments-list-header').hide();
        $('#oc-comments-list').hide();
        $('#oc-comments-list-add-form').hide();
        
        // Request JSONP data
        $.ajax(
        {
            url: Opencast.Watch.getAnnotationURL(),
            data: "episode=" + mediaPackageId+"&type="+annotationType+"&limit=1000",
            dataType: 'json',
            jsonp: 'jsonp',
            success: function (data)
            {
                $.log("Annotation AJAX call: Requesting data succeeded");
                
                var count = 0;
                var slideCount = 0;
                var scrubberCount = 0;
                var normalCount = 0;
                
                if ((data === undefined) || (data['annotations'] === undefined) || (data['annotations'].annotation === undefined))
                {
                    $.log("Annotation AJAX call: Data not available");
                    //show nothing
                    $('#oc-comments-list').html("");
                    //displayNoAnnotationsAvailable("No data defined");
                }
                else
                {
                    $.log("Annotation AJAX call: Data available");
                    
                    var commentData = new Object();                  
                    var commentArray = new Array();
                    
                    $.log("Debug 1");
                    if(data['annotations'].total > 1){

                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            var comment = new Object();
                            
                            comment.id = data['annotations'].annotation[i].annotationId;
                            comment.inpoint = data['annotations'].annotation[i].inpoint;
                            var created = data['annotations'].annotation[i].created;         
                            var dateCr = $.dateStringToDate(created);
                            comment.created = $.getDateString(dateCr) + " " + $.getTimeString(dateCr);
                            comment.user = dataArray[0];
                            comment.text = dataArray[1];
                            comment.type = dataArray[2];                            
                            if(dataArray[2] === "slide"){
                                comment.slide = dataArray[5];
                                slideCount++;                                    
                            }else if(dataArray[2] === "scrubber"){
                                scrubberCount++;
                            }else if(dataArray[2] === "normal"){
                                normalCount++;
                            }
                            commentArray[count] = comment;
                            count++;                                             
                        });                       
                    }else if(data['annotations'].total !== 0){
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation.value.split(":");
                            var comment = new Object();
                            comment.id = data['annotations'].annotation.annotationId;
                            comment.inpoint = data['annotations'].annotation.inpoint;
                            var created = data['annotations'].annotation.created;         
                            var dateCr = $.dateStringToDate(created);
                            comment.created = $.getDateString(dateCr) + " " + $.getTimeString(dateCr);
                            comment.user = dataArray[0];
                            comment.text = dataArray[1];
                            comment.type = dataArray[2];                            
                            if(dataArray[2] === "slide"){
                                comment.slide = dataArray[5];
                                slideCount++;                                    
                            }else if(dataArray[2] === "scrubber"){
                                scrubberCount++;
                            }else if(dataArray[2] === "normal"){
                                normalCount++;
                            }
                            commentArray[0] = comment;
                            count++;   
                    }
                    
                    commentData.comment = commentArray;
                    
                    $.log("commentList template process");
                    // Create Trimpath Template
                    var commentListSet = Opencast.Annotation_Comment_List_Plugin.addAsPlugin($('#oc-comments-list'), commentData);

                    if (!commentListSet)
                    {
                        $.log("No commentList template processed");
                    }        
                }
                
                //process header
                if(count === 1){
                    $("#oc-comments-list-header-top").html(count+" Comment");
                }else{
                    $("#oc-comments-list-header-top").html(count+" Comments");
                }
                var line = "";
                if(slideCount === 1){
                    line += slideCount+ " slide comment, ";
                }else{
                    line += slideCount+ " slide comments, ";
                }
                if(scrubberCount === 1){
                    line += scrubberCount+ " timed comment and ";
                }else{
                    line += scrubberCount+ " timed comments and ";
                }
                if(normalCount === 1){
                    line += normalCount+ " regular comment";
                }else{
                    line += normalCount+ " regular comments ";
                }
                $("#oc-comments-list-header-bottom").html(line);
                
                $('#oc_comments-list-loading').hide();
                $('#oc-comments-list-header').show();
                $('#oc-comments-list').show();
                $('#oc-comments-list-add-form').show();
                //scroll down
                $(window).scrollTop( $('#oc_btn-comments-tab').offset().top - 10 );

            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                $.log("Comment Ajax call: Requesting data failed "+xhr+" "+ ajaxOptions+" "+ thrownError);
            }
        });
        
        
        
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Hide the Annotation_Comment_List
     */
    function hideComments()
    {
        $.log("hide commment list");
        // Change Tab Caption
        comment_list_show = false;
        $('#oc_btn-comments-tab').attr(
        {
            title: COMMENTNOHIDE
        });
        $('#oc_btn-comments-tab').html(COMMENTNOHIDE);
        $("#oc_btn-comments-tab").attr('aria-pressed', 'false');
        $('#oc_comments-list-wrapper').hide();
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Toggle the Annotation_Comment_List
     */
    function doToggleComments()
    {        
        
        $.log("toggle commment list");
        if (comment_list_show === false)
        {
            showComments();
        }
        else
        {
            hideComments();
        }
    }

    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description handle click in the comment list
     * @param commentID, commentValue, commentTime, commentSlide, userId, type
     */
    function clickCommentList(commentID, commentValue, commentTime, commentSlide, userId, type)
    {
        goToComment(commentID, commentValue, commentTime, commentSlide, userId, type);
        //var toolTop = $("#comment-row-"+commentID).offset().top - 10;
        //var toolLeft = $("#comment-row-"+commentID).offset().left + 400;
        
        //$("#oc-comments-list-item-tooltip").css("left", toolLeft + "px");
        //$("#oc-comments-list-item-tooltip").css("top", toolTop + "px");        
        //$("#oc-comments-list-item-tooltip").show();
        //$('#oc-comments-list-item-tooltip-show').click(function(){
        //    goToComment(commentID, commentValue, commentTime, commentSlide, userId, type);
        //});
        //$('#oc-comments-list-item-tooltip-delete').click(function(){
        //    deleteComment(commentID);
        //});
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description handle hover out the comment list
     */
    function hoverOutCommentList()
    {
        $('#oc-comments-list-item-tooltip').hide();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description deletes comment
     */
    function deleteComment(commentID)
    {
        // ajax DELETE Request
        $.ajax(
        {
            type: 'DELETE',
            url: "../../annotation/"+commentID,
            statusCode: {
                200: function() {
                    $.log("Comment DELETE Ajax call: Request success");
                    showComments();
                    if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === true){
                        Opencast.Annotation_Comment.showAnnotation_Comment();
                    }
                }
            },
            complete:
                function(jqXHR, textStatus){
                    $.log("Comment DELETE Ajax call: Request success");
                    showComments();
                    if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === true){
                        Opencast.Annotation_Comment.showAnnotation_Comment();
                    }
                }
            
        });
    }   
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description go to a comment
     * @param commentID, commentValue, commentTime, commentSlide, userId, type
     */
    function goToComment(commentID, commentValue, commentTime, commentSlide, userId, type)
    {
        if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === false){
            $("#oc_checkbox-annotation-comment").attr('checked', true);
            Opencast.Annotation_Comment.showAnnotation_Comment();
        }
        
        //seek player if timed
        if(type === "scrubber"){
            Opencast.Watch.seekSegment(parseInt(commentTime)-2);
            Opencast.Annotation_Comment.hoverComment("scComment"+commentID, commentValue, commentTime, userId);
            //scroll to comment
            $(window).scrollTop( 0 );
        }else if(type === "slide"){
            Opencast.Watch.seekSegment(parseInt(commentTime));
            Opencast.Annotation_Comment.hoverSlideComment("slideComment"+commentID, commentValue, userId, commentSlide);
            //scroll to comment
            $(window).scrollTop( 0 );
        }      
    }
    
        /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Set the mediaPackageId
     * @param String mediaPackageId
     */
    function setMediaPackageId(id)
    {
        mediaPackageId = id;
    }
    
    return {
        initialize: initialize,
        showComments: showComments,
        hideComments: hideComments,
        clickCommentList: clickCommentList,
        deleteComment: deleteComment,
        hoverOutCommentList: hoverOutCommentList,
        goToComment: goToComment,
        doToggleComments: doToggleComments,
        setMediaPackageId: setMediaPackageId
    };
}());
