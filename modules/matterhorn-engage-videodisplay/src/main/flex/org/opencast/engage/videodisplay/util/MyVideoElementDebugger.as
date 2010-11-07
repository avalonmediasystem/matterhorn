package org.opencast.engage.videodisplay.util
{
	import flash.events.NetStatusEvent;
	import flash.events.StatusEvent;
	import flash.net.NetStream;
	import org.osmf.media.MediaResourceBase;
	import org.osmf.net.NetLoadedContext;
	import org.osmf.net.NetLoader;
	import org.osmf.traits.LoadTrait;
	import org.osmf.traits.MediaTraitType;
	import org.osmf.video.VideoElement;

	public class MyVideoElementDebugger extends VideoElement
	{

		private var stream:NetStream;

		public function MyVideoElementDebugger(loader:NetLoader, resource:MediaResourceBase=null)
		{
			super(loader, resource);
		}

		override protected function processReadyState():void
		{
			super.processReadyState();
			var loadTrait:LoadTrait=getTrait(MediaTraitType.LOAD) as LoadTrait;
			var context:NetLoadedContext=NetLoadedContext(loadTrait.loadedContext);
			stream=context.stream;
			stream.addEventListener(NetStatusEvent.NET_STATUS, onNetStatusEvent2);
		}

		private function onNetStatusEvent2(evt:NetStatusEvent):void
		{
			trace(evt.info.code)
		}
	}

}