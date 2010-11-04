/*

 CalendarWriterImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 19, 2008

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
import ch.ethz.replay.ui.scheduler.Attachment;
import ch.ethz.replay.ui.scheduler.DeviceType;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.Recording;
import static ch.ethz.replay.ui.common.web.Header.RES_LAST_MODIFIED;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.AltRep;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.FmtType;
import net.fortuna.ical4j.model.property.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;

/**
 * This class helps in creating an iCalendar conform representation of a list of
 * {@link ch.ethz.replay.ui.scheduler.Recording}s.
 * <p/>
 * <h3>Implementation specialities</h3>
 * Attachments having a type of the format <code>metadata/<var>type</var></code> will be added
 * with a property parameter <code>X-METADATA=<var>type</var></code>.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class CalendarWriterImpl implements CalendarWriter {

    private static final String ICALENDAR_ENCODING = "UTF-8";
    private static final String ICALENDAR_CONTENT_TYPE = MimeTypes.CALENDAR.asString();
    private static Categories CATEGORIES = new Categories("RECORDING");

    private String prodId;
    private String vcardUrl;
    private String attachmentUrl;
    private String bundleSkeletonUrl;
    private boolean validation;

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getProdId() {
        return prodId;
    }

    public String getVcardUrl() {
        return vcardUrl;
    }

    public void setVcardUrl(String vcardUrl) {
        this.vcardUrl = vcardUrl.trim();
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl.trim();
    }

    public String getBundleSkeletonUrl() {
        return bundleSkeletonUrl;
    }

    public void setBundleSkeletonUrl(String bundleSkeletonUrl) {
        this.bundleSkeletonUrl = bundleSkeletonUrl;
    }

    public boolean isValidation() {
        return validation;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Produces a schedule from the given recordings in the
     * <a href="http://tools.ietf.org/html/rfc2445">iCalendar</a> format.
     */
    public boolean write(Collection<Recording> recordings, HttpServletResponse response) throws IOException {
        return write(recordings, null, false, response);
    }

    public boolean write(Collection<Recording> recordings, Date lastModified, boolean plainText, HttpServletResponse response)
            throws IOException {
        // Response header
        response.setCharacterEncoding(ICALENDAR_ENCODING);
        response.setContentType(plainText ? MimeTypes.TEXT.asString() : ICALENDAR_CONTENT_TYPE);
        if (lastModified != null)
            response.setDateHeader(RES_LAST_MODIFIED, lastModified.getTime());

        //
        Calendar cal = new Calendar();
        PropertyList cp = cal.getProperties();
        cp.add(new ProdId(prodId));
        cp.add(Version.VERSION_2_0);
        cp.add(CalScale.GREGORIAN);
        for (Recording rec : recordings) {
            addEvent(cal, rec);
        }
        // validate and serialize
        try {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.setValidating(validation);
            outputter.output(cal, response.getOutputStream());
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    private Calendar addEvent(Calendar cal, Recording rec) {
        VEvent recEvent = new VEvent();
        StringBuilder description = new StringBuilder();
        // Add everything to this property list
        PropertyList ep = recEvent.getProperties();

        // UID is set to the job ID
        ep.add(new Uid(rec.getJobId()));
        // Location
        ep.add(new Location(rec.getLocation().getName()));
        // Start date of recording
        ep.add(new DtStart(createUTCDateTime(rec.getStartDate())));
        // End date of recording
        ep.add(new DtEnd(createUTCDateTime(rec.getEndDate())));
        // Summary
        ep.add(new Summary(rec.getTitle()));
        // Category of this event
        ep.add(CATEGORIES);

        // Devices to use
        ResourceList drl = new ResourceList();
        for (DeviceType dev : rec.getDevices()) {
            drl.add(dev.getName());
        }
        description.append("devices: ").append(drl.toString()).append("\n");
        ep.add(new Resources(drl));

        // Contact persons
        for (Person person : rec.getContactPersons()) {
            ParameterList pl = new ParameterList();
            pl.add(new Cn(createContactInformation(person)));
            pl.add(new AltRep(createVCardUrl(person)));
            String email = person.getPreferredEmailAddress().getAddress();
            if (email == null) throw new RuntimeException("person is required to have an email address");
            try {
                ep.add(new Attendee(pl, "MAILTO:" + email));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        // Attachments
        //
        // Bundle skeleton
        {
            ParameterList pl = new ParameterList();
            pl.add(new FmtType(MimeTypes.ZIP.asString()));
            ep.add(new Attach(pl, createDownloadUrl(bundleSkeletonUrl, rec.getId())));
        }
        // All the rest
        // Leave it out for the moment... all attachments are included in the bundle skeleton
//        for (Attachment attachment : rec.getAttachments()) {
//            ParameterList pl = new ParameterList();
//            MimeType contentType = attachment.getContentType();
//            if (contentType != null) {
//                pl.add(new FmtType(contentType.asString()));
//            }
//            ep.add(new Attach(pl, createDownloadUrl(attachmentUrl, attachment.getId())));
//        }

        // Description
        if (description.length() > 0) {
            ep.add(new Description(description.toString()));
        }

        // Add job to calendar
        cal.getComponents().add(recEvent);
        return cal;
    }

    private String createContactInformation(Person person) {
        StringBuilder b = new StringBuilder(person.getFormattedName());
        return b.toString();
    }

    private URI createVCardUrl(Person person) {
        try {
            return new URI(new MessageFormat(vcardUrl).format(new Object[]{person.getId()}));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a URL where the attachment can be downloaded from.
     */
    private URI createAttachmentUrl(Attachment attachment) {
        List args = new ArrayList();
        args.add(attachment.getId());
        String filename = attachment.getFilename();
        args.add(filename != null ? 0 : 1);
        if (filename != null) args.add(filename);
        try {
            return new URI(new MessageFormat(attachmentUrl).format(args.toArray()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URI createDownloadUrl(String urlPattern, Object... args) {
        try {
            return new URI(new MessageFormat(urlPattern).format(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DateTime createUTCDateTime(java.util.Date date) {
        DateTime dt = new DateTime(date);
        dt.setUtc(true);
        return dt;
    }
}
