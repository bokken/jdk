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

    boolean isLatin1();

    /**
     * Provides a {@link ByteBuffer#asReadOnlyBuffer()) access to the latin-1 bytes.
     * The {@code ByteBuffer} returned will reflect the <i>offset</i> and <i>len</i>
     * and will be at {@link ByteBuffer#position{}) of {@code 0} with both
     * {@link ByteBuffer#capacity()} and {@link ByteBuffer#limit()} reflecting the
     * <i>len</i>. This means it is not possible to access values outside the requested
     * range.
     * 
     * @param offset The offset (in characters) to start at.
     * @param len    The max number of characters to make available.
     * @return {@link ByteBuffer#asReadOnlyBuffer()Read-only) access to the latin-1 bytes.
     * @throws IllegalStateException if {@link #isLatin1()} is {@code false}.
     * @throws IndexOutOfBoundsException if {@code offset < 0} or
     *                                   {@code offset + len} exceeds length of this
     *                                   object.
     */
    ByteBuffer getLatin1Bytes(int offset, int len);
}
