/*
 * Form submission
 *
 * Christoph Driessen <ced@neopoly.de>
 */
(function($) {
    $.logVersion = 0.9;

    $.log = function(msg) {
        if (window.console && window.console.log)
            window.console.log(msg);
    };

    $.fn.dump = function(sep) {
        if (sep) $.log(sep);
        this.each(function() {
            $.log(this);
        });
        return this;
    };
})(jQuery);
