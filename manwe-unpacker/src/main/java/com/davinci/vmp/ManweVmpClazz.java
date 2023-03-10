package com.davinci.vmp;

import java.io.IOException;
import java.util.*;

public class ManweVmpClazz {
    public final ManweVmpConstantPool constantPool;
    public int clazzNameIdx;
    public final String clazzName;
    public int parentNameIdx;
    public final String parentName;

    public int[] interfaceIdxes;
    public String[] interfaceNames;

    public final List<Integer> annotationIdxes;
    public final Map<String, ManweVmpConstantPool.VmpAnnotation> annotationMap;

    public final String unknown;
    public final int access_flag;

    public final Map<String, ManweVmpField> fieldMap;
    public final Map<String, ManweVmpMethod> methodMap;
    public final Map<String, byte[]> extraMap;

    public int jvmClazzNameIdx = -1;
    public int jvmParentNameIdx = -1;

    public int[] jvmInterfaceIdxes = null;
    public final int version;

    public ManweVmpClazz(ManweVmpDataInputStream inStream, ManweVmpConstantPool _constantPool, int _version) throws IOException {
        version = _version;
        constantPool = _constantPool;
        clazzNameIdx = inStream.readUnsignedShort(); //TODO need to be fixed
        parentNameIdx = inStream.readUnsignedShort(); //TODO need to be fixed
        clazzName = constantPool.getItemAt(clazzNameIdx).asString();
        parentName = constantPool.getItemAt(parentNameIdx).asString();

        interfaceIdxes = new int[inStream.readUnsignedByte()]; //TODO need to be fixed
        interfaceNames = new String[interfaceIdxes.length];
        for (int j = 0; j < interfaceIdxes.length; j++) {
            interfaceIdxes[j] = inStream.readUnsignedShort();
            interfaceNames[j] = constantPool.getItemAt(j).asString();
        }

        //remove when interface equals to itself
        List<Integer> ints = new ArrayList<>();
        List<String> strs = new ArrayList<>();
        for (int j = 0; j < interfaceIdxes.length; j++) {
            if (!interfaceNames[j].equals(clazzName)) {
                ints.add(interfaceIdxes[j]);
                strs.add(interfaceNames[j]);
            }
        }

        interfaceIdxes = ints.stream().mapToInt(i -> i).toArray();
        interfaceNames = strs.toArray(new String[0]);

        int annotationCount = inStream.readUnsignedByte();
        annotationMap = new HashMap<>(annotationCount);
        annotationIdxes = new ArrayList<>();
        for (int j = 0; j < annotationCount; j++) {
            int annotationIdx = inStream.readUnsignedShort();
            annotationIdxes.add(annotationIdx);
            ManweVmpConstantPool.VmpAnnotation vmpAnnotation = (ManweVmpConstantPool.VmpAnnotation) constantPool.getItemAt(annotationIdx).getValue();
//            System.out.println(vmpAnnotation.annotationName);
            annotationMap.put(vmpAnnotation.annotationName, vmpAnnotation);
        }

        int unknownIdx = inStream.readUnsignedShort();
        if (unknownIdx < 0xFFFF) {
            unknown = constantPool.getItemAt(unknownIdx).asString();
        } else {
            unknown = "VMP-UNKNOWN";
        }

        access_flag = inStream.readUnsignedShort();

        // read field
        fieldMap = readField(inStream);
        // 读method
        methodMap = readMethod(inStream, clazzNameIdx);

        // read class metadata
        if (version == 5)
            extraMap = inStream.readStrBytesMap();
        else
            extraMap = new HashMap<>();
    }

    private Map<String, ManweVmpField> readField(ManweVmpDataInputStream inStream) throws IOException {
        HashMap<String, ManweVmpField> ret = new HashMap<>();
        int fieldCount = inStream.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            ManweVmpField manweVmpField = new ManweVmpField(inStream, constantPool);
            ret.put(manweVmpField.fieldName, manweVmpField);
//            System.out.printf("VmpField [%d/%d] success, %s%n", i, fieldCount, vmpField);
        }
        return ret;
    }

    private Map<String, ManweVmpMethod> readMethod(ManweVmpDataInputStream inStream, int clazzNameIdx) throws IOException {
        HashMap<String, ManweVmpMethod> ret = new HashMap<>();
        int methodCount = inStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            ManweVmpMethod manweVmpMethod = new ManweVmpMethod(inStream, constantPool, version);
            manweVmpMethod.setClazzNameIdx(clazzNameIdx);
            ret.put(manweVmpMethod.methodName+ manweVmpMethod.description, manweVmpMethod);
            //System.out.printf("VmpMethod [%d/%d] success, %s%n", i, methodCount, vmpMethod);
        }
        if (ret.size() != methodCount) {
            throw new RuntimeException("wtf method duplicate??");
        }
        return ret;
    }

    @Override
    public String toString() {
        return "VmpClazz{" +
                "clazzName='" + clazzName + '\'' +
                ", parentName='" + parentName + '\'' +
                ", interfaceNames=" + Arrays.toString(interfaceNames) +
                ", fieldMap=" + fieldMap.size() +
                ", methodMap=" + methodMap.size() +
                '}';
    }

    private static final int ACC_PUBLIC = 0x0001;
    private static final int ACC_FINAL = 0x0010;
    private static final int ACC_SUPER = 0x0020;
    private static final int ACC_INTERFACE = 0x0200;
    private static final int ACC_ABSTRACT = 0x0400;
    private static final int ACC_SYNTHETIC = 0x1000;
    private static final int ACC_ANNOTATION = 0x2000;
    private static final int ACC_ENUM = 0x4000;

}
