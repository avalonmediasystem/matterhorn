<%--

 recording.jsp
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
    <!-- ${i18n} -->
    <meta name="decorator" content="main"/>

    <replay:library name="datepicker"/>

    <%-- ============================================================ --%>
    <%-- JAVASCRIPT                                                   --%>
    <%-- ============================================================ --%>

    <script type="text/javascript">

        var personQueryElem;
        var personQueryDropDown;
        var selectedPersonsElem;
        var locationElem;
        var locationDropDown;

        function removePerson(id) {
            $('_p-' + id).remove();
            if (selectedPersonsElem.empty()) selectedPersonsElem.hide();
        }

        function addPersonToList(person) {
            // remove the .informal sections
            var informal = person.select('.informal');
            if (informal != null) informal.each(function(e) {
                e.remove()
            });
            // determine person id
            var personId = person.identify().substring('_p-'.length);
            // inject remove button
            person.insert(createPersonRemoveLink(personId));
            // inject hidden form field
            person.insert(createPersonHiddenField(personId));
            person.addClassName('person-label');
            selectedPersonsElem.insert(person);
            selectedPersonsElem.show();
            $('person-autocomplete').value = '';
        }

        function createPersonRemoveLink(id) {
            return ' <a href="#" class="img-button" onclick="removePerson(\'' + id + '\');return false">' +
                   '<img src="${S}/img/remove-small.gif"/></a>';
        }

        function createPersonHiddenField(id) {
            return '<input type="hidden" name="contactPersons" value="' + id + '"/>';
        }

        function createSelectedPersonList() {
        <%-- Person list --%>
        <c:forEach var="person" items="${recording.contactPersons}">
            selectedPersonsElem.insert('<li id="_p-${shelve[person]}" class="person-label">${fn:escapeXml(person.formattedName)} ' +
                                       createPersonRemoveLink('${shelve[person]}') +
                                       createPersonHiddenField('${shelve[person]}') + '</li>');
        </c:forEach>
            if (!selectedPersonsElem.empty()) selectedPersonsElem.show();
        }

        function setInspectSeriesLink(id) {
            if (id == undefined) {
                id = $F('seriesSelect');
            } else {
                $('seriesSelect').setValue(id);
            }
            if (id != -1) {
                $('inspectSeriesLink').writeAttribute('onclick', "submitForm({id: " + id + "}, 'inspectseries')");
            } else {
                $('inspectSeriesLink').writeAttribute('onclick', 'alert("Please choose a series first")');
            }
        }

        /**
         * Automatically called by the surrounding template.
         */
        function initPage() {
        <%--<c:if test="${empty recform.excludedProperties.contactPersons}">--%>
            <%--selectedPersonsElem = $('selected-persons');--%>
            <%--createSelectedPersonList();--%>
        <%--</c:if>--%>
            
        <c:if test="${empty recform.excludedProperties.seriesDublinCore}">
            setInspectSeriesLink(${seriesId});
        </c:if>

        <c:if test="${empty recform.excludedProperties['startDate']}">
            $('startDate').observe('datepicker:change', function() {
                if ($('endDate').value.blank()) {
                    $('endDate').value = $('startDate').value;
                    $('endDate').value = $('startDate').value;
                }
            })
        </c:if>

            // autocompletion fields
        <c:if test="${empty recform.excludedProperties['location']}">
            new Ajax.Autocompleter("location-autocomplete", "location-suggestions", "${C}/locationsuggest", {
                paramName: "l",
                minChars: 1
            });
        </c:if>
        }
    </script>
</head>

<%-- ============================================================ --%>
<%-- BODY                                                         --%>
<%-- ============================================================ --%>

<%--@elvariable id="recording" type="ch.ethz.replay.ui.scheduler.Recording"--%>
<body id="p-recording">

