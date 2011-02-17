/*global $, Opencast*/
/*jslint browser: true, white: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, newcap: true, immed: true, onevar: false */
var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace Description
 */
Opencast.Description = (function ()
{
    var mediaPackageId, duration;
    var DESCRIPTION = "Description",
        DESCRIPTION_HIDE = "Hide Description";
        
    /**
     * @memberOf Opencast.Description
     * @description Displays the Description Tab
     */
    function showDescription()
    {
        // Hide other Tabs
        Opencast.segments.hideSegments();
        Opencast.segments_text.hideSegmentsText();
        Opencast.search.hideSearch();
        // Change Tab Caption
        $('#oc_btn-description').attr(
        {
            title: DESCRIPTION_HIDE
        });
        $('#oc_btn-description').html(DESCRIPTION_HIDE);
        $("#oc_btn-description").attr('aria-pressed', 'true');
        // Show a loading Image
        $('#oc_description').show();
        $('#description-loading').show();
        $('#oc-description').hide();
        // Request JSONP data
        $.ajax(
        {
            url: '../../search/episode.json',
            data: 'id=' + mediaPackageId,
            dataType: 'jsonp',
            jsonp: 'jsonp',
            success: function (data)
            {
                var timeDate = data['search-results'].result.dcCreated;
                var sd = new Date();
                sd.setMinutes(parseInt(timeDate.substring(14, 16), 10));
                sd.setSeconds(parseInt(timeDate.substring(17, 19), 10));
                data['search-results'].result.dcCreated = sd.toLocaleString();
                
                // Trimpath throws (no) errors if a variable is not defined => assign default value
                var tmp = data['search-results'].result.dcSeriesTitle;
                data['search-results'].result.dcSeriesTitle = ((tmp !== undefined) && (tmp !== null)) ? data['search-results'].result.dcSeriesTitle : '';
                tmp = data['search-results'].result.dcContributor;
                data['search-results'].result.dcContributor = ((tmp !== undefined) && (tmp !== null)) ? data['search-results'].result.dcContributor : '';
                tmp = data['search-results'].result.dcLanguage;
                data['search-results'].result.dcLanguage = ((tmp !== undefined) && (tmp !== null)) ? data['search-results'].result.dcLanguage : '';
                tmp = data['search-results'].result.dcViews;
                data['search-results'].result.dcViews = ((tmp !== undefined) && (tmp !== null)) ? data['search-results'].result.dcViews : '';
                tmp = data['search-results'].result.dcCreator;
                data['search-results'].result.dcCreator = ((tmp !== undefined) && (tmp !== null)) ? data['search-results'].result.dcCreator : '';
                
                // Request JSONP data (Stats)
                $.ajax(
                {
                    url: '../../usertracking/stats.json?id=' + mediaPackageId,
                    dataType: 'jsonp',
                    jsonp: 'jsonp',
                    success: function (result)
                    {
                        // If episode is part of a series: get series data    
                        if (data['search-results'].result.dcIsPartOf != '')
                        {
                            // Request JSONP data (Series)
                            $.ajax(
                            {
                                url: '../../series/' + data['search-results'].result.dcIsPartOf + ".json",
                                dataType: 'jsonp',
                                jsonp: 'jsonp',
                                success: function (res)
                                {
                                    for (var i = 0; i < res.series.additionalMetadata.metadata.length; i++)
                                    {
                                        if (res.series.additionalMetadata.metadata[i].key == 'title')
                                        {
                                            data['search-results'].result.dcSeriesTitle = res.series.additionalMetadata.metadata[i].value;
                                        }
                                    }
                                    // Create Trimpath Template
                                    Opencast.Description_Plugin.addAsPlugin($('#oc-description'), data['search-results']);
                                    // Make visible
                                    $('#description-loading').hide();
                                    $('#oc-description').show();
                                },
                                // If no data comes back (JSONP-Call #3)
                                error: function (xhr, ajaxOptions, thrownError)
                                {
                                    $('#description-loading').hide();
                                    $('#oc-description').html('No Description available');
                                    $('#oc-description').show();
                                    $('#scrollcontainer').hide();
                                }
                            });
                        }
                        else
                        {
                            // Create Trimpath Template
                            Opencast.Description_Plugin.addAsPlugin($('#oc-description'), data['search-results']);
                            // Make visible
                            $('#description-loading').hide();
                            $('#oc-description').show();
                        }
                    },
                    // If no data comes back (JSONP-Call #2)
                    error: function (xhr, ajaxOptions, thrownError)
                    {
                        $('#description-loading').hide();
                        $('#oc-description').html('No Description available');
                        $('#oc-description').show();
                        $('#scrollcontainer').hide();
                    }
                });
            },
            // If no data comes back (JSONP-Call #1)
            error: function (xhr, ajaxOptions, thrownError)
            {
                $('#description-loading').hide();
                $('#oc-description').html('No Description available');
                $('#oc-description').show();
                $('#scrollcontainer').hide();
            }
        });
    }
    
    /**
     * @memberOf Opencast.Description
     * @description Hides the Description Tab
     */
    function hideDescription()
    {
        // Change Tab Caption
        $('#oc_btn-description').attr(
        {
            title: DESCRIPTION
        });
        $('#oc_btn-description').html(DESCRIPTION);
        $("#oc_btn-description").attr('aria-pressed', 'false');
        $('#oc_description').hide();
    }
    
    /**
     * @memberOf Opencast.Description
     * @description Toggles the Description Tab
     */
    function doToggleDescription()
    {
        if ($('#oc_btn-description').attr("title") === DESCRIPTION)
        {
            Opencast.segments.hideSegments();
            Opencast.segments_text.hideSegmentsText();
            Opencast.search.hideSearch();
            showDescription();
        }
        else
        {
            hideDescription();
        }
    }
    
    /**
     * @memberOf Opencast.Description
     * @description Set the mediaPackageId
     * @param String mediaPackageId
     */
    function setMediaPackageId(id)
    {
        mediaPackageId = id;
    }
    
    return {
        showDescription: showDescription,
        hideDescription: hideDescription,
        setMediaPackageId: setMediaPackageId,
        doToggleDescription: doToggleDescription
    };
}());
