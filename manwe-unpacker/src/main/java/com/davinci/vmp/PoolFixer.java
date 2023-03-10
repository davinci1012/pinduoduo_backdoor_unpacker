package com.davinci.vmp;

import java.util.*;

public class PoolFixer {
    /**
     * 从vmp的FieldDefine修复所有应指向Class_info和NameAndType_info的数据
     * 从vmp的MethodDefine修复所有应指向Class_info和NameAndType_info的数据
     * 从vmp的UTF8猜测并创建Class_info的数据
     * 从vmp的VmpClazz修复所有应指向Class_info的数据
     */
    static final List<ManweVmpConstantPoolItem> tailAppendList = new LinkedList<>();
    @Deprecated
    // 由于pool可能被改写，该字段无意义
    private static ManweVmpConstantPool pool;
    public static int originalZeroItemIndex = -1;
    public static Map<String, Integer> clazzCache = new HashMap<>();
    public static Map<String, Integer> fieldTypeCache = new HashMap<>();

    public static void init(ManweVmpConstantPool _pool) {
        pool = _pool;
    }

    public static ManweVmpConstantPool getPool() {
        return pool;
    }

    @Deprecated
    public static int fix(int vmpIndex) {
        //if we have patched constant pool, no need to fix here
        return vmpIndex;
    }

    public static int findFixedStringIdx(String s) {
        for (int i = 0; i < pool.size(); i++) {
            if (pool.records[i].value.equals(s))
                return i + 1;
        }
        throw new RuntimeException("Cannot find string");
    }

    public static ManweVmpConstantPoolItem[] moveZeroToLast(ManweVmpConstantPool constantPool, ManweVmpClazz[] manweVmpClazzes) {
        // Step1, move item 0 to tail
        ManweVmpConstantPoolItem[] patchedPool = new ManweVmpConstantPoolItem[constantPool.records.length];

        //fix: we may need a deep copy
        System.arraycopy(constantPool.records, 1, patchedPool, 0, constantPool.records.length - 1);
        //item 0 is a utf-8 ref
        patchedPool[patchedPool.length - 1] = constantPool.records[0];

        // Step2, fix all reference to the original 0
        int totcnt = 0;
        originalZeroItemIndex = patchedPool.length;
        for (ManweVmpConstantPoolItem manweVmpConstantPoolItem : patchedPool) {
            if (fixItemOffset(manweVmpConstantPoolItem, 0, originalZeroItemIndex)) {
                totcnt++;
            }
        }
        System.out.println(String.format("修复了%d处指向0的数据", totcnt));
        // Step3, patch all VmpClass
        Arrays.stream(manweVmpClazzes)
                .filter(x -> x.clazzNameIdx == 0)
                .forEach(x -> x.clazzNameIdx = originalZeroItemIndex);
        Arrays.stream(manweVmpClazzes)
                .filter(x -> x.parentNameIdx == 0)
                .forEach(x -> x.parentNameIdx = originalZeroItemIndex);
        Arrays.stream(manweVmpClazzes)
                .filter(x -> x.interfaceIdxes.length > 0)
                .forEach(x -> {
                    for (int i = 0; i < x.interfaceIdxes.length; i++) {
                        if (x.interfaceIdxes[i] == 0)
                            x.interfaceIdxes[i] = originalZeroItemIndex;
                    }
                });
        return patchedPool;
    }

    public static boolean fixItemOffset(ManweVmpConstantPoolItem item, int originalIdx, int targetIdx) {
        switch (item.type) {
            case ManweVmpConstantPoolItem.VMP_CLAZZ_REF:
                ManweVmpConstantPool.ClazzDefine ref = (ManweVmpConstantPool.ClazzDefine) item.value;
                if (ref.clazzNameIdx == originalIdx) {
                    ref.clazzNameIdx = targetIdx;
                    return true;
                }
                break;
            case ManweVmpConstantPoolItem.VMP_FIELD_REF:
                ManweVmpConstantPool.FieldDefine fieldDefine = (ManweVmpConstantPool.FieldDefine) item.value;
                if (fieldDefine.fieldNameIdx == 0)
                    throw new UnreachableException("0位置是一个UTF8的类名，不应运行到这里");
                if (fieldDefine.clazzIdx == originalIdx) {
                    fieldDefine.clazzIdx = targetIdx;
                    //this.records[itemIdx].value = new FieldRef(targetIdx, fieldRef.fieldIdx, this.records);
                    return true;
                } else if (fieldDefine.fieldNameIdx == originalIdx) {
                    throw new UnreachableException("0位置是一个UTF8的类名，不应运行到这里");
                }
                break;
            case ManweVmpConstantPoolItem.VMP_METHOD_REF:
                ManweVmpConstantPool.MethodDefine methodDefine = (ManweVmpConstantPool.MethodDefine) item.value;
                if (methodDefine.methodNameIdx == 0 || methodDefine.methodDescriptionIdx == 0) {
                    throw new UnreachableException("0位置是一个UTF8的类名，不应运行到这里");
                }
                if (methodDefine.clazzIdx == originalIdx) {
                    methodDefine.clazzIdx = targetIdx;
                    return true;
                } else if (methodDefine.methodDescriptionIdx == originalIdx) {
                    throw new UnreachableException("0位置是一个UTF8的类名，不应运行到这里");
                } else if (methodDefine.methodNameIdx == originalIdx) {
                    throw new UnreachableException("0位置是一个UTF8的类名，不应运行到这里");
                }
                break;
            case ManweVmpConstantPoolItem.ANNOTATION:
                break;
            default:
                break;
        }
        return false;
    }

