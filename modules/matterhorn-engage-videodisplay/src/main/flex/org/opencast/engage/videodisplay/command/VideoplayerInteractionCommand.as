package org.opencast.engage.videodisplay.command
{
    import com.adobe.cairngorm.commands.Command;
    import com.adobe.cairngorm.control.CairngormEvent;

    import org.opencast.engage.videodisplay.event.VideoplayerInteractionEvent;
    import org.opencast.engage.videodisplay.model.VideodisplayModel;


    public class VideoplayerInteractionCommand implements Command
    {

        [Bindable]
        public var model:VideodisplayModel=VideodisplayModel.getInstance();

        public function VideoplayerInteractionCommand()
        {
        }

        public function execute( event : CairngormEvent ) : void
        {
            var videoplayerInteractionEvent:VideoplayerInteractionEvent=VideoplayerInteractionEvent(event);

            switch(videoplayerInteractionEvent.INTERACTION_TYPE)
            {
                case "PLAY":
                    trace("PLAY");
                    model.player.play();
                    break;
                case "PAUSE":
                    trace("PAUSE");
                    model.player.pause();
                    break;
                case "STOP":
                    trace("STOP");
                    model.player.stop();
                    break;
                case "SEEK":
                    trace("SEEK");
                    model.player.seek(videoplayerInteractionEvent.INTERACTION_VALUE);
                    break;
                case "VOLUME":
                    model.player.volume = 0.1 ;
                    trace("VOLUME: "+model.player.volume);
                    break;
                default:
                    trace("Undefined Interaction Type");
                    break;
            }

        }
    }
}


