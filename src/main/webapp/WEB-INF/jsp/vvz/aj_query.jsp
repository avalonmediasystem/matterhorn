<%--

 personsuggestions.jsp
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Oct 24, 2008

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

<%--
Generic suggestions template suitable for simple use cases.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="../prolog.jspf" %>

<%-- Ajax fragment --%>

<c:set var="sep">
    <span class="sep">&diams;</span>
</c:set>

<ul>
  <c:forEach var="g" items="${groups}">
    <li class="title"><a href="${C}/vvz/vvz/select?lv_id=${g._2}">${g._1}</a>
        <c:set var="lvTypCode">lehrveranstaltung.typ.${g._3.code}</c:set>
        <span style="font-size: smaller; color: gray">(${i18n[lvTypCode]})</span></li>
    <ul>
        <c:set var="pre" value="false"/>
        <c:forEach var="name" items="${g._4}">
            <li><c:if test="${pre}">${sep}</c:if> <c:out value="${name}"/></li>
            <c:set var="pre" value="true"/>
        </c:forEach>
    </ul>
    <ul>
        <c:set var="pre" value="false"/>
        <c:forEach var="loc" items="${g._5}">
            <li><c:if test="${pre}">${sep}</c:if> <c:out value="${loc}"/></li>
            <c:set var="pre" value="true"/>
        </c:forEach>
    </ul>
      <%--
    <li><a href="${C}/">[${s.lerneinheitId}]</a> <i>${s.titel}</i> <span>${s.name}, ${s.vorname}</span> <span>${s.location}</span></li>
    --%>
  </c:forEach>
</ul>
