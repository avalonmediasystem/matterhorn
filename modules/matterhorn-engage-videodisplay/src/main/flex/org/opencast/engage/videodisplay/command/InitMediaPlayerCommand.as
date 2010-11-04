package org.opencast.engage.videodisplay.command
{
    import com.adobe.cairngorm.commands.Command;
    import com.adobe.cairngorm.control.CairngormEvent;
    
    import org.opencast.engage.videodisplay.model.VideodisplayModel;
    import org.opencast.engage.videodisplay.util.*;
    import org.osmf.containers.MediaContainer;
    import mx.controls.Alert;
    

    public class InitMediaPlayerCommand implements Command
    {
    	[Bindable]
        public var model:VideodisplayModel = VideodisplayModel.getInstance();
    	
        public function InitMediaPlayerCommand()
        {
        	
        	//model.parallelMedia = new ParallelMedia();
        	
        }

        public function execute( event:CairngormEvent ):void
        {
        	
            // TODO Auto-generated add your code here
            model.parallelMedia = new ParallelMedia();
          
            
        }
    }
}
