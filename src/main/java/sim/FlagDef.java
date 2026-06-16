package sim;

import java.util.*;

public class FlagDef {
    public final String name;
    public final String targetReg;
    public final int targetBit;
    public final CondNode condition;

    public FlagDef(String name, String targetReg, int targetBit, CondNode condition) {
        if (name == null || targetReg == null || condition == null) {
            throw new IllegalArgumentException("FlagDef components cannot be null");
        }
        this.name = name;
        this.targetReg = targetReg;
        this.targetBit = targetBit;
        this.condition = condition;
    }

    public static FlagDef parse(String name, String conditionStr, String target) {
        int paren = target.indexOf('(');
        if (paren < 0 || !target.endsWith(")")) {
            throw new RuntimeException("Invalid target format: " + target + " (expected REG(bit))");
        }
        String reg = target.substring(0, paren);
        int bit = Integer.parseInt(target.substring(paren + 1, target.length() - 1));
        CondNode cond = CondParser.parse(conditionStr);
        return new FlagDef(name, reg, bit, cond);
    }

    public void validate(Set<String> regNames) {
        if (!regNames.contains(targetReg)) {
            throw new RuntimeException("Flag '" + name + "' references unknown target register: " + targetReg);
        }
        condition.validate(regNames);
    }

    public abstract static class CondNode {
        public abstract boolean eval(Registers regs, Registers prevRegs);
        public abstract void validate(Set<String> regNames);
    }

    public static class Literal extends CondNode {
        public final boolean val;

        public Literal(boolean val) {
            this.val = val;
        }

        public boolean eval(Registers regs, Registers prevRegs) {
            return val;
        }

        public void validate(Set<String> regNames) {
        }
    }

    public static class Atom extends CondNode {
        public final String reg;
        public final int bit;
        public final boolean prev;

        public Atom(String reg, int bit, boolean prev) {
            this.reg = reg;
            this.bit = bit;
            this.prev = prev;
        }

        public boolean eval(Registers regs, Registers prevRegs) {
            Registers r = prev ? prevRegs : regs;
            Buffer buf = r.read(reg);
            int byteIdx = (buf.getSize() - 1) - (bit / 8);
            int bitIdx = bit % 8;
            return ((buf.getByte(byteIdx) >> bitIdx) & 1) == 1;
        }

        public void validate(Set<String> regNames) {
            if (!regNames.contains(reg)) {
                throw new RuntimeException("Flag references unknown register: " + reg);
            }
        }
    }

    public static class Not extends CondNode {
        public final CondNode child;

        public Not(CondNode child) {
            this.child = child;
        }

        public boolean eval(Registers regs, Registers prevRegs) {
            return !child.eval(regs, prevRegs);
        }

        public void validate(Set<String> regNames) {
            child.validate(regNames);
        }
    }

    public static class BinOp extends CondNode {
        public final CondNode left;
        public final String op;
        public final CondNode right;

        public BinOp(CondNode left, String op, CondNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public boolean eval(Registers regs, Registers prevRegs) {
            boolean r = right.eval(regs, prevRegs);
            boolean l = left.eval(regs, prevRegs);
            switch (op) {
                case "&&": return l && r;
                case "||": return l || r;
                case "==": return l == r;
                case "!=": return l != r;
                default: throw new RuntimeException("Unknown operator: " + op);
            }
        }

        public void validate(Set<String> regNames) {
            left.validate(regNames);
            right.validate(regNames);
        }
    }

    static class CondParser {
        private final List<String> tokens;
        private int pos;

        static CondNode parse(String s) {
            List<String> toks = tokenize(s);
            if (toks.isEmpty()) throw new RuntimeException("Empty condition");
            CondParser p = new CondParser(toks);
            CondNode n = p.parseExpr();
            if (p.pos != p.tokens.size()) {
                throw new RuntimeException("Unexpected tokens after expression: " + toks.subList(p.pos, toks.size()));
            }
            return n;
        }

        CondParser(List<String> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        String peek() {
            return pos < tokens.size() ? tokens.get(pos) : null;
        }

        String consume() {
            return tokens.get(pos++);
        }

        void expect(String expected) {
            String got = consume();
            if (!got.equals(expected)) {
                throw new RuntimeException("Expected '" + expected + "' but got '" + got + "'");
            }
        }

        CondNode parseExpr() {
            return parseOr();
        }

        CondNode parseOr() {
            CondNode l = parseAnd();
            while ("||".equals(peek())) {
                String op = consume();
                CondNode r = parseAnd();
                l = new BinOp(l, op, r);
            }
            return l;
        }

        CondNode parseAnd() {
            CondNode l = parseEq();
            while ("&&".equals(peek())) {
                String op = consume();
                CondNode r = parseEq();
                l = new BinOp(l, op, r);
            }
            return l;
        }

        CondNode parseEq() {
            CondNode l = parseNot();
            String op = peek();
            while ("==".equals(op) || "!=".equals(op)) {
                consume();
                CondNode r = parseNot();
                l = new BinOp(l, op, r);
                op = peek();
            }
            return l;
        }

        CondNode parseNot() {
            if ("!".equals(peek())) {
                consume();
                return new Not(parseNot());
            }
            return parsePrimary();
        }

        CondNode parsePrimary() {
            String t = peek();
            if (t == null) throw new RuntimeException("Unexpected end of expression");

            if ("prev".equals(t)) {
                consume();
                expect(".");
                String reg = consume();
                expect("(");
                String bitStr = consume();
                expect(")");
                return new Atom(reg, Integer.parseInt(bitStr), true);
            }

            if (t.matches("\\d+")) {
                consume();
                return new Literal(!t.equals("0"));
            }

            String reg = consume();
            expect("(");
            String bitStr = consume();
            expect(")");
            return new Atom(reg, Integer.parseInt(bitStr), false);
        }
    }

    static List<String> tokenize(String s) {
        List<String> toks = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }

            if (c == '!' && i + 1 < s.length() && s.charAt(i + 1) == '=') {
                toks.add("!=");
                i += 2;
                continue;
            }

            if (c == '!' || c == '(' || c == ')' || c == '.') {
                toks.add(String.valueOf(c));
                i++;
                continue;
            }

            if (c == '&' && i + 1 < s.length() && s.charAt(i + 1) == '&') {
                toks.add("&&");
                i += 2;
                continue;
            }

            if (c == '|' && i + 1 < s.length() && s.charAt(i + 1) == '|') {
                toks.add("||");
                i += 2;
                continue;
            }

            if (c == '=' && i + 1 < s.length() && s.charAt(i + 1) == '=') {
                toks.add("==");
                i += 2;
                continue;
            }

            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < s.length() && Character.isDigit(s.charAt(i))) {
                    sb.append(s.charAt(i++));
                }
                toks.add(sb.toString());
                continue;
            }

            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) {
                    sb.append(s.charAt(i++));
                }
                toks.add(sb.toString());
                continue;
            }

            throw new RuntimeException("Unexpected character '" + c + "' in condition");
        }
        return toks;
    }
}
