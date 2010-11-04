/*

 DeviceImpl.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 10, 2008

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

package ch.ethz.replay.ui.scheduler.impl;

import ch.ethz.replay.ui.common.util.Utils;
import ch.ethz.replay.ui.scheduler.DeviceType;
import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link ch.ethz.replay.ui.scheduler.DeviceType}.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "DeviceType")
public class DeviceTypeImpl extends BaseEntity
        implements DeviceType {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @CollectionOfElements
    @JoinTable(name = "DeviceType_Capabilities",
            joinColumns = @JoinColumn(name = "DeviceTypeId"))
    @Column(name = "Capability", unique = true)
    private Set<String> capabilities = new HashSet<String>();

    //

    // Hibernate only
    DeviceTypeImpl() {
    }

    public DeviceTypeImpl(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Two DeviceTypes are defined equal if they have the same {@linkplain #getName() name}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // The name is defined to be non-null and unique so it's the ideal candidate for equality
        return (o instanceof DeviceTypeImpl) &&
                (name.equals(((DeviceTypeImpl) o).getName()));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
