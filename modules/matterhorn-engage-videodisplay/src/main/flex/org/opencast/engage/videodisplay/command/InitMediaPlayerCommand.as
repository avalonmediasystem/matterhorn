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
        public var model:VideodisplayModel = VideodisplayModel.getInstance();

        public function execute( event:CairngormEvent ):void
        {
            var initMediaPlayerEvent:InitMediaPlayerEvent = InitMediaPlayerEvent( event );
            model.parallelMedia = new ParallelMedia( initMediaPlayerEvent._mediaURLOne, initMediaPlayerEvent._mediaURLTwo );
        }
    }
}

