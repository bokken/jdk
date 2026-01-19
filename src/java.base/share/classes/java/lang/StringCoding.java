/*
 * Copyright (c) 2000, 2025, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2024, Alibaba Group Holding Limited. All Rights Reserved.
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

package java.lang;

import jdk.internal.util.Preconditions;
import jdk.internal.vm.annotation.IntrinsicCandidate;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

/**
 * Utility class for string encoding and decoding.
 */
class StringCoding {

    private StringCoding() { }

    /**
     * Count the number of leading non-zero ascii chars in the range.
     */
    static int countNonZeroAscii(String s) {
        byte[] value = s.value();
        if (s.isLatin1()) {
            return countNonZeroAsciiLatin1(value, 0, value.length);
        } else {
            return countNonZeroAsciiUTF16(value, 0, s.length());
        }
    }

    /**
     * Count the number of non-zero ascii chars in the range.
     */
    private static int countNonZeroAsciiLatin1(byte[] ba, int off, int len) {
        int limit = off + len;
        for (int i = off; i < limit; i++) {
            if (ba[i] <= 0) {
                return i - off;
            }
        }
        return len;
    }

    /**
     * Count the number of leading non-zero ascii chars in the range.
     */
    private static int countNonZeroAsciiUTF16(byte[] ba, int off, int strlen) {
        int limit = off + strlen;
        for (int i = off; i < limit; i++) {
            char c = StringUTF16.charAt(ba, i);
            if (c == 0 || c > 0x7F) {
                return i - off;
            }
        }
        return strlen;
    }

    static boolean hasNegatives(byte[] ba, int off, int len) {
        return countPositives(ba, off, len) != len;
    }

    /**
     * Count the number of leading positive bytes in the range.
     *
     * @implSpec the implementation must return len if there are no negative
     *   bytes in the range. If there are negative bytes, the implementation must return
     *   a value that is less than or equal to the index of the first negative byte
     *   in the range.
     *
     * @param ba a byte array
     * @param off the index of the first byte to start reading from
     * @param len the total number of bytes to read
     * @throws NullPointerException if {@code ba} is null
     * @throws ArrayIndexOutOfBoundsException if the provided sub-range is
     *         {@linkplain Preconditions#checkFromIndexSize(int, int, int, BiFunction) out of bounds}
     */
    static int countPositives(byte[] ba, int off, int len) {
        Preconditions.checkFromIndexSize(
                off, len,
                ba.length,      // Implicit null check on `ba`
                Preconditions.AIOOBE_FORMATTER);
        return countPositives0(ba, off, len);
    }

    @IntrinsicCandidate
    private static int countPositives0(byte[] ba, int off, int len) {
        int limit = off + len;
        for (int i = off; i < limit; i++) {
            if (ba[i] < 0) {
                return i - off;
            }
        }
        return len;
    }

    /**
     * Encodes as many ISO-8859-1 codepoints as possible from the source byte
     * array containing characters encoded in UTF-16, into the destination byte
     * array, assuming that the encoding is ISO-8859-1 compatible.
     *
     * @param sa the source byte array containing characters encoded in UTF-16
     * @param sp the index of the <em>character (not byte!)</em> from the source array to start reading from
     * @param da the target byte array
     * @param dp the index of the target array to start writing to
     * @param len the maximum number of <em>characters (not bytes!)</em> to be encoded
     * @return the total number of <em>characters (not bytes!)</em> successfully encoded
     * @throws NullPointerException if any of the provided arrays is null
     */
    static int encodeISOArray(byte[] sa, int sp,
                              byte[] da, int dp, int len) {
        // This method should tolerate invalid arguments, matching the lenient behavior of the VM intrinsic.
        // Hence, using operator expressions instead of `Preconditions`, which throw on failure.
        int sl;
        if ((sp | dp | len) < 0 ||
                // Halving the length of `sa` to obtain the number of characters:
                sp >= (sl = sa.length >>> 1) ||     // Implicit null check on `sa`
                dp >= da.length) {                  // Implicit null check on `da`
            return 0;
        }
        int minLen = Math.min(len, Math.min(sl - sp, da.length - dp));
        return encodeISOArray0(sa, sp, da, dp, minLen);
    }

