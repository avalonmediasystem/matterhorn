<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="WEB-INF/jsp/prolog.jspf" %>
<html>
<script src="${pageContext.servletContext.contextPath}/js/jquery/jquery.js" type="text/javascript"></script>
<script src="${pageContext.servletContext.contextPath}/js/jquery/jquery.compasslayout.js" type="text/javascript"></script>
<style type="text/css">
    body {
        padding: 0;
        margin: 0;
        font-family: Verdana, Helvetica, Arial, sans-serif;
        font-size: 12px;
    }

    #header {
        background: url( 'data:image/png;base64,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' ) top left repeat-x #e7e7e7;
        padding: 80px 0 10px 10px;
    }

    #shadow-edge {
        background: url( 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAVwAAAAHCAYAAABEKPWsAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAElJREFUeNrs2LEJACAMBMAIuq9TW9m4gVnCFHIHv8AXT0iPiBkAPNczRw0ANYO71QBQM7hLDQAuXICvBtcPF6BAyww1ALx3BRgA2Z4FLxJlnx8AAAAASUVORK5CYII=' ) top left repeat-x;
        position: absolute;
        height: 7px;
    }

    #exception-panel {
        padding: 10px 5px;
        color: #555;
        overflow-x: auto;
        overflow-y: auto;
        font-family: Consolas, Monaco, Courier, monospace;
        font-size: 12px;
    }

    #exception-panel p {
        margin: 0 0 0 30px;
    }

    #exception-panel a {
        text-decoration: none;
    }

    h1 {
        font-size: 20px;
        color: #96323d;
    }

    #exception-panel-header {
        padding: 30px 0 0 10px;
        font-size: 14px;
        font-weight: normal;
        height: 18px;
                background: #e7e7e7;
    }

    #exception-panel-title {
        float: left;
        line-height: 18px;
    }

    #exception-panel-options {
        float: right;
        height: 18px;
        line-height: 18px;
        font-size: 12px;
    }

    #stacktrace-overview {
        border-bottom: 1px solid lightgray;
        margin-bottom: 20px;
    }

    .stacktrace {
        margin-bottom: 10px;
    }

    .message {
        display: block;
        color: black;
    }

    .project-class {
        color: #448;
        background: #cccfff;
    }

    .exception {
        font-weight: bold;
        color: black;
    }

    .clazz {
        font-weight: bold;
    }

    .method {
        font-weight: bold;
        font-style: italic;
    }

    .cause {
        font-weight: bold;
        position: relative;
        display: block;
    }

    .nowrap {
        word-wrap: normal;
        white-space: nowrap;
    }
</style>
<script type="text/javascript">

    jQuery(function($) {
        // Word wrap
        $("#word-wrap").click(function() {
            $("#exception-panel").toggleClass("nowrap");
        });

        // Shadow bar
        var scv = $.browser.safari ? -4 : 0;
        $("#shadow-edge").css({zIndex: 99});
        $(window).resize(function() {
            var $ep = $("#exception-panel");
            $("#shadow-edge").css({top: $ep.position().top, width: $ep.width() + scv + 'px'});
        }).resize();
    });
</script>
<body>

<div id="header" class="layout-north">
    <%-- Print specialized messages and hints for well-known exceptions --%>
    <c:set var="e" value="${requestScope['javax.servlet.error.exception']}"/>
    <c:if test="${e.class.name eq 'org.springframework.transaction.CannotCreateTransactionException'}">
        <div class="hint">
            An error occured connecting to the database. Maybe the database is down or the application cannot
            connect to it due to network problems.
        </div>
    </c:if>
</div>

<div id="exception-panel-header" class="layout-north">
    <div id="exception-panel-title">Stacktrace</div>
    <div id="exception-panel-options">
        Word wrap <input id="word-wrap" type="checkbox" value="wrap"/>
    </div>
</div>

