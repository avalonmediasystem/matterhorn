<%--

 icalendar.jsp
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

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="prolog.jspf" %>

<html>
<head>
    <meta name="decorator" content="main"/>
    <style type="text/css">
        table {
            width: 100%;
        }
    </style>
</head>

<%-- ############################################################ --%>
<%-- BODY                                                         --%>
<%-- ############################################################ --%>

<body id="p-icalendar">

<div id="result-pane">
    <%-- production calendar --%>
    <table class="alternating-rows hover">
        <caption>${i18n['label.calendar.table.production']}</caption>
        <thead>
            <tr>
                <th>${i18n['label.calendar.col.name']}</th>
                <th>${i18n['label.calendar.col.download']}</th>
                <th>${i18n['label.calendar.col.url']}</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <replay:link action="calendar/production?plain=true"
                                 target="_blank">${i18n['label.production-calendar']}</replay:link>
                </td>
                <td>
                    <replay:link action="calendar/production">${i18n['label.download']}</replay:link>
                </td>
                <td>
                <span class="small">
                    http://${pageContext.request.serverName}:${pageContext.request.serverPort}${C}/calendar/production
                </span>
                </td>
            </tr>
        </tbody>
    </table>

    <hr class="spacer"/>

    <%-- room calendars --%>
    <table class="alternating-rows hover">
        <caption>${i18n['label.calendar.table.rooms']}</caption>
        <thead>
            <tr>
                <th>${i18n['label.calendar.col.room']}</th>
                <th>${i18n['label.calendar.col.download']}</th>
                <th>${i18n['label.calendar.col.url']}</th>
            </tr>
        </thead>

        <tbody>
            <%--@elvariable id="locations" type="java.util.List<ch.ethz.replay.ui.scheduler.Location>"--%>
            <c:forEach var="location" items="${locations}" varStatus="s">
                <tr>
                    <td>
                        <replay:link action="calendar/recorder?id=${replay:urlEncode(location.name)}&amp;plain=true"
                                     title="${i18n['fragment.view-cal-in-browser']}"
                                     target="_blank"><c:out value="${location.name}"/></replay:link>
                    </td>
                    <td><replay:link action="calendar/recorder?id=${replay:urlEncode(location.name)}"
                                     title="Download calendar">${i18n['label.download']}</replay:link></td>
                    <td>
                <span class="small">
                    http://${pageContext.request.serverName}:${pageContext.request.serverPort}${C}/calendar/recorder?id=${replay:urlEncode(location.name)}
                </span>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

</body>
</html>