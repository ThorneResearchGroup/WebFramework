package tech.tresearchgroup.palila.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Card {
    private long id;
    private String posterLocation;
    private String type;
    private String mediaType;
    private String title;
    private String releaseDate;
    private String mpaaRating;
    private String runtime;
    private String userRating;
}
