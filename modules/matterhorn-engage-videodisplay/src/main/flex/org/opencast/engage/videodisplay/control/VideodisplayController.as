package org.opencast.engage.videodisplay.control
{
	import com.adobe.cairngorm.control.FrontController;

	import org.opencast.engage.videodisplay.command.InitMediaPlayerCommand;

	public class VideodisplayController extends FrontController
	{
		private static var UUID:String="649f31a1-192d-4015-bd6d-8c632687cc71";

		public static const INIT_MEDIAPLAYER:String="initplayer";


		public function VideodisplayController()
		{
			initialiseCommands();
		}

		private function initialiseCommands():void
		{
			addCommand(VideodisplayController.INIT_MEDIAPLAYER, InitMediaPlayerCommand);
		}
	}
}