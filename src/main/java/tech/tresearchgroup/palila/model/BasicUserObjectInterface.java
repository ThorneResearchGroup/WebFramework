package tech.tresearchgroup.palila.model;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;

public interface BasicUserObjectInterface extends BasicObjectInterface {

    @Serialize(order = 3)
    @SerializeNullable PermissionGroupEnum getPermissionGroup();

    @Serialize(order = 4)
    @SerializeNullable String getUsername();

    @Serialize(order = 5)
    @SerializeNullable String getEmail();

    @Serialize(order = 6)
    @SerializeNullable String getPassword();

    @Serialize(order = 7)
    @SerializeNullable String getApiKey();
}
