/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infinitape.etherjar.rlp;

import java.nio.ByteBuffer;

/**
 * @link https://github.com/ethereum/wiki/wiki/RLP#rlp-decoding
 */
public class RlpReader {

    private byte[] input;

    private Current current;
    private int position = 0;
    private int limit;

    public RlpReader(byte[] input) {
        this.input = input;
        this.limit = input.length;
    }

    public RlpReader(byte[] input, int position, int length) {
        this.input = input;
        this.position = position;
        this.limit = position + length;
    }

    private int unsigned(byte b) {
        return ((int)b) & 0xff;
    }

    private Current read() {
        if (notEnough(1)) {
            return new Current(RlpType.NONE, input);
        }
        byte b0 = input[position];
        int i0 = unsigned(b0);
        position++;
        // the data is a string if the range of the first byte(i.e. prefix)
        // is [0x00, 0x7f], and the string is the first byte itself exactly;
        if (i0 <= 0x7f) {
            return new Current(RlpType.BYTES, new byte[] {b0});
        }
        // the data is a string if the range of the first byte is [0x80, 0xb7], and the
        // string whose length is equal to the first byte minus 0x80 follows the first byte;
        if (i0 <= 0xb7) {
            int length = i0 - 0x80;
            byte[] buf = consume(length);
            return new Current(RlpType.BYTES, buf);
        }
        // the data is a string if the range of the first byte is [0xb8, 0xbf], and the length of the
        // string whose length in bytes is equal to the first byte minus 0xb7 follows the first byte, and the
        // string follows the length of the string;
        if (i0 <= 0xbf) {
            int sizeLength = i0 - 0xb7;
            int length = consumeSize(sizeLength);
            byte[] buf = consume(length);
            return new Current(RlpType.BYTES, buf);
        }
        // the data is a list if the range of the first byte is [0xc0, 0xf7], and the concatenation of the RLP encodings
        // of all items of the list which the total payload is equal to the first byte minus 0xc0 follows the first byte;
        if (i0 <= 0xf7) {
            int length = i0 - 0xc0;
            byte[] buf = consume(length);
            return new Current(RlpType.LIST, new RlpReader(buf));
        }
        // the data is a list if the range of the first byte is [0xf8, 0xff], and the total payload of the list whose
        // length is equal to the first byte minus 0xf7 follows the first byte, and the concatenation of the RLP
        // encodings of all items of the list follows the total payload of the list;
        if (i0 <= 0xff) {
            int sizeLength = i0 - 0xf7;
            int length = consumeSize(sizeLength);
            byte[] buf = consume(length);
            return new Current(RlpType.LIST, new RlpReader(buf));
        }
        return new Current(RlpType.INVALID, input);
    }

    private boolean notEnough(int length) {
        return input.length < position + length;
    }

    private byte[] consume(byte[] buf, int bufPosition, int length) {
        if (notEnough(length)) {
            throw new IllegalStateException("Incorrect RLP. Must be: " + (position + length) + " bytes long. Has " + input.length + " bytes");
        }
        if (buf.length < bufPosition + length) {
            throw new IllegalArgumentException("Buffer is too small. Required to real " + length + " bytes (after " + bufPosition + "), has " + buf.length + " bytes buffer");
        }
        System.arraycopy(input, position, buf, bufPosition, length);
        position += length;
        return buf;
    }

    private byte[] consume(int length) {
        return consume(new byte[length], 0, length);
    }

    private int consumeSize(int length) {
        byte[] lengthBuf = consume(new byte[4], 4 - length, length);
        int value = ByteBuffer.wrap(lengthBuf).getInt();
        if (value >= Integer.MAX_VALUE - 8 || value <= 0) {
            throw new IllegalStateException("Input list is too long: " + value);
        }
        return value;
    }

    public boolean isConsumed() {
        return position >= limit;
    }

    private void tryRead() {
        if (current == null && !isConsumed()) {
            this.current = read();
        }
    }

    public boolean hasNext() {
        tryRead();
        return current != null && current.type != RlpType.NONE;
    }

    public byte[] next() {
        tryRead();
        if (current == null) {
            throw new IllegalStateException("RLP stream is fully read");
        }
        byte[] data = current.data;
        current = null;
        return data;
    }

    public RlpType getType() {
        tryRead();
        if (current != null) {
            return current.type;
        }
        return RlpType.NONE;
    }

    public void skip() {
        tryRead();
        current = null;
    }

    public String nextString() {
        return new String(next());
    }

    public long nextNumber() {
        byte[] decoded = next();
        if (decoded.length == 0) {
            return 0;
        }
        if (decoded.length > 8) {
            throw new IllegalArgumentException("Input is too long. Has " + decoded.length + " bytes. Max accepted 8 bytes");
        }
        byte[] buf = new byte[8];
        System.arraycopy(decoded, 0, buf, 8 - decoded.length, decoded.length);
        return ByteBuffer.wrap(buf).getLong();
    }

    public RlpReader nextList() {
        tryRead();
        if (current == null) {
            throw new IllegalStateException("RLP stream is fully read");
        }
        if (current.type != RlpType.LIST) {
            throw new IllegalStateException("Next item is not list: " + current.type);
        }
        RlpReader list = current.list;
        current = null;
        return list;
    }

    private class Current {
        RlpType type;
        byte[] data;
        RlpReader list;

        Current(RlpType type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        Current(RlpType type, RlpReader list) {
            this.type = type;
            this.list = list;
        }
    }
}