<div id="form-panel">

    <c:set var="form" value="${recform}"/>

    <%-- edit recording --%>
    <form:form commandName="recform" action="${C}/recording/save" id="form">

        <script type="text/javascript">var f = $('form')</script>

        <table class="form">
            <c:if test="${empty recform.excludedProperties['dublinCore']}">
                <tr>
                    <td><span class="label">Title</span></td>
                    <td>
                        <c:choose>
                            <c:when test="${!empty recform.title}"><c:out value="${recform.title}"/></c:when>
                            <c:otherwise><button onclick="submitForm({}, 'editdublincore')">Edit metadata</button></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <%-- Metadata --%>
                <c:if test="${!empty recform.title}">
                    <tr>
                        <td></td>
                        <td>
                            <button onclick="submitForm({}, 'editdublincore')">Edit metadata</button>
                        </td>
                    </tr>
                </c:if>
            </c:if>

            <c:if test="${empty recform.excludedProperties['location']}">
                <tr>
                    <td><span class="label">Location</span></td>
                    <td>
                        <form:input path="location" id="location-autocomplete"/>
                        <div id="location-suggestions" class="suggestions"></div>
                        <br><form:errors path="location" cssClass="error"/>
                    </td>
                </tr>
            </c:if>
            <c:if test="${empty form.excludedProperties['startDate']}">
                <tr>
                    <td><span class="label">Starts</span></td>
                    <td>
                        <form:input path="startDate" cssClass="input-date"/>
                        <br><form:errors path="startDate" cssClass="error"/>
                    </td>
                </tr>
            </c:if>
            <c:if test="${empty form.excludedProperties['endDate']}">
                <tr>
                    <td><span class="label">Ends</span></td>
                    <td>
                        <form:input path="endDate" cssClass="input-date"/>
                        <br><form:errors path="endDate" cssClass="error"/>
                    </td>
                </tr>
            </c:if>
            <c:if test="${empty form.excludedProperties['device']}">
                <tr>
                    <td><span class="label">Recording type</span></td>
                    <td>
                        <form:checkboxes path="devices" items="${form.availableDeviceTypes}" itemLabel="name" itemValue="id"/>
                        <br><form:errors path="devices" cssClass="error"/>
                    </td>
                </tr>
            </c:if>
            <%-- This feature is currently not supported --%>
            <%--<c:if test="${empty form.excludedProperties['contactPersons']}">--%>
                <%--<tr>--%>
                    <%--<td>Contact persons</td>--%>
                    <%--<td>--%>
                        <%--<input type="hidden" name="contactPersons" value=""/>--%>
                        <%--<ul id="selected-persons" class="select" style="display:none;"></ul>--%>
                        <%--<input id="person-autocomplete" type="text"/>--%>
                        <%--<replay:button type="create" action="onclick:submitForm({}, '${C}/recording/createperson')"/>--%>
                        <%--<div id="person-suggestions" class="suggestions"></div>--%>
                    <%--</td>--%>
                <%--</tr>--%>
            <%--</c:if>--%>
                <%-- This feature is currently not supported --%>            
            <%--<c:if test="${empty form.excludedProperties['jobTicket']}">--%>
                <%--<tr>--%>
                    <%--<td>Job ticket</td>--%>
                    <%--<td>--%>
                        <%--<form:select path="jobTicket">--%>
                            <%--<option>Please Choose</option>--%>
                            <%--<form:options items="${form.availableJobTickets}" itemLabel="content.name" itemValue="id"/>--%>
                        <%--</form:select>--%>
                    <%--</td>--%>
                <%--</tr>--%>
            <%--</c:if>--%>

            <%-- Series --%>
            <c:if test="${empty excludeProperties['seriesDublinCore']}">
                <tr>
                    <td><span class="label">Series metadata</span></td>
                    <td>
                        <form:select path="series" id="seriesSelect" onchange="setInspectSeriesLink();">
                            <form:option value="-" label="-- Select a series --"/>
                            <form:options items="${form.availableSeries}" itemLabel="title" itemValue="id"/>
                        </form:select>
                        <%--<select id="seriesSelect" name="seriesId" onchange="setInspectSeriesLink()">--%>
                                <%--@elvariable id="series" type="java.util.List<ch.ethz.replay.ui.scheduler.RecordingSeries>"--%>
                            <%--<option value="-1">Select a series</option>--%>
                            <%--<c:forEach var="r" items="${series}">--%>
                                <%--<option value="${r.id}">${r.title}</option>--%>
                            <%--</c:forEach>--%>
                        <%--</select>--%>

                        <a id="inspectSeriesLink" href="#">Inspect</a>
                    </td>
                </tr>
            </c:if>
            <tr>
                <td>
                </td>
                <td><div class="form-button-bar">
                        <button onclick="window.location='cancel'; return false;">Cancel</button>
                        <input type="submit" class="form-save" value="Save"/>
                    </div>
                </td>
            </tr>
        </table>
    </form:form>
</div>

</body>
</html>