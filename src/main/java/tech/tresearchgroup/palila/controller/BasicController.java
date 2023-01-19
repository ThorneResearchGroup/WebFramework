package tech.tresearchgroup.palila.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.activej.csp.file.ChannelFileWriter;
import io.activej.http.HttpHeaders;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.http.MultipartDecoder;
import io.activej.promise.Promisable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tresearchgroup.cao.controller.GenericCAO;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicUserObjectInterface;
import tech.tresearchgroup.palila.model.entities.*;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;
import tech.tresearchgroup.palila.model.enums.PlaybackQualityEnum;
import tech.tresearchgroup.palila.model.enums.ReturnType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class BasicController extends HttpResponses {
    private static final Logger logger = LoggerFactory.getLogger(BasicController.class);
    private final Algorithm algorithm = Algorithm.HMAC256(BaseSettings.secretKey);
    private final JWTVerifier verifier = JWT.require(algorithm).withIssuer(BaseSettings.issuer).build();

    public boolean canAccess(HttpRequest httpRequest, PermissionGroupEnum permissionGroupEnum, BasicUserController basicUserController) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, JWTVerificationException, IOException {
        if (permissionGroupEnum.equals(PermissionGroupEnum.ALL)) {
            return true;
        }
        if (!BaseSettings.enableSecurity) {
            return true;
        }
        try {
            BasicUserObjectInterface basicUserObjectInterface = (BasicUserObjectInterface) getUser(httpRequest, basicUserController);
            if (basicUserObjectInterface == null) {
                if (BaseSettings.debug) {
                    logger.info("Null user object interface");
                }
                return false;
            }
            if (BaseSettings.debug) {
                logger.info("Checking authorization for: " + basicUserObjectInterface.getUsername());
            }
            return isPermitted(permissionGroupEnum, basicUserObjectInterface) && verifyApiKey(httpRequest);
        } catch (JWTVerificationException e) {
            return false;
        }
    }


    public Long getUserId(HttpRequest httpRequest) throws JWTVerificationException {
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
            } catch (JWTVerificationException e) {
                if (BaseSettings.debug) {
                    logger.info("Invalid JWT");
                }
                throw e;
            }
        }
        if (BaseSettings.debug) {
            logger.info("No JWT set");
        }
        //throw new JWTVerificationException("No JWT set");
        return 0L;
    }

    protected Object getUser(HttpRequest httpRequest, BasicUserController genericController) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        Long userId = getUserId(httpRequest);
        if (userId != null && userId != 0 && userId > 0) {
            return genericController.readSecureResponse(userId, ReturnType.OBJECT, httpRequest);
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
                if (BaseSettings.debug) {
                    logger.info("Didn't have required perm");
                }
                return false;
            }
        }
    }

    public GenericController getController(Class theClass, Map<String, GenericController> controllers) {
        String className = theClass.getSimpleName().toLowerCase() + "controller";
        for (Map.Entry<String, GenericController> entry : controllers.entrySet()) {
            if (entry.getKey().equals(className)) {
                return entry.getValue();
            }
        }
        logger.info("Unsupported controller: " + className);
        return null;
    }

    public HttpResponse handle206(Path path, int chunkSize, HttpRequest httpRequest) throws IOException {
        if (path.toFile().exists()) {
            long fileSize = Files.size(path);
            String range = httpRequest.getHeader(HttpHeaders.RANGE);
            if (range != null) {
                String[] ranges = range.replace("bytes=", "").split("-");
                long startValue = Long.parseLong(ranges[0]);
                if (ranges.length == 1) {
                    long end = startValue + chunkSize;
                    if (end > fileSize) {
                        end = fileSize;
                    }
                    return HttpResponse.ok206().withHeader(HttpHeaders.CONTENT_DISPOSITION, "inline").withHeader(HttpHeaders.CONTENT_TYPE, "multipart/byteranges").withHeader(HttpHeaders.CONTENT_RANGE, "bytes " + startValue + "-" + (fileSize - 1) + "/" + fileSize).withBody(FileSystemController.readByteRange(path, startValue, end));
                }
                long endValue = Long.parseLong(ranges[1]);
                //if (CacheController.existsInCache(startValue, endValue, videoId)) {
                //    CachedEntity cachedEntity = CacheController.get(Integer.parseInt(start), endValue, videoId);
                //    return HttpResponse.ok206().withHeader(HttpHeaders.CONTENT_RANGE, "bytes " + startValue + "-" + endValue + "/" + fileSize).withBody(cachedEntity.getData());
                //}
                //CacheController.put(startValue, endValue, videoId, data);
                if (endValue > (startValue + chunkSize)) {
                    endValue = (startValue + chunkSize);
                }
                if (endValue > fileSize) {
                    endValue = fileSize;
                }
                return HttpResponse
                    .ok206()
                    .withHeader(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .withHeader(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .withHeader(HttpHeaders.CONTENT_RANGE, "bytes " + startValue + "-" + endValue + "/" + fileSize)
                    .withBody(FileSystemController.readByteRange(path, startValue, endValue));
            }
            //if (CacheController.existsInCache(0, CHUNK, videoId)) {
            //    return HttpResponse.ok206().withHeader(HttpHeaders.CONTENT_RANGE, "bytes " + 0 + "-" + CHUNK + "/" + fileSize).withBody(CacheController.get(0, CHUNK, videoId).getData());
            //}
            //CacheController.put(0, CHUNK, videoId, data);
            return HttpResponse.ok206()
                .withHeader(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .withHeader(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .withHeader(HttpHeaders.CONTENT_RANGE, "bytes " + 0 + "-" + chunkSize + "/" + fileSize)
                .withBody(FileSystemController.readByteRange(path, 0, chunkSize));
        } else {
            return notFound();
        }
    }

    public @NotNull Promisable<HttpResponse> handleUpload(String mediaType,
                                                          String varName,
                                                          String[] entityPackages,
                                                          String library,
                                                          Map<String, GenericController> controllers,
                                                          @NotNull HttpRequest httpRequest,
                                                          GenericCAO genericCAO) throws ClassNotFoundException {
        Class mediaClass = ReflectionMethods.findClass(mediaType, entityPackages, genericCAO);
        if (mediaClass != null) {
            if (varName != null) {
                UUID uuid = UUID.randomUUID();
                Path file = new File(library + "/" + mediaType + "/" + uuid + ".tmp").toPath();
                File uploadDir = new File(library + "/" + mediaClass.getSimpleName().toLowerCase());
                if (!uploadDir.exists()) {
                    if (!uploadDir.mkdirs()) {
                        return error();
                    }
                }
                return httpRequest.handleMultipart(MultipartDecoder.MultipartDataHandler.file(fileName ->
                        ChannelFileWriter.open(newSingleThreadExecutor(), new File(library + "/" + mediaClass.getSimpleName().toLowerCase() + "/" + uuid + ".tmp").toPath())))
                    .map($ -> finalizeUpload(mediaClass, varName, file, entityPackages, library, controllers, httpRequest, genericCAO));
            }
        }
        return error();
    }

    private HttpResponse finalizeUpload(Class mediaClass,
                                        String varName,
                                        Path file,
                                        String[] entityPackages,
                                        String library,
                                        Map<String, GenericController> controllers,
                                        HttpRequest httpRequest,
                                        GenericCAO genericCAO) {
        try {
            Field field = mediaClass.getDeclaredField(varName);
            Class fieldClass = field.getType();
            if (field.getType().equals(List.class)) {
                fieldClass = ReflectionMethods.getListClass(field, entityPackages, genericCAO);
            }
            Object classObject = ReflectionMethods.getNewInstance(mediaClass);
            Method setTitle = classObject.getClass().getMethod("setTitle", String.class);
            setTitle.invoke(classObject, file.getFileName().toString());
            if (AudioFileEntity.class.equals(fieldClass)) {
                AudioFileEntity audioFileEntity = new AudioFileEntity();
                audioFileEntity.setPath(library + "/" + mediaClass.getSimpleName().toLowerCase() + "/" + file.getFileName());
                Method setter = ReflectionMethods.getSetter(field, classObject.getClass(), AudioFileEntity.class);
                setter.invoke(classObject, audioFileEntity);
            } else if (BookFileEntity.class.equals(fieldClass)) {
            } else if (FileEntity.class.equals(fieldClass)) {
            } else if (GameFileEntity.class.equals(fieldClass)) {
            } else if (ImageFileEntity.class.equals(fieldClass)) {
            } else if (VideoFileEntity.class.equals(fieldClass)) {
                VideoFileEntity videoFileEntity = new VideoFileEntity();
                videoFileEntity.setPath(library + "/" + mediaClass.getSimpleName().toLowerCase() + "/" + file.getFileName());
                videoFileEntity.setPlaybackQualityEnum(PlaybackQualityEnum.ORIGINAL);
                List list = new LinkedList();
                list.add(videoFileEntity);
                Method setter = ReflectionMethods.getSetter(field, classObject.getClass(), List.class);
                setter.invoke(classObject, list);
            }
            GenericController genericController = getController(classObject.getClass(), controllers);
            if (genericController != null) {
                if (genericController.createSecureResponse(classObject, ReturnType.OBJECT, httpRequest) != null) {
                    return ok();
                } else {
                    return error();
                }
            }
            return redirect("/browse/" + mediaClass.getSimpleName().toLowerCase());
        } catch (Exception e) {
            if (!file.toFile().delete()) {
                logger.error("Failed to delete: " + file.toFile().getAbsolutePath());
            }
        }
        return redirect("/error");
    }
}
