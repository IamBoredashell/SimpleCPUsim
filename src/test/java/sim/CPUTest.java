package sim;

import java.util.*;

public class CPUTest {

    private static void printHeader(String name) {
        System.out.println("\n==============================");
        System.out.println("TEST: " + name);
        System.out.println("==============================");
    }

    private static void printState(Registers regs) {
        System.out.print("IR: ");
        regs.read("IR").print();

        System.out.print("PC: ");
        regs.read("PC").print();

        regs.printRegisters();
        System.out.println("------------------------------");
    }

    public static void main(String[] args) {

        printHeader("CPU YAML FULL SYSTEM TEST");

        Registers regs = new Registers();
        RegisterLoader.loadRegistersFromYaml("src/test/resources/CPUTest/registers.yaml", regs);

        Memory mem = new Memory();
        ControlUnit cu = new ControlUnit();
        MicroCodeLoader.load("src/test/resources/CPUTest/microcode.yaml", cu);

        System.out.println("\nLoaded Registers:");
        regs.printRegisters();

        /*
           Program:
           00: 80 50  -> INC A
           02: 80 50  -> INC A
           04: 80 51  -> DEC A
           06: 80 50  -> INC A
           08: FF     -> HALT
        */

        mem.write(new Buffer(new byte[]{0x00}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x01}), new Buffer(new byte[]{0x50}));

        mem.write(new Buffer(new byte[]{0x02}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x03}), new Buffer(new byte[]{0x50}));

        mem.write(new Buffer(new byte[]{0x04}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x05}), new Buffer(new byte[]{0x51}));

        mem.write(new Buffer(new byte[]{0x06}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x07}), new Buffer(new byte[]{0x50}));

        mem.write(new Buffer(new byte[]{0x08}), new Buffer(new byte[]{(byte)0xFF}));

        System.out.println("\nProgram loaded into memory.");

        CPU cpu = new CPU(mem, regs, cu, Endianness.LITTLE);

        cpu.start();

        int step = 0;

        while (cpu.isRunning()) {
            System.out.println("\nStep #" + step);

            cu.step(cpu, "IR");

            printState(regs);

            step++;

            if (step > 100) {
                System.out.println("Safety break (possible infinite loop)");
                break;
            }
        }

        printHeader("FINAL STATE");

        regs.printRegisters();

        System.out.println("\nExpected behavior:");
        System.out.println("Initial A from YAML (e.g. 0001)");
        System.out.println("INC -> +1");
        System.out.println("INC -> +1");
        System.out.println("DEC -> -1");
        System.out.println("INC -> +1");
        System.out.println("Final A should reflect 3");

        System.out.println("==============================");
    }
}
