package com.davinci.vmp;

import java.util.LinkedHashMap;
import java.util.Map;

public class ManweVmpInstruction {
    int opcode;
    int tableType;
    int jvmOffset;

    public boolean isWide = false;
    private int operand1 = -1;
    private int operand2 = -1;

    /**
     * 给switch语句专门开一个字段记录
     */
    Map<Integer, Integer> lookupSwitch;

    static class Table {
        int min;
        int max;
        int[] labels;
        Integer i;

    }

    Table tableSwitch;

    public ManweVmpInstruction(int opcode) {
        this.opcode = opcode;
        this.tableType = ManweCode.firstTable[opcode];
    }

    public ManweVmpInstruction(int opcode, int operand1) {
        this.opcode = opcode;
        this.operand1 = operand1;
        this.tableType = ManweCode.firstTable[opcode];
    }

    public ManweVmpInstruction(int opcode, int operand1, int operand2) {
        this.opcode = opcode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.tableType = ManweCode.firstTable[opcode];
    }

    public void setJvmOffset(int jvmOffset) {
        this.jvmOffset = jvmOffset;
    }


    /**
     * 给switch专门开个字段记录
     *
     * @param choice
     * @param target
     */
    public void updateSwitchMap(int choice, int target) {
        if (this.lookupSwitch == null)
            this.lookupSwitch = new LinkedHashMap<>();
        this.lookupSwitch.put(choice, target);
    }

    public void setOperand1(int operand1) {
        this.operand1 = operand1;
    }

    public void setOperand2(int operand2) {
        this.operand2 = operand2;
    }

    public int getOperand1() {
        if (operand1 == -1) {
            throw new RuntimeException("operand1 fuck");
        }
        return operand1;
    }

    public int getOperand2() {
        if (operand2 == -1) {
            throw new RuntimeException("operand2 fuck");
        }
        return operand2;
    }

    public void setWide(boolean wide) {
        isWide = wide;
    }

    @Override
    public String toString() {
        return "VmpInstruction{" +
                "opcode=" + opcode +
                ", opcode=" + Integer.toHexString(opcode) +
                ", tableType=" + tableType +
                ", jvmOffset=" + jvmOffset +
                ", isWide=" + isWide +
                ", operand1=" + operand1 +
                ", operand2=" + operand2 +
                '}';
    }
}
