package sim;

import java.util.*;

public class ControlUnitTest {

    public static void main(String[] args) {

        System.out.println("=== ControlUnitTest ===");

        Registers regs = new Registers();

        regs.addReg("PC", 1);
        regs.addReg("IR", 1);
        regs.addReg("A", 2);

        regs.write("PC", new Buffer(new byte[]{0x00}));
        regs.write("IR", new Buffer(new byte[]{(byte)0xAA}));
        regs.write("A", new Buffer(new byte[]{0x00, 0x01}));

        Memory mem = new Memory();

        mem.write(new Buffer(new byte[]{0x00}),
                  new Buffer(new byte[]{(byte)0xAA}));
        
        mem.write(new Buffer(new byte[]{0x01}),
                  new Buffer(new byte[]{(byte)0xAA}));

        ControlUnit cu = new ControlUnit();

        cu.register(new Buffer(0), List.of(
                new FetchNext("PC", "IR")
        ));

        cu.register(new Buffer(new byte[]{(byte)0xAA}), List.of(
                new ClearIR("IR")
        ));

        CPU cpu = new CPU(mem, regs, cu, Endianness.LITTLE);

        cpu.start();

        for (int i = 0; i < 5; i++) {

            System.out.println("--- step ---");

            cu.step(cpu, "IR");

            regs.printRegisters();
        }

        System.out.println("==================");
    }
}
