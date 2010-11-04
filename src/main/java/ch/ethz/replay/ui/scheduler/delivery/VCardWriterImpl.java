/*

 VCardWriterImpl.java
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

package ch.ethz.replay.ui.scheduler.delivery;

import ch.ethz.replay.core.api.common.MimeTypes;
import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import ch.ethz.replay.core.api.common.vcard.VCard;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

/**
 * Prints a {@link ch.ethz.replay.core.api.common.vcard.VCard} as described in
 * <a href="http://tools.ietf.org/html/rfc2426">RFC2426</a>.
 * <p/>
 * Currently only the some fields are supported.
 *
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
public class VCardWriterImpl implements VCardWriter {

    private static final String VCARD_ENCODING = "UTF-8";
    private static final String VCARD_CONTENT_TYPE = MimeTypes.TEXT.asString();

    public boolean write(VCard vcard, HttpServletResponse response) throws IOException {
        // Response header
        response.setCharacterEncoding(VCARD_ENCODING);
        response.setContentType(VCARD_CONTENT_TYPE);
        //
        PrintWriter p = new PrintWriter(response.getOutputStream());
        p.println("BEGIN:vcard");
        printSimple(p, "FN", vcard.getFormattedName());
        printList(p, "N", ";", true, vcard.getFamilyName(), vcard.getGivenName(), vcard.getAdditionalNames(),
                vcard.getHonorificPrefixes(), vcard.getHonorificSuffixes()
        );
        printList(p, "NICKNAME", ",", false, vcard.getNicknames());
        printEmail(p, vcard.getEmailAddresses());
        printSimple(p, "ORG", vcard.getOrganization());
        printSimple(p, "TITLE", vcard.getTitle());
        printSimple(p, "ROLE", vcard.getRole());
        p.println("END:vcard");
        p.flush();
        return true;
    }

    private void printEmail(PrintWriter p, Collection<EmailAddress> emailAddresses) {
        if (emailAddresses != null) {
            for (EmailAddress e : emailAddresses) {
                p.print("EMAIL");
                String[] type = {e.getType(), e.isPreferred() ? "pref" : null};
                String t = joinNonNull(type, ',');
                if (t.length() > 0) {
                    p.print(";TYPE=");
                    p.print(t);
                }
                p.print(":");
                p.println(e.getAddress());
            }
        }
    }

    /**
     * Joins only non null elements of <code>s</code>.
     *
     * @return the joined string or ""
     */
    private String joinNonNull(String[] s, char sep) {
        StringBuilder b = new StringBuilder();
        if (s != null) {
            for (String str : s) {
                if (str != null) {
                    if (b.length() > 0) b.append(sep);
                    b.append(str);
                }
            }
        }
        return b.toString();
    }

    private void printSimple(PrintWriter p, String fieldName, String value) {
        if (value != null) {
            p.print(fieldName.toUpperCase());
            p.print(":");
            p.println(value);
        }
    }

    private void printList(PrintWriter p, String fieldName, String separator, boolean all, Collection<String> values) {
        if (values != null) {
            StringBuilder b = new StringBuilder();
            for (String v : values) {
                if (v != null || all) {
                    if (b.length() > 0) b.append(separator);
                    if (v != null) b.append(v);
                }
            }
            if (b.length() > 0) {
                p.print(fieldName.toUpperCase());
                p.print(":");
                p.println(b.toString());
            }
        }
    }

    private void printList(PrintWriter p, String fieldName, String separator, boolean all, String... values) {
        if (values != null) printList(p, fieldName, separator, all, Arrays.asList(values));
    }
}
