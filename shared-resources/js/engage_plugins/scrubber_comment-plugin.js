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
 * @namespace the global Opencast namespace Scrubber_CommentPlugin
 */
Opencast.Scrubber_CommentPlugin = (function ()
{
    //place to render the data in the html ${(parseInt(a.inpoint) / parseInt(duration)) * 100} ;float:left 
    var template_scrubber = '<div ' +
                      'id="annotation_comment_holder" ' +
                      'style="width:100%;" >' +
                      
                     '{for a in comment}' +
                       '<canvas id="scComment${a.id}" class="oc-comment-scrubber-baloon" style="left:${(parseInt(a.inpoint) / parseInt(duration)) * 100}%;" '+
                       'onmouseover="Opencast.Annotation_Comment.hoverComment(\'scComment${a.id}\', \'${a.text}\',\'${a.inpoint}\',\'${a.user}\')" ' +
                       'onmouseout="Opencast.Annotation_Comment.hoverOutComment()" ' +
                       '>' +
                       '</canvas>' +
                       
                     '{/for}' +

                    '</div>';
        

                    
    // The Element to put the div into
    var element;
    // Data to process
    var annotation_CommentData;
    // Processed Data
    var processedTemplateData;
    
    /**
     * @memberOf Opencast.Scrubber_CommentPlugin
     * @description Add As Plug-in
     * @param elem Element to put the Data into
     * @param data The Data to process
     * @return true if successfully processed, false else
     */
    function addAsPlugin(elem, data)
    {
        element = elem;
        annotation_CommentData = data;
        return drawAnnotation_Comment();
    }
    
    /**
     * @memberOf Opencast.Scrubber_CommentPlugin
     * @description Resize Plug-in
     * @return true if successfully processed, false else
     */
    function resizePlugin()
    {
        return drawAnnotation_Comment();
    }
    
    /**
     * @memberOf Opencast.Scrubber_CommentPlugin
     * @description Add annotations into template element
     * processing the template with service data
     * @return true if successfully processed, false else
     */
    function drawAnnotation_Comment()
    {
        if ((element !== undefined) &&
            (annotation_CommentData.comment !== undefined) &&
            (annotation_CommentData.comment.length > 0) &&
            (annotation_CommentData.duration > 0) &&
            (annotation_CommentData.type === "scrubber"))
        {
            Opencast.Utils.log("Scrubber Comment Plugin: Data available, processing template");
            processedTemplateData = template_scrubber.process(annotation_CommentData);
            //Opencast.Utils.log("processedTemplateData: "+processedTemplateData);
            element.html(processedTemplateData);
            //draw balloons with html5
            $(annotation_CommentData.comment).each(function (i)
            {
                var id = annotation_CommentData.comment[i].id;
                var c_canvas = $("#scComment"+id)[0];
                
                drawBalloon(c_canvas);
            });
            
            return true;
        }
        else
        {
            Opencast.Utils.log("Annotation Plugin: No data available");
            return false;
        }
    }
    
     /**
     * @memberOf Opencast.Scrubber_CommentPlugin
     * @description draw the comment icon with the canvas element
     * @param canvas DOM canvas element
     */   
    function drawBalloon(canvas){
        var ctx = canvas.getContext('2d');
        
        ctx.save();
        //ctx.fillStyle = "rgba(167,33,35,0.9)";
        ctx.fillStyle = "rgba(255, 140, 80, 1.0)";
        //ctx.fillStyle = "rgba(83, 168, 253, 0.6)";
        //ctx.strokeStyle = "rgba(0, 0, 0, 1.0)";
        ctx.lineWidth   = 8;

        
        ctx.shadowOffsetX = 5;
        ctx.shadowOffsetY = 2;
        ctx.shadowBlur = 20;
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
        ctx.lineTo(70,0);
        ctx.fill();
        ctx.stroke();
        ctx.restore();
    }
    
    return {
        addAsPlugin: addAsPlugin,
        resizePlugin: resizePlugin
    };
}());
