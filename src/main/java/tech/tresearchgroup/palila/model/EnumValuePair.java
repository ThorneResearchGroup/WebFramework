package tech.tresearchgroup.palila.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EnumValuePair {
    private Enum key;
    private String value;
}
