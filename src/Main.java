public static void main(String[] args){
    
    //Buffer test 
    Buffer buf = new Buffer(4);
    byte[] b = new byte[4];
    b = new byte[] {(byte) 0x01,(byte) 0x01,(byte) 0x01,(byte) 0x01};
    buf.setData(b);
    System.out.println("Buffer test\n");
    System.out.println("Normal read:\n");
    buf.print();
    System.out.println("Replace with 0:\n");
    System.out.println("Before "+ buf.isZero());
    buf.setZero();
    buf.print();
    System.out.println("After "+ buf.isZero());

    System.out.println("GetData:");
    for (byte value : buf.getData()) {
        System.out.printf("%02X ", value);
    }
    

    //Memory test
    System.out.println("\nMemory test\n");
    Memory mem = new Memory(4);

    System.out.println("Write to memory with address\n");
    b = new byte[] {(byte) 0xF1,(byte) 0x31,(byte) 0xA1,(byte) 0xC1};
    buf.setData(b);
    mem.write(0xFFFFL,buf);
    b = new byte[] {(byte) 0x01,(byte) 0x01,(byte) 0x01,(byte) 0x01};
    buf.setData(b);
    mem.write(0xEEEEL,buf);
    System.out.println("Print Memory\n");
    mem.printMemory();




    
}
