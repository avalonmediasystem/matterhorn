/*

 datepicker.js
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Jun 01, 2008

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

/*
  DatePicker
  Based on prototype.js 1.6.0 and datejs 1.0 Alpha-1.

  @author Christoph E. Driessen <ced@neopoly.de>
*/

// If DATEPICKER_DISABLED_AUTOBIND is not set autobind appropriatly tagged input fields.
if (typeof DATEPICKER_DISABLE_AUTOBIND == 'undefined') {
  $(document).observe('dom:loaded', function() {
    new DatePicker.AutoBinder();
  });
}

var DatePicker = { }

/**
 * AutoBinder. By default text input fields with class "input-date" are bound.
 */
DatePicker.AutoBinder = Class.create({

  initialize: function(params) {
    var className = params != null && params.className != null
        ? params.className
        : 'input-date';
    $$('input[type="text"].' + className).each(function(e) {
      new DatePicker.Picker({field: e})
    });
  }
});

/**
 * The DatePicker.
 *
 * Datepicker fires an "datepicker:change" event on the host text field if the
 * user picks a date.
 * 
 * @param field the host text input field this picker shall be bound to
 */
DatePicker.Picker = Class.create({

  initialize: function(params) {
    this.host = $(params.field);
    this.cssClass = params.cssClass;
    this.selectedDay = null;
    this._init();
  },

  _init: function() {
    // create elements
    var cssClass = 'datepicker' + (this.cssClass != null ? ' ' + this.cssClass : '');
    var datepicker = Builder.node('div', {className: cssClass}, [
      header = Builder.node('div', {className: 'menubar'}),
      this.calContainer = Builder.node('div'),
      timeslider = Builder.node('div', {className: 'time-select'})
    ]);
    this._createNavigation(header);

    // insert and position picker
    this.host.insert({after: datepicker});
    var zIndex = this.host.getStyle('zIndex');
    datepicker.setStyle({'zIndex': zIndex == null ? 99 : zIndex + 1});

    this._createTimeSlider(timeslider);
    // hide AFTER creating the sliders otherwise they won't work!
    datepicker.hide();
    // register listener
    this.host.observe('focus', this._onFocus.bindAsEventListener(this))
    this.host.observe('keyup', this._onKeyUp.bindAsEventListener(this))
    // save reference
    this.datepicker = datepicker;
  },

  _onFocus: function() {
    this.setDate(this.host.value);
    this.show();
  },

  _onKeyUp: function(event) {
    if (this.visible()) {
      this.setDate(this.host.value);
    }
  },

  show: function() {
    if (!this.visible()) {
      this.datepicker.clonePosition(this.host,
      {offsetTop: this.host.getHeight(), setHeight: false, setWidth: false});
      new Effect.Appear(this.datepicker, {duration: 0.3});
    }
  },

  hide: function() {
    if (this.visible()) {
      new Effect.Fade(this.datepicker, {duration: 0.3});
    }
  },

  visible: function() {
    return this.datepicker.visible();
  },

  // navigation functions

  previousWeek: function() {
    this.placeCalendar(this.viewStartDate.clone().previous().week());
  },

  nextWeek: function() {
    this.placeCalendar(this.viewStartDate.clone().next().week());
  },

  previousMonth: function() {
    this.placeCalendar(this.viewStartDate.clone().sunday().previous().month().moveToFirstDayOfMonth(), true);
  },

  nextMonth: function() {
    this.placeCalendar(this.viewStartDate.clone().sunday().next().month().moveToFirstDayOfMonth(), true);
  },

  today: function() {
    this.placeCalendar(Date.today().moveToFirstDayOfMonth());
  },

  selection: function() {
    if (this.selectedDay != null) {
      this.placeCalendar(this.selectedDay.clone().moveToFirstDayOfMonth());
    }
  },

  _onOk: function() {
    this._updateInputField();
    this.hide();
  },

  _onClose: function() {
    this.hide();
  },

  /**
   * @param event event or null
   */
  _onDatePicked: function(event) {
    var date = Date.parse(event.target.identify());
    // save selected date but before setting any time
    this._saveSelectedDay(date);
    $$('td.selected').each(function(e) {
      e.removeClassName('selected')
    });
    event.target.addClassName('selected');
  },

  _saveSelectedDay: function(day) {
    this.selectedDay = day.clone();
  },

  _updateInputField: function() {
    var date = this.selectedDay;
    if (date != null) {
      date = date.clone();
      var time = Date.parse(this.time.innerHTML);
      date.set({hour: time.getHours(), minute: time.getMinutes()});
      // update text input field
      this.host.value = date.toString('d') + " " + date.toString('t');
      this.host.fire('datepicker:change');
    }
  },

  /**
   * Sets the date.
   *
   * @param date String or Date
   */
  setDate: function(date) {
    if (Object.isString(date)) {
      if (date.blank()) date = new Date();
      else date = Date.parse(date);
    } else if (date == null) {
      date = new Date();
    }
    try {
      this.sliderHour.setValue(date.getHours() + 1);
      this.sliderMinute.setValue(date.getMinutes() + 1);
      // save the selected day
      var day = this.stripTime(date);
      this._saveSelectedDay(day);
      // display calendar
      this.placeCalendar(day.moveToFirstDayOfMonth(), true);
    } catch(ignore) {
      // in case Date.parse delivered an invalid object
    }
  },

  _displayTime: function() {
    var d = new Date();
    d.setHours(Math.round(this.sliderHour.value - 1));
    d.setMinutes(Math.round(this.sliderMinute.value - 1));
    this.time.update(d.toString('t'));
  },

  // builder functions

  _createNavigation: function(navBar) {
    //navBar.appendChild(Builder.node('table', {className: 'navbar'}, bar = Builder.node('tr')));
    var bar = navBar;
    bar.appendChild(this._createNavButton(' ', this.previousMonth));
    bar.appendChild(this._createNavButton(' ', this.previousWeek));
    bar.appendChild(this._createNavButton(' ', this.today));
    bar.appendChild(this._createNavButton(' ', this.selection));
    bar.appendChild(this._createNavButton(' ', this.nextWeek));
    bar.appendChild(this._createNavButton(' ', this.nextMonth));
    bar.appendChild(this._createNavButton(' ', this._onClose));
    bar.appendChild(this._createNavButton(' ', this._onOk));
  },

  _createNavButton: function(text, callback) {
    //var b = Builder.node('td', {className: 'nav-button'}, text);
    var b = Builder.node('span', {className: 'button'}, text);
    b.observe('click', callback.bindAsEventListener(this));
    return b;
  },

  /**
   * Creates and places a new calendar beginning at date 'date'.
   *
   * @param date a Date
   */
  placeCalendar: function(date, exact) {
    // empty calendar container
    this.calContainer.childElements().each(function(e) {
      e.remove()
    });
    // ...and fill with new
    this._createCalendar(this.calContainer, date, exact);
  },

  /**
   * @param container container element
   * @param date The start date. Will be normalized to first day of week which is monday.
   * @param exact show exactly one month
   */
  _createCalendar: function(container, date, exact) {
    var viewStart = date.clone();
    // correct start date to last monday
    if (!viewStart.is().monday()) viewStart.last().monday();
    // keep start date
    this.viewStartDate = viewStart.clone();

    var month = viewStart.clone().sunday();
    var viewEnd = exact
        ? month.clone().moveToLastDayOfMonth()
        : month.clone().add(1).month();
    if (!viewEnd.is().sunday()) viewEnd.sunday();

    container.insert('<div class="month-year">' +
                     month.toString('MMMM') + " " + month.toString('yyyy') +
                     '</div>');
    container.appendChild(Builder.node('table', {className: 'calendar'}, [
      Builder.node('thead', headRow = Builder.node('tr')),
      body = Builder.node('tbody')]));

    // create calendar
    var headBuilt = false;
    var week = Builder.node('tr');
    for (var day = viewStart; !day.isAfter(viewEnd); day.add(1).day()) {
      if (!headBuilt) {
        headRow.insert("<th>" + day.toString("ddd") + "</th>");
      }
      //
      week.appendChild(this._createDayElement(day, month));
      if (day.is().sunday()) {
        body.appendChild(week);
        headBuilt = true;
        if (!day.equals(viewEnd)) week = Builder.node('tr');
      }
    }
  },

  /**
   * Creates a new day element.
   *
   * @param day the day for which an element shall be created
   * @param month the current month
   */
  _createDayElement: function(day, month) {
    var e = Builder.node('td', {id: day.toString('d')}, day.toString(' d'));
    // register listener
    e.observe('click', this._onDatePicked.bindAsEventListener(this));
    // day in current month?
    if (day.getMonth() == month.getMonth()) {
      e.addClassName('cm');
    }
    // today?
    if (day.equals(Date.today())) {
      e.addClassName('today');
    }
    if (day.equals(this.selectedDay)) {
      e.addClassName('selected');
    }
    return e;
  },

  _createTimeSlider: function(container) {
    // 8:00 is default
    var h = 9, m = 1;
    container.appendChild(this.time = Builder.node('div', {className: 'time'}));
    this._createSlider(container, 'hour', $R(1, 24), h);
    this._createSlider(container, 'minute', $R(1, 60), m);
    this._displayTime();
  },

  _createSlider: function(container, name, range, startValue) {
    var slider = Builder.node('div', {className: 'slider'},
        handle = Builder.node('div', {className: 'handle ' + name}));
    container.appendChild(slider);
    var callback = this._displayTime.bindAsEventListener(this);
    this['slider' + name.capitalize()] = new Control.Slider(handle, slider, {
      range: range,
      sliderValue: startValue,
      onSlide: callback,
      onChange: callback
    });
  },

  // helper functions

  /**
   * Strips of any time information from date and returns it in a new Date.
   * This method leaves 'date' untouched.
   *
   * @param date a Date
   */
  stripTime: function(date) {
    var day = date.clone();
    day.setHours(0);
    day.setMinutes(0);
    day.setSeconds(0);
    day.setMilliseconds(0);
    return day;
  }
});