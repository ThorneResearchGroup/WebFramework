package tech.tresearchgroup.palila.model;

import tech.tresearchgroup.cao.model.CacheMethodEnum;
import tech.tresearchgroup.dao.model.DatabaseTypeEnum;
import tech.tresearchgroup.palila.model.enums.CompressionMethodEnum;
import tech.tresearchgroup.sao.model.SearchMethodEnum;

public class BaseSettings {
    public static boolean debug;
    public static boolean maintenanceMode;
    public static boolean enableSecurity;
    public static String issuer;
    public static String secretKey;
    public static String serverName;
    public static String serverFaviconLocation;
    public static CompressionMethodEnum compressionMethod;
    public static int compressionQuality;
    public static SearchMethodEnum searchMethod;
    public static String searchHost;
    public static String searchKey;
    public static CacheMethodEnum cacheMethodEnum;
    public static boolean cacheEnable;

    public static int maxAssetCacheAge;
    public static long apiCacheSize;
    public static long databaseCacheSize;
    public static long pageCacheSize;
    public static int maxSearchResults;
    public static int maxAPIBrowseResults;
    public static int maxUIBrowseResults;
    public static String databaseName;
    public static DatabaseTypeEnum databaseType;
    public static int minDatabaseConnections;
    public static int maxDatabaseConnections;
    public static boolean loggingEnabled;
    public static String baseLibraryPath;
    public static int cardWidth;
    public static int chunk = 1024 * 2048;
}
