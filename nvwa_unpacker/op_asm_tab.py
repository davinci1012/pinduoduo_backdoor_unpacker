import struct
def iload(ins,dex):
    return "iload %d"%ins.regA
def lload(ins,dex):
    return "lload %d"%ins.regA
def fload(ins,dex):
    return "fload %d"%ins.regA
def dload(ins,dex):
    return "dload %d"%ins.regA

def aload(ins, dex):
    return "aload %d" % ins.regA

def istore(ins,dex):
    return "istore %d"%ins.regA

def lstore(ins,dex):
    return "lstore %d"%ins.regA

def fstore(ins,dex):
    return "fstore %d"%ins.regA

def dstore(ins,dex):
    return "dstore %d"%ins.regA

def astore(ins,dex):
    return "astore %d"%ins.regA

def getfield(ins, dex):
    field = dex.field_pool[ins.regA]
    clz = field.clz
    return "getfield Field {clz} {field} {type}".format(clz=clz.name, field=field.name, type=field.type)

def putfield(ins, dex):
    field = dex.field_pool[ins.regA]
    clz = field.clz
    return "putfield Field {clz} {field} {type}".format(clz=clz.name, field=field.name, type=field.type)

def invokevirtual(ins, dex):
    m = dex.method_pool[ins.regA]
    return "invokevirtual Method " + m.clz.name + " " + m.name + " " + m.signature

def invokestatic(ins, dex):
    m = dex.method_pool[ins.regA]
    return "invokestatic Method " + m.clz.name + " " + m.name + " " + m.signature

def branch(s):
    def f(ins,dex):
        return "%s L_%d"%(s,ins.regA)
    return f

def return_f(ins, dex):
    return "return"

def ldc_string(ins,dex):
    s_idx = dex.constant_pool[ins.regA][1]
    s = dex.string_pool[s_idx]
    s = s.replace("\\","\\\\")
    s = s.replace("\n","\\n")
    s = s.replace("\"","\\\"")
    return "ldc \"%s\""%s

def ldc_int(ins,dex):
    if ins.regA > 0x7fffffff:
        ins.regA -= 0x100000000
    return "ldc %d"%ins.regA

def ldc_class(ins,dex):
    clz = dex.class_pool[ins.regA]
    return "ldc Class %s"%clz.name

def getstatic(ins,dex):
    field = dex.field_pool[ins.regA]
    clz = field.clz
    return "getstatic Field {clz} {field} {type}".format(clz=clz.name, field=field.name, type=field.type)

def putstatic(ins,dex):
    field = dex.field_pool[ins.regA]
    clz = field.clz
    return "putstatic Field {clz} {field} {type}".format(clz=clz.name, field=field.name, type=field.type)

def ldc_qword(ins,dex):
    if ins.regA > 0x7fffffffffffffff:
        ins.regA -= (1 << 64)
    return "ldc2_w %dL"%ins.regA

def ldc_float(ins,dex):
    h = "%08x"%ins.regA
    f = struct.unpack('!f', bytes.fromhex(h))[0]
    return "ldc %f"%f
def ldc_double(ins,dex):
    h = "%016x"%ins.regA
    f = struct.unpack('!d', bytes.fromhex(h))[0]
    return "ldc %f"%f

def sipush(ins,dex):
    if ins.regA > 0x7fffffff:
        ins.regA -= 0x100000000
    #if (ins.regA & 0xffff0000) != 0:
    return "ldc %d"%ins.regA
        #raise Exception("long value %#x"%ins.regA)
    #return "sipush %#x"%ins.regA

def new(ins,dex):
    clz = dex.class_pool[ins.regA]
    return "new %s"%clz.name

def newarray(ins,dex):
    typs = {
        4:"boolean",
        5:"char",
        6:"float",
        7:"double",
        8:"byte",
        9:"short",
        10:"int",
        11:"long",
    }
    return "newarray %s"%typs[ins.regA]


def anewarray(ins,dex):
    clz = dex.class_pool[ins.regA]
    return "anewarray %s"%clz.name

def dup(ins,dex):
    return "dup"
def areturn(ins,dex):
    return "areturn"
def dreturn(ins,dex):
    return "dreturn"
def freturn(ins,dex):
    return "freturn"
def lreturn(ins,dex):
    return "lreturn"
def ireturn(ins,dex):
    return "ireturn"

