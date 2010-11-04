<%--
  Replay scheduler site css
--%>
<%@ page contentType="text/css;charset=UTF-8" pageEncoding="UTF-8" %>

<%-- JSTL --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Define the defaults --%>
<c:set var="lightLineColor">#c5c5c5</c:set>
<c:set var="mediumLineColor">#aaa</c:set>
<c:set var="lightLineStyle">1px solid ${lightLineColor}</c:set>
<c:set var="mediumLineStyle">1px solid ${mediumLineColor}</c:set>
<c:set var="formInputWidth">300</c:set>
<c:set var="emBackgroundColor">#eee</c:set>
<c:set var="leftPagePadding">8</c:set>
<c:set var="buttonBackgroundColor">#4b5740</c:set>
<c:set var="buttonLightBorderColor">#56753b</c:set>
<c:set var="buttonDarkBorderColor">#000</c:set>

<c:set var="cssMainFontSize">font-size: 12px;</c:set>
<c:set var="cssMainFontFamily">
    font-family: Lucida Grande, Verdana, Arial, Helvetica, sans serif;
</c:set>
<c:set var="inputLabelFontSize">11</c:set>

<c:set var="cssInputLabel">
    color: #777;
    font-weight: bold;
    font-size: ${inputLabelFontSize}px;
</c:set>

<%-- main & default styles --%>

body {
    padding: 0 0 0 0;
    margin: 0;
    border: 0;
    ${cssMainFontFamily}
    ${cssMainFontSize}
}

<%-- Menu bar --%>
div#menu-bar {
    height: 20px;
    padding: 32px 0 0 ${leftPagePadding * 2}px;
    background-color: rgb(0, 51, 51);
    border-bottom: 1px solid #000;
    white-space: nowrap;
}

div#menu-bar a {
    font-weight: bold;
    color: #fff;
    -text-shadow: 0px 1px 1px #fff;
}

div#menu-bar a:hover {
    text-decoration: none;
}

div#menu-bar ul, ul.select, ul.list {
    display: inline-block;
    margin: 0;
    padding: 0;
    border: 0;
}

div#menu-bar ul li, ul.select li, ul.list li {
    display: inline;
    margin-right: 10px;
}

div#menu-bar.sub {
    margin: -25px 0px 22px 0px;
    padding-top: 8px;
    background: #f1ffdb;
    border-bottom: 1px solid #aafd34
}

div#menu-bar.last {
    margin-bottom: 20px;
}

div#menu-bar.select {
    margin-top: 5px;
    padding: 0;
    background: none;
}

<%-- Header --%>
div#header {
    border-bottom: ${mediumLineStyle};
    padding: 5px;
    background: ${emBackgroundColor};
}

div#header.sep {
    border-bottom: ${mediumLineStyle};
}

<%-- Footer --%>
div#footer {
    border-top: ${mediumLineStyle};
    background: ${emBackgroundColor};
    padding: 5px
}

<%-- Content --%>
div#content {
}

#input-pane {
}

#result-pane {
}

#response {
    margin-top: 30px;
}


<%-- ======================================== --%>
<%-- General styles                           --%>
<%-- ======================================== --%>

.small {
    font-size: 90%;
}

p.description {
    font-style: italic;
}

span.warning {
    font-weight: bold;
    color: red;
}

