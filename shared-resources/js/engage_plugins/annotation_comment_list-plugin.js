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
 * @namespace the global Opencast namespace segments_text_Plugin
 */
Opencast.Annotation_Comment_List_Plugin = (function ()
{
    // The Template to process
    var template =  '<table cellspacing="5" onmouseout="Opencast.Annotation_Comment_List.hoverOutCommentList()" cellpadding="0" width="100%">' +
                        '{for c in comment}' +
                                '<tr class="oc-comment-list-row" id="comment-row-${c.id}" >' +
                                    '<td class="oc-comment-list-border" width="25px" style="cursor:pointer;cursor:hand;">' +
                                    '</td>' +
                                    '<td class="oc-comment-list-border oc-comment-list-left-row" align="left" style="cursor:pointer;cursor:hand;">' +
                                        '<div class="oc-comment-list-user-icon"><img src="img/icons/user.png" width="50" height="50"></div>' +
                                    '</td>' +
                                    '<td class="oc-comment-list-border" width="10px" style="cursor:pointer;cursor:hand;">' +
                                    '</td>' +
                                    '<td class="oc-comment-list-border oc-comment-list-right-row" align="left" style="cursor:pointer;cursor:hand;">' +
                                        '<div class="oc-comment-list-user-text">${c.user}</div>' +
                                        '<div class="oc-comment-list-textspace"></div>' +                                   
                                        '{if c.type == "scrubber"}' +
                                            '<div class="oc-comment-list-type-text">at ${$.formatSeconds(c.inpoint)}</div>' +
                                        '{elseif c.type == "slide"}'+
                                            '<div class="oc-comment-list-type-text">at slide ${parseInt(c.slide) + 1}</div>' +
                                        '{else}'+
                                            '<div class="oc-comment-list-type-text"></div>' +
                                        '{/if}' +
                                        '<div class="oc-comment-list-textspace"></div>' + 
                                        '<div style="float:left">${c.created}</div>' +
                                        '<a style="float:right; color:blue" href="javascript:Opencast.Annotation_Comment_List.deleteComment(\'${c.id}\')" >remove</a>' +
                                        '<div class="oc-comment-list-textspace"></div>' +
                                        '{if c.type == "scrubber" || c.type == "slide"}' +
                                            '<a style="float:right; color:blue" href="javascript:Opencast.Annotation_Comment_List.clickCommentList(\'${c.id}\',\'${c.text}\',\'${c.inpoint}\',\'${c.slide}\',\'${c.user}\',\'${c.type}\')" >show in player</a>' +
                                        '{/if}' +
                                        '<p class="oc-comment-list-value-text">${c.text}</p>' +
          
                                    '</td>' +
                                '</tr>' +
                        '{forelse}' +
                            'No Comments available' +
                        '{/for}' +
                    '</table>';
    
    // The Element to put the div into
    var element;
    // Data to process
    var annotation_CommentData
    // Precessed Data
    var processedTemplateData = false;
    
    /**
     * @memberOf Opencast.Annotation_Comment_List_Plugin
     * @description Add As Plug-in
     * @param elem Element to fill with the Data (e.g. a div)
     * @param data Data to fill the Element with
     */
    function addAsPlugin(elem, data)
    {
        element = elem;
        annotation_CommentData = data;
        return drawAnnotation_Comment();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List_Plugin
     * @description Resize Plug-in
     * @return true if successfully processed, false else
     */
    function resizePlugin()
    {
        return drawAnnotation_Comment();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List_Plugin
     * @description Add annotations into template element
     * processing the template with service data
     * @return true if successfully processed, false else
     */
    function drawAnnotation_Comment()
    {
        if ((element !== undefined) &&
            (annotation_CommentData.comment !== undefined) &&
            (annotation_CommentData.comment.length > 0))
        {
            $.log("Annotation_Comment_List_Plugin: Data available, processing template");
            processedTemplateData = template.process(annotation_CommentData);
            element.html(processedTemplateData);
          
            return true;
        }
        else
        {
            $.log("Annotation_Comment_List_Plugin: No data available");
            return false;
        }
    }
    
    return {
        addAsPlugin: addAsPlugin,
        resizePlugin: resizePlugin
    };
}());
