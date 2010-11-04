/*
 
 TemporalEditor.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 10 01, 2009

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

package ch.ethz.replay.ui.scheduler.web.propertyeditors;

import ch.ethz.replay.core.common.bundle.dublincore.utils.*;
import ch.ethz.replay.ui.common.web.propertyeditors.SimplePropertyEditorSupport;

import java.beans.PropertyEditor;
import java.util.Date;

/**
 * Handles temporals.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class TemporalEditor extends SimplePropertyEditorSupport<Temporal> {

    private PropertyEditor dateEditor;
    private PropertyEditor durationEditor;
    private PropertyEditor periodEditor;

    /**
     * New editor.
     *
     * @param dateEditor convert from and to {@link java.util.Date}
     * @param durationEditor convert from and to {@link Long}
     * @param periodEditor convert from and to {@link ch.ethz.replay.core.common.bundle.dublincore.utils.DCMIPeriod}
     */
    public TemporalEditor(PropertyEditor dateEditor, PropertyEditor durationEditor, PropertyEditor periodEditor) {
        this.dateEditor = dateEditor;
        this.durationEditor = durationEditor;
        this.periodEditor = periodEditor;
    }

    @Override
    protected String toString(Temporal o) {
        PropertyEditor delegate = null;
        if (o instanceof InstantTemporal)
            delegate = dateEditor;
        else if (o instanceof DurationTemporal)
            delegate = durationEditor;
        else if (o instanceof PeriodTemporal)
            delegate = periodEditor;
        else
            throw new RuntimeException(o + " is not supported");
        delegate.setValue(o.getTemporal());
        return delegate.getAsText();
    }

    @Override
    protected Temporal toObject(String v) {
        try {
            dateEditor.setAsText(v);
            return new InstantTemporal((Date) dateEditor.getValue());
        } catch (IllegalArgumentException e) {
            try {
                durationEditor.setAsText(v);
                return new DurationTemporal((Long) durationEditor.getValue());
            } catch (IllegalArgumentException e1) {
                try {
                    periodEditor.setAsText(v);
                    return new PeriodTemporal((DCMIPeriod) periodEditor.getValue());
                } catch (IllegalArgumentException e2) {
                    throw new IllegalArgumentException("Cannot parse " + v);
                }
            }
        }
    }
}
