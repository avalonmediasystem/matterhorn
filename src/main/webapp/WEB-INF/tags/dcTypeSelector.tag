<%--

 dcTypeSelector.tag
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Jan 27, 2009

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

--%>

<%@ tag display-name="dcTypeSelector" pageEncoding="utf-8" language="java" %>
<%@ tag import="java.util.UUID" %>

<%@ include file="../jsp/prolog.jspf" %>

<%@ attribute name="path" required="true" %>

<%-- # --%>
<c:set var="sId1" value="<%= UUID.randomUUID().toString() %>"/>
<c:set var="sId2" value="<%= UUID.randomUUID().toString() %>"/>
<c:set var="hId" value="<%= UUID.randomUUID().toString() %>"/>

<script type="text/javascript">
    jQuery(function($) {
        // Get input elements
        var $s1 = $('#${sId1}');
        var $s2 = $('#${sId2}');
        var $hidden = $('#${hId}');

        $s1.css('width', 'auto').change(function() {
            type[0] = $(this).val();
            update();
        });
        $s2.css('width', 'auto').change(function() {
            type[1] = $(this).val();
            update();
        });

        var type = $hidden.val().split(/\//);
        $s1.sboxLink($s2, {select: $.makeArray(type)});

        function update() {
            var value = type.join('/');
            if (value.indexOf('-') == 0) value = '';
            $hidden.val(value);
        }
    });
</script>

<form:hidden path="${path}" id="${hId}"/>

<select id="${sId1}">
    <option value="-">-- Select --</option>
    <option>Moving Image</option>
    <option>Sound</option>
    <option>Event</option>
</select>

<select id="${sId2}">
    <optgroup label="Moving Image" class="{depends: 'Moving Image'}">
        <option value="-">-- Select --</option>
        <option>Animation</option>
        <option>Biography</option>
        <option>Discussion</option>
        <option>Documentation</option>
        <option>Farewell Lecture</option>
        <option>Film</option>
        <option>Inaugural Lecture</option>
        <option>Interview</option>
        <option>Lecture Recording</option>
        <option>Magazine</option>
        <option>Music</option>
        <option>News</option>
        <option>Speech</option>
        <option>Sports</option>
        <option>Talk</option>
    </optgroup>

    <optgroup label="Sound" class="{depends: 'Sound'}">
        <option value="-">-- Select --</option>
        <option>Biography</option>
        <option>Discussion</option>
        <option>Documentation</option>
        <option>Farewell Lecture</option>
        <option>Inaugural Lecture</option>
        <option>Interview</option>
        <option>Lecture Recording</option>
        <option>Magazine</option>
        <option>Music</option>
        <option>News</option>
        <option>Speech</option>
        <option>Sports</option>
        <option>Talk</option>
    </optgroup>

    <optgroup label="Event" class="{depends: 'Event'}">
        <option value="-">-- Select --</option>
        <option>Lecture</option>
        <option>Conference</option>
        <option>Presentation</option>
        <option>Workshop</option>
        <option>Award</option>
        <option>Seminar</option>
        <option>Meeting</option>
        <option>Exihibition</option>
        <option>Performance</option>
        <option>Forum</option>
    </optgroup>

    <optgroup label="-" class="{depends: '-'}">
        <option value="-">-- ? --</option>
    </optgroup>
</select>