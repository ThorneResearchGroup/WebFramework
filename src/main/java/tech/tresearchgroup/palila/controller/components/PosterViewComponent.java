package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;
import tech.tresearchgroup.palila.model.Card;

import static j2html.TagCreator.*;

public class PosterViewComponent {
    public static @NotNull DomContent render(Card card, int size) {
        return div(
            div(
                input().withValue(card.getPosterLocation()).withName(card.getClassName() + "-" + card.getId() + "-poster").isHidden(),
                img().withAlt("blah").withClass("img-responsive").withSrc(card.getPosterLocation()).withWidth("%100").withHeight("%100"),
                SelectCheckboxComponent.render("browseSelectCheckbox", "checkbox-" + card.getAction() + card.getClassName() + "-" + card.getId()),
                a(
                    i().withClass("play-button fas fa-play")
                ).withHref("/play/" + card.getClassName() + "/" + card.getId()),
                a(
                    i().withClass("edit-button fas fa-edit")
                ).withHref("/edit/" + card.getClassName() + "/" + card.getId()),
                a(
                    i().withClass("delete-button fas fa-trash-alt")
                ).withHref("#delete-" + card.getClassName() + "-" + card.getId()),
                ModalComponent.render(
                    "Confirm deletion",
                    span(
                        text("Are you sure you want to delete: " + card.getTitle()),
                        br(),
                        br(),
                        text("This action cannot be undone!")
                    ),
                    "/delete/" + card.getClassName() + "/" + card.getId(),
                    "DELETE",
                    "delete-" + card.getClassName() + "-" + card.getId()
                )
            ).withClass("card-image"),
            div(
                div(
                    input().withValue(card.getTitle()).withName(card.getClassName() + "-" + card.getId() + "-title").isHidden(),
                    a(card.getTitle()).withHref("/view/" + card.getClassName() + "/" + card.getId())
                ).withClass("card-title"),
                br(),
                input().withValue(card.getTopLeft()).withName(card.getClassName() + "-" + card.getId() + "-releaseDate").isHidden(),
                div(card.getTopLeft()).withClass("card-release-date"),
                input().withValue(card.getTopRight()).withName(card.getClassName() + "-" + card.getId() + "-mpaaRating").isHidden(),
                div(card.getTopRight()).withClass("card-mpaa-rating"),
                br(),
                input().withValue(card.getBottomLeft()).withName(card.getClassName() + "-" + card.getId() + "-runTime").isHidden(),
                div(card.getBottomLeft()).withClass("card-runtime"),
                input().withValue(card.getBottomRight()).withName(card.getClassName() + "-" + card.getId() + "-userRating").isHidden(),
                div(card.getBottomRight()).withClass("card-user-rating"),
                br()
            ).withClass("card-header")
        ).withClass("card").withStyle("width: " + size + "px;");
    }
}
