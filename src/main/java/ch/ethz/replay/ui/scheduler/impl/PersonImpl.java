/*

 PersonImpl.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 2, 2008

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

import ch.ethz.replay.ui.scheduler.Person;

import org.apache.commons.lang.StringUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Simple implementation of {@link ch.ethz.replay.ui.scheduler.Person} with support for only a subset of the properties.
 * 
 * 
 */
@Entity(name = "Person")
@Access(AccessType.FIELD)
public class PersonImpl implements Person {

  @Id
  @GeneratedValue
  Long id;

  @Column(nullable = false)
  private String familyName;

  @Column(nullable = false)
  private String givenName;

  @Column(nullable = false)
  private String honorificPrefixes;

  @Column(nullable = false)
  private String emailAddress = null;

  public PersonImpl() {
  }

  public PersonImpl(String givenName, String familyName) {
    this.familyName = familyName;
    this.givenName = givenName;
  }

  public PersonImpl(String givenName, String familyName, String preferredEmailAddress) {
    this.familyName = familyName;
    this.givenName = givenName;
    emailAddress = preferredEmailAddress;
  }

  @Override
  public String getFormattedName() {
    StringBuilder b = new StringBuilder();
    if (StringUtils.isNotBlank(honorificPrefixes))
      b.append(honorificPrefixes).append(" ");
    if (StringUtils.isNotBlank(givenName))
      b.append(givenName).append(" ");
    if (StringUtils.isNotBlank(familyName))
      b.append(familyName);
    return b.toString();
  }

  @Override
  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  @Override
  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  @Override
  public String getHonorificPrefixes() {
    return honorificPrefixes;
  }

  public void setHonorificPrefixes(String honorificPrefixes) {
    this.honorificPrefixes = honorificPrefixes;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Person#getEmailAddress()
   */
  @Override
  public String getEmailAddress() {
    return emailAddress;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Person#setEmailAddress(java.lang.String)
   */
  @Override
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Person p) {
    int result = 0;
    result = _compareTo(getFamilyName(), p.getFamilyName());
    if (result != 0)
      return result;
    result = _compareTo(getGivenName(), p.getGivenName());
    if (result != 0)
      return result;
    if (emailAddress == null)
      return p.getEmailAddress() == null ? 0 : -1;
    if (p.getEmailAddress() == null)
      return -1;
    return 0;
  }

  @SuppressWarnings("unchecked")
  private int _compareTo(@SuppressWarnings("rawtypes") Comparable a, @SuppressWarnings("rawtypes") Comparable b) {
    if (a == null)
      return b == null ? 0 : -1;
    if (b == null)
      return -1;
    return a.compareTo(b);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ethz.replay.ui.scheduler.Person#getId()
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Sets the identifier.
   * 
   * @param id
   *          the identifier
   */
  public void setId(Long id) {
    this.id = id;
  }
}
