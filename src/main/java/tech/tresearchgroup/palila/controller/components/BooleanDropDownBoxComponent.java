package tech.tresearchgroup.palila.controller.components;

import j2html.tags.specialized.LabelTag;

import java.util.Objects;

import static j2html.TagCreator.*;

public class BooleanDropDownBoxComponent {
    public static LabelTag render(String title, String name, Enum selectedKey) {
        return label(
            text(title),
            select(
                iffElse(Objects.equals("true", selectedKey.toString().toLowerCase()),
                    option("true").withValue("true").isSelected(),
                    option("false").withValue("false")
                )
            ).withName(name)
        );
    }
}
