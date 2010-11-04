<%--

 dublincore.jsp
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
    <!-- ${i18n['init']} -->
    <meta name="decorator" content="main"/>
    <replay:library name="jquery"/>
    <replay:library name="jquery.consolelog"/>
    <replay:library name="jquery.metadata.min"/>
    <replay:library name="jquery.metaselector"/>
    <replay:library name="jquery.sbox"/>
    <replay:library name="jquery.elastic-1.6.2"/>
    <replay:library name="jquery.tablednd-0.5"/>

    <script type="text/javascript">
        jQuery(function($) {
            // Give focus to first not disabled text input
            //$(".lform input[type='text']:not(:disabled):first").focus();

            // -------------------------------------------
            // Layouts and element enhancements
            // -------------------------------------------

            $("textarea.autogrow").elastic();

            //$("*[readonly]").css({color: "#999"});

            $("#toggle-help").click(function() {
                $("div.help").slideToggle('normal');
            });

            $("table#form").tableDnD({
                onDrop: function(table, row) {
                    // Get the row parent (which is a tbody)
                    var $p = $(row).parent();
                    console.log($p[0]);
                    // Correct the indixes of the input elements
                    var index = 0;
                    $("tr", $p).each(function() {
                        $(":input", this).each(function() {
                            var $input = $(this);
                            $input.attr("name", $input.attr("name").replace(/\[\d+\]/, "[" + index + "]"));
                        });
                        index += 1;
                    });
                    // Keep single label on top
                    $(".drag-keep-top", $p).parent().fadeOut(function() {
                        $(this).appendTo($("tr:first td:eq(1)", $p)).fadeIn();
                    });
                },
                dragHandle: "drag-handle"
            });

            // IDEAS
            // Make .group divs sortable and store their order in a cookie
        });

    </script>
</head>

<%-- ============================================================ --%>
<%-- BODY                                                         --%>
<%-- ============================================================ --%>

<body id="p-dublincore" pagecss="true">

<replay:fragment name="header">
    <h3>
        ${i18n[replay:concat("metadata-title.", form.title)]}
    </h3>
    <span style="float:right;">
        <label>Show help</label>
        <input id="toggle-help" type="checkbox"/>
    </span>
</replay:fragment>

