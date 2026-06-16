package sim;

import java.util.*;

public class FlagTest {

    private static void printHeader(String name) {
        System.out.println("\n=== " + name + " ===");
    }

    public static void main(String[] args) {
        testTokenizer();
        testParser();
        testAtomEval();
        testNotEval();
        testBinOpEval();
        testPrevState();
        testYamlLoader();
        testEvalFlagNoBranch();
        testEvalFlagWithBranch();
        testEvalFlagBranchInYaml();
        testDuplicateRejected();
        testUnknownRegRejected();
        testBitBounds();
        testComprehensiveMultiFlag();

        System.out.println("\n=== ALL FLAG TESTS PASSED ===");
    }

    static void testTokenizer() {
        printHeader("Tokenizer");

        List<String> toks = FlagDef.tokenize("!A(0)&&prev.A(1)");
        assertEqual(Arrays.asList("!", "A", "(", "0", ")", "&&", "prev", ".", "A", "(", "1", ")"), toks, "basic");

        toks = FlagDef.tokenize("A(0) || B(1)");
        assertEqual(Arrays.asList("A", "(", "0", ")", "||", "B", "(", "1", ")"), toks, "spaces");

        toks = FlagDef.tokenize("A(0)==B(1)");
        assertEqual(Arrays.asList("A", "(", "0", ")", "==", "B", "(", "1", ")"), toks, "eq");

        toks = FlagDef.tokenize("A(0)!=B(1)");
        assertEqual(Arrays.asList("A", "(", "0", ")", "!=", "B", "(", "1", ")"), toks, "neq");

        System.out.println("  PASSED");
    }

    static void testParser() {
        printHeader("Parser");

        FlagDef.CondNode n = FlagDef.CondParser.parse("A(0)");
        assertTrue(n instanceof FlagDef.Atom, "atom");
        FlagDef.Atom a = (FlagDef.Atom) n;
        assertEqual("A", a.reg, "atom reg");
        assertEqual(0, a.bit, "atom bit");
        assertFalse(a.prev, "atom prev");

        n = FlagDef.CondParser.parse("prev.A(1)");
        assertTrue(n instanceof FlagDef.Atom, "prev atom");
        a = (FlagDef.Atom) n;
        assertEqual("A", a.reg, "prev atom reg");
        assertEqual(1, a.bit, "prev atom bit");
        assertTrue(a.prev, "prev atom prev");

        n = FlagDef.CondParser.parse("!A(0)");
        assertTrue(n instanceof FlagDef.Not, "not");
        FlagDef.Not not = (FlagDef.Not) n;
        assertTrue(not.child instanceof FlagDef.Atom, "not child");

        n = FlagDef.CondParser.parse("A(0)&&B(1)");
        assertTrue(n instanceof FlagDef.BinOp, "and");
        FlagDef.BinOp op = (FlagDef.BinOp) n;
        assertEqual("&&", op.op, "and op");

        n = FlagDef.CondParser.parse("!A(0)&&prev.A(1)");
        op = (FlagDef.BinOp) n;
        assertEqual("&&", op.op, "complex op");
        assertTrue(op.left instanceof FlagDef.Not, "complex left Not");
        assertTrue(op.right instanceof FlagDef.Atom, "complex right Atom");

        n = FlagDef.CondParser.parse("A(0)||B(1)&&C(2)");
        op = (FlagDef.BinOp) n;
        assertEqual("||", op.op, "prec op");
        assertTrue(op.right instanceof FlagDef.BinOp, "prec right BinOp(&&)");

        n = FlagDef.CondParser.parse("A(0)==B(1)");
        op = (FlagDef.BinOp) n;
        assertEqual("==", op.op, "eq op");

        n = FlagDef.CondParser.parse("A(0)!=B(1)");
        op = (FlagDef.BinOp) n;
        assertEqual("!=", op.op, "neq op");

        System.out.println("  PASSED");
    }