a {
    color: darkgreen;
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

div.hint {
    margin: 3px 0 3px 0;
    border: 1px solid darkred;
    padding: 2px;
    text-align: center;
    color: darkred;
    background: #fff2f8;
}

span.info-label {
    padding: 2px 6px 2px 6px;
    background-color: #dbe5ff;
    font-weight: bold;
    -moz-border-radius: 10px;
    -webkit-border-radius: 10px;
    border-radius: 10px;
}

span.mandatory:before {
    content: '* ';
    color: lightcoral;
}

<%-- Fold --%>
a.fold-button {
    cursor: pointer;
    font-weight: bold;
}

<%-- --- Overlay --- --%>

div.overlay {
    position: absolute;
    font-size: 90%;
    background: lightgoldenrodyellow;
}

div.overlay div.overlay-head {
    position: absolute;
    background: darkgoldenrod;
    height: 15px;
    width: 100%;
}

div.overlay div.overlay-body {
    margin-top: 15px;
}

div.overlay div.overlay-head a.button {
    display: block;
    float: right;
    margin-left: 5px;
}

div.overlay div.overlay-body {
    border: 1px solid darkgoldenrod;
    border-top: 0;
    padding: 5px;
}

div.overlay p {
    margin: 2px;
}

.replay-logo {
    padding: 0 15px 0 3px;
    font-weight: bold;
    color: #044e19;
    text-shadow: 0px 1px 1px #fff;
}

li.person-label {
    border: 1px solid #aafd34;
    background: #f1ffdb;
    padding: 0 3px 0 3px;
    -moz-border-radius: 6px;
    -webkit-border-radius: 6px;
    border-radius: 6px;
}

li.person-label a img {
    vertical-align: baseline;
}

<%-- ======================================== --%>
<%-- Autocomplete                             --%>
<%-- ======================================== --%>

div.suggestions {
    position: absolute;
    background-color: #999999;
    margin: 5px 0 0 0;
    padding: 0;
    border: 1px solid #666666;
}

div.suggestions ul {
    list-style-type: none;
    margin: 0;
    padding: 2px;
    max-height: 100px;
    overflow-y: auto;
}

div.suggestions ul li.selected {
    background-color: #dbe5ff;
}

div.suggestions ul li {
    display: block;
    list-style-type: none;
    margin: 0;
    padding: 2px;
    cursor: pointer;
    line-height: 1.3em;
    color: black;
    border-top: 1px solid #666666;
}

div.suggestions ul li:first-child {
    border-top: none;
}

<%-- ======================================== --%>
<%-- Tables                                   --%>
<%-- ======================================== --%>

table {
    border-collapse: separate;
    border-spacing: 0;
    empty-cells: show;
    -width: 100%;
    ${cssMainFontFamily}
    ${cssMainFontSize}
}

table.hover tr:hover {
    background: lightgoldenrodyellow;
}

caption {
    padding: 10px 0 10px 0;
    -background: #d8d8d8;
    font-size: 110%;
    font-weight: bold;
    text-align: center;
    text-shadow: 0px 1px 1px #eee;
}

caption a.button {
    display: block;
    float: right;
    margin-left: 5px;
}

caption span {
    float: left;
}

td:last-child, th:last-child {
    padding-right: 5px;
}

td:first-child, th:first-child {
    padding-left: 5px;
}

td.car, th.car {
    text-align: right;
}

td.cac, th.cac {
    text-align: center;
}

td.cal, th.cal {
    text-align: left;
}

tr.odd {
    background: none;
}

tr.even {
    background: #f8f8f8;
}

tr.even td {
    border-top: 1px solid #e8e8e8;
    border-bottom: 1px solid #e8e8e8;
}

tr.disabled, tr.grayed-out {
    color: gray;
}

th {
    padding: 2px 10px 2px 2px;
    background: white;
    text-align: left;
    border-bottom: 1px solid darkgray
}

th:last-child {
    padding-right: 3px;
}

<%-- --- table subheadings -----%>

tr.subheading td {
    padding-top: 20px;
}

tr.subheading td div.subheading {
    width: 286px;
    height: 17px;
    padding: 0 7px 0 7px;
    line-height: 17px;
    background: darkgray;
    color: white;
    -moz-border-radius: 6px;
    -webkit-border-radius: 6px;
    border-radius: 6px;
}

tr.subheading td div.subheading a.img-button {
    float: right;
}

tr.subheading td div.subheading a.img-button img {
    margin-top: 1px;
}

tr.subheading td div.subheading span {
    float: left;
    font-style: italic;
}

<%-- ======================================== --%>
<%-- General purpose styles                   --%>
<%-- ======================================== --%>

div.left {
    float: left;
}

div.right {
    float: right;
}

hr.spacer {
    height: 15px;
    border: none;
}

hr.spacer-big {
    height: 25px;
    border: none;
}

<%-- --- Errors and messages -----%>

form span.error {
    float: left;
    clear: both;
    margin: 1px 0 8px 0;
    padding: 8px 3px 3px 3px;
    line-height: 1.3em;
    color: darkred;
    background: url("fielderrorbox.png") no-repeat top left;
    font-size: 90%;
}

div.error {
    padding: 5px;
    border: 1px solid #ead1d5;
    background: #fff2f2;
    color: darkred;
    clear: both;
    text-align: center;
}

div.message {
    padding: 5px;
    border: 1px solid #244da1;
    background: #ccccff;
    color: darkblue;
    clear: both;
    text-align: center;
}

<%-- Legend --%>

div.legend {
    padding: 5px 10px 5px 10px;
    font-size: 90%;
}

div.legend span {
    margin-right: 20px;
}

div.legend div {
    line-height: 23px;
}

div.legend img {
    vertical-align: text-bottom;
}

<%-- ======================================== --%>
<%-- Forms                                    --%>
<%-- ======================================== --%>

form {
    margin: 0;
}

div#form-panel {
    position: relative;
    clear: both;
    padding: 5px 0 5px 0;
}

