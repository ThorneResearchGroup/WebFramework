package tech.tresearchgroup.palila.controller.components;

import j2html.tags.specialized.LabelTag;

import static j2html.TagCreator.*;

public class InputBoxComponent {
    public static LabelTag render(String name, String text) {
        return render(name, text, "");
    }

    public static LabelTag render(String name, String text, String value) {
        return label(
            text(text),
            input().withType("text").withValue(value).withName(name)
        );
    }
}
