package org.matterhorn.sidebyside.impl;

import java.awt.Rectangle;

import org.opencastproject.mediapackage.Track;
import org.opencastproject.mediapackage.VideoStream;

import org.matterhorn.sidebyside.api.SideBySideService;

/**
 * Implements a system to create monostream video compositions from two videos in the same mediapackage
 */
public class SideBySideServiceImpl implements SideBySideService {

  @Override
  public Track process(Track presenterTrack, int presenterX, int presenterY,
          Track presentationTrack, int presentationX, int presentationY,
          int layoutWidth, int layoutHeight) {

    return process(presenterTrack, 0, presenterX, presenterY,
            presentationTrack, 0, presentationX, presentationY,
            layoutWidth, layoutHeight);
  }


  @Override
  public Track process(Track presenterTrack, int presenterStreamIndex, int presenterX, int presenterY,
          Track presentationTrack, int presentationStreamIndex, int presentationX, int presentationY,
          int layoutWidth, int layoutHeight) {

    String[] names = {"Presenter", "Presentation"};
    Track[] tracks = {presenterTrack, presentationTrack};
    int[] streamIndex = {presenterStreamIndex, presentationStreamIndex};
    int[] x = {presenterX, presentationX};
    int[] y = {presenterY, presentationY};
    VideoStream[] videoStreams = {null, null};
    Rectangle[] trackRectangles = {null, null};

    // Check tracks have video streams
    if (!presenterTrack.hasVideo())
      throw new IllegalArgumentException("Presenter track has no video stream");

    // Check layout dimensions
    Rectangle layoutRect = new Rectangle(layoutWidth, layoutHeight);

    if (layoutRect.isEmpty())
      throw new IllegalArgumentException(String.format("Invalid layout size: %d:%d", layoutWidth, layoutHeight));

    for (int i = 0; i < 2; i++) {

      // Check video stream
      try {
        videoStreams[i] = tracks[i].getVideoStreams()[streamIndex[i]];
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException(String.format("%s track has not stream %d", names[i], streamIndex[i]));
      }
      
      // Check dimensions
      trackRectangles[i] = new Rectangle(x[i], y[i],
              videoStreams[i].getFrameWidth(), videoStreams[i].getFrameHeight());

      // Is it a real rectangle (no negative values, etc.)?
      if (trackRectangles[i].isEmpty())
        throw new IllegalArgumentException(
                String.format("Invalid %s frame size: %d:%d",
                        names[i].toLowerCase(), trackRectangles[i].width, trackRectangles[i].height));

      // Is it within the specified layout?
      if (!layoutRect.contains(trackRectangles[i]))
        throw new IllegalArgumentException(String.format("Presenter video is out of bounds: (%d - %d, %d - %d)",
                trackRectangles[i].x, trackRectangles[i].x + trackRectangles[i].width, 
                trackRectangles[i].y, trackRectangles[i].y + trackRectangles[i].height));
    }

    return null;

  }


}
