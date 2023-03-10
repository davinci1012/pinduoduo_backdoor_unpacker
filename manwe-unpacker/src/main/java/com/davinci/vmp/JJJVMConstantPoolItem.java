/* 
 * Copyright 2015 Igor Maznitsa (http://www.igormaznitsa.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.davinci.vmp;


/**
 * Describing structure and contains data of a constant pool item. Data for
 * multi-field items presented as packed integer object
 */
public class JJJVMConstantPoolItem {

  /**
   * Constant pool UTF8 string item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.7}
   */
  public static final int CONSTANT_UTF8 = 1;
  
  /**
   * Constant pool UNICODE string item.
   */
  public static final int CONSTANT_UNICODE = 2;
  
  /**
   * Constant pool INTEGER item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.4}
   */
  public static final int CONSTANT_INTEGER = 3;
  
  /**
   * Constant pool FLOAT item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.4}
   */
  public static final int CONSTANT_FLOAT = 4;
  
  /**
   * Constant pool LONG item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5}
   */
  public static final int CONSTANT_LONG = 5;
  
  /**
   * Constant pool DOUBLE item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5}
   */
  public static final int CONSTANT_DOUBLE = 6;
  
  /**
   * Constant pool Class Reference item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.1}
   */
  public static final int CONSTANT_CLASSREF = 7;
  
  /**
   * Constant pool String Reference item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.3}
   */
  public static final int CONSTANT_STRING = 8;
  
  /**
   * Constant pool Field Reference item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.2}
   */
  public static final int CONSTANT_FIELDREF = 9;
  
  /**
   * Constant pool Method Reference item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.2}
   */
  public static final int CONSTANT_METHODREF = 10;
  
  /**
   * Constant pool INTERFACE METHOD instance.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.2}
   */
  public static final int CONSTANT_INTERFACEMETHOD = 11;
  
  /**
   * Constant pool NAME+TYPE Reference item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.6}
   */
  public static final int CONSTANT_NAMETYPEREF = 12;
  
  /**
   * Constant pool Method Handle item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.8}
   */
  public static final int CONSTANT_METHODHANDLE = 15;
  
  /**
   * Constant pool Method Type item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.9}
   */
  public static final int CONSTANT_METHODTYPE = 16;
  
  /**
   * Constant pool Invoke dynamic item.
   * {@link https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.10}
   */
  public static final int CONSTANT_INVOKEDYNAMIC = 18;
}
