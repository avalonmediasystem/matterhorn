<%--

 schedule.jsp
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

    <replay:library name="datepicker"/>
    <replay:library name="jquery.cookies-2.0.1.ced.min"/>

    <%-- ================================== --%>
    <%-- JAVASCRIPT                         --%>
    <%-- ================================== --%>

    <script type="text/javascript">

        /* Autorefresh */
        jQuery(function($) {
            var URL = '${C}/schedule/list #autoRefreshPanel';

            var delay = $('#autoRefresh').cookieBind().val();
            if (delay > 0)
                $('#result-pane').heartbeat(URL, {delay: delay, startDelay: delay, complete: styles});

            // Bind select box
            $('#autoRefresh').change(function() {
                var delay = $(this).val();
                if (delay > 0)
                    $('#result-pane').heartbeat(URL, {delay: delay, complete: styles});
                else
                    $('#result-pane').stopHeartbeat();
            });

            // Styles
            styles();

            function styles() {
                $("table#recordings tr.recording:odd").attr("odd");
                $("table#recordings tr.recording:even").attr("even");
            }
        });

        function initPage() {
        <%-- Currently unused
        personDetail = $('person-detail');
        personDetail.absolutize();
        --%>
            // todo replace with jQuery alternative
            new Ajax.Autocompleter("locationAutocomplete", "locationSuggestions", "${C}/locationsuggest", {
                paramName: "l",
                minChars: 2
            });
        }
    </script>
</head>


<%-- ================================== --%>
<%-- BODY                               --%>
<%-- ================================== --%>

<body id="p-schedule" pagecss="true">

<replay:fragment name="header">
    <%-- filter form --%>
    <div class="fold" title="Filter">
        <div id="form-panel">
            <form:form commandName="scheduleFilter" action="${C}/schedule/list" id="form">
                <script type="text/javascript">var f = $('form');</script>

                <table class="form">
                    <tr>
                        <td>${i18n['label.starting-after']}</td>
                        <td>
                            <form:input path="startingAfter" onkeypress="submitOnEnter(event, f)"
                                        cssClass="input-date"
                                        autocomplete="false"/>
                            <replay:button type="clear" action="onclick:submitForm({startingAfter: ''})"/>
                            <form:errors path="startingAfter" cssClass="error"/>
                        </td>
                    </tr>

                    <tr>
                        <td>${i18n['label.ending-after']}</td>
                        <td>
                            <form:input path="endingAfter" onkeypress="submitOnEnter(event, f)"
                                        cssClass="input-date"
                                        autocomplete="false"/>
                            <replay:button type="clear" action="onclick:submitForm({endingAfter: ''})"/>
                            <form:errors path="endingAfter" cssClass="error"/>
                        </td>
                    </tr>

                    <tr>
                        <td>${i18n['label.location']}</td>
                        <td>
                            <form:input path="locationName" id="locationAutocomplete"
                                        onkeypress="submitOnEnter(event, f)"/>
                            <replay:button type="clear" action="onclick:submitForm({'locationName': ''})"/>
                            <div id="locationSuggestions" class="suggestions"></div>
                        </td>
                    </tr>

                    <tr>
                        <td><span title="${i18n['fragment.freetext-search-title']}">${i18n['label.search']}</span>
                        </td>
                        <td>
                            <form:input path="freeSearch" onkeypress="submitOnEnter(event, f)"/>
                            <replay:button type="clear" action="onclick:submitForm({freeSearch: ''})"/>
                        </td>
                    </tr>

                    <tr>
                        <td>Series:</td>
                        <td>
                            <form:select path="series" multiple="false" size="1" itemValue="id">
                                <form:option value="-1" label="All"/>
                                <form:options items="${series}" itemValue="id" itemLabel="title"/>
                            </form:select>
                        </td>
                    </tr>

                    <tr>
                        <td></td>
                        <td>
                            <div class="form-button-bar">
                                <input type="submit" value="${i18n['button.search.text']}"/>
                            </div>
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>
</replay:fragment>

<!-- ====================================================================== -->
<!-- SCHEDULE                                                               -->
<!-- ====================================================================== -->

<div id="result-pane">
    <%-- Display the recordings --%>
    <table id="recordings">
        <tbody>
            <%--@elvariable id="recordings" type="java.util.List<ch.ethz.replay.ui.scheduler.Recording>"--%>
        <c:forEach var="recording" items="${recordings}">
            <c:if test="${empty lastRec or recording.otherDay[lastRec.startDate]}">
                <tr class="day-separator">
                    <td><span
                            style="display: inline-block; width: ${100 / 7 * recording.weekDay}%"></span><span
                            class="day-separator-date"><fmt:formatDate value="${recording.startDate}"
                                                                       pattern="E, MMM dd yy"/></span></td>
                </tr>
            </c:if>
            <c:set var="lastRec" value="${recording}"/>
            <tr class="recording ${recording.bygone ? 'bygone' : ''}${recording.recording ? ' running' : ''}">
                <td>
                    <div class="head">
                        <div class="left">
                            <span class="bullet">&bull;</span>
                            <span class="period">
                            <fmt:formatDate value="${recording.startDate}" pattern="hh:mm a"/>
                            - <fmt:formatDate value="${recording.endDate}" pattern="hh:mm a"/>
                            </span>
                            <span class="location">${recording.location.name}</span>
                        </div>

                            <%-- Actions --%>
                        <div class="right">
                            <c:if test="${!empty recording.series}">
                                <a href="filterbyseries?id=${recording.series.id}" class="img-button" title="Filter by series">
                                    <img src="${S}/img/filter.png" alt="[Filter]"/>
                                </a>
                            </c:if>
                            <c:if test="${recording.editable}">
                                <replay:button type="edit" action="/recording/edit?id=${recording.id}"/>
                            </c:if>
                            <a href="delete?id=${recording.id}" class="img-button"
                               title="Remove recording" onclick="return confirm('Are you sure?');">
                                <img src="${S}/img/remove.png" alt="[Remove]"/>
                            </a>
                        </div>
                    </div>
                    <div class="body">
                            <%-- Recording marker --%>
                        <c:if test="${recording.recording}">
                            <span class="recording-marker" title="Currently recording">R</span>
                        </c:if>
                            <%-- Series --%>
                        <c:if test="${!empty recording.series}">
                            <c:choose>
                                <c:when test="${!empty recording.series.dublinCore}">
                                    <a href="editseriesdublincore?id=${recording.series.id}" title="Series">
                                        <c:out value="${recording.series.title}"/>
                                    </a>
                                </c:when>
                                <c:otherwise><span title="Series"><c:out value="${recording.series.title}"/></span></c:otherwise>
                            </c:choose>
                            <span style="color: #aaa;">&rarr;</span>
                        </c:if>
                            <%-- Lecture--%>
                        <c:choose>
                            <c:when test="${recording.editable}">
                                <a href="${C}/schedule/editdublincore?id=${recording.id}" title="Episode">
                                    <c:out value="${recording.title}"/>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span title="Episode"><c:out value="${recording.title}"/></span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<%-- Currently unused
<div id="person-detail" class="overlay" style="display:none;z-index:99"></div>
--%>

<replay:fragment name="footer">
    ${i18n['fragment.recording-count'][fn:length(recordings)]}
</replay:fragment>

</body>
</html>