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
    var clickedOnComment = false;
    var infoTime = "";
    var commentAtInSeconds;
    var cookieName = "oc_comment_username";
    var default_name = "Your name!";
    var cm_username;
    var defaul_comment_text = "Type your comment here!"
    var comments_cache;
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Initializes Annotation Comment
     */

    function initialize()
    {
    	
    	$("Comment Plugin init");
    	
    	//Read Cookie for default Name
		cm_username = default_name;
		var nameEQ = cookieName + "=";
		var ca = document.cookie.split(';');
		for(var i = 0; i < ca.length; i++) {
			var c = ca[i];
			while(c.charAt(0) == ' ')
			c = c.substring(1, c.length);
			if(c.indexOf(nameEQ) == 0)
				cm_username = c.substring(nameEQ.length, c.length);
		}
		
		$.log("Comment Plugin set username to: "+cm_username);
    	
    	
       	// Handler keypress ALT+CTRL+a
        $(document).keyup(function (event)
        {
            if (event.altKey === true && event.ctrlKey === true)
            {
                if (event.which === 65)
                {
                    if(annotationCommentDisplayed === true){
				    	$("#oc_btn-add-comment").click();            	
                    }

                }

            }
        });
        
        $("#oc_btn-add-comment").click(function(){
        	//pause player
        	Opencast.Player.doPause();
        	
        	//exit shown infos
        	$(".oc-comment-add-exit").click();
        	
        	clickedOnHoverBar = true;
    	    //hide other slide comments
        	$('div[id^="scComment"]').hide();
			var left = $("#scrubber").offset().left + ($("#scrubber").width() / 2) ;
			var top = $("#data").offset().top - 105;
			$("#comment-Info").css("left", left+"px");
            $("#comment-Info").css("top", top+"px");
			$("#comment-Info").show();           
			
			$("#cm-add-box").show();
			
		    $('#cm-add-box').attr(
            {
                title: "Add timed comment"
            });
			
            //Info Text
            var curTime = Opencast.Player.getCurrentTime();;
            var infoText = "on " + curTime;    
            $("#oc-comment-add-header-text").html(infoText);
            
            $("#oc-comment-add-namebox").focus();
            $("#oc-comment-add-namebox").select(); 
        });
        
        
        // resize handler
        $('#oc_flash-player').bind('doResize', function(e) {
           
           //hideAnnotation_Comment();
           
			//positioning of the slide comment box
			var flashWidth = $('#oc_flash-player').width() / 2;
			var flashHeight = $('#oc_flash-player').height()-10;
			var flashTop = $('#oc_flash-player').offset().top;
			var flashLeft = $('#oc_flash-player').offset().left;
			
			
			var scHeight = 0;
			var scWidth = 0;
			var scLeft = 0;
			var scTop = 0;
			
			if(((flashWidth - 5) / flashHeight) < (4/3) ){
				scHeight = (flashWidth - 5) / (4/3);
				scWidth = (4/3)*scHeight;
				scLeft = flashWidth;
				scTop = flashHeight - scHeight + 4;
			}else{
				scWidth = (4/3) * flashHeight;
				scHeight = scWidth / (4/3);
				scLeft = flashWidth;
				scTop = 5;
			}
			
			$("#oc_slide-comments").css("position","absolute");
			$("#oc_slide-comments").css("height",scHeight+"px");
			$("#oc_slide-comments").css("width",scWidth+"px");
			$("#oc_slide-comments").css("left",scLeft+"px");
			$("#oc_slide-comments").css("top",scTop+"px");
			/*
			if(annotationCommentDisplayed){
				$.log("BLUB");
			    //Workaround: 500ms after resize repaint comments and marks
			    window.setTimeout(function() {
			    	$.log("after resize and 500ms show annotations");  
					Opencast.Annotation_Comment.showAnnotation_Comment();
				}, 500);            	
			}*/
			//Opencast.Annotation_Comment.showAnnotation_Comment();
 
            
            
        });
        
        //// UI ////
        
    	// show namebox and next button -> hide comment add box and add button
		$("#oc-comment-add-namebox").show();
		$("#oc-comment-add-submit-name").show();
		$("#oc-comment-add-textbox").hide();
		$("#oc-comment-add-submit").hide();
        
        $(".oc-comment-add-exit").click(function(){
			// show namebox and next button -> hide comment add box and add button
			$("#oc-comment-add-namebox").show();
			$("#oc-comment-add-submit-name").show();
			$("#oc-comment-add-textbox").hide();
			$("#oc-comment-add-submit").hide();
            // hide info box -> hide add box show hover tooltip cm-info-box
            $("#comment-Info").hide();
            $("#cm-add-box").hide();
            $("#cm-info-box").hide();
            $("#cm-info-hover").show();
            // back to default
            $("#oc-comment-add-textbox").val(defaul_comment_text);
            $("#oc-comment-add-namebox").val(cm_username);
            clickedOnHoverBar = false;
            clickedOnComment = false;
			//show other slide comments
			$('canvas[id^="slideComment"]').show();
			$('div[id^="scComment"]').show();
			$('#cm-add-box').attr(
            {
                title: ""
            });
        });
        
        $("#oc-comment-add-submit").click(function(){
        	if($("#oc-comment-add-textbox").val() !== defaul_comment_text){
		    	// show namebox and next button -> hide comment add box and add button
		        $("#oc-comment-add-namebox").show();
		        $("#oc-comment-add-submit-name").show();
		        $("#oc-comment-add-textbox").hide();
		        $("#oc-comment-add-submit").hide();
		        // hide info box -> hide add box show hover tooltip
		        $("#comment-Info").hide();
		        $("#cm-add-box").hide();
		        $("#cm-info-hover").show();
		        clickedOnHoverBar = false;
		        var commentValue = $("#oc-comment-add-textbox").val();
		        commentValue = commentValue.replace(/<>/g,"");
		        commentValue = commentValue.replace(/'/g,"`");
		        commentValue = commentValue.replace(/"/g,"`");
		        commentValue = commentValue.replace(/\n/,"");	        
		        var nameValue = $("#oc-comment-add-namebox").val();
		        nameValue = nameValue.replace(/<>/g,"");       
		        nameValue = nameValue.replace(/'/g,"`"); 
		        nameValue = nameValue.replace(/"/g,"`");  
		        // back to default
		        $("#oc-comment-add-textbox").val(defaul_comment_text);
		        $("#oc-comment-add-namebox").val(cm_username);
				//show other slide comments
				$('canvas[id^="slideComment"]').show();
				$('div[id^="scComment"]').show(); 
		        if($('#cm-add-box').attr("title") === "Add timed comment"){
		            //add scrubber comment
		            addComment(nameValue,parseInt(Opencast.Player.getCurrentPosition()),commentValue,"scrubber");                
		        }else if($('#cm-add-box').attr("title") === "Add slide comment"){
		            //add slide comment
		            addComment(nameValue,
								parseInt(Opencast.Player.getCurrentPosition()),
								commentValue,
								"slide",
								relativeSlideCommentPosition.x,
								relativeSlideCommentPosition.y,
								Opencast.segments.getCurrentSlideId()
		                      );                              
		        }
				$('#cm-add-box').attr(
		        {
		            title: ""
		        });
		    }
        });
        
        $("#oc-comment-add-submit-name").click(function(){
        	var nameVal = $("#oc-comment-add-namebox").val();
        	if(nameVal !== default_name){
		        // hide namebox and next button -> show comment add box and add button
		        $("#oc-comment-add-namebox").hide();
		        $("#oc-comment-add-submit-name").hide();
		        $("#oc-comment-add-textbox").show();
		        $("#oc-comment-add-submit").show();
                var infoText = "";    

           		if($('#cm-add-box').attr("title") === "Add timed comment"){
               		infoText = nameVal + " on " + Opencast.Player.getCurrentTime();
		        }else if($('#cm-add-box').attr("title") === "Add slide comment"){
                	var curSlide = Opencast.segments.getCurrentSlideId()+1;
                	infoText = nameVal + " at slide " + curSlide;       
		        }
		        
		        $("#oc-comment-add-header-text").html(infoText);
                $("#oc-comment-add-textbox").focus();
                $("#oc-comment-add-textbox").select();
		    }

        });
        
        // Handler keypress Enter on textbox
        $("#oc-comment-add-textbox").keyup(function (event)
        {
            if (event.which === 13)
            {
                $("#oc-comment-add-submit").click();
            }
        });
        // Handler keypress Enter on namebox
        $("#oc-comment-add-namebox").keyup(function (event)
        {
            if (event.which === 13)
            {
                $("#oc-comment-add-submit-name").click();
            }
        });
        
        //// UI END ////
        
        // change scrubber position handler
        $('#scrubber').bind('changePosition', function(e) {
        	//Check wether comments on the current slide 
            if(Opencast.segments.getCurrentSlideId() !== oldSlideId){
                if(annotationCommentDisplayed){
                    showAnnotation_Comment();
                    //exit shown infos
        			$(".oc-comment-add-exit").click();
                }                   
                oldSlideId = Opencast.segments.getCurrentSlideId();
            }
            //Check wether comments on the current time
            $('div[id^="scComment"]').each(function(i){
            	if((parseInt(Opencast.Player.getCurrentPosition())+1) === parseInt($(this).attr("inpoint"))){
            		//show comment info for 3 seconds
            		$(this).mouseover();
            		window.setTimeout(function() {  
    					Opencast.Annotation_Comment.hoverOutComment();
					}, 5000); 
            		
            	}
            });
            
        });
        $('#draggable').bind('dragstop', function (event, ui){
        	//Check wether comments on the current slide 
             if(Opencast.segments.getCurrentSlideId() !== oldSlideId){
                if(annotationCommentDisplayed){
                    showAnnotation_Comment();
                    //exit shown infos
        			$(".oc-comment-add-exit").click();                    
                }                   
                oldSlideId = Opencast.segments.getCurrentSlideId();
            }                
        });
        
        //double click handler on slide comment box
        $("#oc_slide-comments").dblclick(function(event){
        	
        	//exit shown infos
        	$(".oc-comment-add-exit").click();
        	
        	//hide doubleclick info
        	$("#oc_dbclick-info").hide();
        	
        	//pause player
    		Opencast.Player.doPause();
        	
        	//hide other slide comments
        	$('canvas[id^="slideComment"]').hide();
           
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
            $.log("dblclick "+ciLeft+" "+ciTop);
            $("#comment-Info").css("left", ciLeft+"px");
            $("#comment-Info").css("top", ciTop+"px");
            $("#comment-Info").show();           
            
            $("#cm-add-box").show();          
            //Info Text
            if(Opencast.Player.getUserId() !== null){
                user = Opencast.Player.getUserId();
            }
            var curSlide = Opencast.segments.getCurrentSlideId();
            var infoText = "Slide " + curSlide;    
            $("#oc-comment-add-header-text").html(infoText);
            
            $("#oc-comment-add-namebox").focus();
            $("#oc-comment-add-namebox").select();               
       });
       
       $("#oc_slide-comments").mouseenter(function(){
       	
       		var sl_left = $('#oc_slide-comments').offset().left + $('#oc_slide-comments').width() + 2;
       		var sl_top = $('#oc_slide-comments').offset().top + $('#oc_slide-comments').height() - 40;
       		
            $("#oc_dbclick-info").css("left", sl_left+"px");
            $("#oc_dbclick-info").css("top", sl_top+"px");
            
            $("#oc_dbclick-info").show();     	
       });
       
       $("#oc_slide-comments").mouseleave(function(){
       		$("#oc_dbclick-info").hide();
       });
        
        // Display the controls
        $('#oc_checkbox-annotation-comment').show();    // checkbox
        $('#oc_label-annotation-comment').show();       // 
        $('#oc_video-view').show();                     // slide comments
        //$("#oc_ui_tabs").tabs('enable', 3);             // comment tab

    }    
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(user,curPosition,value,type,xPos,yPos,segId)
    {
        //var user = "Anonymous";
        //if(Opencast.Player.getUserId() !== null){
        //    user = Opencast.Player.getUserId();
        //}
        
        //Create cookie with username
        document.cookie = cookieName+"="+user+"; path=/engage/ui/";
        
        //Replace default username
        cm_username = user;
        
        //comment data [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
        var data = "";
        if(xPos !== undefined && yPos !== undefined){
            data = user+"<>"+value+"<>"+type+"<>"+xPos+"<>"+yPos+"<>"+segId;
            //var markdiv = "<div style='height:100%; width:5px; background-color: #A72123; float: right;'> </div>";
            //$("#segment"+segId).html(markdiv);
        }else{
            data = user+"<>"+value+"<>"+type;        
        }
        
        $.ajax(
        {
            type: 'PUT',
            url: "../../annotation/",
            data: "episode="+mediaPackageId+"&type="+annotationType+"&in="+curPosition+"&value="+data+"&out="+curPosition,
            dataType: 'xml',
            success: function (xml)
            {
                $.log("add comment success");
                //erase cache
                comments_cache = undefined;
                //show new comments
                showAnnotation_Comment();
                //check checkbox
                $('#oc_checkbox-annotation-comment').attr('checked', true);
                
                var comment_list_show = $('#oc_btn-comments-tab').attr("title");
                if(comment_list_show == "Hide Comments"){
                    Opencast.Annotation_Comment_List.showComments();
                }                    
            },
            error: function (jqXHR, textStatus, errorThrown)
            {
                $.log("Add_Comment error: "+textStatus);
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
                $.log("Annotation AJAX call: Requesting data succeeded");
                
                //demark segements              
                for(var slidesNr = Opencast.segments.getNumberOfSegments()-1 ; slidesNr >= 0 ; slidesNr--){
                    $("#segment"+slidesNr).html("");
                }
                
                if ((data === undefined) || (data['annotations'] === undefined) || (data['annotations'].annotation === undefined))
                {
                    $.log("Annotation AJAX call: Data not available");
                    //show nothing
                    $('#oc-comment-scrubber-box').html("");
                    $('#oc_slide-comments').html("");
                }
                else
                {
                    $.log("Annotation AJAX call: Data available");
                    data['annotations'].duration = duration; // duration is in seconds
                    data['annotations'].nrOfSegments = Opencast.segments.getNumberOfSegments();
                    
                    var scrubberData = new Object();
                    var slideData = new Object();
                    
                    var scrubberArray = new Array();
                    var slideArray = new Array();
                    var toMarkSlidesArray = new Array();

                    scrubberData.duration = duration;
                    scrubberData.type = "scrubber";
                    slideData.type = "slide";
                    
                    if(data['annotations'].total > 1){
                        var scCount = 0;
                        var slCount = 0;
                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by <> [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
                            var dataArray = data['annotations'].annotation[i].value.split("<>");
                            var comment = new Object();
                            comment.user = dataArray[0];
                            comment.id = data['annotations'].annotation[i].annotationId;
                            comment.text = dataArray[1];
                            //found scrubber comment
                            if(dataArray[2] === "scrubber"){                                                              
                                comment.inpoint = data['annotations'].annotation[i].inpoint;                        
                                scrubberArray[scCount] = comment;
                                scCount++;
                            //found slide comment on current slide
                            }else if(dataArray[2] === "slide" && dataArray[5] == Opencast.segments.getCurrentSlideId()){
                                comment.slideNr = dataArray[5];
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[3];
                                comment.relPos.y = dataArray[4];                  
                                slideArray[slCount] = comment;
                                slCount++;
                                var slideFound = false;
                                for (i in toMarkSlidesArray) {
       								if (toMarkSlidesArray[i] === dataArray[5]) {
       									slideFound = true;
       								}
   								}
   								if(slideFound === false){
   									toMarkSlidesArray[toMarkSlidesArray.length] = dataArray[5];
   								}
   							//found slide comment                               
                            }else if(dataArray[2] === "slide"){
                                var slideFound = false;
                                for (i in toMarkSlidesArray) {
       								if (toMarkSlidesArray[i] === dataArray[5]) {
       									slideFound = true;
       								}
   								}
   								if(slideFound === false){
   									toMarkSlidesArray[toMarkSlidesArray.length] = dataArray[5];
   								}                             	
                            }                      
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            //split data by <> [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
                            var dataArray = data['annotations'].annotation.value.split("<>");
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
                                toMarkSlidesArray[0] = dataArray[5];                     
                            }else if(dataArray[2] === "slide"){
                            	toMarkSlidesArray[0] = dataArray[5];
                            }
                    }
                    
                    scrubberData.comment = scrubberArray;
                    slideData.comment = slideArray;

                    // Create Trimpath Template
                    var scrubberCommentSet = Opencast.Scrubber_CommentPlugin.addAsPlugin($('#oc-comment-scrubber-box'), scrubberData);
                    var slideCommentSet = Opencast.Slide_CommentPlugin.addAsPlugin($('#oc_slide-comments'), slideData);
                    if (!scrubberCommentSet)
                    {
                        $.log("No scrubberComment template processed");
                        //$("#oc-comment-scrubber-box").html("");
                    }
                    else
                    {                                                
                        //$("#oc-comment-scrubber-box").show();
                    }
                    
                    if (!slideCommentSet)
                    {
                        $.log("No slideComment template processed");
                        $("#oc_slide-comments").html("");
                    }
                    else
                    {                        
                        //$("#oc_slide-comments").show();
                    }
     
                    //mark segments
                    if(toMarkSlidesArray.length > 0){
                        $.log("Slide Comments available");
                        $(toMarkSlidesArray).each(function (i){
                        	$.log("Mark Slide: "+toMarkSlidesArray[i]);
                            var markdiv = "<div id='oc-comment-segmark_"+ toMarkSlidesArray[i] +"' style='width:6px; float: left;'> </div>";
                            $("#segment"+toMarkSlidesArray[i]).html(markdiv);
                            $("#oc-comment-segmark_"+ toMarkSlidesArray[i]).corner("cc:#000000 bevel bl 6px");
                        });
                    }
                                        
                    
                }
                $("#oc_slide-comments").show();
                $("#oc-comment-scrubber-box").show();
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                $.log("Comment Ajax call: Requesting data failed "+xhr+" "+ ajaxOptions+" "+ thrownError);
            }
        });
    }

    /**
     * @memberOf Opencast.annotation_comment
     * @description click event comment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function clickComment(commentId, commentValue, commentTime, userId)
    {
    	clickedOnComment = true;
    	//process position and set comment info box
        var left = $("#" + commentId).offset().left;
        var top = $("#data").offset().top - 105;
        $("#comment-Info").css("left", left+"px");
        $("#comment-Info").css("top", top+"px");
        $("#comment-Info").show();
        $("#cm-add-box").hide();
        $("#cm-info-box").show();
        $("#cm-info-hover").hide();
        $("#oc-comment-info-header-text").html(userId+" at "+$.formatSeconds(commentTime));
        $("#oc-comment-info-textbox").html(commentValue);
        //seek player to comment
        Opencast.Watch.seekSegment(parseInt(commentTime)-2);
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description clickSlideComment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function clickSlideComment(commentId, commentValue, userId, slideNr)
    {
        //hide double click info
        $("#oc_dbclick-info").hide();
        
        clickedOnComment = true;
        var left = $("#" + commentId).offset().left + 8;
        var top = $("#" + commentId).offset().top - 100;
        $("#comment-Info").css("left", left+"px");
        $("#comment-Info").css("top", top+"px");
        $("#comment-Info").show();
        $("#cm-add-box").hide();
        $("#cm-info-box").show();
        $("#cm-info-hover").hide();
        var slNr = parseInt(slideNr) + 1;
        $("#oc-comment-info-header-text").html(userId + " at slide "+slNr);
        $("#oc-comment-info-textbox").html(commentValue);
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverComment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function hoverComment(commentId, commentValue, commentTime, userId)
    {
        if(clickedOnHoverBar === false){
            var left = $("#" + commentId).offset().left;
            var top = $("#data").offset().top - 105;
            $("#comment-Info").css("left", left+"px");
            $("#comment-Info").css("top", top+"px");
            clickedOnHoverBar = true;
            $("#comment-Info").show();
            $("#cm-add-box").hide();
            $("#cm-info-box").show();
            $("#cm-info-hover").hide();
            $("#oc-comment-info-header-text").html(userId+" at "+$.formatSeconds(commentTime));
            $("#oc-comment-info-textbox").html(commentValue);
        }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverSlideComment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function hoverSlideComment(commentId, commentValue, userId, slideNr)
    {
        //hide double click info
        $("#oc_dbclick-info").hide();
        
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
            var slNr = parseInt(slideNr) + 1;
            $("#oc-comment-info-header-text").html(userId + " at slide "+slNr);
            $("#oc-comment-info-textbox").html(commentValue);
            
        }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverOutSlideComment
     * @param commentId the id of the comment
     */
    function hoverOutSlideComment()
    {
    	if(clickedOnComment === false){
	        //show dblick info
	        $("#oc_dbclick-info").show();
	        
	        clickedOnHoverBar = false;
	        $("#comment-Info").hide();
	        $("#cm-info-hover").hide();
	        $("#cm-info-box").hide();
	        $("#oc-comment-add-textbox").val(defaul_comment_text);
	        $("#oc-comment-add-namebox").val(cm_username);
       }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverOutComment
     * @param commentId the id of the comment
     */
    function hoverOutComment()
    {
    	if(clickedOnComment === false){
			clickedOnHoverBar = false;
			$("#comment-Info").hide();
			$("#cm-info-hover").hide();
			$("#cm-info-box").hide();
			// back to default
			$("#oc-comment-add-textbox").val(defaul_comment_text);
			$("#oc-comment-add-namebox").val(cm_username);
       }
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Hide the Annotation
     */
    function hideAnnotation_Comment()
    {
    	//remove segment marks
    	$('div[id^="oc-comment-segmark_"]').remove();
        $("#oc-comment-scrubber-box").hide();
        $('canvas[id^="slideComment"]').hide();
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
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Gets status of annotations are shown
     */
    function getAnnotationCommentDisplayed()
    {
        return annotationCommentDisplayed;
    }
    
    
    
    return {
        initialize: initialize,
        hideAnnotation_Comment: hideAnnotation_Comment,
        showAnnotation_Comment: showAnnotation_Comment,
        getAnnotationCommentDisplayed: getAnnotationCommentDisplayed,
        setDuration: setDuration,
        setMediaPackageId: setMediaPackageId,
        clickComment: clickComment,
        clickSlideComment: clickSlideComment,
        hoverComment: hoverComment,
        hoverOutComment: hoverOutComment,
        hoverSlideComment: hoverSlideComment,
        hoverOutSlideComment: hoverOutSlideComment,
        doToggleAnnotation_Comment: doToggleAnnotation_Comment
    };
}());
