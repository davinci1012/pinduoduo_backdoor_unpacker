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
                    // case 0 ????????????1 0-15???????????????
                    outStreamCntOnly.writeByte(opcode);
                    break;
                }
                case 1: {
                    if (opcode == OP_BIPUSH || opcode == OP_SIPUSH) {
                        // bipush???byte ipush???sipush??? short ipush
                        // ??????oper ??????short??????????????????????????????????????????
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
                    // 5???4???invoke
                    outStreamCntOnly.writeByte(opcode);
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    if (opcode == OP_INVOKE_INTERFACE) {
                        //????????????count?????????????????????
                        outStreamCntOnly.writeShort(0);
                    }
                    break;
                }
                case 7: {
                    // case7??????????????????VMP???JVM????????????????????????repair
                    outStreamCntOnly.writeByte(opcode);
                    int oper = inStream.readUnsignedShort();
                    outStreamCntOnly.writeShort(oper);
                    instruction.setOperand1(oper);
                    if (opcode == OP_GOTO_W || opcode == OP_JSR_W) {
                        throw new UnreachableException("goto_w ??? jsr_w ??????????????????");
                    }
                    break;
                }
                case 9: {
                    if (instruction.opcode == OP_LDC || instruction.opcode == OP_LDC_W) {
                        // ?????????LDC_W
                        int oper = inStream.readUnsignedShort();
                        if (oper == 0) {
                            throw new UnreachableException("ldc #0??????0????????????????????????????????????????????????");
                        }
                        outStreamCntOnly.writeByte(OP_LDC_W);
                        instruction.opcode = OP_LDC_W;
                        outStreamCntOnly.writeShort(oper);
                        instruction.setOperand1(oper);
                    } else {
                        throw new UnreachableException("ldc2_w ??????????????????");
                    }
                    break;
                }
                case 2: {
                    outStreamCntOnly.writeByte(opcode);
                    int toWrite = inStream.readUnsignedByte();
                    if (toWrite == 0xFF) {
                        // ????????????????????????????????????????????????????????????????????????
                        toWrite = inStream.readUnsignedShort();
                        throw new UnreachableException("???????????????????????????");
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
                        // ????????????????????????????????????????????????????????????????????????
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
                    //?????????switch???????????????171 0xAB lookupswitch

                    // u1 opcode
                    // u? padding
                    // i4 default
                    // i4 npairs
                    //   i4 match * npairs
                    //   i4 offset * npairs

                    // ?????? outStreamCntOnly ??????????????????????????????????????????????????????fix???????????????
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
                    throw new RuntimeException("???????????????");
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
            throw new RuntimeException("Code_attribute ???????????????????????????");
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
                        // case 0 ????????????1 0-15???????????????
                        outStream.writeByte(opcode);
                        break;
                    }
                    case 1: {
                        outStream.writeByte(opcode);
                        if (opcode == OP_SIPUSH) {
                            // bipush ????????????sipush
                            // sipush ????????? short
                            outStream.writeShort(instruction.getOperand1());
                        } else if (opcode == OP_NEWARRAY) {
                            // newarray ?????????byte????????????
                            // ?????????nop????????????????????????
                            outStream.writeByte(instruction.getOperand1());
                            outStream.writeByte(OP_NOP);
                        } else {
                            throw new UnreachableException("not reachable");
                        }
                        break;
                    }
                    case 3: {
                        // ??????????????????????????????
                        // VMP??????????????????UTF8???JVM??????????????????Const_ClassInfo
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
                            // interface ????????????????????? addValue(E) ??? count ??????2
                            outStream.writeShort(parameterCount + 1);
                        } else {
                            outStream.writeByte(opcode);
                            outStream.writeShort(instruction.getOperand1());
                            //System.out.println(PoolFixer.getPool().getItemAt(instruction.getOperand1()-1).getValue().toString());
                        }
                        break;
                    }
                    case 7: {
                        // ???????????????goto?????????????????????
                        // ??????op1???7???VMP???????????????[7]???JVM ??????????????? PC+7
                        outStream.writeByte(opcode);
                        int targetJvmPC = instructions[instruction.getOperand1()].jvmOffset;
                        int currentJvmPC = instruction.jvmOffset;
                        int jumpDiff = targetJvmPC - currentJvmPC;
                        outStream.writeShort(jumpDiff);
                        if (opcode == OP_GOTO_W || opcode == OP_JSR_W) {
                            throw new UnreachableException("goto_w ??? jsr_w??????????????????");
                        }
                        break;
                    }
                    case 9: {
                        if (instruction.opcode == OP_LDC_W) {
                            // ????????? LDC_W
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
                        // switch?????????????????????
                        // ??????1??????case7??????
                        // ??????2???default??????0x7FFF

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
                        throw new UnreachableException("???????????????");
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
