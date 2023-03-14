from enum import Enum

# opcode codesize disasm tojvm
from pwn import *
from opinfo import opinfo, OpType

from op_asm_tab import table as asmtable


class Opcode:
    def __init__(self):
        pass
    def parse(self, buf):
        val = u8(buf.get(1))
        self.opcode = val
        if val not in opinfo:
            raise Exception("unknown opcode %#x" % val)
        d = opinfo[val]
        if d[0] == None:
            self.name = "unknown(%#x)" % val
        else:
            self.name = d[0]
        self.type = d[1]
        self.origin = d[2]
        if self.type == OpType.I:
            return
        if self.type == OpType.IA:
            self.regA = u32(buf.get(4))
            return
        if self.type == OpType.IQ:
            self.regA = u64(buf.get(8))
            return
        if self.type == OpType.IAA:
            self.regA = u32(buf.get(4))
            self.regB = u32(buf.get(4))
            return
        if self.type == OpType.IAAX:
            self.regA = u32(buf.get(4)) # default pc
            self.regB = u32(buf.get(4)) # table size
            self.table = []
            for i in range(self.regB):
                self.table.append([u32(buf.get(4)),u32(buf.get(4))])
            return

        raise Exception("unknown type")

    def to_asm(self, dex):
        if self.opcode not in asmtable:
            return "unknown_asm # %#x " % self.opcode + str(self)
        return asmtable[self.opcode](self, dex)

    def __str__(self):
        if self.type == OpType.I:
            return "%s" % (self.name)
        if self.type == OpType.IA:
            return "%s %#x" % (self.name, self.regA)
        if self.type == OpType.IQ:
            return "%s %#x" % (self.name, self.regA)
        if self.type == OpType.IAA:
            return "%s %#x %#x" % (self.name, self.regA, self.regB)
        if self.type == OpType.IAAX:
            return "lookupswaitch ..."
