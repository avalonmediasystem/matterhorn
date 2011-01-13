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
package org.opencastproject.analysis.speech;

import java.util.Observable;

/**
 * Allows to handle the progress of any process
 */
public class ProcessObservable extends Observable {

  public static enum Mode {
    STATUS, STATUS_PROGRESS, STATUS_PROGRESS_UNDEFINED, SR_PROGRESS, EXCEPTION, END
  }

  /**
   * @param text
   *          The status message
   */
  public void setStatus(String text) {
    setChanged();
    notifyObservers(new ProcessMessage(Mode.STATUS, text));
  }

  /**
   * @param percentage
   *          The status (sub-)progress in percent
   */
  public void setStatusProgress(double percentage) {
    setChanged();
    notifyObservers(new ProcessMessage(Mode.STATUS_PROGRESS, percentage));
  }

  /**
   * Announces an undefined progress value
   */
  public void setStatusProgressUndefined() {
    setChanged();
    notifyObservers(new ProcessMessage(Mode.STATUS_PROGRESS_UNDEFINED));
  }

  /**
   * @param percentage
   *          The overall progress in percent
   */
  public void setProgress(double percentage) {
    setChanged();
    notifyObservers(new ProcessMessage(Mode.SR_PROGRESS, percentage));
  }

  /**
   * @param throwable
   *          En exception occured
   */
  public void setException(Throwable throwable) {
    setChanged();
    notifyObservers(new ProcessMessage(Mode.EXCEPTION, throwable));
  }

  /**
   * Process is finished
   */
  public void setFinished() {
    setChanged();
    notifyObservers(new ProcessMessage(Mode.END));
  }

  /**
   * Wrapper to send the mode plus a value (e.g. string, double, exception)
   * 
   * @author Markus Benz <markus.benz@fhnw.ch>, University of Applied Sciences Northwestern Switzerland
   * @version $Id: ProcessObservable.java 1 Aug 19, 2008 10:58:32 AM benzm0 $
   */
  public static class ProcessMessage {
    private Mode mode;
    private Object arg;

    /**
     * @param mode
     */
    public ProcessMessage(Mode mode) {
      this.mode = mode;
    }

    /**
     * @param mode
     * @param arg
     */
    public ProcessMessage(Mode mode, Object arg) {
      this.mode = mode;
      this.arg = arg;
    }

    /**
     * @return Returns the mode.
     */
    public Mode getMode() {
      return mode;
    }

    /**
     * @param mode
     *          The mode to set.
     */
    public void setMode(Mode mode) {
      this.mode = mode;
    }

    /**
     * @return Returns the arg.
     */
    public Object getArg() {
      return arg;
    }

    /**
     * @param arg
     *          The arg to set.
     */
    public void setArg(Object arg) {
      this.arg = arg;
    }
  }
}
