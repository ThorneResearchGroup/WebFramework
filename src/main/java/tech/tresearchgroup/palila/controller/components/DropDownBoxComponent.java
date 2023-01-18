package tech.tresearchgroup.palila.controller.components;

import j2html.tags.specialized.LabelTag;
import tech.tresearchgroup.palila.model.EnumValuePair;

import java.util.List;
import java.util.Objects;

import static j2html.TagCreator.*;

public class DropDownBoxComponent {
    public static LabelTag render(String title, String name, Enum selectedKey, List<EnumValuePair> values) {
        return label(
            text(title),
            select(
                each(values, value ->
                    iffElse(Objects.equals(value.getKey(), selectedKey),
                        option(value.getValue()).withValue(value.getKey().toString()).isSelected(),
                        option(value.getValue()).withValue(value.getKey().toString())
                    )
                )
            ).withName(name)
        );
    }
}
