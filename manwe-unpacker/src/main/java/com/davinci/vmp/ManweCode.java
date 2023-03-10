package com.davinci.vmp;

import static com.davinci.vmp.PoolFixer.originalZeroItemIndex;

import java.io.*;
import java.util.Map;

public class ManweCode {

    public static final byte[] firstTable;
    public static final int OP_WIDE_PREFIX = 0xc4;
    public static final int OP_IINC = 0x84;
    public static final int OP_GOTO_W = 0xc8;
    public static final int OP_JSR_W = 0xc9;
    public static final int OP_INVOKE_INTERFACE = 0xB9;
    public static final int OP_NEW = 0xBB;
    public static final int OP_ANEWARRAR = 0xBD;
    public static final int OP_CHECKCAST = 0xC0;
    public static final int OP_INSTANCEOF = 0xC0;
    public static final int OP_BIPUSH = 16;
    public static final int OP_SIPUSH = 17;
    public static final int OP_NEWARRAY = 188;
    public static final int OP_NOP = 0;
    public static final int OP_LDC = 0x12;
    public static final int OP_LDC_W = 0x13;

    static {
        byte[] v1 = new byte[201];
        for (int i = 0; i < 201; ++i) {
            v1[i] = (byte) ("000000000000000011999222222222222222222222222200000000222222222222222222222222200000000000000000000000000000000000000000000000000000:0000000000000000000077777777777777772;<0000004444555563130033000=7777".charAt(i) - 0x30);
        }
        firstTable = v1;
    }

    public final int max_stack;
    public final int max_locals;
    @Deprecated
    public final byte[] bytecode;
    public final int bytecodeLen;
    public ManweVmpInstruction[] instructions;
    public int exceptionTableCount;
    public VmpExceptionTable[] exceptionTables;

