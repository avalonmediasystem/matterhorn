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
 * @namespace the global Opencast namespace Annotation_CommentPlugin
 */
Opencast.Annotation_CommentPlugin = (function ()
{
    //place to render the data in the html ${(parseInt(a.inpoint) / parseInt(duration)) * 100} ;float:left 
    var template = '<div ' +
                      'id="annotation_comment_holder" ' +
                      'style="width:100%;" >' +
                      
                   	 '{for a in comment}' +
                       '<canvas id="comment${a.id}" style="z-index:10;width:15px;height:15px;top:90%;position:absolute;left:${(parseInt(a.inpoint) / parseInt(duration)) * 100}%;" '+
                       'onmouseover="Opencast.Annotation_Comment.hoverComment(\'comment${a.id}\', \'${a.text}\')" ' +
                       'onmouseout="Opencast.Annotation_Comment.hoverOutComment(\'comment${a.id}\', \'${a.text}\')" ' +
                       '>' +
                       
                       '</canvas>' +
                     '{/for}' +

                    '</div>';
                    
    var template1 = '<div ' +
                      'id="annotation_comment_holder" ' +
                      'style="width:100%;" >' +
                      
                     '{for a in annotation}' +
                       '<canvas id="comment${a.annotationId}" style="z-index:10;width:15px;height:15px;top:90%;position:absolute;left:${(parseInt(a.inpoint) / parseInt(duration)) * 100}%;" '+
                       'onmouseover="Opencast.Annotation_Comment.hoverComment(\'comment${a.annotationId}\', \'${a.text}\')" ' +
                       'onmouseout="Opencast.Annotation_Comment.hoverOutComment(\'comment${a.annotationId}\', \'${a.text}\')" ' +
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
     * @memberOf Opencast.Annotation_CommentPlugin
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
     * @memberOf Opencast.Annotation_CommentPlugin
     * @description Resize Plug-in
     * @return true if successfully processed, false else
     */
    function resizePlugin()
    {
        return drawAnnotation_Comment();
    }
    
    /**
     * @memberOf Opencast.Annotation_CommentPlugin
     * @description Add annotations into template element
     * processing the template with service data
     * @return true if successfully processed, false else
     */
    function drawAnnotation_Comment()
    {
        if ((element !== undefined) &&
            (annotation_CommentData.comment !== undefined) &&
            (annotation_CommentData.comment.length > 0) &&
            (annotation_CommentData.duration > 0))
        {
            Opencast.Utils.log("Annotation Plugin: Data available, processing template");
            processedTemplateData = template.process(annotation_CommentData);
            element.html(processedTemplateData);
            return true;
        }
        else
        {
            Opencast.Utils.log("Annotation Plugin: No data available");
            return false;
        }
    }
    
    return {
        addAsPlugin: addAsPlugin,
        resizePlugin: resizePlugin
    };
}());
