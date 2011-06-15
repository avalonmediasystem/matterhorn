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
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Initializes Annotation Comment
     *              Checks whether Data are available. If not: Hide Annotations
     */

    function initialize()
    {
        
        //enable log
        Opencast.Utils.enableLogging(true);

        // Handler keypress ALT+CTRL+a
        $(document).keyup(function (event)
        {
            if (event.altKey === true && event.ctrlKey === true)
            {
                if (event.which === 65)
                {
                    $("#oc_comment_dialog").dialog("open");         
                }

            }
        });
        
        //double click handler on slide comment box
        $("#oc_slide-comments").dblclick(function(event){
            Opencast.Utils.log("Div: "+$('#oc_slide-comments').width() + " " + $('#oc_slide-comments').height() + " mouse position: "+event.pageX + " "+event.pageY + " Div position: "+$('#oc_slide-comments').offset().left+" "+$('#oc_slide-comments').offset().top );
            
            var mPos = new Object();
            mPos.x = event.pageX - $('#oc_slide-comments').offset().left;
            mPos.y = event.pageY - $('#oc_slide-comments').offset().top;
            
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
            
            Opencast.Utils.log("relative position: "+relPos.x+" "+relPos.y);
            Opencast.Utils.log("Random: "+Opencast.Utils.getRandom(10,100));
            
            var idRand = Opencast.Utils.getRandom(10,1000);
            if($("#slideComment"+idRand) !== null){
                var canvasTmp = "<canvas id=slideComment" +idRand+ " ></canvas>";
                $("#oc_slide-comments").prepend(canvasTmp);
                $("#slideComment"+idRand).css("position","absolute");
                $("#slideComment"+idRand).css("height","18px");
                $("#slideComment"+idRand).css("width","18px");
                $("#slideComment"+idRand).css("left",relPos.x+"%");
                $("#slideComment"+idRand).css("top",relPos.y+"%");
                drawBalloon($("#slideComment"+idRand)[0]);
            }
                     
       });

        //comment form handlers
        var comment_field = $("#oc_comment_field");
        
        $("#oc_comment_dialog").dialog({
            autoOpen: false,
            height: 300,
            width: 350,
            modal: true,
            buttons: {
                "Create comment": function() {                 
                    addComment(parseInt(Opencast.Player.getCurrentPosition()), comment_field.val(),"scrubber");
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
        
        
        // Request JSONP data, annotation available ?
        $.ajax(
        {
            url: Opencast.Watch.getAnnotationURL(),
            data: 'episode=' + mediaPackageId + '&type=' + annotationType,
            dataType: 'json',
            jsonp: 'jsonp',
            success: function (data)
            {
                Opencast.Utils.log("Annotation AJAX call: Requesting data succeeded");
                if ((data !== undefined) && (data['annotations'] !== undefined) && (data['annotations'].annotation !== undefined))
                {
                    Opencast.Utils.log("Annotation AJAX call: Data available");
                    // Display the controls
                    $('#oc_checkbox-annotation-comment').show();
                    $('#oc_label-annotation-comment').show();
                    $('#oc_video-view').show();
                    Opencast.Analytics.initialize();
                }
                else
                {
                    Opencast.Utils.log("Annotation AJAX call: Data not available");
                    displayNoAnnotationsAvailable("No data available");
                }
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                Opencast.Utils.log("Annotation Ajax call: Requesting data failed");
                displayNoAnnotationsAvailable("No data available");
            }
        });
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(curPosition,value,type,xPos,yPos)
    {
        
        //comment data [text]:[type]:[xPos]:[yPos]:[segid]
        var data = "";
        if(xPos !== undefined && yPos !== undefined){
            data = value+":"+type+":"+xPos+":"+yPos;
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
        
        // Request JSONP data
        $.ajax(
        {
            url: Opencast.Watch.getAnnotationURL(),
            data: "episode=" + mediaPackageId+"&type="+annotationType,
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
                    
                    var commentData = new Object();
                    
                    var commentArray = new Array();
                    commentData.duration = duration;
                    

                    if(data['annotations'].total > 1){
                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by colons [text];[type];[xPos];[yPos]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            if(dataArray[1] === "scrubber"){
                                var comment = new Object();
                                comment.id = data['annotations'].annotation[i].annotationId;
                                comment.inpoint = data['annotations'].annotation[i].inpoint;
                                comment.text = dataArray[0];
                                commentArray[i] = comment;
                            }                        
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            //split data by colons [text];[type];[xPos];[yPos]
                            var dataArray = data['annotations'].annotation.value.split(":");
                            if(dataArray[1] === "scrubber"){
                                var comment = new Object();
                                comment.id = data['annotations'].annotation.annotationId;
                                comment.inpoint = data['annotations'].annotation.inpoint;
                                comment.text = dataArray[0];
                                commentArray[0] = comment;
                            }
                    }
                    
                    commentData.comment = commentArray;
                    $(commentData.comment).each(function(i)
                    {
                       Opencast.Utils.log("Comment: "+commentData.comment[i].text+" "+commentData.comment[i].id+" "+commentData.comment[i].inpoint+" "+commentData.duration); 
                    });
                    // Create Trimpath Template
                    var annotSet = Opencast.Annotation_CommentPlugin.addAsPlugin($('#annotation_comment'), commentData);
                    if (!annotSet)
                    {
                        Opencast.Utils.log("No template processed");
                        //displayNoAnnotationsAvailable("No template available");
                    }
                    else
                    {
                        
                        $(commentData.comment).each(function (i)
                        {
                            var id = commentData.comment[i].id;
                            var c_canvas = $("#comment"+id)[0];
                            
                            drawBalloon(c_canvas);
                        });
                        annotationCommentDisplayed = true;
                        $("#annotation_comment").show();
                        
                        $("#annotation_comment").css("margin-top","8px");

                    }
                }
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                alert("error: "+xhr)
                Opencast.Utils.log("Annotation Ajax call: Requesting data failed");
                //displayNoAnnotationsAvailable("No data available");
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
        //$('#segmentstable1').show();
        //$('#segmentstable2').show();
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
        hideAnnotation_Comment: hideAnnotation_Comment,
        showAnnotation_Comment: showAnnotation_Comment,
        setDuration: setDuration,
        setMediaPackageId: setMediaPackageId,
        hoverComment: hoverComment,
        hoverOutComment: hoverOutComment,
        doToggleAnnotation_Comment: doToggleAnnotation_Comment
    };
}());