table.form {
    border-collapse: collapse;
}

table.form td {
    -border: 1px solid #666;
}

table.form caption {
    margin-left: 10px;
    margin-right: 10px;
}

<%-- left column --%>
table.form td:first-child, table.list td:first-child {
    width: 33%;
    text-align: right;
}

table.form input[type="text"] {
    width: ${formInputWidth}px;
}

table.form select {
    width: ${formInputWidth}px;
}

<c:set var="formElemHeight" value="22"/>

table.form tr {
    line-height: ${formElemHeight}px;
}

input[type=text], input[type=checkbox], textarea {
    -height: ${formElemHeight}px;
    -line-height: ${formElemHeight}px;
    border: 1px solid ${lightLineColor};
    padding: 4px 2px 2px 2px;
    margin: 2px;
    background: #fff;
    ${cssMainFontSize}
}

span.label {
    display: block;
    -height: ${formElemHeight}px;
    -line-height: ${formElemHeight}px;
    padding: 2px;
    margin: 2px;

    font-size: 11px;
    font-weight: bold;
    color: #777;
}

select {
    border: 1px solid ${lightLineColor};
    padding: 2px;
    margin: 2px;
    background: #fff;
    font-size: 12px;
}

input[type="submit"], button {
    margin: 0;
    border: 1px solid ${buttonDarkBorderColor};
    border-top-color: ${buttonLightBorderColor};
    border-left-color: ${buttonLightBorderColor};
    border-bottom-color: ${buttonDarkBorderColor};
    padding: 2px 8px;
    background: ${buttonBackgroundColor};
    color: #fff;
}

div.form-button-bar {
    width: ${formInputWidth}px;
    margin-top: 20px;
    text-align: right;
}

table.form a.img-button img {
    border: 0;
    vertical-align: text-bottom;
}

table.form input[type="text"] + a.img-button img,
    table.form div.datepicker + a.img-button img {
    border: 0;
    padding-top: 5px;
    vertical-align: text-bottom;
}

a.img-button img {
    border: 0;
    vertical-align: text-bottom;
}

a.img-button:hover {
    text-decoration: none;
}


<%-- ======================================== --%>
<%-- List table                               --%>
<%-- ======================================== --%>

table.list {
    border-top: 0px;
    border-color: #e8e8e8;
    border-style: solid;
}

table.list td:first-child {
    color: #666;
}

table.list tr.subheading td div.subheading {
    background: transparent;
    border-bottom: 1px solid lightgray;
    color: #666;
    margin-bottom: 10px;
    -moz-border-radius: 4px;
    -webkit-border-radius: 4px;
    border-radius: 6px;
}

