package sim;

import java.util.Arrays;

public class Buffer {
    private byte[] data;

    public Buffer(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Buffer size cannot be negative");
        }
        this.data = new byte[size];
    }

    public Buffer(Buffer other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy null Buffer");
        }
        this.data = other.data.clone();
    }

    public Buffer(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        this.data = data.clone();
    }

    public byte[] getData() {
        return this.data.clone();
    }

    public void setData(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (data.length != this.data.length) {
            throw new RuntimeException("Buffer size mismatch");
        }
        this.data = data.clone();
    }

    public void setByte(byte value, int index) {
        if (index < 0 || index >= this.data.length) {
            throw new IndexOutOfBoundsException("Buffer index out of bounds: " + index);
        }
        this.data[index] = value;
    }

    public byte getByte(int index) {
        if (index < 0 || index >= this.data.length) {
            throw new IndexOutOfBoundsException("Buffer index out of bounds: " + index);
        }
        return data[index];
    }

    public void setZero() {
        Arrays.fill(data, (byte) 0);
    }

    public boolean isZero() {
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
    }

    public int getSize() {
        return this.data.length;
    }

    public void print() {
        for (byte b : data) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Buffer)) return false;
        Buffer other = (Buffer) o;
        return Arrays.equals(this.data, other.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public void append(byte b) {
        byte[] newData = new byte[data.length + 1];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = b;
        data = newData;
    }

    public void clear() {
        data = new byte[0];
    }

    public Buffer slice(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("Start index cannot be negative");
        }
        if (start >= data.length) return new Buffer(0);

        byte[] newData = new byte[data.length - start];
        System.arraycopy(data, start, newData, 0, newData.length);
        return new Buffer(newData);
    }

	public String toHexString() {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : data) sb.append(String.format("%02X", b));
	    return sb.toString() + "H";
	}

	public String toBinString() {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : data) sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
	    return sb.toString() + "B";
	}
}