    static void testAtomEval() {
        printHeader("Atom eval");

        Registers regs = new Registers();
        regs.addReg("A", 2);
        regs.write("A", new Buffer(new byte[]{0x00, 0x05}));
        Registers prev = regs.copy();

        FlagDef.Atom a0 = new FlagDef.Atom("A", 0, false);
        FlagDef.Atom a1 = new FlagDef.Atom("A", 1, false);
        FlagDef.Atom a2 = new FlagDef.Atom("A", 2, false);

        assertTrue(a0.eval(regs, prev), "A(0)=1 (0x0005)");
        assertFalse(a1.eval(regs, prev), "A(1)=0");
        assertTrue(a2.eval(regs, prev), "A(2)=1");

        System.out.println("  PASSED");
    }

    static void testNotEval() {
        printHeader("Not eval");

        Registers regs = new Registers();
        regs.addReg("A", 1);
        Registers prev = regs.copy();

        FlagDef.Atom a = new FlagDef.Atom("A", 0, false);
        FlagDef.Not n = new FlagDef.Not(a);

        assertFalse(a.eval(regs, prev), "A(0)=0");
        assertTrue(n.eval(regs, prev), "!A(0)=1");

        System.out.println("  PASSED");
    }

    static void testBinOpEval() {
        printHeader("BinOp eval");

        Registers regs = new Registers();
        regs.addReg("A", 1);
        regs.addReg("B", 1);
        Registers prev = regs.copy();

        FlagDef.Atom a0 = new FlagDef.Atom("A", 0, false);
        FlagDef.Atom b0 = new FlagDef.Atom("B", 0, false);

        regs.write("A", new Buffer(new byte[]{0x01}));
        regs.write("B", new Buffer(new byte[]{0x01}));
        assertTrue(new FlagDef.BinOp(a0, "&&", b0).eval(regs, prev), "1&&1");

        regs.write("B", new Buffer(new byte[]{0x00}));
        assertFalse(new FlagDef.BinOp(a0, "&&", b0).eval(regs, prev), "1&&0");
        assertTrue(new FlagDef.BinOp(a0, "||", b0).eval(regs, prev), "1||0");

        regs.write("A", new Buffer(new byte[]{0x00}));
        assertFalse(new FlagDef.BinOp(a0, "||", b0).eval(regs, prev), "0||0");

        regs.write("A", new Buffer(new byte[]{0x01}));
        regs.write("B", new Buffer(new byte[]{0x01}));
        assertTrue(new FlagDef.BinOp(a0, "==", b0).eval(regs, prev), "1==1");
        assertFalse(new FlagDef.BinOp(a0, "!=", b0).eval(regs, prev), "1!=1");

        regs.write("B", new Buffer(new byte[]{0x00}));
        assertFalse(new FlagDef.BinOp(a0, "==", b0).eval(regs, prev), "1==0");
        assertTrue(new FlagDef.BinOp(a0, "!=", b0).eval(regs, prev), "1!=0");

        System.out.println("  PASSED");
    }

    static void testPrevState() {
        printHeader("Prev-state");

        Registers cur = new Registers();
        cur.addReg("A", 1);
        cur.write("A", new Buffer(new byte[]{0x01}));
        Registers prev = new Registers();
        prev.addReg("A", 1);
        prev.write("A", new Buffer(new byte[]{0x00}));

        FlagDef.Atom curAtom = new FlagDef.Atom("A", 0, false);
        FlagDef.Atom prvAtom = new FlagDef.Atom("A", 0, true);

        assertTrue(curAtom.eval(cur, prev), "current A(0)=1");
        assertFalse(prvAtom.eval(cur, prev), "prev A(0)=0");

        System.out.println("  PASSED");
    }

