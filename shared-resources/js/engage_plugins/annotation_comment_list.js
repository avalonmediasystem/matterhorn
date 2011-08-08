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
    var comment_list_show = false;
    var mediaPackageId;
    var annotationType = "comment";
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Initializes the segments view
     */
    function initialize()
    {

        Opencast.Utils.log("Comment List init");
        
        // //Add Comment Form // //
        $("#oc-comments-list-textbox").val(defaultText);
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

    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description submitComment
     */
    function submitComment()
    { 
       var textBoxValue = $("#oc-comments-list-textbox").val();
       Opencast.Utils.log("click submit "+textBoxValue + " "+ defaultText);
       if(textBoxValue !== defaultText){             
           addComment(textBoxValue,"normal");
           $("#oc-comments-list-textbox").val(defaultText);
       }else{
            $("#oc-comments-list-textbox").focus();
            $("#oc-comments-list-textbox").select(); 
       }        
    }    
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(value,type)
    {      
        //comment data [user]:[text]:[type]
        data = Opencast.Player.getUserId()+":"+value+":"+type;        
        
        $.ajax(
        {
            type: 'PUT',
            url: "../../annotation/",
            data: "episode="+mediaPackageId+"&type="+annotationType+"&in="+0+"&value="+data+"&out="+0,
            dataType: 'xml',
            success: function (xml)
            {
                Opencast.Utils.log("Add_Comment success");
                showComments();                           
            },
            error: function (jqXHR, textStatus, errorThrown)
            {
                Opencast.Utils.log("Add_Comment error: "+textStatus);
            }
        });
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Show the Annotation_Comment_List
     */
    function showComments()
    {
        Opencast.Utils.log("show commment list");
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
        $('#oc-comments-list-footer').hide();
        
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
                    
                    var commentData = new Object();                  
                    var commentArray = new Array();
                    var count = 0;
                    var slideCount = 0;
                    var scrubberCount = 0;
                    var normalCount = 0;
                    
                    Opencast.Utils.log("Debug 1");
                    if(data['annotations'].total > 1){

                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by colons [user]:[text]:[type]:[xPos]:[yPos]:[segId]
                            var dataArray = data['annotations'].annotation[i].value.split(":");
                            var comment = new Object();
                            
                            comment.id = data['annotations'].annotation[i].annotationId;
                            comment.inpoint = data['annotations'].annotation[i].inpoint;
                            var created = data['annotations'].annotation[i].created;         
                            var dateCr = Opencast.Utils.dateStringToDate(created);
                            comment.created = Opencast.Utils.getDateString(dateCr) + " " + Opencast.Utils.getTimeString(dateCr);
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
                            var dateCr = Opencast.Utils.dateStringToDate(created);
                            comment.created = Opencast.Utils.getDateString(dateCr) + " " + Opencast.Utils.getTimeString(dateCr);
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
                    
                    Opencast.Utils.log("commentList template process");
                    // Create Trimpath Template
                    var commentListSet = Opencast.Annotation_Comment_List_Plugin.addAsPlugin($('#oc-comments-list'), commentData);

                    if (!commentListSet)
                    {
                        Opencast.Utils.log("No commentList template processed");

                    }else{
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
                    }         
                }
                $('#oc_comments-list-loading').hide();
                $('#oc-comments-list-header').show();
                $('#oc-comments-list').show();
                $('#oc-comments-list-footer').show();
                //scroll down
                $(window).scrollTop( $('#oc_btn-comments-tab').offset().top - 10 );

            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                Opencast.Utils.log("Comment Ajax call: Requesting data failed "+xhr+" "+ ajaxOptions+" "+ thrownError);
            }
        });
        
        
        
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Hide the Annotation_Comment_List
     */
    function hideComments()
    {
        Opencast.Utils.log("hide commment list");
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
        
        Opencast.Utils.log("toggle commment list");
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
     * @description go to a comment
     * @param String commentID inpoint type
     */
    function goToComment(commentID, commentValue, commentTime, commentSlide, userId, type)
    {
        //seek player if timed
        if(type === "scrubber"){
            Opencast.Annotation_Comment.showAnnotation_Comment();
            Opencast.Watch.seekSegment(parseInt(commentTime));
            Opencast.Annotation_Comment.hoverComment("scComment"+commentID, commentValue, commentTime, userId);
            //scroll to comment
            $(window).scrollTop( 0 );
        }else if(type === "slide"){
            Opencast.Annotation_Comment.showAnnotation_Comment();
            Opencast.Watch.seekSegment(parseInt(commentTime));
            Opencast.Annotation_Comment.hoverSlideComment("slideComment"+commentID, commentValue, userId);
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
        goToComment: goToComment,
        doToggleComments: doToggleComments,
        setMediaPackageId: setMediaPackageId
    };
}());
