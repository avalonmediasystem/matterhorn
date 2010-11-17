package org.opencast.engage.videodisplay.util
{
	import bridge.ExternalFunction;

	import flash.external.ExternalInterface;

	import mx.core.Application;

	import org.opencast.engage.videodisplay.model.VideodisplayModel;
	import org.osmf.containers.MediaContainer;
	import org.osmf.elements.LightweightVideoElement;
	import org.osmf.elements.ParallelElement;
	import org.osmf.events.DisplayObjectEvent;
	import org.osmf.events.LoadEvent;
	import org.osmf.events.MediaErrorEvent;
	import org.osmf.events.MediaPlayerCapabilityChangeEvent;
	import org.osmf.events.TimeEvent;
	import org.osmf.layout.HorizontalAlign;
	import org.osmf.layout.LayoutMetadata;
	import org.osmf.layout.LayoutMode;
	import org.osmf.layout.ScaleMode;
	import org.osmf.layout.VerticalAlign;
	import org.osmf.media.MediaPlayer;
	import org.osmf.media.URLResource;
	import org.osmf.traits.AudioTrait;
	import org.osmf.traits.LoadState;
	import org.osmf.traits.MediaTraitType;
	import mx.controls.Alert;

	/**
	 *
	 * @author ket
	 */
	public class ParallelMedia
	{

		/**
		 *
		 * @default
		 */
		public static const HUTTO1:String="rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/0a78520a-3c23-4379-bcc7-eb7c0a803070/track-6/dozent.flv";
		/**
		 *
		 * @default
		 */
		public static const HUTTO2:String="rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/0a78520a-3c23-4379-bcc7-eb7c0a803070/track-7/vga.flv";

		public static const VORN:String="rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/4eb107ec-3310-4c90-88b2-61784fc496f4/track-4/dozent.flv";

		public static const FMS:String="rtmp://cp67126.edgefcs.net/ondemand/mediapm/osmf/content/test/akamai_10_year_f8_512K";
		/**
		 *
		 * @default
		 */
		public static const PROGRESS:String="http://mediapm.edgesuite.net/strobe/content/test/AFaerysTale_sylviaApostol_640_500_short.flv";
		/**
		 *
		 * @default
		 */
		public static const lighty:String="http://131.173.22.24/static/dab950e1-c64b-4907-b9e3-c61bf8ec6110/track-10/vga.mp4";


		private var _time:TimeCode;

		/**
		 *
		 * @param mediaUrl1
		 * @param mediaUrl2
		 */
		public function ParallelMedia(mediaUrl1:String, mediaUrl2:String)
		{
			_url1=mediaUrl1;
			_url2=mediaUrl2;
			//Alert.show("ParallelMedia");
			mediaContainer=new MediaContainer();
			// initialize the timeCode
			_time=new TimeCode();

			var leftlayoutData:LayoutMetadata=new LayoutMetadata();
			leftlayoutData.width=Application.application.width;
			leftlayoutData.height=Application.application.height;
			leftlayoutData.scaleMode=ScaleMode.LETTERBOX;

			var rigthlayoutData:LayoutMetadata=new LayoutMetadata();
			rigthlayoutData.width=Application.application.width;
			rigthlayoutData.height=Application.application.height;
			rigthlayoutData.scaleMode=ScaleMode.LETTERBOX;

			videoElement=new LightweightVideoElement();
			videoElement.resource=new URLResource(mediaUrl1);
			videoElement.smoothing=true;
			videoElement.defaultDuration=1000;
			//var mediaElementVideoOne:MediaElement=videoElement;

			videoElement2=new LightweightVideoElement();
			videoElement2.resource=new URLResource(mediaUrl2);
			videoElement2.smoothing=true;
			videoElement2.defaultDuration=1000;
			//var mediaElementVideoTwo:MediaElement=videoElement2;


			//testvideo3=new LightweightVideoElement();
			//testvideo3.resource=new URLResource(FMS);
			//testvideo3.smoothing=true;


			var oProxyElementTwo:OProxyElement=new OProxyElement(videoElement2);

			videoElement.metadata.addValue(LayoutMetadata.LAYOUT_NAMESPACE, leftlayoutData);
			videoElement2.metadata.addValue(LayoutMetadata.LAYOUT_NAMESPACE, rigthlayoutData);
			//testvideo3.metadata.addValue(LayoutMetadata.LAYOUT_NAMESPACE, rigthlayoutData);

			var parallelElement:ParallelElement=new ParallelElement();

			parallelElement.addChildAt(videoElement, 0);
			parallelElement.addChildAt(oProxyElementTwo, 1);
			//parallelElement.addChildAt(testvideo3, 2);
			//parallelElement.addChildAt(videoElement2, 0);

			var layoutData:LayoutMetadata=new LayoutMetadata();
			layoutData.layoutMode=LayoutMode.HORIZONTAL;

			layoutData.horizontalAlign=HorizontalAlign.LEFT;
			layoutData.verticalAlign=VerticalAlign.TOP;
			//layoutData.horizontalAlign=HorizontalAlign.CENTER;
			//layoutData.verticalAlign=VerticalAlign.MIDDLE;

			layoutData.top=-10;
			layoutData.left=-10;
			//layoutData.width=Application.application.width;
			//layoutData.height=Application.application.height;
			layoutData.scaleMode=ScaleMode.LETTERBOX;

			parallelElement.metadata.addValue(LayoutMetadata.LAYOUT_NAMESPACE, layoutData);

			player=new MediaPlayer(parallelElement);
			player.autoRewind=true;
			player.autoPlay=false;

			player.addEventListener(MediaPlayerCapabilityChangeEvent.CAN_PLAY_CHANGE, onViewable);
			player.addEventListener(DisplayObjectEvent.MEDIA_SIZE_CHANGE, onDimensionChange);
			player.addEventListener(TimeEvent.DURATION_CHANGE, durationChange);
			player.addEventListener(TimeEvent.CURRENT_TIME_CHANGE, onCurrentTimeChange);



			model.player=player;
			mediaContainer.addMediaElement(parallelElement);
			model.parallelMediaContainer=mediaContainer;

			Application.application.bx_multi.attachVideo(mediaContainer);

			// ExternalInterface.call( ExternalFunction.SETTOTALTIME, 120000 );
			//  ExternalInterface.call( ExternalFunction.SETVOLUMESLIDER, 100 );

		}

		/**
		 *
		 * @default
		 */
		public var audiotrait:AudioTrait;
		/**
		 *
		 * @default
		 */
		public var mediaContainer:MediaContainer;


		/**
		 *
		 * @default
		 */
		[Bindable]
		public var model:VideodisplayModel=VideodisplayModel.getInstance();
		/**
		 *
		 * @default
		 */
		public var player:MediaPlayer;

		private var _url1:String;
		private var _url2:String;

		private var videoElement:LightweightVideoElement;
		private var videoElement2:LightweightVideoElement;
		private var testvideo3:LightweightVideoElement;

		/**
		 *
		 * @param info
		 */
		public function onMetadata(info:Object):void
		{
			//here you get your metadata extracting it from the info
			//Object…
			trace("Metadata");
			trace("Width: " + info.width);
			trace("Heigth: " + info.height);

		}

		/**
		 *
		 * @param evt
		 */
		private function onDimensionChange(evt:DisplayObjectEvent):void
		{
			//First we resize the OSMF view (the classic Video element
			//in AS3
			model.player.displayObject.width=evt.newWidth;
			model.player.displayObject.height=evt.newHeight;
			//Then we add the view to the stage …
			//mc_video.addChild(myPlayer.displayObject);
		}

		/**
		 *
		 * @param evt
		 */
		private function onError(evt:MediaErrorEvent):void
		{
			trace(evt.error) //here you can know what was the //error type…

			if (evt.type == MediaErrorEvent.MEDIA_ERROR)
			{
				trace("video not found");

			}
		}

		/**
		 *
		 * @param event
		 */
		private function onLoad(event:LoadEvent):void
		{
			//this means, it loaded OK..
			if (event.loadState == LoadState.READY)
			{
				// videoElement.client.addHandler( NetStreamCodes.ON_META_DATA, onMetadata );
				// videoElement.addEventListener( MediaErrorEvent.MEDIA_ERROR, onError );
			}
		}

		/**
		 *
		 * @param evt
		 */
		private function onViewable(evt:MediaPlayerCapabilityChangeEvent):void
		{
			if (evt.enabled)
			{
				// We need to wait for this event before making anything..
				//If the video is loaded and ready, just play it automatically…

				videoElement.getTrait(MediaTraitType.LOAD).addEventListener(LoadEvent.LOAD_STATE_CHANGE, onLoad);
				videoElement2.getTrait(MediaTraitType.LOAD).addEventListener(LoadEvent.LOAD_STATE_CHANGE, onLoad);

				if (model.player.canPlay)
				{
					model.player.play();
				}
			}
		}

		private function durationChange(event:TimeEvent):void
		{
			// Store new duration as current duration in the videodisplay model
			//duration might change in a media composition
			model.currentDuration=event.time;
			model.currentDurationString=_time.getTC(event.time);
			ExternalInterface.call(ExternalFunction.SETDURATION, event.time);
			ExternalInterface.call(ExternalFunction.SETTOTALTIME, model.currentDurationString);
			ExternalInterface.call(ExternalFunction.SETVOLUMESLIDER, 100);

		}


		/**
		 * onCurrentTimeChange
		 * When the current time is change.
		 * @eventType TimeEvent event
		 */
		private function onCurrentTimeChange(event:TimeEvent):void
		{
			model.currentPlayheadSingle=event.time;
			var newPositionString:String='';
			newPositionString=_time.getTC(model.currentPlayheadSingle);
			ExternalInterface.call(ExternalFunction.SETCURRENTTIME, newPositionString);
			ExternalInterface.call(ExternalFunction.SETPLAYHEAD, model.currentPlayheadSingle);
		}

	}

}