    static void testYamlLoader() {
        printHeader("YAML loader");

        Registers regs = new Registers();
        RegisterLoader.loadRegistersFromYaml("src/test/resources/FlagTest/registers.yaml", regs);
        Map<String, FlagDef> flags = FlagLoader.loadFlagsFromYaml("src/test/resources/FlagTest/flags.yaml", regs);

        assertEqual(3, flags.size(), "flag count");

        FlagDef f = flags.get("ZERO");
        assertEqual("FLAGS", f.targetReg, "ZERO target reg");
        assertEqual(0, f.targetBit, "ZERO target bit");

        f = flags.get("CARRY");
        assertEqual("FLAGS", f.targetReg, "CARRY target reg");
        assertEqual(1, f.targetBit, "CARRY target bit");

        f = flags.get("PREVCHECK");
        assertEqual("FLAGS", f.targetReg, "PREVCHECK target reg");
        assertEqual(2, f.targetBit, "PREVCHECK target bit");

        System.out.println("  PASSED");
    }

    static void testEvalFlagNoBranch() {
        printHeader("EvalFlag (no branch)");

        Registers regs = new Registers();
        regs.addReg("A", 1);
        regs.addReg("FLAGS", 1);
        regs.write("A", new Buffer(new byte[]{0x05}));
        regs.write("FLAGS", new Buffer(new byte[]{0x00}));
        Registers prev = regs.copy();

        CPU cpu = makeCpu(regs);

        FlagDef fd = FlagDef.parse("T", "A(0)", "FLAGS(0)");
        cpu.flags.put("T", fd);
        cpu.flags.put("F", FlagDef.parse("F", "A(1)", "FLAGS(1)"));

        new EvalFlag("T", null, null).execute(cpu);
        assertEqual(1, regs.read("FLAGS").getByte(0) & 1, "FLAGS bit0=1 (A(0) is set)");

        new EvalFlag("F", null, null).execute(cpu);
        assertEqual(0, (regs.read("FLAGS").getByte(0) >> 1) & 1, "FLAGS bit1=0 (A(1) not set)");

        System.out.println("  PASSED");
    }

    static void testEvalFlagWithBranch() {
        printHeader("EvalFlag (with branch via CU step)");

        Registers regs = new Registers();
        regs.addReg("IR", 0);
        regs.addReg("A", 1);
        regs.addReg("B", 1);
        regs.addReg("FLAGS", 1);
        regs.write("A", new Buffer(new byte[]{0x05}));
        regs.write("B", new Buffer(new byte[]{0x00}));
        regs.write("FLAGS", new Buffer(new byte[]{0x00}));

        CPU cpu = makeCpu(regs);
        cpu.flags.put("T", FlagDef.parse("T", "A(0)", "FLAGS(0)"));

        List<MicroOp> onTrue = new ArrayList<>();
        onTrue.add(new IncReg("B"));
        List<MicroOp> onFalse = new ArrayList<>();
        onFalse.add(new DecReg("B"));

        // Register an instruction with EvalFlag + branch
        Buffer opcode = new Buffer(new byte[]{0x01});
        List<MicroOp> ops = new ArrayList<>();
        ops.add(new EvalFlag("T", onTrue, onFalse));
        ops.add(new End());
        cpu.cu.register(opcode, ops);

        // Prime IR with the opcode and start CPU
        regs.appendToReg("IR", (byte) 0x01);
        cpu.start();

        // Step 1: EvalFlag executes, writes flag bit, injects onTrue (IncReg B)
        cpu.step();
        assertEqual(1, regs.read("FLAGS").getByte(0) & 1, "FLAGS bit0=1 (true)");
        assertEqual(0, regs.read("B").getByte(0) & 0xFF, "B=0 (inject queued, not yet run)");

        // Step 2: injected IncReg B executes
        cpu.step();
        assertEqual(1, regs.read("B").getByte(0) & 0xFF, "B incremented to 1 (injected onTrue ran)");

        // Step 3: End executes, CPU stops
        cpu.step();
        assertFalse(cpu.isRunning(), "CPU halted after End");

        System.out.println("  PASSED");
    }

