/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.opencastproject.caption.impl;

import org.opencastproject.caption.api.IllegalTimeFormatException;
import org.opencastproject.caption.api.Time;

/**
 * Implementation of {@link Time}.
 * 
 */
public class TimeImpl implements Time {

  private int hours;
  private int minutes;
  private int seconds;
  private int miliseconds;

  public TimeImpl(int h, int m, int s, int ms) throws IllegalTimeFormatException {
    this.setHours(h);
    this.setMinutes(m);
    this.setSeconds(s);
    this.setMilliseconds(ms);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.caption.api.Time#getHours()
   */
  @Override
  public int getHours() {
    return this.hours;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.caption.api.Time#getMinutes()
   */
  @Override
  public int getMinutes() {
    return this.minutes;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.caption.api.Time#getSeconds()
   */
  @Override
  public int getSeconds() {
    return this.seconds;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.caption.api.Time#getMiliseconds()
   */
  @Override
  public int getMiliseconds() {
    return this.miliseconds;
  }

  /**
   * Checks if hours are inside the boundaries (between 0 and 99 hours).
   * @param h number of hours
   * @throws IllegalTimeFormatException if argument is less than 0 or more than 99.
   */
  private void setHours(int h) throws IllegalTimeFormatException {
    if (h < 0 || h > 99)
      throw new IllegalTimeFormatException("Invalid hour time: " + h);
    this.hours = h;
  }

  /**
   * Checks if minutes are inside the boundaries (between 0 and 59).
   * @param m number of minutes
   * @throws IllegalTimeFormatException if argument is less than 0 or more than 59.
   */
  private void setMinutes(int m) throws IllegalTimeFormatException {
    if (m < 0 || m > 59)
      throw new IllegalTimeFormatException("Invalid minute time: " + m);
    this.minutes = m;
  }

  /**
   * Checks if seconds are inside the boundaries (between 0 and 59).
   * @param s number of seconds
   * @throws IllegalTimeFormatException if argument is less than 0 or more than 59.
   */
  private void setSeconds(int s) throws IllegalTimeFormatException {
    if (s < 0 || s > 59)
      throw new IllegalTimeFormatException("Invalid second time: " + s);
    this.seconds = s;
  }

  /**
   * Checks if milliseconds are inside the boundaries (between 0 and 999).
   * @param ms number of milliseconds
   * @throws IllegalTimeFormatException if argument is less than 0 or more than 999.
   */
  private void setMilliseconds(int ms) throws IllegalTimeFormatException {
    if (ms < 0 || ms > 999)
      throw new IllegalTimeFormatException("Invalid milisecond time: " + ms);
    this.miliseconds = ms;
  }
}
