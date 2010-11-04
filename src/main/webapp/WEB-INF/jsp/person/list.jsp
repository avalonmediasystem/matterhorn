<%--

 list.jsp
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
<%@ include file="../prolog.jspf" %>

<html>
<head>
    <meta name="decorator" content="main"/>
</head>

<%-- ############################################################ --%>
<%-- BODY                                                         --%>
<%-- ############################################################ --%>

<body id="p-person-list">

<div class="menu-bar sub">
    <ul>
        <li><a href="${C}/editperson">${i18n['label.new-vcard']}</a></li>
    </ul>
</div>

<div id="result-pane">
<table class="hover alternating-rows">
    <caption>${i18n['label.overview']}</caption>
    <tbody>
        <c:forEach var="person" items="${persons}">
            <tr>
                <td><c:out value="${person.honorificPrefixes}"/></td>
                <td><c:choose>
                    <c:when test="${person.modifiable}">
                        <a href="${C}/editperson?id=${person.id}" title="Edit">
                            <c:out value="${person.familyName}"/>
                        </a>
                    </c:when>
                    <c:otherwise><c:out value="${person.familyName}"/></c:otherwise>
                </c:choose>

                </td>
                <td><c:out value="${person.givenName}"/></td>
                <td><c:out value="${person.preferredEmailAddress.address}"/></td>
                <td>
                    <c:if test="${person.modifiable}">
                        <replay:button type="edit" action="/editperson?id=${person.id}"/>
                        <replay:button type="remove" action="/removeperson?id=${person.id}"/>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
</div>

</body>
</html>