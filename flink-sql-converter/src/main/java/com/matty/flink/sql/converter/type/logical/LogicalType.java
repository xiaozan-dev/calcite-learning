package com.matty.flink.sql.converter.type.logical;

import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.*;

/**
 * Description:
 *
 * @author mwt
 * @version 1.0
 * @date 2019-10-09
 */
public abstract class LogicalType implements Serializable {

    private final boolean isNullable;

    private final LogicalTypeRoot typeRoot;

    public LogicalType(boolean isNullable, LogicalTypeRoot typeRoot) {
        this.isNullable = isNullable;
        this.typeRoot = Preconditions.checkNotNull(typeRoot);
    }

    /**
     * Returns whether a value of this type can be {@code null}.
     */
    public boolean isNullable() {
        return isNullable;
    }

    /**
     * Returns the root of this type. It is an essential description without additional parameters.
     */
    public LogicalTypeRoot getTypeRoot() {
        return typeRoot;
    }

    /**
     * Returns a deep copy of this type with possibly different nullability.
     *
     * @param isNullable the intended nullability of the copied type
     * @return a deep copy
     */
    public abstract LogicalType copy(boolean isNullable);

    /**
     * Returns a deep copy of this type. It requires an implementation of {@link #copy(boolean)}.
     *
     * @return a deep copy
     */
    public final LogicalType copy() {
        return copy(isNullable);
    }

    /**
     * Returns a string that fully serializes this instance. The serialized string can be used for
     * transmitting or persisting a type.
     *
     * <p>See {@link LogicalTypeParser} for the reverse operation.
     *
     * @return detailed string for transmission or persistence
     */
    public abstract String asSerializableString();

    /**
     * Returns a string that summarizes this type for printing to a console. An implementation might
     * shorten long names or skips very specific properties.
     *
     * <p>Use {@link #asSerializableString()} for a type string that fully serializes
     * this instance.
     *
     * @return summary string of this type for debugging purposes
     */
    public String asSummaryString() {
        return asSerializableString();
    }

    /**
     * Returns whether an instance of the given class can be represented as a value of this logical
     * type when entering the table ecosystem. This method helps for the interoperability between
     * JVM-based languages and the relational type system.
     *
     * <p>A supported conversion directly maps an input class to a logical type without loss of
     * precision or type widening.
     *
     * <p>For example, {@code java.lang.Long} or {@code long} can be used as input for {@code BIGINT}
     * independent of the set nullability.
     *
     * @param clazz input class to be converted into this logical type
     * @return flag that indicates if instances of this class can be used as input into the table
     * ecosystem
     * @see #getDefaultConversion()
     */
    public abstract boolean supportsInputConversion(Class<?> clazz);

    /**
     * Returns whether a value of this logical type can be represented as an instance of the given
     * class when leaving the table ecosystem. This method helps for the interoperability between
     * JVM-based languages and the relational type system.
     *
     * <p>A supported conversion directly maps a logical type to an output class without loss of
     * precision or type widening.
     *
     * <p>For example, {@code java.lang.Long} or {@code long} can be used as output for {@code BIGINT}
     * if the type is not nullable. If the type is nullable, only {@code java.lang.Long} can represent
     * this.
     *
     * @param clazz output class to be converted from this logical type
     * @return flag that indicates if instances of this class can be used as output from the table
     * ecosystem
     * @see #getDefaultConversion()
     */
    public abstract boolean supportsOutputConversion(Class<?> clazz);

    /**
     * Returns the default conversion class. A value of this logical type is expected to be an instance
     * of the given class when entering or is represented as an instance of the given class when
     * leaving the table ecosystem if no other conversion has been specified.
     *
     * <p>For example, {@code java.lang.Long} is the default input and output for {@code BIGINT}.
     *
     * @return default class to represent values of this logical type
     * @see #supportsInputConversion(Class)
     * @see #supportsOutputConversion(Class)
     */
    public abstract Class<?> getDefaultConversion();

    public abstract List<LogicalType> getChildren();

    public abstract <R> R accept(LogicalTypeVisitor<R> visitor);

    @Override
    public String toString() {
        return asSummaryString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogicalType that = (LogicalType) o;
        return isNullable == that.isNullable && typeRoot == that.typeRoot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isNullable, typeRoot);
    }

    // --------------------------------------------------------------------------------------------

    protected String withNullability(String format, Object... params) {
        if (!isNullable) {
            return String.format(format + " NOT NULL", params);
        }
        return String.format(format, params);
    }

    protected static Set<String> conversionSet(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
}
