/**
 *  Copyright 2009-2011 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */

var Videodisplay = Videodisplay || {};

/**
 * @namespace the global namespace Videodisplay
 */
Videodisplay = (function ()
{
    var b_Videodisplay_root,
        initD = false;
    var volSliderArgs,
        volPlayerArgs,
        closedCap = false,
        covOne,
        covTwo,
        strOne,
        strTwo,
        mimOne,
        mimTwo,
        plStyle,
        slideLen,
        capURL,
        vidSizeContLeft,
        vidSizeContRight,
        widthMediaOne,
        heightMediaOne,
        widthMediaTwo,
        heightMediaTwo,
        contLeft;
        
    /**
     * @memberOf Videodisplay
     * @description Initialize the "root" object. This represents the actual "Videodisplay mxml" flex application
     */
    function VideodisplayReady()
    {
        b_Videodisplay_root = FABridge['b_Videodisplay'].root().getFlexAjaxBridge();
        b_Videodisplay_root.onBridgeReady();
        
        initD = true;
        init();
    }

    /**
     * @memberOf Videodisplay
     * @description Returns a Flag that displays if Videdisplay has been initialized
     * @return true if Videodisplay has been initialized, false else
     */
    function initialized()
    {
        return initD;
    }

    /**
     * @memberOf Videodisplay
     * @description Initializez everything
     */
    function init()
    {
        if (volSliderArgs)
        {
            setVolumeSlider(volSliderArgs);
        }
        if (volPlayerArgs)
        {
            setVolumePlayer(volPlayerArgs);
        }
        if (closedCaptions)
        {
            closedCaptions();
        }
        if (covOne)
        {
            setMediaURL(covOne, covTwo, strOne, strTwo, mimOne, mimTwo, plStyle, slideLen);
        }
        if (capURL)
        {
            setCaptionsURL(capURL);
        }
        if (vidSizeContLeft)
        {
            VideoSizeControl(vidSizeContLeft, vidSizeContRight);
        }
        if (heightMediaOne)
        {
            setMediaResolution(widthMediaOne, heightMediaOne, widthMediaTwo, heightMediaTwo, contLeft);
        }
    }

    /**
     * @memberOf Videodisplay
     * @description play
     * @return false if something went wrong
     */
    function play()
    {
        try
        {
            if (initialized())
            {
                var v = b_Videodisplay_root.play();
            }
            return v;
        }
        catch (err)
        {
        }
        return false;
    };

    /**
     * @memberOf Videodisplay
     * @description stop
     */
    function stop()
    {
        if (initialized())
        {
            b_Videodisplay_root.stop();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description pause
     */
    function pause()
    {
        if (initialized())
        {
            return b_Videodisplay_root.pause();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description skipBackward
     */
    function skipBackward()
    {
        if (initialized())
        {
            b_Videodisplay_root.skipBackward();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description rewind
     */
    function rewind()
    {
        if (initialized())
        {
            b_Videodisplay_root.rewind();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description stopRewind
     */
    function stopRewind()
    {
        if (initialized())
        {
            b_Videodisplay_root.stopRewind();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description fastForward
     */
    function fastForward()
    {
        if (initialized())
        {
            b_Videodisplay_root.fastForward();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description stopFastForward
     */
    function stopFastForward()
    {
        if (initialized())
        {
            b_Videodisplay_root.stopFastForward();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description skipForward
     */
    function skipForward()
    {
        if (initialized())
        {
            b_Videodisplay_root.skipForward();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description passCharCode
     * @param argInt
     */
    function passCharCode(argInt)
    {
        if (initialized())
        {
            b_Videodisplay_root.passCharCode(argInt);
        }
    };
    
    /**
     * @memberOf Videodisplay
     * @description seek
     *              Note: pause()/resume() bug red5
     * @param argNumber
     * @return false if seek failed
     */
    function seek(argNumber)
    {
        if (initialized())
        {
            var progress = Opencast.engage.getLoadProgress();
            if (progress === -1)
            {
                // red5 pause/resume bug
                if (Opencast.Player.isPlaying())
                {
                    try
                    {
                        // if playing seek ok
                        var v = b_Videodisplay_root.seek(argNumber);
                        return v;
                    }
                    catch (err)
                    {
                    }
                }
                else
                {
                    try
                    {
                        // player in pause mode
                        b_Videodisplay_root.play();
                        // if playing seek ok
                        var v = b_Videodisplay_root.seek(argNumber);
                        return v;
                    }
                    catch (err)
                    {
                    }
                }
            }
            else
            {
                // progressive download
                var seekValue = Math.min(argNumber, progress);
                try
                {
                    // if playing seek ok
                    var v = b_Videodisplay_root.seek(seekValue);
                    return v;
                }
                catch (err)
                {
                }
            }
        }
        return false;
    };

    /**
     * @memberOf Videodisplay
     * @description mute
     * @return mute
     */
    function mute()
    {
        if (initialized())
        {
            return b_Videodisplay_root.mute();
        }
    };

    /**
     * @memberOf Videodisplay
     * @description setVolumeSlider
     * @param argNumber
     */
    function setVolumeSlider(argNumber)
    {
        if (initialized())
        {
            b_Videodisplay_root.setVolumeSlider(argNumber);
        }
        else
        {
            volSliderArgs = argNumber;
        }
    };

    /**
     * @memberOf Videodisplay
     * @description setVolumePlayer
     * @param argNumber
     */
    function setVolumePlayer(argNumber)
    {
        if (initialized())
        {
            b_Videodisplay_root.setVolumePlayer(argNumber);
        }
        else
        {
            volPlayerArgs = argNumber;
        }
    };

    /**
     * @memberOf Videodisplay
     * @description closedCaptions
     * @param argNumber
     */
    function closedCaptions()
    {
        if (initialized())
        {
            b_Videodisplay_root.closedCaptions();
        }
        else
        {
            closedCap = true;
        }
    };

    /**
     * @memberOf Videodisplay
     * @description setMediaURL
     * @param argCoverOne
     * @param argCoverTwo
     * @param argStringOne
     * @param argStringTwo
     * @param argMimetypeOne
     * @param argMimetypeTwo
     * @param argPlayerstyle
     * @param slideLength
     */
    function setMediaURL(argCoverOne, argCoverTwo, argStringOne, argStringTwo, argMimetypeOne, argMimetypeTwo, argPlayerstyle, slideLength)
    {
        if (initialized())
        {
            b_Videodisplay_root.setMediaURL(argCoverOne, argCoverTwo, argStringOne, argStringTwo, argMimetypeOne, argMimetypeTwo, argPlayerstyle, slideLength);
        }
        else
        {
            covOne = argCoverOne;
            covTwo = argCoverTwo;
            strOne = argStringOne;
            strTwo = argStringTwo;
            mimOne = argMimetypeOne;
            mimTwo = argMimetypeTwo;
            plStyle = argPlayerstyle;
            slideLen = slideLength;
        }
    };

    /**
     * @memberOf Videodisplay
     * @description setCaptionsURL
     * @param argString
     */
    function setCaptionsURL(argString)
    {
        if (initialized())
        {
            b_Videodisplay_root.setCaptionsURL(argString);
        }
        else
        {
            capURL = argString;
        }
    };

    /**
     * @memberOf Videodisplay
     * @description videoSizeControl
     * @param argSizeLeft
     * @param argSizeRight
     */
    function videoSizeControl(argSizeLeft, argSizeRight)
    {
        if (initialized())
        {
            b_Videodisplay_root.videoSizeControl(argSizeLeft, argSizeRight);
        }
        else
        {
            vidSizeContLeft = argSizeLeft;
            vidSizeContRight = argSizeRight;
        }
    };

    /**
     * @memberOf Videodisplay
     * @description getViewState
     */
    function getViewState()
    {
        if (initialized())
        {
            return b_Videodisplay_root.getViewState();
        }
        return {
        };
    };

    /**
     * @memberOf Videodisplay
     * @description getViewState
     * @param argWidthMediaOne
     * @param argHeightMediaOne
     * @param argWidthMediaTwo
     * @param argHeightMediaTwo
     * @param argMultiMediaContainerLeft
     */
    function setMediaResolution(argWidthMediaOne, argHeightMediaOne, argWidthMediaTwo, argHeightMediaTwo, argMultiMediaContainerLeft)
    {
        if (initialized())
        {
            return b_Videodisplay_root.setMediaResolution(argWidthMediaOne, argHeightMediaOne, argWidthMediaTwo, argHeightMediaTwo, argMultiMediaContainerLeft);
        }
        else
        {
            widthMediaOne = argWidthMediaOne;
            heightMediaOne = argHeightMediaOne;
            widthMediaTwo = argWidthMediaTwo;
            heightMediaTwo = argHeightMediaTwo;
            contLeft = argMultiMediaContainerLeft;
        }
    };
    
    return {
        VideodisplayReady: VideodisplayReady,
        play: play,
        stop: stop,
        pause: pause,
        skipBackward: skipBackward,
        rewind: rewind,
        stopRewind: stopRewind,
        fastForward: fastForward,
        stopFastForward: stopFastForward,
        skipForward: skipForward,
        passCharCode: passCharCode,
        seek: seek,
        mute: mute,
        setVolumeSlider: setVolumeSlider,
        setVolumePlayer: setVolumePlayer,
        closedCaptions: closedCaptions,
        setMediaURL: setMediaURL,
        setCaptionsURL: setCaptionsURL,
        videoSizeControl: videoSizeControl,
        getViewState: getViewState,
        setMediaResolution: setMediaResolution
    };
}());

// Listen for the instantiation of the Flex application over the bridge
FABridge.addInitializationCallback("b_Videodisplay", Videodisplay.VideodisplayReady);
