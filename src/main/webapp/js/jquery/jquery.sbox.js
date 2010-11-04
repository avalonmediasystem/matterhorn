/*
 * Link Selectbox jQuery Plugin
 *
 * Christoph Driessen <ced@neopoly.de>
 */
(function($) {
    /**
     *
     * @param child the dependend selectbox
     * @param options (Object, optional)
     */
    $.fn.sboxLink = function(child, options) {
        var settings = $.extend({ select: null }, options);
        var $this = $(this[0]);
        var $child = $(child[0]);
        var $childClone = $child.clone().removeAttr('id');
        if (settings.select) $this.val(settings.select[0]);
        updateChildByOptgroup($this.val());

        $this.change(function() {
            updateChildByOptgroup($this.val());
            // Pass on event
            $child.change();
            $child.focus();
        });

        return this;

        // ----

        function updateChildByOptgroup(val) {
            $child.html($childClone.find('optgroup:meta(depends=' + val + ') option').clone());
            if (settings.select) {
                $child.val(settings.select[1]);
            }
        }

        /*
        function updateChild(val) {
            $child.html($childClone.find(':meta(depends=' + val + ')').clone());
            if (settings.optional) {
                var $optional = $('<option value="' + settings.noneValue + '">' +
                                  settings.noneText + '</option>');
                if ($child.prepend($optional).find(':selected').size() == 0) {
                    $optional.attr('selected', 'selected');
                }
            }
        }
        */
    };

    /**
     * Selects a select box option
     * @param value the value to select
     */
    $.fn.sboxSelect = function(value) {
        if (this.is('select')) {
            $('option', this).each(function() {
                var $this = $(this);
                if ($this.val() == value) {
                    $this.attr('selected', 'selected');
                }
            });
        }
        return this;
    };
})(jQuery);