def copyname(s):
    def f(a,b):
        return s
    return f

def tableswitch(ins,dex):
    raw =  "tableswitch 0\n"
    for i in ins.table:
        raw += "    L_%d\n"%(i[1])
    raw += "        default : L_%d\n"%ins.regA
    return raw

def lookupswitch(ins,dex):
    raw =  "lookupswitch\n"
    for i in ins.table:
        if i[0] > 0x7fffffff:
            i[0] -= 0x100000000
        raw += "        %d : L_%d\n"%(i[0],i[1])
    raw += "        default : L_%d\n"%ins.regA
    return raw

def instanceof(ins,dex):
    clz = dex.class_pool[ins.regA]
    return "instanceof %s"%(clz.name)

def checkcast(ins,dex):
    clz = dex.class_pool[ins.regA]
    return "checkcast %s"%(clz.name)

def multianewarray(ins,dex):
    clz = dex.class_pool[ins.regA]
    return "multianewarray %s %d"%(clz.name,ins.regB)
def iinc(ins,dex):
    b = ins.regB & 0xff
    if b > 0x7f:
        b -= 0x100
    return "iinc %d %d"%(ins.regA,b)

table = {
    0:copyname("nop"),
    1:copyname("aconst_null"),
    0x2: sipush,
    0x3: ldc_qword,
    0x4: ldc_float,
    0x5: ldc_double,
    0x6: ldc_int,
    0x7: ldc_int,
    0x8: ldc_string,
    0x9: ldc_class,
    0xa: iload,
    0xb: lload,
    0xc: fload,
    0xd: dload,
    0xe: aload,
    0xf: copyname("iaload"),
    0x10: copyname("laload"),
    0x11: copyname("faload"),
    0x12: copyname("daload"),
    0x13: copyname("aaload"),
    0x14: copyname("baload"),
    0x15: copyname("caload"),
    0x16: copyname("saload"),
    0x17: istore,
    0x18: lstore,
    0x19: fstore,
    0x1a: dstore,
    0x1b: astore,
    0x1c: copyname("iastore"),
    0x1d: copyname("lastore"),
    0x1e: copyname("fastore"),
    0x1f: copyname("dastore"),
    0x20: copyname("aastore"),
    0x21: copyname("bastore"),
    0x22: copyname("castore"),
    0x23: copyname("sastore"),
    0x24: copyname("pop"),
    0x25: copyname("pop2"),
    0x26: copyname("dup"),
    0x27: copyname("dup_x1"),
    0x28: copyname("dup_x2"),
    0x29: copyname("dup2"),
    0x2a: copyname("dup2_x1"),
    0x2b: copyname("dup2_x2"),
    0x2c: copyname("swap"),
    0x2d: copyname("iadd"),
    0x2e: copyname("ladd"),
    0x2f: copyname("fadd"),
    0x30: copyname("dadd"),
    0x31: copyname("isub"),
    0x32: copyname("lsub"),
    0x33: copyname("fsub"),
    0x34: copyname("dsub"),
    0x35: copyname("imul"),
    0x36: copyname("lmul"),
    0x37: copyname("fmul"),
    0x38: copyname("dmul"),
    0x39: copyname("idiv"),
    0x3a: copyname("ldiv"),
    0x3b: copyname("fdiv"),
    0x3c: copyname("ddiv"),
    0x3d: copyname("irem"),
    0x3e: copyname("lrem"),
    0x3f: copyname("frem"),
    0x40: copyname("drem"),
    0x41: copyname("ineg"),
    0x42: copyname("lneg"),
   0x43: copyname("fneg"),
    0x44: copyname("dneg"),
    0x45: copyname("ishl"),
    0x46: copyname("lshl"),
    0x47: copyname("ishr"),
    0x48: copyname("lshr"),
    0x49: copyname("iushr"),
    0x4a: copyname("lushr"),
    0x4b: copyname("iand"),
    0x4c: copyname("land"),
    0x4d: copyname("ior"),
    0x4e: copyname("lor"),
    0x4f: copyname("ixor"),
    0x50: copyname("lxor"),

    0x51: iinc,

    0x52: copyname("i2l"),
    0x53: copyname("i2f"),
    0x54: copyname("i2d"),
    0x55: copyname("l2i"),
    0x56: copyname("l2f"),
    0x57: copyname("i2d"),
    0x58: copyname("f2i"),
    0x59: copyname("f2l"),
    0x5a: copyname("f2d"),
    0x5b: copyname("d2i"),
    0x5c: copyname("d2l"),
    0x5d: copyname("d2f"),
    0x5e: copyname("i2b"),
    0x5f: copyname("i2c"),
    0x60: copyname("i2s"),

    0x61: copyname("lcmp"),
    0x62: copyname("fcmpl"),
    0x63: copyname("fcmpg"),
    0x64: copyname("dcmpl"),
    0x65: copyname("dcmpg"),

    0x66: branch("ifeq"),
    0x67: branch("ifne"),
    0x68: branch("iflt"),
    0x69: branch("ifge"),
    0x6a: branch("ifgt"),
    0x6b: branch("ifle"),
    0x6c: branch("if_icmpeq"),
    0x6d: branch("if_icmpne"),
    0x6e: branch("if_icmplt"),
    0x6f: branch("if_icmpge"),
    0x70: branch("if_icmpgt"),
    0x71: branch("if_icmple"),
    0x72: branch("if_acmpeq"),
    0x73: branch("if_acmpne"),
    0x74: branch("goto"),
    0x75: tableswitch,
    0x76: lookupswitch,
    0x77: copyname("ireturn"),
    0x78: copyname("lreturn"),
    0x79: copyname("freturn"),
    0x7a: copyname("dreturn"),
    0x7b: copyname("areturn"),
    0x7c: copyname("return"),

    0x7d: getstatic,
    0x7e: getstatic,
    0x7f: getstatic,
    0x80: getstatic,
    0x81: getstatic,
    0x82: getstatic,
    0x83: getstatic,
    0x84: getstatic,
    0x85: getstatic,

    0x86: putstatic,
    0x87: putstatic,
    0x88: putstatic,
    0x89: putstatic,
    0x8a: putstatic,
    0x8b: putstatic,
    0x8c: putstatic,
    0x8d: putstatic,
    0x8e: putstatic,

    0x8f: getfield,
    0x90: getfield,
    0x91: getfield,
    0x92: getfield,
    0x93: getfield,
    0x94: getfield,
    0x95: getfield,
    0x96: getfield,

    0x97: putfield,
    0x98: putfield,
    0x99: putfield,
    0x9a: putfield,
    0x9b: putfield,
    0x9c: putfield,
    0x9d: putfield,
    0x9e: putfield,
    0x9f: putfield,
    0xa0: putfield,



    0xa1: invokevirtual,
    0xa2: invokevirtual,
    0xa3: invokevirtual,
    0xa4: invokevirtual,
    0xa5: invokevirtual,
    0xa6: invokevirtual,
    0xa7: invokevirtual,
    0xa8: invokevirtual,
    0xa9: invokevirtual,
    0xaa: invokevirtual,
    0xab: invokevirtual,
    0xac: invokevirtual,
    0xad: invokevirtual,
    0xae: invokevirtual,
    0xaf: invokevirtual,
    0xb0: invokevirtual,
    0xb1: invokevirtual,
    0xb2: invokevirtual,
    0xb3: invokevirtual,
    0xb4: invokevirtual,

    0xb5: invokestatic,
    0xb6: invokestatic,
    0xb7: invokestatic,
    0xb8: invokestatic,
    0xb9: invokestatic,
    0xba: invokestatic,
    0xbb: invokestatic,
    0xbc: invokestatic,
    0xbd: invokestatic,
    0xbe: invokestatic,

    0xbf: invokevirtual,
    0xc0: invokevirtual,
    0xc1: invokevirtual,
    0xc2: invokevirtual,
    0xc3: invokevirtual,
    0xc4: invokevirtual,
    0xc5: invokevirtual,
    0xc6: invokevirtual,
    0xc7: invokevirtual,
    0xc8: invokevirtual,

    0xca: new,
    0xcb: newarray,
    0xcc: anewarray,
    0xcd: copyname("arraylength"),
    0xce: copyname("athrow"),
    0xcf: checkcast,
    0xd0: instanceof,
    0xd1: copyname("monitorenter"),
    0xd2: copyname("monitorexit"),
    0xd3: multianewarray,
    0xd4: branch("ifnull"),
    0xd5: branch("ifnonnull"),
    0xd6: invokevirtual,
    0xd7: invokevirtual,
    0xd8: invokestatic,
    0xd9: invokestatic,
}
