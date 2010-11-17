package org.opencast.engage.videodisplay.model
{
	import com.adobe.cairngorm.model.ModelLocator;

	import mx.core.UIComponent;

	import org.opencast.engage.videodisplay.state.CoverState;
	import org.opencast.engage.videodisplay.util.ParallelMedia;
	import org.osmf.containers.MediaContainer;
	import org.osmf.media.MediaPlayer;

	[Bindable]
	public class VideodisplayModel implements com.adobe.cairngorm.model.ModelLocator
	{
		private static var videodisplayModel:VideodisplayModel;

		public static function getInstance():VideodisplayModel
		{
			if (videodisplayModel == null)
				videodisplayModel=new VideodisplayModel();

			return videodisplayModel;
		}

		public function VideodisplayModel()
		{
			if (videodisplayModel != null)
				throw new Error("Only one ModelLocator instance should be instantiated.");
		}



		public var videoURLOne:String="";
		public var videoURLTwo:String="";

		public var parallelMedia:ParallelMedia;
		public var parallelMediaContainer:MediaContainer;
		public var mediaContainerUIComponent:UIComponent;
		public var player:MediaPlayer;

// slideLength
		public var slideLength:int;

		public var coverURLOne:String;
		public var coverURLTwo:String;

		public var coverURLSingle:String;

		// coverState
		public var coverState:String=CoverState.ONECOVER;


		// Current Duration
		public var currentDuration:Number=0;
		// Current Duration String
		public var currentDurationString:String='';
		// Current PlayheadSingle
		public var currentPlayheadSingle:Number=0;

		// Current Player State
		public var currentPlayerState:String;
		// playerMode
		public var playerMode:String='';

		// startPlay
		public var startPlay:Boolean=false;
		// startSeek
		public var startSeek:Number=0;


	}
}

