<%--

 events.jsp
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Aug 28, 2008

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

    <replay:library name="datepicker"/>

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
            return ' <a href="#" class="img-button" onclick="removePerson(' + id + ');return false">' +
                   '<img src="${S}/img/remove-small.gif"/></a>';
        }
        function createPersonHiddenField(id) {
            return '<input type="hidden" name="contactPersons" value="' + id + '"/>';
        }

        function createSelectedPersonList() {
        <c:forEach var="person" items="${recording.contactPersons}">
            selectedPersonsElem.insert('<li id="_p-${person.id}" class="person-label">${person.formattedName} ' +
                                       createPersonRemoveLink(${person.id}) +
                                       createPersonHiddenField(${person.id}) + '</li>');
        </c:forEach>
            if (!selectedPersonsElem.empty()) selectedPersonsElem.show();
        }

        /**
         * Automatically called by the surrounding template.
         */
        function initPage() {
            selectedPersonsElem = $('selected-persons');
            createSelectedPersonList();

            $('startDate').observe('datepicker:change', function() {
                if ($('endDate').value.blank()) {
                    $('endDate').value = $('startDate').value;
                    $('endDate').value = $('startDate').value;
                }
            })

            // autocompletion fields
            new Ajax.Autocompleter("location-autocomplete", "location-suggestions", "${C}/locationsuggest", {
                paramName: "l",
                minChars: 2
            });
            new Ajax.Autocompleter("person-autocomplete", "person-suggestions", "${C}/personsuggest", {
                paramName: "p",
                minChars: 2,
                updateElement: addPersonToList
            });
        }
    </script>
</head>

<body id="p-event">

<div id="form-panel">

    <%-- edit recording --%>
    <form:form commandName="event" action="${C}/event/query" id="form">
        <input type="hidden" name="contactPersons">
        <script type="text/javascript">var f = $('form')</script>

        <table class="form">
            <tr>
                <td>${i18n['label.location']}</td>
                <td>
                    <form:input path="location" id="location-autocomplete"/>
                    <div id="location-suggestions" class="suggestions"></div>
                    <form:errors path="location" cssClass="error"/>
                </td>
            </tr>
            <tr>
                <td>${i18n['label.from']}</td>
                <td>
                    <form:input path="startDate" cssClass="input-date"/>
                    <form:errors path="startDate" cssClass="error"/>
                </td>
            </tr>
            <replay:viewOrEdit path="to" edit="true"/>
            <tr>
                <td>${i18n['label.to']}</td>
                <td>
                    <form:input path="endDate" cssClass="input-date"/>
                    <form:errors path="endDate" htmlEscape="false" cssClass="error"/>
                </td>
            </tr>
            <tr>
                <td>${i18n['label.device']}</td>
                <td>
                    <form:checkboxes path="devices" items="${devices}" itemLabel="name" itemValue="id"/>
                    <form:errors path="devices" cssClass="error"/>
                </td>
            </tr>
            <tr>
                <td>${i18n['label.contact-persons']}</td>
                <td>
                    <ul id="selected-persons" class="select" style="display:none;"></ul>
                    <input id="person-autocomplete" type="text"/>
                    <replay:button type="create" action="onclick:submitForm({}, '${C}/recording/createPerson')"/>
                    <div id="person-suggestions" class="suggestions"></div>
                </td>
            </tr>
            <c:choose>
                <c:when test="${!empty recording.dublinCore}">
                    <tr>
                        <td></td>
                        <td>
                            <a href="${C}/dublincore?id=${recording.id}&amp;edit=true">${i18n['label.edit-dublincore']}</a>
                        </td>
                    </tr>
                </c:when>
                <c:when test="${!recording.new}">
                    <tr>
                        <td></td>
                        <td><a href="${C}/dublincore?id=${recording.id}">${i18n['label.create-dublincore']}</a></td>
                    </tr>
                </c:when>
            </c:choose>
        </table>

        <input type="submit" class="form-save" value="${i18n['button.save.text']}"/>
    </form:form>

</div>

</body>
</html>