    public ManweCode(ManweVmpDataInputStream inStream) throws IOException {
        max_locals = inStream.readUnsignedShort();
        max_stack = inStream.readUnsignedShort();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // for count length of data ONLY
        DataOutputStream outStreamCntOnly = new DataOutputStream(bos);
        int vmpCodeLen = inStream.readUnsignedShort();
        instructions = new ManweVmpInstruction[vmpCodeLen];
        for (int i = 0; i < vmpCodeLen; i++) {
            int opcode = inStream.readUnsignedByte();
            ManweVmpDex.opcodeStatistics[opcode]++;
            instructions[i] = new ManweVmpInstruction(opcode);
            ManweVmpInstruction instruction = instructions[i];
            instruction.setJvmOffset(outStreamCntOnly.size());
            switch (instruction.tableType) {
                case 0: {
                    // case 0 的长度是1 0-15可原样写回
                    outStreamCntOnly.writeByte(opcode);
                    break;
                }
                case 1: {
                    if (opcode == OP_BIPUSH || opcode == OP_SIPUSH) {
                        // bipush是byte ipush，sipush是 short ipush
                        // 因为oper 是个short，存在溢出的现象，将二者合并
                        outStreamCntOnly.writeByte(OP_SIPUSH);
                        instruction.opcode = OP_SIPUSH;
                    } else if (opcode == OP_NEWARRAY) {
                        outStreamCntOnly.writeByte(opcode);
                    } else {
                        throw new UnreachableException("not reachable");
                    }
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    break;
                }
                case 3: {
                    // OP_NEW, OP_ANEWARRAR, OP_CHECKCAST, OP_INSTANCEOF
                    outStreamCntOnly.writeByte(opcode);
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    break;
                }
                case 4: {
                    outStreamCntOnly.writeByte(opcode);
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    break;
                }
                case 5: {
                    // 5是4个invoke
                    outStreamCntOnly.writeByte(opcode);
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    if (opcode == OP_INVOKE_INTERFACE) {
                        //这里是个count，占个位置即可
                        outStreamCntOnly.writeShort(0);
                    }
                    break;
                }
                case 7: {
                    // case7是条件跳转，VMP和JVM实现不一致，需要repair
                    outStreamCntOnly.writeByte(opcode);
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    if (opcode == OP_GOTO_W || opcode == OP_JSR_W) {
                        throw new UnreachableException("goto_w 和 jsr_w 暂时不会遇到");
                    }
                    break;
                }
                case 9: {
                    if (instruction.opcode == OP_LDC || instruction.opcode == OP_LDC_W) {
                        // 归一为LDC_W
                        int oper = inStream.readUnsignedShort();
                        if (oper == 0) {
                            throw new UnreachableException("ldc #0，但0早就被移走了，按理说这里不会触发");
                        }
                        outStreamCntOnly.writeByte(OP_LDC_W);
                        instruction.opcode = OP_LDC_W;
                        outStreamCntOnly.writeShort(oper);
                        instruction.setOperand1(oper);
                    } else {
                        throw new UnreachableException("ldc2_w 暂时不会遇到");
                    }
                    break;
                }
                case 2: {
                    outStreamCntOnly.writeByte(opcode);
                    int toWrite = inStream.readUnsignedByte();
                    if (toWrite == 0xFF) {
                        // 虽然这里需要处理，但实际上它不可达，不使用宽指令
                        toWrite = inStream.readUnsignedShort();
                        throw new UnreachableException("发现宽指令需要处理");
                    }
                    outStreamCntOnly.writeByte(toWrite);
                    instruction.setOperand1(toWrite);
                    break;
                }
                case 10: {
                    // 132 0x84h iinc
                    // iinc localIndex constData
                    /*
                    iinc
                    index
                    const

                    wide
                    iinc
                    indexbyte1
                    indexbyte2
                    constbyte1
                    constbyte2
                     */
                    int localIndex = inStream.readUnsignedByte();
                    int constData;
                    if (localIndex == 0xFF) {
                        localIndex = inStream.readUnsignedShort();
                        constData = inStream.readUnsignedShort();
                        // 虽然这里需要处理，但实际上它不可达，不使用宽指令
                        outStreamCntOnly.writeByte(0xc4);//wide 196
                        outStreamCntOnly.writeByte(opcode);
                        outStreamCntOnly.writeShort(localIndex);
                        outStreamCntOnly.writeShort(constData);
                        instruction.setWide(true);
                    } else {
                        constData = inStream.readUnsignedByte();
                        outStreamCntOnly.writeByte(opcode);
                        outStreamCntOnly.writeByte(localIndex);
                        outStreamCntOnly.writeByte(constData);
                    }
                    instruction.setOperand1(localIndex);
                    instruction.setOperand2(constData);
                    break;
                }
                case 12: {
                    //这是个switch，只可能是171 0xAB lookupswitch

                    // u1 opcode
                    // u? padding
                    // i4 default
                    // i4 npairs
                    //   i4 match * npairs
                    //   i4 offset * npairs

                    // 这段 outStreamCntOnly 纯属是算大小的，数据毫无意义，数据在fix中才有意义
                    outStreamCntOnly.writeByte(opcode);
                    fixForSwitchPadding(outStreamCntOnly, instruction);
                    int len = inStream.readUnsignedShort();
                    outStreamCntOnly.writeInt(len);
                    final int defaultValue = 0x1234;
                    outStreamCntOnly.writeInt(defaultValue);
                    for (int j = 0; j < len - 1; j++) {
                        outStreamCntOnly.writeInt(0);
                        outStreamCntOnly.writeInt(0);
                    }

                    for (int j = 0; j < len; j++) {
                        int choice = inStream.readInt();
                        int target = inStream.readUnsignedShort();
                        instruction.updateSwitchMap(choice, target);
                    }
                    break;
                }
                case 11: {
                    ManweVmpInstruction.Table table = new ManweVmpInstruction.Table();
                    table.min = inStream.readInt();
                    table.max = inStream.readInt();
                    table.i = inStream.readUnsignedShort();
                    int len = table.max - table.min + 1;
                    table.labels = new int[len];
                    for(int v21 = 0; v21 < len; ++v21) {
                        table.labels[v21] = inStream.readUnsignedShort();
                    }
                    instruction.tableSwitch = table;

                    //fill original jvm instruction counts
                    outStreamCntOnly.writeByte(opcode);
                    fixForSwitchPadding(outStreamCntOnly, instruction);
                    final int defaultValue = 0x1234;
                    outStreamCntOnly.writeInt(defaultValue);
                    outStreamCntOnly.writeInt(0); //lowint
                    outStreamCntOnly.writeInt(0); //highint


                    for (int j = 0; j < len; j++) {
                        outStreamCntOnly.writeInt(0);
                    }
                    break;
                }
                case 13: {
                    //multinewarray
                    outStreamCntOnly.writeByte(opcode);
                    instruction.setOperand1(inStream.readUnsignedShort());
                    instruction.setOperand2(inStream.readByte());
                    outStreamCntOnly.writeShort(instruction.getOperand1());
                    outStreamCntOnly.writeByte(instruction.getOperand2());
                    break;
                }
                default: {
                    throw new RuntimeException("其余未实现");
                }
            }
        }
        outStreamCntOnly.flush();
        bytecode = bos.toByteArray();
        bytecodeLen = bos.size();
        outStreamCntOnly.close();

        exceptionTableCount = inStream.readUnsignedByte();
        exceptionTables = new VmpExceptionTable[exceptionTableCount];
        for (int i = 0; i < exceptionTableCount; i++) {
            VmpExceptionTable vmpExceptionTable = new VmpExceptionTable();
            vmpExceptionTable.i1 = inStream.readUnsignedShort();
            vmpExceptionTable.i2 = inStream.readUnsignedShort();
            vmpExceptionTable.i3 = inStream.readUnsignedShort();
            vmpExceptionTable.i4 = inStream.readUnsignedShort(); // this entry may need to be fixed
            exceptionTables[i] = vmpExceptionTable;
        }

        if (inStream.available() != 0) {
            throw new RuntimeException("Code_attribute 还有未被解析的数据");
        }
    }

