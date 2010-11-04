/*
 * Heartbeat for jQuery
 * Developed with
 * - jQuery 1.3.2
 *
 * v0.1.0
 *
 * Heartbeat reloads the content of the selected elements in regular intervals.
 *
 * Options:
 * - data: Data in key/value pairs to send to the server. You can pass a function to calculate the
 *     parameters on each request. The function gets called with the URL: dataFunction(url);
 *     See http://docs.jquery.com/Ajax/load#urldatacallback for details
 * - delay: The delay. Depends on "delayFactor"
 * - startDelay: Start the heartbeat after the given time. Depends on "delayFactor"
 * - delayFactor: Each of the delays is multiplied with this factor to get the actual delay in milliseconds.
 *     Use a factor of 1000 to specify delays in seconds.
 * - complete: Callback function
 *
 * Christoph Driessen <ced@neopoly.de>
 */
(function($) {
    var HEARTBEAT_KEY = 'heartbeat';

    $.fn.heartbeat = function(url, options) {
        var $this = this;
        // Compute settings
        var $s = $.extend({
            data: null,
            delay: 10000,
            startDelay: 0,
            delayFactor: 1000,
            complete: null
        }, options);
        var timerId = this.data(HEARTBEAT_KEY);
        if (timerId) clearTimeout(timerId);
        this.data(HEARTBEAT_KEY, setTimeout(beat, $s.startDelay * $s.delayFactor));
        var fun = $.isFunction($s.data);
        return this;

        function beat() {
            $this.load(url, fun ? $s.data(url) : $s.data, $s.complete);
            $this.data(HEARTBEAT_KEY, setTimeout(beat, $s.delay * $s.delayFactor));
        }
    };

    $.fn.stopHeartbeat = function() {
        var timerId = this.data(HEARTBEAT_KEY);
        if (timerId) clearTimeout(timerId);
        return this;
    };
})(jQuery);
