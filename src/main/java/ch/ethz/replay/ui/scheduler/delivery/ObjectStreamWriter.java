/*
 
 ObjectStreamWriter.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Dec 08, 2008

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

package ch.ethz.replay.ui.scheduler.delivery;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes an object to a stream.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public interface ObjectStreamWriter<T> {

    /**
     * Writes an object to a stream.
     * <p>
     * The output stream will be flushed, but not closed.
     *
     * @param object the object to write
     * @param stream the stream to write to
     * @return true the object is valid and could be successfully written
     * @throws java.io.IOException if an IO error occurs
     */
    boolean write(T object, OutputStream stream) throws IOException;
}
