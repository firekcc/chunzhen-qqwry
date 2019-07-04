package net.cz88.ip.qqwry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class QQWry {
    private static final int INDEX_RECORD_LENGTH = 7;
    private static final byte REDIRECT_MODE_1 = 1;
    private static final byte REDIRECT_MODE_2 = 2;
    private static final byte STRING_END = 0;
    private static final String DEF_PATH = "qqwry.dat";
    private final byte[] data;
    private final long indexHead;
    private final long indexTail;

    public QQWry() throws IOException {
        File file = new File(DEF_PATH);
        InputStream in = new FileInputStream(DEF_PATH);
        ByteArrayOutputStream out = new ByteArrayOutputStream(10485760);
        byte[] buffer = new byte[4096];

        while (true) {
            int r = in.read(buffer);
            if (r == -1) {
                this.data = out.toByteArray();
                this.indexHead = this.readLong32(0);
                this.indexTail = this.readLong32(4);
                return;
            }

            out.write(buffer, 0, r);
        }
    }

    private QQWry(byte[] data) {
        this.data = data;
        this.indexHead = this.readLong32(0);
        this.indexTail = this.readLong32(4);
    }

    public QQWry(Path file) throws IOException {
        this(Files.readAllBytes(file));
    }

    public IPZone findIP(String ip) {
        long ipNum = this.toNumericIP(ip);
        QQWry.QIndex idx = this.searchIndex(ipNum);
        return idx == null ? new IPZone(ip) : this.readIP(ip, idx);
    }

    private long getMiddleOffset(long begin, long end) {
        long records = (end - begin) / 7L;
        records >>= 1;
        if (records == 0L) {
            records = 1L;
        }

        return begin + records * 7L;
    }

    private QQWry.QIndex readIndex(int offset) {
        long min = this.readLong32(offset);
        int record = this.readInt24(offset + 4);
        long max = this.readLong32(record);
        return new QQWry.QIndex(min, max, record);
    }

    private int readInt24(int offset) {
        int v = this.data[offset] & 255;
        v |= this.data[offset + 1] << 8 & '\uff00';
        v |= this.data[offset + 2] << 16 & 16711680;
        return v;
    }

    private IPZone readIP(String ip, QQWry.QIndex idx) {
        int pos = idx.recordOffset + 4;
        byte mode = this.data[pos];
        IPZone z = new IPZone(ip);
        if (mode == 1) {
            int offset = this.readInt24(pos + 1);
            if (this.data[offset] == 2) {
                this.readMode2(z, offset);
            } else {
                QQWry.QString mainInfo = this.readString(offset);
                String subInfo = this.readSubInfo(offset + mainInfo.length);
                z.setMainInfo(mainInfo.string);
                z.setSubInfo(subInfo);
            }
        } else if (mode == 2) {
            this.readMode2(z, pos);
        } else {
            QQWry.QString mainInfo = this.readString(pos);
            String subInfo = this.readSubInfo(pos + mainInfo.length);
            z.setMainInfo(mainInfo.string);
            z.setSubInfo(subInfo);
        }

        return z;
    }

    private long readLong32(int offset) {
        long v = (long) this.data[offset] & 255L;
        v |= (long) (this.data[offset + 1] << 8) & 65280L;
        v |= (long) (this.data[offset + 2] << 16) & 16711680L;
        v |= (long) (this.data[offset + 3] << 24) & 4278190080L;
        return v;
    }

    private void readMode2(IPZone z, int offset) {
        int mainInfoOffset = this.readInt24(offset + 1);
        String main = this.readString(mainInfoOffset).string;
        String sub = this.readSubInfo(offset + 4);
        z.setMainInfo(main);
        z.setSubInfo(sub);
    }

    private QQWry.QString readString(int offset) {
        int i = 0;
        byte[] buf = new byte[128];

        while (true) {
            byte b = this.data[offset + i];
            if (0 == b) {
                try {
                    return new QQWry.QString(new String(buf, 0, i, "GB18030"), i + 1);
                } catch (UnsupportedEncodingException var5) {
                    return new QQWry.QString("", 0);
                }
            }

            buf[i] = b;
            ++i;
        }
    }

    private String readSubInfo(int offset) {
        byte b = this.data[offset];
        if (b != 1 && b != 2) {
            return this.readString(offset).string;
        } else {
            int areaOffset = this.readInt24(offset + 1);
            return areaOffset == 0 ? "" : this.readString(areaOffset).string;
        }
    }

    private QQWry.QIndex searchIndex(long ip) {
        long head = this.indexHead;
        long tail = this.indexTail;

        while (tail > head) {
            long cur = this.getMiddleOffset(head, tail);
            QQWry.QIndex idx = this.readIndex((int) cur);
            if (ip >= idx.minIP && ip <= idx.maxIP) {
                return idx;
            }

            if (cur == head || cur == tail) {
                return idx;
            }

            if (ip < idx.minIP) {
                tail = cur;
            } else {
                if (ip <= idx.maxIP) {
                    return idx;
                }

                head = cur;
            }
        }

        return null;
    }

    private long toNumericIP(String s) {
        String[] parts = s.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("ip=" + s);
        } else {
            long n = Long.parseLong(parts[0]) << 24;
            n += Long.parseLong(parts[1]) << 16;
            n += Long.parseLong(parts[2]) << 8;
            n += Long.parseLong(parts[3]);
            return n;
        }
    }

    private static class QString {
        public final String string;
        public final int length;

        public QString(String string, int length) {
            this.string = string;
            this.length = length;
        }
    }

    private static class QIndex {
        public final long minIP;
        public final long maxIP;
        public final int recordOffset;

        public QIndex(long minIP, long maxIP, int recordOffset) {
            this.minIP = minIP;
            this.maxIP = maxIP;
            this.recordOffset = recordOffset;
        }
    }
}

