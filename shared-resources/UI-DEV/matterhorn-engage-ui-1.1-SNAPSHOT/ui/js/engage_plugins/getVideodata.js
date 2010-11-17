/*global $, Opencast*/
/*jslint browser: true, white: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, newcap: true, immed: true, onevar: false */

var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace annotation_chapter delegate. This file contains the rest endpoint and passes the data to the annotation_chapter plugin
 */
Opencast.Videodata = (function(){

    /**
     *  variables
     */
   
   
   function getVideos(){
   
   alert("getVideos");
//	var mediaPackageId = Opencast.engage.getMediaPackageId();
//$.getJSON('../../search/rest/episode.json?id=f48c2be3-c2ca-4bc7-9725-c9e93259b67a&jsonp=?", function (data)
  //     {
    //       var arr = new Array();
     //      var type, mimetype;
      //     for(var i = 0; i < data['search-results'].result.mediapackage.media.track.length; ++i)
       //    {
               //type = data['search-results'].result.mediapackage.media.track[i].@type;
               //mimetype = data['search-results'].result.mediapackage.media.track[i].mimetype;
               //if(((type == "presenter/delivery") || (type == "presentation/delivery")) && (mimetype == "video/x-flv"))
               //{
                //   arr[arr.length] = data['search-results'].result.mediapackage.media.track[i].url;
               //}	   
         //  }
		  //Videodisplay.initMedia('rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/0a78520a-3c23-4379-bcc7-eb7c0a803070/track-6/dozent.flv','rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/0a78520a-3c23-4379-bcc7-eb7c0a803070/track-7/vga.flv');
       Videodisplay.initMedia('rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/algorithmen09_2009_10_27_14_9__131_173_10_32.flv','rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/algorithmen09_2009_10_27_14_9__131_173_10_32.flv');
     //  Videodisplay.initMedia(arr[0],arr[0]);
	   //});
	}
   return {
        getVideos : getVideos
    };
}());
