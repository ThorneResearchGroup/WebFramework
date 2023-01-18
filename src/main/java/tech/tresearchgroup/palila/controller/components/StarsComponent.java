package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import j2html.tags.specialized.ButtonTag;
import tech.tresearchgroup.palila.controller.generators.StarsScriptGenerator;

import static j2html.TagCreator.*;

public class StarsComponent {
    public static DomContent render(boolean editable, String title, String value, String name) {
        return html(
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("0") && !value.equals("null"),
                    div(
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").isHidden().withName(name + "-value"),
                        addButton("1", value),
                        addButton("2", value),
                        addButton("3", value),
                        addButton("4", value),
                        addButton("5", value)
                    ).withId(name).withClass("rating")
                    ,
                    div(
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").isHidden().withId(name + "-value").withName(name + "-value"),
                        addButton("1", null),
                        addButton("2", null),
                        addButton("3", null),
                        addButton("4", null),
                        addButton("5", null)
                    ).withId(name).withClass("rating")
                ),
                iff(value != null && !value.equals("") && !value.equals("0") && !value.equals("null"),
                    div(
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").isHidden().withName(name + "-value"),
                        addButton("1", value),
                        addButton("2", value),
                        addButton("3", value),
                        addButton("4", value),
                        addButton("5", value)
                    ).withId(name).withClass("rating")
                )
            ),
            script(StarsScriptGenerator.getStarsScript(name))
        );
    }

    private static ButtonTag addButton(String value, String selected) {
        if (value != null) {
            if (value.equals(selected)) {
                return button("★").attr("data-star", value).with(span(value + " Star").withClass("screen-reader")).withClass("star is-active");
            }
        }
        return button("★").attr("data-star", value).with(span(value + " Star").withClass("screen-reader")).withClass("star");
    }
}
