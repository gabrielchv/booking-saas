package dev.gazerah.booking.common;

/**
 * Holds the id of the tenant the current request operates on. Populated by the
 * JWT filter from the token's tenant claim, cleared after the request.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long get() {
        return TENANT_ID.get();
    }

    public static Long require() {
        Long tenantId = TENANT_ID.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant in the current request context");
        }
        return tenantId;
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
