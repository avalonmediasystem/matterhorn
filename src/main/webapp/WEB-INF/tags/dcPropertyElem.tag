<%--

 dcPropertyElem.tag
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Oct 16, 2009

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

<%@ tag display-name="dcPropertyElem" pageEncoding="utf-8" language="java" description="A Dublin Core property element" %>

<%@ include file="../jsp/prolog.jspf" %>

<%@ attribute name="label" fragment="true" required="true" description="The label to display" %>
<%@ attribute name="input" fragment="true" required="true" description="The input element" %>
<%@ attribute name="index" type="java.lang.Integer" description="Index of the element starting at 0" %>
<%@ attribute name="mandatory" type="java.lang.Boolean" description="Defaults to 'not mandatory'" %>
<%@ attribute name="disableSort" type="java.lang.Boolean" description="If true, disable sorting by dragging" %>
<%@ attribute name="checkboxPath" description="Path the the checkbox property" %>
<%@ attribute name="showCheckbox" description="Display the checkbox if true" %>

<tr${replay:createConditionalAttribute(disableSort, "class", "nodrag")}>
    <td class="drag-handle"><c:if test="${!empty index}">#</c:if></td>
    <td class="label"><c:if test="${!(index gt 0)}"><span class="label${mandatory ? '' : ''}"><jsp:invoke fragment="label"/></span></c:if></td>
    <td class="input"><jsp:invoke fragment="input"/></td>
    <td class="plusminus"><%-- todo add/remove --%></td>
    <td class="tmpl"><c:if test="${!(index gt 0) && showCheckbox}">
        <form:checkbox path="${checkboxPath}" title="Check to assign to Episode" cssClass="tmpl-assigned"/>
    </c:if></td>
</tr>
