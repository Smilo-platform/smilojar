/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.infinitape.etherjar.abi;

import io.infinitape.etherjar.hex.Hex32;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Signed &amp; unsigned fixed-point number.
 */
public abstract class DecimalType implements SimpleType<BigDecimal> {

    private final int mBits;

    private final int nBits;

    private final BigDecimal fractionFactor;

    protected DecimalType(int mBits, int nBits) {
        if (mBits <= 0 || mBits % 8 != 0)
            throw new IllegalArgumentException("Invalid decimal type 'mBits' count: " + mBits);

        if (nBits <= 0 || nBits % 8 != 0)
            throw new IllegalArgumentException("Invalid decimal type 'nBits' count: " + nBits);

        if (mBits + nBits > 256)
            throw new IllegalArgumentException(
                    "Invalid decimal type total bits count: " + (nBits + mBits));

        this.mBits = mBits;
        this.nBits = nBits;

        this.fractionFactor = new BigDecimal(NumericType.powerOfTwo(nBits));
    }

    /**
     * @return number of N bits
     */
    public int getNBits() {
        return nBits;
    }

    /**
     * @return number of M bits
     */
    public int getMBits() {
        return mBits;
    }

    /**
     * @return number of total bits
     */
    public int getBits() {
        return nBits + mBits;
    }

    /**
     * @return {@code true} if this {@link Type} is signed, otherwise {@code false}
     */
    public boolean isSigned() {
        return getNumericType().isSigned();
    }

    /**
     * Is a {@code value} is in a valid {@link Type} range.
     *
     * @param value a decimal value
     * @return {@code true} if {@code value} is valid, otherwise {@code false}
     */
    public boolean isValueValid(BigDecimal value) {
        return value.compareTo(getMinValue()) >= 0 && value.compareTo(getMaxValue()) < 0;
    }

    /**
     * @return a minimal value (inclusive)
     */
    public abstract BigDecimal getMinValue();

    /**
     * @return a maximum value (exclusive)
     */
    public abstract BigDecimal getMaxValue();

    /**
     * @return an underlying numeric type
     */
    public abstract NumericType getNumericType();

    public Hex32 encode(double value) {
        return encodeSimple(BigDecimal.valueOf(value));
    }

    @Override
    public Hex32 encodeSimple(BigDecimal value) {
        if (!isValueValid(value))
            throw new IllegalArgumentException("Decimal value out of range: " + value);

        BigInteger integer = value.multiply(fractionFactor)
                .setScale(0, RoundingMode.HALF_UP).toBigInteger();

        if (integer.compareTo(getNumericType().getMaxValue()) == 0) {
            integer = integer.subtract(BigInteger.ONE);
        }

        return getNumericType().encodeSimple(integer);
    }

    @Override
    public BigDecimal decodeSimple(Hex32 hex32) {
        BigInteger integer = getNumericType().decodeSimple(hex32);

        //noinspection BigDecimalMethodWithoutRoundingCalled
        return new BigDecimal(integer).divide(fractionFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), nBits, mBits);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        DecimalType other = (DecimalType) obj;

        return nBits == other.nBits
                && mBits == other.mBits;
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
