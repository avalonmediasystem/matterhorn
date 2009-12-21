/**
 *  Copyright 2009 The Regents of the University of California
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
package org.opencastproject.scheduler.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;

import org.opencastproject.scheduler.api.SchedulerEvent;
import org.opencastproject.scheduler.api.SchedulerService;
import org.opencastproject.scheduler.impl.dao.SchedulerServiceImplDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchedulerServiceImplTest {
  private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImplTest.class);
  
  private SchedulerService service = null;
  private static final String storageRoot = "target" + File.separator + "scheduler-test-db";
  private static final String resourcesRoot = "src" + File.separator + "main" + File.separator + "resources";

  private Connection connectToDatabase(File storageDirectory) {
    if (storageDirectory == null) {
      storageDirectory = new File(File.separator + "tmp" + File.separator +"opencast" + File.separator + "scheduler-db");
    }
    String jdbcUrl = "jdbc:derby:" + storageDirectory.getAbsolutePath();
    String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    try {
      Class.forName(driver).newInstance();      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Connection conn = null;
    try {
      Properties props = new Properties();
      conn = DriverManager.getConnection(jdbcUrl + ";create=true", props);
    } catch (SQLException e) {
    }
    return conn;
  }    
  
  @Before
  public void setup() {
    // Clean up database
    try { 
      FileUtils.deleteDirectory(new File(storageRoot));
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }    
    service = new SchedulerServiceImplDAO(connectToDatabase(new File(storageRoot)));
    // set Metadata Mapping Files. This depends on if its called from OSGI or in a regular case.
    try {
      ((SchedulerServiceImplDAO)service).setDublinCoreGenerator(new DublinCoreGenerator(new FileInputStream(resourcesRoot+ File.separator+"config"+File.separator+"dublincoremapping.properties")));
      ((SchedulerServiceImplDAO)service).setCaptureAgentMetadataGenerator(new CaptureAgentMetadataGenerator(new FileInputStream(resourcesRoot+ File.separator+"config"+File.separator+"captureagentmetadatamapping.properties")));
    } catch (FileNotFoundException e) {
      Assert.fail(e.getMessage());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @After
  public void teardown() {
    service = null;
  }
  
  @Test
  public void testEventManagement() {
    SchedulerEvent event = new SchedulerEventImpl();
    event.setID("rrolf1001"); 
    event.setTitle("new recording");
    event.setStartdate(new Date (System.currentTimeMillis())); 
    event.setEnddate(new Date (System.currentTimeMillis()+50000));
    event.setLocation("42/201");
    event.setDevice("testrecorder");
    event.setCreator("secret lecturer");
    SchedulerEvent eventUpdated = service.addEvent(event);
    Assert.assertEquals(service.getEvent(eventUpdated.getID()).getLocation(), event.getLocation());
    System.out.println (service.getCalendarForCaptureAgent("testrecorder"));
    CalendarBuilder calBuilder = new CalendarBuilder();
    Calendar cal;
    // TODO iCal4j does not build the calendar that it created itself, because of a Problem with base64 and maximum line length (see MH-1870)
  /*  try {
      cal = calBuilder.build(new StringReader(service.getCalendarForCaptureAgent("testrecorder")));
      ComponentList vevents = cal.getComponents(VEvent.VEVENT);
      for (int i = 0; i < vevents.size(); i++) {
        PropertyList attachments = ((VEvent)vevents.get(i)).getProperties(Property.ATTACH);
        for (int j = 0; j < attachments.size(); j++) {
          String attached = ((Property)attachments.get(j)).getValue();
          attached = new String (Base64.decodeBase64(attached));
          System.out.println(attached);
        }
      } 
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    } catch (ParserException e) {
      Assert.fail(e.getMessage());
    } */
    eventUpdated.setStartdate(new Date(System.currentTimeMillis()+2000));
    eventUpdated.setContributor("Matterhorn");
    Assert.assertTrue(service.updateEvent(eventUpdated));
    Assert.assertEquals(service.getEvent(eventUpdated.getID()).getContributor(), "Matterhorn");
    Assert.assertNotNull(service.getEvents(null));
    service.removeEvent(eventUpdated.getID());
    Assert.assertNull(service.getEvent(eventUpdated.getID()));   
  }
}