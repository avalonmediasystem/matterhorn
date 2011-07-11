package
{
	import flash.system.Capabilities;
	
	import mx.core.FlexGlobals;
	import mx.core.UIComponent;
	
	import org.osmf.containers.MediaContainer;
	import org.osmf.media.DefaultMediaFactory;
	import org.osmf.media.MediaElement;
	import org.osmf.media.MediaPlayer;
	
	
	//Sets the size of the SWF
	
	public class OSMFPlayer extends UIComponent
	{
		import org.osmf.media.URLResource;
		import mx.collections.ArrayCollection;
		import org.osmf.layout.ScaleMode;
		import org.osmf.layout.LayoutMetadata;
		
		
		//URI of the media
		//public static const PROGRESSIVE_PATH:String = 
			//"http://mediapm.edgesuite.net/strobe/content/test/AFaerysTale_sylviaApostol_640_500_short.flv";
		public static const PROGRESSIVE_PATH:String = 
			"http://gruinard.virtuos.uos.de/downloads/b1c94d20-d468-4517-949b-43164d7b00aa/fcda9e3d-5b27-4eb3-ba21-2b084d78e647/iLOVE_Matterhorn.flv";
		public var player:MediaPlayer;
		public var container:MediaContainer;
		public var mediaFactory:DefaultMediaFactory;
		
		//siehe auch: OSMF video scale mode
		private const scaleModes:ArrayCollection = new ArrayCollection
			(      [ {label:"letter box", data: ScaleMode.LETTERBOX}
				, {label:"stretch", data: ScaleMode.STRETCH}
				, {label:"zoom", data: ScaleMode.ZOOM}
				, {label:"none", data: ScaleMode.NONE}
					]
			);
		
		
		public function OSMFPlayer()
		{
			initPlayer();
		}
		
		
		protected function initPlayer():void
		{
			//the pointer to the media
			var resource:URLResource = new URLResource( PROGRESSIVE_PATH );
			
			// Create a mediafactory instance
			mediaFactory = new DefaultMediaFactory();
			
			//creates and sets the MediaElement (generic) with a resource and path
			var element:MediaElement = mediaFactory.createMediaElement( resource );
			
			//the simplified api controller for media
			player = new MediaPlayer( element );
			
			//LayoutMetadata(player.media.getMetadata(LayoutMetadata.LAYOUT_NAMESPACE)).scaleMode = ScaleMode.LETTERBOX;
			
			
			//the container (sprite) for managing display and layout
			container = new MediaContainer();
			container.addMediaElement( element );
			
			//Fit the player size  
			container.width = Capabilities.screenResolutionX;
			//container.height = Capabilities.screenResolutionY - FlexGlobals.topLevelApplication.actionBar.height;
			container.height = Capabilities.screenResolutionY;
			//container.width = 200;
			//container.height = 300;
			
			
			
			//Adds the container to the stage
			this.addChild( container );
		}
		
		
		
	}
}