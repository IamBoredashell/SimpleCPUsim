package sim;

import java.util.*;

public class CPUGUITest {

    public static void main(String[] args) {
        System.out.println("==============================");
        System.out.println("TEST: CPU GUI LIVE EXECUTION");
        System.out.println("==============================");

        Registers regs = new Registers();
        RegisterLoader.loadRegistersFromYaml("test/CPUTest/registers.yaml", regs);

        Memory mem = new Memory();
        ControlUnit cu = new ControlUnit();
        MicroCodeLoader.load("test/CPUTest/microcode.yaml", cu);

        mem.write(new Buffer(new byte[]{0x00}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x01}), new Buffer(new byte[]{0x50}));

        mem.write(new Buffer(new byte[]{0x02}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x03}), new Buffer(new byte[]{0x50}));

        mem.write(new Buffer(new byte[]{0x04}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x05}), new Buffer(new byte[]{0x51}));

        mem.write(new Buffer(new byte[]{0x06}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x07}), new Buffer(new byte[]{0x50}));

        mem.write(new Buffer(new byte[]{0x08}), new Buffer(new byte[]{(byte)0xFF}));

        System.out.println("Program loaded into memory.");

        CPU testCpu = new CPU(mem, regs, cu, Endianness.LITTLE);
        
        CpuSimulatorUI.injectedCpu = testCpu;

        System.out.println("Booting up the Visual CPU Simulator for live testing...");
        System.out.println("Use the 'Step' button in the GUI to execute the instructions.");
        
        javafx.application.Application.launch(CpuSimulatorUI.class, args);
    }
}
