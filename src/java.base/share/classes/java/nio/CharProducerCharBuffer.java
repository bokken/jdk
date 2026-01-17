package java.nio;

final class CharProducerCharBuffer extends StringCharBuffer implements sun.nio.RawCharacterProducer {

    private final sun.nio.RawCharacterProducer producer;

    CharProducerCharBuffer(CharSequence s, int start, int end, sun.nio.RawCharacterProducer producer) {
        super(s, start, end);
        this.producer = producer;
    }

    @Override
    public int copyAscii(ByteBuffer target, int srcOffset, int len) {
        int position = offset() + position();
        return producer.copyAscii(target, srcOffset + position, Math.min(len, remaining()));
    }

    @Override
    public int copyLatin1(ByteBuffer target, int srcOffset) {
        int position = offset() + position();
        return producer.copyLatin1(target, srcOffset + position, Math.min(len, remaining()));
    }
    //TODO override slice and duplicate
}
