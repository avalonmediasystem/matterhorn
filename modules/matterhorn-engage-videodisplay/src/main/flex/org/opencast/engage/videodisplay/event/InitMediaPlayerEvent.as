package org.opencast.engage.videodisplay.event
{
	import com.adobe.cairngorm.control.CairngormEvent;

	import flash.events.Event;

	import org.opencast.engage.videodisplay.control.VideodisplayController;

	public class InitMediaPlayerEvent extends CairngormEvent
	{
		public static const EVENT_NAME:String="InitMediaPlayerEvent";

		public var _mediaURLOne:String;
		public var _mediaURLTwo:String;

		public function InitMediaPlayerEvent(mediaURLOne:String, mediaURLTwo:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(VideodisplayController.INIT_MEDIAPLAYER, bubbles, cancelable);
			this._mediaURLOne=mediaURLOne;
			this._mediaURLTwo=mediaURLTwo;
		}

		public override function clone():Event
		{
			return new InitMediaPlayerEvent(_mediaURLOne, _mediaURLTwo, bubbles, cancelable);
		}
	}
}