    public static void patchForFieldAndMethod(ManweVmpConstantPoolItem[] records) {
        Arrays.stream(records).filter(x -> x.type == ManweVmpConstantPoolItem.VMP_FIELD_REF)
                .map(ManweVmpConstantPoolItem::getValue)
                .map(x -> (ManweVmpConstantPool.FieldDefine) x)
                .forEach(fieldDefine -> {
                    // 尝试在CP里创建Const_ClassInfo
                    int jvmClazzIdx = createOrGetJvmClazzIdx(fieldDefine.clazzIdx, fieldDefine.clazzName,
                            records.length);
                    // 根据所属类和字段名，推测它的实际数据类型
                    String description = RuntimeTypeFixer.resolve(fieldDefine.clazzName, fieldDefine.fieldName);
                    // 如果ustring里没有它的数据类型，就创建对应的ustring
                    int vmpUstringIdx = createOrGetVmpUstringIdx(description, records);
                    System.out.println("vmpUstringIdx=" + vmpUstringIdx + " ustring=" + description);
                    // 创建JVM格式的Field
                    int jvmNameAndTypeIdx = getJvmNameAndTypeIdx(fieldDefine.fieldNameIdx,
                            vmpUstringIdx, records);
                    fieldDefine.updateJvm(jvmClazzIdx, jvmNameAndTypeIdx);
                });

        // 从vmp的MethodDefine创建Class_info和NameAndType_info
        Arrays.stream(records).filter(x -> x.type == ManweVmpConstantPoolItem.VMP_METHOD_REF)
                .map(ManweVmpConstantPoolItem::getValue)
                .map(x -> (ManweVmpConstantPool.MethodDefine) x)
                .forEach(methodDefine -> {
                    int jvmClazzIdx = createOrGetJvmClazzIdx(methodDefine.clazzIdx, methodDefine.clazzName,
                            records.length);
                    int jvmNameAndTypeIdx = getJvmNameAndTypeIdx(methodDefine,
                            records);
                    methodDefine.updateJvm(jvmClazzIdx, jvmNameAndTypeIdx);
                });
    }

    private static int createOrGetVmpUstringIdx(String description, ManweVmpConstantPoolItem[] records) {
        int ret;
        // cache first
        ret = fieldTypeCache.getOrDefault(description, -1);
        if (ret != -1) {
            return ret;
        }
        // 执行查找
        for (int i = 0; i < records.length; i++) {
            if (records[i].type == ManweVmpConstantPoolItem.CONSTANT_UTF8
                    && records[i].asString().equals(description)) {
                fieldTypeCache.put(description, i);
                ret = i;
            }
        }
        if (ret != -1) {
            // +1 for jvm
            return ret;
        }
        // create if not found
        ManweVmpConstantPoolItem newItem = new ManweVmpConstantPoolItem(null,
                ManweVmpConstantPoolItem.CONSTANT_UTF8, description);
        ret = records.length + tailAppendList.size() + 1;
        tailAppendList.add(newItem);
        // 实际上这里代码从来没执行过，不知道对不对
        throw new UnreachableException("创建UTF8: " + description);
    }

    public static int getJvmNameAndTypeIdx(ManweVmpConstantPool.MethodDefine methodDefine, ManweVmpConstantPoolItem[] records) {
        ManweVmpConstantPoolItem newItem = new ManweVmpConstantPoolItem(null, ManweVmpConstantPoolItem.NAMEANDTYPE_REF,
                new ManweVmpConstantPool.NameAndTypeIndex(methodDefine.methodNameIdx,
                        methodDefine.methodDescriptionIdx,
                        records));

        // 指向新创建的NameAndType结构体，在jvm里，值是它的下标加一
        int jvmNameAndTypeIdx = records.length + tailAppendList.size() + 1;
        tailAppendList.add(newItem);
        return jvmNameAndTypeIdx;
    }

