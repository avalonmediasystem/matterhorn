/*

 CalendarController.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

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

package ch.ethz.replay.ui.scheduler.web.controller;

import static ch.ethz.replay.ui.common.web.Header.REQ_IF_MODIFIED_SINCE;
import ch.ethz.replay.ui.scheduler.*;
import ch.ethz.replay.ui.scheduler.delivery.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This controller is responsible for the delivery of the calendars, the vCards and attachments.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
public class CalendarController extends AbstractDomainController {

    private VCardWriter vcardWriter;
    private CalendarWriter calendarWriter;
    private AttachmentWriter attachmentWriter;
    private BundleSkeletonWriter bundleSkeletonWriter;

    public void setVCardWriter(VCardWriter vcardWriter) {
        this.vcardWriter = vcardWriter;
    }

    public void setCalendarWriter(CalendarWriter calendarWriter) {
        this.calendarWriter = calendarWriter;
    }

    public void setAttachmentWriter(AttachmentWriter attachmentWriter) {
        this.attachmentWriter = attachmentWriter;
    }

    public void setBundleSkeletonWriter(BundleSkeletonWriter bundleSkeletonWriter) {
        this.bundleSkeletonWriter = bundleSkeletonWriter;
    }

    //

    @InitBinder
    public void initBinder(WebDataBinder binder, final Locale locale) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyyMMddHHmmss"), false));
        //binder.registerCustomEditor(Integer.class, "interval", new DurationEditor(locale));
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Delivers the specified vcard in plain text.
     */
    @RequestMapping
    public void vcard(@RequestParam(value = "id", required = true) Long id,
                      HttpServletResponse response) throws IOException {
        Person person = schedule.getPerson(id);
        if (person != null) {
            vcardWriter.write(person, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "vcard " + id + " not found");
        }
    }

    /**
     * Delivers an attachment.
     */
    @RequestMapping
    public void attachment(@RequestParam(value = "id") Long id,
                           HttpServletResponse response) throws IOException {
        Attachment attachment = schedule.getAttachment(id);
        if (attachment != null) {
            // todo remote attachment support
            attachmentWriter.write(attachment, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "attachment " + id + " not found");
        }
    }

    @RequestMapping(value = "bundleskeleton", params = "id")
    public void bundleSkeleton(@RequestParam("id") Long recordingId, HttpServletResponse response)
            throws IOException {
        Recording recording = schedule.getRecording(recordingId);
        bundleSkeletonWriter.write(recording, response);
    }

    @RequestMapping(value = "bundleskeleton", params = "jobid")
    public void bundleSkeletonByJobId(@RequestParam("jobid") String jobId, HttpServletResponse response)
            throws IOException {
        RecordingFilter filter = new RecordingFilter();
        filter.setJobId(jobId);
        List<Recording> recordings = schedule.findRecordings(filter);
        switch (recordings.size()) {
            case 0:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Bundle skeleton not found for job " + jobId);
                break;
            case 1:
                bundleSkeletonWriter.write(recordings.get(0), response);
                break;
            default:
                throw new IllegalArgumentException("Multiple recordings match for job id " + jobId);
        }
    }

    /**
     * Delivers the "production calender" which contains all jobs from the past and the future.
     */
    @RequestMapping
    public void production(@RequestParam(value = "plain", required = false) Boolean plain,
                           HttpServletResponse response)
            throws IOException {
        calendarWriter.write(schedule.findAllRecordings(), null, Boolean.TRUE.equals(plain), response);
    }

    /**
     * Deliver the "recorder calender" which is a subset of the "production calender" containing
     * only present and future jobs. Present jobs are defined as being
     * {@link ch.ethz.replay.ui.scheduler.Recording#getStartDate() started} but not finished.
     *
     * @param name the name of the location
     */
    @RequestMapping
    public void recorder(@RequestParam(value = "id") String name,
                         @RequestParam(value = "modified-since", required = false) Date modifiedSince,
                         @RequestParam(value = "plain", required = false) Boolean plain,
                         HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        long modifiedHeader = request.getDateHeader(REQ_IF_MODIFIED_SINCE);
        if (modifiedHeader >= 0) {
            modifiedSince = new Date(modifiedHeader);
        }
        Location loc = schedule.findLocationByName(name);
        if (loc == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Location " + name + " not found");
            return;
        }
        Date lastModified = schedule.getLastModificationDateOfSchedule(loc);
        if (modifiedSince != null) {
            if (lastModified != null && !modifiedSince.before(lastModified)) {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED, "no modified recordings");
                return;
            }
        }
        List<Recording> recordings = schedule.findRecordings(
                new RecordingFilter(null, loc.getId(), null, null, new Date(), null, null, null, null));
        calendarWriter.write(recordings, lastModified, Boolean.TRUE.equals(plain), response);
    }
}
