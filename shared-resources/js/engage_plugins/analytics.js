/*global $, Opencast*/
/*jslint browser: true, white: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, newcap: true, immed: true, onevar: false */
var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace analytics delegate. This file contains the rest endpoint and passes the data to the analytics plugin
 */
Opencast.Analytics = (function ()
{
    var mediaPackageId, duration, interval;
    var intervalRunning = false;
    var updateInterval = 5000; // in ms
    var analyticsDisplayed = false;
    var ANALYTICS = "Analytics",
        ANALYTICSHIDE = "Analytics off";
    var dateIn,
        resizeEndTimeoutRunning,
        waitForMove = 150;

    /**
     * @memberOf Opencast.Analytics
     * @description Returns if Analytics is currently visible
     * @return true if Analytics is currently visible, false else
     */
    function isVisible()
    {
        return analyticsDisplayed;
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Initializes Analytics
     *              Checks whether Data are available. If not: Hide Analytics
     */
    function initialize()
    {
        // Request JSONP data
        $.ajax(
        {
            type: 'GET',
            contentType: 'text/xml',
            url: "../../usertracking/footprint.xml",
            data: "id=" + mediaPackageId,
            dataType: 'xml',
            success: function (xml)
            {
                var tmpData = $(xml).find('footprint');
                if (tmpData !== undefined)
                {
                    // Display the controls
                    $('#oc_checkbox-statistics').show();
                    $('#oc_label-statistics').show();
                    $('#oc_video-view').show();
                } else
                {
                    displayNoAnalyticsAvailable("No data defined (1), initialize");
                }
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                displayNoAnalyticsAvailable("No data available (1), initialize");
            }
        });
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Show Analytics
     * @param resized if resized (== used cashed footprint) or not (== request new data))
     */
    function showAnalytics(resized)
    {
        if(resized && isVisible())
        {
            var rez = Opencast.AnalyticsPlugin.resizePlugin();
            if(rez)
            {
                return;
            }
        }
        // Request JSONP data
        $.ajax(
        {
            type: 'GET',
            contentType: 'text/xml',
            url: "../../usertracking/footprint.xml",
            data: "id=" + mediaPackageId,
            dataType: 'xml',
            success: function (xml)
            {
                var position = 0;
                var views;
                var lastPosition = -1;
                var lastViews;
                // Check if duration is an Integer
                if (!isNaN(duration) && (typeof(duration) == 'number') && (duration.toString().indexOf('.') == -1))
                {
                    var footprintData = new Array(duration);
                    for (var i = 0; i < footprintData.length; i++)
                    footprintData[i] = 0;
                    $(xml).find('footprint').each(function ()
                    {
                        position = parseInt($(this).find('position').text());
                        views = parseInt($(this).find('views').text());
                        if (position - 1 != lastPosition)
                        {
                            for (var j = lastPosition + 1; j < position; j++)
                            {
                                footprintData[j] = lastViews;
                            }
                        }
                        footprintData[position] = views;
                        lastPosition = position;
                        lastViews = views;
                    });
                    var plugAn = Opencast.AnalyticsPlugin.addAsPlugin($('#analytics'), footprintData);
                    if(plugAn)
                    {
                        if(Opencast.segments.getSlideLength() > 0)
                        {
                            $(".segments").css('top', '-25px');
                            $('#oc_video-view').css('top', '-23px');
                            $('#segmentstable1').css('opacity', '0.65');
                            $('#segmentstable1').css('filter', 'alpha(opacity=65)');
                            $('#oc_video-view').css('position', 'relative');
                        }
                        $('#annotation').css('top', '-25px');
                        $("#analytics").show();
                        
                        //$.sparkline_display_visible();
                        analyticsDisplayed = true;
                        
                        if(!intervalRunning)
                        {
                            // Display actual Results every updateIntervall Milliseconds
                            interval = setInterval(function()
                            {
                                showAnalytics(false);
                            }, updateInterval);
                            intervalRunning = true;
                            showAnalytics(false);
                        }
                    } else
                    {
                        displayNoAnalyticsAvailable("No template available (1)");
                    }
                }
                else
                {
                    displayNoAnalyticsAvailable("No data defined (1)");
                }
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                displayNoAnalyticsAvailable("No data available (1)");
            }
        });
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Displays that no Analytics is available and hides Annotations
     * @param errorDesc Error Description (optional)
     */
    function displayNoAnalyticsAvailable(errorDesc)
    {
        errorDesc = errorDesc || '';
        var optError = (errorDesc != '') ? (": " + errorDesc) : '';
        $("#analytics").html("No analytics available" + optError);
        $('#oc_checkbox-statistics').removeAttr("checked");
        $('#oc_checkbox-statistics').attr('disabled', true);
        $('#oc_checkbox-statistics').hide();
        $('#oc_label-statistics').hide();
        hideAnalytics();
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Binds the Window-Resize-Event
     */
    function initResizeEnd()
    {
        resizeEndTimeoutRunning = false;
        $(window).resize(function()
        {
            dateIn = new Date();
            if (resizeEndTimeoutRunning === false)
            {
                resizeEndTimeoutRunning = true;
                resizeEndTimeoutRunning = setTimeout(resizeEnd, waitForMove);
            }
        });
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Checks if resize is over
     */
    function resizeEnd()
    {
        var dateOut = new Date();
        // if the Resize-Event is not over yet: set new timeout
        if ((dateOut - dateIn) < waitForMove)
        {
            setTimeout(resizeEnd, waitForMove);
        } else {
            // else: repaint Statistics div
            resizeEndTimeoutRunning = false;
            showAnalytics(true);
        }
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Hide the notes
     */
    function hideAnalytics()
    {
        analyticsDisplayed = false;
        if(intervalRunning)
        {
            // Clear Update-Intervall
            clearInterval(interval);
            intervalRunning = false;
        }
        $("#analytics").css('display', 'none');
        $(".segments").css('top', '0');
        $('#oc_video-view').css('top', 'auto');
        $("#annotation_holder").css('top', '0');
        $('#annotation_holder').css('float', 'left');
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Toggle Analytics
     */
    function doToggleAnalytics()
    {
        if (!analyticsDisplayed)
        {
            initResizeEnd();
            showAnalytics(false);
        }
        else
        {
            hideAnalytics();
        }
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Set the mediaPackageId
     * @param String mediaPackageId
     */
    function setMediaPackageId(id)
    {
        mediaPackageId = id;
    }
    
    /**
     * @memberOf Opencast.Analytics
     * @description Set the duration
     * @param int duration 
     */
    function setDuration(val)
    {
        duration = val;
    }
    
    return {
        initialize: initialize,
        isVisible: isVisible,
        hideAnalytics: hideAnalytics,
        showAnalytics: showAnalytics,
        setDuration: setDuration,
        setMediaPackageId: setMediaPackageId,
        doToggleAnalytics: doToggleAnalytics
    };
}());
