/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.yangutils.datamodel.utils.builtindatatype;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Represents YANG decimal 64.
 */
public class YangDecimal64 implements Serializable {

    private static final long serialVersionUID = 8006201668L;

    private int fractionDigit;

    /**
     * Creates an instance of YANG decimal64.
     */
    public YangDecimal64() {
    }

    /**
     * Creates an instance of of YANG decimal64.
     *
     * @param fractionDigit fraction digit
     */
    public YangDecimal64(int fractionDigit) {
        setFractionDigit(fractionDigit);
    }

    /**
     * Returns fraction digit.
     *
     * @return the fractionDigit
     */
    public int getFractionDigit() {
        return fractionDigit;
    }

    /**
     * Sets fraction digit.
     *
     * @param fractionDigit fraction digits.
     */
    public void setFractionDigit(int fractionDigit) {
        this.fractionDigit = fractionDigit;
    }

    /**
     * Returns object of YANG decimal64.
     *
     * @param value fraction digit
     * @return YANG decimal64
     */
    public static YangDecimal64 of(int value) {
        return new YangDecimal64(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fractionDigit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof YangDecimal64) {
            YangDecimal64 other = (YangDecimal64) obj;
            return Objects.equals(fractionDigit, other.fractionDigit);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .omitNullValues()
                .add("fractionDigit", fractionDigit)
                .toString();
    }

    /**
     * Returns the object of YANG decimal64 fromString input String.
     *
     * @param valInString input String
     * @return Object of YANG decimal64
     */
    public static YangDecimal64 fromString(String valInString) {
        try {
            int tmpVal = Integer.parseInt(valInString);
            return of(tmpVal);
        } catch (Exception e) {
        }
        return null;
    }
}
