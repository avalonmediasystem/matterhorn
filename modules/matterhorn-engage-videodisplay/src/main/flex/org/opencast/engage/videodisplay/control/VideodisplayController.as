package org.opencast.engage.videodisplay.control
{
    import com.adobe.cairngorm.control.FrontController;
    import org.opencast.engage.videodisplay.command.InitMediaPlayerCommand;
    import org.opencast.engage.videodisplay.command.VideoplayerInteractionCommand;
    import org.opencast.engage.videodisplay.event.VideoplayerInteractionEvent;

    public class VideodisplayController extends FrontController
    {
        public static var INIT_MEDIAPLAYER:String = "initplayer";
        private static var UUID:String = "649f31a1-192d-4015-bd6d-8c632687cc71";

        public function VideodisplayController()
        {
            initialiseCommands();
        }

        private function initialiseCommands():void
        {
            addCommand( VideodisplayController.INIT_MEDIAPLAYER, InitMediaPlayerCommand );
            addCommand( VideoplayerInteractionEvent.EVENT_NAME, VideoplayerInteractionCommand );
        }
    }
}

