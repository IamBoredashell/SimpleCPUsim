import java.math.BigInteger;

class CPU {
    BigInteger ic;
    Memory memory;
    Registers reg;

    public CPU(Memory memory, Registers reg){
    this.memory = memory;
        this.reg = reg;
        ic=BigInteger.ZERO;
    }
    
}