    static void testEvalFlagBranchInYaml() {
        printHeader("EvalFlag branch in YAML");

        Registers regs = new Registers();
        RegisterLoader.loadRegistersFromYaml("src/test/resources/FlagTest/registers.yaml", regs);
        Map<String, FlagDef> flags = FlagLoader.loadFlagsFromYaml("src/test/resources/FlagTest/flags.yaml", regs);

        ControlUnit cu = new ControlUnit();
        MicroCodeLoader.load("src/test/resources/FlagTest/microcode.yaml", cu);

        Memory mem = new Memory();
        // A = 0x05 -> byte0=0000_0101 -> A(1)=0, so ZERO flag (cond: A(1)) is false
        // onFalse: DecReg A -> A should change
        mem.write(new Buffer(new byte[]{0x00}), new Buffer(new byte[]{0x01}));
        mem.write(new Buffer(new byte[]{0x01}), new Buffer(new byte[]{(byte) 0xFF}));

        CPU cpu = new CPU(mem, regs, cu, Endianness.LITTLE);
        cpu.flags = flags;

        System.out.print("Before A: ");
        regs.read("A").print();
        System.out.print("Before FLAGS: ");
        regs.read("FLAGS").print();

        cpu.start();
        while (cpu.isRunning()) {
            cu.step(cpu, "IR");
        }

        System.out.print("After A: ");
        regs.read("A").print();
        System.out.print("After FLAGS: ");
        regs.read("FLAGS").print();

        Buffer a = regs.read("A");
        assertEqual(0x04, a.getByte(1) & 0xFF, "A byte1 decremented from 0x05 to 0x04");

        Buffer fl = regs.read("FLAGS");
        int zeroBit = fl.getByte(0) & 1;
        assertEqual(0, zeroBit, "ZERO flag=0 (A(1) was 0)");

        System.out.println("  PASSED");
    }

    static void testDuplicateRejected() {
        printHeader("Duplicate rejection");

        try {
            FlagDef.parse("DUP", "A(0)", "FLAGS(0)");
            FlagDef.parse("DUP", "B(0)", "FLAGS(1)");
        } catch (Exception e) {
            assertTrue(false, "parse should not reject duplicates (loader does)");
        }

        System.out.println("  PASSED");
    }

    static void testUnknownRegRejected() {
        printHeader("Unknown register rejection");

        Registers regs = new Registers();
        regs.addReg("X", 1);

        FlagDef fd = FlagDef.parse("BAD", "Z(0)", "X(0)");
        try {
            fd.validate(regs.getRegisterNames());
            assertTrue(false, "should throw for unknown Z");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Z"), "mentions unknown reg: " + e.getMessage());
        }

        fd = FlagDef.parse("BAD2", "X(0)", "Z(0)");
        try {
            fd.validate(regs.getRegisterNames());
            assertTrue(false, "should throw for unknown target Z");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Z"), "mentions unknown target");
        }

        System.out.println("  PASSED");
    }

    static void testBitBounds() {
        printHeader("Bit bounds");

        Registers regs = new Registers();
        regs.addReg("A", 1);
        regs.addReg("FLAGS", 1);
        regs.write("A", new Buffer(new byte[]{0x01}));
        regs.write("FLAGS", new Buffer(new byte[]{0x00}));

        CPU cpu = makeCpu(regs);
        cpu.flags.put("F", FlagDef.parse("F", "A(0)", "FLAGS(8)"));

        try {
            new EvalFlag("F", null, null).execute(cpu);
            assertTrue(false, "should throw for bit 8 on 1-byte register");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("out of bounds"), "bounds check: " + e.getMessage());
        }

