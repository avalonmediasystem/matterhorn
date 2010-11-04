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

import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;
import ch.ethz.replay.ui.common.util.ReplayBuilder;
import ch.ethz.replay.ui.common.web.UrlEncoding;
import ch.ethz.replay.ui.common.web.spring.flow.Back;
import ch.ethz.replay.ui.common.web.propertyeditors.RejectingCustomCollectionEditor;
import ch.ethz.replay.ui.common.web.shelf.Shelf;
import ch.ethz.replay.ui.scheduler.*;
import ch.ethz.replay.ui.scheduler.impl.util.RecordingValidator;
import ch.ethz.replay.ui.scheduler.impl.RecordingImpl;
import ch.ethz.replay.ui.scheduler.web.propertyeditors.LocationEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.context.request.WebRequest;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.util.*;

/**
 * Controller to edit recordings.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Controller
@SessionAttributes(RecordingController.S_FORM)
public class RecordingController extends AbstractDomainController {

    private static final Logger Log = Logger.getLogger(RecordingController.class);

    public static final String VIEW = "recording";

    public static final String S_FORM = "recform";

    //

    @Autowired(required = true)
    private RecordingValidator recVal;

    /**
     * The "cf_call" entry point expects an argument of this type.
     */
    public static class Arg {

        private Recording recording;
        private Map<String, Boolean> excludedProperties = new HashMap<String, Boolean>();
        private boolean validate;

        public Arg(Recording recording, boolean validate, String... excludedProperties) {
            this.recording = recording;
            this.validate = validate;
            for (String prop : excludedProperties) this.excludedProperties.put(prop, Boolean.TRUE);
        }

        public Recording getRecording() {
            return recording;
        }

        public Map<String, Boolean> getExcludedProperties() {
            return excludedProperties;
        }

        public boolean validate() {
            return validate;
        }
    }

    public class Form {
        private RecordingSeries series;

        private Map<String, Boolean> excludedProperties;
        private boolean validate;

        private Recording recording;

        public Form(Recording recording, Map<String, Boolean> excludedProperties, boolean validate) {
            this.recording = recording;
            this.excludedProperties = excludedProperties;
            this.validate = validate;
            this.series = recording.getSeries();
        }

        public String getTitle() {
            return recording.getTitle();
        }

        public Location getLocation() {
            return recording.getLocation();
        }

        public void setLocation(Location location) {
            if (location != null)
                recording.setLocation(location);
        }

        public Date getStartDate() {
            return recording.getStartDate();
        }

        public void setStartDate(Date date) {
            if (date != null)
                recording.setStartDate(date);
        }

        public Date getEndDate() {
            return recording.getEndDate();
        }

        public void setEndDate(Date date) {
            if (date != null)
                recording.setEndDate(date);
        }

        public Set<DeviceType> getDevices() {
            return recording.getDevices();
        }

        public void setDevices(Set<DeviceType> devices) {
            recording.setDevices(devices);
        }

        public Set<Person> getContactPersons() {
            return recording.getContactPersons();
        }

        public void setContactPersons(Set<Person> contactPersons) {
            recording.setContactPersons(contactPersons);
        }

        public JobTicketAttachment getJobTicket() {
            return recording.getJobTicket();
        }

        public void setJobTicket(JobTicketAttachment jobTicket) {
            recording.setJobTicket(jobTicket);
        }

        public ETHZDublinCore getDublinCore() {
            return (ETHZDublinCore) recording.getDublinCore();
        }

        public void setDublinCore(ETHZDublinCore dc) {
            recording.setDublinCore(dc);
        }

        public RecordingSeries getSeries() {
            return series;
        }

        public void setSeries(RecordingSeries series) {
            this.series = series;
        }

        public List<RecordingSeries> getAvailableSeries() {
            return schedule.findAllRecordingSeries();
        }

        public List<JobTicketAttachment> getAvailableJobTickets() {
            return schedule.findAttachmentByType(JobTicketAttachment.class);
        }

        public List<DeviceType> getAvailableDeviceTypes() {
            return schedule.getAvailableDeviceTypes();
        }

        public Map<String, Boolean> getExcludedProperties() {
            return excludedProperties;
        }

        public boolean isValidate() {
            return validate;
        }

        Recording getRecording() {
            return recording;
        }
    }

    // --

    @Resource(name = "shelf")
    private Shelf shelf;

    // --

    @RequestMapping
    public ModelAndView cf_redirect() {
        return new ModelAndView(VIEW, "recording", getRedirectArgument(Recording.class));
    }

    @RequestMapping
    public ModelAndView cf_call() {
        Arg arg = getCallArgument(Arg.class);
        return new ModelAndView(VIEW,
                S_FORM, new Form(arg.getRecording(), arg.getExcludedProperties(), arg.validate()));
    }

    @RequestMapping
    public ModelAndView cf_back(@ModelAttribute(S_FORM) Form form) {
        // Put the form back in request scope
        return new ModelAndView(VIEW, S_FORM, form);
    }

    // --

    /**
     * Prepare to edit a recording. If no id is passed, the session is queried for a recording named
     * <code>recording</code>.
     *
     * @param id the recording id or null
     */
    @RequestMapping
    public ModelAndView edit(@RequestParam(value = "id", required = false) Long id, WebRequest request) {
        Recording rec = id != null
                ? schedule.getRecording(id)
                : new RecordingImpl(); // todo constructor is prohibited: Use of command object required
        return new ModelAndView(VIEW, S_FORM, new Form(rec, Collections.EMPTY_MAP, true));
    }

    /**
     * Create a new person ad-hoc.
     */
    @RequestMapping(value = "createperson", method = RequestMethod.POST)
    public String createPerson(@ModelAttribute("recording") Recording recording) {
        return call(EditPersonController.class, null);
    }

    /**
     * Edit the metadata.
     */
    @RequestMapping(value = "editdublincore", method = RequestMethod.POST)
    public String editDublinCore(@ModelAttribute(S_FORM) final Form form) {
        if (form.getDublinCore() == null) {
            ETHZDublinCore dc = (ETHZDublinCore) ReplayBuilder.createDublinCoreCatalog();
            dc.setDCIdentifier(form.getRecording().getBundleId().getFullName());
            form.setDublinCore(dc);
        }
        final ETHZDublinCore dc = form.getDublinCore();
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(new ETHZDublinCoreCommand(dc), "episode", true, false),
                new Back() {
                    public void apply() {
                        // Need to set the Dublin Core again, to make the recording update its title
                        form.getRecording().setDublinCore(dc);
                    }
                }
        );
    }

    @RequestMapping(value = "inspectseries", method = RequestMethod.POST)
    public String inspectSeries(@ModelAttribute(S_FORM) Form form,
                                @RequestParam("id") final Long id) {
        final RecordingSeries series = schedule.getRecordingSeries(id);
        final ETHZDublinCore dc = (ETHZDublinCore) series.getDublinCore();
        return call(DublinCoreNewController.class,
                new DublinCoreNewController.Arg(new ETHZDublinCoreCommand(dc), "series", false, false),
                new Back() {
                    public void apply() {
                        RecordingSeries series = schedule.getRecordingSeries(id);
                        series.setDublinCore(dc);
                        schedule.saveRecordingSeries(series);
                    }
                });
    }

    /**
     * Saves a recording.
     */
    @RequestMapping(method = RequestMethod.POST)
    public String save(@ModelAttribute(S_FORM) Form form,
                       BindingResult result,
                       SessionStatus status) {
        if (form.isValidate()) {
            // Validation
            recVal.validate(form.getRecording(), result);
            if (result.hasErrors()) {
                return VIEW;
            }
        }

        if (isCalled()) {
            // Finish conversation, do not save to DB, do not validate recording
            status.setComplete();
            shelf.clear();
            return back();
        } else {
            // Save recording to db
            return finishConversation(form, status);
        }
    }

    @RequestMapping("cancel")
    public String cancel(SessionStatus status) {
        status.setComplete();
        shelf.clear();
        if (isCalled())
            return back();
        else
            return redirectTo(ScheduleController.class, "list");
    }

    // --------------------------------------------------------------------------------------------

    /**
     * End the conversation, save the recording and go to schedule overview.
     */
    private String finishConversation(Form form, SessionStatus status) {
        // Finish conversation
        status.setComplete();
        shelf.clear();
        // Save to db
        Recording recording = form.getRecording();
        RecordingSeries series = form.getSeries();
        if (series != null) {
            // Wire up the series
            if (recording.isPartOfSeries())
                series.removeRecording(recording);
            series.addRecording(recording);
        } else {
            schedule.removeRecordingFromSeries(recording);
        }
        schedule.schedule(recording);
        return redirectTo(ScheduleController.class, "list?location=" +
                UrlEncoding.encode(recording.getLocation().getName()));
    }

    // --------------------------------------------------------------------------------------------

    @InitBinder
    public void initBinder(WebDataBinder binder, final Locale locale) {
        // Date editor
        binder.registerCustomEditor(Date.class,
                new CustomDateEditor(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale), true));
        // Device set editor
        binder.registerCustomEditor(Set.class, "devices", new RejectingCustomCollectionEditor(Set.class) {
            @Override
            protected Object convertElement(Object element) {
                if (element != null) {
                    try {
                        return schedule.getDeviceType(Long.parseLong((String) element));
                    } catch (NumberFormatException ignore) {
                    }
                }
                return null;
            }
        });
        // Person set editor
        binder.registerCustomEditor(Set.class, "contactPersons", new RejectingCustomCollectionEditor(Set.class) {
            @Override
            protected Object convertElement(Object element) {
                if (element != null) {
                    try {
                        return shelf.get((String) element);
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                }
                return null;
            }
        });
        // JobTicket
        binder.registerCustomEditor(JobTicketAttachment.class, "jobTicket", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                try {
                    setValue(schedule.getAttachment(Long.parseLong(text)));
                } catch (NumberFormatException ignore) {
                    setValue(null);
                }
            }
        });
        // RecordingSeries
        binder.registerCustomEditor(RecordingSeries.class, "series", new PropertyEditorSupport() {
            @Override
            public void setAsText(String s) {
                try {
                    setValue(schedule.getRecordingSeries(Long.parseLong(s)));
                } catch (NumberFormatException e) {
                    Log.debug("Illegal value: " + s);
                    setValue(null);
                }
            }
        });
        // Location
        binder.registerCustomEditor(Location.class, "location", new LocationEditor(schedule));
    }
}