package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;

import static j2html.TagCreator.html;
import static j2html.TagCreator.input;

public class MediaTypeFieldComponent {
    public static DomContent render(Class theClass) {
        return html(input().withName("mediaClassName").withValue(theClass.getSimpleName().toLowerCase()).isHidden());
    }
}
