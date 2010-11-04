/*

 TestHelper.java
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created May 20, 2008

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

package ch.ethz.replay.ui.scheduler.web;

import ch.ethz.replay.ui.scheduler.*;
import ch.ethz.replay.ui.scheduler.impl.RecordingImpl;

import java.util.*;

/**
 * Helps in creating test records.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class TestHelper {

    private List<DeviceType> deviceTypes = new ArrayList<DeviceType>();

    private List<Location> locations = new ArrayList<Location>();

    private List<Person> persons = new ArrayList<Person>();

    private Schedule schedule;

    /**
     * New TestHelper.
     */
    public TestHelper(Schedule schedule) {
        this.schedule = schedule;
        deviceTypes.add(schedule.getOrCreateDeviceType("presenter"));
        deviceTypes.add(schedule.getOrCreateDeviceType("presentation"));
        deviceTypes.add(schedule.getOrCreateDeviceType("audio"));
        //
        locations.add(schedule.getOrCreateLocation("HGB2"));
        locations.add(schedule.getOrCreateLocation("AM1"));
        locations.add(schedule.getOrCreateLocation("GLS"));
        //
        for (int i = 0; i < 30; i++) {
            Person p;
            do {
                p = createRandomPerson();
            } while (persons.contains(p));
            persons.add(p);
        }
    }

    /**
     * Returns a set of randomly, pool selected devices.
     */
    Set<DeviceType> getRandomDevices() {
        List devices = new ArrayList(deviceTypes);
        Collections.shuffle(devices);
        return new HashSet<DeviceType>(devices.subList(0, 1 + (int) (Math.random() * (devices.size() - 1))));
    }

    /**
     * Creates a random person.
     */
    Person createRandomPerson() {
        String[] givenNames = {"Klaus", "Peter", "Olaf", "Karl", "Hans", "Albert", "Adalbert", "Max", "Kevin", "Jean",
                "Michelle", "Sandra", "Karla", "Charlotte", "Cornelia", "Chris", "Craig"};
        String[] familyNames = {"Pan", "Mayer", "Huerlimann", "Olsson", "Eimer", "Tomson", "Leger", "Frisch", "Wilson",
                "Wood"};
        String[] emailDomains = {"gmx.de", "voila.fr", "ethz.ch"};
        String givenName = selectRandomly(givenNames);
        String familyName = selectRandomly(familyNames);
        Person person = schedule.newPerson(givenName, familyName,
                givenName + "." + familyName + "@" + selectRandomly(emailDomains));
        return person;
    }

    /**
     * Create a random recording with everything necessary set.
     */
    public Recording createRandomRecording() {
        Recording rec = new RecordingImpl(); // todo use of constructor is prohibited
        long now = System.currentTimeMillis();
        long startOffset = Math.round((Math.random() - 0.5) * 1000 * 60 * 60);
        long length = 1000 * 60 * 30 + Math.round(Math.random() * 1000 * 60 * 60);
        rec.setStartDate(new Date(now + startOffset));
        rec.setEndDate(new Date(rec.getStartDate().getTime() + length));
        rec.setLocation(selectRandomly(locations));
        for (int i = 0; i < (Math.random() * 3) + 1; i++) {
            rec.getContactPersons().add(createRandomPerson());
        }
        for (DeviceType d : getRandomDevices()) {
            rec.addDevice(d);
        }
        return rec;
    }

    /**
     * Selects randomly an element from the given array.
     */
    public static <T> T selectRandomly(T[] objects) {
        return objects[(int) (Math.random() * objects.length)];
    }

    /**
     * Selects randomly an element from the given array.
     */
    public static <T> T selectRandomly(List<T> objects) {
        return objects.get((int) (Math.random() * objects.size()));
    }
}
