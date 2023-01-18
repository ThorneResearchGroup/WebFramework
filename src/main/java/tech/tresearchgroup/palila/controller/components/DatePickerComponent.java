package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;

import static j2html.TagCreator.*;

public class DatePickerComponent {
    public static DomContent render(boolean editable, String title, String value, String name) {
        return html(
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("date").withName(name).withValue(value)
                    )
                    ,
                    html(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("date").withName(name)
                    )
                ),
                iff(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("date").withName(name).withValue(value)
                    )
                )
            )
        );
    }
}
