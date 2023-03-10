package com.davinci.vmp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class ManweVmpDex {
    public final ManweVmpConstantPool constantPool;
    public final byte[] magic = new byte[3];

    public int vmpVersion;
    public Map<String, byte[]> headers;
    public ManweVmpClazz[] manweVmpClazzes;

    public static int[] opcodeStatistics = new int[256];
    //Step3, add TypeAndNameInfo for JVM
/*
CONSTANT_Fieldref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}

CONSTANT_Methodref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}

CONSTANT_InterfaceMethodref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
CONSTANT_Class_info {
    u1 tag;
    u2 name_index;
}
CONSTANT_NameAndType_info {
    u1 tag;
    u2 name_index;
    u2 descriptor_index;
}
         */


    public ManweVmpDex(final ManweVmpDataInputStream in) throws IOException {
        final ManweVmpDataInputStream inStream = in;
        // 读3字节的magic
        inStream.read(this.magic);
        if (magic[0] != 0x1A || magic[1] != 0x0C || magic[2] != 0x06) {
            throw new IOException("Check Magic Fail");
        }

        // 读1字节的版本号
        this.vmpVersion = inStream.read();
        System.out.println(this.vmpVersion);
        // 读 headers
        if (this.vmpVersion == 5)
            this.headers = inStream.readStrBytesMap();
        else if (this.vmpVersion < 5)
            this.headers = new HashMap<>();
        // 读 ConstantPool
        this.constantPool = new ManweVmpConstantPool(inStream);
        PoolFixer.init(this.constantPool);
        // 读 N 个class
        int clazzCount = inStream.readUnsignedShort();
        this.manweVmpClazzes = new ManweVmpClazz[clazzCount];
        for (int i = 0; i < clazzCount; i++) {
            this.manweVmpClazzes[i] = new ManweVmpClazz(inStream, constantPool, this.vmpVersion);
            System.out.printf("VmpClass [%d/%d] success, %s%n", i, clazzCount, this.manweVmpClazzes[i]);
        }
        Arrays.stream(this.manweVmpClazzes).forEach(RuntimeTypeFixer::parseVmpClazz);
        this.constantPool.records = PoolFixer.moveZeroToLast(this.constantPool, this.manweVmpClazzes);
        this.constantPool.records = RuntimeTypeFixer.addSingleStringToPool(this.constantPool, "Code");
        this.constantPool.records = RuntimeTypeFixer.addStringToPool(this.constantPool);

        PoolFixer.analyzeUtf8ToClazz(this.constantPool.records);
        PoolFixer.patchForFieldAndMethod(this.constantPool.records);
        Arrays.stream(this.manweVmpClazzes).forEach(clazz -> {
            PoolFixer.patchConstantPoolForClassMeta(clazz, this.constantPool.records.length);
        });
        this.constantPool.records = PoolFixer.doFinal(this.constantPool.records);
    }

    public void writeClazzes(String dir) throws IOException {
        for (ManweVmpClazz manweVmpClazz : manweVmpClazzes) {
            System.out.println("start write " + manweVmpClazz.clazzName);
            Path targetPath = Paths.get(dir + File.separator + manweVmpClazz.clazzName + ".class");
            Files.createDirectories(targetPath.getParent());
            OutputStream fos = Files.newOutputStream(targetPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            writeClazz(manweVmpClazz, new DataOutputStream(fos));
            fos.close();
        }
    }

    private void writeClazz(ManweVmpClazz manweVmpClazz, DataOutputStream outStream) throws IOException {
        /*
        ClassFile {
            u4             magic;
            u2             minor_version;
            u2             major_version;
            u2             constant_pool_count;
            cp_info        constant_pool[constant_pool_count-1];
            u2             access_flags;
            u2             this_class;
            u2             super_class;
            u2             interfaces_count;
            u2             interfaces[interfaces_count];
            u2             fields_count;
            field_info     fields[fields_count];
            u2             methods_count;
            method_info    methods[methods_count];
            u2             attributes_count;
            attribute_info attributes[attributes_count];
        }
        * */

        // write cafebabe
        outStream.writeInt(0xCAFEBABE);
        // write minor_version
        outStream.writeShort(0);
        // write major_version
        outStream.writeShort(52);

        // write constant pool
        writeConstantPool(outStream, manweVmpClazz.constantPool, false);
        // write access_flag
        outStream.writeShort(manweVmpClazz.access_flag);
        // write this_class
        outStream.writeShort(manweVmpClazz.jvmClazzNameIdx);
        // write super_class
        outStream.writeShort(manweVmpClazz.jvmParentNameIdx);
        // write interface
        outStream.writeShort(manweVmpClazz.jvmInterfaceIdxes.length);
        for (int i = 0; i < manweVmpClazz.jvmInterfaceIdxes.length; i++) {
            outStream.writeShort(manweVmpClazz.jvmInterfaceIdxes[i]);
        }
        // write field
        writeField(outStream, manweVmpClazz.fieldMap);
        // write method
        writeMethod(outStream, manweVmpClazz.methodMap);
        // write annotation
        // TODO: attribute未输出
        outStream.writeShort(0);
    }

    private void writeMethod(DataOutputStream outStream, Map<String, ManweVmpMethod> methodMap) throws IOException {
        /*
        method_info {
            u2             access_flags;
            u2             name_index;
            u2             descriptor_index;
            u2             attributes_count;
            attribute_info attributes[attributes_count];
        }*/
        outStream.writeShort(methodMap.size());
        for (ManweVmpMethod manweVmpMethod : methodMap.values()) {
            outStream.writeShort(manweVmpMethod.access_flag);
            // 因为将 pool 整体偏移了1格，这两个数据是可以直接使用的
            outStream.writeShort(manweVmpMethod.nameIdx);
            outStream.writeShort(manweVmpMethod.descriptionIdx);
            // TODO: attribute未输出
            // 但需要手工补充一个Code的attribute
            if (manweVmpMethod.manweCode == null) {
                outStream.writeShort(0);
                continue;
            }
            outStream.writeShort(1);

            /*
            Code_attribute {
                u2 attribute_name_index;
                u4 attribute_length;
                u2 max_stack;
                u2 max_locals;
                u4 code_length;
                u1 code[code_length];
                u2 exception_table_length;
                {   u2 start_pc;
                    u2 end_pc;
                    u2 handler_pc;
                    u2 catch_type;
                } exception_table[exception_table_length];
                u2 attributes_count;
                attribute_info attributes[attributes_count];
            }*/
            outStream.writeShort(PoolFixer.findFixedStringIdx("Code"));
            ManweCode manweCode = manweVmpMethod.manweCode;
            int attrlen = 2 + 2 + 4 + manweCode.bytecodeLen + 2 + manweCode.exceptionTableCount * 8 + 2;
            outStream.writeInt(attrlen);
            outStream.writeShort(manweCode.max_stack);
            outStream.writeShort(manweCode.max_locals);
            outStream.writeInt(manweCode.bytecodeLen);
            outStream.write(manweCode.fixMisc(constantPool));
            outStream.writeShort(manweCode.exceptionTableCount);
            for (ManweCode.VmpExceptionTable exceptionTable : manweCode.exceptionTables) {
                outStream.writeShort(manweVmpMethod.getRealExceptionOffset(exceptionTable.i1));
                outStream.writeShort(manweVmpMethod.getRealExceptionOffset(exceptionTable.i2));
                outStream.writeShort(manweVmpMethod.getRealExceptionOffset(exceptionTable.i3));
                if (exceptionTable.i4 != 0xffff) {
                    String clazzName = this.constantPool.records[exceptionTable.i4 - 1].asString();
                    outStream.writeShort(PoolFixer.getJvmClazzIdx(clazzName));
                } else {
                    outStream.writeShort(0);
                }
                //outStream.writeShort(exceptionTable.i4);
            }
            outStream.writeShort(0);
        }

    }

    private void writeField(DataOutputStream outStream, Map<String, ManweVmpField> fieldMap) throws IOException {
        /*
        * field_info {
            u2             access_flags;
            u2             name_index;
            u2             descriptor_index;
            u2             attributes_count;
            attribute_info attributes[attributes_count];
        }*/
        outStream.writeShort(fieldMap.size());
        for (ManweVmpField manweVmpField : fieldMap.values()) {
            outStream.writeShort(manweVmpField.access_flag);
            // 因为将 pool 整体偏移了1格，这两个数据是可以直接使用的
            outStream.writeShort(manweVmpField.nameIdx);
            outStream.writeShort(manweVmpField.fieldTypeIdx);
            // TODO: attribute未输出
            outStream.writeShort(0);
        }
    }

    private static final boolean fixLongToInt = true;

    private void writeConstantPool(DataOutputStream outStream, ManweVmpConstantPool constantPool, boolean debug) throws IOException {
        if (fixLongToInt) {
            // TODO: 由于long全被干成int了，这里也不用计算了
            // 这个 +1 因为 jvm 是从1开始计数的，而vmp 是从0开始计数的
            outStream.writeShort(constantPool.size() + 1);
        } else {
            // 因为long（和double）会占用额外的数据空间，所以size要加上long的size
            int constantLongCount = (int) Stream.of(constantPool.records)
                    .filter(x -> x.type == ManweVmpConstantPoolItem.LONG)
                    .count();
            outStream.writeShort(constantPool.size() + constantLongCount + 1);
        }
        for (int i = 0; i < constantPool.records.length; i++) {
            ManweVmpConstantPoolItem record = constantPool.records[i];
            switch (record.type) {
                case ManweVmpConstantPoolItem.BOOL: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_INTEGER);
                    outStream.writeByte(record.asInt());
                    if (debug) {
                        System.out.println("[" + i + "] BOOL:" + record.asInt());
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.INTEGER: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_INTEGER);
                    outStream.writeInt(record.asInt());
                    if (debug) {
                        System.out.println("[" + i + "] INTEGER:" + record.asInt());
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.LONG: {
                    // TODO: 这里long会占用jvm 的2个序号，临时写个int，实际上，应该修改 field 和 method 里对它们的引用
                    //  opcode about long should be fix
                    if (fixLongToInt) {
                        outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_INTEGER);
                        outStream.writeInt(0);
                        if (debug) {
                            System.out.println("[" + i + "] INTEGER(fix):" + 0);
                        }
                    } else {
                        outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_LONG);
                        outStream.writeLong(record.asLong());
                        if (debug) {
                            System.out.println("[" + i + "] LONG:" + record.asLong());
                        }
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.FLOAT: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_FLOAT);
                    outStream.writeFloat(record.asFloat());
                    if (debug) {
                        System.out.println("[" + i + "] FLOAT:" + record.asFloat());
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.CONSTANT_UTF8: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_UTF8);
                    outStream.writeUTF(record.asString());
                    if (debug) {
                        System.out.println("[" + i + "] CONSTANT_UTF8:" + record.asString());
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.VMP_CLAZZ_REF: {
                    // 引用自cp，需要fix
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_CLASSREF);
                    outStream.writeShort(PoolFixer.fix(((ManweVmpConstantPool.ClazzDefine) record.value).clazzNameIdx));
                    if (debug) {
                        System.out.println("[" + i + "] CLAZZ_REF:" + ((ManweVmpConstantPool.ClazzDefine) record.value).clazzNameIdx);
                        System.out.println("[" + i + "] CLAZZ_REF:" + record.value);
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.VMP_FIELD_REF: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_FIELDREF);
                    outStream.writeShort(PoolFixer.fix(((ManweVmpConstantPool.FieldDefine) record.value).jvmClazzIdx));
                    outStream.writeShort(PoolFixer.fix(((ManweVmpConstantPool.FieldDefine) record.value).jvmNameAndTypeIdx));
                    if (debug) {
                        System.out.println("[" + i + "] FIELD_REF:" + ((ManweVmpConstantPool.FieldDefine) record.value).clazzIdx);
                        System.out.println("[" + i + "] FIELD_REF:" + ((ManweVmpConstantPool.FieldDefine) record.value).fieldNameIdx);
                        System.out.println("[" + i + "] FIELD_REF:" + record.value);
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.VMP_METHOD_REF: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_METHODREF);
                    outStream.writeShort(PoolFixer.fix(((ManweVmpConstantPool.MethodDefine) record.value).jvmClazzIdx));
                    outStream.writeShort(PoolFixer.fix(((ManweVmpConstantPool.MethodDefine) record.value).jvmNameAndTypeIdx));
                    if (debug) {
                        System.out.println("[" + i + "] METHOD_REF:" + ((ManweVmpConstantPool.MethodDefine) record.value).clazzIdx);
                        System.out.println("[" + i + "] METHOD_REF:" + ((ManweVmpConstantPool.MethodDefine) record.value).methodDescriptionIdx);
                        System.out.println("[" + i + "] METHOD_REF:" + record.value);
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.ANNOTATION:
                case ManweVmpConstantPoolItem.ENUM_REF:
                case ManweVmpConstantPoolItem.USHORT_ARRAY: {
                    // TODO: 这里需要有个占位的数据，正常情况下没人会引用它，写个 int 0
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_INTEGER);
                    outStream.writeInt(0xdeadbeef);
                    if (debug) {
                        System.out.println("[" + i + "] INTEGER:0xdeadbeef");
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.NAMEANDTYPE_REF: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_NAMETYPEREF);
                    outStream.writeShort(((ManweVmpConstantPool.NameAndTypeIndex) record.value).nameIdx);
                    outStream.writeShort(((ManweVmpConstantPool.NameAndTypeIndex) record.value).typeIdx);
                    if (debug) {
                        System.out.println("[" + i + "] NAMEANDTYPE_REF:" + record.value);
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.JVM_CLAZZ_INFO: {
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_CLASSREF);
                    outStream.writeShort(((ManweVmpConstantPool.JvmClassInfo) record.value).nameIdx);
                    if (debug) {
                        System.out.println("[" + i + "] JVM_CLAZZ_INFO:" + record.value);
                    }
                    break;
                }
                case ManweVmpConstantPoolItem.DOUBLE: {
                    //according to JVM standard,
                    /*
                    All 8-byte constants take up two entries in the constant_pool table of the class file. If a CONSTANT_Long_info or CONSTANT_Double_info structure is the item in the constant_pool table at index n, then the next usable item in the pool is located at index n+2. The constant_pool index n+1 must be valid but is considered unusable.
                     */
                    //outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_DOUBLE);
                    //outStream.writeDouble(record.asDouble());
                    //fix me 先不管double
                    outStream.writeByte(JJJVMConstantPoolItem.CONSTANT_INTEGER);
                    outStream.writeInt(0);
                    if (debug) {
                        System.out.println("[" + i + "] DOUBLE(fix):" + 0);
                    }
                    break;
                }
                default: {
                    System.out.println(record.type);
                    throw new RuntimeException("Unknown type");
                }
            }
        }
    }
}