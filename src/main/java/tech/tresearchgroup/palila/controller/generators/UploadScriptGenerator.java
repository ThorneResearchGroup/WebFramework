package tech.tresearchgroup.palila.controller.generators;

public class UploadScriptGenerator {
    public static String getUploadScript(String name, String path) {
        return "function uploadToPath() {" +
            "    var file = document.getElementById('" + name + "').files[0];" +
            "    var formdata = new FormData();" +
            "    formdata.append(" + name + ", file);" +
            "    var ajax = new XMLHttpRequest();" +
            "    ajax.upload.addEventListener('" + name + " + Progress', " + name + "ProgressHandler, false);" +
            "    ajax.addEventListener('" + name + " + Load', " + name + "CompleteHandler, false);" +
            "    ajax.addEventListener('" + name + " + Error', " + name + "ErrorHandler, false);" +
            "    ajax.addEventListener('" + name + " + Abort', " + name + "AbortHandler, false);" +
            "    ajax.open('POST', '" + path + "');" +
            "    ajax.send(formdata);" +
            "}" +
            "" +
            "function " + name + "ProgressHandler(event) {" +
            "    document.getElementById('" + name + "Total').innerHTML = 'Uploaded ' + event.loaded + ' bytes of ' + event.total;" +
            "    var percent = (event.loaded / event.total) * 100;" +
            "    document.getElementById('" + name + "ProgressBar').value = Math.round(percent);" +
            "    document.getElementById('" + name + "Status').innerHTML = Math.round(percent) + '% uploaded... please wait';" +
            "}" +
            "" +
            "function " + name + "CompleteHandler(event) {" +
            "    document.getElementById('" + name + "Status').innerHTML = event.target.responseText;" +
            "    document.getElementById('" + name + "ProgressBar').value = 0;" +
            "}" +
            "" +
            "function " + name + "ErrorHandler(event) {" +
            "    document.getElementById('" + name + "Status').innerHTML = 'Upload Failed';" +
            "}" +
            "" +
            "function " + name + "AbortHandler(event) {" +
            "    document.getElementById('" + name + "Status').innerHTML = 'Upload Aborted';" +
            "}";
    }

    public static String getUploadImageDisplayScript(String imageSelectName, String displayName) {
        return "const image_input = document.getElementById('" + imageSelectName + "');" +
            "" +
            "image_input.addEventListener('change', function() {" +
            "  const reader = new FileReader();" +
            "  reader.addEventListener('load', () => {" +
            "    const uploaded_image = reader.result;" +
            "    document.getElementById('" + displayName + "').style.backgroundImage = `url(${uploaded_image})`;" +
            "  });" +
            "  reader.readAsDataURL(this.files[0]);" +
            "});";
    }
}
