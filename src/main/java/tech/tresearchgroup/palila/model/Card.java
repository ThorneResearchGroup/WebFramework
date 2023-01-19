package tech.tresearchgroup.palila.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class Card {
    private long id;
    private String posterLocation;
    private String action;
    private String className;
    private String title;
    private String topLeft;
    private String topRight;
    private String bottomLeft;
    private String bottomRight;

    public Card(String posterLocation, String title, String topLeft, String topRight, String bottomLeft, String bottomRight) {
        this.posterLocation = posterLocation;
        this.title = title;
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPosterLocation() {
        return posterLocation;
    }

    public void setPosterLocation(String posterLocation) {
        this.posterLocation = posterLocation;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(String topLeft) {
        this.topLeft = topLeft;
    }

    public String getTopRight() {
        return topRight;
    }

    public void setTopRight(String topRight) {
        this.topRight = topRight;
    }

    public String getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(String bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public String getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(String bottomRight) {
        this.bottomRight = bottomRight;
    }

    @Override
    public String toString() {
        return "Card{" +
            "id=" + id +
            ", posterLocation='" + posterLocation + '\'' +
            ", action='" + action + '\'' +
            ", className='" + className + '\'' +
            ", title='" + title + '\'' +
            ", topLeft='" + topLeft + '\'' +
            ", topRight='" + topRight + '\'' +
            ", bottomLeft='" + bottomLeft + '\'' +
            ", bottomRight='" + bottomRight + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return id == card.id && Objects.equals(posterLocation, card.posterLocation) && Objects.equals(action, card.action) && Objects.equals(className, card.className) && Objects.equals(title, card.title) && Objects.equals(topLeft, card.topLeft) && Objects.equals(topRight, card.topRight) && Objects.equals(bottomLeft, card.bottomLeft) && Objects.equals(bottomRight, card.bottomRight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, posterLocation, action, className, title, topLeft, topRight, bottomLeft, bottomRight);
    }
}
