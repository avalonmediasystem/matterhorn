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
package org.opencastproject.util.doc;

import java.util.ArrayList;
import java.util.List;

public class RestTestForm {
  /**
   * Create a form which indicates that the system should generate
   * a test form using the current endpoint data
   * 
   * @return a RestTestForm which indicates the form should be generated 
   * based on the current data in the endpoint
   */
  public static RestTestForm auto() {
    return new RestTestForm(true);
  }

  /**
   * Indicates this form data was autogenerated
   */
  boolean autoGenerated = false;
  /**
   * Indicates whether the form submission should be an ajax submit or a normal submit
   */
  boolean ajaxSubmit = false;
  /**
   * If this is true then the upload contains a bodyParam (i.e. a file upload option)
   */
  boolean fileUpload = false;
  /**
   * This indicates that this test form is for a basic endpoint which has no params
   */
  boolean basic = false;
  /**
   * This is used to render the test form in place of using the template
   */
  String html;
  /**
   * URL to the page where a test form is located (no form is rendered)
   */
  String url;
  String title;
  /**
   * Indicates the the path contains the {FORMAT} key in it which should be replaced by a real
   * value when the path is output
   */
  boolean usesPathFormat = false;
  List<Param> items;

  protected RestTestForm(boolean autoGenerate) {
    this.autoGenerated = autoGenerate;
    this.html = null;
  }

  public RestTestForm(String html) {
    if (DocData.isBlank(html)) {
      throw new IllegalArgumentException("html must not be blank");
    }
    this.html = html;
  }

  public RestTestForm(String url, String title) {
    if (DocData.isBlank(url)) {
      throw new IllegalArgumentException("url must not be blank");
    }
    this.url = url;
    this.title = title;
  }

  public RestTestForm(String title, boolean ajaxSubmit) {
    this.items = new ArrayList<Param>();
    this.title = title;
    this.ajaxSubmit = ajaxSubmit;
  }

  public RestTestForm(Param[] items, String title, boolean ajaxSubmit) {
    if (items == null || items.length == 0) {
      throw new IllegalArgumentException("params must be set to at least on item");
    }
    this.items = new ArrayList<Param>(items.length);
    this.title = title;
    this.ajaxSubmit = ajaxSubmit;
  }

  /**
   * Special constructor which will auto-populate the form using
   * the data in the endpoint,
   * this will enable the ajax submit if it is possible to do so
   * 
   * @param endpoint a populated rest endpoint
   */
  public RestTestForm(RestEndpoint endpoint) {
    if (endpoint == null) {
      throw new IllegalArgumentException("endpoint must be set");
    }
    this.autoGenerated = true;
    this.ajaxSubmit = true;
    this.items = new ArrayList<Param>(3);
    boolean hasUpload = false;
    if (endpoint.getPathParams() != null) {
      for (Param param : endpoint.getPathParams()) {
        param.required = true;
        this.items.add(param);
      }
    }
    if (endpoint.getRequiredParams() != null) {
      for (Param param : endpoint.getRequiredParams()) {
        param.required = true;
        if (Param.Type.FILE.name().equalsIgnoreCase(param.type)) {
          hasUpload = true;
        }
        this.items.add(param);
      }
    }
    if (endpoint.getOptionalParams() != null) {
      for (Param param : endpoint.getOptionalParams()) {
        param.required = false;
        if (Param.Type.FILE.name().equalsIgnoreCase(param.type)) {
          hasUpload = true;
        }
        this.items.add(param);
      }
    }
    if (endpoint.getBodyParam() != null) {
      Param param = endpoint.getBodyParam();
      param.required = true;
      if (Param.Type.FILE.name().equalsIgnoreCase(param.type)) {
        hasUpload = true;
      }
      this.items.add(param);
    }
    if (hasUpload) {
      this.fileUpload = true;
      this.ajaxSubmit = false;
    }
    if (this.items.isEmpty() && endpoint.isGetMethod()) {
      this.basic = true;
    }
  }

  /**
   * Returns true if this form has nothing in it,
   * false if there is data in it
   */
  public boolean isEmpty() {
    boolean empty = true;
    if (items != null && ! items.isEmpty()) {
      empty = false;
    } else if (html != null) {
      empty = false;
    } else if (url != null) {
      empty = false;
    }
    return empty;
  }

  /**
   * Controls whether the form will be submitted via ajax and the content
   * rendered on the page, NOTE that uploading any files or downloading
   * any content that is binary will require not using ajax submit,
   * also note that there may be other cases where ajax submission will
   * fail to work OR where normal submission will fail to work (using PUT/DELETE)
   * 
   * @param ajaxSubmit
   */
  public void setAjaxSubmit(boolean ajaxSubmit) {
    this.ajaxSubmit = ajaxSubmit;
  }

  /**
   * Set this to true if the file contains a file upload control,
   * this will be determined automatically for autogenerated forms
   * @param fileUpload
   */
  public void setFileUpload(boolean fileUpload) {
    this.fileUpload = fileUpload;
    if (this.fileUpload) {
      this.ajaxSubmit = false;
    }
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return "FORM:items="+(items != null ? items.size() : 0)+":url="+url+":html="+(html != null);
  }

  public boolean isAutoGenerated() {
    return autoGenerated;
  }

  public boolean isAjaxSubmit() {
    return ajaxSubmit;
  }

  public boolean isUsesPathFormat() {
    return usesPathFormat;
  }
  
  public boolean isFileUpload() {
    return fileUpload;
  }

  public boolean isBasic() {
    return basic;
  }

  public String getHtml() {
    return html;
  }

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public List<Param> getItems() {
    return items;
  }

}
