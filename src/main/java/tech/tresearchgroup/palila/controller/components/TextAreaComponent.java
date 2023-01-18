package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;

import static j2html.TagCreator.*;

public class TextAreaComponent {
    public static DomContent render(boolean editable, String title, String value, String name, String rows, String columns) {
        return html(
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        label(title).withClass("subLabel"),
                        br(),
                        textarea().withName(name).withRows(rows).withCols(columns).withText(value)
                    )
                    ,
                    html(
                        label(title).withClass("subLabel"),
                        br(),
                        textarea().withName(name).withRows(rows).withCols(columns)
                    )
                ),
                iff(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        label(title).withClass("subLabel"),
                        br(),
                        textarea().withName(name).withRows(rows).withCols(columns).withText(value)
                    )
                )
            )
        );
    }
}
