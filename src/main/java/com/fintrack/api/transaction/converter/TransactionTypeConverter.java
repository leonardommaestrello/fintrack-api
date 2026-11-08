
package com.fintrack.api.transaction.converter;

import com.fintrack.api.transaction.enums.TransactionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionTypeConverter
        implements AttributeConverter<TransactionType, String> {

    @Override
    public String convertToDatabaseColumn(TransactionType type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case INCOME -> "RECEITA";
            case EXPENSE -> "DESPESA";
        };
    }

    @Override
    public TransactionType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case "RECEITA" -> TransactionType.INCOME;
            case "DESPESA" -> TransactionType.EXPENSE;
            default -> throw new IllegalArgumentException(
                    "Unknown transaction type: " + value
            );
        };
    }
}
