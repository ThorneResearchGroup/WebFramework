package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;

import static j2html.TagCreator.*;

public class ModalComponent {
    public static @NotNull DomContent render(String title, DomContent body, String id) {
        return render(title, body, null, null, id);
    }

    public static @NotNull DomContent render(String title, DomContent body, String apiLocation, String confirmButtonText, String id) {
        return div(
            a().withClass("modal-overlay").withHref("#modals").attr("aria-label", "Close"),
            div(
                div(
                    a().withClass("btn btn-clear float-right").withHref("#modals"),
                    div().withClass("modal-title h5").withText(title)
                ).withClass("modal-header"),
                body,
                div(
                    iff(apiLocation != null && confirmButtonText != null,
                        a(confirmButtonText).withClass("btn btn-link").withHref(apiLocation)
                    ),
                    a("Cancel").withClass("btn btn-link").withHref("#modals")
                ).withClass("modal-footer")
            ).withClass("modal-container")
        ).withClass("modal").withId(id);
    }
}
