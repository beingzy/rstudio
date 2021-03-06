/*
 * StaticDataResource.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.core.client.resources;

import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.ext.ResourceGeneratorType;
import org.rstudio.core.rebind.StaticDataResourceGenerator;

@ResourceGeneratorType(StaticDataResourceGenerator.class)
public interface StaticDataResource extends DataResource
{
   /**
    * Retrieves a URL by which the contents of the resource can be obtained. This
    * will be an absolute URL.
    */
   String getUrl();
}
