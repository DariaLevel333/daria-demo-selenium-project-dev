package org.levelci.selenium.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors a JS {@code Error} on {@link AnalysisResult} when {@code error} is set.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisError {

    @JsonProperty
    private String name;

    @JsonProperty
    private String message;

    /** Stack trace string when available. */
    @JsonProperty
    private String stack;

    public static AnalysisError of(String message) {
        return AnalysisError.builder().name("Error").message(message).build();
    }
}
