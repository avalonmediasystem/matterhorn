<%--

 recordingseries.jsp
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Dec 02, 2008

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

<%-- ============================================================ --%>
<%-- BODY                                                         --%>
<%-- ============================================================ --%>

<body id="p-recordingseries">

<c:set var="dummy">${i18n}</c:set>

<div id="result-pane">
    <table class="hover alternating-rows">
        <caption>Available series</caption>
        <thead>
        <tr>
            <th>Title</th>
            <th class="cac">Rec. bygone</th>
            <th class="cac">Rec. total</th>
            <th></th>
        </tr>
        </thead>
        <tbody>

        <%--@elvariable id="series" type="java.util.List<ch.ethz.replay.ui.scheduler.RecordingSeries>"--%>
        <c:forEach var="r" items="${series}">
            <tr>
                <td><c:out value="${r.title}"/></td>
                <td class="cac"><c:out value="${r.bygoneCount}"/></td>
                <td class="cac"><c:out value="${fn:length(r.recordings)}"/></td>
                <td class="car">
                    <c:if test="${r.bygone or fn:length(r.recordings) == 0}">
                        <replay:button type="remove" action="remove?id=${r.id}"/>
                    </c:if>
                    <replay:button type="edit" action="edit?id=${r.id}"/>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <hr class="spacer">

    <div style="text-align: right; padding: 5px;">
        <button onclick="window.location.href='create';">Create new series</button>
    </div>
</div>

</body>
</html>