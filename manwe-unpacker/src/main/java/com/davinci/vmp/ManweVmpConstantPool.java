package com.davinci.vmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManweVmpConstantPool {

    public ManweVmpConstantPoolItem[] records;

    public ManweVmpConstantPool(final ManweVmpDataInputStream inStream) throws IOException {
        int index = 0;


        int itemsNumber = inStream.readUnsignedShort();
        this.records = new ManweVmpConstantPoolItem[itemsNumber];

        for (index = 0; index < itemsNumber; ) {
            final int recordType = inStream.readUnsignedByte();
            Object recordValue;
            switch (recordType) {
                case ManweVmpConstantPoolItem.BOOL: {
                    recordValue = inStream.readUnsignedByte();
                    break;
                }
                case ManweVmpConstantPoolItem.INTEGER: {
                    recordValue = inStream.readInt();
                    break;
                }
                case ManweVmpConstantPoolItem.LONG: {
                    recordValue = inStream.readLong();
                    break;
                }
                case ManweVmpConstantPoolItem.FLOAT: {
                    recordValue = inStream.readFloat();
                    break;
                }
                case ManweVmpConstantPoolItem.DOUBLE: {
                    recordValue = inStream.readDouble();
                    break;
                }
                case ManweVmpConstantPoolItem.CONSTANT_UTF8: {
                    recordValue = inStream.readUTF();
                    break;
                }
                case ManweVmpConstantPoolItem.VMP_CLAZZ_REF: {
                    recordValue = new ClazzDefine(inStream.readUnsignedShort(), this.records);
                    break;
                }
                case ManweVmpConstantPoolItem.VMP_FIELD_REF: {
                    /*
                    CONSTANT_Fieldref_info {
                        u1 tag;
                        u2 class_index;
                        u2 name_and_type_index;
                    }
                    */
                    // VMP里指向两个UTF8，与JVM差异较大
                    recordValue = new FieldDefine(inStream.readUnsignedShort(), inStream.readUnsignedShort(), this.records);
                    break;
                }
                case ManweVmpConstantPoolItem.VMP_METHOD_REF: {
                    /*
                    CONSTANT_Methodref_info {
                        u1 tag;
                        u2 class_index;
                        u2 name_and_type_index;
                    }
                    */
                    // VMP里指向3个UTF8，与JVM差异较大
                    recordValue = new MethodDefine(inStream.readUnsignedShort(), inStream.readUnsignedShort(),
                            inStream.readUnsignedShort(), this.records);
                    break;
                }
                case ManweVmpConstantPoolItem.ANNOTATION: {
                    /*
                    RuntimeVisibleAnnotations_attribute {
                        u2         attribute_name_index;
                        u4         attribute_length;
                        u2         num_annotations;
                        annotation annotations[num_annotations];
                    }
                    这个不该出现在ConstantPool里，对应 class 的 attributes
                    */
                    recordValue = new VmpAnnotation(inStream.readUnsignedShort(), inStream.readUnsignedShort(), this.records);
                    break;
                }
                case ManweVmpConstantPoolItem.ENUM_REF: {
                    // 这里指向的是Enum的 static field，对应 class的 element_value
                    // 这里是配合 RuntimeVisibleAnnotations 用的，其实意义不大
                    int clzNameIdx = inStream.readUnsignedShort();
                    int arrayLen = inStream.readUnsignedByte();
                    ArrayList<Integer> idxList = new ArrayList<>(arrayLen);
                    for (int i = 0; i < arrayLen; i++) {
                        idxList.add(inStream.readUnsignedShort());
                    }
                    recordValue = new EnumRef(clzNameIdx, idxList, records);
                    break;
                }
                case ManweVmpConstantPoolItem.USHORT_ARRAY: {
                    // 同上
                    int arrayLen = inStream.readUnsignedByte();
                    ArrayList<Integer> arrayList = new ArrayList<>(arrayLen);
                    for (int i = 0; i < arrayLen; i++) {
                        arrayList.add(inStream.readUnsignedShort());
                    }
                    recordValue = arrayList;
                    break;
                }
                default: {
                    throw new IOException("Unsupported constant pool item [" + recordType + ']');
                }
            }

            this.records[index++] = new ManweVmpConstantPoolItem(this, recordType, recordValue);
        }
    }

    public ManweVmpConstantPoolItem getItemAt(final int index) {
        return this.records[index];
    }


    public int size() {
        return this.records.length;
    }

    public static class FieldDefine {
        public final String clazzName;
        public final String fieldName;
        public int clazzIdx;
        public int fieldNameIdx;

        public int jvmClazzIdx = -1;
        public int jvmNameAndTypeIdx = -1;

        public FieldDefine(int clazzIdx, int fieldNameIdx, ManweVmpConstantPoolItem[] records) {
            this.clazzIdx = clazzIdx;
            this.clazzName = records[this.clazzIdx].asString();
            this.fieldNameIdx = fieldNameIdx;
            this.fieldName = records[this.fieldNameIdx].asString();
            // Example: FieldRef{clazzName='android/os/Build$VERSION', fieldName='SDK_INT'}
            // TODO: 标准的class这里需要field的数据类型，这里面很可能缺少
        }

        public void updateJvm(int jvmClazzIdx, int jvmNameAndTypeIdx) {
/*
CONSTANT_Fieldref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
*/
        this.jvmClazzIdx = jvmClazzIdx;
        this.jvmNameAndTypeIdx = jvmNameAndTypeIdx;
        }

        @Override
        public String toString() {
            return "FieldDefine{" +
                    "clazzName='" + clazzName + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    ", jvmClazzIdx=" + jvmClazzIdx +
                    ", jvmNameAndTypeIdx=" + jvmNameAndTypeIdx +
                    '}';
        }
    }

    public static class MethodDefine {
        public final String clazzName;
        public final String methodName;
        public final String methodDescription;
        public int clazzIdx;
        public int methodNameIdx;
        public int methodDescriptionIdx;

        public int jvmClazzIdx = -1;
        public int jvmNameAndTypeIdx = -1;

        public MethodDefine(int clazzIdx, int methodNameIdx, int methodDescriptionIdx, ManweVmpConstantPoolItem[] records) {
            this.clazzIdx = clazzIdx;
            this.clazzName = records[this.clazzIdx].asString();
            this.methodNameIdx = methodNameIdx;
            this.methodName = records[this.methodNameIdx].asString();
            this.methodDescriptionIdx = methodDescriptionIdx;
            this.methodDescription = records[this.methodDescriptionIdx].asString();
        }

        public void updateJvm(int jvmClazzIdx, int jvmNameAndTypeIdx) {
/*
CONSTANT_Fieldref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
*/
            this.jvmClazzIdx = jvmClazzIdx;
            this.jvmNameAndTypeIdx = jvmNameAndTypeIdx;
        }

        @Override
        public String toString() {
            return "MethodDefine{" +
                    "clazzName='" + clazzName + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", methodDescription='" + methodDescription + '\'' +
                    ", jvmClazzIdx=" + jvmClazzIdx +
                    ", jvmNameAndTypeIdx=" + jvmNameAndTypeIdx +
                    '}';
        }
    }

    public static class ClazzDefine {
        public final String clazzName;
        public int clazzNameIdx;

        public ClazzDefine(int clazzNameIdx, ManweVmpConstantPoolItem[] records) {
            this.clazzNameIdx = clazzNameIdx;
            this.clazzName = records[this.clazzNameIdx].asString();
        }

        @Override
        public String toString() {
            return "ClazzDefine{" +
                    "clazzName='" + clazzName + '\'' +
                    '}';
        }
    }

    public static class EnumRef {
        public final String clzName;

        private final int clzNameIdx;

        public ArrayList<String> enumList;
        private ArrayList<Integer> enumIdxList;

        public EnumRef(int clzNameIdx, ArrayList<Integer> enumIdxList, ManweVmpConstantPoolItem[] records) {
            this.clzNameIdx = clzNameIdx;
            this.clzName = records[this.clzNameIdx].asString();
            this.enumIdxList = enumIdxList;
            this.enumList = new ArrayList<>(this.enumIdxList.size());
            for (Integer integer : enumIdxList) {
                this.enumList.add(records[integer].asString());
            }
        }

        @Override
        public String toString() {
            return "EnumRef{" +
                    "clzName='" + clzName + '\'' +
                    ", enumList=" + enumList +
                    '}';
        }
    }

    public static class VmpAnnotation {
        public final String annotationName;
        public final int annotationNameIdx;
        public final int annotationValueIdx;
        public final Map<String, Object> kv = new HashMap<>();

        public VmpAnnotation(int annotationNameIdx, int AnnotationValueIdx, ManweVmpConstantPoolItem[] records) {
            this.annotationNameIdx = annotationNameIdx;
            this.annotationName = records[this.annotationNameIdx].asString();
            this.annotationValueIdx = AnnotationValueIdx;
            ArrayList<Integer> annotationValueList = (ArrayList<Integer>) records[this.annotationValueIdx].getValue();
            for (int i = 0; i < annotationValueList.size(); i += 2) {
                String key = records[annotationValueList.get(i)].asString();
                Object value = records[annotationValueList.get(i + 1)].getValue();
                kv.put(key, value);
            }
        }

        @Override
        public String toString() {
            return "VmpAnnotation{" +
                    "annotationName='" + annotationName + '\'' +
                    ", kv=" + kv +
                    '}';
        }
    }

    public static class NameAndTypeIndex {
        public final String name;
        public final int nameIdx;
        public final String type;
        public final int typeIdx;

        @Override
        public String toString() {
            return "NameAndTypeIndex{" +
                "name='" + name + '\'' +
                ", nameIdx=" + nameIdx +
                ", type='" + type + '\'' +
                ", typeIdx=" + typeIdx +
                '}';
        }

        //jvm style, need idx-1
        public NameAndTypeIndex(int nameIdx, int typeIdx, ManweVmpConstantPoolItem[] records) {
            this.name = records[nameIdx - 1].asString();
            this.nameIdx = nameIdx;
            this.type = records[typeIdx - 1].asString();
            this.typeIdx = typeIdx;
        }
    }

    public static class JvmClassInfo {
        /*
        CONSTANT_Class_info {
            u1 tag;
            u2 name_index;
        }
         */
        public int nameIdx;
        public String name;

        public JvmClassInfo(int nameIdx, String name) {
            this.nameIdx = nameIdx;
            this.name = name;
        }

        @Override
        public String toString() {
            return "JvmClassInfo{" +
                    "nameIdx=" + nameIdx +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
