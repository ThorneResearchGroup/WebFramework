package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import tech.tresearchgroup.palila.model.Card;

import java.util.List;

import static j2html.TagCreator.*;

public class EditableScrollingComponent {
    public static DomContent render(boolean editable, String title, List<Card> cards, String url, int size) {
        return render(editable, title, cards, url, false, size);
    }

    public static DomContent render(boolean editable, String title, List<Card> cards, String url, boolean fixedTitle, int size) {
        boolean goodCards = false;
        if (cards != null) {
            if (cards.size() > 0) {
                goodCards = true;
            }
        }
        if (editable) {
            if (goodCards) {
                return span(
                    br(),
                    label(title).withClass("topicLabel"),
                    br(),
                    a().withClass("btn floatRight fas fa-plus").withHref(url).withText("Add"),
                    br(),
                    div(each(cards, card -> PosterViewComponent.render(card, size))).withClass("scrolling-wrapper")
                );
            } else {
                return span(
                    br(),
                    label(title).withClass("topicLabel"),
                    br(),
                    a().withClass("btn floatRight fas fa-plus").withHref(url).withText("Add"),
                    br()
                );
            }
        }
        if (goodCards) {
            return span(
                br(),
                label(title).withClass("topicLabel"),
                br(),
                div(each(cards, card -> PosterViewComponent.render(card, size))).withClass("scrolling-wrapper")
            );
        } else {
            if (fixedTitle) {
                return span(
                    br(),
                    label(title).withClass("topicLabel")
                );
            }
            return null;
        }
    }
}
