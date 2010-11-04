/*

 activestyles.js
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Oct 27, 2008

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

jQuery(function($) {
    /**
     * Assigns table rows class "odd" or "even" grouped by tbody
     */
    $.colorRows = function() {
        $('table.alternating-rows').each(function() {
            $('tbody tr', this).each(function(i) {
                $(this).addClass((i + 1) % 2 == 0 ? 'even' : 'odd');
            });
        });
    };
    $.colorRows();

    // Code copied from http://stackoverflow.com/questions/470772/does-jquery-have-an-equivalent-to-prototypes-element-identify
    $.fn.identify = function(prefix) {
        var i = 0;
        return this.each(function() {
            if ($(this).attr('id')) return;
            do {
                i++;
                var id = prefix + '_' + i;
            } while ($('#' + id).length > 0);
            $(this).attr('id', id);
        });
    };

    /**
     * Fold
     *
     * Divs of class "fold" are enriched with a button to toggle it's visibility.
     * The title attribute of the DIV is taken into account for building the toggle button.
     * Folds store their state into a cookie.
     * The toggle-button is a rendered as an "a" tag with class "fold-button".
     */
    $("div.fold").each(function() {
        var $fold = $(this);
        // Ensure fold has an ID
        $fold.identify("fold");
        
        var open = "> " + $fold.attr("title");
        var closed = "< " + $fold.attr("title");
        var btn = (function() {
            // Close if necessary
            if ($.cookies.get(cookieName($fold)) == "closed") {
                $fold.css("display", "none");
                return closed;
            } else {
                return open;
            }
        })();

        $fold.before($("<a class='fold-button'>" + btn + "</a>").click(function() {
            var $btn = $(this);
            if ($fold.is(":hidden")) show($fold, $btn);
            else hide($fold, $btn);
            // If in a compasslayout environment do a relayout
            $.compassLayout.layout();
        }));

        if (btn == closed) $.compassLayout.layout();

        //

        function show($fold, $btn) {
            $fold.css("display", "block");
            $btn.text(open);
            $.cookies.set(cookieName($fold), "open");
        }

        function hide($fold, $btn) {
            $fold.css("display", "none");
            $btn.text(closed);
            $.cookies.set(cookieName($fold), "closed");
        }

        function cookieName($fold) {
            return "#fold-" + $fold.attr("id");
        }
    });
});

