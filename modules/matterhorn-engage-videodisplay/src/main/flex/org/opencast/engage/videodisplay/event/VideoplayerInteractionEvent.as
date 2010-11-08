package org.opencast.engage.videodisplay.event
{
    import com.adobe.cairngorm.control.CairngormEvent;

    import flash.events.Event;

    public class VideoplayerInteractionEvent extends CairngormEvent 
    {
        public static const EVENT_NAME : String = "VideoplayerInteractionEvent";
        public var INTERACTION_TYPE : String;
        public var INTERACTION_VALUE : int;

        public function VideoplayerInteractionEvent( interactiontype: String, value:int, bubbles : Boolean = false, cancelable : Boolean = false )
        {
            super( EVENT_NAME, bubbles, cancelable );
            this.INTERACTION_TYPE = interactiontype;
            this.INTERACTION_VALUE = value;
        }

        public override function clone() : Event
        {
            return new VideoplayerInteractionEvent( INTERACTION_TYPE, INTERACTION_VALUE, bubbles, cancelable );
        }
    }
}


