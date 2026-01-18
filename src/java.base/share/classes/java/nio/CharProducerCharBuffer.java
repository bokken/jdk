package java.nio;

final class CharProducerCharBuffer extends StringCharBuffer implements sun.nio.RawCharacterProducer {

    private final sun.nio.RawCharacterProducer producer;

    CharProducerCharBuffer(CharSequence s, int start, int end, sun.nio.RawCharacterProducer producer) {
        super(s, start, end);
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
    public ByteBuffer latin1Bytes() {
        ByteBuffer latin1Bytes = producer.latin1Bytes();
        if (latin1Bytes != null) {
            int position = offset + position() + latin1Bytes.position();
            latin1Bytes.position(position);
            latin1Bytes.limit(position + remaining());
            return latin1Bytes;
        }
        return null;
    }
    //TODO override slice and duplicate
}
