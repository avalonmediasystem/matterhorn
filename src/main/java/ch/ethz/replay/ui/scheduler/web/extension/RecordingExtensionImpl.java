/*

 RecordingExtensionImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 23, 2008

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

package ch.ethz.replay.ui.scheduler.web.extension;

import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.impl.util.RecordingValidator;
import ch.ethz.replay.ui.common.util.ExtensionSupport;
import ch.ethz.replay.ui.common.web.CommandMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Map;
import java.util.Date;
import java.util.Calendar;

/**
 * Extension for {@link ch.ethz.replay.ui.scheduler.Recording}s to provide
 * extra information for views.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class RecordingExtensionImpl extends ExtensionSupport<Recording>
        implements RecordingExtension {

    private RecordingValidator recVal;

    public RecordingExtensionImpl(Recording object, RecordingValidator recVal) {
        super(object);
        this.recVal = recVal;
    }

    public boolean isRecording() {
        long now = System.currentTimeMillis();
        return object.getStartDate().getTime() <= now && now <= object.getEndDate().getTime();
    }

    public boolean isEditable() {
        long now = System.currentTimeMillis();
        return now < object.getStartDate().getTime();
    }

    public boolean isValid() {
        Errors errors = new BeanPropertyBindingResult(object, "object");
        recVal.validate(object, errors);
        return !errors.hasErrors();
    }

    public Map getOtherDay() {
        return new CommandMap<Date, Boolean>() {
            @Override
            public Boolean execute(Date date) {
                return date == null ||
                        new DateTime(date).getDayOfYear() - new DateTime(object.getStartDate()).getDayOfYear() != 0;
            }
        };
    }

    public int getWeekDay() {
        return new DateTime(object.getStartDate()).getDayOfWeek() - 1;
    }
}
