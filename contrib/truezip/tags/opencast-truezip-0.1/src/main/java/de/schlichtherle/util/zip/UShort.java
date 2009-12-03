/*
 * Copyright (C) 2007 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schlichtherle.util.zip;

/**
 * Provides constants and static utility methods for unsigned short integer
 * values ({@value SIZE} bits).
 * <p>
 * This class is safe for multithreading.
 * 
 * @author Christian Schlichtherle
 * @version @version@
 * @since TrueZIP 6.7
 */
final class UShort {

    /**
     * The minimum value of an unsigned short integer,
     * which is {@value MIN_VALUE}.
     */
    public static final int MIN_VALUE = 0x0000;

    /**
     * The maximum value of an unsigned short integer,
     * which is {@value MAX_VALUE}.
     */
    public static final int MAX_VALUE = 0xffff;

    /**
     * The number of bits used to represent an unsigned short integer in
     * binary form, which is {@value SIZE}.
     */
    public static final int SIZE = 16;

    /** This class cannot get instantiated. */
    private UShort() {
    }

    /**
     * Checks the parameter range.
     * 
     * @param i The integer to check to be in the range of an unsigned short
     *        integer ({@value SIZE} bits).
     * @param subject The subject of the exception message
     *        - may be <code>null</code>.
     *        This should not end with a punctuation character.
     * @param error First sentence of the exception message
     *        - may be <code>null</code>.
     *        This should not end with a punctuation character.
     * @throws IllegalArgumentException If <code>i</code> is less than
     *         {@link #MIN_VALUE} or greater than {@link #MAX_VALUE}.
     */
    public static final void check(
            final int i,
            final String subject,
            final String error)
    throws IllegalArgumentException {
        if (MIN_VALUE <= i && i <= MAX_VALUE)
            return;

        final StringBuffer message = new StringBuffer();
        if (subject != null) {
            message.append(subject);
            message.append(": ");
        }
        if (error != null) {
            message.append(error);
            message.append(": ");
        }
        message.append(i);
        message.append(" is not within ");
        message.append(MIN_VALUE);
        message.append(" and ");
        message.append(MAX_VALUE);
        message.append(" inclusively.");
        throw new IllegalArgumentException(message.toString());
    }

    /**
     * Checks the parameter range.
     * 
     * @param i The integer to check to be in the range of an unsigned short
     *        integer ({@value SIZE} bits).
     * @throws IllegalArgumentException If <code>i</code> is less than
     *         {@link #MIN_VALUE} or greater than {@link #MAX_VALUE}.
     */
    public static final void check(final int i)
    throws IllegalArgumentException {
        check(i, "Integer out of range", null);
    }
}