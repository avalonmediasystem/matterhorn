<%--

 lecturecourse.jsp
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Sep 16, 2008

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
<%@ page import="ch.ethz.replay.ui.scheduler.web.controller.vvz.LectureCourseController" %>
<%@ page import="ch.ethz.replay.ui.scheduler.external.ethz.LectureCourse" %>
<%@ page import="ch.ethz.replay.ui.scheduler.Recording" %>
<%@ page import="ch.ethz.replay.core.api.common.metadata.dublincore.DublinCore" %>
<%@ page import="ch.ethz.replay.ui.scheduler.web.controller.ETHZDublinCoreCommand" %>
<%@ page import="ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCore" %>
<%@ page import="ch.ethz.replay.core.common.bundle.dublincore.ETHZDublinCoreValidator" %>

<%@ include file="../prolog.jspf" %>

<html>
<head>
    <!-- ${i18n['init']} -->
    <meta name="decorator" content="main"/>
    <replay:library name="jquery"/>
    <script type="text/javascript">
        jQuery(function($) {
            $("#schedule-all").click(function() {
                $("input.schedule").attr("checked", $(this).is(":checked"));
            });
        });
    </script>

    <style type="text/css">
        a.dc-invalid {
            color: #cc0033;
        }
    </style>
</head>

<%-- ============================================================ --%>
<%-- BODY                                                         --%>
<%-- ============================================================ --%>

<body id="p-lecturecourse" pagecss="true">

