/*

 RecordingValidator.java
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

package ch.ethz.replay.ui.scheduler.impl.util;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Date;

import ch.ethz.replay.ui.scheduler.impl.RecordingImpl;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.common.util.AbstractValidator;

/**
 * Validates a {@link ch.ethz.replay.ui.scheduler.Recording}.
 * 
 * 
 */
public class RecordingValidator extends AbstractValidator {

  private Validator dcEpisodeValidator;
  private Validator dcSeriesValidator;

  public RecordingValidator(Validator dcEpisodeValidator, Validator dcSeriesValidator) {
    this.dcEpisodeValidator = dcEpisodeValidator;
    this.dcSeriesValidator = dcSeriesValidator;
  }

  public boolean supports(Class clazz) {
    return Recording.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    super.validate(target, errors);
    Recording rec = (Recording) target;
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", "empty");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", "empty");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "location", "empty");
    if (rec.getDevices().size() == 0) {
      errors.rejectValue("devices", "empty");
    }
    if (rec.getEndDate() != null && rec.getStartDate() != null && !rec.getEndDate().after(rec.getStartDate())) {
      errors.rejectValue("endDate", "error.beforeStart");
    }
    /*
     * if (rec.getEndDate() != null && rec.getEndDate().before(new Date())) { errors.rejectValue("endDate",
     * "error.endDateInPast"); }
     */

    // Validate metadata

    if (rec.getDublinCore() == null)
      errors.rejectValue("dublinCore", "empty");
    else {
      errors.pushNestedPath("dublinCore");
      dcEpisodeValidator.validate(rec.getDublinCore(), errors);
      errors.popNestedPath();
    }

    if (rec.getSeries() != null && rec.getSeries().getDublinCore() != null) {
      errors.pushNestedPath("series.dublinCore");
      dcSeriesValidator.validate(rec.getSeries().getDublinCore(), errors);
      errors.popNestedPath();
    }

    // todo check for overlaping recordings for a certain location
  }
}