    public static class VmpExceptionTable {
        public int i1;
        public int i2;
        public int i3;
        public int i4;
    }

    public byte[] fixMisc(ManweVmpConstantPool pool) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(bos);

        for (int i = 0; i < instructions.length; i++) {
            ManweVmpInstruction instruction = instructions[i];
            int opcode = instruction.opcode;
            if (opcode == 9999) {
                // handle xxx
            } else {
                switch (instruction.tableType) {
                    case 0: {
                        // case 0 的长度是1 0-15可原样写回
                        outStream.writeByte(opcode);
                        break;
                    }
                    case 1: {
                        outStream.writeByte(opcode);
                        if (opcode == OP_SIPUSH) {
                            // bipush 被转化为sipush
                            // sipush 接一个 short
                            outStream.writeShort(instruction.getOperand1());
                        } else if (opcode == OP_NEWARRAY) {
                            // newarray 接一个byte表示类型
                            // 第二个nop是为了方便对齐的
                            outStream.writeByte(instruction.getOperand1());
                            outStream.writeByte(OP_NOP);
                        } else {
                            throw new UnreachableException("not reachable");
                        }
                        break;
                    }
                    case 3: {
                        // 第一个参数含义是类名
                        // VMP里这个字段是UTF8，JVM里这个字段是Const_ClassInfo
                        outStream.writeByte(opcode);
                        if (instruction.getOperand1() == 0) {
                            instruction.setOperand1(originalZeroItemIndex);
                        }
                        String clazzName = pool.records[instruction.getOperand1()-1].asString();
                        outStream.writeShort(PoolFixer.getJvmClazzIdx(clazzName));
                        break;
                    }
                    case 4: {
                        outStream.writeByte(opcode);
                        outStream.writeShort(instruction.getOperand1());
                        break;
                    }
                    case 5: {
                        if (opcode == OP_INVOKE_INTERFACE) {
                            outStream.writeByte(opcode);
                            outStream.writeShort(instruction.getOperand1());
                            ManweVmpConstantPool.MethodDefine methodDefine =
                                    (ManweVmpConstantPool.MethodDefine) pool.getItemAt(instruction.getOperand1() - 1).value;
                            String description = methodDefine.methodDescription;
                            String parameters = description.substring(description.indexOf('(') + 1, description.indexOf(')'));
                            parameters = parameters.replaceAll("L.*?;", "L");
                            parameters = parameters.replace("[", "");
                            int parameterCount = parameters.length();
                            // interface 要自加一，例如 addValue(E) 的 count 视为2
                            outStream.writeShort(parameterCount + 1);
                        } else {
                            outStream.writeByte(opcode);
                            outStream.writeShort(instruction.getOperand1());
                            //System.out.println(PoolFixer.getPool().getItemAt(instruction.getOperand1()-1).getValue().toString());
                        }
                        break;
                    }
                    case 7: {
                        // 条件跳转和goto语句，需要修复
                        // 例如op1是7，VMP表示跳转到[7]，JVM 表示跳转到 PC+7
                        outStream.writeByte(opcode);
                        int targetJvmPC = instructions[instruction.getOperand1()].jvmOffset;
                        int currentJvmPC = instruction.jvmOffset;
                        int jumpDiff = targetJvmPC - currentJvmPC;
                        outStream.writeShort(jumpDiff);
                        if (opcode == OP_GOTO_W || opcode == OP_JSR_W) {
                            throw new UnreachableException("goto_w 和 jsr_w暂时不会遇到");
                        }
                        break;
                    }
                    case 9: {
                        if (instruction.opcode == OP_LDC_W) {
                            // 归一为 LDC_W
                            outStream.writeByte(OP_LDC_W);
                            outStream.writeShort(instruction.getOperand1());
                        } else {
                            throw new UnreachableException("not reachable");
                        }
                        break;
                    }
                    case 2: {
                        outStream.writeByte(opcode);
                        outStream.writeByte(instruction.getOperand1());
                        break;
                    }
                    case 10: {
                        if (instruction.isWide) {
                            outStream.writeByte(0xc4);
                            outStream.writeByte(opcode);
                            outStream.writeShort(instruction.getOperand1());
                            outStream.writeShort(instruction.getOperand2());
                        } else {
                            outStream.writeByte(opcode);
                            outStream.writeByte(instruction.getOperand1());
                            outStream.writeByte(instruction.getOperand2());
                        }
                        break;
                    }
                    case 12: {
                        // switch语句，需要修复
                        // 区别1：与case7相同
                        // 区别2：default就是0x7FFF

                        // u1 opcode
                        // u? padding
                        // i4 default
                        // i4 npairs
                        //   i4 match * npairs
                        //   i4 offset * npairs


                        outStream.writeByte(opcode); // opcode
                        int len = instruction.lookupSwitch.size();
                        fixForSwitchPadding(outStream, instruction);
                        final int defaultKey = -32769;
                        Map<Integer, Integer> lookupSwitch = instruction.lookupSwitch;
                        int defaultValue = lookupSwitch.get(defaultKey);
                        outStream.writeInt(defaultValue); // default
                        outStream.writeInt(len - 1); // npairs
                        for (Map.Entry<Integer, Integer> entry : instruction.lookupSwitch.entrySet()) {
                            if (entry.getKey() == defaultKey) {
                                continue;
                            }
                            outStream.writeInt(entry.getKey()); // match
                            int targetJvmPC = instructions[entry.getValue()].jvmOffset;
                            int currentJvmPC = instruction.jvmOffset;
                            int jumpDiff = targetJvmPC - currentJvmPC;
                            outStream.writeInt(jumpDiff); // offset
                        }
                        break;
                    }
                    case 11: {
                        //fix for tableswitch
                        outStream.writeByte(opcode);
                        ManweVmpInstruction.Table table = instruction.tableSwitch;
                        int len = table.labels.length;
                        fixForSwitchPadding(outStream, instruction);
                        //fix for default
                        int defaultDiff = instructions[table.i].jvmOffset - instruction.jvmOffset;
                        outStream.writeInt(defaultDiff);

                        //high and low do not need fix
                        outStream.writeInt(table.min);
                        outStream.writeInt(table.max);
                        if (table.min > table.max) {
                            throw new RuntimeException("wtf");
                        }

                        //fix all offsets
                        for (int j = 0; j < len; j++) {
                            int offsetDiff = instructions[table.labels[j]].jvmOffset - instruction.jvmOffset;
                            outStream.writeInt(offsetDiff);
                        }
                        break;
                    }
                    case 13: {
                        //similar to case 3, multianewarray
                        outStream.writeByte(opcode);
                        if (instruction.getOperand1() == 0) {
                            instruction.setOperand1(originalZeroItemIndex);
                        }
                        String clazzName = pool.records[instruction.getOperand1()-1].asString();
                        outStream.writeShort(PoolFixer.getJvmClazzIdx(clazzName));
                        outStream.writeByte(instruction.getOperand2());
                        break;
                    }
                    default: {
                        throw new UnreachableException("其余未实现");
                    }
                }
            }
        }
        return bos.toByteArray();
    }

    private void fixForSwitchPadding(DataOutputStream outStream, ManweVmpInstruction instruction) throws IOException {
        int currentOffset = instruction.jvmOffset;
        int paddingLen = 4 - (currentOffset + 1) % 4;
        if (paddingLen >= 4)
            return;
        for (int j = 0; j < paddingLen; j++) {
            outStream.writeByte(0); //padding
        }
    }
}
