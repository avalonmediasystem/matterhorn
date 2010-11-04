/*

 foldable.js
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Aug 07, 2008

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
 * Requires
 * - prototype javascript library >= 1.6.0
 * - scriptaculous >= 1.8.0
 *
 * todo do a dependency check
 * todo documentation and examples
 */

// Bind foldable code automatically to elements of class "foldable"
if (typeof FOLDABLE_DISABLE_AUTOBIND == 'undefined') {
    $(document).observe('dom:loaded', function() {
        Foldable.bind();
    });
}

var Foldable = {};

Foldable.Toggler = Class.create({

    /** Fold speed in pixels per second */
    FOLD_SPEED: 200.0,

    /**
     * Create a new Foldable from the given panel element.
     *
     * @param panel e.g. a DIV
     */
    initialize: function(panel) {
        this.panel = panel;
        this.togglerOpen = new Element('div', {'class': 'foldable-open'});
        this.togglerClosed = new Element('div', {'class': 'foldable-closed'});
        //
        var hidden = panel.hasClassName('hidden') || this._isClosed(panel);

        this.toggler = Builder.node('span', hidden ? this.togglerClosed : this.togglerOpen);
        this.toggler.observe('click', this.toggle.bindAsEventListener(this));

        var title = panel.readAttribute('title');
        panel.insert({before: Builder.node('div', {className: 'foldable-header'},
                [this.toggler, title != null ? Builder.node('span', title) : ''])});
        if (hidden) panel.hide();
    },

    toggle: function() {
        var duration = this.panel.getHeight() / this.FOLD_SPEED;
        if (this.panel.visible()) {
            new Effect.BlindUp(this.panel, { duration: duration });
            this.toggler.update(this.togglerClosed);
            this._storeState(this.panel, false);
            this.panel.fire('foldable:close');
        } else {
            new Effect.BlindDown(this.panel, { duration: duration });
            this.toggler.update(this.togglerOpen);
            this._storeState(this.panel, true);
            this.panel.fire('foldable:open');
        }
    },

    _storeState: function(foldable, open) {
        Cookie.write(this._createCookieName(foldable), open ? 'open' : 'closed', 1000);
    },

    _isClosed: function(foldable) {
        return Cookie.read(this._createCookieName(foldable)) == 'closed'
    },

    _createCookieName: function(foldable) {
        return foldable.identify();
    }
});

Foldable.bind = function() {
    $$('.foldable').each(function(e) {
        new Foldable.Toggler(e);
    });
};

