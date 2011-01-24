/*global $, Opencast*/
/*jslint browser: true, white: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, newcap: true, immed: true, onevar: false */
var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace segments_Plugin
 */
Opencast.segments_ui_Plugin = (function ()
{
    // The Templates to process
    var templateSegments1 = '<tr>' +
                            '{for s in segment}' +
                                '{if s.durationIncludingSegment >= currentTime}' +
                                    '<td role="button" class="segment-holder ui-widget ui-widget-content" ' +
                                         'id="segment${s.index}" ' +
                                         'onmouseover="Opencast.segments_ui.hoverSegment(${parseInt(s.index)})" ' +
                                         'onmouseout="Opencast.segments_ui.hoverOutSegment(${parseInt(s.index)})" ' +
                                         'alt="Slide ${parseInt(s.index) + 1} of ${segment.length}" ' +
                                         'onclick="Opencast.Watch.seekSegment(${Math.floor(parseInt(s.time) / 1000)})" ' +
                                          'style="width: ${parseInt(s.duration) / parseInt(s.completeDuration) * 100}%;" ' +
                                    '>' +
                                        '<span class="segments-time" style="display: none">${Math.floor(parseInt(s.time) / 1000)}</span>' + 
                                    '</td>' +
                                 '{/if}' +
                             '{forelse}' +
                                 '<td style="width: 100%;" id="segment-holder-empty" class="segment-holder" />' +
                             '{/for}' +
                             '</tr>';
    
    var templateMedia1 =     '{for t in track}' +
                                 '{if t.type == "presenter/delivery"}' +
                                     '{if t.mimetype == "video/x-flv"}' +
                                         '{if t.url.substring(0, 4) == "http"}' +
                                             '<div id="oc-video-presenter-delivery-x-flv-http" style="display: none">' +
                                                 '${t.url}' +
                                             '</div>' +
                                             '<div id="oc-resolution-presenter-delivery-x-flv-http" style="display: none">' +
                                                 '${t.video.resolution}' +
                                             '</div>' +
                                             '<div id="oc-mimetype-presenter-delivery-x-flv-http" style="display: none">' +
                                                 '${t.mimetype}' +
                                             '</div>' +
                                         '{/if}' +
                                     '{/if}' +
                                 '{/if}' +
     
                                  '{if (t.type == "presenter/delivery")' +
                                   '&& (t.precedingSiblingType == "presentation/delivery")' +
                                   '&& (t.precedingSiblingMimetypeIsVideo == "true")' +
                                   '&& (t.followingSiblingType == "presentation/delivery")' +
                                   '&& (t.followingSiblingMimetypeIsVideo == "true")}' +
                                      '{if t.mimetype == "audio/x-adpcm"}' +
                                          '{if t.url.substring(0, 4) == "http"}' +
                                              '<div id="oc-video-presenter-delivery-x-flv-http" style="display: none">' +
                                                  '${t.url}' +
                                              '</div>' +
                                              '<div id="oc-resolution-presenter-delivery-x-flv-http" style="display: none">' +
                                                  '${t.video.resolution}' +
                                              '</div>' +
                                              '<div id="oc-mimetype-presenter-delivery-x-flv-http" style="display: none">' +
                                                  '${t.mimetype}' +
                                              '</div>' +
                                          '{/if}' +
                                      '{/if}' +
                                  '{/if}' +
                                  
                                 '{if t.type == "presentation/delivery"}' +
                                     '{if t.mimetype == "video/x-flv"}' +
                                         '{if t.url.substring(0, 4) == "http"}' +
                                             '<div id="oc-video-presentation-delivery-x-flv-http" style="display: none">' +
                                                 '${t.url}' +
                                             '</div>' +
                                             '<div id="oc-resolution-presentation-delivery-x-flv-http" style="display: none">' +
                                                 '${t.video.resolution}' +
                                             '</div>' +
                                             '<div id="oc-mimetype-presentation-delivery-x-flv-http" style="display: none">' +
                                                 '${t.mimetype}' +
                                             '</div>' +
                                         '{/if}' +
                                     '{/if}' +
                                 '{/if}' +
         
                                  '{if t.type == "presenter/delivery"}' +
                                      '{if t.mimetype == "video/x-flv"}' +
                                          '{if t.url.substring(0, 4) == "rtmp"}' +
                                              '<div id="oc-video-presenter-delivery-x-flv-rtmp" style="display: none">' +
                                                  '${t.url}' +
                                              '</div>' +
                                              '<div id="oc-resolution-presenter-delivery-x-flv-rtmp" style="display: none">' +
                                                  '${t.video.resolution}' +
                                              '</div>' +
                                              '<div id="oc-mimetype-presenter-delivery-x-flv-rtmp" style="display: none">' +
                                                  '${t.mimetype}' +
                                              '</div>' +
                                          '{/if}' +
                                      '{/if}' +
                                  '{/if}' + 
         
                                  '{if t.type == "presentation/delivery"}' +
                                      '{if t.mimetype == "video/x-flv"}' +
                                          '{if t.url.substring(0, 4) == "rtmp"}' +
                                              '<div id="oc-video-presentation-delivery-x-flv-rtmp" style="display: none">' +
                                                  '${t.url}' +
                                              '</div>' +
                                              '<div id="oc-resolution-presentation-delivery-x-flv-rtmp" style="display: none">' +
                                                  '${t.video.resolution}' +
                                              '</div>' +
                                              '<div id="oc-mimetype-presentation-delivery-x-flv-rtmp" style="display: none">' +
                                                  '${t.mimetype}' +
                                              '</div>' +
                                          '{/if}' +
                                      '{/if}' +
                                  '{/if}' + 
          
                                  '{if t.type == "presenter/source"}' +
                                      '{if t.mimetype == "video/x-flv"}' +
                                          '{if t.url.substring(0, 4) == "http"}' +
                                              '<div id="oc-video-presenter-source-x-flv-http" style="display: none">' +
                                                  '${t.url}' +
                                              '</div>' +
                                              '<div id="oc-resolution-presenter-source-x-flv-http" style="display: none">' +
                                                  '${t.video.resolution}' +
                                              '</div>' +
                                              '<div id="oc-mimetype-presenter-source-x-flv-http" style="display: none">' +
                                                  '${t.mimetype}' +
                                              '</div>' +
                                          '{/if}' +
                                      '{/if}' +
                                 '{/if}' + 
          
                                  '{if t.type == "presentation/source"}' +
                                      '{if t.mimetype == "video/x-flv"}' +
                                          '{if t.url.substring(0, 4) == "http"}' +
                                              '<div id="oc-video-presentation-source-x-flv-http" style="display: none">' +
                                                  '${t.url}' +
                                              '</div>' +
                                              '<div id="oc-resolution-presentation-source-x-flv-http" style="display: none">' +
                                                  '${t.video.resolution}' +
                                              '</div>' +
                                              '<div id="oc-mimetype-presentation-source-x-flv-http" style="display: none">' +
                                                  '${t.mimetype}' +
                                              '</div>' +
                                          '{/if}' +
                                      '{/if}' +
                                 '{/if}' + 
         
                                 '{if t.type == "presenter/source"}' +
                                     '{if t.mimetype == "video/x-flv"}' +
                                         '{if t.url.substring(0, 4) == "rtmp"}' +
                                             '<div id="oc-video-presenter-source-x-flv-rtmp" style="display: none">' +
                                                 '${t.url}' +
                                             '</div>' +
                                             '<div id="oc-resolution-presenter-source-x-flv-rtmp" style="display: none">' +
                                                 '${t.video.resolution}' +
                                             '</div>' +
                                             '<div id="oc-mimetype-presenter-source-x-flv-rtmp" style="display: none">' +
                                                 '${t.mimetype}' +
                                             '</div>' +
                                         '{/if}' +
                                     '{/if}' +
                                '{/if}' + 
         
                                 '{if t.type == "presentation/source"}' +
                                     '{if t.mimetype == "video/x-flv"}' +
                                         '{if t.url.substring(0, 4) == "rtmp"}' +
                                             '<div id="oc-video-presentation-source-x-flv-rtmp" style="display: none">' +
                                                 '${t.url}' +
                                             '</div>' +
                                             '<div id="oc-resolution-presentation-source-x-flv-rtmp" style="display: none">' +
                                                 '${t.video.resolution}' +
                                             '</div>' +
                                             '<div id="oc-mimetype-presentation-source-x-flv-rtmp" style="display: none">' +
                                                 '${t.mimetype}' +
                                             '</div>' +
                                         '{/if}' +
                                     '{/if}' +
                                '{/if}' + 
                             '{forelse}' +
                                 '' +
                             '{/for}';
                                 
    var templateData1 =      '<div id="oc-title" style="display: none">' +
                                 '{if (dcTitle != undefined) && (dcTitle != "")}' +
                                     '${dcTitle}' +
                                 '{else}' +
                                     'No Title' +
                                 '{/if}' +
                             '</div>' +
                             '<div id="dc-extent" style="display: none">' +
                                 '{if dcExtent != undefined}' +
                                     '${dcExtent}' +
                                 '{else}' +
                                     '0' +
                                 '{/if}' +
                             '</div>' +
                             '<div id="oc-creator" style="display: none">' +
                                 '{if dcCreator != undefined}' +
                                     '${dcCreator}' +
                                 '{else}' +
                                     'No Creator' +
                                 '{/if}' +
                             '</div>' +
                             '<div id="oc-date" style="display: none">' +
                                 '{if dcCreated != undefined}' +
                                     '${dcCreated}' +
                                 '{else}' +
                                     '' +
                                 '{/if}' +
                             '</div>';
        
    var templateMPAttach1 =     '{for a in attachment}' +
                                    '{if a.type == "presenter/player+preview"}' +
                                        '<div id="oc-cover-presenter" style="display: none">' +
                                            '${a.url}' +
                                        '</div>' +
                                    '{/if}' +
                                    '{if a.type == "presentation/player+preview"}' +
                                        '<div id="oc-cover-presentation" style="display: none">' +
                                            '${a.url}' +
                                        '</div>' +
                                    '{/if}' +
                                '{forelse}' +
                                    '' +
                                '{/for}';
        
    var templateData2 =      '<div id="dc-subject" style="display: none">' +
                                 '{if (dcSubject != undefined) && (dcSubject != "")}' +
                                     '${dcTitle}' +
                                 '{else}' +
                                     'No Subject' +
                                 '{/if}' +
                             '</div>' +
                             '<div id="dc-contributor" style="display: none">' +
                                 '{if (dcContributor != undefined) && (dcContributor != "")}' +
                                     '${dcContributor}' +
                                 '{else}' +
                                     'No Department information' +
                                 '{/if}' +
                             '</div>' +
                             '<div id="dc-description" style="display: none">' +
                                 '{if (dcDescription != undefined) && (dcDescription != "")}' +
                                     '${dcDescription}' +
                                 '{else}' +
                                     'No Description' +
                                 '{/if}' +
                             '</div>' +
                             '<div id="dc-language" style="display: none">' +
                                 '{if (dcLanguage != undefined) && (dcLanguage != "")}' +
                                     '${dcLanguage}' +
                                 '{else}' +
                                     'No Language' +
                                 '{/if}' +
                             '</div>';
        
    var templateMPCatalog1 =    '{for c in catalog}' +
                                    '{if c.type == "captions/timedtext"}' +
                                        '{if c.mimetype == "text/xml"}' +
                                            '<div id="oc-captions" style="display: none">' +
                                                '${c.url}' +
                                            '</div>' +
                                        '{/if}' +
                                    '{/if}' +
                                '{forelse}' +
                                    '' +
                                '{/for}';
    
    var templateSegments2 = '{for s in segment}' +
                                '{if s.durationIncludingSegment >= currentTime}' +
                                    '<tr>' +
                                        '<td class="oc-segments-preview">' +
                                            '${s.previews.preview.$}' +
                                        '</td>' +
                                        '<td class="oc-segments-time">' +
                                            '<a class="oc_segments-time" ' +
                                                'onclick="Opencast.Watch.seekSegment(${Math.floor(parseInt(s.time) / 1000)})>" ' +
                                            '</a>' +
                                        '</td>' +
                                        '<td>' +
                                            '${s.text}' +
                                        '</td>' +
                                    '</tr>' +
                                '{/if}' +
                            '{forelse}' +
                                '<td style="width: 100%;" id="segment-holder-empty" class="segment-holder" />' +
                            '{/for}';

    // The Elements to put the div into
    var elementSegments1,
        elementMedia1,
        elementData1,
        elementMediaPackage1,
        elementData2,
        elementMediaPackage2,
        elementSegments2;
    
    // Data to process == search-result.results.result
    // 0 = everything
    // 1 = segments -- segment
    // 2 = mediapackage.media -- track
    // 3 = mediapackage.attachments --attachment
    // 4 = mediapackage.metadata -- catalog
    var segments_ui_data,
        segments_ui_dataSegments,
        segments_ui_dataMPMedia,
        segments_ui_dataMPAttach,
        segments_ui_dataMPCatalog;
        
    // Precessed Data
    var processedTemplateData;

    /**
     * @memberOf Opencast.segments_ui_Plugin
     * @description Add As Plug-in
     * @param elemSegments1 First Segments Element
     * @param elemMedia1 First Media Element
     * @param elemData1 First Data Element
     * @param elemMediaPackage1 First Media Package Element
     * @param elemData2 Second Data Element
     * @param elemMediaPackage2 Second Media Package Element
     * @param elemSegments2 Second Segments Element
     * @param data Data to fill the Elements with
     * @param withSegments boolean Flag if parse with Segments or without
     */
    function addAsPlugin(elemSegments1,
                         elemMedia1,
                         elemData1,
                         elemMediaPackage1,
                         elemData2,
                         elemMediaPackage2,
                         elemSegments2,
                         data,
                         withSegments) {
        elementSegments1 = elemSegments1;
        elementMedia1 = elemMedia1;
        elementData1 = elemData1;
        elementMediaPackage1 = elemMediaPackage1;
        elementData2 = elemData2;
        elementMediaPackage2 = elemMediaPackage2;
        elementSegments2 = elemSegments2;

        segments_ui_data = data;
        segments_ui_dataSegments = segments_ui_data.segments;
        segments_ui_dataMPMedia = segments_ui_data.mediapackage.media;
        segments_ui_dataMPAttach = segments_ui_data.mediapackage.attachments;
        segments_ui_dataMPCatalog = segments_ui_data.mediapackage.metadata;
        createSegments(withSegments);
    }

    /**
     * @memberOf Opencast.segments_ui_Plugin
     * @description Processes the Data and puts it into the Element
     * @param withSegments true if process with Segments, false if without Segments
     */
    function createSegments(withSegments) {
        // Process Element Segments 1
        if (withSegments && (elementSegments1 !== undefined)) {
            processedTemplateData = templateSegments1.process(segments_ui_dataSegments);
            elementSegments1.html(processedTemplateData);
        }

        // Process Element Media 1
        if (elementMedia1 !== undefined) {
            processedTemplateData = templateMedia1.process(segments_ui_dataMPMedia);
            elementMedia1.html(processedTemplateData);
        }

        // Process Element Data 1
        if (elementData1 !== undefined) {
            processedTemplateData = templateData1.process(segments_ui_data);
            elementData1.html(processedTemplateData);
        }

        // Process Element MediaPackage 1
        if (elementMediaPackage1 !== undefined) {
            processedTemplateData = templateMPAttach1.process(segments_ui_dataMPAttach);
            elementMediaPackage1.html(processedTemplateData);
        }

        // Process Element Data 2
        if (elementData2 !== undefined) {
            processedTemplateData = templateData2.process(segments_ui_data);
            elementData2.html(processedTemplateData);
        }

        // Process Element MediaPackage 2
        if (elementMediaPackage2 !== undefined) {
            processedTemplateData = templateMPCatalog1.process(segments_ui_dataMPCatalog);
            elementMediaPackage2.html(processedTemplateData);
        }

        // Process Element Segments 2
        if (withSegments && (elementSegments2 !== undefined)) {
            processedTemplateData = templateSegments2.process(segments_ui_dataSegments);
            elementSegments2.html(processedTemplateData);
        }
    }

    return {
        addAsPlugin: addAsPlugin
    };
}());
