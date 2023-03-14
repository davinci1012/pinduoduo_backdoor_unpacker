
from enum import Enum


class OpType(Enum):
    I = 1
    IA = 2
    IQ = 3
    IAA = 4
    IAAX = 5


opinfo = {
    0: ["nop", OpType.I, 0],
    1: ["zero", OpType.I, 0],




    2: ["sipush2", OpType.IA, 0],
    3: ["sipush3", OpType.IQ, 0],
    4: ["sipush4", OpType.IA, 0],
    5: ["sipush5", OpType.IQ, 0],
    6: ["sipush6", OpType.IA, 0],
    7: ["sipush7", OpType.IA, 0],
    8: ["ldc2_w", OpType.IA, 14],
    9: ["ldc2_w", OpType.IA, 14],
    0xA: ["iload", OpType.IA, 0x15],
    0xB: ["lload", OpType.IA, 0x16],
    0xC: ["fload", OpType.IA, 0x17],
    0xD: ["dload", OpType.IA, 0x18],

    0xe: ["aload", OpType.IA, 0x19],  # load wtf from vars #index
    # sure
    0xf: ["iaload", OpType.I, 0x2e],
    0x10: ["laload", OpType.I, 0x2f],
    0x11: ["faload", OpType.I, 0x30],
    0x12: ["daload", OpType.I, 0x31],
    0x13: ["aaload", OpType.I, 0x32],
    0x14: ["baload", OpType.I, 0x33],
    0x15: ["caload", OpType.I, 0x34],
    0x16: ["saload", OpType.I, 0x35],
    # sure
    0x17: ["istore", OpType.IA, 0x36],
    0x18: ["lstore", OpType.IA, 0x37],
    0x19: ["fstore", OpType.IA, 0x38],
    0x1a: ["dstore", OpType.IA, 0x39],
    0x1b: ["astore", OpType.IA, 0x3a],

    # sure
    0x1c: ["iastore", OpType.I, 0x4f],
    0x1d: ["lastore", OpType.I, 0x50],
    0x1e: ["fastore", OpType.I, 0x51],
    0x1f: ["dastore", OpType.I, 0x52],
    0x20: ["aastore", OpType.I, 0x53],
    0x21: ["bastore", OpType.I, 0x54],
    0x22: ["castore", OpType.I, 0x55],
    0x23: ["sastore", OpType.I, 0x56],

    0x24: ["unknown_24", OpType.IA, 0x57],
    # 0x25: ["pop2", OpType.I, 0x58],
    0x26: ["dup", OpType.IA, 0x59],
    0x27: ["dup_x1", OpType.IA, 0x5a],
    0x28: ["dup_x2", OpType.IA, 0x5b],
    0x29: ["dup2", OpType.IA, 0x5c],
    0x2a: ["dup2_x1", OpType.IA, 0x5d],
    0x2b: ["dup2_x2", OpType.IA, 0x5e],

    0x2c: ["swap", OpType.I, 0x5f],  # swap
    0x2d: ["iadd", OpType.I, 0x60],
    0x2e: ["ladd", OpType.I, 0x61],
    0x2f: ["fadd", OpType.I, 0x62],
    0x30: ["dadd", OpType.I, 0x63],
    0x31: ["isub", OpType.I, 0x64],  # int sub
    0x32: ["lsub", OpType.I, 0x65],
    0x33: ["fsub", OpType.I, 0x66],
    0x34: ["dsub", OpType.I, 0x67],
    0x35: ["imul", OpType.I, 0x68],
    0x36: ["lmul", OpType.I, 0x69],
    0x37: ["fmul", OpType.I, 0x6a],
    0x38: ["dmul", OpType.I, 0x6b],
    0x39: ["idiv", OpType.I, 0x6c],
    0x3a: ["ldiv", OpType.I, 0x6d],
    0x3b: ["fdiv", OpType.I, 0x6e],
    0x3c: ["ddiv", OpType.I, 0x6f],
    0x3d: ["irem", OpType.I, 0x70],
    0x3e: ["lrem", OpType.I, 0x71],
    0x3f: ["frem", OpType.I, 0x72],
    0x40: ["drem", OpType.I, 0x73],
    0x41: ["ineg", OpType.I, 0x74],  # negate int
    0x42: ["lneg", OpType.I, 0x75],  # negate a long
    0x43: ["fneg", OpType.I, 0x76],  # negate a float
    0x44: ["dneg", OpType.I, 0x77],  # negate a double
    0x45: ["ishl", OpType.I, 0x78],  # int shift left
    0x46: ["lshl", OpType.I, 0x79],
    0x47: ["ishr", OpType.I, 0x7a],
    0x48: ["lshr", OpType.I, 0x7b],
    0x49: ["iushr", OpType.I, 0x7c],
    0x4a: ["lushr", OpType.I, 0x7d],
    0x4b: ["iand", OpType.I, 0x7e],
    0x4c: ["land", OpType.I, 0x7f],
    0x4d: ["ior", OpType.I, 0x80],
    0x4e: ["lor", OpType.I, 0x81],
    0x4f: ["ixor", OpType.I, 0x82],
    0x50: ["lxor", OpType.I, 0x83],
    0x51: ["iinc", OpType.IAA, 0x84],
    0x52: ["i2l", OpType.I, 0x85],
    0x53: ["i2f", OpType.I, 0x86],
    0x54: ["i2d", OpType.I, 0x87],
    0x55: ["l2i", OpType.I, 0x88],
    0x56: ["l2f", OpType.I, 0x89],
    0x57: ["l2d", OpType.I, 0x8a],
    0x58: ["f2i", OpType.I, 0x8b],
    0x59: ["f2l", OpType.I, 0x8c],
    0x5a: ["f2d", OpType.I, 0x8d],
    0x5b: ["d2i", OpType.I, 0x8e],
    0x5c: ["d2l", OpType.I, 0x8f],
    0x5d: ["d2f", OpType.I, 0x90],
    0x5e: ["i2b", OpType.I, 0x91],
    0x5f: ["i2c", OpType.I, 0x92],
    0x60: ["i2s", OpType.I, 0x93],
    0x61: ["lcmp", OpType.I, 0x94],
    0x62: ["fcmp", OpType.I, 0x95],
    0x63: ["fcmpg", OpType.I, 0x96],
    0x64: ["dcmpl", OpType.I, 0x97],
    0x65: ["dcmpg", OpType.I, 0x98],
    # branch code
    0x66: ["ifeq", OpType.IA, 0x99],
    0x67: ["ifne", OpType.IA, 0x9a],
    0x68: ["iflt", OpType.IA, 0x9b],
    0x69: ["ifge", OpType.IA, 0x9c],
    0x6a: ["ifgt", OpType.IA, 0x9d],
    0x6b: ["ifle", OpType.IA, 0x9e],
    0x6c: ["if_icmpeq", OpType.IA, 0x9f],
    0x6d: ["if_icmpne", OpType.IA, 0xa0],
    0x6e: ["if_icmplt", OpType.IA, 0xa1],
    0x6f: ["if_icmpge", OpType.IA, 0xa2],
    0x70: ["if_icmpgt", OpType.IA, 0xa3],
    0x71: ["if_icmple", OpType.IA, 0xa4],
    0x72: ["if_acmpeq", OpType.IA, 0xa5],
    0x73: ["if_acmpne", OpType.IA, 0xa6],
    0x74: ["goto", OpType.IA, 0xa7],

    0x75: ["tableswitch", OpType.IAAX, 0xaa],
    0x76: ["lookupswitch", OpType.IAAX, 0xab],
    0x77: ["ireturn", OpType.I, 0xac],
    0x78: ["lreturn", OpType.I, 0xad],
    0x79: ["freturn", OpType.I, 0xae],
    0x7a: ["dreturn", OpType.I, 0xaf],
    0x7b: ["areturn", OpType.I, 0xb0],
    0x7c: ["return", OpType.I, 0xb1],

    0x7d: ["getstatic", OpType.IA, 0xb2],
    0x7e: ["getstatic", OpType.IA, 0xb2],
    0x7f: ["getstatic", OpType.IA, 0xb2],
    0x80: ["getstatic", OpType.IA, 0xb2],
    0x81: ["getstatic", OpType.IA, 0xb2],
    0x82: ["getstatic", OpType.IA, 0xb2],
    0x83: ["getstatic", OpType.IA, 0xb2],
    0x84: ["getstatic", OpType.IA, 0xb2],
    0x85: ["getstatic", OpType.IA, 0xb2],

    0x86: ["putstatic", OpType.IA, 0xb3],
    0x87: ["putstatic", OpType.IA, 0xb3],
    0x88: ["putstatic", OpType.IA, 0xb3],
    0x89: ["putstatic", OpType.IA, 0xb3],
    0x8a: ["putstatic", OpType.IA, 0xb3],
    0x8b: ["putstatic", OpType.IA, 0xb3],
    0x8c: ["putstatic", OpType.IA, 0xb3],
    0x8d: ["putstatic", OpType.IA, 0xb3],
    0x8e: ["putstatic", OpType.IA, 0xb3],

    0x8f: ["getfield", OpType.IA, 0xb4],
    0x90: ["getfield_90", OpType.IA, 0xb4],
    0x91: ["getfield_91", OpType.IA, 0xb4],
    0x92: ["getfield_92", OpType.IA, 0xb4],
    0x93: ["getfield", OpType.IA, 0xb4],
    0x94: ["getfield", OpType.IA, 0xb4],
    0x95: ["getfield", OpType.IA, 0xb4],
    0x96: ["getfield", OpType.IA, 0xb4],
    0x97: ["getfield", OpType.IA, 0xb4],

    0x97: ["putfield", OpType.IA, 0xb5],
    0x98: ["putfield", OpType.IA, 0xb5],
    0x99: ["putfield", OpType.IA, 0xb5],
    0x9a: ["putfield", OpType.IA, 0xb5],
    0x9b: ["putfield", OpType.IA, 0xb5],
    0x9c: ["putfield", OpType.IA, 0xb5],
    0x9d: ["putfield", OpType.IA, 0xb5],
    0x9e: ["putfield", OpType.IA, 0xb5],
    0x9f: ["putfield", OpType.IA, 0xb5],
    0xa0: ["putfield", OpType.IA, 0xb5],


    0xa1: ["invokevirtual", OpType.IA, 0xb6],
    0xa2: ["invokevirtual", OpType.IA, 0xb6],
    0xa3: ["invokevirtual", OpType.IA, 0xb6],
    0xa4: ["invokevirtual", OpType.IA, 0xb6],
    0xa5: ["invokevirtual", OpType.IA, 0xb6],
    0xa6: ["invokevirtual", OpType.IA, 0xb6],
    0xa7: ["invokevirtual", OpType.IA, 0xb6],
    0xa8: ["invokevirtual", OpType.IA, 0xb6],
    0xa9: ["invokevirtual", OpType.IA, 0xb6],
    0xaa: ["invokevirtual", OpType.IA, 0xb6],

    0xab: ["invokespecial", OpType.IA, 0xb7],
    0xac: ["invokespecial", OpType.IA, 0xb7],
    0xad: ["invokespecial", OpType.IA, 0xb7],
    0xae: ["invokespecial", OpType.IA, 0xb7],
    0xaf: ["invokespecial", OpType.IA, 0xb7],
    0xb0: ["invokespecial", OpType.IA, 0xb7],
    0xb1: ["invokespecial", OpType.IA, 0xb7],
    0xb2: ["invokespecial", OpType.IA, 0xb7],
    0xb3: ["invokespecial", OpType.IA, 0xb7],
    0xb4: ["invokespecial", OpType.IA, 0xb7],

    0xb5: ["invokestatic", OpType.IA, 0xb8],
    0xb6: ["invokestatic", OpType.IA, 0xb8],
    0xb7: ["invokestatic", OpType.IA, 0xb8],
    0xb8: ["invokestatic", OpType.IA, 0xb8],
    0xb9: ["invokestatic", OpType.IA, 0xb8],
    0xba: ["invokestatic", OpType.IA, 0xb8],
    0xbb: ["invokestatic", OpType.IA, 0xb8],
    0xbc: ["invokestatic", OpType.IA, 0xb8],
    0xbd: ["invokestatic", OpType.IA, 0xb8],
    0xbe: ["invokestatic", OpType.IA, 0xb8],


    0xbf: ["invokevirtual", OpType.IA, 0xb6],
    0xc0: ["invokevirtual", OpType.IA, 0xb6],
    0xc1: ["invokevirtual", OpType.IA, 0xb6],
    0xc2: ["invokevirtual", OpType.IA, 0xb6],
    0xc3: ["invokevirtual", OpType.IA, 0xb6],
    0xc4: ["invokevirtual", OpType.IA, 0xb6],
    0xc5: ["invokevirtual", OpType.IA, 0xb6],
    0xc6: ["invokevirtual", OpType.IA, 0xb6],
    0xc7: ["invokevirtual", OpType.IA, 0xb6],
    0xc8: ["invokevirtual", OpType.IA, 0xb6],



    0xca: ["new", OpType.IA, 0xbb],
    0xcb: ["newarray", OpType.IA, 0xbc],
    0xcc: ["anewarray", OpType.IA, 0xbd],
    0xcd: ["arraylength", OpType.I, 0xbe],
    0xce: ["athrow", OpType.I, 0xbf],
    0xcf: ["checkcast", OpType.IA, 0xc0],
    0xd0: ["instanceof", OpType.IA, 0xc1],
    0xd1: ["monitorenter", OpType.I, 0xc2],
    0xd2: ["monitorexit", OpType.I, 0xc3],


    0xd3: ["multianewarray", OpType.IAA, 0xc5],
    0xd4: ["ifnull", OpType.IA, 0xc6],
    0xd5: ["ifnonnull", OpType.IA, 0xc7],

    0xd6: ["goto_w", OpType.IA, 0xc8],
    0xd7: ["invokevirtual", OpType.IA, 0xc9],

    0xd8: ["invokestatic", OpType.IA, 0xc9],
    0xd9: ["invokestatic", OpType.IA, 0xc9],












}
