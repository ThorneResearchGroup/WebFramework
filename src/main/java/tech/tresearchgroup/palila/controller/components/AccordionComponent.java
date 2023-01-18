package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;

import static j2html.TagCreator.*;

public class AccordionComponent {
    public static @NotNull DomContent render(String id, String text, DomContent content) {
        return div(
            input().withId(id).withType("radio").withName("accordion-radio").isHidden(),
            label(
                i().withClass("fas fa-arrow-alt-circle-down"),
                text(text)
            ).withClass("accordion-header c-hand").withFor(id),
            div(
                content
            ).withClass("accordion-body")
        ).withClass("accordion");
    }
}
