package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;

import java.util.List;
import java.util.Objects;

import static j2html.TagCreator.*;

public class AutoCompleteDropDownBoxComponent {
    public static DomContent render(boolean editable, String title, String name, String selected, List<String> values) {
        if (selected == null) {
            selected = "";
        }
        String finalSelected = selected;
        return iffElse(editable,
            span(
                br(),
                label(title).withClass("subLabel"),
                br(),
                input().withName(name).withList(name + "-data"),
                datalist(
                    each(values, value ->
                        iffElse(Objects.equals(value.toLowerCase(), finalSelected.toLowerCase()),
                            option(value).isSelected(),
                            option(value)
                        )
                    )
                ).withId(name + "-data")
            ),
            iff(!finalSelected.equals("") && !finalSelected.equals("null"),
                span(
                    br(),
                    label(title).withClass("subLabel"),
                    br(),
                    input().withName(name).withValue(finalSelected)
                )
            )
        );
    }


    //Todo make the javascript wait until the user is done typing
    public static DomContent render(boolean editable, String title, String value, String name, String endpoint) {
        return html(
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").attr("onKeyUp", "showResults(this.value, '" + endpoint + "', '" + name + "')").withValue(value),
                        div().withId(name)
                    ),
                    html(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").attr("onKeyUp", "showResults(this.value, '" + endpoint + "', '" + name + "')"),
                        div().withId(name)
                    )
                ),
                iff(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        br(),
                        label(title).withClass("subLabel"),
                        br(),
                        input().withType("text").attr("onKeyUp", "showResults(this.value, '" + endpoint + "', '" + name + "')").withValue(value),
                        div().withId(name)
                    )
                )
            )
        );
    }
}
