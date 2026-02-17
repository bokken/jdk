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

package java.nio;

import java.util.Objects;

final class CharProducerCharBuffer extends StringCharBuffer implements sun.nio.RawCharacterProducer {

    private final sun.nio.RawCharacterProducer producer;

    CharProducerCharBuffer(CharSequence s, int start, int end, sun.nio.RawCharacterProducer producer) {
        super(s, start, end);
        this.producer = producer;
    }

    private CharProducerCharBuffer(CharSequence s,
                             int mark,
                             int pos,
                             int limit,
                             int cap,
                             int offset,
                             sun.nio.RawCharacterProducer producer) {
        super(s, mark, pos, limit, cap, offset);
        this.producer = producer;
    }

    @Override
    public int copyAscii(ByteBuffer target, int srcOffset, int len) {
        int position = offset + position();
        return producer.copyAscii(target, srcOffset + position, Math.min(len, remaining()));
    }

    @Override
    public int copyLatin1(ByteBuffer target, int srcOffset, int len) {
        int position = offset + position();
        return producer.copyLatin1(target, srcOffset + position, Math.min(len, remaining()));
    }

    @Override
    public boolean isLatin1() {
        return producer.isLatin1();
    }

    @Override
    public ByteBuffer getLatin1Bytes(int srcOffset, int len) {
        int position = offset + position();
        return producer.getLatin1Bytes(srcOffset + position, Math.min(len, remaining()));
    }

    @Override
    public ByteBuffer getUTF16Bytes(int offset, int len) {
        int position = offset + position();
        return producer.getUTF16Bytes(srcOffset + position, Math.min(len, remaining()));
    }

//    @Override
//    public int copyUTF8(ByteBuffer target, int srcOffset, int len) {
//        int position = offset + position();
//        return producer.copyUTF8(target, srcOffset + position, Math.min(len, remaining()));
//    }

    @Override
    public CharBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new CharProducerCharBuffer(str,
                                          -1,
                                          0,
                                          rem,
                                          rem,
                                          offset + pos,
                                          producer);
    }

    @Override
    public CharBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, limit());
        return new CharProducerCharBuffer(str,
                                          -1,
                                          0,
                                          length,
                                          length,
                                          offset + index,
                                          producer);
    }

    @Override
    public CharBuffer duplicate() {
        return new CharProducerCharBuffer(str,
                                          markValue(),
                                          position(),
                                          limit(),
                                          capacity(),
                                          offset,
                                          producer);
    }
}
