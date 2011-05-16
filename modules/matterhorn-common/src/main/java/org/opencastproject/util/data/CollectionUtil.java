/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */

package org.opencastproject.util.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionUtil {

  private CollectionUtil() {
  }

  private static <A, B> Collection<A> buildFrom(Collection<B> as) {
    try {
      return as.getClass().newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Type " + as.getClass() + " needs a parameterless constructor");
    }
  }

  /**
   * Gets a value from a map, creating a new one, if there is no such value, i.e. it is null.
   */
  public static <K, V> V getOrCreate(Map<K, V> map, K key, Creator<V> c) {
    V v = map.get(key);
    if (v == null) {
      v = c.create();
      map.put(key, v);
    }
    return v;
  }

  /**
   * Apply a function <code>f</code> to all elements of collection <code>as</code>
   * to produce a new collection <code>bs</code>.
   * <p/>
   * An (empty) instance of the target collection has to be provided explicitly.
   */
  public static <A, B, BB extends Collection<B>> BB map(Collection<A> as, BB bs, Function<A, B> f) {
    for (A x : as) {
      bs.add(f.apply(x));
    }
    return bs;
  }

  /**
   * Apply a function <code>f</code> to all elements of collection <code>as</code>
   * to produce a new collection <code>bs</code>.
   * <p/>
   * The type of collection <code>as</code> needs a parameterless constructor.
   * <p/>
   * Please note that since java does not support higher-order polymorphism -- which is needed
   * to capture the type of the collection -- some casting on the client side may still be
   * necessary.
   *
   * @throws RuntimeException
   *         if the target collection cannot be created
   */
  public static <A, B> Collection<B> map(Collection<A> as, Function<A, B> f) {
    Collection<B> b = buildFrom(as);
    for (A x : as) {
      b.add(f.apply(x));
    }
    return b;
  }

  /**
   * Apply a function <code>f</code> to all elements of collection <code>as</code>
   * to produce a new collection <code>bs</code> by concatenating the results.
   * <p/>
   * The type of collection <code>as</code> needs a parameterless constructor.
   * <p/>
   * Please note that since java does not support higher-order polymorphism -- which is needed
   * to capture the type of the collection -- some casting on the client side may still be
   * necessary.
   *
   * @throws RuntimeException
   *         if the result collection cannot be created
   */
  public static <A, B> Collection<B> flatMap(Collection<A> as, Function<A, Collection<B>> f) {
    Collection<B> bs = buildFrom(as);
    for (A a : as) {
      bs.addAll(f.apply(a));
    }
    return bs;
  }

  /**
   * Exactly like {@link #flatMap(java.util.Collection, Function)} but you have to provide
   * the target collection yourself.
   */
  public static <A, B, BB extends Collection<B>> BB flatMap(Collection<A> as, BB bs, Function<A, Collection<B>> f) {
    for (A a : as) {
      bs.addAll(f.apply(a));
    }
    return bs;
  }

  /**
   * Returns the first element in <code>as</code> that satisfies a predicate <code>p</code>.
   */
  public static <A> Option<A> find(Collection<A> as, Predicate<A> p) {
    for (A x : as) {
      if (p.apply(x)) return Option.some(x);
    }
    return Option.none();
  }

  /**
   * Tests if at least one element in <code>as</code> satisfies predicate <code>p</code>.
   */
  public static <A> boolean exists(Collection<A> as, Predicate<A> p) {
    for (A a : as) {
      if (p.apply(a)) return true;
    }
    return false;
  }

  /**
   * Return the head of list <code>as</code> or none.
   */
  public static <A> Option<A> head(List<A> as) {
    if (!as.isEmpty())
      return Option.some(as.get(0));
    return Option.none();
  }

  /**
   * Return a new collection containing only the elements that satisfy predicate <code>p</code>.
   * <p/>
   * The type of collection <code>as</code> needs a parameterless constructor.
   */
  public static <A, AA extends Collection<A>> AA filter(AA as, Predicate<A> p) {
    AA filtered = (AA) buildFrom(as);
    for (A a : as) {
      if (p.apply(a))
        filtered.add(a);
    }
    return filtered;
  }

  /**
   * Turns a var arg list into a set.
   * <p/>
   * Returns a {@link java.util.HashSet}.
   */
  public static <A> Set<A> asSet(A... as) {
    Set<A> r = new HashSet<A>(as.length);
    for (A a : as) r.add(a);
    return r;
  }

}