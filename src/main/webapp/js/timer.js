/* ------------------------------------------------------------------------------- */
/* - This file is deprecated and will be removed or replaced in future releases. - */
/* ------------------------------------------------------------------------------- */

var Timer = function(delay) {
    this.delay = delay
    this.timerID = undefined;
};
Timer.prototype.exec = function(f) {
    if (this.timerID) clearTimeout(this.timerID);
    this.timerID = setTimeout(f, this.delay);
};