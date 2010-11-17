/**
 *  Copyright 2009 The Regents of the University of California
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
package org.opencast.engage.videodisplay.business
{
	import bridge.ExternalFunction;

	import com.adobe.cairngorm.control.CairngormEventDispatcher;

	import flash.external.ExternalInterface;
	import mx.controls.Alert;
	import org.opencast.engage.videodisplay.event.InitMediaPlayerEvent;
	import org.opencast.engage.videodisplay.event.VideoplayerInteractionEvent;
	import org.opencast.engage.videodisplay.model.VideodisplayModel;
	import org.opencast.engage.videodisplay.util.TimeCode;

	public class FlexAjaxBridge
	{

		[Bindable]
		public var model:VideodisplayModel;

		private var _time:TimeCode;

		private var undef:int;

		/**
		 * Constructor
		 */
		public function FlexAjaxBridge()
		{
		}

		/**
		 * setMediaURL
		 * Set media URL. Call the event InitMediaPlayerEvent.
		 * Developer: You can change your own urls here.
		 * @param String coverURLOne, String coverURLTwo, String mediaURLOne, String mediaURLTwo, String mimetypeOne, String mimetypeTwo, String playerMode
		 */
		public function initMedia(url1:String, url2:String):void
		{
			var initMediaPlayerEvent:InitMediaPlayerEvent=new InitMediaPlayerEvent(url1, url2);
			CairngormEventDispatcher.getInstance().dispatchEvent(initMediaPlayerEvent);
		}



		/**
		 * play
		 * When the learnder click the play button. Call the event VideoControlEvent.PLAY.
		 * The return value is available in the calling javascript
		 * @return String model.currentPlayerState
		 */
		public function play():void
		{
			var videoplayerInteractionEvent:VideoplayerInteractionEvent=new VideoplayerInteractionEvent("PLAY", undef);
			CairngormEventDispatcher.getInstance().dispatchEvent(videoplayerInteractionEvent);
		}

		/**
		 * pause
		 * When the learnder click the pause button. Call the event VideoControlEvent.PAUSE.
		 * The return value is available in the calling javascript.
		 * @return String model.currentPlayerState
		 */
		public function pause():void
		{
			var videoplayerInteractionEvent:VideoplayerInteractionEvent=new VideoplayerInteractionEvent("PAUSE", undef);
			CairngormEventDispatcher.getInstance().dispatchEvent(videoplayerInteractionEvent);
		}

		/**
		 * stop
		 * When the learnder click the stop button. Call the event VideoControlEvent.STOP.
		 *
		 */
		public function stop():void
		{
			var videoplayerInteractionEvent:VideoplayerInteractionEvent=new VideoplayerInteractionEvent("STOP", undef);
			CairngormEventDispatcher.getInstance().dispatchEvent(videoplayerInteractionEvent);
		}

		public function setMediaResolution(newWidthMediaOne:Number, newHeightMediaOne:Number, newWidthMediaTwo:Number, newHeightMediaTwo:Number, multiMediaContainerLeft:Number):void
		{

		}


		public function seek(time:Number):Number
		{
			//var test : Number = 400;
			//Alert.show("seek: "+time);
			_time=new TimeCode();
			var newPositionString:String=_time.getTC(time);
			ExternalInterface.call(ExternalFunction.SETCURRENTTIME, newPositionString);
			var videoplayerInteractionEvent:VideoplayerInteractionEvent=new VideoplayerInteractionEvent("SEEK", time);
			CairngormEventDispatcher.getInstance().dispatchEvent(videoplayerInteractionEvent);
			/*  if( model.startPlay == false )
			   {
			   model.startSeek = time;
			   _time = new TimeCode();
			   var newPositionString : String = _time.getTC( time );
			   ExternalInterface.call( ExternalFunction.SETCURRENTTIME, newPositionString );
			   }
			 model.player.seek(time);*/
			return time;
		}

		/**
		 * onBridgeReady
		 * When the birdge ist ready call the external function ONPLAYERREADY.
		 */
		public function onBridgeReady():void
		{
			ExternalInterface.call(ExternalFunction.ONPLAYERREADY, true);
		}
	}
}

