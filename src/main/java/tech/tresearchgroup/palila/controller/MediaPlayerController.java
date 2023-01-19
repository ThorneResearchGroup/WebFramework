package tech.tresearchgroup.palila.controller;

import j2html.tags.DomContent;
import tech.tresearchgroup.palila.model.entities.*;

import static j2html.TagCreator.*;

public class MediaPlayerController {
    public static DomContent getPlayer(Object object, String dataUrl) {
        Class theClass = object.getClass();
        if (AudioFileEntity.class.equals(theClass)) {
            return audio(
                source().withSrc(dataUrl).withType("audio/mp3"),
                text("Your browser does not support the audio tag")
            ).isControls();
        } else if (BookFileEntity.class.equals(theClass)) {
            return text("The book viewer is not yet implemented");
        } else if (GameFileEntity.class.equals(theClass)) {
            return text("The game emulator is not yet implemented");
        } else if (ImageFileEntity.class.equals(theClass)) {
            return img().withSrc(dataUrl);
        } else if (VideoFileEntity.class.equals(theClass)) {
            return video(
                source().withSrc(dataUrl).withType("video/mp4"),
                text("Your browser does not support the video tag")
            ).isControls().withHeight("%100").withWidth("%100");
        }
        return null;
    }
}
