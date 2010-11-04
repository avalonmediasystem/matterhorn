/*
 
 DCMIPeriodEditor.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created 09 30, 2009

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

import ch.ethz.replay.core.common.bundle.dublincore.utils.DCMIPeriod;
import ch.ethz.replay.core.common.util.StringSupport;
import ch.ethz.replay.ui.common.web.propertyeditors.SimplePropertyEditorSupport;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyEditor;

import org.springframework.util.StringUtils;

/**
 * X
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class DCMIPeriodEditor extends SimplePropertyEditorSupport<DCMIPeriod> {

    private PropertyEditor dateEditor;

    public DCMIPeriodEditor(PropertyEditor dateEditor) {
        this.dateEditor = dateEditor;
    }

    @Override
    protected String toString(DCMIPeriod o) {
        String start = null;
        String end = null;
        if (o.getStart() != null) {
            dateEditor.setValue(o.getStart());
            start = dateEditor.getAsText();
        }
        if (o.getEnd() != null) {
            dateEditor.setValue(o.getEnd());
            end = dateEditor.getAsText();
        }
        return StringSupport.chain(" - ", start, end);
    }

    @Override
    protected DCMIPeriod toObject(String v) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
