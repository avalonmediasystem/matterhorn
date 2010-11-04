/*

 AbstractDomainController.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 26, 2008

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

import ch.ethz.replay.ui.common.web.spring.flow.ControllerCall;
import ch.ethz.replay.ui.common.web.shelf.Shelf;
import ch.ethz.replay.ui.scheduler.Schedule;
import ch.ethz.replay.ui.scheduler.web.SchedulerSession;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Base support class for controllers using the domain classes.
 * <p>
 * Registers a StringTrimmerEditor that discards all empty value for all string properties. If you do not
 * want this override {@link #isEmptyStringAsNull()}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public abstract class AbstractDomainController {

    protected Schedule schedule;
    protected SchedulerSession scheduleSession;
    protected Shelf shelf;

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setSchedulerSession(SchedulerSession session) {
        this.scheduleSession = session;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    // --------------------------------------------------------------------------------------------

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        if (isEmptyStringAsNull())
            binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    protected boolean isEmptyStringAsNull() {
        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Does a simple redirect;
     */
    public String redirectTo(Class controller, String method) {
        return new StringBuilder("redirect:")
                .append(scheduleSession.getDispatcherServletPath()).append("/")
                .append(scheduleSession.getControllerName(controller)).append("/")
                .append(method)
                .toString();
    }

    // SchedulerSession delegates

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#redirect
     */
    public String redirect(Class controller, Object argument) {
        return scheduleSession.redirect(controller, argument);
    }

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#getRedirectArgument(Class)
     */
    public <T> T getRedirectArgument(Class<T> type) {
        return scheduleSession.getRedirectArgument(type);
    }

    public String call(Class controller, Class returnTo, Object argument) {
        return scheduleSession.call(controller, returnTo, argument);
    }

    public String call(Class contoller) {
        return scheduleSession.call(contoller);
    }

    public String call(Class controller, Object argument) {
        return scheduleSession.call(controller, argument);
    }

    public String call(Class controller, Object argument, Object saveState) {
        return scheduleSession.call(controller, argument, saveState);
    }

    public String call(Class controller, Class returnTo, Object argument, Object saveState) {
        return scheduleSession.call(controller, returnTo, argument, saveState);
    }

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#isCalled
     */
    public boolean isCalled() {
        return scheduleSession.isCalled();
    }

    /**
     */
    public String back() {
        return scheduleSession.back();
    }

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#backIfCalled(String)
     */
    public String backIfCalled(String otherUrl) {
        return scheduleSession.backIfCalled(otherUrl);
    }

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#back(Object)
     */
    public String back(Object returnValue) {
        return scheduleSession.back(returnValue);
    }

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#getCallArgument(Class)
     */
    public <T> T getCallArgument(Class<T> type) {
        return scheduleSession.getCallArgument(type);
    }

    /**
     * @see ch.ethz.replay.ui.scheduler.web.SchedulerSession#getReturnedCall()
     */
    public ControllerCall getReturnedCall() {
        return scheduleSession.getReturnedCall();
    }
}
