package tech.tresearchgroup.palila.model;

import com.google.gson.annotations.JsonAdapter;
import tech.tresearchgroup.palila.model.adapters.ListIgnoreEmptyAdapter;

import java.util.List;

public class ResultEntity {
    private String name;
    @JsonAdapter(ListIgnoreEmptyAdapter.class)
    private List list;


    public ResultEntity(String objectClassName, List objectList) {
        this.name = objectClassName;
        this.list = objectList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
