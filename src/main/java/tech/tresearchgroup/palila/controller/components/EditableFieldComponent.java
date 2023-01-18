package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;

import static j2html.TagCreator.*;

public class EditableFieldComponent {
    public static @NotNull DomContent render(boolean editable, String title, String value, String name) {
        return html(
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("0") && !value.equals("null"),
                    span(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").withValue(value).withName(name)
                    ),
                    span(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").withName(name)
                    )
                ),
                iff(value != null && !value.equals("") && !value.equals("0") && !value.equals("null"),
                    span(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        label(value)
                    )
                )
            )
        );
    }
}
