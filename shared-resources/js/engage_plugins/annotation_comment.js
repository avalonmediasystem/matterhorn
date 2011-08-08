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
    var clickedOnHoverBar = false;
    var infoTime = "";
    var commentAtInSeconds;
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Initializes Annotation Comment
     *              Checks whether Data are available. If not: Hide Annotations
     */

    function initialize()
    {
        
        //enable log
        //Opencast.Utils.enableLogging(true);
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
            
        });
        
        //// HOVER UI ////
        
        $("#oc-comment-hover-box").mouseenter(function(e){
           if(clickedOnHoverBar === false){
                $("#comment-Info").show();
           }
            
        });
        
        //mouse over scrubber channel
        $("#oc-comment-hover-box").mousemove(function(e){
            if(clickedOnHoverBar === false){
                  var left = e.pageX ;
                  var top = $("#data").offset().top - 105;
                  $("#comment-Info").offset({ top: top, left: left });               
                  var playheadPercent = ( left - $('#oc_flash-player').offset().left ) / $('#oc_flash-player').width();
                  commentAtInSeconds = Math.round(playheadPercent * Opencast.Player.getDuration());
                  infoTime = Opencast.Utils.formatSeconds(commentAtInSeconds);           
                  $("#cm-info-time").html(infoTime);  
                  $("#comment-Info").show();         
            }                          
        });
        
        $("#oc-comment-hover-box").mouseout(function(e){
           if(clickedOnHoverBar === false){
                $("#comment-Info").hide();
           }    
           
           
            
        });
        
        $("#oc-comment-hover-box").click(function(){
            $('#cm-add-box').attr(
                {
                    title: "Add timed comment"
                });
            $("#cm-add-box").show();
            $("#cm-add-box").focus();
            $("#cm-add-box").select();
            $("#cm-info-hover").hide();
            clickedOnHoverBar = true;           
            //Info Text
            var infoText = Opencast.Player.getUserId() + " at " + infoTime;           
            $("#oc-comment-add-header-text").html(infoText);
            
        });
        
        $(".oc-comment-add-exit").click(function(){
            // hide info box -> hide add box show hover tooltip cm-info-box
            $("#comment-Info").hide();
            $("#cm-add-box").hide();
            $("#cm-info-box").hide();
            $("#cm-info-hover").show();
            clickedOnHoverBar = false;
        });
        
        $("#oc-comment-add-submit").click(function(){
            // hide info box -> hide add box show hover tooltip
            $("#comment-Info").hide();
            $("#cm-add-box").hide();
            $("#cm-info-hover").show();
            clickedOnHoverBar = false;
            var commentValue = $("#oc-comment-add-textbox").val();
            // back to default
            $("#oc-comment-add-textbox").val("Type your comment here");
            if($('#cm-add-box').attr("title") === "Add timed comment"){
                //add scrubber comment
                addComment(parseInt(commentAtInSeconds),commentValue,"scrubber");                
            }else if($('#cm-add-box').attr("title") === "Add slide comment"){
                //add slide comment
                addComment(parseInt(Opencast.Player.getCurrentPosition()),
                           commentValue,
                           "slide",
                           relativeSlideCommentPosition.x,
                           relativeSlideCommentPosition.y,
                           Opencast.segments.getCurrentSlideId()
                          );                              
            }
        });
        
        //// HOVER UI END ////
        
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
                // set global variable
                relativeSlideCommentPosition = relPos;
                
                $('#cm-add-box').attr(
                {
                    title: "Add slide comment"
                });
                var ciLeft = event.pageX;
                var ciTop = event.pageY-100;
                Opencast.Utils.log("dblclick "+ciLeft+" "+ciTop);
                $("#comment-Info").css("left", ciLeft+"px");
                $("#comment-Info").css("top", ciTop+"px");
                $("#comment-Info").show();  
                
                
                $("#cm-add-box").show();
                $("#cm-add-box").focus();
                $("#cm-add-box").select();
                $("#cm-info-hover").hide();
                clickedOnHoverBar = true;           
                //Info Text
                var curSlide = Opencast.segments.getCurrentSlideId()+1;
                var infoText = Opencast.Player.getUserId() + " at slide " + curSlide;    
                $("#oc-comment-add-header-text").html(infoText);             
                
            }   
       });
        
        // Display the controls
        $('#oc_checkbox-annotation-comment').show();    // checkbox
        $('#oc_label-annotation-comment').show();       // 
        $('#oc_video-view').show();                     // slide comments
        //$("#oc_ui_tabs").tabs('enable', 3);             // comment tab
        
        //check availability
        checkAvailability();

    }    
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(curPosition,value,type,xPos,yPos,segId)
    {
        
        //comment data [user]:[text]:[type]:[xPos]:[yPos]:[segId]
        var data = "";
        if(xPos !== undefined && yPos !== undefined){
            data = Opencast.Player.getUserId()+":"+value+":"+type+":"+xPos+":"+yPos+":"+segId;
            var markdiv = "<div style='height:100%; width:5px; background-color: #A72123; float: right;'> </div>";
            $("#segment"+segId).html(markdiv);
        }else{
            data = Opencast.Player.getUserId()+":"+value+":"+type;        
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
                
                var comment_list_show = $('#oc_btn-comments-tab').attr("title");
                if(comment_list_show == "Hide Comments"){
                    Opencast.Annotation_Comment_List.showComments();
                }                    
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
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            var comment = new Object();
                            comment.user = dataArray[0];
                            comment.id = data['annotations'].annotation[i].annotationId;
                            comment.text = dataArray[1];
                            if(dataArray[2] === "scrubber"){                                                              
                                comment.inpoint = data['annotations'].annotation[i].inpoint;                        
                                scrubberArray[scCount] = comment;
                                scCount++;
                            }else if(dataArray[2] === "slide" && dataArray[5] == Opencast.segments.getCurrentSlideId()){
                                comment.slideNr = dataArray[5];
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[3];
                                comment.relPos.y = dataArray[4];                  
                                slideArray[slCount] = comment;
                                slCount++;                                                          
                            }                      
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation.value.split(":");
                            var comment = new Object();
                            comment.id = data['annotations'].annotation.annotationId;
                            comment.user = dataArray[0];
                            comment.text = dataArray[1];
                            if(dataArray[2] === "scrubber"){                              
                                comment.inpoint = data['annotations'].annotation.inpoint;
                                scrubberArray[0] = comment;
                            }else if(dataArray[2] === "slide" && dataArray[5] == Opencast.segments.getCurrentSlideId()){
                                comment.slideNr = dataArray[5];
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[3];
                                comment.relPos.y = dataArray[4];
                                comment.text = dataArray[1];
                                slideArray[0] = comment;                       
                            }
                    }
                    
                    scrubberData.comment = scrubberArray;
                    slideData.comment = slideArray;

                    // Create Trimpath Template
                    var scrubberCommentSet = Opencast.Scrubber_CommentPlugin.addAsPlugin($('#oc-comment-hover-box'), scrubberData);
                    var slideCommentSet = Opencast.Slide_CommentPlugin.addAsPlugin($('#oc_slide-comments'), slideData);
                    if (!scrubberCommentSet)
                    {
                        Opencast.Utils.log("No scrubberComment template processed");
                        //$("#oc-comment-hover-box").html("");
                    }
                    else
                    {                                                
                        //$("#oc-comment-hover-box").show();
                    }
                    
                    if (!slideCommentSet)
                    {
                        Opencast.Utils.log("No slideComment template processed");
                        $("#oc_slide-comments").html("");
                    }
                    else
                    {                        
                        //$("#oc_slide-comments").show();
                    }
                                        
                    
                }
                $("#oc_slide-comments").show();
                $("#oc-comment-hover-box").show();
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                Opencast.Utils.log("Comment Ajax call: Requesting data failed "+xhr+" "+ ajaxOptions+" "+ thrownError);
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
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            var comment = new Object();
                            if(dataArray[2] === "scrubber"){                                
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.inpoint = data['annotations'].annotation[i].inpoint;
                                comment.text = dataArray[1];
                                scrubberArray[scCount] = comment;
                                scCount++;
                            }else if(dataArray[2] === "slide"){
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[3];
                                comment.relPos.y = dataArray[4];
                                comment.segId = dataArray[5];
                                comment.text = dataArray[1];
                                slideArray[slCount] = comment;
                                slCount++;                                                          
                            }                      
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation.value.split(":");
                            var comment = new Object();
                            if(dataArray[2] === "scrubber"){                              
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.inpoint = data['annotations'].annotation.inpoint;
                                comment.text = dataArray[1];
                                scrubberArray[0] = comment;
                            }else if(dataArray[2] === "slide"){
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[3];
                                comment.relPos.y = dataArray[4];
                                comment.segId = dataArray[5];
                                comment.text = dataArray[1];
                                slideArray[0] = comment;                       
                            }
                    }
                    
                    scrubberData.comment = scrubberArray;
                    slideData.comment = slideArray;

                    if(slideData.comment.length > 0){
                        Opencast.Utils.log("Slide Comments available");
                        var reachedSegID = "";
                        $(slideData.comment).each(function (i){
                            if(reachedSegID !== slideData.comment[i].segId){
                                var markdiv = "<div style='height:100%; width:5px; background-color: #A72123; float: right;'> </div>";
                                $("#segment"+slideData.comment[i].segId).html(markdiv);
                                //$("#segment"+slideData.comment[i]).corner("dogfold br");
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
    function hoverComment(commentId, commentValue, commentTime, userId)
    {
        if(clickedOnHoverBar === false){
            var left = $("#" + commentId).offset().left;
            var top = $("#data").offset().top - 105;
            Opencast.Utils.log("hoverComment ");
            $("#comment-Info").css("left", left+"px");
            $("#comment-Info").css("top", top+"px");
            clickedOnHoverBar = true;
            $("#comment-Info").show();
            $("#cm-add-box").hide();
            $("#cm-info-box").show();
            $("#cm-info-hover").hide();
            $("#oc-comment-info-header-text").html(userId+" at "+Opencast.Utils.formatSeconds(commentTime));
            $("#oc-comment-info-textbox").html(commentValue);
            Opencast.Utils.log("hoverComment ");
        }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description Toggles comment tooltip
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function hoverSlideComment(commentId, commentValue, userId, slideNr)
    {
        
        Opencast.Utils.log("hoverSlideComment ");
        if(clickedOnHoverBar === false){
            var left = $("#" + commentId).offset().left + 8;
            var top = $("#" + commentId).offset().top - 100;
            $("#comment-Info").css("left", left+"px");
            $("#comment-Info").css("top", top+"px");
            clickedOnHoverBar = true;
            $("#comment-Info").show();
            $("#cm-add-box").hide();
            $("#cm-info-box").show();
            $("#cm-info-hover").hide();
            Opencast.Utils.log("hoverSlideComment "+userId);
            var slNr = parseInt(slideNr) + 1;
            $("#oc-comment-info-header-text").html(userId + " at slide "+slNr);
            $("#oc-comment-info-textbox").html(commentValue);
            
        }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description Toggles comment tooltip
     * @param commentId the id of the comment
     */
    function hoverOutSlideComment()
    {
        Opencast.Utils.log("hoverOutSlideComment ");
        clickedOnHoverBar = false;
        $("#comment-Info").hide();
        $("#cm-info-hover").hide();
        $("#cm-info-box").hide();
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description Toggles comment tooltip
     * @param commentId the id of the comment
     */
    function hoverOutComment()
    {
        clickedOnHoverBar = false;
        $("#cm-info-box").hide();
        $("#cm-info-hover").show();
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
        $("#oc-comment-hover-box").html("No Comments available" + optError);
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
        $("#oc-comment-hover-box").hide();
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
        hoverSlideComment: hoverSlideComment,
        hoverOutSlideComment: hoverOutSlideComment,
        doToggleAnnotation_Comment: doToggleAnnotation_Comment
    };
}());
