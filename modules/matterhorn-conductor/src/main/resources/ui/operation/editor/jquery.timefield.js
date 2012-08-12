(function($) {

  $.widget("ui.timefield", {
    // **CHANGE** set default values
    options : {
      value : 0
    },

    /**
     * create the timefield
     */
    _create : function() {
      var self = this;
      this.inputItem = $('<input type="text" />');
      this.inputItem.val(this._format(this.options.value));
      this.inputItem.focusout(function(e) {
        val = self.inputItem.val();
        val = val.split(':');
        newVal = parseInt(val[0]) * 3600;
        newVal += parseInt(val[1]) * 60;
        newVal += parseInt(val[2]);
        newVal += parseInt(val[2].split(".")[1]) / 1000;
        self._setOption('value', newVal);
      });
      this.element.append(this.inputItem);
    },

    /**
     * format the number of seconds and milliseconds to something like
     * hh:MM:ss.mmm
     */
    _format : function(time) {
      if (typeof time == "string") {
        time = time.replace("s", "");
        time = parseFloat(time);
      }
      seconds = parseInt(time);
      millis = parseInt((time - seconds) * 1000);
      formatted = this._formatSeconds(time) + "." + millis;
      return formatted;
    },

    /**
     * format the seconds to something like 00:00:12 from ocUtils
     */
    _formatSeconds : function(seconds) {
      var result = "";
      if (parseInt(seconds / 3600) < 10) {
        result += "0";
      }
      result += parseInt(seconds / 3600);
      result += ":";
      if ((parseInt(seconds / 60) - parseInt(seconds / 3600) * 60) < 10) {
        result += "0";
      }
      result += parseInt(seconds / 60) - parseInt(seconds / 3600) * 60;
      result += ":";
      if (seconds % 60 < 10) {
        result += "0";
      }
      result += Math.round(seconds % 60);
      return result;
    },

    /**
     * set an option
     */
    _setOption : function(key, value) {
      switch (key) {

      case "value":
        value += "";
        if (value.indexOf("s") == -1) {
          value += "s";
        }
        this.inputItem.val(this._format(value));
        this.options.value = value;
        break;
      default:
        this.options[key] = value;
      }
    },

  });
})(jQuery);