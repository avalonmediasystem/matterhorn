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

import org.opencastproject.util.MimeTypes;

import ch.ethz.replay.ui.scheduler.DeviceType;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.Recording;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ResourceList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.AltRep;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.FmtType;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Resources;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

/**
 * This class helps in creating an iCalendar conform representation of a list of
 * {@link ch.ethz.replay.ui.scheduler.Recording}s.
 * <p/>
 * <h3>Implementation specialities</h3>
 * Attachments having a type of the format <code>metadata/<var>type</var></code> will be added with a property parameter
 * <code>X-METADATA=<var>type</var></code>.
 * 
 * 
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
   * Produces a schedule from the given recordings in the <a href="http://tools.ietf.org/html/rfc2445">iCalendar</a>
   * format.
   */
  public boolean write(Collection<Recording> recordings, HttpServletResponse response) throws IOException {
    return write(recordings, null, false, response);
  }

  public boolean write(Collection<Recording> recordings, Date lastModified, boolean plainText,
          HttpServletResponse response) throws IOException {
    // Response header
    response.setCharacterEncoding(ICALENDAR_ENCODING);
    response.setContentType(plainText ? MimeTypes.TEXT.asString() : ICALENDAR_CONTENT_TYPE);
    if (lastModified != null)
      response.setDateHeader("Last-Modified", lastModified.getTime());

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
    ep.add(new Uid(rec.getRecordingId()));
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
      pl.add(new Cn(person.getFormattedName()));
      pl.add(new AltRep(createVCardUrl(person)));
      String email = person.getEmailAddress();
      if (email == null)
        throw new RuntimeException("person is required to have an email address");
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
      ep.add(new Attach(pl, createDownloadUrl(bundleSkeletonUrl, rec.getRecordingId())));
    }
    // All the rest
    // Leave it out for the moment... all attachments are included in the bundle skeleton
    // for (Attachment attachment : rec.getAttachments()) {
    // ParameterList pl = new ParameterList();
    // MimeType contentType = attachment.getContentType();
    // if (contentType != null) {
    // pl.add(new FmtType(contentType.asString()));
    // }
    // ep.add(new Attach(pl, createDownloadUrl(attachmentUrl, attachment.getId())));
    // }

    // Description
    if (description.length() > 0) {
      ep.add(new Description(description.toString()));
    }

    // Add job to calendar
    cal.getComponents().add(recEvent);
    return cal;
  }

  private URI createVCardUrl(Person person) {
    try {
      return new URI(new MessageFormat(vcardUrl).format(new Object[] { person.getId() }));
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
