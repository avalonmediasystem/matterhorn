/*global $, Opencast*/
/*jslint browser: true, white: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, newcap: true, immed: true, onevar: false */

var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace annotation_chapter delegate. This file contains the rest endpoint and passes the data to the annotation_chapter plugin
 */
Opencast.insertFlash = (function(){

    /**
     *  variables
     */
   
   
   function insertFlash(){
		alert("da");
        $('#oc_flash-player').flash({
    src: 'Videodisplay.swf',
    width: 320,
    height: 240
});

        
        }
   return {
        insertFlash : insertFlash
    };
}());
