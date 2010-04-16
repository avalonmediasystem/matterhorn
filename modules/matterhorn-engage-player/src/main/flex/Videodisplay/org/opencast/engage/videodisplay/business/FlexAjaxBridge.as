package org.opencast.engage.videodisplay.business
{
    import bridge.ExternalFunction;
    
    import flash.events.KeyboardEvent;
    import flash.external.ExternalInterface;
    
    import org.opencast.engage.videodisplay.control.event.ClosedCaptionsEvent;
    import org.opencast.engage.videodisplay.control.event.InitMediaPlayerEvent;
    import org.opencast.engage.videodisplay.control.event.LoadDFXPXMLEvent;
    import org.opencast.engage.videodisplay.control.event.SetVolumeEvent;
    import org.opencast.engage.videodisplay.control.event.VideoControlEvent;
    import org.opencast.engage.videodisplay.model.VideodisplayModel;
    import org.osmf.layout.HorizontalAlign;
    import org.osmf.layout.LayoutMetadata;
    import org.osmf.layout.LayoutMode;
    import org.osmf.layout.ScaleMode;
    import org.osmf.layout.VerticalAlign;
    import org.swizframework.Swiz;

    public class FlexAjaxBridge
    {
        [Autowire]
        [Bindable]
        public var model:VideodisplayModel;

        /** Constructor */
        public function FlexAjaxBridge()
        {
            Swiz.autowire( this );
        }

        /**
         * play
         *
         * When the learnder click on the play button
         * The return value is available in the calling javascript
         * */
        public function play():String
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.PLAY ) );
            return model.currentPlayerState;
        }

        /**
         * pause
         *
         * When the learnder click on the pause button
         * The return value is available in the calling javascript
         * */
        public function pause():String
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.PAUSE ) );
            return model.currentPlayerState;
        }

        /**
         * stop
         *
         * When the learnder click on the stop button
         * */
        public function stop():void
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.STOP ) );
        }

        /**
         * skipBackward
         *
         * When the learnder click on the skip backward button
         * */
        public function skipBackward():void
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SKIPBACKWARD ) );
        }

        /**
         * rewind
         *
         * When the learnder click on the rewind button
         * */
        public function rewind():void
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.REWIND ) );
        }

        /**
         * fastForward
         *
         * When the learnder click on the fast forward button
         * */
        public function fastForward():void
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.FASTFORWARD ) );
        }

        /**
         * skipForward
         *
         * When the learnder click on the skip forward button
         * */
        public function skipForward():void
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SKIPFORWARD ) );
        }

        /**
         * seek
         *
         * When the learner seek the video
         * */
        public function seek( time:Number ):Number
        {
            model.mediaPlayer.seek( time );
            return time;
        }
        
        /**
         * mute
         *
         * Wehn the learner mutes the video
         * The return value is available in the calling javascript
         * */
        public function mute():Number
        {
            Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.MUTE ) );
            return model.playerVolume;
        }
        
        /**
         * setVolumeSlider
         *
         * Set the volume slider
         * */
        public function setVolumePlayer( newVolume:Number ):void
        {
            Swiz.dispatchEvent( new SetVolumeEvent( newVolume ));
        }
        
        /**
         * setClosedCaptions
         *
         * To see the captions
         * */
        public function closedCaptions():void
        {
            Swiz.dispatchEvent( new ClosedCaptionsEvent() );
        }

        /**
         * setCaptionsURL
         *
         * Set captions URL and load the file.
         */
        public function setCaptionsURL( captionsURL:String ):void
        {
            if ( captionsURL != model.captionsURL )
            {
                model.captionsURL = captionsURL;
                Swiz.dispatchEvent( new LoadDFXPXMLEvent( model.captionsURL ) );
            }
        }
        
        /**
         * setMediaURL
         *
         * Set media URL and init the player.
         */
        public function setMediaURL( mediaURLOne:String, mediaURLTwo:String ):void
        {
            Swiz.dispatchEvent( new InitMediaPlayerEvent( mediaURLOne, mediaURLTwo ) );
        }
        
        /**
         * videoSizeControl
         *
         * When the learner press the video size control button.
         */
        public function videoSizeControl( sizeLeft:Number, sizeRight:Number ):void
        {
			if( sizeLeft == 100 )
            {
                model.layoutMetadataOne.percentWidth = sizeLeft;
                model.layoutMetadataTwo.percentWidth = sizeRight;
                model.layoutMetadataOne.horizontalAlign = HorizontalAlign.CENTER;
                model.layoutMetadataParallelElement.layoutMode = LayoutMode.NONE;
            }
            else if(sizeRight == 100)
            {
                model.layoutMetadataOne.percentWidth = sizeLeft;
                model.layoutMetadataTwo.percentWidth = sizeRight;
                model.layoutMetadataTwo.horizontalAlign = HorizontalAlign.CENTER;
                model.layoutMetadataParallelElement.layoutMode = LayoutMode.NONE;
            }
            else 
            {               
            	model.layoutMetadataParallelElement.layoutMode = LayoutMode.HORIZONTAL;
            	model.layoutMetadataOne.percentWidth = sizeLeft;
                model.layoutMetadataTwo.percentWidth = sizeRight;
                model.layoutMetadataOne.horizontalAlign = HorizontalAlign.CENTER;
                model.layoutMetadataTwo.horizontalAlign = HorizontalAlign.CENTER;
            }
		}
        
         /**
         * getViewState
         *
         * The return value is available in the calling javascript.
         */
        public function getViewState():String
        {
        	return model.mediaState;
        }
        
        /**
         * reportKeyDown
         *
         *
         */
        public function reportKeyUp( event:KeyboardEvent ):void
        {
            if( event.altKey && event.ctrlKey )
            {
                passCharCode( event.keyCode );
            }
        }

        /**
         * passCharCode
         *
         * When the learner press any key for the mediaplayer
         */
        public function passCharCode( charCode:int ):void
        {
            // Play or pause the video
            if( charCode == 80 || charCode == 112 ) // P or p
            {
                if( model.mediaPlayer.playing )
                {
                    Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.PAUSE ) );
                }
                else
                {
                    Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.PLAY ) );
                }
            }

            // Mute the video
            if( charCode == 83 || charCode == 115 ) // S or s
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.STOP ) );
            }

            // Mute the video
            if( charCode == 77 || charCode == 109 ) // M or m
            {
               ExternalInterface.call( ExternalFunction.MUTE, '' )
            }

            // Volume up
            if ( charCode == 85 || charCode == 117 ) // U or u
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.VOLUMEUP ) );
            }

            // Volume down
            if( charCode == 68 || charCode == 100 ) // D or d
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.VOLUMEDOWN ) );
            }

            // Seek 0
            if( charCode == 48 ) // 0
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKZERO ) );
            }

            // Seek 1
            if( charCode == 49 ) // 1
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKONE ) );
            }

            // Seek 2
            if( charCode == 50 ) // 2
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKTWO ) );
            }

            // Seek 3
            if( charCode == 51 ) // 3
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKTHREE ) );
            }

            // Seek 4
            if( charCode == 52 ) // 4
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKFOUR ) );
            }

            // Seek 5
            if( charCode == 53 ) // 5
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKFIVE ) );
            }

            // Seek 6
            if( charCode == 54 ) // 6
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKSIX ) );
            }

            // Seek 7
            if( charCode == 55 ) // 7
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKSEVEN ) );
            }

            // Seek 8
            if( charCode == 56 ) // 8
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKEIGHT ) );
            }

            // Seek 9
            if( charCode == 57 ) // 9
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SEEKNINE ) );
            }

            // Closed Caption
            if( charCode == 67 || charCode == 99 ) // C or c
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.CLOSEDCAPTIONS ) );
            }

            // rewind
            if( charCode == 82 || charCode == 114 ) // R or r
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.REWIND ) );
            }

            // Fast forward
            if( charCode == 70 || charCode == 102 ) // F or f
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.FASTFORWARD ) );
            }

            // time
            if( charCode == 84 || charCode == 116 ) // T or t
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.HEARTIMEINFO ) );
            }

            // Information
            if( charCode == 73 || charCode == 105 ) // I or i
            {
                Swiz.dispatchEvent( new VideoControlEvent( VideoControlEvent.SHORTCUTS ) );
            }
        }
        
        /**
         * onBridgeReady
         *
         * 
         */
        public function onBridgeReady():void
        {
            ExternalInterface.call( ExternalFunction.ONPLAYERREADY );
        }
        
     }
}