<div class="lform">
    <%--
      Path is form.dc.dublinCore with dc = ETHZDublinCoreCommand; dublinCore = ETHZDublinCore
      Note that validation takes place on the ETHZDublinCore object, so be sure to descend the path
    --%>
    <form:form commandName="form.dc" action="save">
        <form:errors path="*"/>

        <table id="form" class="form">
                <%-- Identifier --%>
            <tf:dcProperty test="${form.concrete}" help="Identifier">
                <tf:dcPropertyElem>
                    <jsp:attribute name="label">Identifier</jsp:attribute>
                    <jsp:attribute name="input"><form:input path="DCIdentifier" readonly="${form.episode ? true : false}"/></jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Is Part Of --%>
            <tf:dcProperty test="${form.episode}" help="The series ID this episode is part of">
                <tf:dcPropertyElem checkboxPath="isPartOfAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Is part of</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="isPartOf"/>
                        <form:errors path="dublinCore.isPartOf"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Event number --%>
            <tf:dcProperty help="The code of the Lerneinheit">
                <tf:dcPropertyElem checkboxPath="eventNumberAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">ETHZ Event number</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="eventNumber"/>
                        <form:errors path="dublinCore.eventNumber"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Type --%>
            <tf:dcProperty>
                <tf:dcPropertyElem checkboxPath="typeAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Type</jsp:attribute>
                    <jsp:attribute name="input">
                        <tf:dcTypeSelector path="type"/>
                        <form:errors path="dublinCore.type"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Title --%>
            <tf:dcProperty help="The original title and its english translation">
                <tf:dcPropertyElem checkboxPath="originalTitleAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Title</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="originalTitle"/>
                        <form:errors path="dublinCore.originalTitle"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
                <tf:dcPropertyElem checkboxPath="englishTitleAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Title (en)</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="englishTitle"/>
                        <form:errors path="dublinCore.englishTitle"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Creator --%>
            <tf:dcProperty help="surname, name">
                <c:forEach items="${form.dc.creators}" varStatus="s">
                    <tf:dcPropertyElem checkboxPath="creatorsAssigned" showCheckbox="${form.template}"
                                       index="${s.index}">
                        <jsp:attribute name="label"><span class="drag-keep-top">Creator</span></jsp:attribute>
                        <jsp:attribute name="input">
                            <form:input path="creators[${s.index}]"/>
                            <%-- An index out of bounds may occur here --%>
                            <c:catch><form:errors path="dublinCore.creators[${s.index}]"/></c:catch>
                        </jsp:attribute>
                    </tf:dcPropertyElem>
                </c:forEach>
            </tf:dcProperty>

                <%-- Contributors --%>
            <tf:dcProperty help="Departement">
                <c:forEach items="${form.dc.contributors}" varStatus="s">
                    <tf:dcPropertyElem checkboxPath="contributorsAssigned" showCheckbox="${form.template}">
                        <jsp:attribute name="label">Contributor
                            <tf:dcLanguageSelect path="contributors[${s.index}].language"/>
                        </jsp:attribute>
                        <jsp:attribute name="input">
                            <form:input path="contributors[${s.index}].value"/>
                            <%-- An index out of bounds may occur here --%>
                            <c:catch><form:errors path="dublinCore.contributors[${s.index}].value"/></c:catch>
                        </jsp:attribute>
                    </tf:dcPropertyElem>
                </c:forEach>
            </tf:dcProperty>

                <%-- Created --%>
            <tf:dcProperty test="${form.concrete}">
                <tf:dcPropertyElem>
                    <jsp:attribute name="label">Created</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="created"/>
                        <form:errors path="dublinCore.created"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Issued --%>
            <tf:dcProperty test="${form.concrete}">
                <tf:dcPropertyElem>
                    <jsp:attribute name="label">Issued</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="issued"/>
                        <form:errors path="dublinCore.issued"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Description --%>
            <tf:dcProperty help="The description and its english translation">
                <tf:dcPropertyElem checkboxPath="originalDescriptionAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Description</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:textarea path="originalDescription" cssClass="autogrow"/>
                        <form:errors path="dublinCore.originalDescription"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
                <tf:dcPropertyElem checkboxPath="englishDescriptionAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Description (en)</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:textarea path="englishDescription" cssClass="autogrow"/>
                        <form:errors path="dublinCore.englishDescription"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Language --%>
            <tf:dcProperty>
                <tf:dcPropertyElem checkboxPath="languageAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Language</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="language"/>
                        <form:errors path="dublinCore.language"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Spatial --%>
            <tf:dcProperty>
                <c:forEach items="${form.dc.spatials}" varStatus="s">
                    <tf:dcPropertyElem checkboxPath="spatialsAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">
                        Spatial <tf:dcLanguageSelect path="spatials[${s.index}].language"/>
                    </jsp:attribute>
                        <jsp:attribute name="input">
                            <form:input path="spatials[${s.index}].value"/>
                            <%-- An index out of bounds may occur here --%>
                            <c:catch><form:errors path="dublinCore.spatials[${s.index}].value"/></c:catch>
                        </jsp:attribute>
                    </tf:dcPropertyElem>
                </c:forEach>
            </tf:dcProperty>

                <%-- Temporal --%>
            <tf:dcProperty test="${form.concrete}">
                <tf:dcPropertyElem>
                    <jsp:attribute name="label">Temporal</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="temporal"/>
                        <form:errors path="dublinCore.temporal"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- License --%>
            <tf:dcProperty help="The license in german and english">
                <tf:dcPropertyElem checkboxPath="licenseDeAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">License (de)</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="licenseDE"/>
                        <form:errors path="dublinCore.licenseDE"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
                <tf:dcPropertyElem checkboxPath="licenseEnAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">License (en)</jsp:attribute>
                    <jsp:attribute name="input">
                        <form:input path="licenseEN"/>
                        <form:errors path="dublinCore.licenseEN"/>
                    </jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

                <%-- Publisher --%>
            <tf:dcProperty test="${form.concrete}">
                <c:forEach items="${form.dc.publisher}" varStatus="s">
                    <tf:dcPropertyElem>
                    <jsp:attribute name="label">
                        <spring:bind path="publisher[${s.index}].language">
                            Publisher (<c:out value="${status.value}"/>)
                        </spring:bind>
                    </jsp:attribute>
                        <jsp:attribute name="input"><form:input path="publisher[${s.index}].value" readonly="true"/></jsp:attribute>
                    </tf:dcPropertyElem>
                </c:forEach>
            </tf:dcProperty>

                <%-- License --%>
            <%--<tf:dcProperty test="${form.concrete}">--%>
                <%--<c:forEach items="${form.dc.license}" varStatus="s">--%>
                    <%--<tf:dcPropertyElem>--%>
                    <%--<jsp:attribute name="label">--%>
                        <%--<spring:bind path="license[${s.index}].language">--%>
                            <%--License (<c:out value="${status.value}"/>)--%>
                        <%--</spring:bind>--%>
                    <%--</jsp:attribute>--%>
                        <%--<jsp:attribute name="input"><form:input path="license[${s.index}].value" readonly="true"/></jsp:attribute>--%>
                    <%--</tf:dcPropertyElem>--%>
                <%--</c:forEach>--%>
            <%--</tf:dcProperty>--%>

                <%-- Rights holder --%>
            <tf:dcProperty test="${form.concrete}">
                <c:forEach items="${form.dc.rightsHolder}" varStatus="s">
                    <tf:dcPropertyElem>
                    <jsp:attribute name="label">
                        <spring:bind path="rightsHolder[${s.index}].language">
                            Rights holder (<c:out value="${status.value}"/>)
                        </spring:bind>
                    </jsp:attribute>
                        <jsp:attribute name="input"><form:input path="rightsHolder[${s.index}].value" readonly="true"/></jsp:attribute>
                    </tf:dcPropertyElem>
                </c:forEach>
            </tf:dcProperty>

                <%-- Advertised --%>
            <tf:dcProperty help="Can this item be queried?">
                <tf:dcPropertyElem checkboxPath="advertisedAssigned" showCheckbox="${form.template}">
                    <jsp:attribute name="label">Advertised</jsp:attribute>
                    <jsp:attribute name="input"><form:checkbox path="advertised"/></jsp:attribute>
                </tf:dcPropertyElem>
            </tf:dcProperty>

            <tr>
                <td colspan="2"></td>
                <td>
                    <div style="width: 400px; text-align: right;">
                        <button onclick="window.location.href='cancel'; return false;">Cancel</button>
                        <input type="submit" value="Save"/>
                    </div>
                </td>
                <td colspan="2"></td>
            </tr>

        </table>
    </form:form>
</div>

</body>
</html>