    @IntrinsicCandidate
    private static int encodeISOArray0(byte[] sa, int sp,
                                       byte[] da, int dp, int len) {
        int i = 0;
        for (; i < len; i++) {
            char c = StringUTF16.getChar(sa, sp++);
            if (c > '\u00FF')
                break;
            da[dp++] = (byte)c;
        }
        return i;
    }

    /**
     * Encodes as many ASCII codepoints as possible from the source
     * character array into the destination byte array, assuming that
     * the encoding is ASCII compatible.
     *
     * @param sa the source character array
     * @param sp the index of the source array to start reading from
     * @param da the target byte array
     * @param dp the index of the target array to start writing to
     * @param len the maximum number of characters to be encoded
     * @return the total number of characters successfully encoded
     * @throws NullPointerException if any of the provided arrays is null
     */
    static int encodeAsciiArray(char[] sa, int sp,
                                byte[] da, int dp, int len) {
        // This method should tolerate invalid arguments, matching the lenient behavior of the VM intrinsic.
        // Hence, using operator expressions instead of `Preconditions`, which throw on failure.
        if ((sp | dp | len) < 0 ||
                sp >= sa.length ||      // Implicit null check on `sa`
                dp >= da.length) {      // Implicit null check on `da`
            return 0;
        }
        int minLen = Math.min(len, Math.min(sa.length - sp, da.length - dp));
        return encodeAsciiArray0(sa, sp, da, dp, minLen);
    }

    @IntrinsicCandidate
    static int encodeAsciiArray0(char[] sa, int sp,
                                 byte[] da, int dp, int len) {
        int i = 0;
        for (; i < len; i++) {
            char c = sa[sp++];
            if (c >= '\u0080')
                break;
            da[dp++] = (byte)c;
        }
        return i;
    }


    static int copy8Latin1ByteUTF8(byte[] val, int offset, ByteBuffer target, int remaining) {
        // assert target.remaining() >= 16;
        // assert val.length >= offset + 8;
        byte c = val[offset];
        if (c < 0) {
            if (remaining == 1) {
                return 0;
            }
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else {
            target.put(c);
            --remaining;
        }
        
        c = val[offset + 1];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else if (remaining >= 1) {
            target.put(c);
            --remaining;
        } else {
            return 1;
        }
        
        c = val[offset + 2];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else if (remaining >= 1) {
            target.put(c);
            --remaining;
        } else {
            return 2;
        }
        
        c = val[offset + 3];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else if (remaining >= 1) {
            target.put(c);
            --remaining;
        } else {
            return 3;
        }
        
        c = val[offset + 4];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else if (remaining >= 1) {
            target.put(c);
            --remaining;
        } else {
            return 4;
        }
        
        c = val[offset + 5];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else if (remaining >= 1) {
            target.put(c);
            --remaining;
        } else {
            return 5;
        }
        
        c = val[offset + 6];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
            remaining -= 2;
        } else if (remaining >= 1) {
            target.put(c);
            --remaining;
        } else {
            return 6;
        }
        
        c = val[offset + 7];
        if (c < 0 && remaining >= 2) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else if (remaining >= 1) {
            target.put(c);
        } else {
            return 7;
        }

        return 8;
    }

