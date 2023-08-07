/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.expression.function.scalar.multivalue;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;

import org.elasticsearch.search.aggregations.metrics.CompensatedSum;
import org.elasticsearch.xpack.esql.planner.LocalExecutionPlanner;
import org.elasticsearch.xpack.ql.expression.Expression;
import org.elasticsearch.xpack.ql.tree.Source;
import org.elasticsearch.xpack.ql.type.DataType;
import org.elasticsearch.xpack.ql.type.DataTypes;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.function.Supplier;

import static org.elasticsearch.xpack.ql.util.NumericUtils.asLongUnsigned;
import static org.elasticsearch.xpack.ql.util.NumericUtils.unsignedLongAsBigInteger;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class MvSumTests extends AbstractMultivalueFunctionTestCase {
    public MvSumTests(@Name("TestCase") Supplier<TestCase> testCaseSupplier) {
        this.testCase = testCaseSupplier.get();
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() {
        return parameterSuppliersFromTypedData(List.of(new TestCaseSupplier("mv_sum(<double>)", () -> {
            List<Double> mvData = randomList(1, 100, () -> randomDouble());
            return new TestCase(
                List.of(new TypedData(mvData, DataTypes.DOUBLE, "field")),
                "MvSum[field=Attribute[channel=0]]",
                DataTypes.DOUBLE,
                equalTo(mvData.stream().mapToDouble(Double::doubleValue).summaryStatistics().getSum())
            );
        })));
    }

    @Override
    protected Expression build(Source source, Expression field) {
        return new MvSum(source, field);
    }

    @Override
    protected DataType[] supportedTypes() {
        return representableNumerics();
    }

    @Override
    protected Matcher<Object> resultMatcherForInput(List<?> input, DataType dataType) {
        return switch (LocalExecutionPlanner.toElementType(dataType)) {
            case DOUBLE -> {
                CompensatedSum sum = new CompensatedSum();
                for (Object i : input) {
                    sum.add((Double) i);
                }
                yield equalTo(sum.value());
            }
            case INT -> equalTo(input.stream().mapToInt(o -> (Integer) o).sum());
            case LONG -> {
                if (dataType == DataTypes.UNSIGNED_LONG) {
                    long sum = asLongUnsigned(0);
                    for (Object i : input) {
                        sum = asLongUnsigned(unsignedLongAsBigInteger(sum).add(unsignedLongAsBigInteger((long) i)).longValue());
                        ;
                    }
                    yield equalTo(sum);
                }
                yield equalTo(input.stream().mapToLong(o -> (Long) o).sum());
            }
            case NULL -> nullValue();
            default -> throw new UnsupportedOperationException("unsupported type " + input);
        };
    }

}
