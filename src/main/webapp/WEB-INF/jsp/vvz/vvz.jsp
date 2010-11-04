<%--

 vvz.jsp
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Sep 15, 2008

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
<%@ include file="../prolog.jspf" %>

<c:set var="dummy">${i18n}</c:set>

<html>
<head>
    <meta name="decorator" content="main"/>
    <replay:library name="timer"/>
    <replay:library name="jquery"/>
    <script type="text/javascript">
        jQuery(function($) {
            var timer = new Timer(500);
            $("#query").keyup(function() {
                var q = $(this).val();
                if (q.length > 1) {
                    timer.exec(function() {
                        $("#result").load("${C}/vvz/vvzquery", {q: q});
                    });
                } else {
                    $("#result").text("");
                }
            }).focus();
        });
    </script>
    <style type="text/css">
        #result ul {
            list-style: none;
        }

        /* Title */
        #result > ul > li {
            padding-top: 10px;
            line-height: 21px;
        }

        /* Separation between series */
        #result ul ul li:last-child {
            margin-bottom: 5px;
        }

        /* Person & location */
        #result ul ul li {
            display: inline;
            line-height: 21px;
        }

        #query {
            width: 250px;
        }

        span.sep {
            color: #aaa;
            padding: 0 5px 0 5px;
        }
    </style>
</head>

<%-- ============================================================ --%>
<%-- BODY                                                         --%>
<%-- ============================================================ --%>

<body id="p-vvz" pagecss="true">

<replay:fragment name="header">
    <div id="input-pane" class="layout-north">
        <div style="text-align: center; margin-top: 30px;">
            ${semester.semkez} &nbsp;
            <fmt:formatDate dateStyle="short" value="${semester.semesterbeginn}"/> -
            <fmt:formatDate dateStyle="short" value="${semester.semesterende}"/>
        </div>
        <div style="text-align: center; margin: 5px 0 5px 0"><input type="text" id="query"/></div>
        <c:if test="${onlyLectures}">
            <div style="text-align: center;">
                <small>Please note that only lectures will be searched!</small>
            </div>
        </c:if>
    </div>
</replay:fragment>

<div id="result-pane">
    <%-- Print errors --%>
    <c:if test="${!empty error}">
        <div class="error fade">${i18n[error]}</div>
    </c:if>
    <c:if test="${empty semester}">
        <div class="error fade">
            Either the VVZ database is not reachable or empty or there is no current semester in it.
        </div>
    </c:if>

    <%-- Query results go here --%>
    <div id="result" style="min-width:70%"></div>
</div>
</body>
</html>