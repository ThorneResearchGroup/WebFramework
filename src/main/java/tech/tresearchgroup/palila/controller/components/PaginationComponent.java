package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import org.jetbrains.annotations.NotNull;

import static j2html.TagCreator.*;

public class PaginationComponent {
    public static @NotNull DomContent render(int currentPage, long maxPage, String prefix) {
        if (maxPage > 0) {
            boolean canShowPrevious = currentPage > 0;
            boolean canShowNext = (maxPage - currentPage) >= 1;
            boolean canShowOne = currentPage >= 2;
            boolean canShowTwo = currentPage >= 1;
            boolean canShowThree = (maxPage - currentPage) >= 1;
            boolean canShowFour = (maxPage - currentPage) >= 2;
            return ul(
                iffElse(canShowPrevious,
                    li(
                        a("Previous").withHref(prefix + "?page=" + (currentPage - 1))
                    ).withClass("page-item"),
                    li(
                        a("Previous")
                    ).withClass("page-item disabled")
                ),
                iff(canShowOne,
                    li(
                        a(String.valueOf((currentPage - 2))).withHref(prefix + "?page=" + (currentPage - 2))
                    ).withClass("page-item")
                ),
                iff(canShowTwo,
                    li(
                        a(String.valueOf((currentPage - 1))).withHref(prefix + "?page=" + (currentPage - 1))
                    ).withClass("page-item")
                ),
                li(
                    a(String.valueOf(currentPage)).withHref(prefix + "?page=" + currentPage)
                ).withClass("page-item active"),
                iff(canShowThree,
                    li(
                        a(String.valueOf((currentPage + 1))).withHref(prefix + "?page=" + (currentPage + 1))
                    ).withClass("page-item")
                ),
                iff(canShowFour,
                    li(
                        a(String.valueOf((currentPage + 2))).withHref(prefix + "?page=" + (currentPage + 2))
                    ).withClass("page-item")
                ),
                iffElse(canShowNext,
                    li(
                        a("Next").withHref(prefix + "?page=" + (currentPage + 1))
                    ).withClass("page-item"),
                    li(
                        a("Next")
                    ).withClass("page-item disabled")
                )
            ).withClass("pagination");
        } else {
            return ul().withClass("pagination");
        }
    }
}
