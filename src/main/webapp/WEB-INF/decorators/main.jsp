<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
  "http://www.w3.org/TR/html4/loose.dtd">

<%--

 main.jsp
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
<%@ page import="ch.ethz.replay.ui.scheduler.web.controller.vvz.VvzController" %>
<%@ include file="../jsp/prolog.jspf" %>


<html>
<head>
    <title><decorator:title default="REPLAY | Scheduler"/></title>

    <c:set var="bodyId"><decorator:getProperty property="body.id"/></c:set>
    <c:set var="pagecss"><decorator:getProperty property="body.pagecss"/></c:set>
    <c:set var="pagejs"><decorator:getProperty property="body.pagejs"/></c:set>
    <c:set var="pageName" value="${fn:substringAfter(bodyId, 'p-')}"/>

    <%-- include stylesheets --%>
    <link rel="stylesheet" href="${S}/css/site.jsp" type="text/css" media="screen">
    <c:if test="${pagecss}">
        <link rel="stylesheet" href="${S}/css/${pageName}.css" type="text/css" media="screen"/>
    </c:if>

    <%-- include libraries --%>
    <replay:library name="prototype"/>
    <replay:library name="jquery"/>
    <replay:library name="jquery.compasslayout"/>
    <replay:library name="scriptaculous"/>
    <replay:library name="activestyles"/>
    <replay:library name="application"/>
    <decorator:head/>
    <c:if test="${pagejs}">
        <replay:library name="${pageName}.js"/>
    </c:if>

    <script type="text/javascript">
        jQuery(function($) {
            $("#ajax-indicator").ajaxSend(function() {$(this).fadeIn();});
            $("#ajax-indicator").ajaxStop(function() {$(this).fadeOut();});
        });
    </script>
</head>

<body id="${bodyId}">
<%-- Menu bar --%>
<div id="menu-bar" class="layout-north">
    <span>
        <ul>
            <li><a href="${C}/schedule/list">${i18n['label.schedule']}</a></li>
            <li><a href="${C}/recording/edit">${i18n['label.new-recording']}</a></li>
            <c:if test="<%= VvzController.isConfigured() %>">
                <li><a href="${C}/vvz/vvz">${i18n['label.newRecordingVvz']}</a></li>
            </c:if>
            <li><a href="${C}/recordingseries/list">Series</a></li>
            <%-- Disabled --%>
            <%--<li><a href="${C}/listpersons">${i18n['label.vcards']}</a></li>--%>
            <li><a href="${C}/icalendar">${i18n['label.icalendar']}</a></li>
        </ul>
    </span>
    <span id="ajax-indicator" style="float:right; display:none;">
        <img src="${S}/img/ajax-loader.gif" alt="[ajax]" style="margin: 5px"/>
    </span>
</div>

<%-- Header --%>
<replay:yield name="header">
    <div id="header" class="layout-north">
        ${fragment}
    </div>
</replay:yield>

<%-- Content --%>
<div id="content" class="layout-center">
    <decorator:body/>
</div>

<%-- Footer --%>
<replay:yield name="footer">
    <div id="footer" class="layout-south">${fragment}</div>
</replay:yield>
</body>
</html>