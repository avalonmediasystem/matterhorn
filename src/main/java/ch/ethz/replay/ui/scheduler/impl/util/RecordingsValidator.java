/*
 
 RecordingsValidator.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 14, 2009

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

package ch.ethz.replay.ui.scheduler.impl.util;

import ch.ethz.replay.ui.common.util.AbstractValidator;
import ch.ethz.replay.ui.scheduler.Recording;
import org.springframework.validation.Errors;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.List;

/**
 * Validates a collection of {@link ch.ethz.replay.ui.scheduler.Recording}s.
 */
public class RecordingsValidator extends AbstractValidator {

    private RecordingValidator recVal;

    public RecordingsValidator(RecordingValidator recVal) {
        this.recVal = recVal;
    }

    public boolean supports(Class clazz) {
        return List.class.isAssignableFrom(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {
        List<Recording> recs = (List<Recording>) target;
        for (int i = 0; i < recs.size(); i++) {
            Recording rec;
            try {
                rec = recs.get(i);
            } catch (ClassCastException e) {
                throw new RuntimeException("Validator can only be used with collections of Recordings");
            }
            errors.pushNestedPath("list[" + i + "]");
            recVal.validate(rec, errors);
            errors.popNestedPath();
        }
    }

    @Override
    public Errors newErrorsFor(final Object target) {
        // The access helper is needed to descend into the list items later on
        // @see errors.pushNestedPath
        return new BeanPropertyBindingResult(new AccessHelper() {
            public List getList() {
                return (List) target;
            }
        }, "target");
    }

    public RecordingValidator getRecordingValidator() {
        return recVal;
    }

    interface AccessHelper {
        List getList();
    }
}
