/*
 * Metadata Selector for jQuery
 * Developed with
 * - jQuery 1.3.1
 * - metadata plugin 3620
 *
 * v0.5
 *
 * Christoph Driessen <ced@neopoly.de>
 *
 * Examples:
 * $('div:meta(number=3)')
 */
(function($) {
    if (typeof $.fn.metadata == 'undefined')
        alert('metaselector jQuery plugin depends on plugin "metadata"');

    $.expr[':'].meta = function(elem, index, args, elemStack) {
        var $this = $(elem);
        var expr = args[3].split('=');
        var metaKey = expr[0];
        if (expr.length > 1) {
            return $this.metadata()[metaKey] == expr[1];
        } else {
            return $this.metadata()[metaKey] != undefined;
        }
    };
})(jQuery);