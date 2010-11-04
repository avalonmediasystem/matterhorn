/*

 dropshadow.js
 Written and maintained by Christoph E. Driessen <ced@neopoly.de>
 Created Jun 05, 2008

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

/*
  Based on prototype.js 1.6.0

  @author Christoph E. Driessen <ced@neopoly.de>
*/

$(document).observe('dom:loaded', function() {
  $$('.dropshadow').each(function(e) {
    e = $(e);
    //e.absolutize();
//    var zIndex = e.getStyle('zIndex');
//    if (zIndex == null) {
//      e.setStyle({zIndex: 99});
//      zIndex = 98;
//    } else {
//      zIndex -= 1;
//    }

    var shadow = Builder.node('table', [
        Builder.node('tr'), [
          left = Builder.node('td'), right = Builder.node('td')
        ]]);
    //shadow.relativize();
    //shadow2.relativize();
    //shadow.absolutize();
    //shadow.clonePosition(e);
    shadow.setStyle({
      //width: (e.getWidth() + 8) + 'px',
      //height: (e.getHeight() + 8) + 'px',
      padding: '0',
      margin: '0 0 0 0',
      border: '0',
      'float': 'left'});
//    e.setStyle({
//      width: (e.getWidth() + 8) + 'px',
//      height: (e.getHeight() + 8) + 'px'});

    // transfer children of e to the shadow container
    $A(e.childElements()).each(function(e) {
      left.appendChild($(e).remove());
    });
    e.appendChild(shadow);

    //if (!e.visible()) shadow.hide();

    //e.insert({after: shadow});
  });
});

