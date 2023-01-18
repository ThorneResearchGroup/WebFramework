package tech.tresearchgroup.palila.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CachedEntity implements Serializable {
    private String date;
    private byte[] data;
}
