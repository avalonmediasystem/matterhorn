/*
 
 Tuple5.java
 Written and maintained by Christoph Driessen <ced@neopoly.de>
 Created Sep 18, 2009

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

package ch.ethz.replay.ui.scheduler.util;

/**
 * Tuple of 5.
 * 
 * 
 */
public class Tuple5<A, B, C, D, E> {

  private A _1;
  private B _2;
  private C _3;
  private D _4;
  private E _5;

  public Tuple5(A _1, B _2, C _3, D _4, E _5) {
    this._1 = _1;
    this._2 = _2;
    this._3 = _3;
    this._4 = _4;
    this._5 = _5;
  }

  public A get_1() {
    return _1;
  }

  public B get_2() {
    return _2;
  }

  public C get_3() {
    return _3;
  }

  public D get_4() {
    return _4;
  }

  public E get_5() {
    return _5;
  }
}
