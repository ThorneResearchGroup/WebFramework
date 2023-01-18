package tech.tresearchgroup.palila.model;

import tech.tresearchgroup.palila.controller.ReflectionMethods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BasicFormObject {
    public Card toCard() throws InvocationTargetException, IllegalAccessException {
        Class theClass = this.getClass();
        Method getId = ReflectionMethods.getId(theClass);
        Card card = new Card();
        card.setId((Long) getId.invoke(this));
        card.setPosterLocation("/assets/poster.png");
        card.setMediaType(theClass.getSimpleName().toLowerCase());
        return card;
    }
}
