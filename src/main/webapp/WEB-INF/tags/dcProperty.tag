<%--

 dcProperty.tag
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

<%@ tag display-name="dcProperty" pageEncoding="utf-8" language="java" description="A Dublin Core property" %>

<%@ include file="../jsp/prolog.jspf" %>

<%@ attribute name="test" type="java.lang.Boolean" description="If the condition is true the field is output." %>
<%@ attribute name="help" description="Help text for this field" %>

<c:if test="${empty test || test}">
<tbody>
    <jsp:doBody/>
    <c:if test="${!empty help}">
        <tr><td colspan="2"></td><td><div class="help">${help}</div></td></tr>
    </c:if>
</tbody>
</c:if>
