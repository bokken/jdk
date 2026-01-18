package sun.nio;

import java.nio.ByteBuffer;

public interface RawCharacterProducer {
//    interface RawCharacterConsumer<R> {
//        /**
//         * Provides the raw binary representation of characters for consumption.
//         * @param bytes The raw bytes.
//         * @param offset The offset (of character count) into the <i>bytes</i> to start.
//         * @param len The number of characters (not bytes) to read.
//         */
//        R consume(byte[] bytes, int offset, int len);
//    }
//
//    <R extends Object> R consume(RawCharacterConsumer<? extends R> consumer);
//
//    /**
//     * @return {@code true} if the {@code byte[]} produced contains characters in <i>latin 1</i> or {@code false} if <i>utf-16</i>.
//     */
//    boolean isLatin1();

    int copyAscii(ByteBuffer target, int srcOffset, int len);
    int copyLatin1(ByteBuffer target, int srcOffset, int len);

    ByteBuffer latin1Bytes();
}
