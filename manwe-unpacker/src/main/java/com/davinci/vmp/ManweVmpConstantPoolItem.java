package com.davinci.vmp;

public class ManweVmpConstantPoolItem {
    public static final int BOOL = 2; //ok
    public static final int INTEGER = 3; //ok
    public static final int LONG = 4; //ok
    public static final int FLOAT = 5; //ok
    public static final int DOUBLE = 6; //ok
    public static final int CONSTANT_UTF8 = 7; //ok
    public static final int VMP_CLAZZ_REF = 8; //

    public static final int JVM_CLAZZ_INFO = 1008; //
    public static final int VMP_FIELD_REF = 9;
    public static final int VMP_METHOD_REF = 10;
    public static final int ANNOTATION = 11;
    public static final int ENUM_REF = 20;
    public static final int USHORT_ARRAY = 21;
    public static final int NAMEANDTYPE_REF = 12;// not used in VMP

    /**
     * The Field contains the type of class pool item.
     */
    protected final int type;

    /**
     * The Value contains content of the class pool item as Object, multi-value
     * items are packed as Integer
     */
    protected Object value;

    /**
     * The Link to the owning constant pool.
     */
    protected final ManweVmpConstantPool cpool;

    public ManweVmpConstantPoolItem(final ManweVmpConstantPool cp, final int type, final Object value) {
        this.cpool = cp;
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    public int asInt() {
        return (Integer) this.value;
    }

    public Integer asInteger() {
        return (Integer) this.value;
    }

    public Double asDouble() {
        return (Double) this.value;
    }

    public Float asFloat() {
        return (Float) this.value;
    }

    public Long asLong() {
        return (Long) this.value;
    }

    public String asString() {
        return (String) this.value;
    }

    private int extractHighUShort() {
        return this.asInt() >>> 16;
    }

    private int extractLowUShort() {
        return this.asInt() & 0xFFFF;
    }

    public String getClassName() {
        throw new RuntimeException("Not Implement");
    }

    public String getSignature() {
        throw new RuntimeException("Not Implement");
    }

    public String getName() {
        throw new RuntimeException("Not Implement");
    }

    public Object asObject() {
        return this.value;
    }

}