    public static int getJvmNameAndTypeIdx(int fieldNameIdx, int fieldTypePlaceHolder, ManweVmpConstantPoolItem[] records) {
        ManweVmpConstantPoolItem newItem = new ManweVmpConstantPoolItem(null, ManweVmpConstantPoolItem.NAMEANDTYPE_REF,
                new ManweVmpConstantPool.NameAndTypeIndex(fieldNameIdx,
                        fieldTypePlaceHolder + 1,
                        records));
        // 指向新创建的NameAndType结构体，在jvm里，值是它的下标加一
        int jvmNameAndTypeIdx = records.length + tailAppendList.size() + 1;
        tailAppendList.add(newItem);
        return jvmNameAndTypeIdx;
    }

    public static int getJvmClazzIdx(String clazzName) {
        // 如果已经创建过ClassInfo（例如多个field和method是同一个class下的），就直接查表
        int jvmClazzIdx = clazzCache.getOrDefault(clazzName, -1);
        if (jvmClazzIdx != -1) {
            return jvmClazzIdx;
        }
        throw new RuntimeException("无法通过 classname 找到对应的 jvmClazzIdx: " + clazzName);
    }

    public static int createOrGetJvmClazzIdx(int clazzIdx, String clazzName, int recordsLen) {
        int jvmClazzIdx;

        // 如果已经创建过 ClassInfo（例如多个field和method是同一个class下的），就直接查表
        jvmClazzIdx = clazzCache.getOrDefault(clazzName, -1);
        if (jvmClazzIdx != -1) {
            return jvmClazzIdx;
        }

        // 没找到，指向新创建的ClassInfo结构体，在jvm里，值是它的下标加一
        //temp -1 because we are now in JVM-style constpool, but classref constructor uses Idx to access array directly
        ManweVmpConstantPoolItem newItem =
                new ManweVmpConstantPoolItem(null, ManweVmpConstantPoolItem.JVM_CLAZZ_INFO,
                        new ManweVmpConstantPool.JvmClassInfo(clazzIdx, clazzName));
        jvmClazzIdx = recordsLen + tailAppendList.size() + 1;
        tailAppendList.add(newItem);
        clazzCache.put(clazzName, jvmClazzIdx);
        return jvmClazzIdx;
    }

    public static void patchConstantPoolForClassMeta(ManweVmpClazz clazz, int length) {
        //patch constant pool for clazz's meta

        //Step 1: add CLASS_REF for this clazz
        clazz.jvmClazzNameIdx = createOrGetJvmClazzIdx(clazz.clazzNameIdx, clazz.clazzName,
                length);

        //Step 2: add ClASS_REF for parent clazz
        clazz.jvmParentNameIdx = createOrGetJvmClazzIdx(clazz.parentNameIdx, clazz.parentName,
                length);

        //Step 3: fix interfaces
        clazz.jvmInterfaceIdxes = new int[clazz.interfaceIdxes.length];
        for (int i = 0; i < clazz.interfaceIdxes.length; i++) {
            clazz.jvmInterfaceIdxes[i] = createOrGetJvmClazzIdx(clazz.interfaceIdxes[i],
                    clazz.interfaceNames[i], length);
        }
    }

    public static ManweVmpConstantPoolItem[] doFinal(ManweVmpConstantPoolItem[] records) {
        ManweVmpConstantPoolItem[] finalArray = new ManweVmpConstantPoolItem[records.length + tailAppendList.size()];
        System.arraycopy(records, 0, finalArray, 0, records.length);
        System.arraycopy(tailAppendList.toArray(), 0, finalArray, records.length, tailAppendList.size());
        tailAppendList.clear();
        return finalArray;
    }

    public static void analyzeUtf8ToClazz(ManweVmpConstantPoolItem[] records) {
        // 有些 opcode 例如 new、checkcast会使用到常量池里的数据类型，vmp中以utf8存放
        // 需要修改为ClassInfo
        for (int i = 0; i < records.length; i++) {
            ManweVmpConstantPoolItem record = records[i];
            if (record.type == ManweVmpConstantPoolItem.CONSTANT_UTF8) {
                String data = record.asString();
                if (data.equals("Ljava/lang/String;") || (!data.startsWith("L") && !data.startsWith("(")
                        && !data.startsWith("/")
                        && !data.contains(":") && !data.contains("%")
                        && (data.split("/").length >= 3 || data.startsWith("[")))) {
                    //System.out.println("NEW:"+data);
                    createOrGetJvmClazzIdx(i + 1, data, records.length);
                }
            }
        }
    }
}
