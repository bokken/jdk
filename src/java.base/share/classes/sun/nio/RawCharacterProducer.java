/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.nio;

import java.nio.ByteBuffer;

public interface RawCharacterProducer {
    interface RawCharacterConsumer<R> {
        /**
         * Provides the raw binary representation of characters for consumption.
         * @param bytes The raw bytes.
         * @param offset The offset (of character count) into the <i>bytes</i> to start.
         * @param len The number of characters (not bytes) to read.
         */
        R consume(byte[] bytes, int offset, int len);
    }

    <R extends Object> R consume(RawCharacterConsumer<? extends R> consumer);

    /**
     * @return {@code true} if the {@code byte[]} produced contains characters in <i>latin 1</i> or {@code false} if <i>utf-16</i>.
     */
    boolean isLatin1();

    /**
     * Copies any/all ascii characters to <i>target</i> starting at <i>offset</i>
     * and copying up to <i>len</i> or {@link ByteBuffer#remaining()
     * target.remaining()} characters.
     * 
     * Will stop when <i>len</i> is reached, <i>target</i> has no more remaining
     * capacity, or a non-ascii character is encountered.
     * 
     * @param target The buffer to put ascii characters to.
     * @param offset The offset (in characters) to start copying from.
     * @param len    The max number of characters to copy.
     * @return The number of characters copied. Will be {@code >=0}.
     * @throws IndexOutOfBoundsException if {@code offset < 0} or
     *                                   {@code offset + len} exceeds length of this
     *                                   object.
     */
    int copyAscii(ByteBuffer target, int offset, int len);
    int copyLatin1(ByteBuffer target, int offset, int len);
    int copyUTF8(ByteBuffer target, int offset, int len);
}
