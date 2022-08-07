package mathrone.backend.config;

import lombok.Getter;

@Getter
public class CacheKey {
    public static final String USER = "user";
    public static final String RefreshToken = "RefreshToken";
    public static final int DEFAULT_EXPIRE_SEC = 60;
}
