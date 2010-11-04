/*

 application.js
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

 */


/* ------------------------------------------------------------------------------- */
/* - This file is deprecated and will be removed or replaced in future releases. - */
/* ------------------------------------------------------------------------------- */

$(document).observe('dom:loaded', function() {

    // user stuff
    if (typeof initPage != 'undefined') {
        initPage();
    }
});

function INJECT_submitForm(params, action) {
    var form = $(this).up('form');
    if (form != undefined) {
        $H(params).each(function(pair) {
            var input = form.select('input[name="' + pair.key + '"]').first();
            if (input == undefined) {
                // form.insert("...") didn't work in Safari 3.x The element wasn't inserted
                // so wie use the Builder.
                form.appendChild(Builder.node('input', {name: pair.key, value: pair.value, type: 'hidden'}));
            } else {
                input.value = pair.value;
            }
        });
        if (action != null) {
            form.action = action;
        }
        form.submit();
    }
}

/**
 * Submits the enclosing form after setting the given parameters.
 *
 * @param params a parameter map
 * @params action An optional form action. Will replace the action defined in the page.
 */
HTMLAnchorElement.prototype.submitForm = INJECT_submitForm;


/**
 * Called within an overlay div it will be returned.
 */
HTMLAnchorElement.prototype.myOverlay = function() {
    return $(this).up('div.overlay');
}

HTMLButtonElement.prototype.submitForm = INJECT_submitForm;

/**
 * Add new function "lift" to Elements.
 */
Element.addMethods({
    lift: function(element, top, left, width, height) {
        element = $(element);
        element.absolutize();
        if (top != null) element.setStyle({top: top + 'px'});
        if (left != null) element.setStyle({left: left + 'px'});
        if (width != null) {
            var w = Object.isNumber(width) ? width : new Number(width);
            if (!isNaN(w)) width = w + 'px';
        } else {
            width = 'auto';
        }
        if (height != null) {
            var h = Object.isNumber(height) ? height : new Number(height);
            if (!isNaN(h)) height = h + 'px';
        } else {
            height = 'auto';
        }
        element.setStyle({width: width, height: height});
    }
});

// --


/**
 * Binds drop down elements to parent elements.
 * By convention drop downs have to be from class "drop-down" with an id
 * starting with their parent's id suffixed with "-drop-down". For example "person" and
 * "person-drop-down" will form such a pair.
 */
function bindDropDowns() {
    $(document.body).getElementsByClassName('drop-down').each(function(dropDown) {
        dropDown.absolutize();
        dropDown.hide();
        dropDown.setStyle({'overflow-y': 'auto'});
        var parentId = dropDown.readAttribute('id').sub('-drop-down', '');
        var parent = $(parentId);
        dropDown.clonePosition(parent, {offsetTop: parent.getHeight()});
    });
}

function repositionDropDowns() {
    $(document.body).getElementsByClassName('drop-down').each(function(dropDown) {
        var parentId = dropDown.identify().sub('-drop-down', '');
        var parent = $(parentId);
        dropDown.clonePosition(parent, {offsetTop: parent.getHeight()});
    });
}

function submitOnEnter(event, form) {
    var charCode = (event.which) ? event.which : event.keyCode
    if (charCode == "13") {
        form.submit();
    }
}

