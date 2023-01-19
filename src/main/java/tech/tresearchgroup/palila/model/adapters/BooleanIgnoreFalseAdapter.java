package tech.tresearchgroup.palila.model.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class BooleanIgnoreFalseAdapter extends TypeAdapter<Boolean> {
    private static final boolean BOOLEAN_FALSE = false;

    @Override
    public Boolean read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return false;
        }
        return true;
    }

    @Override
    public void write(JsonWriter out, Boolean data) throws IOException {
        if (data == null || data.equals(BOOLEAN_FALSE)) {
            out.nullValue();
            return;
        }
        out.value(data.booleanValue());
    }
}