<div id="result-pane">
    <form action="schedule" method="post">
        <input type="hidden" name="schedule" value=""/>

        <table class="alternating-rows hover">
            <c:set var="dc" value="${!empty course.series ? course.series.dublinCore : course.dublinCoreDefaults}"/>
            <caption><c:out value="${dc.originalTitle}"/>
                <c:choose>
                    <c:when test="${!empty course.series}">
                        <%-- todo get access to configured validator bean --%>
                        <% boolean seriesDCValid = ETHZDublinCoreValidator.Obj.isValid(((LectureCourse) pageContext.findAttribute("course")).getSeries().getDublinCore()); %>
                        <a href="edit_sdc" class="img-button">
                            <img src='${S}/img/edit-<%= seriesDCValid ? "ok" : "error" %>.png' alt="Edit metadata"/>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span class="info-label">No series</span>
                    </c:otherwise>
                </c:choose>
            </caption>
            <%-- Table header --%>
            <thead>
            <tr>
                <th class="car">#</th>
                <th>Date</th>
                <th>From</th>
                <th class="sep-right">To</th>
                <th class="cac">Recording</th>
                <th class="cac">Metadata</th>
                <th class="cac">Schedule</th>
            </tr>
            </thead>
            <%-- Table body --%>
            <tbody>
            <%-- Defaults --%>
            <tr style="background: #efe">
                <td colspan="4" class="car">
                    <div style="height: 25px; line-height: 25px;"><span class="info-label">Template</span></div>
                </td>
                <td class="cac">
                    <a href="editrecordingdefaults" title="Edit defaults" class="img-button">
                        <img src="${S}/img/edit-ok.png" alt="Edit defaults"/>
                    </a>
                    &nbsp;
                    <a href="reset_rs" title="Apply defaults to all recordings" class="img-button">
                        <img src="${S}/img/reset.png" alt="Edit defaults"/>
                    </a>
                </td>
                <td class="cac">
                    <a href="edit_dcd" title="Edit defaults" class="img-button">
                        <img src="${S}/img/edit-ok.png" alt="Edit defaults"/>
                    </a>
                    &nbsp;
                    <a href="reset_dcs" title="Reset all metadata to default values" class="img-button">
                        <img src="${S}/img/reset.png" alt="Edit defaults"/>
                    </a>
                </td>
                <td class="cac"><input type="checkbox" id="schedule-all" title="toggle all on/off"/></td>
            </tr>

            <%-- Recordings --%>
            <%--@elvariable id="recording" type="ch.ethz.replay.ui.scheduler.Recording"--%>
            <c:set var="lastLocation" value=""/>
            <c:set var="scheduleGroup" value="<%= 0 %>"/>
            <c:forEach var="recording" items="${recordings}" varStatus="s">
                <%-- Draw a separator upon location change --%>
                <c:if test="${lastLocation ne recording.location}">
                    <!-- Inc schedule group counter -->
                    <c:set var="scheduleGroup" value="${scheduleGroup + 1}"/>
                    <!-- A new location : draw a separator -->
                    <tr class="subheading" style="background: #fff">
                        <td colspan="4" class="cac">
                            <div class="subheading"
                                 style="display: inline-block; width: 100%">${recording.location.name}</div>
                        </td>
                        <td colspan="2"></td>
                        <td class="cac"><input type="checkbox" class="schedule"
                                               onclick="jQuery('input.schedule-group-${scheduleGroup}').attr('checked', jQuery(this).is(':checked'));"/>
                        </td>
                    </tr>
                </c:if>

                <!-- At the same location as the last one -->
                <tr${replay:createConditionalAttribute(recording.bygone, 'class', 'disabled')}>
                    <td class="car">${s.index + 1}</td>
                    <td><%-- Day --%><fmt:formatDate value="${recording.startDate}"
                                                     pattern="${i18n['fmt.recording.start.day']}"/></td>
                    <td><%-- From --%><fmt:formatDate value="${recording.startDate}"
                                                      pattern="${i18n['fmt.recording.start.time']}"/></td>
                    <td><%-- To --%><fmt:formatDate value="${recording.endDate}"
                                                    pattern="${i18n['fmt.recording.end.time']}"/></td>
                    <td class="cac"><%-- Recording edit --%>
                        <a href="edit_r?id=${s.index}" class="img-button" title="Edit recording">
                            <c:set var="imgSuffix">${!recording.valid ? 'error' : (recording.different ? 'modified' : 'ok')}</c:set>
                            <img src="${S}/img/edit-${imgSuffix}.png" alt="Edit recording"/>
                        </a>
                        &nbsp;
                        <a href="reset_r?id=${s.index}" title="Reset to defaults"
                           class="img-button">
                            <img src="${S}/img/reset.png" alt="Reset to defaults"/>
                        </a>
                    </td>
                    <td class="cac"><%-- Dublin Core edit --%>

                        <a href="edit_dc?id=${s.index}" class="img-button" title="Edit metadata">
                            <%
                                ETHZDublinCore rdc = (ETHZDublinCore) ((Recording) pageContext.findAttribute("recording")).getDublinCore();
                                ETHZDublinCoreCommand dcc = (ETHZDublinCoreCommand) pageContext.findAttribute(LectureCourseController.S_DCD);
                                String imgSuffix;
                                // todo get access to configured validator bean
                                if (!ETHZDublinCoreValidator.Obj.isValid(rdc)) {
                                    imgSuffix = "error";
                                } else if (dcc.differsAssigned(rdc)) {
                                    imgSuffix = "modified";
                                } else {
                                    imgSuffix = "ok";
                                }
                            %>
                            <img src="${S}/img/edit-<%= imgSuffix %>.png" alt="Edit metadata"/>
                        </a>
                        &nbsp;
                        <a href="reset_dc?id=${s.index}" title="Reset to defaults"
                           class="img-button">
                            <img src="${S}/img/reset.png" alt="Reset to defaults"/>
                        </a>

                    </td>
                    <td class="cac">
                            <%-- Select recordings to be scheduled --%>
                        <input type="checkbox" name="selected" value="${s.index}"
                               class="schedule schedule-group-${scheduleGroup}"
                            ${replay:createConditionalAttribute(selected[s.index] ne null || recording.id ne null, "checked", "true")}
                    </td>
                </tr>
                <!-- Save current location -->
                <c:set var="lastLocation" value="${recording.location}"/>
            </c:forEach>
            </tbody>
        </table>

        <div style="text-align: right; padding: 15px 10px;">
            <input type="submit" class="form-save2" title="Put all recordings on the schedule" value="Schedule"/>
        </div>
    </form>
</div>

<replay:fragment name="footer">
    <div class="legend">
        <div>
            <span><img src="${S}/img/edit-ok.png"> = OK, aligns with defaults</span>
            <span><img src="${S}/img/edit-modified.png"> = OK, differs from defaults</span>
            <span><img src="${S}/img/edit-error.png"> = Incomplete, needs to be filled in</span>
        </div>
        <div><span><img src="${S}/img/reset.png"> = Reset to default values</span></div>
        <div>LECTURES ARE SCHEDULED WITH AN OFFSET OF -5 MINUTES TO THE START AND +5 MINUTES TO THE END TIME!</div>
    </div>
</replay:fragment>

</body>
</html>
</html>