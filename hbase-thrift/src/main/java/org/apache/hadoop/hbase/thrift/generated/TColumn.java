/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.hadoop.hbase.thrift.generated;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
/**
 * Holds column name and the cell.
 */
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2020-04-17")
public class TColumn implements org.apache.thrift.TBase<TColumn, TColumn._Fields>, java.io.Serializable, Cloneable, Comparable<TColumn> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TColumn");

  private static final org.apache.thrift.protocol.TField COLUMN_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("columnName", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField CELL_FIELD_DESC = new org.apache.thrift.protocol.TField("cell", org.apache.thrift.protocol.TType.STRUCT, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TColumnStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TColumnTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.nio.ByteBuffer columnName; // required
  public @org.apache.thrift.annotation.Nullable TCell cell; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    COLUMN_NAME((short)1, "columnName"),
    CELL((short)2, "cell");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // COLUMN_NAME
          return COLUMN_NAME;
        case 2: // CELL
          return CELL;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.COLUMN_NAME, new org.apache.thrift.meta_data.FieldMetaData("columnName", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Text")));
    tmpMap.put(_Fields.CELL, new org.apache.thrift.meta_data.FieldMetaData("cell", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TCell.class)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TColumn.class, metaDataMap);
  }

  public TColumn() {
  }

  public TColumn(
    java.nio.ByteBuffer columnName,
    TCell cell)
  {
    this();
    this.columnName = org.apache.thrift.TBaseHelper.copyBinary(columnName);
    this.cell = cell;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TColumn(TColumn other) {
    if (other.isSetColumnName()) {
      this.columnName = org.apache.thrift.TBaseHelper.copyBinary(other.columnName);
    }
    if (other.isSetCell()) {
      this.cell = new TCell(other.cell);
    }
  }

  public TColumn deepCopy() {
    return new TColumn(this);
  }

  @Override
  public void clear() {
    this.columnName = null;
    this.cell = null;
  }

  public byte[] getColumnName() {
    setColumnName(org.apache.thrift.TBaseHelper.rightSize(columnName));
    return columnName == null ? null : columnName.array();
  }

  public java.nio.ByteBuffer bufferForColumnName() {
    return org.apache.thrift.TBaseHelper.copyBinary(columnName);
  }

  public TColumn setColumnName(byte[] columnName) {
    this.columnName = columnName == null ? (java.nio.ByteBuffer)null   : java.nio.ByteBuffer.wrap(columnName.clone());
    return this;
  }

  public TColumn setColumnName(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer columnName) {
    this.columnName = org.apache.thrift.TBaseHelper.copyBinary(columnName);
    return this;
  }

  public void unsetColumnName() {
    this.columnName = null;
  }

  /** Returns true if field columnName is set (has been assigned a value) and false otherwise */
  public boolean isSetColumnName() {
    return this.columnName != null;
  }

  public void setColumnNameIsSet(boolean value) {
    if (!value) {
      this.columnName = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public TCell getCell() {
    return this.cell;
  }

  public TColumn setCell(@org.apache.thrift.annotation.Nullable TCell cell) {
    this.cell = cell;
    return this;
  }

  public void unsetCell() {
    this.cell = null;
  }

  /** Returns true if field cell is set (has been assigned a value) and false otherwise */
  public boolean isSetCell() {
    return this.cell != null;
  }

  public void setCellIsSet(boolean value) {
    if (!value) {
      this.cell = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case COLUMN_NAME:
      if (value == null) {
        unsetColumnName();
      } else {
        if (value instanceof byte[]) {
          setColumnName((byte[])value);
        } else {
          setColumnName((java.nio.ByteBuffer)value);
        }
      }
      break;

    case CELL:
      if (value == null) {
        unsetCell();
      } else {
        setCell((TCell)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case COLUMN_NAME:
      return getColumnName();

    case CELL:
      return getCell();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case COLUMN_NAME:
      return isSetColumnName();
    case CELL:
      return isSetCell();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof TColumn)
      return this.equals((TColumn)that);
    return false;
  }

  public boolean equals(TColumn that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_columnName = true && this.isSetColumnName();
    boolean that_present_columnName = true && that.isSetColumnName();
    if (this_present_columnName || that_present_columnName) {
      if (!(this_present_columnName && that_present_columnName))
        return false;
      if (!this.columnName.equals(that.columnName))
        return false;
    }

    boolean this_present_cell = true && this.isSetCell();
    boolean that_present_cell = true && that.isSetCell();
    if (this_present_cell || that_present_cell) {
      if (!(this_present_cell && that_present_cell))
        return false;
      if (!this.cell.equals(that.cell))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetColumnName()) ? 131071 : 524287);
    if (isSetColumnName())
      hashCode = hashCode * 8191 + columnName.hashCode();

    hashCode = hashCode * 8191 + ((isSetCell()) ? 131071 : 524287);
    if (isSetCell())
      hashCode = hashCode * 8191 + cell.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(TColumn other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetColumnName()).compareTo(other.isSetColumnName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumnName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columnName, other.columnName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetCell()).compareTo(other.isSetCell());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCell()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cell, other.cell);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("TColumn(");
    boolean first = true;

    sb.append("columnName:");
    if (this.columnName == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.columnName, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("cell:");
    if (this.cell == null) {
      sb.append("null");
    } else {
      sb.append(this.cell);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (cell != null) {
      cell.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TColumnStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TColumnStandardScheme getScheme() {
      return new TColumnStandardScheme();
    }
  }

  private static class TColumnStandardScheme extends org.apache.thrift.scheme.StandardScheme<TColumn> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TColumn struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // COLUMN_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.columnName = iprot.readBinary();
              struct.setColumnNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CELL
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.cell = new TCell();
              struct.cell.read(iprot);
              struct.setCellIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TColumn struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.columnName != null) {
        oprot.writeFieldBegin(COLUMN_NAME_FIELD_DESC);
        oprot.writeBinary(struct.columnName);
        oprot.writeFieldEnd();
      }
      if (struct.cell != null) {
        oprot.writeFieldBegin(CELL_FIELD_DESC);
        struct.cell.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TColumnTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TColumnTupleScheme getScheme() {
      return new TColumnTupleScheme();
    }
  }

  private static class TColumnTupleScheme extends org.apache.thrift.scheme.TupleScheme<TColumn> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TColumn struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetColumnName()) {
        optionals.set(0);
      }
      if (struct.isSetCell()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetColumnName()) {
        oprot.writeBinary(struct.columnName);
      }
      if (struct.isSetCell()) {
        struct.cell.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TColumn struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.columnName = iprot.readBinary();
        struct.setColumnNameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.cell = new TCell();
        struct.cell.read(iprot);
        struct.setCellIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