<div id="shadow-edge"></div>
<div id="exception-panel" class="layout-center nowrap"><%
    StringWriter buffer = new StringWriter(2048);
    PrintWriter p = new PrintWriter(buffer);
    ((Throwable) request.getAttribute("javax.servlet.error.exception")).printStackTrace(p);

    StringBuilder stacktrace = new StringBuilder();
    StringBuilder overview = new StringBuilder();
    int anchor = 0;
    Throwable t = (Throwable) request.getAttribute("javax.servlet.error.exception");
    while (t != null) {
        if (stacktrace.length() > 0) stacktrace.append("Caused by: ");
        stacktrace.append("<div class='stacktrace'>")
                .append("<a name='a-").append(anchor).append("'>")
                .append("<a href='#overview'>")
                .append("<span class='exception'>")
                .append(t.getClass().getName()).append("</span></a>");

        // Clippy
        String clippy = getServletConfig().getServletContext().getContextPath() + "/js/clippy.swf";
        String clippyBG = "#fff";
        StringWriter clippyTxt = new StringWriter();
        t.printStackTrace(new PrintWriter(clippyTxt));
        stacktrace.append("&nbsp<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\"\n" +
"            width=\"110\"\n" +
"            height=\"14\"\n" +
"            id=\"clippy\" >\n" +
"    <param name=\"movie\" value=\"" + clippy + "\"/>\n" +
"    <param name=\"allowScriptAccess\" value=\"always\" />\n" +
"    <param name=\"quality\" value=\"high\" />\n" +
"    <param name=\"scale\" value=\"noscale\" />\n" +
"    <param NAME=\"FlashVars\" value=\"text="+clippyTxt+"\">\n" +
"    <param name=\"bgcolor\" value=\""+clippyBG+"\">\n" +
"    <embed src=\"" + clippy + "\"\n" +
"           width=\"110\"\n" +
"           height=\"14\"\n" +
"           name=\"clippy\"\n" +
"           quality=\"high\"\n" +
"           allowScriptAccess=\"always\"\n" +
"           type=\"application/x-shockwave-flash\"\n" +
"           pluginspage=\"http://www.macromedia.com/go/getflashplayer\"\n" +
"           FlashVars=\"text="+clippyTxt+"\"\n" +
"           bgcolor=\""+clippyBG+"\"\n" +
"    />\n" +
"    </object>");
        // /Clippy

        stacktrace.append("<span class='message'>").append(t.getMessage()).append("</span>");

        if (overview.length() == 0) overview.append("<a name='overview'>");
        overview.append("<div class='stacktrace'>")
                .append("<a href='#a-").append(anchor).append("'>")
                .append("<span class='exception'>")
                .append(t.getClass().getName()).append("</span></a>");
        overview.append("<span class='message'>").append(t.getMessage()).append("</span></div>");

        for (StackTraceElement e : t.getStackTrace()) {
            Matcher m = Pattern.compile("^(.*\\.)(.+?)(\\$\\$.+)?$").matcher(e.getClassName());
            m.matches();
            stacktrace.append("<p>at ");
            // Project class detection
            boolean projectClass = e.getClassName().matches(".*?\\.replay\\..*");
            if (projectClass) stacktrace.append("<span class='project-class'>");
            stacktrace
                    .append("<span class='package'>").append(m.group(1)).append("</span>")
                    .append("<span class='clazz'>").append(m.group(2)).append("</span>")
                    .append("<span class='advice'>").append(m.group(3) != null ? m.group(3) : "").append("</span>")
                    .append("<span class='method'>.").append(e.getMethodName()).append("</span>")
                    .append("<span class='location'>(");
            if (e.getFileName() != null) stacktrace.append(e.getFileName());
            if (e.getLineNumber() >= 0) stacktrace.append(':').append(e.getLineNumber());
            stacktrace.append(")</span>");
            if (projectClass) stacktrace.append("</span>");
            stacktrace.append("</p>");
        }
        stacktrace.append("</div>");

        anchor++;
        t = t.getCause();
    }
    out.println("<div id='stacktrace-overview'>" + overview + "</div>");
    out.println("<div id='stacktrace'>" + stacktrace + "</div>");
%>
</div>
</body>
</html>
