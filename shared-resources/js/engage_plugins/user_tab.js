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
 * @namespace the global Opencast namespace Description
 */
Opencast.User = (function ()
{
    /**
     * @memberOf Opencast.User
     * @description Displays the User Tab
     */
    function showUserTab()
    {
        // Hide other Tabs
        Opencast.segments.hideSegments();
        Opencast.segments_text.hideSegmentsText();
        Opencast.search.hideSearch();
        Opencast.Description.hideDescription();
        // Change Tab Caption
        $('#oc_btn-clipshow-user').attr(
        {
            title: "Hide User"
        });
        $('#oc_btn-clipshow-user').html("Hide User");
        $("#oc_btn-clipshow-user").attr('aria-pressed', 'true');
        // Show a loading Image
        $('#oc_clipshow-user').show();
        $('#clipshow-user-loading').show();
        $('#oc-clipshow-user').hide();
        Opencast.User_Plugin.addAsPlugin($('#oc-clipshow-user'));
    }

    /**
     * @memberOf Opencast.Description
     * @description Displays that no Description is available
     * @param errorDesc Error Description (optional)
     */
    function displayNoDescriptionAvailable(errorDesc)
    {
        errorDesc = errorDesc || '';
        $('#description-loading').hide();
        var optError = (errorDesc != '') ? (": " + errorDesc) : '';
        $('#oc-description').html('No Description available' + optError);
        $('#oc-description').show();
        $('#scrollcontainer').hide();
    }
    
    /**
     * @memberOf Opencast.User
     * @description Hides the User Tab
     */
    function hideUserTab()
    {
        // Change Tab Caption
        $('#oc_btn-clipshow-user').attr(
        {
            title: "User"
        });
        $('#oc_btn-clipshow-user').html("User");
        $("#oc_btn-clipshow-user").attr('aria-pressed', 'false');
        $('#oc_clipshow-user').hide();
    }
    
    /**
     * @memberOf Opencast.User
     * @description Toggles the User Tab
     */
    function doToggleUserTab()
    {
        if ($('#oc_btn-clipshow-user').attr("title") === "User")
        {
            showUserTab();
        }
        else
        {
            hideUserTab();
        }
    }
    
    return {
        showUserTab: showUserTab,
        hideUserTab: hideUserTab,
        doToggleUserTab: doToggleUserTab
    };
}());
