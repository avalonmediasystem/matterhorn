/*
 
 BundleSkeletonWriterImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Dec 08, 2008

 Copyright (c) 2007 ETH Zurich, Switzerland

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package ch.ethz.replay.ui.scheduler.delivery;

import ch.ethz.replay.core.api.common.MimeTypes;
import ch.ethz.replay.core.api.common.bundle.Bundle;
import ch.ethz.replay.core.api.common.bundle.BundleReference;
import ch.ethz.replay.core.api.common.bundle.DublinCoreCatalog;
import ch.ethz.replay.core.api.exception.BundleException;
import ch.ethz.replay.core.api.exception.UnsupportedBundleElementException;
import ch.ethz.replay.core.common.bundle.BundleReferenceImpl;
import ch.ethz.replay.core.common.bundle.ZipPackager;
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;
import ch.ethz.replay.core.common.id.CommentedUUIDIdBuilderImpl;
import ch.ethz.replay.core.common.util.StringSupport;
import ch.ethz.replay.ui.common.util.CollectionUtils;
import ch.ethz.replay.ui.common.util.ReplayBuilder;
import ch.ethz.replay.ui.common.util.RethrowException;
import static ch.ethz.replay.ui.common.web.Header.RES_CONTENT_DISPOSITION;

import ch.ethz.replay.ui.scheduler.Location;
import ch.ethz.replay.ui.scheduler.Recording;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class BundleSkeletonWriterImpl implements BundleSkeletonWriter {

    public static final int UUID_COMMENT_MAX_LENGTH = 40;
    public static final int UUID_COMMENT_MAX_TITLE_LENGTH = 15;

    public boolean write(Recording recording, HttpServletResponse response) throws IOException {
        Bundle bundle = ReplayBuilder.createBundle(recording.getBundleId(),
                createSpeakingBundleDirName(recording));
        try {
            // Response header
            response.setContentType(MimeTypes.ZIP.asString());
            response.setHeader(RES_CONTENT_DISPOSITION, "attachment; filename=" +
                    recording.getJobId() + "." + MimeTypes.ZIP.getSuffix());
            // Metadata
            DublinCoreCatalog dc = recording.getDublinCore();
            if (dc == null)
                throw new RuntimeException("Recording with job ID " + recording.getJobId() + " does not have an " +
                        "associated (episode) Dublin Core");
            dc.save();
            bundle.add(dc);
            // Series metadata
            if (recording.isPartOfSeries()) {
                DublinCoreCatalog sdc = recording.getSeries().getDublinCore();
                if (sdc != null) {
                    String seriesID = recording.getSeries().getSeriesId();
                    if (seriesID == null) {
                        throw new RuntimeException("RecordingSeries does not have a series ID " +
                                "which is necessary for the Dublin Core bundle reference");
                    }
                    sdc.referTo(new BundleReferenceImpl(BundleReference.TYPE_SERIES, seriesID));
                    sdc.save();
                    bundle.add(sdc);
                }
            }
            bundle.save();
            bundle.pack(new ZipPackager(), response.getOutputStream());
            return true;
        } catch (UnsupportedBundleElementException e) {
            throw new RethrowException(e);
        } catch (TransformerException e) {
            throw new RethrowException(e);
        } catch (ParserConfigurationException e) {
            throw new RethrowException(e);
        } catch (BundleException e) {
            throw new RethrowException(e);
        } finally {
            bundle.delete();
        }
    }

    /**
     * Create a bundle dir name from the creator, the room and the job ID.
     */
    private String createSpeakingBundleDirName(Recording recording) {
        // If no ETHZ-DublinCore available just return the recording ID
        ETHZDublinCore dc = null;
        try {
            dc = (ETHZDublinCore) recording.getDublinCore();
            if (dc == null)
                return recording.getJobId();
        } catch (ClassCastException e) {
            return recording.getJobId();
        }

        StringBuilder comment = new StringBuilder();
        // Date
        append(comment, String.format("%1$tY%1$tm%1$td-%1$tH%1$tM", recording.getStartDate()), -1);
        // Location
        Location location = recording.getLocation();
        if (location != null)
            append(comment, location.getName(), -1);
        // Creator
        append(comment, CollectionUtils.first(dc.getCreators()), -1);
//        // Title
//        append(comment, dc.getOriginalTitle(), UUID_COMMENT_MAX_TITLE_LENGTH);

        // Limit
        if (comment.length() > UUID_COMMENT_MAX_LENGTH)
            comment.delete(UUID_COMMENT_MAX_LENGTH, comment.length());
        // Build ID
        if (comment.length() > 0)
            return new CommentedUUIDIdBuilderImpl().createWithComment(recording.getJobId(), comment.toString());
        else
            return recording.getJobId();
    }

    private void append(StringBuilder b, String s, int limit) {
        if (StringSupport.notEmpty(s)) {
            if (b.length() > 0)
                b.append("_");
            String limited = s;
            if (limit > 0)
                limited = s.substring(0, Math.min(s.length(), limit));
            b.append(limited);
        }
    }
}
