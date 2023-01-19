package tech.tresearchgroup.palila.model.entities;

import com.google.gson.annotations.JsonAdapter;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.dao.model.LockType;
import tech.tresearchgroup.palila.model.BasicFormObject;
import tech.tresearchgroup.palila.model.adapters.LongIgnoreZeroAdapter;

import java.util.Date;

public class AudioFileEntity extends BasicFormObject implements BasicObjectInterface {

    private transient Date created;

    private transient Date updated;

    private Long id;

    private LockType lockType;
    @JsonAdapter(LongIgnoreZeroAdapter.class)
    private Long views;

    private String path;

    public AudioFileEntity() {
    }

    public AudioFileEntity(@Deserialize("created") Date created,
                           @Deserialize("updated") Date updated,
                           @Deserialize("id") Long id,
                           @Deserialize("lockType") LockType lockType,
                           @Deserialize("views") Long views,
                           @Deserialize("path") String path) {
        this.created = created;
        this.updated = updated;
        this.id = id;
        this.lockType = lockType;
        this.views = views;
        this.path = path;
    }

    @Serialize(order = 0)
    @SerializeNullable
    public Date getCreated() {
        return created;
    }

    @Serialize(order = 1)
    @SerializeNullable
    public Date getUpdated() {
        return updated;
    }

    @Serialize(order = 2)
    @SerializeNullable
    public Long getId() {
        return id;
    }

    @Serialize(order = 3)
    @SerializeNullable
    public LockType getLockType() {
        return lockType;
    }

    @Serialize(order = 4)
    @SerializeNullable
    public Long getViews() {
        return views;
    }

    @Serialize(order = 5)
    @SerializeNullable
    public String getPath() {
        return path;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
