
package com.fintrack.api.transaction.export;

import com.fintrack.api.transaction.dto.TransactionResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class TransactionCsvExporter {

    private static final String HEADER =
            "Date,Description,Type,Category,Amount\r\n";

    public byte[] export(List<TransactionResponse> transactions) {

        StringBuilder csv = new StringBuilder(HEADER);

        for (TransactionResponse transaction : transactions) {

            csv.append(escape(transaction.transactionDate()))
                    .append(",");

            csv.append(escape(transaction.description()))
                    .append(",");

            csv.append(escape(transaction.type()))
                    .append(",");

            csv.append(escape(transaction.categoryName()))
                    .append(",");

            csv.append(escape(transaction.amount()))
                    .append("\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(Object value) {

        if (value == null) {
            return "";
        }

        String text = value.toString();

        // Prevent spreadsheet formula injection.
        // Also protect values with leading whitespace.
        String trimmed = text.stripLeading();

        if (!trimmed.isEmpty()
                && "=+-@\t\r\n".indexOf(trimmed.charAt(0)) >= 0) {
            text = "'" + text;
        }

        // Escape CSV double quotes.
        text = text.replace("\"", "\"\"");

        // Quote fields containing special characters.
        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")
                || text.contains("\r")) {

            return "\"" + text + "\"";
        }

        return text;
    }
}
