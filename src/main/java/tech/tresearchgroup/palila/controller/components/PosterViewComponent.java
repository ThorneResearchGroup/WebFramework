package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;
import tech.tresearchgroup.palila.model.Card;

import static j2html.TagCreator.*;

public class PosterViewComponent {
    public static @NotNull DomContent render(Card card, int size) {
        return div(
            div(
                input().withValue(card.getPosterLocation()).withName(card.getMediaType() + "-" + card.getId() + "-poster").isHidden(),
                img().withAlt("blah").withClass("img-responsive").withSrc(card.getPosterLocation()).withWidth("%100").withHeight("%100"),
                SelectCheckboxComponent.render("browseSelectCheckbox", "checkbox-" + card.getType() + card.getMediaType() + "-" + card.getId()),
                a(
                    i().withClass("play-button fas fa-play")
                ).withHref("/play/" + card.getMediaType() + "/" + card.getId()),
                a(
                    i().withClass("edit-button fas fa-edit")
                ).withHref("/edit/" + card.getMediaType() + "/" + card.getId()),
                a(
                    i().withClass("delete-button fas fa-trash-alt")
                ).withHref("#delete-" + card.getType() + card.getMediaType() + "-" + card.getId() + "-modal"),
                ModalComponent.render(
                    "Confirm deletion",
                    span(
                        text("Are you sure you want to delete: " + card.getTitle()),
                        br(),
                        br(),
                        text("This action cannot be undone!")
                    ),
                    "/api/delete/" + card.getMediaType() + "?id=" + card.getId(),
                    "DELETE",
                    "delete-" + card.getType() + card.getMediaType() + "-" + card.getId() + "-modal"
                )
            ).withClass("card-image"),
            div(
                div(
                    input().withValue(card.getTitle()).withName(card.getMediaType() + "-" + card.getId() + "-title").isHidden(),
                    a(card.getTitle()).withHref("/view/" + card.getMediaType() + "/" + card.getId())
                ).withClass("card-title"),
                br(),
                input().withValue(card.getReleaseDate()).withName(card.getMediaType() + "-" + card.getId() + "-releaseDate").isHidden(),
                div(card.getReleaseDate()).withClass("card-release-date"),
                input().withValue(card.getMpaaRating()).withName(card.getMediaType() + "-" + card.getId() + "-mpaaRating").isHidden(),
                div(card.getMpaaRating()).withClass("card-mpaa-rating"),
                br(),
                input().withValue(card.getRuntime()).withName(card.getMediaType() + "-" + card.getId() + "-runTime").isHidden(),
                div(card.getRuntime()).withClass("card-runtime"),
                input().withValue(card.getUserRating()).withName(card.getMediaType() + "-" + card.getId() + "-userRating").isHidden(),
                div(card.getUserRating()).withClass("card-user-rating"),
                br()
            ).withClass("card-header")
        ).withClass("card").withStyle("width: " + size + "px;");
    }
}
