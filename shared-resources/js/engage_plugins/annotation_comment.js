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
 * @namespace the global Opencast namespace Annotation_Comment
 */
Opencast.Annotation_Comment = (function ()
{
    var mediaPackageId, duration;
    var annotationCommentDisplayed = false;
    var ANNOTATION_COMMENT = "Annotation",
        ANNOTATION_COMMENTHIDE = "Annotation off";
    var annotationType = "comment";
    var oldSlideId = 0;
    var relativeSlideCommentPosition;
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Initializes Annotation Comment
     *              Checks whether Data are available. If not: Hide Annotations
     */

    function initialize()
    {
        
        //enable log
        Opencast.Utils.enableLogging(true);
        
        Opencast.Utils.log("Comments Init");

        // Handler keypress ALT+CTRL+a
        $(document).keyup(function (event)
        {
            if (event.altKey === true && event.ctrlKey === true)
            {
                if (event.which === 65)
                {
                    if(annotationCommentDisplayed === true){
                        openCommentDialog("scrubber");
                    }

                }

            }
        });
        
        Opencast.Utils.log("Comments Init 1");
        // resize handler
        $('#oc_flash-player').bind('doResize', function(e) {
           
            //positioning of the slide comment box
            var flashWidth = $('#oc_flash-player').width();
            var flashHeight = $('#oc_flash-player').height();
            var flashTop = $('#oc_flash-player').offset().top;
            var flashLeft = $('#oc_flash-player').offset().left;
            
            var scWidth = 4/3 * flashHeight;
            var scLeft = flashWidth / 2;
            
            $("#oc_slide-comments").css("position","absolute");
            $("#oc_slide-comments").css("height",flashHeight+"px");
            $("#oc_slide-comments").css("width",scWidth+"px");
            $("#oc_slide-comments").css("left",scLeft+"px");
            $("#oc_slide-comments").css("top",0+"px");
            
            //positioning of the scrubber comment box        
            var analyWidth = flashWidth;
            var analyHeight = 25;
            var analyTop = flashHeight + $("oc_video-player-controls").height() + 20;
            var analyLeft = 0;
            
            $("#annotation_comment").css("position","absolute");
            $("#annotation_comment").css("height",analyHeight+"px");
            $("#annotation_comment").css("width",analyWidth+"px");
            $("#annotation_comment").css("left",analyLeft+"px");
            $("#annotation_comment").css("top",analyTop+"px");
            
        });
        
        // change scrubber position handler
        $('#scrubber').bind('changePosition', function(e) {
            if(Opencast.segments.getCurrentSlideId() !== oldSlideId){
                if(annotationCommentDisplayed){
                    showAnnotation_Comment();
                }                   
                oldSlideId = Opencast.segments.getCurrentSlideId();
            }
            
        });
        $('#draggable').bind('dragstop', function (event, ui){
             if(Opencast.segments.getCurrentSlideId() !== oldSlideId){
                if(annotationCommentDisplayed){
                    showAnnotation_Comment();
                }                   
                oldSlideId = Opencast.segments.getCurrentSlideId();
            }                
        });
        
        //double click handler on slide comment box
        $("#oc_slide-comments").dblclick(function(event){
            if(annotationCommentDisplayed === true){
               
                var mPos = new Object();
                mPos.x = event.pageX - $('#oc_slide-comments').offset().left - 10;
                mPos.y = event.pageY - $('#oc_slide-comments').offset().top - 18;
                
                Opencast.Utils.log("mouse position: "+mPos.x+" "+mPos.y);
                
                var relPos = new Object();
                if($('#oc_slide-comments').width() > 0){
                    relPos.x = ( mPos.x / $('#oc_slide-comments').width() ) * 100;
                }else{
                    relPos.x = 0;
                }
                if($('#oc_slide-comments').height() > 0){
                    relPos.y = ( mPos.y / $('#oc_slide-comments').height() ) * 100;
                }else{
                    relPos.y = 0;
                }    
                
                //addComment(parseInt(Opencast.Player.getCurrentPosition()),"value","slide",relPos.x,relPos.y,Opencast.segments.getCurrentSlideId());
                relativeSlideCommentPosition = relPos;
                openCommentDialog("slide");
                //addComment(parseInt(Opencast.Player.getCurrentPosition()),"value","slide",relPos.x,relPos.y,Opencast.segments.getCurrentSlideId());
            }   
       });

   
          
        // Display the controls
        $('#oc_checkbox-annotation-comment').show();
        $('#oc_label-annotation-comment').show();
        $('#oc_video-view').show();
        
        //check availability
        checkAvailability();

    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description open add comment dialog box
     * @param String commentType
     */
    function openCommentDialog(commentType)
    {
        //TODO check null global varables
        
        //process comment dialog
        //comment form handlers
        var comment_field = $("#oc_comment_field");
        if(commentType === "scrubber"){
            //add scrubber comment dialog
            
            //
            $("#oc_comment_dialog").dialog({
                autoOpen: false,
                height: 300,
                width: 350,
                modal: true,
                buttons: {
                    "Create Scrubber comment": function() {                 
                        addComment(parseInt(Opencast.Player.getCurrentPosition()),
                                   comment_field.val(),
                                   "scrubber"
                                  );
                        $( this ).dialog( "close" );
                    },
                    Cancel: function() {
                        $( this ).dialog( "close" );
                    }
                },
                close: function() {
                    comment_field.val( "" ).removeClass( "ui-state-error" );
                }
            });             
        }else if(commentType === "slide"){
            //add slide comment dialog
            
            //
            $("#oc_comment_dialog").dialog({
                autoOpen: false,
                height: 300,
                width: 350,
                modal: true,
                buttons: {
                    "Create Slide comment": function() {                 
                        addComment(parseInt(Opencast.Player.getCurrentPosition()),
                                   comment_field.val(),
                                   "slide",
                                   relativeSlideCommentPosition.x,
                                   relativeSlideCommentPosition.y,
                                   Opencast.segments.getCurrentSlideId()
                                  );
                        $( this ).dialog( "close" );
                    },
                    Cancel: function() {
                        $( this ).dialog( "close" );
                    }
                },
                close: function() {
                    comment_field.val( "" ).removeClass( "ui-state-error" );
                }
            });            
        }
        
        //open dialog
        $("#oc_comment_dialog").dialog("open");
    }    
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(curPosition,value,type,xPos,yPos,segId)
    {
        
        //comment data [text]:[type]:[xPos]:[yPos]:[segId]
        var data = "";
        if(xPos !== undefined && yPos !== undefined){
            data = value+":"+type+":"+xPos+":"+yPos+":"+segId;
            var markdiv = "<div style='height:100%; width:5px; background-color: #A72123; float: right;'> </div>";
            $("#segment"+segId).html(markdiv);
        }else{
            data = value+":"+type;        
        }
        
        $.ajax(
        {
            type: 'PUT',
            url: "../../annotation/",
            data: "episode="+mediaPackageId+"&type="+annotationType+"&in="+curPosition+"&value="+data+"&out="+curPosition,
            dataType: 'xml',
            success: function (xml)
            {
                Opencast.Utils.log("Add_Comment success");
                showAnnotation_Comment();                           
            },
            error: function (jqXHR, textStatus, errorThrown)
            {
                Opencast.Utils.log("Add_Comment error: "+textStatus);
            }
        });
    }
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Show Annotation_Comment
     */
    function showAnnotation_Comment()
    {
        annotationCommentDisplayed = true;
        // Request JSONP data
        $.ajax(
        {
            url: Opencast.Watch.getAnnotationURL(),
            data: "episode=" + mediaPackageId+"&type="+annotationType+"&limit=1000",
            dataType: 'json',
            jsonp: 'jsonp',
            success: function (data)
            {
                Opencast.Utils.log("Annotation AJAX call: Requesting data succeeded");
                if ((data === undefined) || (data['annotations'] === undefined) || (data['annotations'].annotation === undefined))
                {
                    Opencast.Utils.log("Annotation AJAX call: Data not available");
                    //displayNoAnnotationsAvailable("No data defined");
                }
                else
                {
                    Opencast.Utils.log("Annotation AJAX call: Data available");
                    data['annotations'].duration = duration; // duration is in seconds
                    data['annotations'].nrOfSegments = Opencast.segments.getNumberOfSegments();
                    
                    var scrubberData = new Object();
                    var slideData = new Object();
                    
                    var scrubberArray = new Array();
                    var slideArray = new Array();
                    scrubberData.duration = duration;
                    scrubberData.type = "scrubber";
                    slideData.type = "slide";
                    
                    if(data['annotations'].total > 1){
                        var scCount = 0;
                        var slCount = 0;
                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by colons [text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            var comment = new Object();
                            if(dataArray[1] === "scrubber"){                                
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.inpoint = data['annotations'].annotation[i].inpoint;
                                comment.text = dataArray[0];
                                scrubberArray[scCount] = comment;
                                scCount++;
                            }else if(dataArray[1] === "slide" && dataArray[4] == Opencast.segments.getCurrentSlideId()){
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[2];
                                comment.relPos.y = dataArray[3];
                                comment.text = dataArray[0];
                                slideArray[slCount] = comment;
                                slCount++;                                                          
                            }                      
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            Opencast.Utils.log("Debug 1.2");
                            //split data by colons [text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation.value.split(":");
                            var comment = new Object();
                            if(dataArray[1] === "scrubber"){                              
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.inpoint = data['annotations'].annotation.inpoint;
                                comment.text = dataArray[0];
                                scrubberArray[0] = comment;
                            }else if(dataArray[1] === "slide" && dataArray[4] == Opencast.segments.getCurrentSlideId()){
                                Opencast.Utils.log(data['annotations'].annotation.annotationId +" slide: "+dataArray[4]);
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[2];
                                comment.relPos.y = dataArray[3];
                                comment.text = dataArray[0];
                                slideArray[0] = comment;                       
                            }
                    }
                    
                    scrubberData.comment = scrubberArray;
                    slideData.comment = slideArray;

                    // Create Trimpath Template
                    var scrubberCommentSet = Opencast.Scrubber_CommentPlugin.addAsPlugin($('#annotation_comment'), scrubberData);
                    var slideCommentSet = Opencast.Slide_CommentPlugin.addAsPlugin($('#oc_slide-comments'), slideData);
                    if (!scrubberCommentSet)
                    {
                        Opencast.Utils.log("No scrubberComment template processed");
                        $("#annotation_comment").html("");
                    }
                    else
                    {                                                
                        $("#annotation_comment").show();
                    }
                    
                    if (!slideCommentSet)
                    {
                        Opencast.Utils.log("No slideComment template processed");
                        $("#oc_slide-comments").html("");
                    }
                    else
                    {                        
                        $("#oc_slide-comments").show();
                    }
                                        
                    
                }
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                Opencast.Utils.log("Annotation Ajax call: Requesting data failed");
            }
        });
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description check annotation available
     */   
    function checkAvailability(){    
            // Request JSONP data, annotation available ?
        $.ajax(
        {
            url: Opencast.Watch.getAnnotationURL(),
            data: 'episode=' + mediaPackageId + '&type=' + annotationType+'&limit=1000',
            dataType: 'json',
            jsonp: 'jsonp',
            success: function (data)
            {
                Opencast.Utils.log("Comment AJAX call: Requesting data succeeded");

                
                if ((data !== undefined) && (data['annotations'] !== undefined) && (data['annotations'].annotation !== undefined))
                {
                    Opencast.Utils.log("Annotation AJAX call: Data available");
                    data['annotations'].duration = duration; // duration is in seconds
                    data['annotations'].nrOfSegments = Opencast.segments.getNumberOfSegments();
                    
                    var scrubberData = new Object();
                    var slideData = new Object();
                    
                    var scrubberArray = new Array();
                    var slideArray = new Array();
                    scrubberData.duration = duration;
                    scrubberData.type = "scrubber";
                    slideData.type = "slide";
                    
                    if(data['annotations'].total > 1){
                        var scCount = 0;
                        var slCount = 0;
                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by colons [text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            var comment = new Object();
                            if(dataArray[1] === "scrubber"){                                
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.inpoint = data['annotations'].annotation[i].inpoint;
                                comment.text = dataArray[0];
                                scrubberArray[scCount] = comment;
                                scCount++;
                            }else if(dataArray[1] === "slide"){
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[2];
                                comment.relPos.y = dataArray[3];
                                comment.segId = dataArray[4];
                                comment.text = dataArray[0];
                                slideArray[slCount] = comment;
                                slCount++;                                                          
                            }                      
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            Opencast.Utils.log("Debug 1.2");
                            //split data by colons [text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation.value.split(":");
                            var comment = new Object();
                            if(dataArray[1] === "scrubber"){                              
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.inpoint = data['annotations'].annotation.inpoint;
                                comment.text = dataArray[0];
                                scrubberArray[0] = comment;
                            }else if(dataArray[1] === "slide"){
                                Opencast.Utils.log(data['annotations'].annotation.annotationId +" slide: "+dataArray[4]);
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[2];
                                comment.relPos.y = dataArray[3];
                                comment.segId = dataArray[4];
                                comment.text = dataArray[0];
                                slideArray[0] = comment;                       
                            }
                    }
                    
                    scrubberData.comment = scrubberArray;
                    slideData.comment = slideArray;

                    if(slideData.comment.length > 0){
                        Opencast.Utils.log("Slide Comments available");
                        var reachedSegID = "";
                        $(slideData.comment).each(function (i){
                            Opencast.Utils.log("Comment:"+slideData.comment[i].id+" marked Segement: "+slideData.comment[i].segId);
                            if(reachedSegID !== slideData.comment[i].segId){
                                var markdiv = "<div style='height:100%; width:5px; background-color: #A72123; float: right;'> </div>";
                                $("#segment"+slideData.comment[i].segId).html(markdiv);
                                reachedSegID = slideData.comment[i].segId;
                            }
                            
                        });
                    }
                    
                    Opencast.Analytics.initialize();
                }

            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                Opencast.Utils.log("Comment Ajax call: Requesting data failed");
                Opencast.Analytics.initialize();
            }
        });
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description draw the comment icon with the canvas element
     * @param canvas DOM canvas element
     */   
    function drawBalloon(canvas){
        var ctx = canvas.getContext('2d');
        
        ctx.save();
        ctx.fillStyle = "rgba(167,33,35,0.9)";
        
        ctx.shadowOffsetX = 5;
        ctx.shadowOffsetY = 2;
        ctx.shadowBlur = 10;
        ctx.shadowColor = "rgba(0, 0, 0, 0.8)";
    
        ctx.beginPath();
        ctx.moveTo(70,0);
        ctx.quadraticCurveTo(10,0,10,45);
        ctx.lineTo(10,70);
        ctx.quadraticCurveTo(10,110,60,110);
        ctx.lineTo(150,110);
        ctx.lineTo(130,145);
        ctx.lineTo(200,110);
        ctx.lineTo(230,110);
        ctx.quadraticCurveTo(280,110,280,75);
        ctx.lineTo(280,40);
        ctx.quadraticCurveTo(280,0,220,0);
        ctx.fill();
        ctx.restore();
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description Toggles comment tooltip
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function hoverComment(commentId, commentValue)
    {
        $("#comment-tooltip").html(commentValue);
        var commentLeft = $("#" + commentId).offset().left;
        var commentTop = $("#" + commentId).offset().top;
        var commentWidth = $("#" + commentId).width();
        var tooltipWidth = $("#comment-tooltip").width();
        $("#comment-tooltip").css("left", (commentLeft + commentWidth / 2 - tooltipWidth / 2) + "px");
        $("#comment-tooltip").css("top", commentTop - 25 + "px");
        $("#comment-tooltip").show();
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description Toggles comment tooltip
     * @param commentId the id of the comment
     */
    function hoverOutComment(commentId)
    {
        $("#comment-tooltip").hide();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Displays that no Annotation is available and hides Annotations
     * @param errorDesc Error Description (optional)
     */
    function displayNoAnnotationsAvailable(errorDesc)
    {
        errorDesc = errorDesc || '';
        var optError = (errorDesc != '') ? (": " + errorDesc) : '';
        $("#annotation_comment").html("No Comments available" + optError);
        $('#oc_checkbox-annotation-comment').removeAttr("checked");
        $('#oc_checkbox-annotation-comment').attr('disabled', true);
        $('#oc_checkbox-annotation-comment').hide();
        $('#oc_label-annotation-comment').hide();
        hideAnnotation_Comment();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Hide the Annotation
     */
    function hideAnnotation_Comment()
    {
        $("#annotation_comment").hide();
        $("#oc_slide-comments").hide();
        annotationCommentDisplayed = false;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Toggle Analytics
     */
    function doToggleAnnotation_Comment()
    {
        if (!annotationCommentDisplayed)
        {
            showAnnotation_Comment();
        }
        else
        {
            hideAnnotation_Comment();
        }
        return true;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Set the mediaPackageId
     * @param String mediaPackageId
     */
    function setMediaPackageId(id)
    {
        mediaPackageId = id;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Set the duration
     * @param int duration
     */
    function setDuration(val)
    {
        duration = val;
    }
    
    return {
        initialize: initialize,
        drawBalloon:drawBalloon,
        hideAnnotation_Comment: hideAnnotation_Comment,
        showAnnotation_Comment: showAnnotation_Comment,
        setDuration: setDuration,
        setMediaPackageId: setMediaPackageId,
        hoverComment: hoverComment,
        hoverOutComment: hoverOutComment,
        doToggleAnnotation_Comment: doToggleAnnotation_Comment
    };
}());
