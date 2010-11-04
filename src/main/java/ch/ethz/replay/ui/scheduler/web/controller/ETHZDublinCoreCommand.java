/*
 
 ETHZDublinCoreCommand.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Oct 01, 2009

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

package ch.ethz.replay.ui.scheduler.web.controller;

import ch.ethz.replay.core.api.common.metadata.dublincore.DublinCoreValue;
import ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore;
import ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore;
import ch.ethz.replay.core.common.bundle.dublincore.utils.DCMIPeriod;
import ch.ethz.replay.core.common.bundle.dublincore.utils.Temporal;
import ch.ethz.replay.core.common.util.CollectionSupport;
import ch.ethz.replay.core.common.util.StringSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Command object to deal with {@link ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore}s.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class ETHZDublinCoreCommand {

    private static final Logger Log = Logger.getLogger(ETHZDublinCoreCommand.class);

    public static class Value {

        public static final Value EMPTY = new Value();

        private String value;
        private String language;

        public Value(String value, String language) {
            this.value = value;
            this.language = language;
        }

        public Value() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    private ETHZDublinCore dc;

    // Editable lists are locally buffered, because direct access to the lists
    // of the Dublin Core did not work; the values aren't updated by the Spring MVC framework

    private List<Value> contributors = new ArrayList<Value>();
    private List<Value> spatials = new ArrayList<Value>();
    private List<String> creators = new ArrayList<String>();
    
    // For template

    private boolean isPartOfAssigned, eventNumberAssigned, typeAssigned,
            originalTitleAssigned, englishTitleAssigned,
            originalDescriptionAssigned, englishDescriptionAssigned,
            languageAssigned, advertisedAssigned,
            creatorsAssigned, spatialsAssigned, contributorsAssigned,
            licenseDeAssigned, licenseEnAssigned;

    public ETHZDublinCoreCommand(ETHZDublinCore dc) {
        if (dc == null)
            throw new IllegalArgumentException("Dublin Core must not be null");
        
        this.dc = dc;                                                                                                                                               

        if (dc.getContributors() != null)
            for (DublinCoreValue v : dc.getContributors())
                contributors.add(new Value(v.getValue(), v.getLanguage()));
        if (dc.getSpatials() != null)
            for (DublinCoreValue v : dc.getSpatials())
                spatials.add(new Value(v.getValue(), v.getLanguage()));
        if (dc.getCreators() != null)
            creators.addAll(dc.getCreators());

        // Add empty values, to get an extra empty input field
        contributors.add(new Value("", DublinCore.LANGUAGE_UNDEFINED));
        spatials.add(new Value("", DublinCore.LANGUAGE_UNDEFINED));
        creators.add("");
    }

    /**
     * Sync fields to wrapped Dublin Core
     */
    public ETHZDublinCore sync() {
        dc.setContributors(toDCVList(contributors));
        dc.setSpatials(toDCVList(spatials));
        dc.setCreators(compact(creators));
        return dc;
    }

    /**
     * Sync properties that have a <code><i>property</i>Assigned</code> which is
     * set to true to another Dublin Core.
     */
    public void syncAssigned(ETHZDublinCore fdc) {
        if (advertisedAssigned)
            fdc.setAdvertised(dc.isAdvertised());
        if (contributorsAssigned)
            if (dc.getContributors().size() > 0) fdc.setContributors(dc.getContributors());
            else fdc.removeContributors();
        if (creatorsAssigned)
            if (dc.getCreators().size() > 0) fdc.setCreators(dc.getCreators());
            else fdc.removeCreators();
        if (englishDescriptionAssigned)
            if (dc.getEnglishDescription() != null) fdc.setEnglishDescription(dc.getEnglishDescription());
            else fdc.removeEnglishDescription();
        if (englishTitleAssigned)
            if (dc.getEnglishTitle() != null) fdc.setEnglishTitle(dc.getEnglishTitle());
            else fdc.removeEnglishTitle();
        if (eventNumberAssigned)
            if (dc.getEventNumber() != null) fdc.setEventNumber(dc.getEventNumber());
            else fdc.removeEventNumber();
        if (isPartOfAssigned)
            if (dc.getIsPartOf() != null) fdc.setIsPartOf(dc.getIsPartOf());
            else fdc.removeIsPartOf();
        if (typeAssigned)
            if (dc.getType() != null) fdc.setType(dc.getType()[0], dc.getType()[1]);
            else fdc.removeType();
        if (languageAssigned)
            if (dc.getLanguage() != null) fdc.setLanguage(dc.getLanguage());
            else fdc.removeLanguage();
        if (originalDescriptionAssigned)
            if (dc.getOriginalDescription() != null) fdc.setOriginalDescription(dc.getOriginalDescription());
            else fdc.removeOriginalDescription();
        if (originalTitleAssigned)
            if (dc.getOriginalTitle() != null) fdc.setOriginalTitle(dc.getOriginalTitle());
            else fdc.removeOriginalTitle();
        if (spatialsAssigned)
            if (dc.getSpatials().size() > 0) fdc.setSpatials(dc.getSpatials());
            else fdc.removeSpatials();
        if (licenseDeAssigned)
            if (dc.getLicenseDE() != null) fdc.setLicenseDE(dc.getLicenseDE());
        if (licenseEnAssigned)
            if (dc.getLicenseEN() != null) fdc.setLicenseEN(dc.getLicenseEN());

//        if (xxXxAssigned)                 
//            if (dc.getXxXx().size() > 0) fdc.setXxXx(dc.getXxXx());
//            else fdc.removeXxXx();        
    }

    /**
     * Check if something is assigned.
     */
    public boolean hasAssignments() {
        return advertisedAssigned || contributorsAssigned || creatorsAssigned ||
                englishDescriptionAssigned || englishTitleAssigned || eventNumberAssigned ||
                isPartOfAssigned || typeAssigned || languageAssigned || originalDescriptionAssigned ||
                originalTitleAssigned || spatialsAssigned;
    }

    /**
     * Check if another Dublin Core differs in at least one of the assigned fields.
     */
    public boolean differsAssigned(ETHZDublinCore fdc) {
        return !(!advertisedAssigned || eq(fdc.isAdvertised(), dc.isAdvertised())) ||
                !(!contributorsAssigned || eq(fdc.getContributors(), dc.getContributors())) ||
                !(!creatorsAssigned || eq(fdc.getCreators(), dc.getCreators())) ||
                !(!englishDescriptionAssigned || eq(fdc.getEnglishDescription(), dc.getEnglishDescription())) ||
                !(!englishTitleAssigned || eq(fdc.getEnglishTitle(), dc.getEnglishTitle())) ||
                !(!eventNumberAssigned || eq(fdc.getEventNumber(), dc.getEventNumber())) ||
                !(!isPartOfAssigned || eq(fdc.getIsPartOf(), dc.getIsPartOf())) ||
                !(!typeAssigned || eq(fdc.getTypeCombined(), dc.getTypeCombined())) ||
                !(!languageAssigned || eq(fdc.getLanguage(), dc.getLanguage())) ||
                !(!originalDescriptionAssigned || (eq(fdc.getOriginalDescription(), dc.getOriginalDescription()))) ||
                !(!originalTitleAssigned || eq(fdc.getOriginalTitle(), dc.getOriginalTitle())) ||
                !(!spatialsAssigned || eq(fdc.getSpatials(), dc.getSpatials())) ||
                !(!licenseDeAssigned || eq(fdc.getLicenseDE(), dc.getLicenseDE())) ||
                !(!licenseEnAssigned || eq(fdc.getLicenseEN(), dc.getLicenseEN()));
    }

    private boolean eq(Object a, Object b) {
        if (a == null ^ b == null) return false;
        if (a == b) return true;
        return a.equals(b);
    }

    private List<DublinCoreValue> toDCVList(List<Value> xs) {
        List<DublinCoreValue> xsn = new ArrayList<DublinCoreValue>(xs.size());
        for (Value x : xs) {
            if (x.getValue() != null) {
                xsn.add(new DublinCoreValue(x.getValue(), x.getLanguage()));
            }
        }
        return xsn;
    }

    private <A> List<A> compact(List<A> x) {
        return CollectionSupport.filter(x, new CollectionSupport.Predicate<A>() {
            public boolean evaluate(A object, int index) {
                return object != null;
            }
        });
    }

    public ETHZDublinCore getDublinCore() {
        return dc;
    }

    // Can't be modified for now, so no need for buffering
    public List<DublinCoreValue> getPublisher() {
        return dc.getPublisher();
    }

    // Can't be modified for now, so no need for buffering
    public List<DublinCoreValue> getRightsHolder() {
        return dc.getRightsHolder();
    }

    // Can't be modified for now, so no need for buffering
    public List<DublinCoreValue> getLicense() {
        return dc.get(DublinCore.PROPERTY_LICENSE);
    }

    public String getLicenseDE() {
        return dc.getLicenseDE();
    }

    public void setLicenseDE(String license) {
        if (license != null) dc.setLicenseDE(license);
        else dc.removeLicenseDE();
    }

    public String getLicenseEN() {
        return dc.getLicenseEN();
    }

    public void setLicenseEN(String license) {
        if (license != null) dc.setLicenseEN(license);
        else dc.removeLicenseEN();
    }

    public String getDCIdentifier() {
        return dc.getDCIdentifier();
    }

    public void setDCIdentifier(String id) {
        if (id != null) dc.setDCIdentifier(id);
        else dc.removeDCIdentifier();
    }

    public String getIsPartOf() {
        return dc.getIsPartOf();
    }

    public void setIsPartOf(String seriesID) {
        if (seriesID != null) dc.setIsPartOf(seriesID);
        else dc.removeIsPartOf();
    }

    public void setOriginalTitle(String title) {
        if (title != null) dc.setOriginalTitle(title);
        else dc.removeOriginalTitle();
    }

    public String getOriginalTitle() {
        return dc.getOriginalTitle();
    }

    public String getEnglishTitle() {
        return dc.getEnglishTitle();
    }

    public void setEnglishTitle(String title) {
        if (title != null) dc.setEnglishTitle(title);
        else dc.removeEnglishTitle();
    }

    public void setOriginalDescription(String description) {
        if (description != null) dc.setOriginalDescription(description);
        else dc.removeOriginalDescription();
    }

    public String getOriginalDescription() {
        return dc.getOriginalDescription();
    }

    public void setEnglishDescription(String description) {
        if (description != null) dc.setEnglishDescription(description);
        else dc.removeEnglishDescription();
    }

    public String getEnglishDescription() {
        return dc.getEnglishDescription();
    }

    public void setCreated(Date date) {
        if (date != null) dc.setCreated(date);
        else dc.removeCreated();
    }

    public void setCreated(DCMIPeriod period) {
        if (period != null) dc.setCreated(period.getStart(), period.getEnd());
        else dc.removeCreated();
    }

    public Temporal<?> getCreated() {
        return dc.getCreated();
    }

    public void setAvailable(Date date) {
        if (date != null) dc.setAvailable(date);
        else dc.removeAvailable();
    }

    public Date getAvailable() {
        return dc.getAvailable();
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setExtent(Long extent) {
        if (extent != null) dc.setExtent(extent);
        dc.removeExtent();
    }

    public Long getExtent() {
        return dc.getExtent();
    }

    public void setIssued(Date date) {
        if (date != null) dc.setIssued(date);
        else dc.removeIssued();
    }

    public Date getIssued() {
        return dc.getIssued();
    }

    public void setLanguage(String lang) {
        if (lang != null) dc.setLanguage(lang);
        else dc.removeLanguage();
    }

    public String getLanguage() {
        return dc.getLanguage();
    }

    public void setModified(Date date) {
        if (date != null) dc.setModified(date);
        else dc.removeModified();
    }

    public Date getModified() {
        return dc.getModified();
    }

    public List<Value> getSpatials() {
        return spatials;
    }

    public List<Value> getContributors() {
        if (!contributors.isEmpty())
            return contributors;
        else
            return new ArrayList<Value>();
    }

    public void setTemporal(Date from, Date to) {
        if (from != null && to != null) dc.setTemporal(from, to);
        else dc.removeTemporal();
    }

    public void setTemporal(Date dateTime) {
        if (dateTime != null) dc.setTemporal(dateTime);
        else dc.removeTemporal();
    }

    public Temporal<?> getTemporal() {
        return dc.getTemporal();
    }

    public void setType(String type) {
        if (type != null) {
            String t[] = type.split("/");
            dc.setType(StringSupport.camelCase(t[0], ' ', true),
                    StringSupport.camelCase(t[1], ' ', true));
        } else dc.removeType();
    }

    public String getType() {
        if (dc.getType() != null)
            return StringSupport.flattenCamelCase(dc.getType()[0], ' ', true) +
                    "/" +
                    StringSupport.flattenCamelCase(dc.getType()[1], ' ', true);
        else
            return null;
    }

    public void setValid(Date date) {
        if (date != null) dc.setValid(date);
        else dc.removeValid();
    }

    public Date getValid() {
        return dc.getValid();
    }

    public void setEventNumber(String number) {
        if (number != null) dc.setEventNumber(number);
        else dc.removeEventNumber();
    }

    public String getEventNumber() {
        return dc.getEventNumber();
    }

    public void setAdvertised(Boolean advertised) {
        dc.setAdvertised(advertised);
    }

    // Because it returns an object it must be prefixed with "get"
    public Boolean getAdvertised() {
        return dc.isAdvertised();
    }

    // --

    public boolean isIsPartOfAssigned() {
        return isPartOfAssigned;
    }

    public void setIsPartOfAssigned(boolean isPartOfAssigned) {
        this.isPartOfAssigned = isPartOfAssigned;
    }

    public boolean isEventNumberAssigned() {
        return eventNumberAssigned;
    }

    public void setEventNumberAssigned(boolean eventNumberAssigned) {
        this.eventNumberAssigned = eventNumberAssigned;
    }

    public boolean isTypeAssigned() {
        return typeAssigned;
    }

    public void setTypeAssigned(boolean typeAssigned) {
        this.typeAssigned = typeAssigned;
    }

    public boolean isOriginalTitleAssigned() {
        return originalTitleAssigned;
    }

    public void setOriginalTitleAssigned(boolean originalTitleAssigned) {
        this.originalTitleAssigned = originalTitleAssigned;
    }

    public boolean isEnglishTitleAssigned() {
        return englishTitleAssigned;
    }

    public void setEnglishTitleAssigned(boolean englishTitleAssigned) {
        this.englishTitleAssigned = englishTitleAssigned;
    }

    public boolean getCreatorsAssigned() {
        return creatorsAssigned;
    }

    public void setCreatorsAssigned(boolean creatorsAssigned) {
        this.creatorsAssigned = creatorsAssigned;
    }

    public boolean getSpatialsAssigned() {
        return spatialsAssigned;
    }

    public void setSpatialsAssigned(boolean spatialsAssigned) {
        this.spatialsAssigned = spatialsAssigned;
    }

    public boolean getContributorsAssigned() {
        return contributorsAssigned;
    }

    public void setContributorsAssigned(boolean contributorsAssigned) {
        this.contributorsAssigned = contributorsAssigned;
    }

    public boolean isOriginalDescriptionAssigned() {
        return originalDescriptionAssigned;
    }

    public void setOriginalDescriptionAssigned(boolean originalDescriptionAssigned) {
        this.originalDescriptionAssigned = originalDescriptionAssigned;
    }

    public boolean isEnglishDescriptionAssigned() {
        return englishDescriptionAssigned;
    }

    public void setEnglishDescriptionAssigned(boolean englishDescriptionAssigned) {
        this.englishDescriptionAssigned = englishDescriptionAssigned;
    }

    public boolean isLanguageAssigned() {
        return languageAssigned;
    }

    public void setLanguageAssigned(boolean languageAssigned) {
        this.languageAssigned = languageAssigned;
    }

    public boolean isAdvertisedAssigned() {
        return advertisedAssigned;
    }

    public void setAdvertisedAssigned(boolean advertisedAssigned) {
        this.advertisedAssigned = advertisedAssigned;
    }

    public boolean isLicenseDeAssigned() {
        return licenseDeAssigned;
    }

    public void setLicenseDeAssigned(boolean licenseDeAssigned) {
        this.licenseDeAssigned = licenseDeAssigned;
    }

    public boolean isLicenseEnAssigned() {
        return licenseEnAssigned;
    }

    public void setLicenseEnAssigned(boolean licenseEnAssigned) {
        this.licenseEnAssigned = licenseEnAssigned;
    }
}
