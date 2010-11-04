/*
 
 ExtenderSupport.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 8, 2008

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

package ch.ethz.replay.ui.scheduler.web.extension;

import ch.ethz.replay.ui.common.util.Extender;
import ch.ethz.replay.ui.scheduler.Recording;
import ch.ethz.replay.ui.scheduler.impl.util.RecordingValidator;

/**
 * ExtenderSupport contains convenience methods for the most common extension situations.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class ExtenderSupport {

    private ExtenderSupport() {
    }

    public static Recording extend(Recording rec, final RecordingValidator recVal) {
        return new Extender<Recording>(RecordingExtensionImpl.class){
            @Override
            protected Object newExtensionFor(Recording object) {
                return new RecordingExtensionImpl(object, recVal);
            }
        }.extend(rec);
    }
}