    static void copy8Latin1ByteUTF8NoRemainingCheck(byte[] val, int offset, ByteBuffer target) {
        // assert target.remaining() >= 16;
        // assert val.length >= offset + 8;
        byte c = val[offset];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 1];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 2];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 3];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 4];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 5];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 6];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }

        c = val[offset + 7];
        if (c < 0) {
            target.putShort((short) ((((0xc0 | ((c & 0xff) >> 6)) & 0xFF) << 8)
                    | ((0x80 | (c & 0x3F)) & 0xFF)));
        } else {
            target.put(c);
        }
    }

    static int encodeUTF8_UTF16(byte[] val, ByteBuffer target, int srcOffset, int len) {
        if (target.hasArray()) {
            return encodeUTF8_UTF16_Array(val, target.array(), srcOffset, target.arrayOffset() + target.position(), len, target.remaining());
        }
        return encodeUTF8_UTF16_Buffer(val, target, srcOffset, len);
    }

    private static int encodeUTF8_UTF16_Buffer(byte[] val, ByteBuffer target, int srcOffset, int len) {
        int sp = srcOffset;
        int sl = sp + len;

        while (sp < sl) {
            // ascii fast loop;
            char c = StringUTF16.getChar(val, sp);
            if (c >= '\u0080') {
                break;
            }
            target.put((byte)c);
            sp++;
        }
        int remaining = target.remaining();
        while (sp < sl) {
            char c = StringUTF16.getChar(val, sp++);
            if (c < 0x80 && remaining >= 1) {
                target.put((byte)c);
                --remaining;
            } else if (c < 0x800 && remaining >= 2) {
                target.put((byte)(0xc0 | (c >> 6)));
                target.put((byte)(0x80 | (c & 0x3f)));
                remaining -= 2;
            } else if (Character.isSurrogate(c)) {
                int uc = -1;
                char c2;
                if (Character.isHighSurrogate(c) && sp < sl &&
                        Character.isLowSurrogate(c2 = StringUTF16.getChar(val, sp))) {
                    uc = Character.toCodePoint(c, c2);
                }
                if (uc < 0 || remaining <= 4) {
                    break;
                }

                target.put((byte)(0xf0 | (uc >> 18)));
                target.put((byte)(0x80 | ((uc >> 12) & 0x3f)));
                target.put((byte)(0x80 | ((uc >>  6) & 0x3f)));
                target.put((byte)(0x80 | (uc & 0x3f)));
                sp++;  // 2 chars
                remaining -= 4;
            } else if (remaining >= 3) {
                // 3 bytes, 16 bits
                target.put((byte)(0xe0 | (c >> 12)));
                target.put((byte)(0x80 | ((c >>  6) & 0x3f)));
                target.put((byte)(0x80 | (c & 0x3f)));
                remaining -= 3;
            } else {
                break;
            }
        }
        return sp - srcOffset;
    }

    static int encodeUTF8_UTF16_Array(byte[] val, byte[] target, int srcOffset, int targetOffset, int len, int targetRemaining) {
        int sp = srcOffset;
        int sl = sp + len;

        while (sp < sl) {
            // ascii fast loop;
            char c = StringUTF16.getChar(val, sp);
            if (c >= '\u0080') {
                break;
            }
            target[targetOffset++] = (byte) c;
            sp++;
            --targetRemaining;
        }
        while (sp < sl) {
            char c = StringUTF16.getChar(val, sp++);
            if (c < 0x80 && targetRemaining >= 1) {
                target[targetOffset++] = (byte) c;
                --targetRemaining;
            } else if (c < 0x800 && targetRemaining >= 2) {
                target[targetOffset++] = (byte)(0xc0 | (c >> 6));
                target[targetOffset++] = (byte)(0x80 | (c & 0x3f));
                targetRemaining -= 2;
            } else if (Character.isSurrogate(c)) {
                int uc = -1;
                char c2;
                if (Character.isHighSurrogate(c) && sp < sl &&
                        Character.isLowSurrogate(c2 = StringUTF16.getChar(val, sp))) {
                    uc = Character.toCodePoint(c, c2);
                }
                if (uc < 0 || targetRemaining <= 4) {
                    break;
                }

                target[targetOffset++] = (byte)(0xf0 | (uc >> 18));
                target[targetOffset++] = (byte)(0x80 | ((uc >> 12) & 0x3f));
                target[targetOffset++] = (byte)(0x80 | ((uc >>  6) & 0x3f));
                target[targetOffset++] = (byte)(0x80 | (uc & 0x3f));
                sp++;  // 2 chars
                targetRemaining -= 4;
            } else if (targetRemaining >= 3) {
                // 3 bytes, 16 bits
                target[targetOffset++] = (byte)(0xe0 | (c >> 12));
                target[targetOffset++] = (byte)(0x80 | ((c >>  6) & 0x3f));
                target[targetOffset++] = (byte)(0x80 | (c & 0x3f));
                targetRemaining -= 3;
            } else {
                break;
            }
        }
        return sp - srcOffset;
    }
}
