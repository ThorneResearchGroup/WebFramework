package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;
import tech.tresearchgroup.palila.model.KeyValuePair;

import java.util.List;

import static j2html.TagCreator.*;

public class ChipsComponent {
    public static @NotNull DomContent render(boolean editable, String title, List<KeyValuePair> value, String id) {
        if (value != null) {
            for (KeyValuePair keyValuePair : value) {
                keyValuePair.setKey(id + "-" + keyValuePair.getKey());
            }
        }
        return html(
            iffElse(editable,
                iffElse(value != null,
                    html(
                        label(title).withClass("subLabel"),
                        br(),
                        each(value, ChipsComponent::renderIndividual)
                    ),
                    html(
                        label(title).withClass("subLabel")
                    )
                ),
                iff(value != null,
                    html(
                        label(title).withClass("subLabel"),
                        br(),
                        each(value, ChipsComponent::renderIndividual)
                    )
                )
            )
        );
    }

    private static @NotNull DomContent renderIndividual(KeyValuePair keyValuePair) {
        return html(
            input().withType("text").isHidden().withValue(keyValuePair.getValue()).withName(keyValuePair.getKey()),
            span(keyValuePair.getValue()).withClass("chip")
        );
    }
}
