package com.davinci.vmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManweVmpField {
    public final ManweVmpConstantPool constantPool;
    public final int nameIdx;
    public final String fieldName;
    public final int fieldTypeIdx;
    public final String fieldType;
    public final int access_flag;

    public final Map<String, ManweVmpConstantPool.VmpAnnotation> annotationMap;
    public final List<Integer> annotationIdxes;
    public final String unknown;

    public ManweVmpField(ManweVmpDataInputStream inStream, ManweVmpConstantPool _constantPool) throws IOException {
/*
        field_info {
            u2             access_flags;
            u2             name_index; -> utf8
            u2             descriptor_index; -> utf8
            u2             attributes_count;
            attribute_info attributes[attributes_count];
        }
*/
        constantPool = _constantPool;
        nameIdx = inStream.readUnsignedShort();
        fieldName = constantPool.getItemAt(nameIdx).asString();
        fieldTypeIdx = inStream.readUnsignedShort();
        fieldType = constantPool.getItemAt(fieldTypeIdx).asString();
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

        int unknownIdx = inStream.readUnsignedShort();
        if (unknownIdx < 0xFFFF) {
            unknown = constantPool.getItemAt(unknownIdx).asString();
        } else {
            unknown = "VMP-UNKNOWN";
        }
    }

    @Override
    public String toString() {
        return "VmpField{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                '}';
    }
}
