/*
 * Copyright (c) 2008, 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * @test
 * @bug 6419791
 * @summary
 * @run junit ISO8859x
 * @author Martin Buchholz
 */
public class ISO8859x {
    private static final byte[] lowBytes = new byte[0xa0];
    private static final char[] lowChars = new char[0xa0];
    private static final String lowString;
    private static final List<Charset> charsets = Charset.availableCharsets().values().stream()
                                                         .filter(cs -> cs.name().matches(".*(8859).*"))
                                                         .toList();

    static {
        for (int i = 0; i < 0xa0; i++) {
            lowBytes[i] = (byte) i;
            lowChars[i] = (char) i;
        }
        lowString = new String(lowChars);
    }

    private static List<Charset> charsets() {
        return charsets;
    }

    private static List<Arguments> encodeArguments() {
        List<Arguments> args = new ArrayList<>(charsets.size() * 5);
        for (Charset c : charsets) {
            String name = c.name();
            Function<String, CharBuffer> cbFx = CharBuffer::wrap;
            args.add(Arguments.of(c, cbFx, name + " wrapped String"));
            cbFx = s -> CharBuffer.wrap(s.toCharArray());
            args.add(Arguments.of(c, cbFx, name + " wrapped char[]"));
            cbFx = s -> CharBuffer.wrap(s.toCharArray()).asReadOnlyBuffer();
            args.add(Arguments.of(c, cbFx, name + " read only wrapped char[]"));
            cbFx = s -> CharBuffer.wrap(CharBuffer.wrap(s));
            args.add(Arguments.of(c, cbFx, name + " wrapped wrapped String"));
        }
        return args;
    }

    @ParameterizedTest
    @MethodSource("charsets")
    void testCanEncode(Charset cs) {
        assertTrue(cs.canEncode());
    }

    @ParameterizedTest
    @MethodSource("charsets")
    void testGetBytes(Charset cs) throws Exception {
        String csn = cs.name();
        assertArrayEquals(lowBytes, lowString.getBytes(csn));
    }

    @ParameterizedTest
    @MethodSource("charsets")
    void testNewString(Charset cs) throws Exception {
        String csn = cs.name();
        assertEquals(lowString, new String(lowBytes, csn));
    }

    @ParameterizedTest(name="{2}")
    @MethodSource("encodeArguments")
    void testEncodeAscii(Charset cs, Function<String, CharBuffer> cbFunction, String name) {
        CharsetEncoder encoder = cs.newEncoder();
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT)
               .onMalformedInput(CodingErrorAction.REPORT);
        ByteBuffer target = ByteBuffer.allocate(lowBytes.length);
        CharBuffer cb = cbFunction.apply(lowString);
        CoderResult result = encoder.encode(cb, target, true);
        assertEquals(CoderResult.UNDERFLOW, result);
        assertFalse(cb.hasRemaining());
        assertArrayEquals(lowBytes, target.array());
    }

    @ParameterizedTest(name="{2}")
    @MethodSource("encodeArguments")
    void testEncoderDecoder(Charset cs, Function<String, CharBuffer> cbFunction, String name) throws Exception {
        CharsetEncoder encoder = cs.newEncoder();
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT)
               .onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT)
               .onMalformedInput(CodingErrorAction.REPORT);

        byte[] bytes = new byte[1];
        for (int c = 0xa0; c < 0x100; c++) {
            try {
                bytes[0] = (byte) c;
                char[] chars = decoder.decode(ByteBuffer.wrap(bytes)).array();
                byte[] bytes2 = encoder.encode(cbFunction.apply(new String(chars))).array();
                assertArrayEquals(bytes, bytes2);
            } catch (Exception e) {
                // ignore unmappable characters
            }
        }
    }
}
