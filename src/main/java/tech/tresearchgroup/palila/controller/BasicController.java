package tech.tresearchgroup.palila.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.activej.http.HttpHeaders;
import io.activej.http.HttpRequest;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicUserObjectInterface;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;

public class BasicController extends HttpResponses {
    private final Algorithm algorithm = Algorithm.HMAC256(BaseSettings.secretKey);
    private final JWTVerifier verifier = JWT.require(algorithm).withIssuer(BaseSettings.issuer).build();

    public boolean canAccess(HttpRequest httpRequest, PermissionGroupEnum permissionGroupEnum, BasicUserController basicUserController) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (permissionGroupEnum.equals(PermissionGroupEnum.ALL)) {
            return true;
        }
        if (!BaseSettings.enableSecurity) {
            return true;
        }
        BasicUserObjectInterface basicUserObjectInterface = (BasicUserObjectInterface) getUser(httpRequest, basicUserController);
        if (basicUserObjectInterface == null) {
            return false;
        }
        if (BaseSettings.debug) {
            System.out.println("Checking authorization for: " + basicUserObjectInterface.getUsername());
        }
        return isPermitted(permissionGroupEnum, basicUserObjectInterface) && verifyApiKey(httpRequest);
    }


    public Long getUserId(HttpRequest httpRequest) {
        if (!BaseSettings.enableSecurity) {
            return 0L;
        }
        String jwt = getJwt(httpRequest);
        if (jwt != null) {
            try {
                DecodedJWT decodedJWT = verifier.verify(jwt);
                return Long.valueOf(decodedJWT.getSubject());
            } catch (MissingClaimException e) {
                if (BaseSettings.debug) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return null;
    }

    protected Object getUser(HttpRequest httpRequest, BasicUserController genericController) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Long userId = getUserId(httpRequest);
        if (userId != null && userId != 0 && userId > 0) {
            return genericController.readSecureResponse(userId, httpRequest);
        }
        return null;
    }

    public boolean verifyApiKey(HttpRequest httpRequest) {
        if (!BaseSettings.enableSecurity) {
            return true;
        }
        try {
            String jwt = getJwt(httpRequest);
            if (jwt != null) {
                verifier.verify(jwt);
                return true;
            }
        } catch (JWTVerificationException exception) {
            if (BaseSettings.debug) {
                exception.printStackTrace();
            }
            return false;
        }
        return false;
    }

    String getJwt(HttpRequest httpRequest) {
        String jwt = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (jwt == null) {
            jwt = httpRequest.getCookie("authorization");
        }
        return jwt;
    }

    public String generateKey(long userId) {
        return JWT.create()
            .withIssuer(BaseSettings.issuer)
            .withSubject(String.valueOf(userId))
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .sign(algorithm);
    }

    private boolean isPermitted(PermissionGroupEnum requiredPermission, BasicUserObjectInterface userEntity) {
        switch (requiredPermission) {
            case OPERATOR -> {
                return userEntity.getPermissionGroup().equals(PermissionGroupEnum.OPERATOR);
            }
            case ADMINISTRATOR -> {
                return userEntity.getPermissionGroup().equals(PermissionGroupEnum.OPERATOR) ||
                    userEntity.getPermissionGroup().equals(PermissionGroupEnum.ADMINISTRATOR);
            }
            case MODERATOR -> {
                return userEntity.getPermissionGroup().equals(PermissionGroupEnum.OPERATOR) ||
                    userEntity.getPermissionGroup().equals(PermissionGroupEnum.ADMINISTRATOR) ||
                    userEntity.getPermissionGroup().equals(PermissionGroupEnum.MODERATOR);
            }
            case USER -> {
                return userEntity.getPermissionGroup().equals(PermissionGroupEnum.OPERATOR) ||
                    userEntity.getPermissionGroup().equals(PermissionGroupEnum.ADMINISTRATOR) ||
                    userEntity.getPermissionGroup().equals(PermissionGroupEnum.MODERATOR) ||
                    userEntity.getPermissionGroup().equals(PermissionGroupEnum.USER);
            }
            case ALL -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
