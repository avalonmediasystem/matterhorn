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
    <meta name="decorator" content="main"/>
    <replay:library name="jquery"/>
    <replay:library name="jquery.consolelog"/>
    <replay:library name="jquery.metadata.min"/>
    <replay:library name="jquery.metaselector"/>
    <replay:library name="jquery.sbox"/>
</head>

<%-- ############################################################ --%>
<%-- BODY                                                         --%>
<%-- ############################################################ --%>

<body id="p-dublincore" pagecss="false">

<div id="form-panel">

    <%--
    edit: user requested dublin core editing AND the recording is still editable
    editable: the recording is editable
    --%>
    <%--@elvariable id="dublincore" type="ch.ethz.replay.ui.common.web.dublincore.DublinCoreCommand"--%>
    <form:form commandName="dublincore" action="save" id="form">

        <c:if test="${!editable}">
            <div class="hint">
                    ${i18n['fragment.dublincore.not-editable-anymore']}
            </div>
        </c:if>

        <%-- DublinCore Table --%>
        <table class="${edit ? 'form': 'list'}">
            <caption>
                <c:out value="${i18n['label.dublincore']}"/>
            </caption>

                <%-- Global erros --%>
            <spring:hasBindErrors name="dublincore">
                <c:if test="${errors.globalErrorCount gt 0}">
                    <tr>
                        <td></td>
                            <%--@elvariable id="errors" type="org.springframework.validation.BeanPropertyBindingResult"--%>
                        <td>
                            <div>Please correct the following errors:</div>
                            <c:forEach var="error" items="${errors.globalErrors}">
                                <span class="error">${i18n[error.code]}</span><br>
                            </c:forEach>
                        </td>
                    </tr>
                </c:if>
            </spring:hasBindErrors>

                <%-- iterate over all languages --%>
            <c:forEach var="language" items="${dublincore.languages}">

                <%-- heading of set --%>
                <spring:bind path="languageSets[${language}]">
                    <%--@elvariable id="set" type="ch.ethz.replay.ui.common.web.dublincore.DcLanguageSet"--%>
                    <c:set var="set" value="${status.value}"/>

                    <tr class="subheading">
                        <td></td>
                        <td>
                            <div class="subheading">
                                <span>${i18n[conc['language.'][set.languageCode]]}</span>
                                    <%-- remove language set button --%>
                                <c:if test="${edit and set.optional}">
                                    <replay:button type="remove"
                                                   action="onclick:submitForm({lang: '${set.languageCode}'}, 'removelang')"/>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </spring:bind>

                <%-- list all DublinCore fields --%>
                <spring:nestedPath path="languageSets[${language}]">
                    <c:forEach var="field" items="${set.supportedFields}">
                        <c:set var="property" value="${field.localName}"/>
                        <c:choose>
                            <%-- dcterms:type --%>
                            <c:when test="${property eq 'type'}">
                                <replay:formRow label="${property}" cssLabelClass="mandatory">
                                    <tf:dcTypeSelector path="type"/>
                                    <form:errors cssClass="error" path="${property}"/>
                                </replay:formRow>
                            </c:when>
                            <%-- replay:promoted | replay:advertised --%>
                            <c:when test="${property eq 'promoted' or property eq 'advertised'}">
                                <replay:formRow label="${property}">
                                    <form:checkbox path="${property}"/>
                                </replay:formRow>
                            </c:when>
                            <%-- dcterms:* --%>
                            <c:otherwise>
                                <replay:viewOrEdit path="${property}" edit="${edit}"
                                                   mandatory="${property eq 'title' and language eq dublincore.defaultLanguage}"/>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </spring:nestedPath>
            </c:forEach>
                <%-- /iterate --%>

                <%-- add new language --%>
                <%--@elvariable id="languages" type="java.util.List"--%>
            <c:if test="${edit and !empty languages}">
                <tr class="subheading">
                    <td>${i18n['label.addLanguage']}</td>
                    <td>
                        <replay:list elements="${languages}" cssClass="select">
                            <a href="#"
                               onclick="submitForm({lang: '${elem}'}, 'addlang')">${i18n[conc['language.'][elem]]}</a>
                        </replay:list>
                    </td>
                </tr>
            </c:if>

        </table>

        <%-- if in edit mode display a submit button --%>
        <c:if test="${edit}">
            <input type="submit" value="${i18n['button.save.text']}" class="form-save"/>
        </c:if>
        <a href="cancel" class="form-cancel">Cancel</a>
    </form:form>

</div>
<div id="result-pane">
    <div class="legend">
        Contributor and Creator may take multiple values separated by semicolon.
    </div>
</div>

</body>
</html>