        System.out.println("  PASSED");
    }

    static void testComprehensiveMultiFlag() {
        printHeader("Comprehensive multi-flag CPU program");

        Registers regs = new Registers();
        RegisterLoader.loadRegistersFromYaml("src/test/resources/ComprehensiveFlagTest/registers.yaml", regs);
        Map<String, FlagDef> flags = FlagLoader.loadFlagsFromYaml("src/test/resources/ComprehensiveFlagTest/flags.yaml", regs);

        ControlUnit cu = new ControlUnit();
        MicroCodeLoader.load("src/test/resources/ComprehensiveFlagTest/microcode.yaml", cu);

        Memory mem = new Memory();
        // Program (bit 0 = LSB of big-endian register):
        // A starts at [0x00, 0x07] = value 7 (bits 0,1,2 set)
        //
        // 0x00: BIT0_A  (A(0)=1 → true,  IncReg B)            → FLAGS(0)=1, B=1
        // 0x01: EVEN_A  (A(0)=1 → 1==0 false, DecReg X)       → FLAGS(3)=0, X=-1
        // 0x02: EVEN_A  (same, A unchanged)                    → FLAGS(3)=0, X=-2
        // 0x03: EVEN_A  (same)                                 → FLAGS(3)=0, X=-3
        // 0x04: PREV_BIT0 (prev.A(0)=1, A unchanged → true)   → FLAGS(4)=1, B=2
        // 0x05: B_NOTZERO (B=[0,2]=2, bit1 set → true)        → FLAGS(5)=1, X=-2
        // 0x06: NOT_ZERO  (A has bits, not zero → false)      → FLAGS(9)=0, X=-1 (both branches inc)
        // 0x07: PREV_PREV (prev.A(0)==A(0)=1 → false)         → FLAGS(10)=0, X=-2
        // 0x08: A_EQ_B   (A(0)=1 != B(0)=0 → false)          → FLAGS(8)=0, X=-3
        // 0x09: ALWAYS   (constant 1 → true)                  → FLAGS(7)=1
        // 0x0A: NEVER    (constant 0 → false)                 → FLAGS(6)=0
        // 0x0B: HALT
        mem.write(new Buffer(new byte[]{0x00}), new Buffer(new byte[]{0x10}));
        mem.write(new Buffer(new byte[]{0x01}), new Buffer(new byte[]{0x13}));
        mem.write(new Buffer(new byte[]{0x02}), new Buffer(new byte[]{0x13}));
        mem.write(new Buffer(new byte[]{0x03}), new Buffer(new byte[]{0x13}));
        mem.write(new Buffer(new byte[]{0x04}), new Buffer(new byte[]{0x14}));
        mem.write(new Buffer(new byte[]{0x05}), new Buffer(new byte[]{0x15}));
        mem.write(new Buffer(new byte[]{0x06}), new Buffer(new byte[]{0x16}));
        mem.write(new Buffer(new byte[]{0x07}), new Buffer(new byte[]{0x17}));
        mem.write(new Buffer(new byte[]{0x08}), new Buffer(new byte[]{0x18}));
        mem.write(new Buffer(new byte[]{0x09}), new Buffer(new byte[]{0x19}));
        mem.write(new Buffer(new byte[]{0x0A}), new Buffer(new byte[]{0x1A}));
        mem.write(new Buffer(new byte[]{0x0B}), new Buffer(new byte[]{(byte) 0xFF}));

        CPU cpu = new CPU(mem, regs, cu, Endianness.LITTLE);
        cpu.flags = flags;

        System.out.println("\nInitial state:");
        System.out.print("  A: "); regs.read("A").print();
        System.out.print("  B: "); regs.read("B").print();
        System.out.print("  X: "); regs.read("X").print();
        System.out.print("  FLAGS: "); regs.read("FLAGS").print();

        cpu.start();
        int stepNum = 0;
        while (cpu.isRunning()) {
            cpu.step();
            System.out.println("Step " + stepNum + ":");
            System.out.print("  A: "); regs.read("A").print();
            System.out.print("  B: "); regs.read("B").print();
            System.out.print("  X: "); regs.read("X").print();
            System.out.print("  FLAGS: "); regs.read("FLAGS").print();
            stepNum++;
            if (stepNum > 100) {
                System.out.println("SAFETY BREAK");
                break;
            }
        }

        System.out.println("\nFinal state (after " + stepNum + " steps):");
        System.out.print("  A: "); regs.read("A").print();
        System.out.print("  B: "); regs.read("B").print();
        System.out.print("  X: "); regs.read("X").print();
        System.out.print("  FLAGS: "); regs.read("FLAGS").print();

        System.out.println("\nVerifying flag register bits:");

        Buffer fl = regs.read("FLAGS");
        int flByte0 = fl.getByte(0) & 0xFF;  // bits 8-15 (MSB)
        int flByte1 = fl.getByte(1) & 0xFF;  // bits 0-7 (LSB)

        // BIT0_A: A(0)=1 → true → FLAGS(0)=1
        assertEqual(1, flByte1 & 1, "FLAGS(0)=1 (BIT0_A true)");

        // EVEN_A last eval: A(0)=1 → false → FLAGS(3)=0
        assertEqual(0, (flByte1 >> 3) & 1, "FLAGS(3)=0 (EVEN_A last was false)");

        // PREV_BIT0: prev.A(0)=1 → true → FLAGS(4)=1
        assertEqual(1, (flByte1 >> 4) & 1, "FLAGS(4)=1 (PREV_BIT0 true)");

        // B_NOTZERO: B bit 1 set → true → FLAGS(5)=1
        assertEqual(1, (flByte1 >> 5) & 1, "FLAGS(5)=1 (B_NOTZERO true, B=2)");

        // NEVER: constant 0 → false → FLAGS(6)=0
        assertEqual(0, (flByte1 >> 6) & 1, "FLAGS(6)=0 (NEVER false)");

        // ALWAYS: constant 1 → true → FLAGS(7)=1
        assertEqual(1, (flByte1 >> 7) & 1, "FLAGS(7)=1 (ALWAYS true)");

        // A_EQ_B: A bits != B bits → false → FLAGS(8)=0
        assertEqual(0, flByte0 & 1, "FLAGS(8)=0 (A_EQ_B false)");

        // NOT_ZERO: A has bits set → false → FLAGS(9)=0
        assertEqual(0, (flByte0 >> 1) & 1, "FLAGS(9)=0 (NOT_ZERO false)");

        // PREV_PREV: prev.A(0)==A(0) → false → FLAGS(10)=0
        assertEqual(0, (flByte0 >> 2) & 1, "FLAGS(10)=0 (PREV_PREV false)");

        // Verify register values (A unchanged since no op modifies it)
        Buffer aBuf = regs.read("A");
        assertEqual(0x00, aBuf.getByte(0) & 0xFF, "A byte0 = 0x00");
        assertEqual(0x07, aBuf.getByte(1) & 0xFF, "A byte1 = 0x07");

        // B incremented by BIT0_A (×1) and PREV_BIT0 (×1) = [0, 2]
        Buffer bBuf = regs.read("B");
        assertEqual(0x00, bBuf.getByte(0) & 0xFF, "B byte0 = 0x00");
        assertEqual(0x02, bBuf.getByte(1) & 0xFF, "B byte1 = 0x02");

        System.out.println("  PASSED");
    }

    private static CPU makeCpu(Registers regs) {
        Memory mem = new Memory();
        ControlUnit cu = new ControlUnit();
        Buffer halt = new Buffer(new byte[]{(byte) 0xFF});
        List<MicroOp> haltOps = new ArrayList<>();
        haltOps.add(new End());
        cu.register(halt, haltOps);
        return new CPU(mem, regs, cu, Endianness.LITTLE);
    }

    static void assertEqual(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new RuntimeException("FAIL [" + msg + "]: expected <" + expected + "> but got <" + actual + ">");
        }
        System.out.println("  OK " + msg);
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new RuntimeException("FAIL [" + msg + "]: expected true");
        System.out.println("  OK " + msg);
    }

    static void assertFalse(boolean cond, String msg) {
        if (cond) throw new RuntimeException("FAIL [" + msg + "]: expected false");
        System.out.println("  OK " + msg);
    }
}
