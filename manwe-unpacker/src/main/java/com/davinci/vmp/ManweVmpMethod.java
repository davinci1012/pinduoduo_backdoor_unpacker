package com.davinci.vmp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManweVmpMethod {
    public final ManweVmpConstantPool constantPool;
    public final int nameIdx;
    public final String methodName;
    public final int descriptionIdx;
    public final String description;
    public final int access_flag;

    public final Map<String, ManweVmpConstantPool.VmpAnnotation> annotationMap;
    public final List<Integer> annotationIdxes;

    // 注意这个不是正常的 jvmCodeLen
    public final int vmpCodeLen;
    public final ManweCode manweCode;
    public final Map<String, byte[]> extraMap;

    public int clazzNameIdx;

    public ManweVmpMethod(ManweVmpDataInputStream inStream, ManweVmpConstantPool _constantPool, int version) throws IOException {

        /*
        CONSTANT_Methodref_info {
            u1 tag;
            u2 class_index;[ref]
            u2 name_and_type_index;[ref]
        }
        CONSTANT_Class_info {
            u1 tag;
            u2 name_index;[UTF8]
        }
        CONSTANT_NameAndType_info {
            u1 tag;
            u2 name_index;[UTF8]
            u2 descriptor_index;[UTF8]
        }
         */
        constantPool = _constantPool;
        nameIdx = inStream.readUnsignedShort();
        methodName = constantPool.getItemAt(nameIdx).asString();
        descriptionIdx = inStream.readUnsignedShort();
        description = constantPool.getItemAt(descriptionIdx).asString();
        access_flag = inStream.readUnsignedShort();

        int annotationCount = inStream.readUnsignedByte();
        annotationMap = new HashMap<>(annotationCount);
        annotationIdxes = new ArrayList<>();
        for (int j = 0; j < annotationCount; j++) {
            int annotationIdx = inStream.readUnsignedShort();
            annotationIdxes.add(annotationIdx);
            ManweVmpConstantPool.VmpAnnotation vmpAnnotation = (ManweVmpConstantPool.VmpAnnotation) constantPool.getItemAt(annotationIdx).getValue();
            annotationMap.put(vmpAnnotation.annotationName, vmpAnnotation);
        }
        // read data
        vmpCodeLen = inStream.readUnsignedShort();
        if (vmpCodeLen > 0) {
            byte[] bytecode = new byte[vmpCodeLen];
            inStream.readFully(bytecode, 0, vmpCodeLen);
            manweCode = new ManweCode(new ManweVmpDataInputStream(new ByteArrayInputStream(bytecode)));
        } else {
            manweCode = null;
        }
        if (version == 5)
            extraMap = inStream.readStrBytesMap();
        else
            extraMap = new HashMap<>();
        // Example: MethodRef{clazzName='com/xunmeng/pinduoduo/alive/base/ability/common/AliveAbility',
        //          methodName='isAbilityDisabled2022Q3',
        //          methodDescription='(Ljava/lang/String;)Z'}
    }


    public int getRealExceptionOffset(int instructionIdx) {
        return manweCode.instructions[instructionIdx].jvmOffset;
    }

    public void setClazzNameIdx(int clazzNameIdx) {
        this.clazzNameIdx = clazzNameIdx;
    }

    @Override
    public String toString() {
        return "VmpMethod{" +
                "methodName='" + methodName + '\'' +
                ", description='" + description + '\'' +
                ", bytecodeLen=" + vmpCodeLen +
                '}';
    }
}
