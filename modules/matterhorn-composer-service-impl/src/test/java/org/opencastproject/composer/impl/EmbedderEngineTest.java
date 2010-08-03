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
package org.opencastproject.composer.impl;

import org.opencastproject.composer.api.EmbedderEngine;
import org.opencastproject.composer.api.EmbedderException;
import org.opencastproject.composer.impl.qtembedder.QTSbtlEmbedderEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for QuickTime embedder engine.
 * 
 */
public class EmbedderEngineTest {

  private EmbedderEngine engine;
  private File captions;
  private File movie;
  private Map<String, String> properties;
  private File resultingFile;

  @Before
  public void setUp() throws Exception {
    // create engine
    engine = new QTSbtlEmbedderEngine();
    // load captions and movie
    captions = new File(EmbedderEngineTest.class.getResource("/captions_test.srt").toURI());
    Assert.assertNotNull(captions);
    movie = new File(EmbedderEngineTest.class.getResource("/slidechanges.mov").toURI());
    Assert.assertNotNull(movie);
    // create properties
    properties = new HashMap<String, String>();
    properties.put("param.lang", "en");
  }

  @Test
  public void testEmbedding() throws EmbedderException, URISyntaxException {
    resultingFile = engine.embed(movie, captions, properties);
  }

  @After
  public void tearDown() throws Exception {
    if (resultingFile != null)
      Assert.assertTrue(resultingFile.delete());
  }

}
