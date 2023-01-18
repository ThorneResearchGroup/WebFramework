package tech.tresearchgroup.palila.controller.components;

import j2html.tags.specialized.LabelTag;

import static j2html.TagCreator.*;

public class CheckboxComponent {
    public static LabelTag render(String name, String text) {
        return render(name, text, false);
    }

    public static LabelTag render(String name, String text, boolean checked) {
        return label(
            text(text),
            iffElse(checked,
                input().withType("checkbox").withName(name).isChecked(),
                input().withType("checkbox").withName(name)
            )
        );
    }
}
