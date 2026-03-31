package com.todolist.api.security.store;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class JdbcTokenStateStore implements TokenStateStore {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTokenStateStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TokenMetadata findTokenByHash(String tokenHash) {
        List<TokenMetadata> result = jdbcTemplate.query(
                """
                SELECT token_hash, created_at, expires_at, revoked_at
                FROM security_tokens
                WHERE token_hash = ?
                """,
                (rs, rowNum) -> new TokenMetadata(
                        rs.getString("token_hash"),
                        OffsetDateTime.parse(rs.getString("created_at")),
                        OffsetDateTime.parse(rs.getString("expires_at")),
                        rs.getString("revoked_at") == null ? null : OffsetDateTime.parse(rs.getString("revoked_at"))
                ),
                tokenHash
        );
        return result.isEmpty() ? null : result.getFirst();
    }

    @Override
    public void insertTokenIfAbsent(TokenMetadata tokenMetadata) {
        jdbcTemplate.update(
                """
                INSERT INTO security_tokens(token_hash, created_at, expires_at, revoked_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(token_hash) DO NOTHING
                """,
                tokenMetadata.tokenHash(),
                tokenMetadata.createdAt().toString(),
                tokenMetadata.expiresAt().toString(),
                tokenMetadata.revokedAt() == null ? null : tokenMetadata.revokedAt().toString()
        );
    }

    @Override
    public void insertOrReplaceToken(TokenMetadata tokenMetadata) {
        jdbcTemplate.update(
                """
                INSERT INTO security_tokens(token_hash, created_at, expires_at, revoked_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(token_hash)
                DO UPDATE SET created_at = excluded.created_at,
                              expires_at = excluded.expires_at,
                              revoked_at = excluded.revoked_at
                """,
                tokenMetadata.tokenHash(),
                tokenMetadata.createdAt().toString(),
                tokenMetadata.expiresAt().toString(),
                tokenMetadata.revokedAt() == null ? null : tokenMetadata.revokedAt().toString()
        );
    }

    @Override
    public void revokeToken(String tokenHash, OffsetDateTime revokedAt) {
        jdbcTemplate.update(
                """
                UPDATE security_tokens
                SET revoked_at = ?
                WHERE token_hash = ?
                """,
                revokedAt.toString(),
                tokenHash
        );
    }

    @Override
    public int incrementAndGetRateCount(String tokenHash, long windowMinute) {
        int updated = jdbcTemplate.update(
                """
                UPDATE security_rate_windows
                SET request_count = request_count + 1
                WHERE token_hash = ? AND window_minute = ?
                """,
                tokenHash,
                windowMinute
        );

        if (updated == 0) {
            try {
                jdbcTemplate.update(
                        """
                        INSERT INTO security_rate_windows(token_hash, window_minute, request_count)
                        VALUES (?, ?, 1)
                        """,
                        tokenHash,
                        windowMinute
                );
            } catch (DuplicateKeyException ex) {
                jdbcTemplate.update(
                        """
                        UPDATE security_rate_windows
                        SET request_count = request_count + 1
                        WHERE token_hash = ? AND window_minute = ?
                        """,
                        tokenHash,
                        windowMinute
                );
            }
        }

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT request_count
                FROM security_rate_windows
                WHERE token_hash = ? AND window_minute = ?
                """,
                Integer.class,
                tokenHash,
                windowMinute
        );
        return count == null ? 0 : count;
    }
}
