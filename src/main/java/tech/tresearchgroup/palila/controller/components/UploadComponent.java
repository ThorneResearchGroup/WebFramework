package tech.tresearchgroup.palila.controller.components;

import j2html.tags.DomContent;
import tech.tresearchgroup.palila.controller.generators.UploadScriptGenerator;

import static j2html.TagCreator.*;

public class UploadComponent {
    public static DomContent render(boolean editable, String value, String name, String uploadPath) {
        return html(
            script(UploadScriptGenerator.getUploadScript(name, uploadPath)),
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        input().withType("file").withName(name).withId(name).withValue(value),
                        br(),
                        input().withType("button").withValue("Upload file").attr("onclick", "uploadToPath()"),
                        br(),
                        progress().withId(name + "ProgressBar").withValue("0").withMax("100"),
                        h3().withId("status"),
                        p().withId(name + "Total")
                    ),
                    html(
                        input().withType("file").withName(name).withId(name),
                        br(),
                        input().withType("button").withValue("Upload file").attr("onclick", "uploadToPath()"),
                        br(),
                        progress().withId(name + "ProgressBar").withValue("0").withMax("100"),
                        h3().withId("status"),
                        p().withId(name + "Total")
                    )
                ),
                iff(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        input().withType("file").withName(name).withId(name).withValue(value),
                        br(),
                        input().withType("button").withValue("Upload file").attr("onclick", "uploadToPath()"),
                        br(),
                        progress().withId(name + "ProgressBar").withValue("0").withMax("100"),
                        h3().withId("status"),
                        p().withId(name + "Total")
                    )
                )
            )
        );
    }

    public static DomContent render(boolean editable, String value, String name) {
        return html(
            iffElse(editable,
                iffElse(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        input().withType("file").withName(name).withId(name).withValue(value),
                        br(),
                        input().withType("button").withValue("Upload file").attr("onclick", "uploadFile('" + name + "')"),
                        br(),
                        progress().withId("progressBar").withValue("0").withMax("100"),
                        h3().withId("status"),
                        p().withId("loaded_n_total")
                    ),
                    html(
                        input().withType("file").withName(name).withId(name),
                        br(),
                        input().withType("button").withValue("Upload file").attr("onclick", "uploadFile('" + name + "')"),
                        br(),
                        progress().withId("progressBar").withValue("0").withMax("100"),
                        h3().withId("status"),
                        p().withId("loaded_n_total")
                    )
                ),
                iff(value != null && !value.equals("") && !value.equals("null"),
                    html(
                        input().withType("file").withName(name).withId(name).withValue(value),
                        br(),
                        input().withType("button").withValue("Upload file").attr("onclick", "uploadFile('" + name + "')"),
                        br(),
                        progress().withId("progressBar").withValue("0").withMax("100"),
                        h3().withId("status"),
                        p().withId("loaded_n_total")
                    )
                )
            )
        );
    }
}
