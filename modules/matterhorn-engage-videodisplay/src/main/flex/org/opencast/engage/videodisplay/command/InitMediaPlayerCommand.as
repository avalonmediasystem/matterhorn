package org.opencast.engage.videodisplay.command
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	import org.opencast.engage.videodisplay.event.InitMediaPlayerEvent;
	import org.opencast.engage.videodisplay.model.VideodisplayModel;
	import org.opencast.engage.videodisplay.util.*;

	public class InitMediaPlayerCommand implements Command
	{

		public function InitMediaPlayerCommand()
		{

		}

		[Bindable]
		public var model:VideodisplayModel=VideodisplayModel.getInstance();

		public function execute(event:CairngormEvent):void
		{
			var initMediaPlayerEvent:InitMediaPlayerEvent=InitMediaPlayerEvent(event);
			//model.parallelMedia=new ParallelMedia(initMediaPlayerEvent._mediaURLOne, initMediaPlayerEvent._mediaURLTwo);
			//pseudostreaming
			//model.parallelMedia = new ParallelMedia( "http://131.173.22.24/static/dab950e1-c64b-4907-b9e3-c61bf8ec6110/track-9/dozent.mp4", "http://131.173.22.24/static/dab950e1-c64b-4907-b9e3-c61bf8ec6110/track-10/vga.mp4" );
			//streamin 0.6.3
			//model.parallelMedia = new ParallelMedia( "rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/0a78520a-3c23-4379-bcc7-eb7c0a803070/track-6/dozent.flv","rtmp://freecom.serv.uni-osnabrueck.de/oflaDemo/matterhorn/0a78520a-3c23-4379-bcc7-eb7c0a803070/track-7/vga.flv");
			model.parallelMedia = new ParallelMedia( "rtmp://gruinard.virtuos.uos.de:1935/vod/mp4:sample.mp4","rtmp://gruinard.virtuos.uos.de:1935/vod/mp4:sample.mp4");

		}
	}
}

