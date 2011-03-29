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
package org.opencastproject.series.impl;


import org.opencastproject.metadata.dublincore.DublinCore;
import org.opencastproject.metadata.dublincore.DublinCoreCatalog;
import org.opencastproject.metadata.dublincore.DublinCoreCatalogService;
import org.opencastproject.security.api.AccessControlEntry;
import org.opencastproject.security.api.AccessControlList;
import org.opencastproject.series.api.SeriesQuery;
import org.opencastproject.series.impl.persistence.SeriesServiceDatabaseImpl;
import org.opencastproject.series.impl.solr.SeriesServiceSolrIndex;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.util.PathSupport;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for Series Service.
 *
 */
public class SeriesServiceImplTest {

  private ComboPooledDataSource pooledDataSource;
  private SeriesServiceDatabaseImpl seriesDatabase;
  private String storage;
  
  private SeriesServiceSolrIndex index;
  private DublinCoreCatalogService dcService;
  private String root;
  
  private SeriesServiceImpl seriesService;
  
  private DublinCoreCatalog testCatalog;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    long currentTime = System.currentTimeMillis();
    storage = PathSupport.concat("target", "db" + currentTime + ".h2.db");
    
    pooledDataSource = new ComboPooledDataSource();
    pooledDataSource.setDriverClass("org.h2.Driver");
    pooledDataSource.setJdbcUrl("jdbc:h2:./target/db" + currentTime);
    pooledDataSource.setUser("sa");
    pooledDataSource.setPassword("sa");

    // Collect the persistence properties
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("javax.persistence.nonJtaDataSource", pooledDataSource);
    props.put("eclipselink.ddl-generation", "create-tables");
    props.put("eclipselink.ddl-generation.output-mode", "database");

    seriesDatabase = new SeriesServiceDatabaseImpl();
    seriesDatabase.setPersistenceProvider(new PersistenceProvider());
    seriesDatabase.setPersistenceProperties(props);
    dcService = new DublinCoreCatalogService();
    seriesDatabase.setDublinCoreService(dcService);
    seriesDatabase.activate(null);
    
    root = PathSupport.concat("target", Long.toString(currentTime));
    index = new SeriesServiceSolrIndex(root);
    index.setDublinCoreService(dcService);
    index.activate(null);
    
    seriesService = new SeriesServiceImpl();
    seriesService.setPersistence(seriesDatabase);
    seriesService.setIndex(index);
    seriesService.activate(null);

    InputStream in = null;
    try {
      in = getClass().getResourceAsStream("/dublincore.xml");
      testCatalog = dcService.load(in);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
  
  @Test
  public void testSeriesManagemnt() throws Exception {
    testCatalog.set(DublinCore.PROPERTY_TITLE, "Some title");
    seriesService.updateSeries(testCatalog);
    DublinCoreCatalog retrivedSeries = seriesService.getSeries(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER));
    Assert.assertEquals("Some title", retrivedSeries.getFirst(DublinCore.PROPERTY_TITLE));
    
    testCatalog.set(DublinCore.PROPERTY_TITLE, "Some other title");
    seriesService.updateSeries(testCatalog);
    retrivedSeries = seriesService.getSeries(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER));
    Assert.assertEquals("Some other title", retrivedSeries.getFirst(DublinCore.PROPERTY_TITLE));
    
    seriesService.deleteSeries(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER));
    try {
      seriesService.getSeries(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER));
      Assert.fail("Series should not be available after removal.");
    } catch (NotFoundException e) {
      // expected
    }
  }
  
  @Test
  public void testSeriesQuery() throws Exception {
    testCatalog.set(DublinCore.PROPERTY_TITLE, "Some title");
    seriesService.updateSeries(testCatalog);
    SeriesQuery q = new SeriesQuery().setSeriesTitle("other");
    List<DublinCoreCatalog> result = seriesService.getSeries(q).getCatalogList();
    Assert.assertEquals(0, result.size());
    
    testCatalog.set(DublinCore.PROPERTY_TITLE, "Some other title");
    seriesService.updateSeries(testCatalog);
    result = seriesService.getSeries(q).getCatalogList();
    Assert.assertEquals(1, result.size());
  }
  
  @Test
  public void testACLManagment() throws Exception {
    // sample access control list
    AccessControlList accessControlList = new AccessControlList();
    List<AccessControlEntry> acl = accessControlList.getEntries();
    acl.add(new AccessControlEntry("admin", "delete", true));
    
    try {
      seriesService.updateAccessControl("failid", accessControlList);
      Assert.fail("Should fail when adding ACL to nonexistent series,");
    } catch (NotFoundException e) {
      // expected
    }
    
    seriesService.updateSeries(testCatalog);
    seriesService.updateAccessControl(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER), accessControlList);
    AccessControlList retrievedACL = seriesService.getSeriesAccessControl(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER));
    Assert.assertNotNull(retrievedACL);
    acl = retrievedACL.getEntries();
    Assert.assertEquals(acl.size(), 1);
    Assert.assertEquals("admin", acl.get(0).getRole());
    
    acl = accessControlList.getEntries();
    acl.clear();
    acl.add(new AccessControlEntry("student", "read", true));
    seriesService.updateAccessControl(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER), accessControlList);
    retrievedACL = seriesService.getSeriesAccessControl(testCatalog.getFirst(DublinCore.PROPERTY_IDENTIFIER));
    Assert.assertNotNull(retrievedACL);
    acl = retrievedACL.getEntries();
    Assert.assertEquals(acl.size(), 1);
    Assert.assertEquals("student", acl.get(0).getRole());
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    seriesDatabase.deactivate(null);
    pooledDataSource.close();
    FileUtils.forceDelete(new File(storage));
    seriesDatabase = null;
    index.deactivate();
    FileUtils.deleteDirectory(new File(root));
    index = null;
  }

}
